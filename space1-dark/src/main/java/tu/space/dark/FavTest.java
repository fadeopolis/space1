package tu.space.dark;

import java.util.Enumeration;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.broker.BrokerService;

import tu.space.components.Cpu;
import tu.space.jms.JMS;
import tu.space.utils.Logger;
import tu.space.worker.Manufacturer;

public class FavTest {
	static final int port = 6666;
	
	public static void main( String[] args ) throws Exception {
		Logger.configure();
		
//		BrokerService bs = startBroker();
//		
//		waitForUserToPressEnter();
//		
//		bs.stop();
//		
//		doMiddlewareStuff();
//		doJMSStuff();
		printQueue( "PcSpec", "orderId = '40fdae1f'" );
	}
	
	static BrokerService startBroker() throws Exception {
		BrokerService broker = new BrokerService();
		broker.setBrokerName( "dark-server" );
		broker.addConnector( "tcp://localhost:" + port );
		broker.setDeleteAllMessagesOnStartup( true );
		
		broker.start();
		
		return broker;
	}
	
	static void doJMSStuff() throws Exception {
		final Connection c = JMS.openConnection( port );
		final Session    s = JMS.createSession( c );
		
		c.start();

		final MessageProducer mpT = s.createProducer( s.createTopic( "Mainboard" ) );

		Message m = s.createObjectMessage( new Cpu( "62465ce3", "", false, null ) );
		m.setBooleanProperty( "REMOVED", true );
		
		mpT.send( m );
		s.commit();
		
		c.close();
	}
	
	static void doMiddlewareStuff() {
		new Manufacturer( "man1", new JMSMiddleware( port ) );
	}
	
	static void printQueue( String name, String selector ) throws JMSException {		
		final Connection c = JMS.openConnection( port );
		final Session    s = JMS.createSession( c );
		
		c.start();

		QueueBrowser qb = s.createBrowser( s.createQueue( name ), selector );
		
		System.out.println( "Printing: " + name );
		
		int i = 0;
		@SuppressWarnings("unchecked")
		Enumeration<ObjectMessage> e = qb.getEnumeration();
		while ( e.hasMoreElements() ) {
			ObjectMessage m = e.nextElement();
			Object        o = m.getObject();
			
			System.out.println( o.getClass().getSimpleName() + " " + o );
			@SuppressWarnings("unchecked")
			Enumeration<String> pns = m.getPropertyNames();
			while ( pns.hasMoreElements() ) {
				String prop = pns.nextElement();
				
				System.out.println( "\t'" + prop + "'\t\t: '" + m.getObjectProperty( prop ) + "'" );
			}
			i++;
		}
		qb.close();
		
		c.stop();
		c.close();
		
		System.out.println( "DONE, " + i + " elements" );
	}

	static void waitForUserToPressEnter() {
		System.out.println( "PRESS ENTER TO CONTINUE" );
		new Scanner( System.in ).hasNextLine();
	}
}
