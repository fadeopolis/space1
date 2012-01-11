package tu.space.light;

import static tu.space.util.ContainerCreator.ANY_MAX;
import static tu.space.util.ContainerCreator.FIFO_MAX;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_COMPLETENESS;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_DEFECT;
import static tu.space.util.ContainerCreator.any;
import static tu.space.util.ContainerCreator.fifo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
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
import tu.space.components.Cpu.Type;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.util.ContainerCreator;
import tu.space.util.LogBack;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;
import tu.space.utils.UUIDGenerator;

/**
 * The manufacturer will be notified if a component was built, constructs a
 * computer and set a notification if pc is built.
 * 
 * @author system
 * 
 */
public class Manufacturer extends Processor<Component> {

	public Manufacturer(String id, Capi capi, int space)
			throws MzsCoreException {
		super(id, capi, space);

		crefCpu = ContainerCreator.getCpuContainer(this.space, capi);
		crefGpu = ContainerCreator.getGpuContainer(this.space, capi);
		crefMainboards = ContainerCreator.getMainboardContainer(this.space, capi);
		crefRam = ContainerCreator.getRamContainer(this.space, capi);
		crefPc = ContainerCreator.getPcContainer(this.space, capi);
		
		crefOrder = ContainerCreator.getOrderContainer(this.space, capi);
	}

	public Manufacturer(String[] args) throws MzsCoreException {
		super(args);

		crefCpu = ContainerCreator.getCpuContainer(this.space, capi);
		crefGpu = ContainerCreator.getGpuContainer(this.space, capi);
		crefMainboards = ContainerCreator.getMainboardContainer(this.space, capi);
		crefRam = ContainerCreator.getRamContainer(this.space, capi);
		crefPc = ContainerCreator.getPcContainer(this.space, capi);
		
		crefOrder = ContainerCreator.getOrderContainer(this.space, capi);
	}

	// container refs
	private final ContainerReference crefMainboards;
	private final ContainerReference crefCpu;
	private final ContainerReference crefGpu;
	private final ContainerReference crefRam;
	private final ContainerReference crefPc;
	private final ContainerReference crefOrder;

	private final UUIDGenerator uuids = new UUIDGenerator();

	/**
	 * @param String
	 *            args[0] workerId()
	 * @throws MzsCoreException
	 */
	public static void main(String[] args) throws MzsCoreException {
		Logger.configure();
		LogBack.configure();

		Manufacturer mySm = new Manufacturer(args);

		//mySm.buildPcOnStartUp();
		mySm.guildContractOnStartUp();
		
		mySm.run();
	}

