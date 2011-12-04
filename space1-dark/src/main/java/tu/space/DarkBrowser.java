package tu.space;

import tu.space.gui.Browser;
import tu.space.utils.Logger;

public class DarkBrowser {
	public static void main( String... args ) throws Exception {
		Logger.configure();
				
		new Browser( new DarkDataProvider() );
	}
}
