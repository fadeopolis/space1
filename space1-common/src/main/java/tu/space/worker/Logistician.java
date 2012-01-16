package tu.space.worker;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.components.Product;
import tu.space.contracts.Order;
import tu.space.middleware.Input;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;
import tu.space.middleware.Middleware.Operation;
import tu.space.middleware.OrderTracker;
import tu.space.middleware.Output;

public class Logistician extends Worker implements Listener<Order> {
	public Logistician( final String id, Middleware m ) {
		super( id, m );
		
		pcIn    = mw.getTestedComputers();
		storage = mw.getStorage();
		trash   = mw.getTrash();
		
		new OrderTracker( m, this );
		
		// test on startup
		while ( true ) {
			mw.beginTransaction();
			
			Computer pc = pcIn.take();

			if ( pc == null ) {
				// hack, sometimes the selectors won't get it, try without and test by hand
				pc = mw.getComputerInput().take();
			}
			
			if ( pc == null || pc.defect == TestStatus.UNTESTED || pc.complete == TestStatus.UNTESTED ) {
				mw.rollbackTransaction();
				break;
			}
			
			pc = pc.tagAsFinished( id );
			
			if ( pc.hasDefect() || !pc.isComplete() ) {
				trash.write( pc );
			} else {
				storage.write( pc );
			}
			
			mw.commitTransaction();
		}
		
		// notifications
		mw.registerListener( Computer.class, Operation.CREATED, new Listener<Computer>() {
			@Override
			public synchronized void onEvent( Computer pc ) {
				if ( pc.defect != TestStatus.UNTESTED && pc.complete != TestStatus.UNTESTED ) {
					mw.beginTransaction();
					
					pc = pcIn.take();

					if ( pc == null ) {
						// hack, sometimes the selectors won't get it, try without and test by hand
						pc = mw.getComputerInput().take();
					}
					
					if ( pc == null || pc.defect == TestStatus.UNTESTED || pc.complete == TestStatus.UNTESTED ) {
						mw.rollbackTransaction();
						return;
					}
					
					pc = pc.tagAsFinished( id );
					
					if ( pc.hasDefect() || !pc.isComplete() ) {
						trash.write( pc );
					} else {
						storage.write( pc );
					}
					
					mw.commitTransaction();
				}
			}
		});
	}
	
	private final Input<Computer>   pcIn;
	private final Output<Computer>  storage;
	private final Output<Product>   trash;

	@Override
	public synchronized void onEvent( Order p ) {
		log.info( "%s: Order %s is done!", this, p.id );
		mw.signalOrderIsDone( p );
	}
}
