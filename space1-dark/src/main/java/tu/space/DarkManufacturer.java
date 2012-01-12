package tu.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamModule;
import tu.space.jms.JMS;
import tu.space.jms.JMSReader;
import tu.space.jms.JMSWriter;
import tu.space.utils.DummyFuture;
import tu.space.utils.Logger;
import tu.space.utils.UUIDGenerator;
import tu.space.utils.Util;

public class DarkManufacturer {
	public static final String USAGE = "usage: manufacturer ID PORT";
	
	public static void main( String... args ) throws JMSException {
		new DarkManufacturer( args );
	}
	DarkManufacturer( String[] args ) throws JMSException {	
		if ( args.length != 2 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		id = args[0];
		
		Logger.configure();
		final Logger log = Logger.make( DarkManufacturer.class );

		final UUIDGenerator uuids = new UUIDGenerator();
		
		final Connection conn = JMS.openConnection( Integer.parseInt( args[1] ));
		final Session    sess = JMS.createSession( conn );
	
		final JMSWriter<Computer>  pcs    = JMS.getPCWriter( sess );
		final JMSWriter<RamModule> ramOut = JMS.getRAMWriter( sess );
		
		final JMSReader<Cpu>       cpus = JMS.getCPUReader( sess );
		final JMSReader<Gpu>       gpus = JMS.getGPUReader( sess );
		final JMSReader<Mainboard> mbds = JMS.getMainboardReader( sess );
		final JMSReader<RamModule> rams = JMS.getRAMReader( sess );
		
		conn.start();
		final ExecutorService ex = Executors.newCachedThreadPool();
		
		new Thread() {
			public void run() {
				System.out.println("PRESS ENTER TO QUIT");
				Util.waitForNewline();

				ex.shutdownNow();
				JMS.close( conn );
			}
		}.start();
		
		try {
			while ( true ) {
				Future<Cpu>             cpuF  = getComponent( ex, cpus );
				Future<Gpu>             gpuF  = getComponent( ex, gpus );
				Future<Mainboard>       mbdF  = getComponent( ex, mbds );
				Future<List<RamModule>> ramF  = getRam( ex, rams );
				
				String          uuid  = uuids.generate();
				Cpu             cpu   = cpuF.get();
				Gpu             gpu   = getOrCancel( gpuF );
				Mainboard       mbd   = mbdF.get();
				List<RamModule> ram   = ramF.get();

				// return unused pieces
				if ( ram.size() == 3 ) {
					ramOut.send( ram.remove( 0 ) );
				}
				log.info("%s is using %d pieces of RAM", id, ram.size() );
					
				Computer c = new Computer( uuid, id, null, cpu, gpu, mbd, ram );				
				
				// simulate work
				Util.sleep();
				
				pcs.send( c );

				sess.commit();
				
				log.info("%s made a PC", id );
			}
		} catch ( JMSException e ) {
		} catch ( InterruptedException e ) {
		} catch ( ExecutionException e ) {
		} finally {
			log.info("%s finished", id );
			if ( ex != null ) ex.shutdownNow();
			JMS.close( conn );
		}
	}
	
	private <P extends Product> Future<P> getComponent( ExecutorService ex, final JMSReader<P> in ) {
		
		if ( ex.isTerminated() ) return new DummyFuture<P>();
		
		return ex.submit( new Callable<P>() {
			@Override
			public P call() throws Exception {
				P p = in.read();
				log.info( "%s got a %s", id, p.getClass().getSimpleName() );
				
				return p;
			}
		});
	}
	private Future<List<RamModule>> getRam( ExecutorService ex, final JMSReader<RamModule> in ) {
		if ( ex.isTerminated() ) return DummyFuture.make( Collections.<RamModule>emptyList() );
			
		return ex.submit( new Callable<List<RamModule>>() {
			@Override
			public List<RamModule> call() throws Exception {
				List<RamModule> ram = new ArrayList<RamModule>();
				
				RamModule r = in.read();

				if ( r == null ) return ram;
				
				ram.add( r );
				log.info("%s got a RAM module", id);
				
				for ( int i = 0; i < 3; i++ ) {
					r = in.readNoWait();
					if ( r != null ) {
						ram.add( r );
						log.info("%s got a RAM module", id);
					}									
				}
					
				return ram;
			}
		});
	}
	
	private static <E> E getOrCancel( Future<E> f ) {
		try {
			if ( f.isDone() ) {
				return f.get();
			} else {
				f.cancel( true );
				return null;
			}
		} catch ( InterruptedException e ) {
			return null;
		} catch ( ExecutionException e ) {
			return null;
		}
	}

	private final String id;
	private final Logger log = Logger.make( getClass() );
}
