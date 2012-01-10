package tu.space.light;

import static tu.space.util.ContainerCreator.DEFAULT_TX_TIMEOUT;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_COMPLETENESS;
import static tu.space.util.ContainerCreator.LABEL_UNTESTED_FOR_DEFECT;
import static tu.space.util.ContainerCreator.SELECTOR_UNTESTED_FOR_COMPLETENESS;
import static tu.space.util.ContainerCreator.getPcContainer;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;
import org.slf4j.LoggerFactory;

import tu.space.components.Computer;
import tu.space.components.RamModule;
import tu.space.util.ContainerCreator;
import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class FavTest implements NotificationListener {
	public static void main( String... args ) throws MzsCoreException, InterruptedException {
		tu.space.utils.Logger.configure();	
		BasicConfigurator.configureDefaultContext();
		Logger lc = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger( Logger.ROOT_LOGGER_NAME );
		lc.setLevel( Level.ERROR );
		
		//embedded space on localhost port 9877
		MzsCore core = ContainerCreator.getCore( 9987 );
		final Capi    capi = new Capi(core);
		
		final ContainerReference cref = getPcContainer( core.getConfig().getSpaceUri(), capi );
		
		NotificationManager nm = new NotificationManager( core );
//		nm.createNotification( cref, new FavTest(), EnumSet.of( Operation.WRITE ), null, null );
		
		capi.createTransaction( DEFAULT_TX_TIMEOUT, core.getConfig().getSpaceUri() );
		
		Worker w = new DefectTester( "test", "9987" );
		new Thread( w ).start();
		Thread.sleep( 500 );
		capi.write( 
			cref, 
			new Entry( 
				new Computer( "foo", "foo", null, null, null, new RamModule[0] ), 
				LABEL_UNTESTED_FOR_DEFECT, LABEL_UNTESTED_FOR_COMPLETENESS,
				LabelCoordinator.newCoordinationData( "Computer.id:foo" )
			) 
		);
				
		
		Thread.sleep( 2000 );
		
		List<Serializable> l = capi.take( cref, SELECTOR_UNTESTED_FOR_COMPLETENESS, 1000, null );
		for ( Object o : l ) {
			System.err.println( o );
		}
		
		w.clean();
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
