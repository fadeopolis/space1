package tu.space.light;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import ch.qos.logback.classic.Level;

import tu.space.components.Component;
import tu.space.util.ContainerCreator;
import tu.space.util.LogBack;
import tu.space.utils.Logger;

import static tu.space.util.ContainerCreator.*;

public class LoadBalancer implements Runnable {
	public static void main( String[] args ) throws MzsCoreException, InterruptedException {
		Logger.configure();
		LogBack.configure( Level.ERROR );
		
		if ( args.length < 2 ) {
			System.err.println( "usage: LoadBalancer ID PORTS..." );
			System.exit( 1 );
		}
		
		String name = args[0];
		int[] spaces = new int[args.length-1];
		for ( int i = 1; i < args.length; i++ ) spaces[i-1] = Integer.parseInt( args[i] );  
		
		new LoadBalancer( name, spaces ).run();
	}
	
	public LoadBalancer( String name, int... ports ) throws MzsCoreException, InterruptedException {
		MzsCore core = DefaultMzsCore.newInstance( 0 );
			
		this.name   = name;
		this.capi   = new Capi( core );

		List<Space> sp = new ArrayList<LoadBalancer.Space>();
		for ( int port : ports ) {
			sp.add( new Space( ContainerCreator.getSpaceURI( port ) ) );
		}
		
		this.spaces = Collections.unmodifiableList( sp );
		
	}
	
	@Override
	public void run() {
		log.info( "Load Balancer %s started", name );

		try {
			while (true) {
				Worker.sleep( 200 );
			}
		} finally {
			clean();
		}
		
//		log.info( "Load Balancer %s stopped", name );
	}
	
	private void clean() {
		for ( Space s : spaces ) s.clean();
		
		capi.getCore().shutdown( true );
	}
	
	private final String      name;
	private final Capi        capi;
	private final List<Space> spaces;
	private final Logger      log  = Logger.make( getClass() );
	
	private final class Space {
		public Space( URI uri ) throws MzsCoreException, InterruptedException {
			this.nm  = new NotificationManager( capi.getCore() );
			
			ContainerReference cpus = ContainerCreator.getCpuContainer( uri, capi );
			ContainerReference gpus = ContainerCreator.getGpuContainer( uri, capi );
			ContainerReference mbds = ContainerCreator.getMainboardContainer( uri, capi );
			ContainerReference rams = ContainerCreator.getRamContainer( uri, capi );

			// count whats here at startup
			numCPUs = new AtomicInteger( capi.test( cpus, ANY_MAX,  MzsConstants.RequestTimeout.DEFAULT, null ) );
			numGPUs = new AtomicInteger( capi.test( gpus, ANY_MAX,  MzsConstants.RequestTimeout.DEFAULT, null ) );
			numMBDs = new AtomicInteger( capi.test( mbds, FIFO_MAX, MzsConstants.RequestTimeout.DEFAULT, null ) );
			numRAMs = new AtomicInteger( capi.test( rams, ANY_MAX,  MzsConstants.RequestTimeout.DEFAULT, null ) );
			
			// install listeners
			nots[0] = nm.createNotification( ContainerCreator.getCpuContainer( uri, capi ), new Counter( numCPUs ), Operation.TAKE, Operation.DELETE, Operation.WRITE );
			nots[1] = nm.createNotification( ContainerCreator.getGpuContainer( uri, capi ), new Counter( numGPUs ), Operation.TAKE, Operation.DELETE, Operation.WRITE );
			nots[2] = nm.createNotification( ContainerCreator.getMainboardContainer( uri, capi ), new Counter( numMBDs ), Operation.TAKE, Operation.DELETE, Operation.WRITE );
			nots[3] = nm.createNotification( ContainerCreator.getRamContainer( uri, capi ), new Counter( numRAMs ), Operation.TAKE, Operation.DELETE, Operation.WRITE );

			log.info( "#CPU: " + numCPUs.get() );
			log.info( "#GPU: " + numGPUs.get() );
			log.info( "#MBD: " + numMBDs.get() );
			log.info( "#RAM: " + numRAMs.get() );
		}
		
		final NotificationManager nm;
		
		final AtomicInteger numCPUs;
		final AtomicInteger numGPUs;
		final AtomicInteger numMBDs;
		final AtomicInteger numRAMs;
		
		final Notification[] nots = new Notification[4];
		
		void clean() {
			for ( Notification n : nots ) try { n.destroy(); } catch ( MzsCoreException e ) {}
			nm.shutdown();
		}
	}
	private static final class Counter implements NotificationListener {
		public Counter( AtomicInteger count ) {
			this.count = count;
		}

		private final AtomicInteger count;

		@Override
		public void entryOperationFinished( Notification source, Operation operation, List<? extends Serializable> entries ) {
			int cnt = 0;

			Class<?> c = null;
			for ( Serializable s : entries ) {
				c = s.getClass();
				if ( s instanceof Entry )        { s = ((Entry) s).getValue(); }
				if ( !(s instanceof Component) ) { continue; }
				
				
				switch ( operation ) {
					case TAKE:
					case DELETE:
						cnt = count.decrementAndGet();
						break;
					case WRITE:
						cnt = count.incrementAndGet();
						break;
				}
				
			}
			final Class<?> c2 = c;
			
			if ( cnt == 0 ) {
				new Thread() {
					public void run() {
						Worker.sleep( 500 );
						if ( count.get() == 0 ) System.out.println( "STILL ZERO " + c2.getSimpleName() );
					}
				}.start();
			}
		}
	}
}
