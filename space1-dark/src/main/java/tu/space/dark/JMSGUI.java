package tu.space.dark;

import java.util.Scanner;

import org.apache.activemq.broker.BrokerService;

import tu.space.gui.GUI;
import tu.space.utils.Logger;

public class JMSGUI {
	public static void main( String[] args ) throws Exception {
		if ( args.length != 1 ) {
			System.err.println("usage: DarkServer PORT" );
			System.exit( 1 );
		}

		int port = Integer.parseInt( args[0] );
		
		System.out.println("STARTING DARK SERVER ON PORT " + port );

		Logger.configure();
				
//		BrokerService broker = new BrokerService();
//		broker.setBrokerName( "dark-server" );
//		broker.addConnector( "tcp://localhost:" + port );
//		broker.setDeleteAllMessagesOnStartup( true );
//		
//		broker.start();
//		
		new GUI( new JMSMiddleware( port ) );
		
		System.out.println("PRESS ENTER TO QUIT");
		new Scanner( System.in ).hasNextLine();
		
//		broker.stop();
		
		System.out.println("DARK SERVER FINISHED");
	}
}
