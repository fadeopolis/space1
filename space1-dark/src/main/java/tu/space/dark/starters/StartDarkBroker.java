package tu.space.dark.starters;

import org.apache.activemq.broker.BrokerService;

import tu.space.utils.Logger;

public class StartDarkBroker extends DarkStarter {
	public static void main( String[] args ) throws Exception {
		new StartDarkBroker().start( BrokerService.class, args );
	}

	@Override
	public void start( Class<?> c, String id, int port ) throws Exception {
		Logger.configure();
		
		BrokerService broker = new BrokerService();
		broker.setBrokerName( "dark-server" );
		broker.addConnector( "tcp://localhost:" + port );
		broker.setDeleteAllMessagesOnStartup( true );
		
		broker.start();
	}
}
