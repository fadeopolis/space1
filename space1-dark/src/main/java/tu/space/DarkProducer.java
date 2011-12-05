package tu.space;

import java.util.Random;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import tu.space.components.Component;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;
import tu.space.utils.UUIDGenerator;
import tu.space.utils.Util;

public final class DarkProducer implements Runnable {
	public static final int MAX_SLEEP_TIME = 3000;
	public static final String USAGE = 
			"usage: producer ID TYPE QUOTA ERROR_RATE\n"      +
			"  where TYPE is one of CPU, GPU, MAINBOARD, RAM"
	;
	
	public static void main( String... args ) throws InterruptedException, JMSException {
		if ( args.length != 4 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id        = args[0];
		final int    quota     = Integer.parseInt( args[2] );
		final double errorRate = Double.parseDouble( args[3] );
		
		Component.Factory<?> factory;
		if ( "CPU".equalsIgnoreCase( args[1] ) )            factory = new Component.CpuFactory();
		else if ( "GPU".equalsIgnoreCase( args[1] ) )       factory = new Component.GpuFactory();
		else if ( "MAINBOARD".equalsIgnoreCase( args[1] ) ) factory = new Component.MainboardFactory();
		else if ( "RAM".equalsIgnoreCase( args[1] ) )       factory = new Component.RamModuleFactory();
		else {
			System.err.println( USAGE );
			System.exit( 1 );
			return;
		}
		
		Logger.configure();

		new DarkProducer( id, quota, errorRate, factory ).run();
	}	
	
	public <C extends Component> DarkProducer( 
			String id, int quota, double errorRate, Component.Factory<C> f ) throws JMSException {
		this.id = id;
		this.quota = quota;
		this.errorRate = errorRate;
		this.factory = f;
		
		this.connection = JMS.openConnection();
		this.session    = JMS.createSession( connection );

		this.jms = new JMSWriter( session, f.getType() );
	}
	
	public void run() {
		try {
			connection.start();
			
			log.info( "Starting %s", id );
			doRun();
		} catch ( JMSException e ) {
		} finally {			
			JMS.close( connection );
		}
		log.info( "%s finished", id );
	}
	private void doRun() {
		for ( int i = quota; i > 0; i-- ) {
			log.info("%s has to make %d more component(s)", this, i);

			// produce next component
			boolean   faulty    = rand.nextDouble() < errorRate; // bernoulli experiment
			UUID      productId = uuids.generate();
			Component c         = factory.make( productId, id, faulty );

			assert c.id         == productId;
			assert c.producerId == id;
			assert c.hasDefect  == faulty;

			// sleep to simulate work time
			Util.sleep( rand.nextInt( MAX_SLEEP_TIME ) );

			// send out produced component
			try {
				jms.send( c );
				session.commit();				
			} catch ( JMSException e ) {
				JMS.rollback( session );
				throw new SpaceException(e);
			}
		}
	}
	
	private final String id;
	private final int    quota;
	private final double errorRate;
	
	private final Component.Factory<?> factory;
	
	private final Connection connection;
	private final Session    session;
	
	private final JMSWriter jms;
	
	private final Random        rand  = new Random();
	private final UUIDGenerator uuids = new UUIDGenerator();  
	private final Logger        log   = Logger.make( getClass() );
}
