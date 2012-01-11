package tu.space.light;

import static tu.space.util.ContainerCreator.any;
import static tu.space.util.ContainerCreator.fifo;

import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
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
	
	private Order order = null;

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

		mySm.buildContractOnStartUp();
		mySm.run();
	}

	private void buildContractOnStartUp(){
		TransactionReference tx;
		try {
			tx = capi.createTransaction(5000, space);
			order = (Order) capi.read(crefOrder, fifo(1), ContainerCreator.DEFAULT_TX_TIMEOUT, tx).get(0);
			
			//build as many pc's as possible form space
			for(int i = 0; i < order.quantitiy; i++){
				buildPc( order );
			}
			//commit after loop
			capi.commitTransaction( tx );
		} catch (MzsCoreException e) {
			//no order found nothing to produce here
		}
	}

	@Override
	protected void registerNotifications() {
		registerNotification(crefCpu, 		 Operation.WRITE);
		registerNotification(crefGpu,		 Operation.WRITE);
		registerNotification(crefMainboards, Operation.WRITE);
		registerNotification(crefRam, 		 Operation.WRITE);
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
			order = (Order) capi.read(crefOrder, fifo(1), ContainerCreator.DEFAULT_TX_TIMEOUT, tx).get(0);
			
			buildPc(order);			
			// if we got here we can commit, so return true
			return true;
		} catch ( CountNotMetException ex ) {
			rollback( tx );			
			// if we got here we have rolled back, so return false
			return false;
		}
	}

	private synchronized void buildPc(Order order) throws MzsCoreException {
		TransactionReference tx = null;
		try{
			tx = capi.createTransaction(5000, space);
			// mandatory take parts
			Mainboard mbd = (Mainboard) capi.take(crefMainboards, fifo(1), RequestTimeout.TRY_ONCE, tx).get( 0 );
			
			//Build spec. cpu-type
			Cpu cpu;
			
			if(order.cpuType == Type.SINGLE_CORE){
				cpu = (Cpu) capi.take(crefCpu, LabelCoordinator.newSelector(ContainerCreator.SINGLE_CORE, 1), 
						MzsConstants.RequestTimeout.ZERO, tx).get(0);
			} else if(order.cpuType == Type.DUAL_CORE){
				cpu = (Cpu) capi.take(crefCpu, LabelCoordinator.newSelector(ContainerCreator.DUAL_CORE, 1), 
						MzsConstants.RequestTimeout.ZERO, tx).get(0);
			} else if(order.cpuType == Type.QUAD_CORE){
				cpu = (Cpu) capi.take(crefCpu, LabelCoordinator.newSelector(ContainerCreator.QUAD_CORE, 1), 
						MzsConstants.RequestTimeout.ZERO, tx).get(0);
			} else {
				//this should never be the case
				cpu = null;
			}
			
			//precondition ram has to be 1, 2 or 4, spec. in order ram quantity field
			List<RamModule> rams = capi.take(crefRam, any(order.ramQuantity),  RequestTimeout.TRY_ONCE, tx );
	
			// optional parts
			Gpu gpu = null;
			if(order.gpu){
				gpu = (Gpu) capi.take(crefGpu, any(1), RequestTimeout.TRY_ONCE, tx).get(0);
			}
			
			// assemble pc
			Computer pc = new Computer(uuids.generate(), workerId, cpu, gpu, mbd, rams);
			
			writePc(crefPc, pc);
			
			capi.commitTransaction( tx );
		} catch (MzsCoreException ex){
			//some parts not in space we can not produce anything
			log.error("Parts not available pc not build");
			rollback( tx );
		}
	}
}