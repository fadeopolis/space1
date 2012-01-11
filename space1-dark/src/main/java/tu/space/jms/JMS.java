package tu.space.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.DarkServer;
import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamModule;

public abstract class JMS {
	public static final String STR_TESTED_FOR_DEFECT       = "TESTED_FOR_DEFECT";
	public static final String STR_TESTED_FOR_COMPLETENESS = "TESTED_FOR_COMPLETENESS";

	public static Connection openConnection( int port ) throws JMSException {
		return new ActiveMQConnectionFactory( DarkServer.BROKER_URL + port ).createConnection();
	}
	
	public static Session createSession( Connection c ) throws JMSException {
		return c.createSession( true, Session.SESSION_TRANSACTED );
	}
	public static Session createSessionWithoutTransactions( Connection c ) throws JMSException {
		return c.createSession( false, Session.AUTO_ACKNOWLEDGE );
	}
	
	public static JMSWriter<Computer>  getPCWriter( Session s )        throws JMSException { return getWriter( s, Computer.class ); }
	public static JMSWriter<Cpu>       getCPUWriter( Session s )       throws JMSException { return getWriter( s, Cpu.class ); }
	public static JMSWriter<Gpu>       getGPUWriter( Session s )       throws JMSException { return getWriter( s, Gpu.class ); }
	public static JMSWriter<Mainboard> getMainboardWriter( Session s ) throws JMSException { return getWriter( s, Mainboard.class ); }
	public static JMSWriter<RamModule> getRAMWriter( Session s )       throws JMSException { return getWriter( s, RamModule.class ); }
	public static <P extends Product> JMSWriter<P> getWriter( Session s, Class<P> c ) throws JMSException {
		return new JMSWriter<P>( s, c.getSimpleName() );
	}
	public static JMSWriter<Computer>  getStorageWriter( Session s ) throws JMSException {
		return new JMSWriter<Computer>( s, "storage" );
	}
	public static JMSWriter<Computer>  getTrashWriter( Session s ) throws JMSException {
		return new JMSWriter<Computer>( s, "trash" );
	}
	
	public static JMSReader<Computer>  getPCReader( Session s )        throws JMSException { return getReader( s, Computer.class ); }
	public static JMSReader<Cpu>       getCPUReader( Session s )       throws JMSException { return getReader( s, Cpu.class ); }
	public static JMSReader<Gpu>       getGPUReader( Session s )       throws JMSException { return getReader( s, Gpu.class ); }
	public static JMSReader<Mainboard> getMainboardReader( Session s ) throws JMSException { return getReader( s, Mainboard.class ); }
	public static JMSReader<RamModule> getRAMReader( Session s )       throws JMSException { return getReader( s, RamModule.class ); }
	public static <P extends Product> JMSReader<P> getReader( Session s, Class<P> c ) throws JMSException {
		return new JMSReader<P>( c, s, c.getSimpleName() );
	}

	public static void rollback( Session s ) {
		try {
			s.rollback();
		} catch ( JMSException e ) {
		}
	}
	public static void close( Connection c ) {
		try {
			c.close();
		} catch ( JMSException e ) {
		}
	}
	
	public static ObjectMessage toMessage( Session sess, Product p ) throws JMSException {
		ObjectMessage msgOut = sess.createObjectMessage( p );

		if ( p instanceof Computer ) {
			Computer c = (Computer) p;

			msgOut.setBooleanProperty( STR_TESTED_FOR_DEFECT,       c.defect   != TestStatus.UNTESTED );
			msgOut.setBooleanProperty( STR_TESTED_FOR_COMPLETENESS, c.complete != TestStatus.UNTESTED );
		}
		
		return msgOut;
	}
	
	public static ObjectMessage toCreatedMessage( Session sess, Product p ) throws JMSException {
		ObjectMessage msg = toMessage( sess, p );
		
		msg.setBooleanProperty("created", true);
		
		return msg;
	}

	public static ObjectMessage toRemovedMessage( Session sess, Product p ) throws JMSException {
		ObjectMessage msg = toMessage( sess, p );
		
		msg.setBooleanProperty("removed", true);
		
		return msg;
	}
	
	private JMS() {/****/}
}
