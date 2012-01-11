package tu.space.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import tu.space.components.Product;

public class JMSWriter<P extends Product> {
	JMSWriter( Session s, String name ) throws JMSException {
		session = s;
		queue   = s.createProducer( s.createQueue( name ) );
		topic   = s.createProducer( s.createTopic( name ) );
	}
	
	public void send( P p ) throws JMSException {
		queue.send( JMS.toMessage( session, p ) );
		topic.send( JMS.toCreatedMessage( session, p ) );
	}

	public void sendRemoved( P p ) throws JMSException {
		topic.send( JMS.toRemovedMessage( session, p ) );
	}

	private final Session         session;
	private final MessageProducer queue;
	private final MessageProducer topic;
}
