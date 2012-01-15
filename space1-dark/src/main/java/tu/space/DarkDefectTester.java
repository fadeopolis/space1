package tu.space;

import static tu.space.jms.JMS.STR_TESTED_FOR_DEFECT;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.components.Computer.TestStatus;
import tu.space.jms.JMS;
import tu.space.jms.JMSWriter;
import tu.space.utils.Logger;
import tu.space.utils.Util;

public class DarkDefectTester {
	public static final String USAGE    = "usage: defect-tester ID PORT";
	public static final String SELECTOR = STR_TESTED_FOR_DEFECT + "=false";
	
	public static void main( String... args ) throws JMSException {
		if ( args.length != 2 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id   = args[0];
		final int    port = Integer.parseInt( args[1] );
		
		Logger.configure();
		final Logger log = Logger.make( DarkDefectTester.class );

		final Connection conn = JMS.openConnection( port );
		final Session    sess = conn.createSession( true, Session.AUTO_ACKNOWLEDGE );
	
		final JMSWriter<Computer>  pc  = JMS.getPCWriter( sess );
		final JMSWriter<Cpu>       cpu = JMS.getCPUWriter( sess );
		final JMSWriter<Gpu>       gpu = JMS.getGPUWriter( sess );
		final JMSWriter<Mainboard> mbd = JMS.getMainboardWriter( sess );
		final JMSWriter<RamModule> ram = JMS.getRAMWriter( sess );
		
		MessageConsumer in = sess.createConsumer( sess.createQueue( "computer" ), SELECTOR );
		in.setMessageListener( new MessageListener() {
			@Override
			public void onMessage( Message message ) {
				try {
					ObjectMessage msg = (ObjectMessage) message;
					
					Computer c = (Computer) msg.getObject();
					
					pc.sendRemoved( c );

					// simulate work
					Util.sleep();
					
					if ( c.hasDefect() ) {
						log.info("%s found PC %s to have defects", id, c.id );

						if ( c.cpu      != null  && !c.cpu.hasDefect       )   cpu.send( c.cpu );
						if ( c.gpu      != null  && !c.gpu.hasDefect       )   gpu.send( c.gpu );
						if ( c.mainboard != null && !c.mainboard.hasDefect )   mbd.send( c.mainboard );
						for ( RamModule r : c.ram ) if ( !r.hasDefect ) ram.send( r );
					} else {
						log.info("%s found PC %s to be OK", id, c.id );

						c = c.tagAsTestedForDefect( id, TestStatus.NO );

						pc.send( c );
					}
					sess.commit();
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
