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
		
		final String peerGroup  = "sbc";
		final String uri        = "tcp://localhost:61616";//"peer://" + peerGroup + "/" + brokerName; //+ "?persistent=false";
				
		Thread.sleep( new Random().nextInt( 1000 ) );
		
		ActiveMQConnectionFactory cf    = new ActiveMQConnectionFactory( uri );

		Connection conn = cf.createConnection();
		Session    sess = conn.createSession( false, Session.AUTO_ACKNOWLEDGE );

		conn.start();
		
		Queue queue = sess.createQueue( "queue.Test" );
		
		MessageProducer out = sess.createProducer( queue );
		out.send( sess.createTextMessage("hi") );
//		sess.commit();
		
		Thread.sleep( new Random().nextInt( 1000 ) );
		
		MessageConsumer c = sess.createConsumer( queue );
		System.out.println( c.receive( 500 ) );
		
		conn.close();
		broker.stop();
		
//		broker.addConnector( new TransportConnector() );
//		
//		broker.addConnector( "peer://" + peerGroup + "/" + brokerName ); //+ "?persistent=false" );
////		broker.addConnector("tcp://localhost:61616");
//
//		broker.start();
//
//		Thread.sleep( new Random().nextInt( 1000 ) );
//		
//		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
//		ActiveMQQueue             queue   = new ActiveMQQueue("queue.Test");
//		
//		Connection conn = factory.createConnection();
//		Session    sess = conn.createSession( false, Session.AUTO_ACKNOWLEDGE );
//		
//		MessageConsumer c = sess.createConsumer( queue );
//		
//		c.receive( 100 );
//		
//		MessageProducer p = sess.createProducer( queue );
//		p.send( sess.createTextMessage( "hi" ) );
//		
//		broker.stop();
	}
}
