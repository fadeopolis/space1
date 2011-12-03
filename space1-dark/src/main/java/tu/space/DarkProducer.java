package tu.space;

import tu.space.components.Component;
import tu.space.middleware.JMSMiddlewareFactory;
import tu.space.middleware.Middleware;
import tu.space.utils.Logger;
import tu.space.workers.Producer;

public final class DarkProducer {
	public static final int MAX_SLEEP_TIME = 1;
	public static final String USAGE = 
			"usage: producer ID TYPE QUOTA ERROR_RATE\n"      +
			"  where TYPE is one of CPU, GPU, MAINBOARD, RAM"
	;
	
	public static void main( String... args ) throws InterruptedException {
		if ( args.length != 4 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id        = args[0];
		final int    quota     = Integer.parseInt( args[2] );
		final double errorRate = Double.parseDouble( args[3] );
		
		Component.Factory<?> factory;
		if ( "CPU".equalsIgnoreCase( args[1] ) )            factory = new Component.CpuFactory();
		else if ( "GPU".equalsIgnoreCase( args[1] ) )       factory = new Component.CpuFactory();
		else if ( "MAINBOARD".equalsIgnoreCase( args[1] ) ) factory = new Component.CpuFactory();
		else if ( "RAM".equalsIgnoreCase( args[1] ) )       factory = new Component.CpuFactory();
		else {
			System.err.println( USAGE );
			System.exit( 1 );
			return;
		}
		
		Logger.configure();

		Middleware m = new JMSMiddlewareFactory().make();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Producer<?> p = new Producer( id, m, factory, quota, errorRate );
		
		m.start();
		
		p.run();
	}	
}
