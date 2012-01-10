package tu.space.light;

import static tu.space.util.ContainerCreator.DEFAULT_TX_TIMEOUT;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;

/**
 * Ships a pc after she was notified
 * 
 * @author raunig stefan
 */
public class Logistican extends Processor<Computer> {
	public Logistican( String... args ) throws MzsCoreException {
		super( args );
		
		pcs     = ContainerCreator.getPcContainer( space, capi );
		storage = ContainerCreator.getStorageContainer( space, capi );
		trash   = ContainerCreator.getPcDefectContainer( space, capi );
	}
		
	public Logistican( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
		
		pcs     = ContainerCreator.getPcContainer( this.space, capi );
		storage = ContainerCreator.getStorageContainer( this.space, capi );
		trash   = ContainerCreator.getPcDefectContainer( this.space, capi );
	}
	
	public static void main(String[] args) throws MzsCoreException {
		Logger.configure();	

		new Logistican( args ).run();
	}
	
	@Override
	protected void registerNotifications() {
		registerNotification( pcs, Operation.WRITE );
	}

	@Override
	protected boolean shouldProcess( Computer e, Operation o, List<CoordinationData> cds ) {
		return e.defect != TestStatus.UNTESTED && e.complete != TestStatus.UNTESTED;
	}

	@Override
	protected void process( Computer pc, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException {
		// remove this PC from the space
		capi.delete( pcs, LabelCoordinator.newSelector( "Computer.id:" + pc.id ), DEFAULT_TX_TIMEOUT, tx );
		
		pc = pc.tagAsFinished( workerId );
		
		if ( pc.hasDefect() || pc.isComplete() ){
			capi.write( trash, RequestTimeout.DEFAULT, tx, new Entry( pc ) );
			log.info("Logistican: %s, tested completeness of Pc: %s, result uncomplete move to trash", workerId, pc.id );
		} else {
			capi.write( storage, RequestTimeout.DEFAULT, tx, new Entry( pc ) );
			log.info("Logistican: %s, delivered Pc: %s", workerId, pc.id );					
		}
	}
	
	private final ContainerReference pcs;
	private final ContainerReference trash;
	private final ContainerReference storage;
	
}
