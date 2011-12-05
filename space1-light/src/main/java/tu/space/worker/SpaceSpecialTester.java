package tu.space.worker;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;

public class SpaceSpecialTester extends SpaceTester {

	private List<Computer> pcs;
	
	public SpaceSpecialTester(String workerId) {
		super(workerId);
	}

	/**
	 * Main 
	 * 
	 * @param args String args[0] workerId
	 * @throws SpaceException
	 */
	public static void main(String[] args) {
		//args
		if(args.length != 1){
			System.err.println("Usage: ./SpaceSpecialTester id");
			System.exit(1);
		}
		
		Logger.configure();	

		new SpaceSpecialTester(args[0]);
		
		while(true){}
	}
	
	/**
	 * override to perform task2
	 */
	@Override
	protected void check(final Computer computer) {
		try {
			//take pcs from space marked testedCompleteness
			if(computer == null){
				pcs = capi.take(crefPc, Arrays.asList(LabelCoordinator.newSelector("testedCompleteness", MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO, null);
			} else {
				pcs = Arrays.asList(computer);
			}
			
			for(Computer pc: pcs){
				if(!pc.hasDefect()){
					//check if pc is complete
					pc = pc.tagAsTestedForDefect(workerId, TestStatus.YES);
					Entry entry = new Entry(pc, LabelCoordinator.newCoordinationData("testedError"));		
					capi.write(crefPc, RequestTimeout.DEFAULT, null, entry);
					log.info("Tester: %s, tested error of Pc: %s, result okay", workerId, pc.id.toString());
				} else {
					//move to trash
					pc = pc.tagAsTestedForDefect(workerId, TestStatus.YES);
					Entry entry = new Entry(pc);
					capi.write(crefPcDefect, RequestTimeout.DEFAULT, null, entry);
					log.info("Tester: %s, tested error of Pc: %s, result uncomplete move to trash", workerId, pc.id.toString());
				}
			}
		} catch (MzsCoreException e) {
			log.info("Tester: %s could not test pc.", workerId);
			e.printStackTrace();
		}
	}
	
	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1, List<? extends Serializable> entries) {		
		Entry entry = (Entry) entries.get(0);
		if(((LabelData) entry.getCoordinationData().get(0)).getLabel().equals("testedCompleteness")){
			check((Computer) entry.getValue());
		}
	}
}
