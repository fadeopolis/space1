package tu.space.dark;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQMessage;

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

	public JMSMiddleware( String id, int port ) {
		try {
			connection = JMS.openConnection( id, port );
			session    = JMS.createSession( connection );
			
			orderOut  = new JMSOutput<Order>( Order.class, session );
			pcSpecOut = new JMSOutput<PcSpec>( PcSpec.class, session );
			
			orders = new CopyOnWriteArrayList<Order>( orders() );
			
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
		registerListener( Order.class, o, l );
	}

	@Override
	public void registerStorageListener( Operation o, Listener<Computer> l ) {
		registerListener( Computer.class, JMS.getStorage( session ), o, null, l );
	}

	@Override
	public void registerTrashListener( Operation o, Listener<Product> l ) {
		registerListener( Product.class, JMS.getTrash( session ), o, null, l );
	}

	@Override
	public void registerComputerListener( Operation o, Listener<Computer> l ) {
		registerListener( Computer.class, o, l );
	}

	@Override
	public void registerListenerForComputersUntestedForDefect( Operation o, Listener<Computer> l ) {
		registerListener( Computer.class, o, UNTESTED_FOR_DEFECT_SELECTOR, l );
	}

	@Override
	public void registerListenerForComputersUntestedForCompleteness( Operation o, Listener<Computer> l ) {
		registerListener( Computer.class, o, UNTESTED_FOR_COMPLETENESS_SELECTOR, l );
	}

	@Override
	public void registerTestedComputerListener( Operation o, Listener<Computer> l ) {
		registerListener( Computer.class, o, TESTED_SELECTOR, l );
	}
	
	@Override
	public <P extends Product> void registerListener( Class<P> c, Operation o, Listener<P> l ) {
		registerListener( c, o, null, l );
	}

	@Override
	public void setOrderItemListener( final OrderItemListener l ) {
		registerListener( Computer.class, JMS.getStorage( session ), Operation.CREATED, IS_IN_ORDER_SELECTOR, new Listener<Computer>() {
			@Override
			public void onEvent( Computer p ) {
				l.onOrderItemFinished( p.orderId );
			}
		});
		registerListener( PcSpec.class, Operation.CREATED, new Listener<PcSpec>() {
			@Override
			public void onEvent( PcSpec p ) {
				if ( p.init() ) return;
				
				l.onOrderItemDefect( p.orderId );
			}			
		});
		registerListener( PcSpec.class, Operation.REMOVED, new Listener<PcSpec>() {
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
		getInput( Order.class, "orderId='" + o.id + "'" ).take();
	}

	@Override
	public Iterable<Input<PcSpec>> orderItems() {
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
						
						return getInput( PcSpec.class, "orderId='" + out.id + "'"  );
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
	public List<Order> orders() {
		try {
			QueueBrowser      qb    = session.createBrowser( JMS.getQueue( session, Order.class ) );
			Marshaller<Order> marsh = Marshaller.Order;
			List<Order> os = new ArrayList<Order>();
			
			@SuppressWarnings("unchecked")
			Enumeration<ObjectMessage> e = qb.getEnumeration();
			while ( e.hasMoreElements() ) {
				Message m = e.nextElement();

				os.add( marsh.fromMessage( m ) );
			}
			
			return os;
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}	
	
	@Override
	public Input<Computer> getComputerInput() {
		return getInput( Computer.class, null );
	}

	@Override
	public Input<Computer> getComputersUntestedForDefect() {
		return getInput( Computer.class, UNTESTED_FOR_DEFECT_SELECTOR );
	}

	@Override
	public Input<Computer> getComputersUntestedForCompleteness() {
		return getInput( Computer.class, UNTESTED_FOR_COMPLETENESS_SELECTOR );
	}

	@Override
	public Input<Computer> getTestedComputers() {
		return getInput( Computer.class, TESTED_SELECTOR );
	}

	@Override
	public Output<Computer> getComputerOutput() {
		return new JMSOutput<Computer>( Computer.class, session );
	}

	@Override
	public CpuInput getCpuInput() {
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
			
			private final JMSInput<Cpu> any    = getInput( Cpu.class, null );
			private final JMSInput<Cpu> single = getInput( Cpu.class, "CPU='" + Cpu.Type.SINGLE_CORE.name() + "'" );
			private final JMSInput<Cpu> dual   = getInput( Cpu.class, "CPU='" + Cpu.Type.DUAL_CORE.name()   + "'" );
			private final JMSInput<Cpu> quad   = getInput( Cpu.class, "CPU='" + Cpu.Type.QUAD_CORE.name()   + "'" );
		};
	}

	@Override
	public Input<Gpu> getGpuInput() {
		return getInput( Gpu.class, null );
	}

	@Override
	public Input<Mainboard> getMainboardInput() {
		return getInput( Mainboard.class, null );
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
			
			private final JMSInput<RamModule> ramIn   = getInput( RamModule.class, null );
			private final JMSOutput<RamModule> ramOut = getOutput( RamModule.class, null );
		};
	}

	@Override
	public Output<Cpu> getCpuOutput() {
		return new JMSOutput<Cpu>( Cpu.class, session );
	}

	@Override
	public Output<Gpu> getGpuOutput() {
		return getOutput( Gpu.class, null );
	}

	@Override
	public Output<Mainboard> getMainboardOutput() {
		return getOutput( Mainboard.class, null );
	}

	@Override
	public Output<RamModule> getRamOutput() {
		return getOutput( RamModule.class, null );
	}

	@Override
	public Output<Computer> getStorage() {
		return new JMSOutput<Computer>( Computer.class, session, JMS.getStorage( session ) );
	}

	@Override
	public Output<Product> getTrash() {
		return new JMSOutput<Product>( Product.class, session, JMS.getTrash( session ) );
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

	private <P extends Product> void registerListener( Class<P> c, Operation o, String selector, final Listener<P> l ) {
		registerListener( c, JMS.getQueue( session, c ), o, selector, l );
	}
	private <P extends Product> void registerListener( final Class<P> c, Queue queue, Operation o, String selector, final Listener<P> l ) {
		try {
			final Session         s  = JMS.createSession( connection );
			final Topic           t  = JMS.getTopic( s, queue.getQueueName(), o );
			final MessageConsumer mc = s.createConsumer( t, selector );
			final Marshaller<P>   ma = Marshaller.forType( c );
			
			new Thread( "Listener-" + queue.getQueueName() + "-" + o + "-[" + selector + "]" ) {
				public void run() {
					
					while ( true ) {
						try {
							Message m = mc.receive();
							
							if ( m == null ) {
								continue;
							}
							
							// acknowledge recieval of topic message
							s.commit();
							
							try { sleep( 100 ); } catch ( InterruptedException e ) {}
							
							ActiveMQMessage advisory = (ActiveMQMessage) m;
							Message         msg      = (Message) advisory.getDataStructure();

							l.onEvent( ma.fromMessage( msg ) );
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
	
	<P extends Product> JMSInput<P> getInput( Class<P> c, String selector ) {
		try {
			return new JMSInput<P>( c, session, JMS.getQueue( session, c ), selector );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	<P extends Product> JMSOutput<P> getOutput( Class<P> c, String selector ) {
		return new JMSOutput<P>( c, session, JMS.getQueue( session, c ) );
	}
	
	private final Connection connection;
	private final Session    session;
	private final JMSOutput<Order>  orderOut;
	private final JMSOutput<PcSpec> pcSpecOut;
	
	private final List<Order> orders;
	
	private final Logger log = Logger.make( getClass() );
	
	public static final String TESTED_FOR_DEFECT       = "TESTED_FOR_DEFECT";
	public static final String TESTED_FOR_COMPLETENESS = "TESTED_FOR_COMPLETENESS";
	
	private static final String IS_IN_ORDER_SELECTOR               = "(orderId IS NOT NULL)";
	private static final String UNTESTED_FOR_DEFECT_SELECTOR       = "(TESTED_FOR_DEFECT        = false)";
	private static final String UNTESTED_FOR_COMPLETENESS_SELECTOR = "(TESTED_FOR_COMPLETENESS  = false)";
	private static final String TESTED_SELECTOR                    = "(TESTED_FOR_DEFECT = true) AND (TESTED_FOR_COMPLETENESS  = true)";
}
