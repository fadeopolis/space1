package tu.space.worker;


import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;

/**
 * 
 * Tester will be notified if a computer is ready for testing, does it and 
 * marks it okay or failure. Afterwards set a notification pc was tested. 
 * 
 * @author raunig stefan
 */
public class SpaceCompletenessTester implements NotificationListener {

	protected final Logger log = Logger.make(getClass());
	
	protected final String workerId;
	
	protected final Capi capi;
	protected final URI  space;
	
	private final NotificationManager notification;
	
	protected ContainerReference crefPc;
	protected ContainerReference crefPcDefect;
	
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
			System.err.println("Usage: ./SpaceTester id");
			System.exit(1);
		}
		
		Logger.configure();	

		new SpaceCompletenessTester(args[0]);
		
		while(true){}
	}
	
	public SpaceCompletenessTester(final String workerId){
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
	
	/**
	 * To be overriden by other testers
	 * 
	 * @throws MzsCoreException
	 */
	protected void check(final Computer computer){
		try {
			//transaction
			TransactionReference tx = capi.createTransaction(5000, space);
			
			//take pcs from space marked untested
			if(computer == null){
				pcs = capi.take(crefPc, Arrays.asList(LabelCoordinator.newSelector("untested", MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO, tx);
			} else {
				pcs = Arrays.asList(computer);
			}
			for(Computer pc: pcs){
				if(pc.isComplete()){
					//check if pc is complete
					pc = pc.tagAsTestedForCompleteness(workerId, TestStatus.YES);
				} else {
					pc = pc.tagAsTestedForCompleteness(workerId, TestStatus.NO);
				}
				
				Entry entry;
				if(pc.defect == TestStatus.UNTESTED) {
					entry = new Entry(pc, LabelCoordinator.newCoordinationData("untested"));
				} else {
					entry = new Entry(pc, LabelCoordinator.newCoordinationData("tested"));
				}
				capi.write(crefPc, RequestTimeout.DEFAULT, tx, entry);
				log.info("Tester: %s, tested completeness of Pc: %s, result okay", workerId, pc.id.toString());
				
			}
			
			capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			log.info("Tester: %s could not test pc.", workerId);
			e.printStackTrace();
		}
	}
	
	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1, List<? extends Serializable> entries) {		
		Entry entry = (Entry) entries.get(0);
		if(((LabelData) entry.getCoordinationData().get(0)).getLabel().equals("untested")){
			Computer pc = (Computer) entry.getValue();
			if(pc.complete == TestStatus.UNTESTED) {
				try {
					ArrayList<Serializable> pcs = capi.take(crefPc, Arrays.asList(LabelCoordinator.newSelector("untested", 1)), MzsConstants.RequestTimeout.ZERO, null);
					check((Computer) pcs.get(0));
				} catch (MzsCoreException e) {
					//do nothing
				}
			}
		}
	}
}
