package tu.space.dark.starters;

import tu.space.worker.Manufacturer;

public class StartDarkManufacturer {
	public static void main( String[] args ) throws Exception {
		new DarkStarter().start( Manufacturer.class, args );
	}
}
