package tu.space.light.starters;

import ch.qos.logback.classic.Level;
import tu.space.light.SpaceMiddleware;
import tu.space.middleware.Middleware;
import tu.space.util.LogBack;
import tu.space.utils.Starter;
import tu.space.worker.Worker;

public class LightStarter extends Starter {
	@Override
	public void start( Class<? extends Worker> c, String id, int port ) throws Exception {
		LogBack.configure( Level.WARN );
		
		super.start( c, id, port );
	}
	
	@Override
	protected Middleware makeMiddleware( int port ) {
		return new SpaceMiddleware( port );
	}
}
