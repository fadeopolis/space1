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

public class DefectTester extends Tester {

	public static void main(String... args) throws MzsCoreException {
		Logger.configure();	
		LogBack.configure();

		if ( args.length != 2 ) {
			System.err.println("usage: DefectTester NAME PORT" );
			System.exit( 1 );
		} else {
			try{
				Integer.parseInt(args[1]);
			} catch (NumberFormatException e){
				System.err.println("usage: DefectTester NAME PORT, Port is not a number");
			}
		}
		
		String workerId = args[0];
		Capi   capi     = new Capi( DefaultMzsCore.newInstance( 0 ) );
		int    space    = Integer.parseInt( args[1] );
		
		new DefectTester( workerId, capi, space ).run();
	}

	public DefectTester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
	}

	@Override
	protected LabelSelector testLabel() {
		return SELECTOR_UNTESTED_FOR_DEFECT;
	}

	@Override
	protected boolean isOK( Computer c ) {
		return !c.hasDefect();
	}

	@Override
	protected Computer tag( Computer c ) {
		return c.tagAsTestedForDefect( workerId, c.hasDefect() ? TestStatus.YES : TestStatus.NO );
	}
	
	@Override
	protected boolean shouldProcess( Computer e, Operation o, List<CoordinationData> cds ) {
		return e.defect == TestStatus.UNTESTED;
	}


}
