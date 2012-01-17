package tu.space.dark.worker;

import tu.space.components.Computer;
import tu.space.middleware.Middleware;
import tu.space.dark.JMSInput;
import tu.space.dark.worker.DarkTester;

public class DefectTester extends DarkTester {

	public DefectTester( String id, Middleware m ) {
		super( id, m );
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
	protected JMSInput<Computer> input() {
		return (JMSInput<Computer>) mw.getComputersUntestedForDefect();
	}
}
