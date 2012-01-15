package tu.space.light;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamList;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.contracts.PcSpec;
import tu.space.middleware.CpuInput;
import tu.space.middleware.Input;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;
import tu.space.middleware.OrderItemListener;
import tu.space.middleware.Output;
import tu.space.middleware.RamInput;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;

public class SpaceMiddleware implements Middleware {
	public SpaceMiddleware( int port ) {
		space = URI.create( "xvsm://localhost:" + port );
		core  = DefaultMzsCore.newInstance( 0 );
		capi  = new Capi( core );
		crefs = new SpaceContainers( capi, space );
		nm    = new NotificationManager( core );
		
		ContainerReference os    = crefs.getOrders();
		ContainerReference specs = crefs.getPcSpecs();
		
		orderOut = new SpaceOutput<Order>( os ) {
			@Override
			protected void coordData( Order o, List<CoordinationData> l ) {
				l.add( KeyCoordinator.newCoordinationData( o.id ) );
			}
		};
		pcSpecOut = new SpaceOutput<PcSpec>( specs ) {
			@Override
			protected void coordData( PcSpec p, List<CoordinationData> l ) {
				l.add( LabelCoordinator.newCoordinationData( p.orderId ) );
			}			
		};
		
		registerListener( Operation.CREATED, os, new Listener<Order>() {
			@Override public synchronized void onEvent( Order o ) { 
				orders.add( o );
				synchronized ( orderCreatedListeners ) {
					Iterator<Listener<Order>> it = orderCreatedListeners.iterator();
					while ( it.hasNext() ) it.next().onEvent( o );
				}
			}
		});
		registerListener( Operation.REMOVED, os, new Listener<Order>() {
			@Override public synchronized void onEvent( Order o ) { 
				orders.remove( o ); 
				synchronized ( orderRemovedListeners ) {
					Iterator<Listener<Order>> it = orderRemovedListeners.iterator();
					while ( it.hasNext() ) it.next().onEvent( o );
				}
			}
		});
	}
		
	@Override
	public void beginTransaction() {
		if ( tx != null ) throw new SpaceException("There already is a running transaction:" + tx );
		
		try {
			tx = capi.createTransaction( TX_TIMEOUT, space );
		} catch ( MzsCoreException e ) {
			throw new SpaceException( e );
		}
	}

	@Override
	public void commitTransaction() {
		if ( tx == null ) throw new SpaceException("There already no running transaction" );
		
		try {
			capi.commitTransaction( tx );
			tx = null;
		} catch ( MzsCoreException e ) {
			throw new SpaceException( e );
		}
	}

	@Override
	public void rollbackTransaction() {
		if ( tx == null ) throw new SpaceException("There already no running transaction" );
		
		try {
			capi.rollbackTransaction( tx );
			tx = null;
		} catch ( MzsCoreException e ) {
			log.error( "Could not roll back transaction: %s - %s", e, e.getMessage() );
		}
	}

	@Override 
	public void registerOrderListener( Operation o, Listener<Order> l ) {
		switch ( o ) {
			case CREATED: orderCreatedListeners.add( l );
			case REMOVED: orderRemovedListeners.add( l );
		}
	}
	
	@Override
	public void registerStorageListener( Operation o, Listener<Computer> l ) {
		registerListener( o, crefs.getStorage(), l );
	}

	@Override
	public void registerTrashListener( Operation o, Listener<Product> l ) {
		registerListener( o, crefs.getTrash(), l );
	}

	@Override
	public <P extends Product> void registerListener( Class<P> c, Operation o, Listener<P> l ) {
		registerListener( o, crefs.getContainer( c ), l );		
	}

	@Override
	public void setOrderItemListener( final OrderItemListener l ) {
		orderItemListener = l;
		
		orderItemNotifications[0] = registerListener( Operation.CREATED, crefs.getPcSpecs(), new Listener<PcSpec>() {
			@Override
			public synchronized void onEvent( PcSpec p ) {
				l.onOrderItemDefect( p.orderId );
			}
		});	
		orderItemNotifications[1] = registerListener( Operation.REMOVED, crefs.getPcSpecs(), new Listener<PcSpec>() {
			@Override
			public synchronized void onEvent( PcSpec p ) {
				l.onOrderItemProduced( p.orderId );
			}
		});	
		orderItemNotifications[2] = registerListener( Operation.CREATED, crefs.getStorage(), new Listener<Computer>() {
			@Override
			public synchronized void onEvent( Computer c ) {
				if ( c.finished && c.orderId != null ) l.onOrderItemFinished( c.orderId );
			}
		});		
	}
	
	@Override
	public void signalPcForOrderDefect( Computer c ) {
		if ( c.orderId == null ) return;
		
		pcSpecOut.write( new PcSpec( c.orderId, c.cpu.type, c.gpu != null, c.ram.size() ) );
	}
	
