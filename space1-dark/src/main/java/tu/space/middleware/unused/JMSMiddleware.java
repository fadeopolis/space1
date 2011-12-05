package tu.space.middleware.unused;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.unused.middleware.Category;
import tu.space.unused.middleware.Middleware;
import tu.space.utils.SpaceException;
import tu.space.utils.UUIDGenerator;

public class JMSMiddleware implements Middleware {
	public UUID generateId() {
		return uuidGen.generate();
	}

	public void beginTransaction() {
		/* NOP */
	}

	public void commit() {
		try {
			session.commit();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	public void rollback() {
		try {
			session.rollback();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}

	public JMSCategory<Cpu>       cpus()                              { return cpus;                             }
	public JMSCategory<Gpu>       gpus()                              { return gpus;                             }
	public JMSCategory<Mainboard> mainboards()                        { return mainboards;                       }
	public JMSCategory<RamModule> ramModules()                        { return ramModules;                       }
	public JMSCategory<Computer>  allComputers()                      { return allComputers;                     }
	public JMSCategory<Computer>  computersUntestedForDefect()        { return computersUntestedForDefect;       }
	public JMSCategory<Computer>  computersUntestedForCompleteness()  { return computersUntestedForCompleteness; }
	public JMSCategory<Computer>  testedComputers()                   { return testedComputers;                  }
	public JMSCategory<Computer>  storage()                           { return storage;                          }
	public JMSCategory<Computer>  trash()                             { return trash;                            }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Iterable<Category<Serializable>> allCategories() {
		List l = Arrays.asList(
			cpus(), gpus(), mainboards(), ramModules(), 
			allComputers(), 
//			computersUntestedForDefect(), computersUntestedForCompleteness(), testedComputers(), 
			storage(), trash()
		);
		
		return l;
	}
	
	public void send( Component c ) {
		     if ( c instanceof Cpu )       cpus.send( (Cpu) c );
		else if ( c instanceof Gpu )       gpus.send( (Gpu) c );
		else if ( c instanceof Mainboard ) mainboards.send( (Mainboard) c );
		else if ( c instanceof RamModule ) ramModules.send( (RamModule) c );
		else throw new SpaceException( "Bad component type " + c.getClass() );
	}
	
	public void start() {
		try {
			connection.start();
		} catch ( JMSException e ) {
			throw new SpaceException( e );
		}
	}
	public void stop() {
		try {
			connection.close();
		} catch ( JMSException e ) {
//			throw new SpaceException( e );
		}
	}
	
	JMSMiddleware( Connection conn, Session session ) {
		this.connection = conn;
		this.session    = session;
		
		this.cpus                             = new JMSCategory<Cpu>( "CPUs", "cpu", session );
		this.gpus                             = new JMSCategory<Gpu>( "GPUs", "gpu", session );
		this.mainboards                       = new JMSCategory<Mainboard>( "Mainboards", "mainboard", session );
		this.ramModules                       = new JMSCategory<RamModule>( "RAM", "ram", session );
		this.allComputers                     = new JMSCategory<Computer>( "All computers", "computer", session );
		this.computersUntestedForDefect       = new JMSCategory<Computer>( "untested-for-defect", "computer", session, TEST_FOR_DEFECT );
		this.computersUntestedForCompleteness = new JMSCategory<Computer>( "untested-for-completeness", "computer", session, TEST_FOR_COMPLETENESS );
		this.testedComputers                  = new JMSCategory<Computer>( "tested-computers", "computer", session, TEST_FOR_DEFECT, TEST_FOR_COMPLETENESS );
		this.storage                          = new JMSCategory<Computer>( "Storage", "storage", session, FINISHED );
		this.trash                            = new JMSCategory<Computer>( "Trash", "trash", session );
	}
	
	final static MessageSelector TEST_FOR_DEFECT       = new MessageSelector( "tested_for_defect",       false );
	final static MessageSelector TEST_FOR_COMPLETENESS = new MessageSelector( "tested_for_completeness", false );
	final static MessageSelector FINISHED              = new MessageSelector( "finished",                true  );
	
	private final Connection connection;
	private final Session    session;
	
	private final JMSCategory<Cpu>       cpus;
	private final JMSCategory<Gpu>       gpus;
	private final JMSCategory<Mainboard> mainboards;
	private final JMSCategory<RamModule> ramModules;
	private final JMSCategory<Computer>  allComputers;
	private final JMSCategory<Computer>  computersUntestedForDefect;
	private final JMSCategory<Computer>  computersUntestedForCompleteness;
	private final JMSCategory<Computer>  testedComputers;
	private final JMSCategory<Computer>  storage;
	private final JMSCategory<Computer>  trash;
	
	private final UUIDGenerator uuidGen = new UUIDGenerator();
}
