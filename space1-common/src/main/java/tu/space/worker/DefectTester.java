package tu.space.worker;

import tu.space.components.Computer;
import tu.space.middleware.Input;
import tu.space.middleware.Middleware;
import tu.space.middleware.Middleware.Operation;

public class DefectTester extends Tester {

	public DefectTester( String id, Middleware m ) {
		super( id, m );
	}

	@Override 
	protected Input<Computer> getInput() { 
		return mw.getComputersUntestedForDefect();
	}

	@Override 
	protected boolean isOK( Computer c ) { 
		return !c.hasDefect();
	}

	@Override
	protected Computer tag( Computer c ) {
		return c.tagAsTestedForDefect( id );
	}

	@Override
	protected void registerListener() {
		mw.registerListenerForComputersUntestedForDefect( Operation.CREATED, this );
	}
}
