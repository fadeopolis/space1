package tu.space.util.internal;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

public class ContainerWriter<E extends Serializable> {
	public ContainerWriter( Capi capi, ContainerReference cref ) {
		this.capi = capi;
		this.cref = cref;
	}
	
	public void write( E e, TransactionReference tx, CoordinationData... cd ) throws MzsCoreException {
		write( e, tx, Arrays.asList( cd ) );
	}
	public void write( E e, TransactionReference tx, List<? extends CoordinationData> cd ) throws MzsCoreException {
		write( e, tx, MzsConstants.RequestTimeout.INFINITE, cd );
	}

	public void writeNoWait( E e, TransactionReference tx, CoordinationData... cd ) throws MzsCoreException {
		write( e, tx, Arrays.asList( cd ) );
	}
	public void writeNoWait( E e, TransactionReference tx, List<? extends CoordinationData> cd ) throws MzsCoreException {
		write( e, tx, MzsConstants.RequestTimeout.TRY_ONCE, cd );
	}
	
	public void write( E e, TransactionReference tx, long timeOut, CoordinationData... cd ) throws MzsCoreException {
		write( e, tx, timeOut, Arrays.asList( cd ) );
	}
	public void write( E e, TransactionReference tx, long timeOut, List<? extends CoordinationData> cd ) throws MzsCoreException {
		capi.write( cref, timeOut, tx, new Entry( e, cd ) );
	}
	
	private final Capi               capi;
	private final ContainerReference cref;
}
