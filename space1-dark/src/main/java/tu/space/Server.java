package tu.space;

import java.net.URI;
import java.util.Scanner;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

public class Server {
	public static void main( String... args ) throws Exception {
		final String brokerName = "broker-" + System.currentTimeMillis();
		
		BrokerService broker = new BrokerService();
		broker.setBrokerName( brokerName );
		
		broker.addConnector( "peer://sbc-peers/" + brokerName );
		
		TransportConnector tc = new TransportConnector();
		tc.setUri( new URI("peer://sbc-peers/" + brokerName) );
		
		broker.addConnector( tc );
		
//		try {
//			broker.addConnector( "tcp://localhost:61616" );
//		} catch ( Exception e ) {
//			broker.addConnector( "tcp://localhost:61617" );
//		}
//			
//		broker.addNetworkConnector( "peer://sbc-peers/" + brokerName );
		
		broker.start();
		broker.waitUntilStarted();
		
		System.out.println("STARTED SERVER");
		System.out.println("PRESS ENTER TO QUIT");
		
		new Scanner( System.in ).hasNextLine();
		
		broker.stop();
	}
}
