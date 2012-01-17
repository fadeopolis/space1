package tu.space.dark;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import tu.space.components.Product;
import tu.space.middleware.Output;
import tu.space.utils.AbstractGeneric;
import tu.space.utils.SpaceException;

public class JMSOutput<P extends Product> extends AbstractGeneric<P> implements Output<P> {
	JMSOutput( Class<P> c, Session s ) {
		this( c, s, JMS.getQueue( s, c ) );
	}
	JMSOutput( Class<P> c, Session s, Queue queue ) {
		super( c );

		try {
			this.session    = s;
			this.queue      = s.createProducer( queue );
			this.marshaller = Marshaller.forType( c );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	@Override
	public void write( P p ) {
		try {
			queue.send( marshaller.toMessage( session, p ) );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	private final Session         session;
	private final MessageProducer queue;
	private final Marshaller<P>   marshaller;
}
