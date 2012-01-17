package tu.space.light.starters;

import tu.space.worker.CompletenessTester;

public class StartLightCompletenessTester {
	public static void main( String[] args ) throws Exception {
		new LightStarter().start( CompletenessTester.class, args );
	}
}
