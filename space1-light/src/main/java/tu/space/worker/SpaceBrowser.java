package tu.space.worker;

import tu.space.gui.Browser;
import tu.space.util.LogBack;
import tu.space.utils.Logger;

public class SpaceBrowser {
	public static void main( String... args ) throws Exception {
		Logger.configure();
		LogBack.configure();
				
		new Browser(new SpaceDataProvider());
	}
}
