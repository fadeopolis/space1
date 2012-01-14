package tu.space.util;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import tu.space.components.Mainboard;

public class MainboardManager extends AbstractProductManager<Mainboard> {
	public MainboardManager( URI space, Capi capi ) throws MzsCoreException {
		super( space, capi, ContainerCreator.getMainboardContainer( space, capi ) );
	}

	@Override
	protected List<FifoSelector> selectors() {
		return Arrays.asList( FifoCoordinator.newSelector() );
	}
}
