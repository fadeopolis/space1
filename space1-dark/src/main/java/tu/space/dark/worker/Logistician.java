package tu.space.dark.worker;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.components.Product;
import tu.space.contracts.Order;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;
import tu.space.middleware.OrderTracker;
import tu.space.middleware.Output;
import tu.space.dark.JMSInput;
import tu.space.dark.worker.DarkProcessor;

public class Logistician extends DarkProcessor<Computer> {
	public Logistician( final String id, Middleware m ) {
		super( id, m );

		storage = mw.getStorage();
		trash   = mw.getTrash();
		
		new OrderTracker( m, new Listener<Order>() {
			@Override
			public void onEvent( Order p ) {
				log.info( "%s: Order %s is done!", Logistician.this, p.id );
				mw.signalOrderIsDone( p );
			}
		});
	}
	
	@Override
	protected synchronized boolean process( Computer pc ) {
		mw.beginTransaction();
		
		if ( pc == null ) {
			// hack, sometimes the selectors won't get it, try without and test by hand
			pc = mw.getComputerInput().take();
		}
		
		if ( pc == null || pc.defect == TestStatus.UNTESTED || pc.complete == TestStatus.UNTESTED ) {
			return false;
		}
		
		pc = pc.tagAsFinished( id );
		
		if ( pc.hasDefect() || !pc.isComplete() ) {
			trash.write( pc );
		} else {
			storage.write( pc );
		}
		
		return true;
	}

	private final Output<Computer>  storage;
	private final Output<Product>   trash;

	@Override
	protected JMSInput<Computer> input() {
		return (JMSInput<Computer>) mw.getTestedComputers();
	}
}
