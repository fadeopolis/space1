package tu.space.worker;

import tu.space.components.Computer;
import tu.space.middleware.Input;
import tu.space.middleware.Middleware;
import tu.space.middleware.Middleware.Operation;

public class CompletenessTester extends Tester {

	public CompletenessTester( String id, Middleware m ) {
		super( id, m );
	}

	@Override 
	protected Input<Computer> getInput() { 
		return mw.getComputersUntestedForCompleteness();
	}

	@Override 
	protected boolean isOK( Computer c ) { 
		return c.isComplete();
	}

	@Override
	protected Computer tag( Computer c ) {
		return c.tagAsTestedForCompleteness( id );
	}
	
	@Override
	protected void registerListener() {
		mw.registerListenerForComputersUntestedForCompleteness( Operation.CREATED, this );
	}
}
