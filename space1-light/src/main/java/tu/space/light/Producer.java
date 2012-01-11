package tu.space.light;

import java.util.Random;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Component;
import tu.space.components.Cpu;
import tu.space.components.Cpu.Type;
import tu.space.util.ContainerCreator;
import tu.space.utils.UUIDGenerator;

public class Producer<C extends Component> extends Worker {
	public Producer( String name, Capi capi, int port, int quantity, double errorRate, 
			Component.Factory<C> f, ContainerReference cref) {
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
			
			//labeldata for cpu-type
			Entry entry = null;
			if(c instanceof Cpu){
				if(((Cpu) c).type == Type.SINGLE_CORE){
					entry = new Entry( c,  
							LabelCoordinator.newCoordinationData(ContainerCreator.SINGLE_CORE));
				} else if (((Cpu) c).type == Type.DUAL_CORE){
					entry = new Entry( c,  
							LabelCoordinator.newCoordinationData(ContainerCreator.DUAL_CORE));
				} else if (((Cpu) c).type == Type.QUAD_CORE){
					entry = new Entry( c,  
							LabelCoordinator.newCoordinationData(ContainerCreator.QUAD_CORE));					
				} else {
					entry = null;
				}
			} else {
				//every component but not a cpu
				entry = new Entry( c );
			}
			
			capi.write( cref, RequestTimeout.DEFAULT, tx, entry );
			
			//commit the transaction
			capi.commitTransaction(tx);
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
	private final ContainerReference   cref;
}
