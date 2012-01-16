package tu.space.light.starters;

import tu.space.worker.Logistician;

public class StartLogistician {
	public static void main( String[] args ) throws Exception {
		new LightStarter().start( Logistician.class, args );
	}
}
