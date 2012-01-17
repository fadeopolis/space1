package tu.space.dark.starters;

import tu.space.dark.worker.CompletenessTester;

public class StartDarkCompletenessTester {
	public static void main( String[] args ) throws Exception {
		new DarkStarter().start( CompletenessTester.class, args );
	}
}
