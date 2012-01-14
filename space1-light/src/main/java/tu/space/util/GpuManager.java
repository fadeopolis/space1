package tu.space.util;

import java.net.URI;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import tu.space.components.Gpu;

public class GpuManager extends AbstractProductManager<Gpu> {
	public GpuManager( URI space, Capi capi ) throws MzsCoreException {
		super( space, capi, ContainerCreator.getGpuContainer( space, capi ) );
	}
}
