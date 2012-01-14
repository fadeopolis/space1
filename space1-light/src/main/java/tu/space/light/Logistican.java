package tu.space.light;

import static tu.space.util.ContainerCreator.SELECTOR_TESTED_FOR_COMPlETENESS;
import static tu.space.util.ContainerCreator.SELECTOR_TESTED_FOR_DEFECT;
import static tu.space.util.ContainerCreator.selector;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.util.ComputerManager;
import tu.space.util.CpuManager;
import tu.space.util.GpuManager;
import tu.space.util.LogBack;
import tu.space.util.MainboardManager;
import tu.space.util.OrderManager;
import tu.space.util.RamManager;
import tu.space.util.StorageManager;
import tu.space.util.TrashManager;
import tu.space.utils.Logger;

public class Logistican extends Processor<Computer> {
	
	public static void main(String... args) throws MzsCoreException {
		Logger.configure();
		LogBack.configure();

		if ( args.length != 2 ) {
			System.err.println("usage: Logistician NAME PORT" );
			System.exit( 1 );
		} else {
			try{
				Integer.parseInt(args[1]);
			} catch (NumberFormatException e){
				System.err.println("usage: Logistician NAME PORT, Port is not a number");
			}
		}
		
		String workerId = args[0];
		Capi   capi     = new Capi( DefaultMzsCore.newInstance( 0 ) );
		int    space    = Integer.parseInt( args[1] );
		
		new Logistican( workerId, capi, space ).run();
	}

	public Logistican(String id, Capi capi, int space) throws MzsCoreException {
		super(id, capi, space);

		pcs  = new ComputerManager( this.space, capi );
		cpus = new CpuManager( this.space, capi );
		gpus = new GpuManager( this.space, capi );
		mbds = new MainboardManager( this.space, capi );
		rams = new RamManager( this.space, capi );
		orders  = new OrderManager( this.space, capi );
		trash   = new TrashManager( this.space, capi );
		storage = new StorageManager( this.space, capi );
	}

	public void storePc( Computer c, TransactionReference tx ) throws MzsCoreException {
		List<LabelSelector> cd = new ArrayList<LabelSelector>();
		cd.add( SELECTOR_TESTED_FOR_DEFECT );
		cd.add( SELECTOR_TESTED_FOR_COMPlETENESS );
		if ( c.orderId != null ) { cd.add( selector(c.orderId) ); }
		
		Computer pc = pcs.takeOne( tx, cd );

		pc = pc.tagAsFinished( workerId );

		if ( !pc.isComplete() || pc.hasDefect() ) {
			log.info( "%s: Got an bad PC %s", this, pc.id );
			
			trash.write( tx, pc );
		} else {
			log.info( "%s: Got a mighty fine PC %s", this, pc.id );
						
			if ( pc.orderId != null ) orders.signalPcIsFinished( tx, pc );
			
			storage.write( tx, pc );
		}
	}

	@Override
	protected void registerNotifications() throws MzsCoreException {
		TransactionReference tx = null; 
		try {
			while ( true ) {
				tx = beginTransaction();
				
				List<LabelSelector> cd = new ArrayList<LabelSelector>();
				cd.add( SELECTOR_TESTED_FOR_DEFECT );
				cd.add( SELECTOR_TESTED_FOR_COMPlETENESS );

				Computer pc = pcs.takeOne( tx, cd );
				
				storePc( pc, tx );
				
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
			storePc( pc, tx );
			return true;			
		} catch ( CountNotMetException e ) {
			rollback( tx );
			return false;
		}
	}
	
	@Override
	protected boolean shouldProcess( Computer e, Operation o, List<CoordinationData> cds ) {
		return e.complete != TestStatus.UNTESTED && e.defect != TestStatus.UNTESTED;
	}

	protected final ComputerManager  pcs;
	protected final CpuManager       cpus;
	protected final GpuManager       gpus;
	protected final MainboardManager mbds;
	protected final RamManager       rams;
	protected final OrderManager     orders;
	protected final TrashManager     trash;
	protected final StorageManager   storage;
}
