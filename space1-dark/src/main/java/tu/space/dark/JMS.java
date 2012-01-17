package tu.space.dark;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;

import tu.space.components.Product;
import tu.space.middleware.Middleware.Operation;
import tu.space.utils.SpaceException;

abstract class JMS {
	static Connection openConnection( String clientID, int port ) throws JMSException {
		Connection c = new ActiveMQConnectionFactory( "tcp://localhost:" + port ).createConnection();
		c.setClientID( clientID );
		
		return c;
	}
	
	static Session createSession( Connection c ) throws JMSException {
		Session s = c.createSession( true, Session.AUTO_ACKNOWLEDGE );
		c.start();
		return s;
	}
	
	static Queue getQueue( Session s, Class<? extends Product> c ) {
		return getQueue( s, c.getSimpleName() );
	}
	static Queue getStorage( Session s ) {
		return getQueue( s, "Storage" );
	}
	static Queue getTrash( Session s ) {
		return getQueue( s, "Trash" );
	}
	static Topic getTopic( Session s, Class<? extends Product> c, Operation o ) {
		return getTopic( s, c.getSimpleName(), o );
	}
	static Topic getStorageTopic( Session s, Operation o ) {
		return getTopic( s, "Storage", o );
	}
	static Topic getTrashTopic( Session s, Operation o ) {
		return getTopic( s, "Trash", o );
	}
	
	private static Queue getQueue( Session s, String name ) {
		try {
			return s.createQueue( name );
		} catch ( JMSException e ) {
			throw new SpaceException();
		}
	}
	static Topic getTopic( Session s, String queue, Operation o ) {
		try {
			Queue q = getQueue( s, queue );
		
			switch ( o ) {
				case CREATED: return AdvisorySupport.getMessageDeliveredAdvisoryTopic( q );
				case REMOVED: return AdvisorySupport.getMessageConsumedAdvisoryTopic( q );
				default:      throw new SpaceException();
			}
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	private JMS() {/****/}
}
