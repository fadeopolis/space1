package tu.space.light.starters;

import tu.space.light.SpaceMiddleware;
import tu.space.middleware.Middleware;
import tu.space.util.LogBack;
import tu.space.utils.Starter;

public class LightStarter extends Starter {
	@Override
	public void start( Class<?> c, String id, int port ) throws Exception {
		LogBack.configure();
		
		super.start( c, id, port );
	}
	
	@Override
	protected Middleware makeMiddleware( String id, int port ) {
		return new SpaceMiddleware( port );
	}
}
