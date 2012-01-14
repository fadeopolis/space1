package tu.space.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Product;
import tu.space.util.internal.ContainerUtil;

public abstract class AbstractProductManager<P extends Product> implements ProductManager<P> {
	public AbstractProductManager( URI space, Capi capi, ContainerReference cref ) throws MzsCoreException {
		this.util = new ContainerUtil<P>( capi, cref );
		this.capi = capi;
		this.cref = cref;
	}
	
	public void write( TransactionReference tx, P p ) throws MzsCoreException {
		util.write.write( p, tx, coordData( p ) );
	}
	
	public P take( TransactionReference tx ) throws MzsCoreException {
		return util.take.takeOneNoWait( tx, selectors() );
	}

	@Override
	public void registerNotification( NotificationListener nl, Operation... ops ) throws MzsCoreException, InterruptedException {
		if ( nm == null ) nm = new NotificationManager( capi.getCore() );

		ns.add( nm.createNotification( cref, nl, ops ) );
	}
	
	@Override
	public void shutdown() throws MzsCoreException {
		MzsCoreException e = null;
		
		if ( nm != null ) nm.shutdown();

		for ( Notification n : ns ) {
			try {
				n.destroy();
			} catch ( MzsCoreException ex ) {
				e = ex;
			}
		}
		
		if ( e != null ) throw e;
	}
	
	@Override
	public String id() {
		return cref.getId();
	}
	
	//***** PROTECTED
	
	// hooks
	protected List<? extends Selector>         selectors()      { return Collections.emptyList(); }
	protected List<? extends CoordinationData> coordData( P p ) { return Collections.emptyList(); }
	
	// use these in sub classes if you need them
	protected void write( TransactionReference tx, P p, CoordinationData cd ) throws MzsCoreException {
		util.write.write( p, tx, cd );
	}
	
	protected P takeOne( TransactionReference tx, Selector s ) throws MzsCoreException {
		return take( tx, s ).get( 0 );
	}
	protected P takeOne( TransactionReference tx, List<? extends Selector> s ) throws MzsCoreException {
		return take( tx, s ).get( 0 );
	}
	protected List<P> take( TransactionReference tx, Selector s ) throws MzsCoreException {
		return util.take.takeNoWait( tx, s );
	}
	protected List<P> take( TransactionReference tx, List<? extends Selector> s ) throws MzsCoreException {
		return util.take.takeNoWait( tx, s );
	}
	
	//***** PRIVATE
	
	private final ContainerUtil<P>    util;
	private final Capi                capi;
	private final ContainerReference  cref;
	
	private NotificationManager nm;
	private final List<Notification>  ns = new ArrayList<Notification>();
}
