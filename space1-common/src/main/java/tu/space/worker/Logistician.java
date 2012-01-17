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

public class Logistician extends Processor<Computer> {
	public Logistician( final String id, Middleware m ) {
		super( id, m );
		
		pcIn    = mw.getTestedComputers();
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
	protected boolean process( Computer p ) {
		mw.beginTransaction();
		
		Computer pc = pcIn.take();

		if ( pc == null ) {
			// hack, sometimes the selectors won't get it, try without and test by hand
			pc = mw.getComputerInput().take();
		}
		
		if ( pc == null || pc.defect == TestStatus.UNTESTED || pc.complete == TestStatus.UNTESTED ) {
			mw.rollbackTransaction();
			return false;
		}
		
		pc = pc.tagAsFinished( id );
		
		if ( pc.hasDefect() || !pc.isComplete() ) {
			trash.write( pc );
		} else {
			storage.write( pc );
		}
		
		mw.commitTransaction();
		return true;
	}

	@Override
	protected void registerListener() {
		mw.registerTestedComputerListener( Operation.CREATED, this );
	}
	
	private final Input<Computer>   pcIn;
	private final Output<Computer>  storage;
	private final Output<Product>   trash;
}
