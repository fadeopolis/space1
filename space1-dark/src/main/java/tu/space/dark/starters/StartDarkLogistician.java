package tu.space.dark.starters;

import tu.space.dark.worker.Logistician;

public class StartDarkLogistician {
	public static void main( String[] args ) throws Exception {
		new DarkStarter().start( Logistician.class, args );
	}
}