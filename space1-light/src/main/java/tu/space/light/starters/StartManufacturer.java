package tu.space.light.starters;

import tu.space.worker.Manufacturer;

public class StartManufacturer {
	public static void main( String[] args ) throws Exception {
		new LightStarter().start( Manufacturer.class, args );
	}
}
