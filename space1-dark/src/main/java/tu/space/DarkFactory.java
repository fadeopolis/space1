package tu.space;

import java.util.Random;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

public class DarkFactory {
	public static void main( String... args ) throws Exception {
		final String brokerName = "broker-" + System.currentTimeMillis();
		
		BrokerService broker = new BrokerService();
		broker.setBrokerName( brokerName );
		broker.addConnector( "tcp://localhost:61616" );
		
		broker.start();
		
		final String uri        = "tcp://localhost:61616";
				
		Thread.sleep( new Random().nextInt( 1000 ) );
		
		ActiveMQConnectionFactory cf    = new ActiveMQConnectionFactory( uri );

		Connection conn = cf.createConnection();
		Session    sess = conn.createSession( false, Session.AUTO_ACKNOWLEDGE );

		conn.start();
		
		Queue queue = sess.createQueue( "queue.Test" );
		
		MessageProducer out = sess.createProducer( queue );
		out.send( sess.createTextMessage("hi") );


		Thread.sleep( new Random().nextInt( 1000 ) );
		
		MessageConsumer c = sess.createConsumer( queue );
		System.out.println( c.receive( 500 ) );
		
		conn.close();
		broker.stop();
	}
}
