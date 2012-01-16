package tu.space.worker;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamList;
import tu.space.components.RamModule;
import tu.space.contracts.PcSpec;
import tu.space.middleware.CpuInput;
import tu.space.middleware.Input;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;
import tu.space.middleware.Middleware.Operation;
import tu.space.middleware.Output;
import tu.space.middleware.RamInput;

public final class Manufacturer extends Worker {

	public Manufacturer( String id, Middleware m ) {
		super( id, m );
		
		this.pcs  = mw.getComputerOutput();
		this.cpus = mw.getCpuInput();
		this.gpus = mw.getGpuInput();
		this.mbds = mw.getMainboardInput();
		this.rams = mw.getRamInput();
		
//		m.registerComponentListener( Operation.CREATED, new Listener<Component>() {
//			@Override public void onEvent( Component p ) { onPartCreated( p ); }
//		});
		
		m.registerListener( Cpu.class,       Operation.CREATED, new Listener<Cpu>() {
			@Override public synchronized void onEvent( Cpu p ) { onPartCreated( p ); }
		} );
		m.registerListener( Gpu.class,       Operation.CREATED, new Listener<Gpu>() {
			@Override public synchronized void onEvent( Gpu p ) { onPartCreated( p ); }
		} );
		m.registerListener( Mainboard.class, Operation.CREATED, new Listener<Mainboard>() {
			@Override public synchronized void onEvent( Mainboard p ) { onPartCreated( p ); }
		} );
		m.registerListener( RamModule.class, Operation.CREATED, new Listener<RamModule>() {
			@Override public synchronized void onEvent( RamModule p ) { onPartCreated( p ); }
		} );
	}

	private synchronized void onPartCreated( Component c ) {
		// try building for an order
		for ( Input<PcSpec> order : mw.orders() ) {
			mw.beginTransaction();
			
			PcSpec spec = order.take();
			
			if ( spec == null ) {
				mw.rollbackTransaction();
				continue;
			}
			
			Computer pc = buildPcForSpec( spec );
		
			if ( pc == null ) {
				mw.rollbackTransaction();
			} else {
				pcs.write( pc );
				mw.commitTransaction();
				log.info( "%s: Built PC %s for order %s", this, pc.id, spec.orderId );
				return;
			}
		}
		
		// if we get here no PC was built
		
		mw.beginTransaction();
		
		Computer pc = buildPc();
		
		if ( pc == null ) {
			mw.rollbackTransaction();
		} else {
			pcs.write( pc );
			mw.commitTransaction();
			log.info( "%s: Built PC %s", this, pc.id );
		}
	}
	
	private Computer buildPcForSpec( PcSpec spec ) {
		Cpu       cpu = cpus.take( spec.cpuType );
		Gpu       gpu = spec.needGpu ? gpus.take() : null;
		Mainboard mbd = mbds.take();
		RamList   ram = rams.take( spec.numRams );
		
		if ( cpu == null || mbd == null || ram == null || (spec.needGpu && gpu == null) ) return null;		
		
		return make( spec.orderId, cpu, gpu, mbd, ram );
	}
	private Computer buildPc() {
		Cpu       cpu = cpus.take();
		Gpu       gpu = gpus.take();
		Mainboard mbd = mbds.take();
		RamList   ram = rams.take();

		if ( cpu == null || mbd == null || ram == null ) return null;
		
		return make( null, cpu, gpu, mbd, ram );
	}
	
	private Computer make( String orderId, Cpu cpu, Gpu gpu, Mainboard mbd, RamList ram ) {
		return new Computer( genUUID(), id, orderId, cpu, gpu, mbd, ram );
	}
	
	private final CpuInput         cpus;
	private final Input<Gpu>       gpus;
	private final Input<Mainboard> mbds;
	private final RamInput         rams;
	
	private final Output<Computer> pcs;
}
