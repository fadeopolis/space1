package tu.space.light;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.util.ComputerManager;
import tu.space.util.CpuManager;
import tu.space.util.GpuManager;
import tu.space.util.LogBack;
import tu.space.util.MainboardManager;
import tu.space.util.RamManager;
import tu.space.utils.Logger;
import tu.space.utils.UUIDGenerator;

/**
 * The manufacturer will be notified if a component was built, constructs a
 * computer and set a notification if pc is built.
 * 
 * @author system
 * 
 */
public class Manufacturer extends Processor<Component> {

	public Manufacturer( String id, Capi capi, int space ) throws MzsCoreException {
		super(id, capi, space);

		pcs  = new ComputerManager( this.space, capi );
		cpus = new CpuManager( this.space, capi );
		gpus = new GpuManager( this.space, capi );
		mbds = new MainboardManager( this.space, capi );
		rams = new RamManager( this.space, capi );
	}

	// container refs
	private final ComputerManager  pcs;
	private final CpuManager       cpus;
	private final GpuManager       gpus;
	private final MainboardManager mbds;
	private final RamManager       rams;
	
	private final UUIDGenerator uuids = new UUIDGenerator();

	public static void main(String[] args) throws MzsCoreException {
		Logger.configure();
		LogBack.configure();

		if ( args.length != 2 ) {
			System.err.println("usage: Manufacturer NAME PORT" );
			System.exit( 1 );
		} else {
			try{
				Integer.parseInt(args[1]);
			} catch (NumberFormatException e){
				System.err.println("usage: Manufacturer NAME PORT, Port is not a number");
			}
		}
		
		String workerId = args[0];
		Capi   capi     = new Capi( DefaultMzsCore.newInstance( 0 ) );
		int    space    = Integer.parseInt( args[1] );
		
		Manufacturer mySm = new Manufacturer( workerId, capi, space );

		mySm.buildContractOnStartUp();
		mySm.run();
	}

	private void buildContractOnStartUp(){
		TransactionReference tx = null;
		try {
			while ( true ) {
				tx = beginTransaction();
				
				tryToBuildPc( tx );
				
				//commit after loop
				commit( tx );				
			}
		} catch (MzsCoreException e) {
			//no order found nothing to produce here
			rollback( tx );
		}
	}

	@Override
	protected void registerNotifications() {
		registerNotification(cpus, Operation.WRITE);
		registerNotification(gpus, Operation.WRITE);
		registerNotification(mbds, Operation.WRITE);
		registerNotification(rams, Operation.WRITE);
	}

	@Override
	protected boolean shouldProcess(Component e, Operation o,
			List<CoordinationData> cds) {
		return true;
	}

	@Override
	protected boolean process(Component e, Operation op, List<CoordinationData> cds, TransactionReference tx) throws MzsCoreException {
		try {
			tryToBuildPc( tx );

			// if we got here we can commit, so return true
			return true;
		} catch ( CountNotMetException ex ) {
			log.debug( "%s: Not enough parts for a PC", this );
			rollback( tx );
			// if we got here we have rolled back, so return false
			return false;
		}
	}

	private synchronized void tryToBuildPc( TransactionReference tx ) throws MzsCoreException {
		boolean builtOne = false;
		for ( Order o : orders.allOrders( tx ) ) {
			try {
				if ( o.shouldBuildMore() ) {
					tryToBuildPcForOrder( o );

					// if we get here, a pc was built
					builtOne = true;
					o = o.incProduced();
					break;
				}
			} catch ( CountNotMetException e ) {
				log.debug( "%s: not enough parts for order %s", this, o.id );
			} finally {
				try { orders.returnOrder( tx, o ); } catch ( TransactionException e ) {}
			}
		}
		
		if ( !builtOne ) tryToBuildPcWithoutOrder( tx );
	}
	
	private synchronized void tryToBuildPcForOrder( Order o ) throws MzsCoreException {
		TransactionReference tx = null;
		try {
			tx = beginTransaction();
			
			doTryToBuildPcForOrder( tx, o );
			log.info( "%s built a PC for order %s", this, o.id );
		} catch ( CountNotMetException e ) {
			rollback( tx );
			throw e;
		}
	}
	private void doTryToBuildPcForOrder( TransactionReference tx, Order o ) throws MzsCoreException {
		// mandatory take parts
		Mainboard mbd = mbds.take( tx );
					
		//Build spec. cpu-type
		Cpu cpu = cpus.take( tx, o.cpuType );
				
		//precondition ram has to be 1, 2 or 4, spec. in order ram quantity field
		List<RamModule> rams = this.rams.take( tx, o.ramQuantity );
			
		// optional parts
		Gpu gpu = null;
		if( o.gpu ){
			gpu = gpus.take( tx );
		}
					
		// assemble pc
		Computer pc = new Computer(uuids.generate(), workerId, o.id, cpu, gpu, mbd, rams);
		
		pcs.write( tx, pc );
					
		commit( tx );		
	}
	
	private synchronized void tryToBuildPcWithoutOrder( TransactionReference tx ) throws MzsCoreException {
		// mandatory take parts
		Cpu       cpu = cpus.take( tx );
		Mainboard mbd = mbds.take( tx );
		RamModule ram = rams.take( tx );

		// optional parts
		Gpu gpu;
		try {
			gpu = gpus.take( tx );
		} catch (CountNotMetException e) {
			gpu = null;
		}

		// ram is a bitch
		List<RamModule> rams;
		try {
			rams = this.rams.take( tx, 3 );
		} catch (CountNotMetException e1) {
			try {
				rams = this.rams.take( tx, 1 );
			} catch (CountNotMetException e2) {
				rams = new ArrayList<RamModule>();
			}		
		}		
		rams.add( ram );

		// assemble pc
		pcs.write( tx, new Computer( uuids.generate(), workerId, null, cpu, gpu, mbd, rams ) );
		log.info( "%s built a PC", this );
	}
}