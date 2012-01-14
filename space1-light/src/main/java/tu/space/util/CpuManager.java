package tu.space.util;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Cpu;

public class CpuManager extends AbstractProductManager<Cpu> {
	public CpuManager( URI space, Capi capi ) throws MzsCoreException {
		super( space, capi, ContainerCreator.getCpuContainer( space, capi ) );
	}
	
	public Cpu take( TransactionReference tx, Cpu.Type type ) throws MzsCoreException {
		if ( type == null ) 
			return take( tx );
		else
			return takeOne( tx, LabelCoordinator.newSelector( type.name() ) );
	}

	@Override
	protected List<LabelData> coordData( Cpu c ) {
		return Arrays.asList( ContainerCreator.label( "ANY" ), ContainerCreator.label( c.type.name() ) );
	}
	@Override
	protected List<LabelSelector> selectors() {
		return Arrays.asList( LabelCoordinator.newSelector( "ANY" ) );
	}
}
