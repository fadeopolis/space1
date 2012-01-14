package tu.space.util;

import java.net.URI;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;

import tu.space.components.Computer;

public class StorageManager extends AbstractProductManager<Computer> {
	public StorageManager( URI space, Capi capi ) throws MzsCoreException {
		super( space, capi, ContainerCreator.getStorageContainer( space, capi ) );
	}
}
