package tu.space.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import tu.space.components.Product;
import tu.space.middleware.Input;
import tu.space.utils.AbstractGeneric;
import tu.space.utils.Logger;

public class JMSReader<P extends Product> extends AbstractGeneric<P> implements Input<P> {
	public JMSReader( Class<P> c, Session s ) throws JMSException {
		this( c, s, null );
	}
	public JMSReader( Class<P> c, Session s, String selector ) throws JMSException {
		this( c, s, c.getSimpleName(), selector );
	}
	JMSReader( Class<P> c, Session s, String name, String selector ) throws JMSException {
		super( c );
		
		session = s;
		queue   = s.createConsumer( s.createQueue( name ), selector );
		topic   = s.createProducer( s.createTopic( name ) );
	}
	
	@Override
	public P take() {
		try {
			return readNoWait();
		} catch ( JMSException e ) {
			log.warn( e.toString() );
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public P read() throws JMSException {
		Message m = queue.receive();
		if ( m == null ) return null;
		
		P p = (P) ((ObjectMessage) m).getObject();		
		
		topic.send( JMS.toRemovedMessage( session, p ) );
		
		return p;
	}

	public P readNoWait() throws JMSException {
		Message m = queue.receiveNoWait();
		if ( m == null ) return null;
		
		@SuppressWarnings("unchecked")
		P p = (P) ((ObjectMessage) m).getObject();		
		
		topic.send( JMS.toRemovedMessage( session, p ) );
		
		return p;
	}
	
	private Session         session;
	private MessageConsumer queue;
	private MessageProducer topic;
	private final Logger    log = Logger.make( getClass() );
}
