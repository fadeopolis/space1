package tu.space.dark;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import tu.space.components.Product;
import tu.space.utils.SpaceException;

class JMSQueues {
	public JMSQueues( Session s ) {
		this.s = s;
	}
	
	public MessageConsumer getStorageTopic( String selector ) {
		return getTopicIn( "Storage", selector );
	}
	public MessageConsumer getTrashTopic( String selector ) {
		return getTopicIn( "Trash", selector );
	}	
	
	public <P extends Product> MessageConsumer getTopicIn( Class<P> c, String selector ) {
		return getTopicIn( c.getSimpleName(), selector );
	}

	//***** PRIVATE
	
	private MessageConsumer getTopicIn( String name, String selector ) {
		return getConsumer( getTopic( name ), selector );
	}

	private MessageConsumer getConsumer( Destination d, String selector ) {
		try {
			return s.createConsumer( d, selector );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	private Topic getTopic( String name ) {
		try {
			return s.createTopic( name );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	private final Session s;

}
