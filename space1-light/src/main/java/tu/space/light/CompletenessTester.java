package tu.space.light;

import static tu.space.util.ContainerCreator.*;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.components.RamModule;
import tu.space.util.ContainerCreator;
import tu.space.util.LogBack;
import tu.space.utils.Logger;

public class CompletenessTester extends Worker {

	public static void main(String... args) throws MzsCoreException {
		Logger.configure();	
		LogBack.configure();

		new CompletenessTester( args ).run();
	}

	public CompletenessTester( String... args ) throws MzsCoreException {
		super( args );
		
		pcs  = ContainerCreator.getPcContainer(   this.space, capi );
		cpus = ContainerCreator.getCpuContainer( this.space, capi );
		gpus = ContainerCreator.getGpuContainer( this.space, capi );
		mbds = ContainerCreator.getMainboardContainer( this.space, capi );
		rams = ContainerCreator.getRamContainer( this.space, capi );
	}
	public CompletenessTester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
		
		pcs  = ContainerCreator.getPcContainer(   this.space, capi );
		cpus = ContainerCreator.getCpuContainer( this.space, capi );
		gpus = ContainerCreator.getGpuContainer( this.space, capi );
		mbds = ContainerCreator.getMainboardContainer( this.space, capi );
		rams = ContainerCreator.getRamContainer( this.space, capi );
	}

	public void run() {
		TransactionReference tx = null; 
		try {
			while ( true ) {
				tx = capi.createTransaction( DEFAULT_TX_TIMEOUT, space );
				
				Computer pc;
				try {
					pc = (Computer) capi.take( pcs, SELECTOR_UNTESTED_FOR_COMPLETENESS, RequestTimeout.INFINITE, tx ).get( 0 );
				} catch ( CountNotMetException e ) {
					// no part available
					rollback( tx );
					continue;
				}

				List<CoordinationData> cd = new ArrayList<CoordinationData>();

				if ( !pc.isComplete() ) {
					log.info( "%s: Got an incomplete PC %s", this, pc.id );
					
					pc = pc.tagAsTestedForCompleteness( workerId, TestStatus.YES );

					// dismantle defect PC
					if ( pc.cpu       != null && !pc.cpu.hasDefect       ) capi.write( cpus, new Entry( pc.cpu ) );
					if ( pc.gpu       != null && !pc.gpu.hasDefect       ) capi.write( gpus, new Entry( pc.gpu ) );
					if ( pc.mainboard != null && !pc.mainboard.hasDefect ) capi.write( mbds, new Entry( pc.mainboard ) );
					for ( RamModule ram : pc.ramModules )
						if ( !ram.hasDefect ) capi.write( rams, new Entry( ram ) );
					
					cd.add( label("DUMMY") );
				} else {
					log.info( "%s: Got a complete PC %s", workerId, pc.id );

					pc = pc.tagAsTestedForCompleteness( workerId, TestStatus.NO );
					if ( pc.defect == TestStatus.UNTESTED ) cd.add( LABEL_UNTESTED_FOR_DEFECT );
				}

				capi.write( pcs, new Entry( pc, cd ) );
				
				capi.commitTransaction( tx );
			}
		} catch ( Exception e ) {
			rollback( tx );
			e.printStackTrace();
		} finally {
			clean();
		}
	}
//	
//	@Override
//	protected void registerNotifications() {
//		registerNotification( pcs, Operation.WRITE );
//	}
//
//	@Override
//	protected boolean shouldProcess( Computer pc, Operation o, List<CoordinationData> cds ) {
//		return pc.defect == TestStatus.UNTESTED;
//	}
//	
//	@Override
//	protected void process( Computer pc, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException {
//		// remove this PC from the space
//		capi.delete( pcs, LabelCoordinator.newSelector( "Computer.id:" + pc.id ), DEFAULT_TX_TIMEOUT, tx );
//		
//		List<CoordinationData> data = new ArrayList<CoordinationData>( cds );
//		
//		if ( pc.hasDefect() ){
//			pc = pc.tagAsTestedForDefect(workerId, TestStatus.YES);
//
//			if ( !pc.cpu.hasDefect )       capi.write( cpus, new Entry( pc.cpu ) );
//			if ( !pc.gpu.hasDefect )       capi.write( gpus, new Entry( pc.gpu ) );
//			if ( !pc.mainboard.hasDefect ) capi.write( mbds, new Entry( pc.mainboard ) );
//			for ( RamModule ram : pc.ramModules ) 
//				if ( !ram.hasDefect ) capi.write( rams, new Entry( ram ) );
//			
//			data.add( LABEL_DEFECT );
//		} else {
//			pc = pc.tagAsTestedForDefect(workerId, TestStatus.NO);
//		}
//			
//		data.remove( LABEL_UNTESTED_FOR_DEFECT );
//		
//		capi.write( pcs, new Entry( pc, data ) );
//		log.info("Tester: %s, tested error of PC: %s", workerId, pc.id );
//	}
//	
//	public void onStartUp(){
//		try {
//			TransactionReference tx = capi.createTransaction(5000, space);
//
//			ArrayList<Computer> computers = capi.take(pcs, Arrays.asList(LabelCoordinator.newSelector(STR_UNTESTED_FOR_DEFECT, MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO, tx);
//			
//			for(Computer pc: computers){
//				// remove this PC from the space
//				capi.delete( pcs, LabelCoordinator.newSelector( "Computer.id:" + pc.id ), DEFAULT_TX_TIMEOUT, tx );
//				
//				List<CoordinationData> data = new ArrayList<CoordinationData>();
//				
//				if ( pc.hasDefect() ){
//					pc = pc.tagAsTestedForDefect(workerId, TestStatus.YES);
//
//					if ( !pc.cpu.hasDefect )       capi.write( cpus, new Entry( pc.cpu ) );
//					if ( !pc.gpu.hasDefect )       capi.write( gpus, new Entry( pc.gpu ) );
//					if ( !pc.mainboard.hasDefect ) capi.write( mbds, new Entry( pc.mainboard ) );
//					for ( RamModule ram : pc.ramModules ) 
//						if ( !ram.hasDefect ) capi.write( rams, new Entry( ram ) );
//					
//					data.add( LABEL_DEFECT );
//				} else {
//					pc = pc.tagAsTestedForDefect(workerId, TestStatus.NO);
//				}
//					
//				data.remove( LABEL_UNTESTED_FOR_DEFECT );
//				
//				capi.write( pcs, new Entry( pc, data ) );
//				log.info("Tester: %s, tested error of PC: %s", workerId, pc.id );
//			}
//			
//			capi.commitTransaction(tx);
//		} catch (MzsCoreException e) {
//			log.error("Error at collecting pc's from startup");
//		}
//	}
//	
	private final ContainerReference pcs;
	private final ContainerReference cpus;
	private final ContainerReference gpus;
	private final ContainerReference mbds;
	private final ContainerReference rams;
}
