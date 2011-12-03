package tu.space;

import tu.space.middleware.JMSMiddlewareFactory;
import tu.space.middleware.Middleware;
import tu.space.utils.Logger;
import tu.space.workers.Manufacturer;

public class DarkManufacturer {
	public static final String USAGE = "usage: manufacturer ID";
	
	public static void main( String... args ) throws InterruptedException {
		if ( args.length != 1 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id = args[0];
		
		Logger.configure();

		final Middleware m = new JMSMiddlewareFactory().make();
		
		new Manufacturer( id, m ).run();
	}
}
