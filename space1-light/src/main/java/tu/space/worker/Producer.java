package tu.space.worker;

import java.util.Random;

import tu.space.components.Component;
import tu.space.utils.UUIDGenerator;

/**
 *
 * This class represents a chinese worker, cheep and poor but nobody cares.
 *
 * @author raunig stefan
**/
public abstract class Producer implements Runnable {
	
	private Thread thread;
	
	private String workerId, component;
	private int quantity;
	private double errorRate;
	
	public Producer(final String workerId, final double errorRate, final String component, final int quantity){
		this.workerId = workerId;
		this.errorRate = errorRate;
		this.component = component;
		this.quantity = quantity;
	}
	
	public synchronized void start(){
		if (thread == null){
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public synchronized void interrupt(){
		if(thread != null){
			thread.interrupt();
			thread = null;
		}
	}
	
	public void run() {
		if(thread == null) start();
		for(int i = 0; i<quantity;i++){		
			/*
			 * The production needs time, here we simulate
			 * a working period between 1-3 sec.
			 */
			Random rand = new Random();		
			try {
//				Thread.sleep(rand.nextInt(3000));
				/**
				 * DEBUG producing at higher rate
				 */
				Thread.sleep(rand.nextInt(500));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Component to be produced		
			Component.Factory<?> factory;
			if (      "CPU".equalsIgnoreCase( component ) )       factory = new Component.CpuFactory();
			else if ( "GPU".equalsIgnoreCase( component ) )       factory = new Component.GpuFactory();
			else if ( "MAINBOARD".equalsIgnoreCase( component ) ) factory = new Component.MainboardFactory();
			else if ( "RAM".equalsIgnoreCase( component ) )       factory = new Component.RamModuleFactory();
			else {
				System.exit(1);
				factory = null;
			}
			Random rand2  = new Random();
			UUIDGenerator uuids = new UUIDGenerator();
			
			// produce next component
			boolean faulty    = rand2.nextDouble() < errorRate; // bernoulli experiment
			String  productId = uuids.generate();
			
			Component c = factory.make( productId, workerId, faulty );
			
			assert c.id         == productId;
			assert c.producerId == workerId;
			assert c.hasDefect  == faulty;
			
			//this method will be overriden by subclasses
			publish(c);
		}
		//finish and go home
		System.out.println("Finished work going home! Worker id: "+ workerId);
		clean();
	}
	
	/**
	 * To be overriden by space version or message version, to add functionallity
	 */
	public void publish(Component component) {/*empty*/}
	
	/**
	 * To be overriden by space version or message version, to free resources
	 */
	public void clean(){/*empty*/}
}
