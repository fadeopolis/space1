package tu.space.light;

import static tu.space.util.ContainerCreator.*;

import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.util.ContainerCreator;

public abstract class Tester extends Processor<Computer> {

	public Tester( String... args ) throws MzsCoreException {
		super( args );
		
		pcs  = ContainerCreator.getPcContainer(   this.space, capi );
		cpus = ContainerCreator.getCpuContainer( this.space, capi );
		gpus = ContainerCreator.getGpuContainer( this.space, capi );
		mbds = ContainerCreator.getMainboardContainer( this.space, capi );
		rams = ContainerCreator.getRamContainer( this.space, capi );
	}
	public Tester( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
		
		pcs  = ContainerCreator.getPcContainer(   this.space, capi );
		cpus = ContainerCreator.getCpuContainer( this.space, capi );
		gpus = ContainerCreator.getGpuContainer( this.space, capi );
		mbds = ContainerCreator.getMainboardContainer( this.space, capi );
		rams = ContainerCreator.getRamContainer( this.space, capi );
	}

	protected abstract void testPc( TransactionReference tx ) throws MzsCoreException;

	protected final ContainerReference pcs;
	protected final ContainerReference cpus;
	protected final ContainerReference gpus;
	protected final ContainerReference mbds;
	protected final ContainerReference rams;

	@Override
	protected void registerNotifications() throws MzsCoreException {
		TransactionReference tx = null; 
		try {
			while ( true ) {
				tx = capi.createTransaction( DEFAULT_TX_TIMEOUT, space );
				
				testPc( tx );
				
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
			testPc( tx );
			return true;			
		} catch ( CountNotMetException e ) {
			rollback( tx );
			return false;
		}
	}
}
