package tu.space.light;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Computer;
import tu.space.components.Computer.TestStatus;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;

import static tu.space.util.ContainerCreator.*;

public abstract class Worker implements Runnable {
	public Worker( String[] args ) {
		if ( args.length != 2 ) {
			System.err.println("usage: " + getClass().getSimpleName() + " NAME PORT" );
			System.exit( 1 );
		} else {
			try{
				Integer.parseInt(args[1]);
			} catch (NumberFormatException e){
				System.err.println("usage: " + getClass().getSimpleName() + " NAME PORT, Port is not a number");
			}
		}
		
		this.workerId = args[0];
		this.capi     = new Capi( DefaultMzsCore.newInstance( 0 ) );
		this.space    = ContainerCreator.getSpaceURI( Integer.parseInt( args[1] ) );
	}
	
	public Worker( String name, Capi capi, int spacePort ) {
		this.workerId = name;
		this.capi     = capi;
		this.space    = ContainerCreator.getSpaceURI( spacePort );
	}
	
	public abstract void run();
	
	public void clean() {
		capi.getCore().shutdown( false );
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + workerId;
	}
	
	protected void rollback( TransactionReference tx ) {
		try {
			if ( tx != null ) capi.rollbackTransaction( tx );
		} catch ( MzsCoreException e ) {
			log.error( "Could not roll back transaction: " + e.getMessage() );
//			e.printStackTrace();
		}
	}
	
	public final static void sleep( int millis ) {
		try {
			Thread.sleep( millis );
		} catch ( InterruptedException e ) {
		}
	}
	protected final void sleep() {
		sleep( rand.nextInt( MAX_WORK_SLEEP_TIME ) );
	}
	
	protected void writePc( ContainerReference cref, Computer pc ) throws MzsCoreException {
		List<CoordinationData> cd = new ArrayList<CoordinationData>();
		
		if ( pc.defect == TestStatus.UNTESTED ) {
			cd.add( LABEL_UNTESTED_FOR_DEFECT );
		} else {
			cd.add( LABEL_TESTED_FOR_DEFECT );
		}
		if ( pc.complete == TestStatus.UNTESTED ) {
			cd.add( LABEL_UNTESTED_FOR_COMPLETENESS );
		} else {
			cd.add( LABEL_TESTED_FOR_COMPLETENESS );
		}
		
		capi.write( cref, new Entry( pc, cd ) );	
	}
	
	public    final String workerId;
	protected final Capi   capi;
	protected final URI    space;
	protected final Logger log  = Logger.make( getClass() );
	protected final Random rand = new Random();
	
	private static final int MAX_WORK_SLEEP_TIME = 3000;
}
