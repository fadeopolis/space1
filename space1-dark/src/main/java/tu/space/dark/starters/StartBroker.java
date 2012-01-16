package tu.space.dark.starters;

import org.apache.activemq.broker.BrokerService;

import tu.space.middleware.Middleware;
import tu.space.utils.Starter;

public class StartBroker extends Starter {
	public static void main( String[] args ) throws Exception {
		new StartBroker().start( BrokerService.class, args );
	}

	@Override
	public void start( Class<?> c, String id, int port ) throws Exception {
		BrokerService broker = new BrokerService();
		broker.setBrokerName( "dark-server" );
		broker.addConnector( "tcp://localhost:" + port );
		broker.setDeleteAllMessagesOnStartup( true );
		
		broker.start();
	}

	@Override
	protected Middleware makeMiddleware( int port ) {
		return null;
	}
}
