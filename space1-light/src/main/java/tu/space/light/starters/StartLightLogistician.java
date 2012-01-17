package tu.space.light.starters;

import tu.space.worker.Logistician;

public class StartLightLogistician {
	public static void main( String[] args ) throws Exception {
		new LightStarter().start( Logistician.class, args );
	}
}
