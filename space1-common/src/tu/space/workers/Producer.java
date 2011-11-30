package tu.space.workers;

import java.util.Random;

import tu.space.components.Component;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.utils.EnumComponent;

/**
 *
 * This class represents a chinese worker, cheep and poor but nobody cares.
 *
 * @author raunig stefan
**/
public class Producer implements Runnable {
	
	private Thread thread;
	
	private String workerId;
	private int errorRate, quantity;
	private EnumComponent enumValue;
	
	public Producer(final String workerId, final int errorRate, final EnumComponent enumValue, final int quantity){
		this.workerId = workerId;
		this.errorRate = errorRate;
		this.enumValue = enumValue;
		this.quantity = quantity;
	}
	
	public synchronized void start(){
		if (thread == null){
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void run() {
		if(thread == null) start();
		for(int i = 0; i<quantity;i++){
			/*
			 * The production needs time, here we simulate
			 * a working period between 1-3 sec.
			 */
			Random rand = new Random(12345);		
			try {
				Thread.sleep(rand.nextLong() % 3001);
			} catch (InterruptedException e) {
				//TODO log
				e.printStackTrace();
			}
			
			//Component to be produced
			Component component;
			
			switch(enumValue){
				case CPU :
					component = new Cpu(generateId(), workerId, generateError());
					break;
				case GPU :
					component = new Gpu(generateId(), workerId, generateError());
					break;
				case RAM :
					component = new RamModule(generateId(), workerId, generateError());
					break;
				case MAINBOARD :
					component = new Mainboard(generateId(), workerId, generateError());
					break;
				default:
					//not a component
					thread.interrupt();
				}
			
			//make Component available in space
			//TODO
			component = null;
			//set notifications
			//TODO
		}
	}
	
	
	/**
	 * TODO DELETE later maybe we need it ??? 
	 * 
	 * @param id @ args[0], errorRate @ args[1], component @ args[2] quanitiy @ args[3]
	 * @throws SpaceException 
	 */
//	public static void main(String[] args) throws SpaceException {
//		//check input
//		if(args.length != 4){
//			throw new SpaceException("Usage: ./producer id errorRate component quantity");
//		}
//		
//		//workerId
//		String workerId = args[0];
//		
//		//parse integer
//		int quantity=0;
//		try{
//			error = Integer.parseInt(args[1]);
//			quantity = Integer.parseInt(args[3]);
//		} catch (NumberFormatException ex){
//			//TODO log
//			System.err.print("ErrorRate or quantity is not a number!");
//			ex.printStackTrace();
//			System.exit(1);
//		}
//				
//		for(int i = 0; i<quantity;i++){
//			/*
//			 * The production needs time, here we simulate
//			 * a working period between 1-3 sec.
//			 */
//			Random rand = new Random();		
//			try {
//				Thread.sleep(rand.nextLong() % 3000);
//			} catch (InterruptedException e) {
//				//TODO log
//				e.printStackTrace();
//			}
//			
//			//Component to be produced
//			Component component;
//			
//			switch(EnumComponent.valueOf(args[1])){
//				case CPU :
//					component = new Cpu(generateId(), workerId, generateError());
//					break;
//				case GPU :
//					component = new Cpu(generateId(), workerId, generateError());
//					break;
//				case RAM :
//					component = new Cpu(generateId(), workerId, generateError());
//					break;
//				case MAINBOARD :
//					component = new Cpu(generateId(), workerId, generateError());
//					break;
//				default:
//					//not a component
//					throw new SpaceException("The specified component is not valid!");
//			}
//			
//			//make Component available in space
//			//TODO
//		}
//	}

	/**
	 * Decide if component has an error, 
	 * 
	 * @return boolean
	 */
	private boolean generateError() {
		int decider = errorRate % 101;
		
		Random generator = new Random(12345);
		int random = generator.nextInt(101);
		
		if(decider >= random) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return unique string
	 */
	private String generateId() {
		//TODO
		return "";
	}
}
