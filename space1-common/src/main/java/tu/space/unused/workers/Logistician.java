package tu.space.unused.workers;

import tu.space.components.Computer;
import tu.space.unused.middleware.Listener;
import tu.space.unused.middleware.Middleware;


/**
 *
 * Fat guy with a truck, what else!
 *
**/
public class Logistician extends Worker {
	public Logistician( String id, Middleware m ) {
		super( id, m );
	}

	@Override
	protected final void doRun() {
		middleware.testedComputers().setConsumingListener( new Listener<Computer>() {
			@Override
			public void handle( Computer e ) {
				if ( e.hasDefect() ) 
					middleware.trash().send( e );
				else
					middleware.storage().send( e );
				
				middleware.commit();
			}
		});
		middleware.start();	
		
		waitForNewLine();
	}
}
