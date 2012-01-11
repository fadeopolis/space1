package tu.space.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import tu.space.components.Component;
import tu.space.components.Computer;

public class JMSWriter {
	public JMSWriter( Session s, String name ) throws JMSException {
		session = s;
		queue   = s.createProducer( s.createQueue( name ) );
		topic   = s.createProducer( s.createTopic( name ) );
	}
	
	public void send( Component c ) throws JMSException {
		queue.send( JMS.toMessage( session, c ) );
		topic.send( JMS.toCreatedMessage( session, c ) );
	}
	public void send( Computer c ) throws JMSException {
		queue.send( JMS.toMessage( session, c ) );
		topic.send( JMS.toCreatedMessage( session, c ) );
	}	
	
	private Session         session;
	private MessageProducer queue;
	private MessageProducer topic;
}
