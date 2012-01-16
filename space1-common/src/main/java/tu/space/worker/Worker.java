package tu.space.worker;

import java.util.Random;

import tu.space.middleware.Middleware;
import tu.space.utils.Logger;
import tu.space.utils.UUIDGenerator;

public abstract class Worker {
	public Worker( String id, Middleware m ) {
		super();
		this.id = id;
		this.mw = m;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + id;
	}
	
	protected void shutdown() {
		mw.shutdown();
	}
	
	// simulate work
	protected void sleep() {
		try {
			Thread.sleep( MIN_SLEEP_TIME + rand.nextInt( MAX_SLEEP_TIME - MIN_SLEEP_TIME ) );
		} catch ( InterruptedException e ) {
		}
	}

	protected String genUUID()      { return uuid.generate(); }
	protected double randomDouble() { return rand.nextDouble(); }
		
	protected final String     id;
	protected final Middleware mw;
	protected final Logger     log = Logger.make( getClass() );
	
	private final UUIDGenerator uuid = new UUIDGenerator();
	private final Random        rand = new Random();
	
	private static final int MIN_SLEEP_TIME = 1000;
	private static final int MAX_SLEEP_TIME = 3000;
}
