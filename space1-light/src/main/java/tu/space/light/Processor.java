package tu.space.light;

import java.io.Serializable;
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
import tu.space.utils.SpaceException;

/**
 * A processor reads a part from the space, does something with it and writes something else to the space
 */
public abstract class Processor<E> extends Worker {
	public Processor( String[] args ) {
		super( args );
		
		this.notifications = new NotificationManager( capi.getCore() );
	}
	
	public Processor( String id, Capi capi, int space ) {
		super( id, capi, space );
		
		this.notifications = new NotificationManager( capi.getCore() );
	}
	
	@Override
	public final void run() {
		registerNotifications();
	}

	/**
	 * Called at startup, use registerNotification() for setup
	 */
	protected abstract void registerNotifications();

	/**
	 * Check if we should care about this part
	 * !! This does not run in a transaction !!
	 */
	protected abstract boolean shouldProcess( E e, Operation o, List<CoordinationData> cds );

	/**
	 * Process part
	 * !! This runs in a transaction that is automatically committed when process() returns !!
	 */
	protected abstract void process( E e, Operation o, List<CoordinationData> cds, TransactionReference tx ) throws MzsCoreException;
	
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
				if ( !(s instanceof Entry) ) {
					log.error( "WTF? Got something in a notification that is not an entry: %s", s );
				}
				
				Entry                  entry = (Entry) s;
				E                      e     = (E) entry.getValue();
				List<CoordinationData> cds   = entry.getCoordinationData();

				if ( !shouldProcess( e, operation, cds ) ) { return; }
				
				TransactionReference tx = null;
				try {
					tx = capi.createTransaction( ContainerCreator.DEFAULT_TX_TIMEOUT, space );
					
					process( e, operation, cds, tx );

					capi.commitTransaction( tx );
				} catch ( MzsCoreException ex ) {
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
}
