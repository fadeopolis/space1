package tu.space.worker;

import java.net.URI;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;

import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Component;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;
import tu.space.utils.SpaceException;
import tu.space.worker.Producer;

/**
 * 
 * Space version of bsp1 Producer thread
 * 
 * @author raunig stefan
 */
public class SpaceProducer extends Producer {
	
	private final Logger log = Logger.make(getClass());
	
	private final String workerId;

	private final MzsCore core;
	private final Capi capi;
	private final URI space;
		
	public static void main(String[] args) throws SpaceException {
		//test main
		Logger.configure();

		new SpaceProducer("TestWorker3", 10, "ram", 3).start();
	}
	
	public SpaceProducer(final String workerId, final double errorRate, final String component, final int quantity) {
		super(workerId, errorRate, component, quantity);
		
		this.workerId = workerId;
		
		//default local space
		core = DefaultMzsCore.newInstance(0);
	    capi = new Capi(core);
	    
	    //embedded space
	    space = URI.create("xvsm://localhost:9877");
	}
	
	@Override
	public void publish(Component component) {
		try {
			//lookup container or create it
			ContainerReference cref=null;
			if(component instanceof Cpu) {
				cref = ContainerCreator.getCpuContainer(space, capi);
			}
			else if(component instanceof Gpu) {
				cref = ContainerCreator.getGpuContainer(space, capi);
			}
			else if(component instanceof RamModule) {
				cref = ContainerCreator.getRamContainer(space, capi);
			}
			else if(component instanceof Mainboard) {
				cref = ContainerCreator.getMainboardContainer(space, capi);
			}
			else throw new SpaceException("This should never be thrown");
			//create a transaction with a timeout of 5000 milliseconds
	        TransactionReference tx = capi.createTransaction(5000, space);
	        	        
	        //write an entry to the container using the default timeout and the transaction
	        log.info("Worker: %s, produziere %s, Error: %s", workerId, component.id.toString(), component.hasDefect);
	        capi.write(cref, RequestTimeout.DEFAULT, tx, new Entry(component));
	        
	        //commit the transaction
	        capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			System.err.println("ERROR");
			e.printStackTrace();
		} catch (SpaceException e) {
			System.err.println("This should be never thrown");
			e.printStackTrace();
		}
	}
	
	/**
	 * Clean up shutdown core
	 */
	@Override
	public void clean(){
		this.core.shutdown(true);
	}
}
