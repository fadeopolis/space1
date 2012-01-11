package tu.space.light;

import static tu.space.util.ContainerCreator.*;

import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.components.RamModule;
import tu.space.util.LogBack;
import tu.space.utils.Logger;

public class DefectTester extends Tester {

	public static void main(String... args) throws MzsCoreException {
		Logger.configure();	
		LogBack.configure();

		new DefectTester( args ).run();
	}

	public DefectTester( String... args ) throws MzsCoreException {
		super( args );
	}
	public DefectTester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
	}

	@Override
	protected void testPc( TransactionReference tx ) throws MzsCoreException {
		Computer pc = (Computer) capi.take( pcs, SELECTOR_UNTESTED_FOR_DEFECT, RequestTimeout.TRY_ONCE, tx ).get( 0 );

		if ( pc.hasDefect() ) {
			log.info( "%s: Got a defect PC %s", this, pc );
			
			pc = pc.tagAsTestedForDefect( workerId, TestStatus.YES );

			// dismantle defect PC
			if ( pc.cpu       != null && !pc.cpu.hasDefect       ) capi.write( cpus, new Entry( pc.cpu ) );
			if ( pc.gpu       != null && !pc.gpu.hasDefect       ) capi.write( gpus, new Entry( pc.gpu ) );
			if ( pc.mainboard != null && !pc.mainboard.hasDefect ) capi.write( mbds, new Entry( pc.mainboard ) );
			for ( RamModule ram : pc.ramModules )
				if ( !ram.hasDefect ) capi.write( rams, new Entry( ram ) );
			
		} else {
			log.info( "%s: Got a working PC %s", workerId, pc );

			pc = pc.tagAsTestedForDefect( workerId, TestStatus.NO );
			
			writePc( pcs, pc );
		}
	}

	@Override
	protected boolean shouldProcess( Computer e, Operation o, List<CoordinationData> cds ) {
		return e.defect == TestStatus.UNTESTED;
	}
}
