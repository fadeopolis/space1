package tu.space;

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

import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.utils.Logger;
import tu.space.utils.Util;

public class DarkDefectTester {
	public static final String USAGE    = "usage: defect-tester ID";
	public static final String SELECTOR = "defect IS NOT NULL";
	
	public static void main( String... args ) throws JMSException {
		if ( args.length != 1 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id = args[0];
		
		Logger.configure();
		final Logger log = Logger.make( DarkDefectTester.class );

		final Connection conn = new ActiveMQConnectionFactory( DarkServer.BROKER_URL ).createConnection();
		final Session    sess = conn.createSession( true, Session.AUTO_ACKNOWLEDGE );
	
		final Queue queue = sess.createQueue( "computer" );
		final Topic topic = sess.createTopic( "computer" );
		
		final MessageProducer qOut = sess.createProducer( queue );
		final MessageProducer tOut = sess.createProducer( topic );
		
		MessageConsumer in = sess.createConsumer( queue, "defect IS NOT NULL" );
		in.setMessageListener( new MessageListener() {
			@Override
			public void onMessage( Message message ) {
				try {
					ObjectMessage msg = (ObjectMessage) message;
					
					Computer c = (Computer) msg.getObject();
					
					tOut.send( JMS.toRemovedMessage( sess, c ) );
					
					c = c.tagAsTestedForDefect( id, c.hasDefect() ? TestStatus.YES : TestStatus.NO );

					// simulate work
					Util.sleep( 3000 );

					qOut.send( JMS.toMessage( sess, c ) );
					tOut.send( JMS.toCreatedMessage( sess, c ) );
					sess.commit();
					
					log.info("%s tested a PC for defects", id);
				} catch ( JMSException e ) {
					e.printStackTrace();
					try {
						sess.rollback();
					} catch ( JMSException e1 ) {
						e1.printStackTrace();
					}
				}
			}
		});
		conn.start();
		
		System.out.println("PRESS ENTER TO QUIT");
		Util.waitForNewline();

		JMS.close( conn );
	}
}
