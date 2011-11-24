package tu.space.workers;

import java.util.Random;

/**
 *
 * Welcome to the world of grown ups.
 *
 **/
public abstract class Worker {	
	private String id;
	
	public static void main(String[] args){
	}
	
	/**
	 * Every worker has a id
	 * @return id of worker
	 */
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	/**
	 * The production needs time, this method simulates 
	 * a working period between 1-3 sec.
	 */
	public void doWork() {
		Random rand = new Random();		
		try {
			Thread.sleep(rand.nextLong() % 3000);
		} catch (InterruptedException e) {
			//do some logging
			e.printStackTrace();
		}
	}
}
