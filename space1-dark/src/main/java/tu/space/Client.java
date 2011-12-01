package tu.space;

import java.util.Arrays;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Client {
	public static void main( String... args ) throws Exception {
		final String brokerName = "broker-" + System.currentTimeMillis();
		Connection conn = null;
		
		try {			
			ActiveMQConnectionFactory cf    = new ActiveMQConnectionFactory( "peer://sbc-peers/" + brokerName );

			conn = cf.createConnection();
			conn.start();
			
			System.out.println("STARTED CLIENT " + brokerName );
//			System.out.println("PRESS ENTER TO QUIT");
			System.out.println( Arrays.toString( args ) );
			
			Session    sess = conn.createSession( true, Session.AUTO_ACKNOWLEDGE );

			Queue queue = sess.createQueue( "queue.Test" );
			
			MessageProducer out = sess.createProducer( queue );
			out.send( sess.createTextMessage("hi from " + brokerName ) );
			sess.commit();
			
			Thread.sleep( new Random().nextInt( 3000 ) );
			
			MessageConsumer c = sess.createConsumer( queue );
			Message m;
			while ( (m = c.receive( 500 )) != null ) {
				TextMessage txt = (TextMessage) m;
				
				System.out.println( brokerName + " got " + txt.getText() );
				sess.commit();
			}
		} finally {
			if ( conn != null ) conn.close();
		}
		
		System.out.println( brokerName + " FINISHED" );
	}
}
