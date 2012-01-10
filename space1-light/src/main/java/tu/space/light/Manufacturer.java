package tu.space.light;

import static tu.space.util.ContainerCreator.ANY_MAX;
import static tu.space.util.ContainerCreator.FIFO_MAX;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_COMPLETENESS;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_DEFECT;
import static tu.space.util.ContainerCreator.any;
import static tu.space.util.ContainerCreator.fifo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.util.ContainerCreator;
import tu.space.util.LogBack;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;
import tu.space.utils.UUIDGenerator;

/**
 * The manufacturer will be notified if a component was built, constructs a computer
 * and set a notification if pc is built.
 * @author system
 *
 */
public class Manufacturer extends Processor<Component> {

	public Manufacturer( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );

		crefCpu        = ContainerCreator.getCpuContainer( this.space, capi );
		crefGpu        = ContainerCreator.getGpuContainer( this.space, capi );
		crefMainboards = ContainerCreator.getMainboardContainer( this.space, capi );
		crefRam        = ContainerCreator.getRamContainer( this.space, capi );
		crefPc         = ContainerCreator.getPcContainer( this.space, capi );
	}

	public Manufacturer( String[] args ) throws MzsCoreException {
		super( args );

		crefCpu        = ContainerCreator.getCpuContainer( this.space, capi );
		crefGpu        = ContainerCreator.getGpuContainer( this.space, capi );
		crefMainboards = ContainerCreator.getMainboardContainer( this.space, capi );
		crefRam        = ContainerCreator.getRamContainer( this.space, capi );
		crefPc         = ContainerCreator.getPcContainer( this.space, capi );
	}

	//container refs
	private final ContainerReference crefMainboards;
	private final ContainerReference crefCpu;
	private final ContainerReference crefGpu;
	private final ContainerReference crefRam;
	private final ContainerReference crefPc;

	private final UUIDGenerator uuids = new UUIDGenerator();
	
	/**
	 * @param String args[0] workerId()
	 * @throws MzsCoreException 
	 */
	public static void main(String[] args) throws MzsCoreException {
		Logger.configure();
		LogBack.configure();
		
		Manufacturer mySm = new Manufacturer(args);

		mySm.buildPcOnStartUp();

		mySm.run();
	}
	
	
	/**
	 * Collects all components and builds pc's if possible
	 */
	public void buildPcOnStartUp(){
		List<Computer> pcs = new ArrayList<Computer>();
		try{
			//crate transaction
			TransactionReference tx = capi.createTransaction(500000, space);
		
			/*
			 * read all entries of mainboard to decide how many pc's we can build
			 * from space.
			 */
			ArrayList<Component> readMainboards = capi.read(crefMainboards, Arrays.asList(FifoCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.INFINITE , tx);
			
			/*
			 * a trick to optimize the loop iterations, because we can only build as much pc's
			 * as we have core components like cpu and mainboards
			 * sizeof(mainboards)-sizeof(cpus) we do not consider ram for now
			 */
			int cpuAmount = 0; 
			int ramAmount = 0;
			int gpuAmount = 0;
			boolean once=true;
					
			for(int i=0;i<=readMainboards.size()-cpuAmount;i++){
				if(once){
					ArrayList<Component> readcpus = capi.read(crefMainboards, FIFO_MAX, RequestTimeout.ZERO , tx);
					ArrayList<Component> readram = capi.read(crefRam, ANY_MAX, RequestTimeout.ZERO, tx);
					ArrayList<Component> readgpu = capi.read(crefGpu, ANY_MAX, RequestTimeout.ZERO, tx);
					cpuAmount = readcpus.size();
					ramAmount = readram.size();
					gpuAmount = readgpu.size();
					once = false;
				}
				
				// as long as mainboard cpu and ram is available build a pc
				if(cpuAmount > 0 && ramAmount > 0){				
					//take mainboard and cpu
					ArrayList<Component> takeMainboard = capi.take(crefMainboards, fifo(1), MzsConstants.RequestTimeout.TRY_ONCE, tx);
					ArrayList<Component> takeCpu = capi.take(crefCpu, any(1), MzsConstants.RequestTimeout.TRY_ONCE, tx);
					cpuAmount--;
					
					//how many ram 
					List<RamModule> takeRams = null;
					switch(ramAmount){
						case 0:
							break;
						case 1:
							takeRams = capi.take(crefRam, any(1), RequestTimeout.TRY_ONCE, tx);
							ramAmount--;
							break;
						case 2:
							takeRams = capi.take(crefRam, any(2), RequestTimeout.TRY_ONCE, tx);
							ramAmount -= 2;
							break;
						case 3:
							//3 is one to much take 2
							takeRams = capi.take(crefRam, any(2), RequestTimeout.TRY_ONCE, tx);
							ramAmount -= 2;
							break;
						default :
							//well we have 4 or more then let us take 4
							takeRams = capi.take(crefRam, any(4), RequestTimeout.TRY_ONCE, tx);
							ramAmount -= 4;
							break;
					}
					log.info("Size of rams taken: %d, and size of remaining: %d\n", takeRams.size(), ramAmount);
					ArrayList<Component> takeGpu = null;
					//have gpu then take it else leave it
					if(gpuAmount > 0){
						takeGpu = capi.take(crefGpu, any(1), MzsConstants.RequestTimeout.TRY_ONCE, tx);
						gpuAmount--;
					}
					
					if(takeGpu == null){
						//ArrayList must have one element set it to null, for pc has no gpu
						takeGpu = new ArrayList<Component>();
						takeGpu.add(null);
					}
					pcs.add(new Computer(uuids.generate(), workerId, (Cpu) takeCpu.get(0), (Gpu) takeGpu.get(0), (Mainboard) takeMainboard.get(0), takeRams));
				}
			}
			
			//write the computers to space
			for(Computer pc: pcs){
				//mark them with untested
				Entry entry = new Entry(pc, LabelCoordinator.newCoordinationData("untested"));
				
				capi.write(crefPc, MzsConstants.RequestTimeout.DEFAULT, tx, entry);
				log.info("Worker: %s, build pc: %s", workerId, pc.id.toString());
			}
			
			//commit the transaction
			capi.commitTransaction(tx);
		}catch (SpaceException e) {
			System.out.println("ERROR with message: "+ e.getMessage());
			e.printStackTrace();
		} catch (MzsCoreException e) {
			log.info("Worker: %s, something went wrong at building the pc at startup!", workerId);
			e.printStackTrace();
		}
	}
		
	@Override
	protected void registerNotifications() {
		registerNotification( crefCpu,        Operation.WRITE );
		registerNotification( crefGpu,        Operation.WRITE );
		registerNotification( crefMainboards, Operation.WRITE );
		registerNotification( crefRam,        Operation.WRITE );
	}

	@Override
	protected boolean shouldProcess( Component e, Operation o, List<CoordinationData> cds ) { 
		return true;
	}
	
	@Override
	protected void process( Component e, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException {
		buildPc( tx );
	}
	
	private void buildPc( TransactionReference tx ) throws MzsCoreException {
			// mandatory take parts
			Cpu cpu       = (Cpu) capi.take( crefCpu, any(1), RequestTimeout.ZERO, tx ).get( 0 );
			Mainboard mbd = (Mainboard) capi.take( crefMainboards, fifo(1), RequestTimeout.ZERO, tx ).get( 0 );

			// optional parts
			Gpu gpu;
			try {
				gpu = (Gpu) capi.take( crefGpu, any(1), RequestTimeout.ZERO, tx ).get( 0 );			
			} catch ( MzsCoreException e ) {
				gpu = null;
			}
		
			// ram is a bitch
			int numRams = capi.test( crefRam, ANY_MAX, RequestTimeout.ZERO, tx );
		
			List<RamModule> ram;
			switch ( numRams ) {
				case 0:
					ram = Collections.emptyList();
					capi.rollbackTransaction( tx );
					break;
				case 1:
				case 2:
					ram = capi.take( crefRam, any( numRams ), RequestTimeout.ZERO, tx );
					break;
				// if 3, take 2
				case 3:
					ram = capi.take( crefRam, any( 2 ), RequestTimeout.ZERO, tx );
					break;
				// if 4 or more, take 4
				default:
					ram = capi.take( crefRam, any(4), RequestTimeout.ZERO, tx );				
					break;
			}
		
			//assemble pc
			Computer pc = new Computer( uuids.generate(), workerId, cpu, gpu, mbd, ram );
			capi.write( crefPc, RequestTimeout.DEFAULT, tx, new Entry(
					pc,
					LABEL_UNTESTED_FOR_COMPLETENESS,
					LABEL_UNTESTED_FOR_DEFECT, 
					LabelCoordinator.newCoordinationData( "Computer.id:" + pc.id )
			));
	}
}
