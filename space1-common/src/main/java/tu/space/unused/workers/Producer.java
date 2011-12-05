package tu.space.unused.workers;

import java.util.Random;
import java.util.UUID;

import tu.space.components.Component;
import tu.space.unused.middleware.Middleware;
import tu.space.utils.Util;

public class Producer<C extends Component> extends Worker {
	public static final int MAX_SLEEP_TIME = 1;

	public final int    quota;
	public final double errorRate;
	
	public Producer( String id, Middleware m, Component.Factory<C> f, int quota, double errorRate ) {
		super( id, m );
		
		this.quota     = quota;
		this.errorRate = errorRate;

		this.factory    = f;
	}

	protected final void doRun() {
		for ( int i = quota; i > 0; i-- ) {
			LOG.info("%s has to make %d more component(s)", this, i);

			// produce next component
			boolean faulty    = rand.nextDouble() < errorRate; // bernoulli experiment
			UUID    productId = middleware.generateId();
			C       c         = factory.make( productId, id, faulty );

			assert c.id         == productId;
			assert c.producerId == id;
			assert c.hasDefect  == faulty;

			// sleep to simulate work time
			Util.sleep( rand.nextInt( MAX_SLEEP_TIME ) );

			// send out produced component
			try {
				middleware.beginTransaction();
					middleware.send( c );
				middleware.commit();				
			} catch ( RuntimeException e ) {
				middleware.rollback();
				throw e;
			}
		}
	}
	
	private final Component.Factory<C> factory;
	private final Random               rand = new Random();
}
