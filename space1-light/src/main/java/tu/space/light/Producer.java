package tu.space.light;

import java.util.Random;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Component;
import tu.space.utils.UUIDGenerator;

public class Producer<C extends Component> extends Worker {
	public Producer( String name, Capi capi, int port, int quantity, double errorRate, Component.Factory<C> f, ContainerReference cref ) {
		super( name, capi, port );
		
		this.quantity  = quantity;
		this.errorRate = errorRate;
		this.factory   = f;
		this.cref      = cref;
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
			
			Component c = factory.make( productId, workerId, faulty );
			
			assert c.id         == productId;
			assert c.producerId == workerId;
			assert c.hasDefect  == faulty;
			
			publish( c );
		}

		//finish and go home
		System.out.println("Finished work going home! Worker id: "+ workerId);
		clean();
	}

	public void publish( Component c ) {
		TransactionReference tx = null;
		try {
			tx = capi.createTransaction(5000, space);

			//write an entry to the container using the default timeout and the transaction
			log.info("Worker: %s, produziere %s, Error: %s", workerId, c.id.toString(), c.hasDefect);
		
			capi.write( cref, RequestTimeout.DEFAULT, tx, new Entry( c ) );
			
			//commit the transaction
			capi.commitTransaction(tx);
		} catch ( MzsCoreException e ) {
			rollback( tx );
			
			log.error( "Could not publish part %s", c );
			e.printStackTrace();
		}
	}
	
	private final int    quantity;
	private final double errorRate;
	private final Component.Factory<C> factory;
	private final ContainerReference   cref;
}
