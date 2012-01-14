package tu.space.util;

import static tu.space.util.ContainerCreator.any;

import java.net.URI;
import java.util.List;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.RamModule;

public class RamManager extends AbstractProductManager<RamModule> {
	public RamManager( URI space, Capi capi ) throws MzsCoreException {
		super( space, capi, ContainerCreator.getRamContainer( space, capi ) );
	}
	
	public List<RamModule> take( TransactionReference tx, int count ) throws MzsCoreException {
		return take( tx, any(count) );
	}
}
