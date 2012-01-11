package tu.space.jms;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.DarkServer;
import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;

public abstract class JMS {
	public static final String STR_TESTED_FOR_DEFECT       = "TESTED_FOR_DEFECT";
	public static final String STR_TESTED_FOR_COMPLETENESS = "TESTED_FOR_COMPLETENESS";

	public static Connection openConnection( int port ) throws JMSException {
		return new ActiveMQConnectionFactory( DarkServer.BROKER_URL + port ).createConnection();
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
	
	public static ObjectMessage toMessage( Session sess, Serializable s ) throws JMSException {
		ObjectMessage msgOut = sess.createObjectMessage( s );

		if ( s instanceof Computer ) {
			Computer c = (Computer) s;

			msgOut.setBooleanProperty( STR_TESTED_FOR_DEFECT,       c.defect   != TestStatus.UNTESTED );
			msgOut.setBooleanProperty( STR_TESTED_FOR_COMPLETENESS, c.complete != TestStatus.UNTESTED );
		}
		
		return msgOut;
	}
	
	public static ObjectMessage toCreatedMessage( Session sess, Serializable s ) throws JMSException {
		ObjectMessage msg = toMessage( sess, s );
		
		msg.setBooleanProperty("created", true);
		
		return msg;
	}

	public static ObjectMessage toRemovedMessage( Session sess, Serializable s ) throws JMSException {
		ObjectMessage msg = toMessage( sess, s );
		
		msg.setBooleanProperty("removed", true);
		
		return msg;
	}
	
	private JMS() {/****/}
}
