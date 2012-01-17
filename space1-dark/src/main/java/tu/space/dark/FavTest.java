package tu.space.dark;

import java.util.Enumeration;
import java.util.Random;
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

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;
import tu.space.components.Cpu;
import tu.space.components.Product;
//import tu.space.dark.worker.Manufacturer;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware.Operation;
import tu.space.utils.Logger;

public class FavTest {
	public static final int port = 6666;
	
	public static void main( String[] args ) throws Exception {
		Logger.configure();
		
//		BrokerService bs = startBroker();
		
		doJMSStuff();

//		waitForUserToPressEnter();
		
//		bs.stop();
//		
//		doMiddlewareStuff();
//		printQueue( "Component", "__TYPE__='Cpu'" );
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
//		new Thread("PRODUCER") {
//			public void run() {
//				try {
//					Connection c = JMS.openConnection( "foo", port );
//					final Session    s = JMS.createSession( c );
//				
//					final MessageProducer mp = s.createProducer( s.createQueue( "FOO" ) );
//
//					c.start();
//				
//					while ( true ) {
//						Message m = s.createTextMessage("FOO");
//					
//						mp.send( m );
//						s.commit();
//					
//						System.out.println("SENT " + m.getJMSMessageID() );
//
//						try { Thread.sleep( 2000 ); } catch ( InterruptedException e ) {}
//					}
//				} catch ( JMSException e ) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}.start();
//		
//		new Thread("CONSUMER") {
//			public void run() {
//				try {
//					final Connection c = JMS.openConnection( "bar", port );
//					final Session    s = JMS.createSession( c );
//
//					final MessageConsumer mc = s.createConsumer( s.createQueue( "FOO" ) );
//				
//					c.start();
//				
//					Random r = new Random();
//
//					while ( true ) {
//						Message m = mc.receive();
//					
//						if ( r.nextBoolean() ) {
//							System.out.println( "ACCEPTED: " + m.getJMSMessageID() );
//							s.commit();
//						} else {
//							System.out.println( "REJECTED: " + m.getJMSMessageID() );
//							s.rollback();
//						}
//					}
//				} catch ( JMSException e ) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}.start();
//		
		Connection c = JMS.openConnection( "foo", port );
		final Session    s = JMS.createSession( c );
	
//		final MessageProducer mp = s.createProducer( s.createQueue( "FOO" ) );
//		final MessageProducer mp = s.createProducer( s.createQueue( "BAR" ) );

		final MessageConsumer mc = s.createConsumer( AdvisorySupport.getMessageDeliveredAdvisoryTopic( s.createQueue( "Component" ) ) );
		c.start();

		ActiveMQMessage msg = (ActiveMQMessage) mc.receive();
		TextMessage     obj = (TextMessage) msg.getDataStructure();
//		ObjectMessage   obj = (ObjectMessage) msg.getDataStructure();
		s.commit();

//		print( msg.getContent() );
		print( obj.getStringProperty( "foo" ) );
//		print( obj.getText() );
		
		c.close();
	}
	
	static void doMiddlewareStuff() {
		new JMSMiddleware( "bar", port ).registerListener( Cpu.class, Operation.CREATED, new Listener<Cpu>() {

			@Override
			public void onEvent( Cpu p ) {
				// TODO Auto-generated method stub
				System.out.println("foo");
			}
		} );
//		new Manufacturer( "man1", new JMSMiddleware( "man1", port ) );
	}
	
	static void printQueue( String name, String selector ) throws JMSException {		
		final Connection c = JMS.openConnection( "bar", port );
		final Session    s = JMS.createSession( c );
		
		c.start();

//		MessageConsumer mc = s.createConsumer( s.createQueue( name ) );
//		print( mc.receiveNoWait() );
		
		QueueBrowser qb = s.createBrowser( s.createQueue( name ), selector );
		Marshaller<Product> marsh = Marshaller.ByType;
		
		System.out.println( "Printing: " + name );
		
		int i = 0;
		@SuppressWarnings("unchecked")
		Enumeration<ObjectMessage> e = qb.getEnumeration();
		while ( e.hasMoreElements() ) {
			Message  m = e.nextElement();
			Object   o = marsh.fromMessage( m );
			
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
