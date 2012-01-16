package tu.space.worker;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.middleware.Input;
import tu.space.middleware.Middleware;

public class DefectTester extends Tester {

	public DefectTester( String id, Middleware m ) {
		super( id, m );
	}

	@Override 
	protected Input<Computer> getInput() { 
		return mw.getComputersUntestedForDefect();
	}

	@Override
	protected boolean isUntested( Computer pc ) { 
		return pc.defect == TestStatus.UNTESTED;
	}

	@Override 
	protected boolean isOK( Computer c ) { 
		return !c.hasDefect();
	}

	@Override
	protected Computer tag( Computer c ) {
		return c.tagAsTestedForDefect( id, c.hasDefect() ? TestStatus.YES : TestStatus.NO );
	}
}
