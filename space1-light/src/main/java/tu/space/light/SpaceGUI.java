package tu.space.light;

import org.mozartspaces.core.DefaultMzsCore;

import tu.space.gui.GUI;
import tu.space.util.LogBack;
import tu.space.worker.CompletenessTester;
import tu.space.worker.DefectTester;
import tu.space.worker.Logistician;
import tu.space.worker.Manufacturer;

public class SpaceGUI {
	public static void main( String... args ) throws Exception {
		LogBack.configure();

		DefaultMzsCore.newInstance( 6666 );
		
		new GUI( new SpaceMiddleware( 6666 ) );
				
		new Manufacturer( "man1", new SpaceMiddleware( 6666 ) );
		new DefectTester( "def1", new SpaceMiddleware( 6666 ) );
		new CompletenessTester( "com1", new SpaceMiddleware( 6666 ) );
		new Logistician( "log1", new SpaceMiddleware( 6666 ) );
		
		new Manufacturer( "man2", new SpaceMiddleware( 6666 ) );
		new DefectTester( "def2", new SpaceMiddleware( 6666 ) );
		new CompletenessTester( "com2", new SpaceMiddleware( 6666 ) );
		new Logistician( "log2", new SpaceMiddleware( 6666 ) );

		new Manufacturer( "man3", new SpaceMiddleware( 6666 ) );
		new DefectTester( "def3", new SpaceMiddleware( 6666 ) );
		new CompletenessTester( "com3", new SpaceMiddleware( 6666 ) );
		new Logistician( "log3", new SpaceMiddleware( 6666 ) );

		while ( true ) {}
	}
}
