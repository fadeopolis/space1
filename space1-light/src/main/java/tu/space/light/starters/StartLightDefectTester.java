package tu.space.light.starters;

import tu.space.worker.DefectTester;

public class StartLightDefectTester {
	public static void main( String[] args ) throws Exception {
		new LightStarter().start( DefectTester.class, args );
	}
}