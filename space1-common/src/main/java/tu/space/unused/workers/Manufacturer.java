package tu.space.unused.workers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.unused.middleware.Category;
import tu.space.unused.middleware.Middleware;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;

public class Manufacturer extends Worker {
	private final Logger LOG = Logger.make( getClass() );

	public Manufacturer( String id, Middleware m ) {
		super( id, m );
	}

	protected void doRun() {		
		middleware.start();
		new WorkerThread().start();
		
		waitForNewLine();
	}

	private class WorkerThread extends Thread {
		public WorkerThread() {
			super("worker-thread");
		}
		
		@Override
		public void run() {
			final ArrayBlockingQueue<RamModule> tmp = new ArrayBlockingQueue<RamModule>( 3 );
			final ExecutorService               ex  = Executors.newCachedThreadPool();
			
			try {
				while ( true ) {
					Future<String>      uuidF      = ex.submit( new Callable<String>() {
						@Override
						public String call() throws Exception {
							return middleware.generateId();
						}
					});		
					Future<Cpu>       cpuF       = getComponent( ex, middleware.cpus(), "CPU" );
					Future<Gpu>       gpuF       = getComponent( ex, middleware.gpus(), "GPU" );
					Future<Mainboard> mainboardF = getComponent( ex, middleware.mainboards(), "Mainboard" );
					Future<RamModule> ramF       = getComponent( ex, middleware.ramModules(), "RAM" ); // get 1 ram
					// try to get as much as 3 more ram sticks
					Future<?>         ramsF      = ex.submit( new Runnable() {
						@Override
						public void run() {
							try {
								while ( tmp.size() < 3 ) {
									RamModule ram = middleware.ramModules().receive();
									if ( ram != null ) tmp.put( ram );
								}
							} catch ( SpaceException e ) {
								// the connection was closed while we were trying to read from it, no bother
							} catch ( InterruptedException e ) {
								e.printStackTrace();
							}
						}
					});

					try {
						// mandatory parts
						String          uuid = uuidF.get();
						LOG.info("%s got id", Manufacturer.this);
						Cpu             cpu  = cpuF.get();
						Mainboard       mbd  = mainboardF.get();
						RamModule       ram  = ramF.get();
						// optional parts
						Gpu gpu;
						List<RamModule> rams;
						
						if ( gpuF.isDone()  ) {
							gpu = gpuF.get();
						} else {
							gpuF.cancel( true );
							gpu = null;
						}
						if ( ramsF.isDone() ) {
							rams = new ArrayList<RamModule>( tmp );
						} else {
							ramsF.cancel( true );
							rams = new ArrayList<RamModule>();
						}
						LOG.info("%s got %d ram", Manufacturer.this, (ram != null ? 1 : 0) + rams.size());

						while ( rams.size() != 0 && rams.size() != 1 && rams.size() != 3 )
							middleware.ramModules().send( rams.remove( 0 ) );
						rams.add( ram );
						
						middleware.allComputers().send( new Computer( uuid, id, cpu, gpu, mbd, rams ) );
						LOG.info("%s produced a computer", Manufacturer.this);

						middleware.commit();
					} catch ( InterruptedException e1 ) {
						middleware.rollback();
						e1.printStackTrace();
					} catch ( ExecutionException e1 ) {
						middleware.rollback();
						e1.printStackTrace();
					}				
				}
			} catch ( SpaceException e ) {
//				e.printStackTrace();
			} finally {
				ex.shutdown();
			}
		}
	}
	
	private <E extends Serializable> Future<E> getComponent( ExecutorService ex, final Category<E> c, final String type ) {
		return ex.submit( new Callable<E>() {
			@Override
			public E call() throws Exception {
				E e = c.receive();
				if ( e != null ) LOG.info("%s got %s", Manufacturer.this, type);
				return e;
			}
		});
	}
}
