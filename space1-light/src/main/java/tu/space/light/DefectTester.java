package tu.space.light;

import static tu.space.util.ContainerCreator.DEFAULT_TX_TIMEOUT;
import static tu.space.util.ContainerCreator.LABEL_DEFECT;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_DEFECT;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.RamModule;
import tu.space.components.Computer.TestStatus;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;

public class DefectTester extends Processor<Computer> {

	public static void main(String... args) throws MzsCoreException {
		Logger.configure();	

		new DefectTester( args ).run();
	}

	public DefectTester( String... args ) throws MzsCoreException {
		super( args );
		
		pcs = ContainerCreator.getPcContainer( this.space, capi );
		cpus = ContainerCreator.getCpuContainer( this.space, capi );
		gpus = ContainerCreator.getGpuContainer( this.space, capi );
		mbds = ContainerCreator.getMainboardContainer( this.space, capi );
		rams = ContainerCreator.getRamContainer( this.space, capi );
	}
	public DefectTester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
		
		pcs = ContainerCreator.getPcContainer( this.space, capi );
		cpus = ContainerCreator.getCpuContainer( this.space, capi );
		gpus = ContainerCreator.getGpuContainer( this.space, capi );
		mbds = ContainerCreator.getMainboardContainer( this.space, capi );
		rams = ContainerCreator.getRamContainer( this.space, capi );
	}

	@Override
	protected void registerNotifications() {
		registerNotification( pcs, Operation.WRITE );
	}

	@Override
	protected boolean shouldProcess( Computer pc, Operation o, List<CoordinationData> cds ) {
		return pc.defect == TestStatus.UNTESTED;
	}
	
	@Override
	protected void process( Computer pc, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException {
		// remove this PC from the space
		capi.delete( pcs, LabelCoordinator.newSelector( "Computer.id:" + pc.id ), DEFAULT_TX_TIMEOUT, tx );
		
		List<CoordinationData> data = new ArrayList<CoordinationData>( cds );
		
		if ( pc.hasDefect() ){
			pc = pc.tagAsTestedForDefect(workerId, TestStatus.YES);

			if ( !pc.cpu.hasDefect )       capi.write( cpus, new Entry( pc.cpu ) );
			if ( !pc.gpu.hasDefect )       capi.write( gpus, new Entry( pc.gpu ) );
			if ( !pc.mainboard.hasDefect ) capi.write( mbds, new Entry( pc.mainboard ) );
			for ( RamModule ram : pc.ramModules ) 
				if ( !ram.hasDefect ) capi.write( rams, new Entry( ram ) );
			
			data.add( LABEL_DEFECT );
		} else {
			pc = pc.tagAsTestedForDefect(workerId, TestStatus.NO);
		}
			
		data.remove( LABEL_UNTESTED_FOR_DEFECT );
		
		capi.write( pcs, new Entry( pc, data ) );
		log.info("Tester: %s, tested error of PC: %s", workerId, pc.id );
	}
	
	private final ContainerReference pcs;
	private final ContainerReference cpus;
	private final ContainerReference gpus;
	private final ContainerReference mbds;
	private final ContainerReference rams;
}