	@Override
	public void signalOrderIsDone( Order o ) {
		try {
			capi.delete( crefs.getOrders(), KeyCoordinator.newSelector( o.id ), WRITE_TIMEOUT, tx );
		} catch ( MzsCoreException e ) {
			throw new SpaceException( e );
		}
	}
	
	@Override
	public Iterable<Input<PcSpec>> orders() {
		return new Iterable<Input<PcSpec>>() {
			@Override
			public Iterator<Input<PcSpec>> iterator() {
				return new Iterator<Input<PcSpec>>() {
					@Override
					public boolean hasNext() {
						fetch();
						return o != null;
					}

					@Override
					public Input<PcSpec> next() {
						fetch();
						
						if ( o == null ) throw new NoSuchElementException();
						
						Order out = o;
						o = null;
						
						return getInput( crefs.getPcSpecs(), LabelCoordinator.newSelector( out.id ) );
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					private void fetch() {
						if ( o != null ) return;
						
						if ( !it.hasNext() ) return;
						
						o = it.next();
					}
					
					private Order o;
					private Iterator<Order> it = orders.iterator();
				};
			}
		};
	}

	@Override
	public Input<Computer> getComputerInput() {
		return getInput( crefs.getPcs() );
	}
	
	@Override
	public Input<Computer> getComputersUntestedForDefect() {
		return getInput( crefs.getPcs(), SELECTOR_UNTESTED_FOR_DEFECT );
	}
	
	@Override
	public Input<Computer> getComputersUntestedForCompleteness() {
		return getInput( crefs.getPcs(), SELECTOR_UNTESTED_FOR_COMPLETENESS );
	}
	
	@Override
	public Input<Computer> getTestedComputers() {
		return getInput( crefs.getPcs(), SELECTOR_TESTED_FOR_DEFECT, SELECTOR_TESTED_FOR_COMPLETENESS );
	}
	
	@Override
	public Output<Computer> getComputerOutput() {
		return new SpaceOutput<Computer>( crefs.getPcs() ) {
			@Override
			protected void coordData( Computer pc, List<CoordinationData> cd ) {
				if ( pc.defect == TestStatus.UNTESTED ) {
					cd.add( LABEL_UNTESTED_FOR_DEFECT );
				} else {
					cd.add( LABEL_TESTED_FOR_DEFECT );
				}
				if ( pc.complete == TestStatus.UNTESTED ) {
					cd.add( LABEL_UNTESTED_FOR_COMPLETENESS );
				} else {
					cd.add( LABEL_TESTED_FOR_COMPLETENESS );
				}
				if ( pc.orderId != null ) {
					cd.add( label(pc.orderId) );
				}
			}			
		};
	}

	@Override
	public CpuInput getCpuInput() {
		return new SpaceCpuInput( crefs.getCpus() );
	}

	@Override
	public Input<Gpu> getGpuInput() {
		return getInput( crefs.getGpus() );
	}

	@Override
	public Input<Mainboard> getMainboardInput() {
		return getInput( crefs.getMainboards(), FifoCoordinator.newSelector( 1 ) );
	}

	@Override
	public RamInput getRamInput() {
		return new SpaceRamInput( crefs.getRams() );
	}

	@Override
	public Output<Cpu> getCpuOutput() {
		return new SpaceOutput<Cpu>( crefs.getCpus() ) {
			@Override
			protected void coordData( Cpu cpu, List<CoordinationData> c ) {
				c.add( LabelCoordinator.newCoordinationData( cpu.type.name() ) );				
			}	
		};
	}

	@Override
	public Output<Gpu> getGpuOutput() {
		return getOutput( crefs.getGpus() );
	}

	@Override
	public Output<Mainboard> getMainboardOutput() {
		return getOutput( crefs.getMainboards() );
	}

	@Override
	public Output<RamModule> getRamOutput() {
		return getOutput( crefs.getRams() );
	}

	@Override
	public Output<Computer> getStorage() {
		return getOutput( crefs.getStorage() );
	}

	@Override
	public Output<Product> getTrash() {
		return getOutput( crefs.getTrash() );
	}

	@Override
	public String genId() {
		return Integer.toHexString( (int) System.nanoTime() );
	}

	@Override
	public void placeOrder( Cpu.Type cpuType, int ramAmount, boolean gpu, int quanitity ) {
		Order o = new Order( genId(), cpuType, ramAmount, gpu, quanitity );

		// we have to disable order item notifications while placing an order
		if ( orderItemListener != null ) {
			try {
				orderItemNotifications[0].destroy();
				orderItemNotifications[1].destroy();
				orderItemNotifications[2].destroy();							
			} catch ( MzsCoreException e ) {
				throw new SpaceException( e );
			}
		}
		
		for ( int i = 0; i < o.quantity; i++ ) pcSpecOut.write( o.getSpec() );
		orderOut.write( o );
		
		// reenable order item notifications
		if ( orderItemListener != null ) setOrderItemListener( orderItemListener );
	}

	@Override
	public void shutdown() {
		if ( nm != null ) nm.shutdown();
		
		for ( Notification n : ns )
			try {
				n.destroy();
			} catch ( MzsCoreException e ) {
				e.printStackTrace();
			}

		core.shutdown( true );
	}
	
	private <P extends Product> Notification registerListener( Operation op, 
	                                                   ContainerReference cref, 
	                                                   final Listener<P> l ) {
		try {
			Notification n = nm.createNotification( cref, new NotificationListener() {
				@Override
				@SuppressWarnings("unchecked")
				public void entryOperationFinished( Notification n, org.mozartspaces.notifications.Operation o, List<? extends Serializable> entries ) {
					for ( Serializable s : entries ) {
						Entry e = null;
						if ( s instanceof Entry ) {
							e = (Entry) s;
							s = ((Entry) s).getValue();
						}
						
						if ( s instanceof Entry ) { s = ((Entry) s).getValue(); }
						l.onEvent( (P) s );
					}
				}
			}, op2ops( op ) );
			
			ns.add( n );
			
			return n;
		} catch ( MzsCoreException e ) {
			throw new SpaceException( e );
		} catch ( InterruptedException e ) {
			throw new SpaceException( e );
		}
	}
	
	private static org.mozartspaces.notifications.Operation[] op2ops( Operation op ) {
		switch ( op ) {
			case CREATED: return new org.mozartspaces.notifications.Operation[] {
				org.mozartspaces.notifications.Operation.WRITE
			};
			case REMOVED: return new org.mozartspaces.notifications.Operation[] {
				org.mozartspaces.notifications.Operation.TAKE,
				org.mozartspaces.notifications.Operation.DELETE					
			};
			default:      throw new SpaceException();
		}
	}

	private <P extends Product> Input<P> getInput( ContainerReference cref, final Selector... sels ) {
		return new SpaceInput<P>( cref ) {
			@Override
			protected void selectors( List<Selector> s ) {
				for ( Selector sel : sels ) s.add( sel );
			}			
		};
	}
	
	private <P extends Product> Output<P> getOutput( ContainerReference cref, final CoordinationData... cds ) {
		return new SpaceOutput<P>( cref ) {
			@Override
			protected void coordData( P p, List<CoordinationData> c ) {
				for ( CoordinationData cd : cds ) c.add( cd );
			}			
		};
	}
	
	private abstract class SpaceInput<P extends Product> implements Input<P> {
		public SpaceInput( ContainerReference cref ) {
			this.cref = cref;
		}
		
		@Override
		public P take() {
			List<Selector> selectors = new ArrayList<Selector>();
			selectors( selectors );
			
			List<P> ps = __take__( selectors );
			
			if ( ps == null || ps.isEmpty() )
				return null;
			else
				return ps.get( 0 );
		}

		protected final List<P> __take__( Selector... selectors ) {
			return __take__( Arrays.asList( selectors ) );
		}
		@SuppressWarnings("unchecked")
		protected final List<P> __take__( List<Selector> selectors ) {
			try {
				List<Serializable> l = capi.take( cref, selectors, READ_TIMEOUT, tx );
				
				List<P> ps = new ArrayList<P>( l.size() );
				for ( Serializable s : l ) ps.add( (P) entry2product( s ) );
				
				return ps;
			} catch ( CountNotMetException e ) {
				return null;
			} catch ( MzsCoreException e ) {
				throw new SpaceException( e );
			}
		}
		
		protected void selectors( List<Selector> s ) {}
		
		private final ContainerReference cref;
	}
	private class SpaceCpuInput extends SpaceInput<Cpu> implements CpuInput {
		public SpaceCpuInput( ContainerReference cref ) {
			super( cref );
		}

		@Override
		public Cpu take( Cpu.Type type ) {
			List<Cpu> cs = __take__( LabelCoordinator.newSelector( type.name() ) );
			
			if ( cs == null || cs.isEmpty() ) 
				return null;
			else 
				return cs.get( 0 );
		}	
	}
	private class SpaceRamInput implements RamInput {
		public SpaceRamInput( ContainerReference cref ) {
			this.cref = cref;
		}
		
		@Override
		public RamList take() {
			List<RamModule> ram;
			
			ram = __take__( AnyCoordinator.newSelector( 4 ) );
			if ( ram == null ) ram = __take__( AnyCoordinator.newSelector( 2 ) );
			if ( ram == null ) ram = __take__( AnyCoordinator.newSelector( 1 ) );
			
			if ( ram == null ) return null;

			return new RamList( ram );
		}
		@Override
		public RamList take( int amount ) {
			List<RamModule> ram;
			
			ram = __take__( AnyCoordinator.newSelector( amount ) );
			
			if ( ram == null ) return null;
			
			return new RamList( ram );
		}

		protected final List<RamModule> __take__( Selector... selectors ) {
			return __take__( Arrays.asList( selectors ) );
		}
		protected final List<RamModule> __take__( List<Selector> selectors ) {
			try {
				List<Serializable> l = capi.take( cref, selectors, READ_TIMEOUT, tx );
				
				List<RamModule> ps = new ArrayList<RamModule>( l.size() );
				for ( Serializable s : l ) ps.add( (RamModule) entry2product( s ) );
				
				return ps;
			} catch ( CountNotMetException e ) {
				return null;
			} catch ( MzsCoreException e ) {
				throw new SpaceException( e );
			}
		}
		
		private final ContainerReference cref;
	}
	
	private class SpaceOutput<P extends Product> implements Output<P> {
		public SpaceOutput( ContainerReference cref ) {
			this.cref = cref;
		}
		
		@Override
		public void write( P p ) {
			List<CoordinationData> coordData = new ArrayList<CoordinationData>();
			coordData( p, coordData );
		
			try {
				capi.write( cref, WRITE_TIMEOUT, tx, new Entry( p, coordData ) );
			} catch ( MzsCoreException e ) {
				throw new SpaceException( e );
			}
		}
		
		protected void coordData( P p, List<CoordinationData> s ) {}
		
		private final ContainerReference cref;
	}
	
	@SuppressWarnings("unchecked")
	private static <P extends Product> P entry2product( Serializable s ) {
		if ( s instanceof Entry ) s = ((Entry) s).getValue();
		
		return (P) s;
	}
	
	private TransactionReference tx;

	private OrderItemListener orderItemListener;
	private Notification[]    orderItemNotifications = new Notification[3];
	
	private final MzsCore core;
	private final URI     space;
	private final Capi    capi;
	private final SpaceContainers crefs;
	private final NotificationManager nm;
	private final List<Notification>  ns = new ArrayList<Notification>();

	private final List<Order>           orders                = new CopyOnWriteArrayList<Order>();
	private final List<Listener<Order>> orderCreatedListeners = new Vector<Listener<Order>>();
	private final List<Listener<Order>> orderRemovedListeners = new Vector<Listener<Order>>();
	
	private final Output<Order>  orderOut;
	private final Output<PcSpec> pcSpecOut;
	
	private final Logger log = Logger.make( getClass() );
	
	private static final long TX_TIMEOUT    = MzsConstants.TransactionTimeout.INFINITE;
	private static final long READ_TIMEOUT  = MzsConstants.RequestTimeout.TRY_ONCE;
	private static final long WRITE_TIMEOUT = MzsConstants.RequestTimeout.INFINITE;
	
	private static final String STR_TESTED_FOR_DEFECT         = "TESTED_FOR_DEFECT";
	private static final String STR_UNTESTED_FOR_DEFECT       = "UNTESTED_FOR_DEFECT";
	private static final String STR_TESTED_FOR_COMPLETENESS   = "TESTED_FOR_COMPLETENESS";
	private static final String STR_UNTESTED_FOR_COMPLETENESS = "UNTESTED_FOR_COMPLETENESS";

	private static final LabelData LABEL_TESTED_FOR_COMPLETENESS   = label( STR_TESTED_FOR_COMPLETENESS   );
	private static final LabelData LABEL_UNTESTED_FOR_COMPLETENESS = label( STR_UNTESTED_FOR_COMPLETENESS );
	private static final LabelData LABEL_TESTED_FOR_DEFECT         = label( STR_TESTED_FOR_DEFECT         );
	private static final LabelData LABEL_UNTESTED_FOR_DEFECT       = label( STR_UNTESTED_FOR_DEFECT       );

	private static final LabelSelector SELECTOR_TESTED_FOR_COMPLETENESS   = labelSel( STR_TESTED_FOR_COMPLETENESS   );
	private static final LabelSelector SELECTOR_UNTESTED_FOR_COMPLETENESS = labelSel( STR_UNTESTED_FOR_COMPLETENESS );
	private static final LabelSelector SELECTOR_TESTED_FOR_DEFECT         = labelSel( STR_TESTED_FOR_DEFECT         );
	private static final LabelSelector SELECTOR_UNTESTED_FOR_DEFECT       = labelSel( STR_UNTESTED_FOR_DEFECT       );
	
	private static final LabelData     label( String label )    { return LabelCoordinator.newCoordinationData( label ); }
	private static final LabelSelector labelSel( String label ) { return LabelCoordinator.newSelector( label ); }
	
}
