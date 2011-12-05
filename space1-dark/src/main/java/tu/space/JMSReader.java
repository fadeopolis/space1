package tu.space;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import tu.space.components.Component;
import tu.space.components.Computer;

public class JMSReader {
	public JMSReader( Session s, String name ) throws JMSException {
		session = s;
		queue   = s.createConsumer( s.createQueue( name ) );
		topic   = s.createProducer( s.createTopic( name ) );
	}
	
	public Computer  readComputer() throws JMSException {
		Message m = queue.receive();
		if ( m == null ) return null;
		
		ObjectMessage msg = (ObjectMessage) m;
		Computer c = (Computer) msg.getObject();		
		
		topic.send( JMS.toRemovedMessage( session, c ) );
		
		return c;
	}
	public Component readComponent() throws JMSException {
		Message m = queue.receive();
		if ( m == null ) return null;
		
		ObjectMessage msg = (ObjectMessage) m;
		Component c = (Component) msg.getObject();		
		
		topic.send( JMS.toRemovedMessage( session, c ) );
		
		return c;
	}

	private Session         session;
	private MessageConsumer queue;
	private MessageProducer topic;
}
