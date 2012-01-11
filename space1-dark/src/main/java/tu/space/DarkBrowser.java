package tu.space;

import tu.space.gui.Browser;
import tu.space.utils.Logger;

public class DarkBrowser {
	public static void main( String... args ) throws Exception {
		if ( args.length != 1 ) {
			System.err.println("usage: DarkBrowser PORT" );
			System.exit( 1 );
		}

		Logger.configure();
				
		new Browser( new DarkDataProvider( Integer.parseInt( args[0] ) ) );
	}
}
