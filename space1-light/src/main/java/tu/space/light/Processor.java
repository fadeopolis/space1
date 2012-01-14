package tu.space.light;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.util.ContainerCreator;
import tu.space.util.OrderManager;
import tu.space.util.ProductManager;
import tu.space.utils.SpaceException;

/**
 * A processor reads a part from the space, does something with it and writes something else to the space
 */
public abstract class Processor<E> extends Worker {
	public Processor( String id, Capi capi, int space ) throws MzsCoreException {
		super( id, capi, space );
		
		this.notifications = new NotificationManager( capi.getCore() );
		this.orders        = ContainerCreator.getOrders( this.space, capi );
	}
	
	@Override
	public final void run() {
		try {
			registerNotifications();
		} catch ( MzsCoreException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Called at startup, use registerNotification() for setup
	 */
	protected abstract void registerNotifications() throws MzsCoreException;

	/**
	 * Check if we should care about this part
	 * !! This does not run in a transaction !!
	 */
	protected abstract boolean shouldProcess( E e, Operation o, List<CoordinationData> cds );

	/**
	 * Process part
	 * !! This runs in a transaction that is automatically committed when process() returns true !!
	 */
	protected abstract boolean process( E e, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException;

	protected final void registerNotification( ProductManager<?> p, Operation... ops ) {
		try {
			p.registerNotification( new Listener(), ops );
		} catch ( MzsCoreException e ) {
			log.error("Could not register operation for %s", p.id() );
			throw new SpaceException( e );
		} catch ( InterruptedException e ) {
			log.error("Could not register operation for %s", p.id() );
			throw new SpaceException( e );
		}		
	}
	
	protected final void registerNotification( ContainerReference cref, Operation... ops ) {
		try {
			notifications.createNotification( cref, new Listener(), ops );
		} catch ( MzsCoreException e ) {
			log.error("Could not register operation for %s", cref.getId() );
			throw new SpaceException( e );
		} catch ( InterruptedException e ) {
			log.error("Could not register operation for %s", cref.getId() );
			throw new SpaceException( e );
		}
	}

	private final class Listener implements NotificationListener {
		@SuppressWarnings("unchecked")
		@Override
		public void entryOperationFinished( Notification source, Operation operation, List<? extends Serializable> entries ) {
			for ( Serializable s : entries ) {
				List<CoordinationData> cds;
				
				if ( s instanceof Entry ) {
					Entry e = (Entry) s;
					
					s   = e.getValue();
					cds = e.getCoordinationData();
				} else {
					cds = new ArrayList<CoordinationData>();
				}
				
				E e = (E) s;
	
				if ( !shouldProcess( e, operation, cds ) ) { return; }
					
				TransactionReference tx = null;
				try {
					tx = beginTransaction();
					
					if ( process( e, operation, cds, tx ) ) {
						commit( tx );						
					}
				} catch ( Exception ex ) {
					rollback( tx );
					
					log.error("Error while processing element %s", e );
					ex.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void clean() {
		notifications.shutdown();
		super.clean();
	}
	
	private final NotificationManager notifications;
	protected final OrderManager orders;
}
