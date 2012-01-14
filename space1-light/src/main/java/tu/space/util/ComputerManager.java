package tu.space.util;

import static tu.space.util.ContainerCreator.LABEL_TESTED_FOR_COMPLETENESS;
import static tu.space.util.ContainerCreator.LABEL_TESTED_FOR_DEFECT;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_COMPLETENESS;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_DEFECT;
import static tu.space.util.ContainerCreator.label;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;

public class ComputerManager extends AbstractProductManager<Computer> {
	public ComputerManager( URI space, Capi capi ) throws MzsCoreException {
		super( space, capi, ContainerCreator.getPcContainer( space, capi ) );
	}
	
	public Computer take( TransactionReference tx, LabelSelector s ) throws MzsCoreException {
		return super.takeOne( tx, s );
	}
	
	public Computer takeOne( TransactionReference tx, List<? extends Selector> s ) throws MzsCoreException {
		return super.takeOne( tx, s );
	}
	
	@Override
	protected List<LabelData> coordData( Computer pc ) {
		List<LabelData> cd = new ArrayList<LabelData>();
		
		if ( pc.defect == TestStatus.UNTESTED ) {
			cd.add( LABEL_UNTESTED_FOR_DEFECT );
		} else {
			cd.add( LABEL_TESTED_FOR_DEFECT );
		}
		if ( pc.complete == TestStatus.UNTESTED ) {
			cd.add( LABEL_UNTESTED_FOR_COMPLETENESS );
		} else {
			cd.add( LABEL_TESTED_FOR_COMPLETENESS );
		}
		if ( pc.orderId != null ) {
			cd.add( label(pc.orderId) );
		}		
		
		cd.add( label("ANY") );
	
		return cd;
	}
	@Override
	protected List<LabelSelector> selectors() {
		return Arrays.asList( LabelCoordinator.newSelector( "ANY" ) );
	}

}
