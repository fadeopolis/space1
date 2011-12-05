package tu.space;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.net.ssl.SSLEngineResult.Status;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.components.Computer;
import tu.space.components.RamModule;
import tu.space.components.Computer.TestStatus;
import tu.space.middleware.unused.JMSMiddleware;
import tu.space.middleware.unused.JMSMiddlewareFactory;
import tu.space.middleware.unused.Listener;
import tu.space.unused.middleware.Middleware;
import tu.space.utils.Logger;
import tu.space.utils.Util;

public class Test {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main( String... args ) throws InterruptedException, JMSException {
		Logger.configure();

//		final Middleware m = new JMSMiddlewareFactory().make();
		
//		m.cpus().setConsumingListener( (Listener<Cpu>) l );
//		m.gpus().setConsumingListener( (Listener<Gpu>) l );
//		m.mainboards().setConsumingListener( (Listener<Mainboard>) l );
//		m.ramModules().setConsumingListener( (Listener<RamModule>) l );
//		m.start();
//	
//		m.allComputers().send(
//			new Computer(UUID.randomUUID(), "foo", null, null, null, new RamModule[0])
////				.tagAsTestedForDefect( "me", TestStatus.NO )
//		);
//		m.commit();
//
//		m.stop();

		Connection conn = new ActiveMQConnectionFactory( DarkServer.BROKER_URL ).createConnection();
		Session    sess = conn.createSession( true, Session.AUTO_ACKNOWLEDGE );
	
		Queue queue = sess.createQueue( "computer" );
		Topic topic = sess.createTopic( "computer" );
		
		MessageProducer qOut = sess.createProducer( queue );
		MessageProducer tOut = sess.createProducer( topic );
		
		Computer c = new Computer(UUID.randomUUID(), "foo", null,null,null, Arrays.<RamModule>asList());

//		c = c.tagAsTestedForDefect( "foo", TestStatus.YES );
		
		ObjectMessage msg = JMS.toCreatedMessage( sess, c );
		
		conn.start();
		qOut.send( msg );
		tOut.send( msg );
		sess.commit();		
		JMS.close( conn );
		
//		MessageConsumer in = sess.createConsumer( sess.createQueue( "computers.queue" ), "tested_for_defect=false" );
//		in.setMessageListener( new MessageListener() {
//			@Override
//			public void onMessage( Message message ) {
//				try {
//					ObjectMessage msg = (ObjectMessage) message;
//					
//					String str = "";
//					str += "GOT: \n";
//					str += "\t" + msg.getJMSDestination() + "\n";
//					str += "\t" + msg.getObject()         + "\n";
//
//					for ( Enumeration<String> e = msg.getPropertyNames(); e.hasMoreElements(); ) {
//						String name = e.nextElement();
//						
//						str += "\t" + name + "=" + msg.getStringProperty( name ) + "\n";
//					}
//
//					System.out.println( str );
//				} catch ( JMSException e ) {
//					e.printStackTrace();
//				}
//			}
//		});
//		conn.start();
//		
//		System.out.println("PRESS ENTER TO QUIT");
//		Util.waitForNewline();
//
//		JMS.close( conn );
	}
}
