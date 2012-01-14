package tu.space.light;

import static tu.space.util.ContainerCreator.*;

import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.util.LogBack;
import tu.space.utils.Logger;

public class CompletenessTester extends Tester {

	public static void main(String... args) throws MzsCoreException {
		Logger.configure();	
		LogBack.configure();

		if ( args.length != 2 ) {
			System.err.println("usage: CompletenessTester NAME PORT" );
			System.exit( 1 );
		} else {
			try{
				Integer.parseInt(args[1]);
			} catch (NumberFormatException e){
				System.err.println("usage: CompletenessTester NAME PORT, Port is not a number");
			}
		}
		
		String workerId = args[0];
		Capi   capi     = new Capi( DefaultMzsCore.newInstance( 0 ) );
		int    space    = Integer.parseInt( args[1] );
		
		new CompletenessTester( workerId, capi, space ).run();
	}

	public CompletenessTester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
	}

	@Override
	protected LabelSelector testLabel() {
		return SELECTOR_UNTESTED_FOR_COMPLETENESS;
	}
	@Override
	protected boolean isOK( Computer c ) {
		return c.isComplete();
	}

	@Override
	protected Computer tag( Computer c ) {
		return c.tagAsTestedForCompleteness( workerId, c.isComplete() ? TestStatus.YES : TestStatus.NO );
	}
	
	@Override
	protected boolean shouldProcess( Computer e, Operation o, List<CoordinationData> cds ) {
		return e.complete == TestStatus.UNTESTED;
	}

}
