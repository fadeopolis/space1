package tu.space;

import static tu.space.jms.JMS.*;

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

import tu.space.components.Computer;
import tu.space.jms.JMS;
import tu.space.utils.Logger;
import tu.space.utils.Util;

public class DarkLogistician {
	public static final String USAGE    = "usage: logistician ID PORT";
	public static final String SELECTOR = String.format(
			"(%s = true) AND (%s = true)",
			STR_TESTED_FOR_COMPLETENESS, 
			STR_TESTED_FOR_DEFECT 
	);

	public static void main( String... args ) throws JMSException {
		if ( args.length != 2 ) {
			System.err.println( USAGE );
			System.exit( 2 );
		}
		final String id   = args[0];
		final int    port = Integer.parseInt( args[1] );

		Logger.configure();
		final Logger log = Logger.make( DarkLogistician.class );

		final Connection conn = JMS.openConnection( port );
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
		
		MessageConsumer in = sess.createConsumer( computerQ, SELECTOR );
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
