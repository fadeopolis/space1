package tu.space;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;

public abstract class JMS {
	public static Connection openConnection() throws JMSException {
		return new ActiveMQConnectionFactory( DarkServer.BROKER_URL ).createConnection();
	}
	
	public static Session createSession( Connection c ) throws JMSException {
		return c.createSession( true, Session.SESSION_TRANSACTED );
	}
	public static Session createSessionWithoutTransactions( Connection c ) throws JMSException {
		return c.createSession( false, Session.AUTO_ACKNOWLEDGE );
	}
	
	public static void rollback( Session s ) {
		try {
			s.rollback();
		} catch ( JMSException e ) {
		}
	}
	public static void close( Connection c ) {
		try {
			c.close();
		} catch ( JMSException e ) {
		}
	}
	
	public static ObjectMessage toMessage( Session sess, Computer c ) throws JMSException {
		ObjectMessage msgOut = sess.createObjectMessage( c );
		
		if ( c.defect   == TestStatus.UNTESTED ) msgOut.setStringProperty( "defect",   "FOO" );
		if ( c.complete == TestStatus.UNTESTED ) msgOut.setStringProperty( "complete", "FOO" );
		msgOut.setBooleanProperty( "finished", c.finished );
		
		return msgOut;
	}
	
	public static ObjectMessage toMessage( Session sess, Component c ) throws JMSException {
		ObjectMessage msgOut = sess.createObjectMessage( c );
		
		return msgOut;
	}
	
	public static ObjectMessage toCreatedMessage( Session sess, Computer c ) throws JMSException {
		ObjectMessage msg = toMessage( sess, c );
		
		msg.setBooleanProperty("created", true);
		
		return msg;
	}
	public static ObjectMessage toCreatedMessage( Session sess, Component c ) throws JMSException {
		ObjectMessage msg = toMessage( sess, c );
		
		msg.setBooleanProperty("created", true);
		
		return msg;
	}

	public static ObjectMessage toRemovedMessage( Session sess, Computer c ) throws JMSException {
		ObjectMessage msg = toMessage( sess, c );		
		
		msg.setBooleanProperty("removed", true);
		
		return msg;
	}
	public static ObjectMessage toRemovedMessage( Session sess, Component c ) throws JMSException {
		ObjectMessage msg = toMessage( sess, c );
		
		msg.setBooleanProperty("removed", true);
		
		return msg;
	}
	
	private JMS() {/****/}
}