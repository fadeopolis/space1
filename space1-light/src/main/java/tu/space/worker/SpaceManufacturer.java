package tu.space.worker;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;
import tu.space.utils.UUIDGenerator;

/**
 * The manufacturer will be notified if a component was built, constructs a computer
 * and set a notification if pc is built.
 * @author system
 *
 */
public class SpaceManufacturer implements NotificationListener {

	private final Logger log = Logger.make( getClass() );
	private UUIDGenerator uuids = new UUIDGenerator();
	
	private final String workerId;
	
	//components and pc's
	private final List<Computer> pcs = new ArrayList<Computer>();

	//container refs
	private ContainerReference crefMainboards;
	private ContainerReference crefCpu;
	private ContainerReference crefGpu;
	private ContainerReference crefRam;
	private ContainerReference crefPc;
	
	private final NotificationManager notification;
	private final Capi capi;
	private final URI space;
	
	/**
	 * @param String args[0] workerId()
	 */
	public static void main(String[] args) {
		//check args
		if(args.length != 1){
			System.err.println("Usage: ./Manufacturer id");
			System.exit(1);
		}
		
		Logger.configure();
		
		SpaceManufacturer mySm = new SpaceManufacturer(args[0]);
		mySm.buildPcOnStartUp();
		//run and wait for notifications
		while(true){}
	}
	
