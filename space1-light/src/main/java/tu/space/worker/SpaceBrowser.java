package tu.space.worker;

import tu.space.gui.Browser;
import tu.space.utils.Logger;

public class SpaceBrowser {
	public static void main( String... args ) throws Exception {
		Logger.configure();
				
		new Browser(new SpaceDataProvider());
	}
}