	private void guildContractOnStartUp(){
		TransactionReference tx;
		Order order;
		try {
			tx = capi.createTransaction(5000, space);
			order = (Order) capi.read(crefOrder, fifo(1), ContainerCreator.DEFAULT_TX_TIMEOUT, tx).get(0);
			
			//build as many pc's as possible form space
			for(int i = 0; i < order.quantitiy; i++){
				try{
					Mainboard mainboard = (Mainboard) capi.take(crefMainboards, fifo(1), 
							MzsConstants.RequestTimeout.ZERO, tx).get(0);
					
					Cpu cpu = null;
					
					//select cpu-type spec. from order
					if(order.cpuType == Type.SINGLE_CORE){
						cpu = (Cpu) capi.take(crefCpu, LabelCoordinator.newSelector(ContainerCreator.SINGLE_CORE, 1), 
								MzsConstants.RequestTimeout.ZERO, tx).get(0);
					} else if(order.cpuType == Type.DUAL_CORE){
						cpu = (Cpu) capi.take(crefCpu, LabelCoordinator.newSelector(ContainerCreator.DUAL_CORE, 1), 
								MzsConstants.RequestTimeout.ZERO, tx).get(0);
					} else if(order.cpuType == Type.QUAD_CORE){
						cpu = (Cpu) capi.take(crefCpu, LabelCoordinator.newSelector(ContainerCreator.QUAD_CORE, 1), 
								MzsConstants.RequestTimeout.ZERO, tx).get(0);
					}
					
					Gpu gpu = null;
					
					//gpu
					if(order.gpu){
						gpu = (Gpu) capi.take(crefGpu, any(1), 
								MzsConstants.RequestTimeout.ZERO, tx).get(0);
					}
					
					//TODO ram
					log.info("%s %s %s", cpu, gpu, mainboard);
				} catch (MzsCoreException e ){
					//core component missing stop building pc's
					
				}
			}
		} catch (MzsCoreException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Collects all components and builds pc's if possible
	 */
//	public void buildPcOnStartUp() {
//		List<Computer> pcs = new ArrayList<Computer>();
//		try {
//			// crate transaction
//			TransactionReference tx = capi.createTransaction(500000, space);
//
//			/*
//			 * read all entries of mainboard to decide how many pc's we can
//			 * build from space.
//			 */
//			ArrayList<Component> readMainboards = capi.read(crefMainboards,
//					Arrays.asList(FifoCoordinator
//							.newSelector(MzsConstants.Selecting.COUNT_MAX)),
//					MzsConstants.RequestTimeout.INFINITE, tx);
//
//			/*
//			 * a trick to optimize the loop iterations, because we can only
//			 * build as much pc's as we have core components like cpu and
//			 * mainboards sizeof(mainboards)-sizeof(cpus) we do not consider ram
//			 * for now
//			 */
//			int cpuAmount = 0;
//			int ramAmount = 0;
//			int gpuAmount = 0;
//			boolean once = true;
//
//			for (int i = 0; i <= readMainboards.size() - cpuAmount; i++) {
//				if (once) {
//					ArrayList<Component> readcpus = capi.read(crefMainboards,
//							FIFO_MAX, RequestTimeout.ZERO, tx);
//					ArrayList<Component> readram = capi.read(crefRam, ANY_MAX,
//							RequestTimeout.ZERO, tx);
//					ArrayList<Component> readgpu = capi.read(crefGpu, ANY_MAX,
//							RequestTimeout.ZERO, tx);
//					cpuAmount = readcpus.size();
//					ramAmount = readram.size();
//					gpuAmount = readgpu.size();
//					once = false;
//				}
//
//				// as long as mainboard cpu and ram is available build a pc
//				if (cpuAmount > 0 && ramAmount > 0) {
//					// take mainboard and cpu
//					ArrayList<Component> takeMainboard = capi.take(
//							crefMainboards, fifo(1),
//							MzsConstants.RequestTimeout.TRY_ONCE, tx);
//					ArrayList<Component> takeCpu = capi.take(crefCpu, any(1),
//							MzsConstants.RequestTimeout.TRY_ONCE, tx);
//					cpuAmount--;
//
//					// how many ram
//					List<RamModule> takeRams = null;
//					switch (ramAmount) {
//					case 0:
//						break;
//					case 1:
//						takeRams = capi.take(crefRam, any(1),
//								RequestTimeout.TRY_ONCE, tx);
//						ramAmount--;
//						break;
//					case 2:
//						takeRams = capi.take(crefRam, any(2),
//								RequestTimeout.TRY_ONCE, tx);
//						ramAmount -= 2;
//						break;
//					case 3:
//						// 3 is one to much take 2
//						takeRams = capi.take(crefRam, any(2),
//								RequestTimeout.TRY_ONCE, tx);
//						ramAmount -= 2;
//						break;
//					default:
//						// well we have 4 or more then let us take 4
//						takeRams = capi.take(crefRam, any(4),
//								RequestTimeout.TRY_ONCE, tx);
//						ramAmount -= 4;
//						break;
//					}
//					
//					ArrayList<Component> takeGpu = null;
//					// have gpu then take it else leave it
//					if (gpuAmount > 0) {
//						takeGpu = capi.take(crefGpu, any(1),
//								MzsConstants.RequestTimeout.TRY_ONCE, tx);
//						gpuAmount--;
//					}
//
//					if (takeGpu == null) {
//						// ArrayList must have one element set it to null, for
//						// pc has no gpu
//						takeGpu = new ArrayList<Component>();
//						takeGpu.add(null);
//					}
//					pcs.add(new Computer(uuids.generate(), workerId,
//							(Cpu) takeCpu.get(0), (Gpu) takeGpu.get(0),
//							(Mainboard) takeMainboard.get(0), takeRams));
//				}
//			}
//
//			// write the computers to space
//			for (Computer pc : pcs) {
//				// mark them with untested
//				Entry entry = new Entry(pc,
//						LabelCoordinator.newCoordinationData("untested"));
//
//				capi.write(crefPc, MzsConstants.RequestTimeout.DEFAULT, tx,
//						entry);
//				log.info("Worker: %s, build pc: %s", workerId, pc.id.toString());
//			}
//
//			// commit the transaction
//			capi.commitTransaction(tx);
//		} catch (SpaceException e) {
//			System.out.println("ERROR with message: " + e.getMessage());
//			e.printStackTrace();
//		} catch (MzsCoreException e) {
//			log.info(
//					"Worker: %s, something went wrong at building the pc at startup!",
//					workerId);
//			e.printStackTrace();
//		}
//	}

	@Override
	protected void registerNotifications() {
		registerNotification(crefCpu, Operation.WRITE);
		registerNotification(crefGpu, Operation.WRITE);
		registerNotification(crefMainboards, Operation.WRITE);
		registerNotification(crefRam, Operation.WRITE);
	}

	@Override
	protected boolean shouldProcess(Component e, Operation o,
			List<CoordinationData> cds) {
		return true;
	}

	@Override
	protected boolean process(Component e, Operation o,
			List<CoordinationData> cds, TransactionReference tx)
			throws MzsCoreException {
		try {
			buildPc(tx);			
			// if we got here we can commit, so return true
			return true;
		} catch ( CountNotMetException ex ) {
			rollback( tx );			
			// if we got here we have rolled back, so return false
			return false;
		}
	}

	private synchronized void buildPc(TransactionReference tx) throws MzsCoreException {
		// mandatory take parts
		Cpu       cpu = (Cpu)       capi.take(crefCpu,        any(1),  RequestTimeout.TRY_ONCE, tx).get(0);
		Mainboard mbd = (Mainboard) capi.take(crefMainboards, fifo(1), RequestTimeout.TRY_ONCE, tx).get(0);
		RamModule ram = (RamModule) capi.take(crefRam,        any(1),  RequestTimeout.TRY_ONCE, tx ).get( 0 );

		// optional parts
		Gpu gpu;
		try {
			gpu = (Gpu) capi.take(crefGpu, any(1), RequestTimeout.TRY_ONCE, tx).get(0);
		} catch (CountNotMetException e) {
			gpu = null;
		}

		// ram is a bitch
		List<RamModule> rams;
		try {
			rams = capi.take(crefRam, any(3), RequestTimeout.TRY_ONCE, tx);
		} catch (CountNotMetException e1) {
			try {
				rams = capi.take(crefRam, any(1), RequestTimeout.TRY_ONCE, tx);
			} catch (CountNotMetException e2) {
				rams = new ArrayList<RamModule>();
			}		
		}		
		rams.add( ram );

		// assemble pc
		Computer pc = new Computer(uuids.generate(), workerId, cpu, gpu, mbd, rams);
		capi.write(
			crefPc,
			RequestTimeout.DEFAULT, 
			tx, 
			new Entry(
				pc,
				LABEL_UNTESTED_FOR_COMPLETENESS, 
				LABEL_UNTESTED_FOR_DEFECT
			)
		);
	}
}