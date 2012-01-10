package tu.space.light;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.util.ContainerCreator;
import tu.space.util.LogBack;
import tu.space.utils.UUIDGenerator;

import static tu.space.util.ContainerCreator.*;

public class Manu2 extends Worker {
	public Manu2(String id, Capi capi, int space) throws MzsCoreException {
		super(id, capi, space);

		pcs  = ContainerCreator.getPcContainer(this.space, capi);
		cpus = ContainerCreator.getCpuContainer(this.space, capi);
		gpus = ContainerCreator.getGpuContainer(this.space, capi);
		mbds = ContainerCreator.getMainboardContainer(this.space, capi);
		rams = ContainerCreator.getRamContainer(this.space, capi);
	}

	public Manu2(String[] args) throws MzsCoreException {
		super(args);

		pcs  = ContainerCreator.getPcContainer(this.space, capi);
		cpus = ContainerCreator.getCpuContainer(this.space, capi);
		gpus = ContainerCreator.getGpuContainer(this.space, capi);
		mbds = ContainerCreator.getMainboardContainer(this.space, capi);
		rams = ContainerCreator.getRamContainer(this.space, capi);
	}
	
	public static void main( String[] args ) throws MzsCoreException {
		LogBack.configure();
		
		new Manu2( args ).run();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		TransactionReference tx = null;
		try {
			while ( true ) {
				tx = capi.createTransaction( DEFAULT_TX_TIMEOUT, space );
			
				try {
					// mandatory parts
					Cpu       cpu = (Cpu)       capi.take( cpus, Collections.<Selector>emptyList(), MzsConstants.RequestTimeout.ZERO, tx ).get( 0 );
					Mainboard mbd = (Mainboard) capi.take( mbds, Collections.<Selector>emptyList(), MzsConstants.RequestTimeout.ZERO, tx ).get( 0 );
					RamModule ram = (RamModule) capi.take( rams, Collections.<Selector>emptyList(), MzsConstants.RequestTimeout.ZERO, tx ).get( 0 );
					
					// optional parts
					Gpu gpu = null;
					try {
						gpu = (Gpu) capi.take( gpus, Collections.<Selector>emptyList(), MzsConstants.RequestTimeout.ZERO, tx ).get( 0 );
					} catch ( CountNotMetException e ) {}
					
					// try getting as much ram as you can
					List<RamModule> restRam = null;
					try {
						// 3 + 1 = 4
						restRam = (List<RamModule>) capi.take( rams, any( 3 ), MzsConstants.RequestTimeout.ZERO, tx ).get( 0 );
					} catch ( CountNotMetException e1 ) {
						try {
							// 1 + 1 = 2
							restRam = (List<RamModule>) capi.take( rams, any( 1 ), MzsConstants.RequestTimeout.ZERO, tx ).get( 0 );
						} catch ( CountNotMetException e2 ) {}
					}
					if ( restRam == null ) restRam = Arrays.asList( ram );
					
					Computer pc = new Computer( uuids.generate(), workerId, cpu, gpu, mbd, restRam );
					
					capi.write( pcs, new Entry( pc, LABEL_UNTESTED_FOR_COMPLETENESS, LABEL_UNTESTED_FOR_DEFECT ) );
					
					capi.commitTransaction( tx );			
				} catch ( CountNotMetException e ) {
					// not enough parts present
					continue;
				}
			}
		} catch ( Exception e ) {
			rollback( tx );
			e.printStackTrace();
		} finally {
			clean();
		}
	}
	
	private final ContainerReference pcs;
	private final ContainerReference cpus;
	private final ContainerReference gpus;
	private final ContainerReference mbds;
	private final ContainerReference rams;

	private final UUIDGenerator uuids = new UUIDGenerator();
}
