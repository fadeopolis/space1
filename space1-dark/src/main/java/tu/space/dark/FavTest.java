package tu.space.dark;

import java.util.Enumeration;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.AdvisoryConsumer;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import tu.space.components.Cpu;
import tu.space.jms.JMS;
import tu.space.utils.Logger;
import tu.space.worker.Manufacturer;

public class FavTest {
	static final int port = 6666;
	
	public static void main( String[] args ) throws Exception {

		Logger.configure();
		
		BrokerService bs = startBroker();
		
		doJMSStuff();

//		waitForUserToPressEnter();
		
		bs.stop();
//		
//		doMiddlewareStuff();
//		printQueue( "PcSpec", "orderId = '3181c6d0'" );
	}
	
	static BrokerService startBroker() throws Exception {
		BrokerService broker = new BrokerService();
		broker.setBrokerName( "dark-server" );
		broker.addConnector( "tcp://localhost:" + port );
		broker.setDeleteAllMessagesOnStartup( true );
			
		PolicyMap   pm = new PolicyMap();
		PolicyEntry pe = new PolicyEntry();
		pe.setAdvisoryForConsumed( true );
		pe.setAdvisoryForDelivery( true );
//		pm.put( AdvisorySupport.getMessageDeliveredAdvisoryTopic( new ActiveMQQueue( "Cpu" ) ), pe );
		pm.put( new ActiveMQQueue(">"), pe );
		broker.setDestinationPolicy( pm );
//		broker.getDestinationPolicy();
		broker.start();
		
		return broker;
	}
	
	static void doJMSStuff() throws Exception {
		final Connection c = JMS.openConnection( port );
		final Session    s = JMS.createSession( c );
		
		c.start();

		
		final MessageProducer mp = s.createProducer( s.createQueue( "Cpu" ) );
		final MessageConsumer mc = s.createConsumer( AdvisorySupport.getProducerAdvisoryTopic( new ActiveMQQueue( "Cpu" ) ) );
		final MessageConsumer mc2 = s.createConsumer( AdvisorySupport.getMessageDeliveredAdvisoryTopic( new ActiveMQQueue( "Cpu" ) ) );

		Message m = s.createTextMessage( "foo" );
//		Message m = s.createObjectMessage( new Cpu( "62465ce3", "", false, null ) );
		m.setBooleanProperty( "REMOVED", true );
		
		mp.send( m );
		s.commit();
		
		ActiveMQMessage msg = (ActiveMQMessage) mc2.receive();
		TextMessage     obj = (TextMessage) msg.getDataStructure();
//		ObjectMessage   obj = (ObjectMessage) msg.getDataStructure();
		s.commit();

		print( msg.getContent() );
		print( obj.getObjectProperty( "REMOVED" ) );
		print( obj.getText() );
		
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

	static void print( Object o ) {
		System.err.println( o );
	}
}
