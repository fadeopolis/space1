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
import tu.space.utils.Logger;
import tu.space.utils.Util;

public class DarkLogistician {
	public static final String USAGE    = "usage: logistician ID";
	public static final String SELECTOR = "(defect IS NULL) AND (complete IS NULL)";
	
	public static void main( String... args ) throws JMSException {
		if ( args.length != 1 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id = args[0];
		
		Logger.configure();
		final Logger log = Logger.make( DarkLogistician.class );

		final Connection conn = new ActiveMQConnectionFactory( DarkServer.BROKER_URL ).createConnection();
		final Session    sess = conn.createSession( true, Session.SESSION_TRANSACTED );
	
		final Queue storageQ = sess.createQueue( "storage" );
		final Topic storageT = sess.createTopic( "storage" );
		
		final MessageProducer storageQOut = sess.createProducer( storageQ );
		final MessageProducer storageTOut = sess.createProducer( storageT );

		final Queue trashQ = sess.createQueue( "trash" );
		final Topic trashT = sess.createTopic( "trash" );
		
		final MessageProducer trashQOut = sess.createProducer( trashQ );
		final MessageProducer trashTOut = sess.createProducer( trashT );
		
		final Queue computerQ = sess.createQueue( "computer" );
		final Topic computerT = sess.createTopic( "computer" );

		final MessageProducer computerTOut = sess.createProducer( computerT );
		
		MessageConsumer in = sess.createConsumer( 
		                            computerQ, 
		                            "(defect IS NULL) AND (complete IS NULL)"
		                     );
		in.setMessageListener( new MessageListener() {
			@Override
			public void onMessage( Message message ) {
				try {
					ObjectMessage msg = (ObjectMessage) message;
					
					Computer c = (Computer) msg.getObject();
					
					computerTOut.send( JMS.toRemovedMessage( sess, c ) );
					
					c = c.tagAsFinished( id );

					// simulate work
					Util.sleep( 3000 );
					
					if ( c.hasDefect() || !c.isComplete() ) {
						trashQOut.send( JMS.toMessage( sess, c ) );
						trashTOut.send( JMS.toCreatedMessage( sess, c ) );
						sess.commit();
						log.info("%s trashed a PC", id);
					} else {
						storageQOut.send( JMS.toMessage( sess, c ) );
						storageTOut.send( JMS.toCreatedMessage( sess, c ) );
						sess.commit();
						log.info("%s stored a PC", id);
					}
				} catch ( JMSException e ) {
					JMS.rollback( sess );
					e.printStackTrace();
				}
			}
		});
		conn.start();
		
		System.out.println("PRESS ENTER TO QUIT");
		Util.waitForNewline();
		
		JMS.close( conn );
	}
}
