package tu.space.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public abstract class JMSTopicListener {
	public JMSTopicListener( Session s, String topicName ) throws JMSException {
		this( s, topicName, null );
	}
	public JMSTopicListener( Session s, String topicName, String selector ) throws JMSException {
		MessageConsumer in  = s.createConsumer( s.createTopic( topicName ), selector );
		
		in.setMessageListener( new MessageListener() {
			@Override
			public void onMessage( Message message ) {
				try {
					Object o = ((ObjectMessage) message).getObject();
					
					if ( message.getBooleanProperty( "created" ) ) 
						onCreated( o, message );
					if ( message.getBooleanProperty( "removed" ) ) 
						onRemoved( o, message );						
				} catch ( JMSException e ) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public abstract void onCreated( Object o, Message msg );
	public abstract void onRemoved( Object o, Message msg );
}
