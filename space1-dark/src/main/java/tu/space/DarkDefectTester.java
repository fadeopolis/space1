package tu.space;

import tu.space.middleware.JMSMiddlewareFactory;
import tu.space.middleware.Middleware;
import tu.space.utils.Logger;
import tu.space.workers.DefectTester;

public class DarkDefectTester {
	public static final String USAGE = "usage: defect-tester ID";
	
	public static void main( String... args ) {
		if ( args.length != 1 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id = args[0];
		
		Logger.configure();

		final Middleware m = new JMSMiddlewareFactory().make();
		
		new DefectTester( id, m ).run();
	}
}
