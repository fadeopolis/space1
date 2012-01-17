package tu.space.dark.starters;

import tu.space.dark.worker.DefectTester;

public class StartDarkDefectTester {
	public static void main( String[] args ) throws Exception {
		new DarkStarter().start( DefectTester.class, args );
	}
}
