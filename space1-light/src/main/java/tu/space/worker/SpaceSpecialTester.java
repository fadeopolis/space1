package tu.space.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;

@Deprecated
public class SpaceSpecialTester extends SpaceTester {

	private List<Computer> pcs;
	
	/**
	 * Constructor
	 * 
	 * @param workerId
	 */
	public SpaceSpecialTester(final String workerId) {
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
			//transaction
			TransactionReference tx = capi.createTransaction(5000, space);
			
			//take pcs from space marked testedCompleteness
			if(computer == null){
				pcs = capi.take(crefPc, Arrays.asList(LabelCoordinator.newSelector("untested", MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.ZERO, tx);
			} else {
				pcs = Arrays.asList(computer);
			}
			
			for(Computer pc: pcs){
				if(pc.hasDefect()){
					//check if pc is complete
					pc = pc.tagAsTestedForDefect(workerId, TestStatus.YES);
				} else {
					pc = pc.tagAsTestedForDefect(workerId, TestStatus.NO);
				}
				
				Entry entry;
				if(pc.complete == TestStatus.UNTESTED) {
					entry = new Entry(pc, LabelCoordinator.newCoordinationData("untested"));
				} else {
					entry = new Entry(pc, LabelCoordinator.newCoordinationData("tested"));
				}		
				capi.write(crefPc, RequestTimeout.DEFAULT, tx, entry);
				log.info("Tester: %s, tested error of Pc: %s", workerId, pc.id.toString());
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
			if(pc.defect == TestStatus.UNTESTED) {
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
