package tu.space;

import java.util.Scanner;

import org.apache.activemq.broker.BrokerService;

import tu.space.utils.Logger;

public class DarkServer {
	public static final String BROKER_URL = "tcp://localhost:";
	
	public static void main( String... args ) throws Exception {		
		if ( args.length != 1 ) {
			System.err.println("usage: DarkServer PORT" );
			System.exit( 1 );
		}
		
		System.out.println("STARTING DARK SERVER");

		Logger.configure();
				
		BrokerService broker = new BrokerService();
		broker.setBrokerName( "dark-server" );
		broker.addConnector( BROKER_URL + Integer.parseInt( args[0] ) );
		broker.setDeleteAllMessagesOnStartup( true );
		
		broker.start();
		
		System.out.println("PRESS ENTER TO QUIT");
		new Scanner( System.in ).hasNextLine();
		
		broker.stop();
		
		System.out.println("DARK SERVER FINISHED");
	}
}
