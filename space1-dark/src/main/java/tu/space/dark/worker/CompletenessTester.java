package tu.space.dark.worker;

import tu.space.components.Computer;
import tu.space.middleware.Middleware;
import tu.space.dark.JMSInput;
import tu.space.dark.worker.DarkTester;

public class CompletenessTester extends DarkTester {

	public CompletenessTester( String id, Middleware m ) {
		super( id, m );
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
	protected JMSInput<Computer> input() {
		return (JMSInput<Computer>) mw.getComputersUntestedForCompleteness();
	}
}
