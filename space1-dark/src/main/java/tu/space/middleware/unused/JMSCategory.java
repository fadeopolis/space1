package tu.space.middleware.unused;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.unused.middleware.Category;
import tu.space.unused.middleware.Listener;
import tu.space.utils.SpaceException;
import tu.space.utils.Util;

public class JMSCategory<E extends Serializable> implements Category<E> {
	public Iterable<E> browse() {
		return new Iterable<E>() {
			@Override
			public Iterator<E> iterator() {
				final Enumeration<ObjectMessage> e = getBrowser();

				return new Iterator<E>() {

					@Override
					public boolean hasNext() {
						return e.hasMoreElements();
					}

					@SuppressWarnings("unchecked")
					@Override
					public E next() {
						try {
							return (E) e.nextElement().getObject();
						} catch ( JMSException e ) {
							throw new SpaceException( e );
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public void setConsumingListener( final Listener<E> l ) {
		try {
			queueIn.setMessageListener( new MessageListenerAdapter<E>( l ) );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	public void setListener( MessageListener jms ) {
		try {
			queueIn.setMessageListener( jms );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	public void setNonConsumingListener( Listener<E> l ) {
		try {
			topicIn.setMessageListener( new MessageListenerAdapter<E>( l ) );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	@SuppressWarnings("unchecked")
	public E receive() {
		try {
			Message msg = queueIn.receive();
			if ( msg == null )
				return null;

			return (E) ((ObjectMessage) msg).getObject();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	public void send( E e ) {
		try {
			queueOut.send( toMessage( e ) );
			topicOut.send( toMessage( e ) );
			session.commit();
		} catch ( JMSException e1 ) {
			throw new SpaceException( e1 );
		}
	}

	public String name() {
		return name;
	}
	
	JMSCategory( String name, String resourceName, Session session, MessageSelector... selectors ) {
		try {
			this.name        = name;
			this.selectorStr = Util.join( " AND ", selectors );
			this.session     = session;
			
			this.queue = session.createQueue( resourceName );
			this.topic = session.createTopic( resourceName );

			this.queueOut = session.createProducer( queue );
			this.topicOut = session.createProducer( topic );

			this.queueIn = session.createConsumer( queue, selectorStr );
			this.topicIn = session.createConsumer( topic, selectorStr );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	@SuppressWarnings("unchecked")
	private Enumeration<ObjectMessage> getBrowser() {
		try {
			return session.createBrowser( queue ).getEnumeration();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	private ObjectMessage toMessage( Serializable s ) throws JMSException {
		ObjectMessage msg = session.createObjectMessage( s );
		
		// i'm too lazy to find a better solution
		if ( s instanceof Computer ) {
			Computer c = (Computer) s;
			
			msg.setBooleanProperty( JMSMiddleware.TEST_FOR_DEFECT.key,       c.defect   != TestStatus.UNTESTED );
			msg.setBooleanProperty( JMSMiddleware.TEST_FOR_COMPLETENESS.key, c.complete != TestStatus.UNTESTED );
			msg.setBooleanProperty( JMSMiddleware.FINISHED.key,              c.finished );
		}
		
		return msg;
	}

	public String toString() {
		return getClass().getSimpleName() + "{" + name + ", selector='" + selectorStr + "'}";
	}
	
	private final String  name;
	private final String  selectorStr;
	private final Session session;

	private final Queue			queue;
	private final Topic			topic;

	protected MessageProducer	queueOut;
	protected MessageProducer	topicOut;

	protected MessageConsumer	queueIn;
	protected MessageConsumer	topicIn;

	static class MessageListenerAdapter<E> implements MessageListener {
		public MessageListenerAdapter( Listener<E> l ) {
			this.l = l;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void onMessage( Message message ) {
			try {
				l.handle( (E) ((ObjectMessage) message).getObject() );
			} catch ( JMSException e ) {
				e.printStackTrace();
			}
		}

		private final Listener<E>	l;
	}
}