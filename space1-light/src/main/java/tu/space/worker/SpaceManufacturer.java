package main.java.tu.space.worker;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import main.java.tu.space.workers.Manufacturer;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;


/**
 * The manufacturer will be notified if a component was built, constructs a computer
 * and set a notification if pc is built.
 * @author system
 *
 */
public class SpaceManufacturer extends Manufacturer implements NotificationListener {

	private final NotificationManager notification;
	private final Capi capi;
	private final URI space;
	
	public SpaceManufacturer(){
		//default local space
		MzsCore core = DefaultMzsCore.newInstance(0);
		capi = new Capi(core);
		notification = new NotificationManager(core);
			    
		//standalone server URI
		space = URI.create("xvsm://localhost:9877");
	}
	
	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1, List<? extends Serializable> arg2) {

	}
}
