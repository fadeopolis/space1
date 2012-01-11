package tu.space.light;

import static tu.space.util.ContainerCreator.DEFAULT_TX_TIMEOUT;
import static tu.space.util.ContainerCreator.SELECTOR_TESTED_FOR_COMPlETENESS;
import static tu.space.util.ContainerCreator.SELECTOR_TESTED_FOR_DEFECT;

import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
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
import tu.space.util.LogBack;
import tu.space.utils.Logger;

public class Logistican extends Processor<Computer> {
	
	public static void main(String... args) throws MzsCoreException {
		Logger.configure();
		LogBack.configure();

		new Logistican(args).run();
	}

	public Logistican(String... args) throws MzsCoreException {
		super(args);

		pcs = ContainerCreator.getPcContainer(this.space, capi);
		trash = ContainerCreator.getPcDefectContainer(this.space, capi);
		storage = ContainerCreator.getStorageContainer(this.space, capi);
	}

	public Logistican(String id, Capi capi, int space) throws MzsCoreException {
		super(id, capi, space);

		pcs = ContainerCreator.getPcContainer(this.space, capi);
		trash = ContainerCreator.getPcDefectContainer(this.space, capi);
		storage = ContainerCreator.getStorageContainer(this.space, capi);
	}

	public void storePc( TransactionReference tx ) throws MzsCoreException {
		Computer pc = (Computer) capi.take( 
				pcs,
				Arrays.asList( SELECTOR_TESTED_FOR_DEFECT, SELECTOR_TESTED_FOR_COMPlETENESS ), 
				RequestTimeout.TRY_ONCE, 
				tx ).get( 0 );

		pc = pc.tagAsFinished( workerId );

		if ( !pc.isComplete() || pc.hasDefect() ) {
			log.info( "%s: Got an bad PC %s", this, pc.id );
			
			capi.write( trash, new Entry( pc ) );
		} else {
			log.info( "%s: Got a mighty fine PC %s", this, pc.id );
			
			capi.write( storage, new Entry( pc ) );
		}
	}

	@Override
	protected void registerNotifications() throws MzsCoreException {
		TransactionReference tx = null; 
		try {
			while ( true ) {
				tx = capi.createTransaction( DEFAULT_TX_TIMEOUT, space );
				
				storePc( tx );
				
				capi.commitTransaction( tx );
			}
		} catch ( CountNotMetException e ) {
			rollback( tx );
		}
		
		registerNotification( pcs, Operation.WRITE );
	}
	@Override
	protected boolean process( Computer pc, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException {
		try {
			storePc( tx );
			return true;			
		} catch ( CountNotMetException e ) {
			rollback( tx );
			return false;
		}
	}
	
	@Override
	protected boolean shouldProcess( Computer e, Operation o, List<CoordinationData> cds ) {
		return e.complete != TestStatus.UNTESTED && e.defect != TestStatus.UNTESTED;
	}

	private final ContainerReference pcs;
	private final ContainerReference trash;
	private final ContainerReference storage;
}
