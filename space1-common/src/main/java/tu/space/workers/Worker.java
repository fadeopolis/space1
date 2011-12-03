package tu.space.workers;

import tu.space.middleware.Middleware;
import tu.space.utils.Logger;
import tu.space.utils.Util;


/**
 *
 * Welcome to the world of grown ups.
 *
 **/
public abstract class Worker implements Runnable  {	
	public    final String     id;
	protected final Middleware middleware;
	protected final Logger     LOG = Logger.make( getClass() );

	public Worker( String id, Middleware m ) {
		super();
		this.id = id;
		this.middleware = m;
	}

	public final void run() {
		LOG.info("STARTING %s", this);
		
		try {
			doRun();
		} finally {
			middleware.stop();
		}
		
		LOG.info("%s finished", this);
	}
	
	protected abstract void doRun();
	
	protected final void waitForNewLine() {
		System.out.println("PRESS ENTER TO QUIT");
		Util.waitForNewline();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + id + "]";
	}
}
