package tu.space.worker;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;

/**
 * Ships a pc after she was notified
 * 
 * @author raunig stefan
 */
public class SpaceLogistican implements NotificationListener {

	private final Logger log = Logger.make(getClass());
	
	private final String workerId;
	
	private final Capi capi;
	private final URI space;
	
	private final NotificationManager notification;
	
	private ContainerReference crefPc;
	private ContainerReference crefPcDefect;
	private ContainerReference crefStorage;
	
	private List<Computer> pcs;
	
	/**
	 * Main 
	 * 
	 * @param args String args[0] workerId
	 * @throws SpaceException
	 */
	public static void main(String[] args) {
		//args
		if(args.length != 1){
			System.err.println("Usage: ./SpaceLogistican id");
			System.exit(1);
		}
		
		Logger.configure();	

		new SpaceLogistican(args[0]);
		
		while(true){}
	}
	
	public SpaceLogistican(final String workerId){
		this.workerId = workerId;
		
		//default local space
		MzsCore core = DefaultMzsCore.newInstance(0);
		capi = new Capi(core);
		notification = new NotificationManager(core);
		
		//emebbed space uri
		space = URI.create("xvsm://localhost:9877");
		
		try {
			//lookup container
			crefPc = ContainerCreator.getPcContainer(space, capi);
			crefPcDefect = ContainerCreator.getPcDefectContainer(space, capi);
			crefStorage = ContainerCreator.getStorageContainer(space, capi);
			
			//create Notifications
			notification.createNotification(crefPc, this, Operation.WRITE);

			//null to take pc's from space
			check(null);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void check(final Computer computer){
		try {
			//transaction
			TransactionReference tx = capi.createTransaction(5000, space);
			
			//take pcs from space marked untested
			if(computer == null){
				pcs = capi.take(crefPc, Arrays.asList(LabelCoordinator.newSelector("tested", MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO, tx);
			} else {
				pcs = Arrays.asList(computer);
			}
			for(Computer pc: pcs){
				//set pc finish
				if ( pc.hasDefect() || !pc.isComplete() ) {
					//move to trash
					pc = pc.tagAsFinished(workerId, null);
					Entry entry = new Entry(pc);
					capi.write(crefPcDefect, RequestTimeout.DEFAULT, tx, entry);
					log.info("Logistican: %s, tested completeness of Pc: %s, result uncomplete move to trash", workerId, pc.id.toString());
				} else {
					pc = pc.tagAsFinished(workerId, null);
					Entry entry = new Entry(pc);		
					capi.write(crefStorage, RequestTimeout.DEFAULT, tx, entry);
					log.info("Logistican: %s, delivered Pc: %s", workerId, pc.id.toString());					
				}
			}
			
			capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			log.info("Logistican: %s could not deliver pc.", workerId);
			e.printStackTrace();
		}
	}
	
	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1, List<? extends Serializable> entries) {
		try {
			ArrayList<Serializable> pc = capi.take(crefPc, Arrays.asList(LabelCoordinator.newSelector("tested", 1)), MzsConstants.RequestTimeout.ZERO, null);
			check((Computer) pc.get(0));
		} catch (MzsCoreException e) {
			//do nothing
		}
	}
}
