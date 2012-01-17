package tu.space.dark.starters;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.ActiveMQQueue;

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

		// enable advisory messages
		PolicyMap   pm = new PolicyMap();
		PolicyEntry pe = new PolicyEntry();
		pe.setAdvisoryForConsumed( true );
		pe.setAdvisoryForDelivery( true );
		pm.put( new ActiveMQQueue(">"), pe );
		broker.setDestinationPolicy( pm );
		
		broker.start();
	}
}
