package tu.space.util.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

public class ContainerTaker<E extends Serializable> {
	public ContainerTaker( Capi capi, ContainerReference cref ) {
		this.capi = capi;
		this.cref = cref;
	}

	//***** READ SINGLE ELEMENT
	
	public E takeOne( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return takeOne( tx, Arrays.asList( sel ) );
	}
	public E takeOne( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return takeOne( tx, MzsConstants.RequestTimeout.INFINITE, sel );
	}

	public E takeOneNoWait( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return takeOneNoWait( tx, Arrays.asList( sel ) );
	}
	public E takeOneNoWait( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return takeOne( tx, MzsConstants.RequestTimeout.TRY_ONCE, sel );
	}
	
	public E takeOne( TransactionReference tx, long timeOut, Selector... sel ) throws MzsCoreException {
		return takeOne( tx, Arrays.asList( sel ) );
	}
	public E takeOne( TransactionReference tx, long timeOut, List<? extends Selector> sel ) throws MzsCoreException {
		return take( tx, timeOut, sel ).get( 0 );
	}
	
	//***** READ BULK
	
	public List<E> take( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return take( tx, Arrays.asList( sel ) );
	}
	public List<E> take( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return take( tx, MzsConstants.RequestTimeout.INFINITE, sel );
	}

	public List<E> takeNoWait( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return take( tx, Arrays.asList( sel ) );
	}
	public List<E> takeNoWait( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return take( tx, MzsConstants.RequestTimeout.TRY_ONCE, sel );
	}

	
	public List<E> take( TransactionReference tx, long timeOut, Selector... sel ) throws MzsCoreException {
		return take( tx, Arrays.asList( sel ) );
	}
	public List<E> take( TransactionReference tx, long timeOut, List<? extends Selector> sel ) throws MzsCoreException {
		List<Serializable> s = capi.take( cref, sel, MzsConstants.RequestTimeout.TRY_ONCE, tx );
		
		return process( s );
	}
	
	private static <E> List<E> process( List<Serializable> l ) {
		List<E> es = new ArrayList<E>( l.size() );
		
		for ( Serializable s : l ) {
			@SuppressWarnings("unchecked")
			E e = (E) (s instanceof Entry ? ((Entry) s).getValue() : s);
			
			es.add( e );
		}
		
		return es;
	}
	
	private final Capi               capi;
	private final ContainerReference cref;
}
