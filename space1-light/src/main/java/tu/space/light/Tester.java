package tu.space.light;

import static tu.space.util.ContainerCreator.selector;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.RamModule;
import tu.space.util.ComputerManager;
import tu.space.util.CpuManager;
import tu.space.util.GpuManager;
import tu.space.util.MainboardManager;
import tu.space.util.RamManager;


public abstract class Tester extends Processor<Computer> {

	public Tester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
		
		pcs  = new ComputerManager( this.space, capi );
		cpus = new CpuManager( this.space, capi );
		gpus = new GpuManager( this.space, capi );
		mbds = new MainboardManager( this.space, capi );
		rams = new RamManager( this.space, capi );
	}

	protected abstract LabelSelector testLabel();
	protected abstract boolean       isOK( Computer c );
	protected abstract Computer      tag( Computer c );
	
	protected final ComputerManager  pcs;
	protected final CpuManager       cpus;
	protected final GpuManager       gpus;
	protected final MainboardManager mbds;
	protected final RamManager       rams;
	
	private void testPc( Computer pc, TransactionReference tx ) throws MzsCoreException {
		List<LabelSelector> cd = new ArrayList<LabelSelector>();
		cd.add( testLabel() );
		if ( pc.orderId != null ) { cd.add( selector(pc.orderId) ); }
			
		pc = pcs.takeOne( tx, cd );

		pc = tag( pc );

		if ( isOK( pc ) ) {
			log.info( "%s: Got a working PC %s", workerId, pc );
				
			pcs.write( tx, pc );
		} else {
			log.info( "%s: Got a defect PC %s", this, pc );
			
			if ( pc.cpu       != null && !pc.cpu.hasDefect       )      cpus.write( tx, pc.cpu );
			if ( pc.gpu       != null && !pc.gpu.hasDefect       )      gpus.write( tx, pc.gpu );
			if ( pc.mainboard != null && !pc.mainboard.hasDefect )      mbds.write( tx, pc.mainboard );
			for ( RamModule ram : pc.ramModules ) if ( !ram.hasDefect ) rams.write( tx, ram );			
		}
	}

	@Override
	protected void registerNotifications() throws MzsCoreException {
		TransactionReference tx = null; 
		try {
			while ( true ) {
				tx = beginTransaction();
				
				Computer pc = pcs.take( tx, testLabel() );
				
				testPc( pc, tx );
				
				commit( tx );
			}
		} catch ( CountNotMetException e ) {
			rollback( tx );
		}
		
		registerNotification( pcs, Operation.WRITE );
	}
	@Override
	protected boolean process( Computer pc, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException {
		try {
			testPc( pc, tx );
			return true;			
		} catch ( CountNotMetException e ) {
			rollback( tx );
			return false;
		}
	}

}
