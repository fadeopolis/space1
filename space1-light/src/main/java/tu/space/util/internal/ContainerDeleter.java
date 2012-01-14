package tu.space.util.internal;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

public class ContainerDeleter<E extends Serializable> {
	public ContainerDeleter( Capi capi, ContainerReference cref ) {
		this.capi = capi;
		this.cref = cref;
	}
	
	public int delete( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return delete( tx, Arrays.asList( sel ) );
	}
	public int delete( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return delete( tx, MzsConstants.RequestTimeout.INFINITE, sel );
	}

	public int deleteNoWait( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return delete( tx, Arrays.asList( sel ) );
	}
	public int deleteNoWait( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return delete( tx, MzsConstants.RequestTimeout.TRY_ONCE, sel );
	}

	public int delete( TransactionReference tx, long timeOut, Selector... sel ) throws MzsCoreException {
		return delete( tx, Arrays.asList( sel ) );
	}
	public int delete( TransactionReference tx, long timeOut, List<? extends Selector> sel ) throws MzsCoreException {
		return capi.delete( cref, sel, MzsConstants.RequestTimeout.TRY_ONCE, tx );
	}
	
	private final Capi               capi;
	private final ContainerReference cref;
}
