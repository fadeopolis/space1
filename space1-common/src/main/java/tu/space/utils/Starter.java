package tu.space.utils;

import java.lang.reflect.Constructor;

import tu.space.middleware.Middleware;

public abstract class Starter {
	public void start( Class<?> c, String[] args ) throws Exception {
		if ( args.length != 2 ) {
			System.err.println("Usage: " + c.getSimpleName() + " ID PORT" );
			System.exit( 1 );
		}
		String id   = args[0];
		int    port = Integer.parseInt( args[1] );
		
		start( c, id, port );
	}
	public void start( Class<?> c, String id, int port ) throws Exception {
		Logger.configure();
		
		Logger log = Logger.make( c );
		
		log.info( "STARTING %s %s ON PORT %d", c.getSimpleName(), id, port );
		
		Constructor<?> cons = c.getConstructor( String.class, Middleware.class );
		Middleware m = makeMiddleware( port );
		
		// start worker
		cons.newInstance( id, m );
	}
	
	protected abstract Middleware makeMiddleware( int port );
}
