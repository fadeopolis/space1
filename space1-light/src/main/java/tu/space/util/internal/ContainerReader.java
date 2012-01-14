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

public class ContainerReader<E extends Serializable> {
	public ContainerReader( Capi capi, ContainerReference cref ) {
		this.capi = capi;
		this.cref = cref;
	}
//	public ContainerReader( URI space, Capi capi, String name, Coordinator... cs ) throws MzsCoreException {
//		this( capi, ContainerCreator.getContainer( capi, space, name, cs ) );
//	}

	//***** READ SINGLE ELEMENT
	
	public E readOne( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return readOne( tx, Arrays.asList( sel ) );
	}
	public E readOne( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return readOne( tx, MzsConstants.RequestTimeout.INFINITE, sel );
	}

	public E readOneNoWait( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return readOneNoWait( tx, Arrays.asList( sel ) );
	}
	public E readOneNoWait( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return readOne( tx, MzsConstants.RequestTimeout.TRY_ONCE, sel );
	}
	
	public E readOne( TransactionReference tx, long timeOut, Selector... sel ) throws MzsCoreException {
		return readOne( tx, Arrays.asList( sel ) );
	}
	public E readOne( TransactionReference tx, long timeOut, List<? extends Selector> sel ) throws MzsCoreException {
		return read( tx, timeOut, sel ).get( 0 );
	}
	
	//***** READ BULK
	
	public List<E> read( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return read( tx, Arrays.asList( sel ) );
	}
	public List<E> read( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return read( tx, MzsConstants.RequestTimeout.INFINITE, sel );
	}

	public List<E> readNoNoWait( TransactionReference tx, Selector... sel ) throws MzsCoreException {
		return read( tx, Arrays.asList( sel ) );
	}
	public List<E> readNoWait( TransactionReference tx, List<? extends Selector> sel ) throws MzsCoreException {
		return read( tx, MzsConstants.RequestTimeout.TRY_ONCE, sel );
	}

	
	public List<E> read( TransactionReference tx, long timeOut, Selector... sel ) throws MzsCoreException {
		return read( tx, Arrays.asList( sel ) );
	}
	public List<E> read( TransactionReference tx, long timeOut, List<? extends Selector> sel ) throws MzsCoreException {
		List<Serializable> s = capi.read( cref, sel, MzsConstants.RequestTimeout.TRY_ONCE, tx );
		
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
