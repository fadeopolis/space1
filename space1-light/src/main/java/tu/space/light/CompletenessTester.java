package tu.space.light;

import static tu.space.util.ContainerCreator.DEFAULT_TX_TIMEOUT;
import static tu.space.util.ContainerCreator.LABEL_INCOMPLETE;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_COMPLETENESS;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;

public class CompletenessTester extends Processor<Computer> {

	public static void main(String... args) throws MzsCoreException {
		Logger.configure();	

		new CompletenessTester( args ).run();
	}

	public CompletenessTester( String... args ) throws MzsCoreException {
		super( args );
		
		pcs = ContainerCreator.getPcContainer( this.space, capi );
	}
	public CompletenessTester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
		
		pcs = ContainerCreator.getPcContainer( this.space, capi );
	}

	@Override
	protected void registerNotifications() {
		registerNotification( pcs, Operation.WRITE );
	}

	@Override
	protected boolean shouldProcess( Computer pc, Operation o, List<CoordinationData> cds ) {
		return pc.complete == TestStatus.UNTESTED;
	}
	
	@Override
	protected void process( Computer pc, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException {
		// remove this PC from the space
		capi.delete( pcs, LabelCoordinator.newSelector( "Computer.id:" + pc.id ), DEFAULT_TX_TIMEOUT, tx );
		
		List<CoordinationData> data = new ArrayList<CoordinationData>( cds );
		
		if ( pc.hasDefect() ){
			pc = pc.tagAsTestedForCompleteness(workerId, TestStatus.YES);
			data.add( LABEL_INCOMPLETE );
		} else {
			pc = pc.tagAsTestedForCompleteness(workerId, TestStatus.YES);
		}
			
		data.remove( LABEL_UNTESTED_FOR_COMPLETENESS );
		
		capi.write( pcs, new Entry( pc, data ) );
		log.info("Tester: %s, tested error of PC: %s", workerId, pc.id );
	}
	
	private final ContainerReference pcs;

	
	public void onStartUp() {
		// TODO Auto-generated method stub
		
	}
}
