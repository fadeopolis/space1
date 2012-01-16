import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreException;

import tu.space.light.SpaceMiddleware;
import tu.space.util.LogBack;
import tu.space.worker.DefectTester;


public class FavTest {
	public static void main( String[] args ) throws MzsCoreException {
		LogBack.configure();
		
//		MzsCore core = DefaultMzsCore.newInstance( 0 );
//		Capi    capi = new Capi( core );
//		
//		ContainerReference cref = cref( capi );
		
//		new Logistician( "log1", new SpaceMiddleware( 6666 ) );
//		capi.write( 
//			cref, 
//			new Entry( 
//				new Computer( 
//					"foo", 
//					"man2", 
//					"o2",
//					new Cpu( "c", "p", false, Cpu.Type.SINGLE_CORE ),
//					new Gpu( "g", "p", false ),
//					new Mainboard( "m", "p", false ),
//					new RamList( new RamModule("r", "p", false) )
//				).tagAsTestedForCompleteness( "ct", TestStatus.YES )
//				 .tagAsTestedForDefect( "dt", TestStatus.NO ),
//				labels()
//			) 
//		);
	
//		new Manufacturer( "man2", new SpaceMiddleware( 6666 ) );
		new DefectTester( "def2", new SpaceMiddleware( 6666 ) );
		
		print("");
//		print( m.getComputerInput().take() );			
	}
	
	static void print( Object o ) {
		System.err.println( o );
	}
	
	public static List<CoordinationData> labels() {
		List<CoordinationData> cd = new ArrayList<CoordinationData>();

		cd.add( LabelCoordinator.newCoordinationData( "TESTED_FOR_DEFECT" ) );
		cd.add( LabelCoordinator.newCoordinationData( "TESTED_FOR_COMPLETENESS" ) );
//		cd.add( LabelCoordinator.newCoordinationData( "ANY" ) );

		return cd;
	}
	public static List<Selector> sels() {
		List<Selector> cd = new ArrayList<Selector>();

		cd.add( LabelCoordinator.newSelector( "TESTED_FOR_DEFECT" ) );
		cd.add( LabelCoordinator.newSelector( "TESTED_FOR_COMPLETENESS" ) );
//		cd.add( LabelCoordinator.newCoordinationData( "ANY" ) );

		return cd;
	}

	static ContainerReference cref( Capi capi ) throws MzsCoreException {
		try {
			ContainerReference cref = capi.createContainer(
					"Computer",
					URI.create("xvsm://localhost:6666"), 
					MzsConstants.Container.UNBOUNDED, 
					Arrays.asList( new AnyCoordinator() ),
					Arrays.asList( new LabelCoordinator() ),
					null
			);
			return cref;
		} catch ( MzsCoreException e ) {
			return capi.lookupContainer( "Computer", URI.create("xvsm://localhost:6666"), 5000, null );
		}
	}
	
}