	public SpaceManufacturer(final String workerId){
		this.workerId = workerId;
		
		//default local space
		MzsCore core = DefaultMzsCore.newInstance(0);
		capi = new Capi(core);
		notification = new NotificationManager(core);
			    
		//standalone server URI
		space = URI.create("xvsm://localhost:9877");
		
		try {
			//lookup containers
			crefMainboards = ContainerCreator.getMainboardContainer(space, capi);
			crefCpu = ContainerCreator.getCpuContainer(space, capi);
			crefGpu = ContainerCreator.getGpuContainer(space, capi);
			crefRam = ContainerCreator.getRamContainer(space, capi);
			crefPc  = ContainerCreator.getPcContainer(space, capi);
			
			//create Notifications
			notification.createNotification(crefMainboards, this, Operation.WRITE);
			notification.createNotification(crefCpu, this, Operation.WRITE);
			notification.createNotification(crefGpu, this, Operation.WRITE);
			notification.createNotification(crefRam, this, Operation.WRITE);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Collects all components and builds pc's if possible
	 */
	public void buildPcOnStartUp(){
		try{
						
			//crate transaction
			TransactionReference tx = capi.createTransaction(5000, space);
		
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
					ArrayList<Component> readcpus = capi.read(crefMainboards, Arrays.asList(FifoCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO , tx);
					ArrayList<Component> readram = capi.read(crefRam, Arrays.asList(AnyCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO, tx);
					ArrayList<Component> readgpu = capi.read(crefGpu, Arrays.asList(AnyCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO, tx);
					cpuAmount = readcpus.size();
					ramAmount = readram.size();
					gpuAmount = readgpu.size();
					once = false;
				}
				
				// as long as mainboard cpu and ram is available build a pc
				if(cpuAmount > 0 && ramAmount > 0){				
					//take mainboard and cpu
					ArrayList<Component> takeMainboard = capi.take(crefMainboards, Arrays.asList(FifoCoordinator.newSelector(1)), MzsConstants.RequestTimeout.TRY_ONCE, tx);
					ArrayList<Component> takeCpu = capi.take(crefCpu, Arrays.asList(AnyCoordinator.newSelector(1)), MzsConstants.RequestTimeout.TRY_ONCE, tx);
					cpuAmount--;
					
					//how many ram 
					List<RamModule> takeRams = null;
					switch(ramAmount){
						case 0:
							break;
						case 1:
							takeRams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(1)), MzsConstants.RequestTimeout.TRY_ONCE, tx);
							ramAmount--;
							break;
						case 2:
							takeRams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(2)), MzsConstants.RequestTimeout.TRY_ONCE, tx);
							ramAmount -= 2;
							break;
						case 3:
							//3 is one to much take 2
							takeRams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(2)), MzsConstants.RequestTimeout.TRY_ONCE, tx);
							ramAmount -= 2;
							break;
						default :
							//well we have 4 or more then let us take 4
							takeRams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(4)), MzsConstants.RequestTimeout.TRY_ONCE, tx);
							ramAmount -= 4;
							break;
					}
					log.info("Size of rams taken: %d, and size of remaining: %d\n", takeRams.size(), ramAmount);
					ArrayList<Component> takeGpu = null;
					//have gpu then take it else leave it
					if(gpuAmount > 0){
						takeGpu = capi.take(crefGpu, Arrays.asList(AnyCoordinator.newSelector(1)), MzsConstants.RequestTimeout.TRY_ONCE, tx);
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
		
	/**
	 * callback for notifications of type Component
	 */
	@Override
	public void entryOperationFinished(Notification notificaton, Operation operation, List<? extends Serializable> components) {

		try {
        	//component notification
			Component component = (Component) ((Entry) components.get(0)).getValue();
			TransactionReference tx = capi.createTransaction(5000, space);
						
			ArrayList<Cpu> cpus				= null;
			ArrayList<Mainboard> mainboards = null;
			ArrayList<RamModule> rams		= null;
			ArrayList<Gpu> gpus				= null;
			
			boolean create = true;

			//find out what type of components we have
			if(component instanceof Mainboard){
				try {
	                cpus       = capi.take(crefCpu, Arrays.asList(AnyCoordinator.newSelector(1)),
	                				MzsConstants.RequestTimeout.ZERO, tx);
	                
	                mainboards = capi.take(crefMainboards, Arrays.asList(FifoCoordinator.newSelector(1)), 
	                				MzsConstants.RequestTimeout.ZERO, tx);
	                try {
	                	gpus   = capi.take(crefGpu, Arrays.asList(AnyCoordinator.newSelector(1)),
       		         			 	 MzsConstants.RequestTimeout.ZERO, tx);
	                } catch (MzsCoreException e) {
		                /*no gpu available np gpu is not core component*/
	                	gpus = new ArrayList<Gpu>();
	                	gpus.add(null);
		            }
	                rams = capi.read(crefRam, Arrays.asList(AnyCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)),
           		         			 MzsConstants.RequestTimeout.ZERO, tx);
				} catch (MzsCoreException e) {
	                /*one core component missing do nothing*/
	            	create = false;
	            }
			} else if(component instanceof Cpu){
				try {
	                cpus       = capi.take(crefCpu, Arrays.asList(AnyCoordinator.newSelector(1)),
  		         					MzsConstants.RequestTimeout.ZERO, tx);

	                mainboards = capi.take(crefMainboards, Arrays.asList(FifoCoordinator.newSelector(1)),
           		         			MzsConstants.RequestTimeout.ZERO, tx);
	                try {
	                	gpus   = capi.take(crefGpu, Arrays.asList(AnyCoordinator.newSelector(1)),
       		         			 	MzsConstants.RequestTimeout.ZERO, tx);
	                } catch (MzsCoreException e) {
		                /*no gpu available np gpu is not core component*/
	                	gpus = new ArrayList<Gpu>();
	                	gpus.add(null);
		            }
	                rams       = capi.read(crefRam, Arrays.asList(AnyCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)),
           		         			 MzsConstants.RequestTimeout.ZERO, tx);
	            } catch (MzsCoreException e) {
	                /*one core component missing do nothing*/
	            	create = false;
	            }
			} else if(component instanceof RamModule){
				try {
	                cpus       = capi.take(crefCpu, Arrays.asList(AnyCoordinator.newSelector(1)),
	                				 MzsConstants.RequestTimeout.ZERO, tx);
	                mainboards = capi.take(crefMainboards, Arrays.asList(FifoCoordinator.newSelector(1)),
           		         			 MzsConstants.RequestTimeout.ZERO, tx);
	                try {
	                	gpus   = capi.take(crefGpu, Arrays.asList(AnyCoordinator.newSelector(1)),
       		         			 	 MzsConstants.RequestTimeout.ZERO, tx);
	                } catch (MzsCoreException e) {
		                /*no gpu available np gpu is not core component*/
	                	gpus = new ArrayList<Gpu>();
	                	gpus.add(null);
		            }
	                rams = capi.read(crefRam, Arrays.asList(AnyCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)),
           		         			 MzsConstants.RequestTimeout.ZERO, tx);
	            } catch (MzsCoreException e) {
	                /*one core component missing do nothing*/
	            	create = false;
	            	capi.rollbackTransaction(tx);
	            }
			} else if (component instanceof Gpu){
				//do nothing, gpu is not a core component
				return;
			} else {
				/*
				 * this is when no type of component is found.
				 * this should never be the case
				 */
				create = false;
				throw new SpaceException("Item is not of type Component");
			}
			
			if(create){
				//build the pc
				Computer pc = null;		
				switch(rams.size()){
					case 0:
						capi.rollbackTransaction( tx );
						return;
					case 1:
						rams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(1)), MzsConstants.RequestTimeout.ZERO, tx);
						break;
					case 2:
						rams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(2)), MzsConstants.RequestTimeout.ZERO, tx);
						break;
					case 3:
						rams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(2)), MzsConstants.RequestTimeout.ZERO, tx);
						break;
					default:
						rams = capi.take(crefRam, Arrays.asList(AnyCoordinator.newSelector(4)), MzsConstants.RequestTimeout.ZERO, tx);
						break;
				}

				//assemble pc
				pc = new Computer(uuids.generate(), workerId, cpus.get(0), gpus.get(0), mainboards.get(0), rams);

				//label as untested
				Entry entry = new Entry(pc, LabelCoordinator.newCoordinationData("untested"));
				//write to space
				capi.write(crefPc, MzsConstants.RequestTimeout.DEFAULT, tx, entry);
				
				//commit Transaction and build pc if possible
				capi.commitTransaction(tx);
				
				log.info("Worker: %s, Pc build with id: %s", workerId, pc.id.toString());
			}
        } catch (MzsCoreException e) {
        	log.info("Worker: %s, could not build Pc", workerId);
            e.printStackTrace();
        } catch (SpaceException e) {
        	log.info("SpaceException! %s", e.getMessage());
		}
	}
}
