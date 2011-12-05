package tu.space;

import java.util.Scanner;

import org.apache.activemq.broker.BrokerService;

import tu.space.utils.Logger;

public class DarkServer {
	public static final String BROKER_URL = "tcp://localhost:61616";
	
	public static void main( String... args ) throws Exception {		
		System.out.println("STARTING DARK SERVER");

		Logger.configure();
				
		BrokerService broker = new BrokerService();
		broker.setBrokerName( "dark-server" );
		broker.addConnector( BROKER_URL );
		broker.setDeleteAllMessagesOnStartup( true );
		
		broker.start();
		
		System.out.println("PRESS ENTER TO QUIT");
		new Scanner( System.in ).hasNextLine();
		
		broker.stop();
		
		System.out.println("DARK SERVER FINISHED");
	}
}
