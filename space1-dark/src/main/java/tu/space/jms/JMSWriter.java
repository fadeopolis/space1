package tu.space.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import tu.space.components.Product;
import tu.space.middleware.Output;
import tu.space.utils.AbstractGeneric;
import tu.space.utils.SpaceException;

public class JMSWriter<P extends Product> extends AbstractGeneric<P> implements Output<P> {
	JMSWriter( Class<P> c, Session s ) throws JMSException {
		this( c, s, c.getSimpleName() );
	}
	JMSWriter( Class<P> c, Session s, String name ) throws JMSException {
		super( c );
		
		session = s;
		queue   = s.createProducer( s.createQueue( name ) );
		topic   = s.createProducer( s.createTopic( name ) );
	}
	
	@Override
	public void write( P p ) {
		try {
			send( p );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
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
