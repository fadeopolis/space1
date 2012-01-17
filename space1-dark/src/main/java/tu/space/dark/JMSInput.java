package tu.space.dark;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import tu.space.components.Product;
import tu.space.middleware.Input;
import tu.space.utils.AbstractGeneric;
import tu.space.utils.Logger;

public class JMSInput<P extends Product> extends AbstractGeneric<P> implements Input<P> {
	JMSInput( Class<P> c, Session s, Queue queue, String selector ) throws JMSException {
		super( c );
		
		this.queue      = s.createConsumer( queue, selector );
		this.marshaller = Marshaller.forType( c );
	}
	
	@Override
	public P take() {
		try {
			Message m = queue.receiveNoWait();
			
			if ( m == null ) return null;
			
			return marshaller.fromMessage( m );
		} catch ( JMSException e ) {
			log.warn( e.toString() );
			return null;
		}
	}
	
	private final MessageConsumer queue;
	private final Marshaller<P>   marshaller;
	private final Logger    log = Logger.make( getClass() );
}
