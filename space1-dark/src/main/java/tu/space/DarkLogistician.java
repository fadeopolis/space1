package tu.space;

import static tu.space.jms.JMS.*;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import tu.space.components.Computer;
import tu.space.jms.JMS;
import tu.space.jms.JMSReader;
import tu.space.jms.JMSWriter;
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
	
		final JMSReader<Computer> computer = JMS.getPCReader( sess );
		final JMSWriter<Computer> storage  = JMS.getStorageWriter( sess );
		final JMSWriter<Computer> trash    = JMS.getTrashWriter( sess );
		
		final Queue computerQ = sess.createQueue( "computer" );

		MessageConsumer in = sess.createConsumer( computerQ, SELECTOR );
		in.setMessageListener( new MessageListener() {
			@Override
			public void onMessage( Message message ) {
				try {
					Computer c = computer.read();
					
					c = c.tagAsFinished( id );

					// simulate work
					Util.sleep( 3000 );
					
					if ( c.hasDefect() || !c.isComplete() ) {
						trash.send( c );
						log.info("%s trashed PC %s", id, c.id);
					} else {
						storage.send( c );
						log.info("%s stored  PC %s", id, c.id);
					}
					sess.commit();
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
