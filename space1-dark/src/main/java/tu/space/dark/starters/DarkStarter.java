package tu.space.dark.starters;

import tu.space.dark.JMSMiddleware;
import tu.space.middleware.Middleware;
import tu.space.utils.Starter;

public class DarkStarter extends Starter {
	@Override
	protected Middleware makeMiddleware( String id, int port ) {
		return new JMSMiddleware( id, port );
	}
}