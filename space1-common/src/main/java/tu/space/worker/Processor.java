package tu.space.worker;

import tu.space.components.Product;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;

public abstract class Processor<P extends Product> extends Worker implements Listener<P>, Runnable {

	public Processor( String id, Middleware m ) {
		super( id, m );
	}

	@Override
	public void run() {
		// process on startup
		boolean keepGoing;
		do {
			keepGoing = process( null );
		} while ( keepGoing );
		
		// process all incoming stuff
		registerListener();		
	}
	
	@Override
	public synchronized void onEvent( P p ) {
		process( p );
	}
	
	protected abstract void    registerListener();
	protected abstract boolean process( P p );
}
