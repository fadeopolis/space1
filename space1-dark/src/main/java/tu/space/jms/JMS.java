package tu.space.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.contracts.PcSpec;
import tu.space.utils.SpaceException;

public abstract class JMS {
	public static final String STR_TESTED_FOR_DEFECT       = "TESTED_FOR_DEFECT";
	public static final String STR_TESTED_FOR_COMPLETENESS = "TESTED_FOR_COMPLETENESS";

	public static Connection openConnection( int port ) throws JMSException {
		return new ActiveMQConnectionFactory( "tcp://localhost:" + port ).createConnection();
	}
	
	public static Session createSession( Connection c ) throws JMSException {
		return c.createSession( true, Session.AUTO_ACKNOWLEDGE );
	}
	public static Session createSessionWithoutTransactions( Connection c ) throws JMSException {
		return c.createSession( false, Session.AUTO_ACKNOWLEDGE );
	}
	
	public static JMSWriter<Computer>  getPCWriter( Session s )        { return getWriter( s, Computer.class ); }
	public static JMSWriter<Cpu>       getCPUWriter( Session s )       { return getWriter( s, Cpu.class ); }
	public static JMSWriter<Gpu>       getGPUWriter( Session s )       { return getWriter( s, Gpu.class ); }
	public static JMSWriter<Mainboard> getMainboardWriter( Session s ) { return getWriter( s, Mainboard.class ); }
	public static JMSWriter<RamModule> getRAMWriter( Session s )       { return getWriter( s, RamModule.class ); }
	public static <P extends Product> JMSWriter<P> getWriter( Session s, Class<P> c ) {
		try {
			return new JMSWriter<P>( c, s );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	public static JMSWriter<Computer>  getStorageWriter( Session s ) {
		try {
			return new JMSWriter<Computer>( Computer.class, s, "Storage" );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	public static JMSWriter<Product>  getTrashWriter( Session s ) {
		try {
			return new JMSWriter<Product>( Product.class, s, "Trash" );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	public static JMSReader<Computer>  getPCUntestedForDefectReader( Session s ) {
		try {
			return new JMSReader<Computer>( Computer.class, s, UNTESTED_FOR_DEFECT_SELECTOR );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	public static JMSReader<Computer>  getPCUntestedForCompletenessReader( Session s ) {
		try {
			return new JMSReader<Computer>( Computer.class, s, UNTESTED_FOR_COMPLETENESS_SELECTOR );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	public static JMSReader<Computer>  getTestedPcReader( Session s ) {
		try {
			return new JMSReader<Computer>( Computer.class, s, TESTED_FOR_COMPLETENESS_SELECTOR + " AND " + TESTED_FOR_DEFECT_SELECTOR );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	
	public static JMSReader<Computer>  getPCReader( Session s )        { return getReader( s, Computer.class ); }
	public static JMSReader<Cpu>       getCPUReader( Session s )       { return getReader( s, Cpu.class ); }
	public static JMSReader<Gpu>       getGPUReader( Session s )       { return getReader( s, Gpu.class ); }
	public static JMSReader<Mainboard> getMainboardReader( Session s ) { return getReader( s, Mainboard.class ); }
	public static JMSReader<RamModule> getRAMReader( Session s )       { return getReader( s, RamModule.class ); }
	public static <P extends Product> JMSReader<P> getReader( Session s, Class<P> c ) {
		try {
			return new JMSReader<P>( c, s );
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
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
			
			if ( c.orderId != null ) msgOut.setStringProperty( "orderId", c.orderId );
		} else if ( p instanceof PcSpec ) {
			PcSpec ps = (PcSpec) p;
			
			if ( ps.orderId != null ) msgOut.setStringProperty( "orderId", ps.orderId );
		} else if ( p instanceof Order ) {
			Order o = (Order) p;
			
			if ( o.id != null ) msgOut.setStringProperty( "orderId", o.id );
		} else if ( p instanceof Cpu ) {
			Cpu c = (Cpu) p;
			
			msgOut.setStringProperty( "CPU", c.type.name() );
		}
		
		return msgOut;
	}
	
	public static ObjectMessage toCreatedMessage( Session sess, Product p ) throws JMSException {
		ObjectMessage msg = toMessage( sess, p );
		
		msg.setBooleanProperty("CREATED", true);
		
		return msg;
	}

	public static ObjectMessage toRemovedMessage( Session sess, Product p ) throws JMSException {
		ObjectMessage msg = toMessage( sess, p );
		
		msg.setBooleanProperty("REMOVED", true);
		
		return msg;
	}
	
	private JMS() {/****/}
	
	public static final String CREATED_SELECTOR = "(CREATED = true)";
	public static final String REMOVED_SELECTOR = "(REMOVED = true)";

	public static final String IS_IN_ORDER_SELECTOR = "(orderId IS NOT NULL)";
	
	public static final String UNTESTED_FOR_DEFECT_SELECTOR       = "(TESTED_FOR_DEFECT        = false)";
	public static final String UNTESTED_FOR_COMPLETENESS_SELECTOR = "(TESTED_FOR_COMPLETENESS  = false)";
	public static final String TESTED_FOR_DEFECT_SELECTOR         = "(TESTED_FOR_DEFECT        = true)";
	public static final String TESTED_FOR_COMPLETENESS_SELECTOR   = "(TESTED_FOR_COMPLETENESS  = true)";
}
