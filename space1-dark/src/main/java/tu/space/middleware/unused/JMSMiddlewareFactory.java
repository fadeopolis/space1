package tu.space.middleware.unused;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.DarkServer;
import tu.space.utils.SpaceException;

public class JMSMiddlewareFactory {
	public JMSMiddlewareFactory() {
		factory = new ActiveMQConnectionFactory( DarkServer.BROKER_URL );
	}
	
	public JMSMiddleware make() {		
		try {
			Connection c = factory.createConnection();
			Session    s = c.createSession( true, Session.SESSION_TRANSACTED );
			
			c.start();
			
			return new JMSMiddleware( c, s );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	private final ConnectionFactory factory;
}
