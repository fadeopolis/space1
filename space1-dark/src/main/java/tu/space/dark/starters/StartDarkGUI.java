package tu.space.dark.starters;

import tu.space.gui.GUI;

public class StartDarkGUI extends DarkStarter {
	public static void main( String[] args ) throws Exception {
		new StartDarkGUI().start( GUI.class, args );
	}
	
	@Override
	public void start( Class<?> c, String id, int port ) throws Exception {
		new GUI( makeMiddleware( port ) );
	}
}
