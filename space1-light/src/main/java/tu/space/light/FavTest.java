package tu.space.light;

import static tu.space.util.ContainerCreator.*;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.*;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;
import tu.space.util.LogBack;
import ch.qos.logback.classic.Level;

public class FavTest implements NotificationListener {
	public static void main( String... args ) throws MzsCoreException, InterruptedException {
		tu.space.utils.Logger.configure();	
		LogBack.configure( Level.ERROR );

		MzsCore core = DefaultMzsCore.newInstance( 9877 );
		final Capi    capi = new Capi(core);

		final ContainerReference cref = getContainer( capi, core.getConfig().getSpaceUri(), "foo",
			new LabelCoordinator(),
			new AnyCoordinator()
		);
				
		TransactionReference tx = capi.createTransaction( DEFAULT_TX_TIMEOUT, core.getConfig().getSpaceUri() );
		
		capi.write( cref, new Entry( "foo", LabelCoordinator.newCoordinationData( "foo" ) ) );
		capi.write( cref, new Entry( "bar", LabelCoordinator.newCoordinationData( "foo" ) ) );
		capi.write( cref, new Entry( "bar", LabelCoordinator.newCoordinationData( "foo" ) ) );
		
		System.err.println( capi.test( cref, ANY_MAX, RequestTimeout.ZERO, tx ) );
		
		core.shutdown( true );
	}

	@Override
	public void entryOperationFinished( Notification source, Operation operation, List<? extends Serializable> entries ) {
		for ( Serializable s : entries ) {
			if ( !(s instanceof Entry) ) {
				System.err.println("??" + s);
				continue;
			}
			
			Entry                  e   = (Entry) s;
			List<CoordinationData> cds = e.getCoordinationData();
			
			System.err.println("GOT: "  + e );
			for ( CoordinationData cd : cds ) {
				System.err.print("\t");
				System.err.println( "'" + ((LabelData) cd).getLabel() + "'" );
			}
		}
		
		try {
			source.destroy();
		} catch ( MzsCoreException e ) {
			e.printStackTrace();
		}
	}
}
