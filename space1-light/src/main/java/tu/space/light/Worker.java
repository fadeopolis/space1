package tu.space.light;

import java.net.URI;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;

public abstract class Worker implements Runnable {
	public Worker( String[] args ) {
		if ( args.length != 2 ) {
			System.err.println("usage: " + getClass().getSimpleName() + " NAME PORT" );
			System.exit( 1 );
		}
		
		this.workerId = args[0];
		this.capi     = new Capi( DefaultMzsCore.newInstance( 0 ) );
		this.space    = ContainerCreator.getSpaceURI( Integer.parseInt( args[1] ) );
		this.log      = Logger.make( getClass() );
	}
	
	public Worker( String name, Capi capi, int spacePort ) {
		this.workerId = name;
		this.capi     = capi;
		this.space    = ContainerCreator.getSpaceURI( spacePort );
		this.log      = Logger.make( getClass() );
	}
	
	public abstract void run();
	
	public void clean() {
		capi.getCore().shutdown( false );
	}
	
	protected void rollback( TransactionReference tx ) {
		try {
			if ( tx != null ) capi.rollbackTransaction( tx );
		} catch ( MzsCoreException e ) {
			log.error( "Could not roll back transaction" );
			e.printStackTrace();
		}
	}
	
	public    final String workerId;
	protected final Capi   capi;
	protected final URI    space;
	protected final Logger log;
}
