package tu.space.dark;

import static tu.space.jms.JMS.CREATED_SELECTOR;
import static tu.space.jms.JMS.IS_IN_ORDER_SELECTOR;
import static tu.space.jms.JMS.REMOVED_SELECTOR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Cpu.Type;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamList;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.contracts.PcSpec;
import tu.space.jms.JMS;
import tu.space.jms.JMSReader;
import tu.space.jms.JMSWriter;
import tu.space.middleware.CpuInput;
import tu.space.middleware.Input;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;
import tu.space.middleware.OrderItemListener;
import tu.space.middleware.Output;
import tu.space.middleware.RamInput;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;

public class JMSMiddleware implements Middleware {

	public JMSMiddleware( int port ) {
		try {
			connection = JMS.openConnection( port );
			session    = JMS.createSession( connection );
			
			orderOut  = JMS.getWriter( session, Order.class );
			pcSpecOut = JMS.getWriter( session, PcSpec.class );
			
			registerOrderListener( Operation.CREATED, new Listener<Order>() {
				@Override
				public void onEvent( Order p ) {
					orders.add( p );
				}
			});
			registerOrderListener( Operation.REMOVED, new Listener<Order>() {
				@Override
				public void onEvent( Order p ) {
					orders.remove( p );
				}
			});
			
			connection.start();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	@Override
	public void beginTransaction() {
	}

	@Override
	public void commitTransaction() {
		try {
			session.commit();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	@Override
	public void rollbackTransaction() {
		try {
			session.rollback();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	@Override
	public void registerOrderListener( Operation o, Listener<Order> l ) {
		registerListener( Order.class, op2selector( o ), l );
	}

	@Override
	public void registerStorageListener( Operation o, Listener<Computer> l ) {
		registerListener( "Storage", op2selector( o ), l );
	}

	@Override
	public void registerTrashListener( Operation o, Listener<Product> l ) {
		registerListener( "Trash", op2selector( o ), l );
	}

//	@Override
//	public void registerComponentListener( Operation o, Listener<Component> l ) {
//		
//	}
//	void registerComputerListener( Operation o, Listener<Computer> l );

//	void registerListenerForComputersUntestedForDefect( Operation o, Listener<Computer> l );
//	void registerListenerForComputersUntestedForCompleteness( Operation o, Listener<Computer> l );
//	
//	void registerTestedComputerListener( Operation o, Listener<Computer> l );

	
	@Override
	public <P extends Product> void registerListener( Class<P> c, Operation o, Listener<P> l ) {
		registerListener( c, op2selector( o ), l );
	}

	@Override
	public void setOrderItemListener( final OrderItemListener l ) {
		registerListener( "Storage", CREATED_SELECTOR + " AND " + IS_IN_ORDER_SELECTOR, new Listener<Computer>() {
			@Override
			public void onEvent( Computer p ) {
				l.onOrderItemFinished( p.orderId );
			}
		});
		registerListener( PcSpec.class, CREATED_SELECTOR , new Listener<PcSpec>() {
			@Override
			public void onEvent( PcSpec p ) {
				if ( p.init() ) return;
				
				l.onOrderItemDefect( p.orderId );
			}			
		});
		registerListener( PcSpec.class, REMOVED_SELECTOR , new Listener<PcSpec>() {
			@Override
			public void onEvent( PcSpec p ) {
				l.onOrderItemProduced( p.orderId );
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
			new JMSReader<>( Order.class, session, "orderId =" + o.id ).take();
		} catch ( JMSException e ) {
			throw new SpaceException();
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
						
						try {
							return new JMSReader<PcSpec>( PcSpec.class, session, "orderId = '" + out.id + "'"  );
						} catch ( JMSException e ) {
							throw new SpaceException();
						}
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
		return JMS.getPCReader( session );
	}

	@Override
	public Input<Computer> getComputersUntestedForDefect() {
		return JMS.getPCUntestedForDefectReader( session );
	}

	@Override
	public Input<Computer> getComputersUntestedForCompleteness() {
		return JMS.getPCUntestedForCompletenessReader( session );
	}

	@Override
	public Input<Computer> getTestedComputers() {
		return JMS.getTestedPcReader( session );
	}

	@Override
	public Output<Computer> getComputerOutput() {
		return JMS.getPCWriter( session );
	}

	@Override
	public CpuInput getCpuInput() {
		try {
			return new CpuInput() {
				@Override
				public Cpu take() {
					return any.take();
				}
				
				@Override
				public Cpu take( Type type ) {
					switch ( type ) {
						case SINGLE_CORE: return single.take();
						case DUAL_CORE:   return dual.take();
						case QUAD_CORE:   return quad.take();
						default: throw new SpaceException();
					}
				}
				
				private final JMSReader<Cpu> any    = JMS.getCPUReader( session );
				private final JMSReader<Cpu> single = new JMSReader<Cpu>( Cpu.class, session, "CPU = " + Cpu.Type.SINGLE_CORE.name() );
				private final JMSReader<Cpu> dual   = new JMSReader<Cpu>( Cpu.class, session, "CPU = " + Cpu.Type.DUAL_CORE.name() );
				private final JMSReader<Cpu> quad   = new JMSReader<Cpu>( Cpu.class, session, "CPU = " + Cpu.Type.QUAD_CORE.name() );
			};
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	@Override
	public Input<Gpu> getGpuInput() {
		return JMS.getGPUReader( session );
	}

	@Override
	public Input<Mainboard> getMainboardInput() {
		return JMS.getMainboardReader( session );
	}

	@Override
	public RamInput getRamInput() {
		return new RamInput() {
			@Override
			public RamList take() {
				List<RamModule> rams = new ArrayList<RamModule>();
				
				for ( int i = 0; i < 4; i++ ) {
					RamModule r = ramIn.take();
					if ( r != null ) rams.add( r );
				}

				if ( rams.isEmpty() ) return null;
				
				if ( rams.size() == 3 ) ramOut.write( rams.remove( 0 ) );
				
				return new RamList( rams );
			}
			
			@Override
			public RamList take( int amount ) {
				List<RamModule> rams = new ArrayList<RamModule>();
				
				for ( int i = 0; i < amount; i++ ) {
					RamModule r = ramIn.take();
					if ( r != null ) rams.add( r );
				}

				if ( rams.size() != amount ) return null;
				
				return new RamList( rams );
			}
			
			private final JMSReader<RamModule> ramIn  = JMS.getRAMReader( session );
			private final JMSWriter<RamModule> ramOut = JMS.getRAMWriter( session );
		};
	}

	@Override
	public Output<Cpu> getCpuOutput() {
		return JMS.getCPUWriter( session );
	}

	@Override
	public Output<Gpu> getGpuOutput() {
		return JMS.getGPUWriter( session );
	}

	@Override
	public Output<Mainboard> getMainboardOutput() {
		return JMS.getMainboardWriter( session );
	}

	@Override
	public Output<RamModule> getRamOutput() {
		return JMS.getRAMWriter( session );
	}

	@Override
	public Output<Computer> getStorage() {
		return JMS.getStorageWriter( session );
	}

	@Override
	public Output<Product> getTrash() {
		return JMS.getTrashWriter( session );
	}

	@Override
	public String genId() {
		return Integer.toHexString( (int) System.nanoTime() );
	}

	@Override
	public synchronized void placeOrder( Type cpuType, int ramAmount, boolean gpu, int quanitity ) {
		// place order
		Order o = new Order( genId(), cpuType, ramAmount, gpu, quanitity );
		orderOut.write( o );
		
		for ( int i = 0; i < quanitity; i++ ) {
			pcSpecOut.write( new PcSpec( o.id, o.cpuType, o.gpu, o.ramQuantity, true ) );
		}
		commitTransaction();
	}

	@Override
	public void shutdown() {
		try {
			connection.stop();
			connection.close();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	//***** PRIVATE

	private <P extends Product> void registerListener( Class<P> c, String selector, final Listener<P> l ) {
		registerListener( c.getSimpleName(), selector, l );
	}
	private <P extends Product> void registerListener( String topicName, String selector, final Listener<P> l ) {
		try {
			final Session         s  = JMS.createSession( connection );
			final MessageConsumer mc = s.createConsumer( s.createTopic( topicName ), selector );
			
			new Thread() {
				public void run() {
					
					while ( true ) {
						try {
							Message m = mc.receive();
							
							if ( m == null ) continue;
							
							// acknowledge recival of topic message
							s.commit();
							
							try { sleep( 100 ); } catch ( InterruptedException e ) {}
							
							@SuppressWarnings("unchecked")
							P p = (P) ((ObjectMessage) m).getObject();
							l.onEvent( p );
						} catch ( JMSException e ) {
							log.error( e.toString() );
							continue;
						}					
					}
				}
			}.start();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	private static String op2selector( Operation o ) {
		switch ( o ) {
			case CREATED: return CREATED_SELECTOR;
			case REMOVED: return REMOVED_SELECTOR;
			default:      throw new SpaceException();
		}
	}
	
	private final Connection connection;
	private final Session    session;
	private final JMSWriter<Order>  orderOut;
	private final JMSWriter<PcSpec> pcSpecOut;
	
	private final List<Order> orders = new CopyOnWriteArrayList<Order>();
	
	private final Logger log = Logger.make( getClass() );	
}
