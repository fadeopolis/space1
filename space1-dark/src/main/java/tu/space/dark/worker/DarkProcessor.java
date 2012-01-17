package tu.space.dark.worker;

import tu.space.components.Product;
import tu.space.dark.JMSInput;
import tu.space.middleware.Middleware;
import tu.space.worker.Worker;

public abstract class DarkProcessor<P extends Product> extends Worker implements Runnable {

	public DarkProcessor( String id, Middleware m ) {
		super( id, m );
		
		this.in = input();
	}

	@Override
	public void run() {
		while ( true ) {
			mw.beginTransaction();
			
			P p = in.takeBlocking();
			
			if ( p == null ) {
				mw.rollbackTransaction();
			}
			
			if ( process( p ) ) {
				mw.commitTransaction();
			} else {
				mw.rollbackTransaction();
			}
		}
	}
	
	protected abstract boolean process( P p );
	
	protected abstract JMSInput<P> input();
	
	private final JMSInput<P> in;
}
