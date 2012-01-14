package tu.space.util;

import java.net.URI;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import tu.space.components.Product;

public class TrashManager extends AbstractProductManager<Product> {
	public TrashManager( URI space, Capi capi ) throws MzsCoreException {
		super( space, capi, ContainerCreator.getTrashContainer( space, capi ) );
	}
}
