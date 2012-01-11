package tu.space;

import java.util.Random;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import tu.space.components.Component;
import tu.space.jms.JMS;
import tu.space.jms.JMSWriter;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;
import tu.space.utils.UUIDGenerator;
import tu.space.utils.Util;

public final class DarkProducer implements Runnable {
	public static final int MAX_SLEEP_TIME = 3000;
	public static final String USAGE = 
			"usage: producer ID PORT TYPE QUOTA ERROR_RATE\n"      +
			"  where TYPE is one of CPU, GPU, MAINBOARD, RAM"
	;
	
	public static void main( String... args ) throws InterruptedException, JMSException {
		if ( args.length != 5 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id        = args[0];
		final int    port      = Integer.parseInt( args[1] );
		final int    quota     = Integer.parseInt( args[3] );
		final double errorRate = Double.parseDouble( args[4] );
		
		Component.Factory<?> factory;
		try {
			factory = Component.makeFactory( args[2] );
		} catch ( SpaceException e ) {
			System.err.println( USAGE );
			System.exit( 1 );
			return;			
		}
		
		Logger.configure();

		new DarkProducer( id, port, quota, errorRate, factory ).run();
	}	
	
	public <C extends Component> DarkProducer( 
			String id, int port, int quota, double errorRate, Component.Factory<C> f ) throws JMSException {
		this.id = id;
		this.quota = quota;
		this.errorRate = errorRate;
		this.factory = f;
		
		this.connection = JMS.openConnection( port );
		this.session    = JMS.createSession( connection );

		this.jms = JMS.getWriter( session, f.getType() );
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
	
	@SuppressWarnings("unchecked")
	private void doRun() {
		for ( int i = quota; i > 0; i-- ) {
			log.info("%s has to make %d more component(s)", this, i);

			// produce next component
			boolean   faulty    = rand.nextDouble() < errorRate; // bernoulli experiment
			String    productId = uuids.generate();
			Component c         = factory.make( productId, id, faulty );

			assert c.id         == productId;
			assert c.producerId == id;
			assert c.hasDefect  == faulty;

			// sleep to simulate work time
			Util.sleep();

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
	
	@Override
	public String toString() {
		return id;
	}
	
	private final String id;
	private final int    quota;
	private final double errorRate;
	
	private final Component.Factory<?> factory;
	
	private final Connection connection;
	private final Session    session;
	
	@SuppressWarnings("rawtypes")
	private final JMSWriter jms;
	
	private final Random        rand  = new Random();
	private final UUIDGenerator uuids = new UUIDGenerator();  
	private final Logger        log   = Logger.make( getClass() );
}
