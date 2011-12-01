package main.java.tu.space.worker;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import main.java.tu.space.components.Component;
import main.java.tu.space.components.Cpu;
import main.java.tu.space.components.Gpu;
import main.java.tu.space.components.Mainboard;
import main.java.tu.space.components.RamModule;
import main.java.tu.space.util.ContainerCreator;
import main.java.tu.space.utils.EnumComponent;
import main.java.tu.space.utils.SpaceException;
import main.java.tu.space.workers.Producer;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;


/**
 * 
 * Space version of bsp1 Producer thread
 * 
 * @author raunig stefan
 */
public class SpaceProducer extends Producer implements NotificationListener{

	private final NotificationManager notification;
	private final Capi capi;
	private final URI space;
	
	public SpaceProducer(String workerId, int errorRate, EnumComponent enumValue, int quantity) {
		super(workerId, errorRate, enumValue, quantity);
		
		//default local space
		MzsCore core = DefaultMzsCore.newInstance(0);
	    capi = new Capi(core);
	    notification = new NotificationManager(core);
	    
	    //standalone server URI
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
	        capi.write(cref, RequestTimeout.DEFAULT, tx, new Entry(component));
	        
	        //set notification
	        notification.createNotification(cref, this, Operation.WRITE);
	        
	        //commit the transaction
	        capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			//TODO
			System.err.println("ERROR");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1, List<? extends Serializable> arg2) {
	}
}
