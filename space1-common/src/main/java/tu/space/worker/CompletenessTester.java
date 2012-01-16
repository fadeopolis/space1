package tu.space.worker;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.middleware.Input;
import tu.space.middleware.Middleware;

public class CompletenessTester extends Tester {

	public CompletenessTester( String id, Middleware m ) {
		super( id, m );
	}

	@Override 
	protected Input<Computer> getInput() { 
		return mw.getComputersUntestedForCompleteness();
	}

	@Override
	protected boolean isUntested( Computer pc ) {
		return pc.complete == TestStatus.UNTESTED;
	}

	@Override 
	protected boolean isOK( Computer c ) { 
		return c.isComplete();
	}

	@Override
	protected Computer tag( Computer c ) {
		return c.tagAsTestedForCompleteness( id, c.isComplete() ? TestStatus.YES : TestStatus.NO );
	}
}
