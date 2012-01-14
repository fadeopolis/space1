package tu.space.light;

import java.util.Random;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Component;
import tu.space.util.ProductManager;
import tu.space.utils.UUIDGenerator;

public class Producer<C extends Component> extends Worker {
	public Producer( String name, Capi capi, int port, int quantity, double errorRate, 
			Component.Factory<C> f, ProductManager<C> p ) {
		super( name, capi, port );
		
		this.quantity  = quantity;
		this.errorRate = errorRate;
		this.factory   = f;
		this.manager   = p;
	}
	
	@Override
	public void run() {
		for( int i = 0; i < quantity; i++ ) {		
			Random        rand  = new Random();		
			/*
			 * The production needs time, here we simulate
			 * a working period between 1-3 sec.
			 */
			try {
				Thread.sleep(rand.nextInt(500));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			UUIDGenerator uuids = new UUIDGenerator();
			
			// produce next component
			boolean   faulty    = rand.nextDouble() < errorRate; // bernoulli experiment
			String    productId = uuids.generate();
			
			C c = factory.make( productId, workerId, faulty );
			
			assert c.id         == productId;
			assert c.producerId == workerId;
			assert c.hasDefect  == faulty;
			
			publish( c );
		}

		//finish and go home
		System.out.println("Finished work going home! Worker id: "+ workerId);
		clean();
	}

	public void publish( C c ) {
		TransactionReference tx = null;
		try {
			tx = beginTransaction();

			log.info("Worker: %s, produziere %s, Error: %s", workerId, c.id.toString(), c.hasDefect);
			
			manager.write( tx, c );
			
			commit( tx );
		} catch ( MzsCoreException e ) {
			if ( tx != null ) rollback( tx );
			
			log.error( "Could not publish part %s", c );
			e.printStackTrace();
		}
	}
	
	@Override
	public void clean() {}
	
	private final int    quantity;
	private final double errorRate;
	private final Component.Factory<C> factory;
	private final ProductManager<C>    manager;
}
