package tu.space.workers;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;

/**
 *
 * The lowest form of life on earth a hardware/software tester.
 *
**/
public class CompletenessTester extends Worker {
	public CompletenessTester( String id, Middleware m ) {
		super( id, m );
	}

	@Override
	protected final void doRun() {
		middleware.computersUntestedForCompleteness().setConsumingListener( new Listener<Computer>() {
			@Override
			public void handle( Computer c ) {
				TestStatus status = c.isComplete() ? TestStatus.YES : TestStatus.NO;
					
				middleware.allComputers().send( c.tagAsTestedForCompleteness( id, status ) );
				middleware.commit();
				
				LOG.info("%s tested a computer", this);
			}
		});
		middleware.start();

		waitForNewLine();
	}
}
