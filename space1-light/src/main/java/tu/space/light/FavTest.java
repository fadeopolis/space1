package tu.space.light;

import static tu.space.util.ContainerCreator.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.util.ComputerManager;
import tu.space.util.LogBack;
import tu.space.util.OrderManager;
import ch.qos.logback.classic.Level;

public class FavTest implements NotificationListener {
	public static void main( String... args ) throws Exception {
		tu.space.utils.Logger.configure();	
		LogBack.configure( Level.ERROR );

		final MzsCore core = DefaultMzsCore.newInstance( 9877 );
		final Capi    capi = new Capi(core);

		try {
			main( core, capi );			
		} catch ( Exception e ) {
			throw e;
		} finally {
			core.shutdown( true );			
		}
	}
	static void main( final MzsCore core, final Capi capi ) throws MzsCoreException {
		ComputerManager cm = new ComputerManager( core.getConfig().getSpaceUri(), capi );
						
		OrderManager om = new OrderManager( core.getConfig().getSpaceUri(), capi );
		
		TransactionReference tx;
		
		tx = tx( capi );
			cm.write( tx, new Computer("pc1", "man1", null, null, null, null, new RamModule[0]) );		
		capi.commitTransaction( tx );

		tx = tx( capi );
			print( cm.take( tx, SELECTOR_UNTESTED_FOR_DEFECT ) );
		capi.commitTransaction( tx );

//		tx = tx( capi );
//			om.publishOrder( new Order( "o1", Cpu.Type.SINGLE_CORE, 2, true, 5 ), tx );
//			om.publishOrder( new Order( "o2", Cpu.Type.SINGLE_CORE, 2, true, 5 ), tx );
//		capi.commitTransaction( tx );
		
//		tx = tx( capi );
//			for ( Order o : om.allOrders( tx ) ) {
//				System.err.println( o );
//				om.returnOrder( tx, o );
//			}
//			System.err.println();
//		capi.commitTransaction( tx );
//
//		tx = tx( capi );
//			om.signalPcIsProduced( tx, new Computer( null, null, "o1", null, null, null, new RamModule[0] ) );
//		capi.commitTransaction( tx );
//		tx = tx( capi );
//			om.signalPcIsDefect( tx, new Computer( null, null, "o1", null, null, null, new RamModule[0] ) );
//		capi.commitTransaction( tx );
//		tx = tx( capi );
//			om.signalPcIsProduced( tx, new Computer( null, null, "o1", null, null, null, new RamModule[0] ) );
//		capi.commitTransaction( tx );
//		tx = tx( capi );
//			om.signalPcIsFinished( tx, new Computer( null, null, "o1", null, null, null, new RamModule[0] ) );
//		capi.commitTransaction( tx );
//		
//		tx = tx( capi );
//			for ( Order o : om.allOrders( tx ) ) {
//				System.err.println( o );
//				om.returnOrder( tx, o );
//			}
//			System.err.println();
//		capi.commitTransaction( tx );

		
//		capi.write( cref, new Entry( "foo", KeyCoordinator.newCoordinationData( "foo" ) ) );
//		capi.write( cref, new Entry( "bar", LabelCoordinator.newCoordinationData( "foo" ) ) );
//		capi.write( cref, new Entry( "bar", LabelCoordinator.newCoordinationData( "foo" ) ) );
		
//		System.err.println( capi.test( cref, ANY_MAX, RequestTimeout.ZERO, tx ) );
	}

	static void print( Object o ) {
		System.err.println( o );
	}
	
	static TransactionReference tx( Capi capi ) throws MzsCoreException {
		return capi.createTransaction( MzsConstants.TransactionTimeout.INFINITE, capi.getCore().getConfig().getSpaceUri() );
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
