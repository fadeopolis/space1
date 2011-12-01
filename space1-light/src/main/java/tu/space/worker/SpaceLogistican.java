package main.java.tu.space.worker;

import java.io.Serializable;
import java.util.List;

import main.java.tu.space.workers.Logistican;

import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;


/**
 * 
 * Ships a pc after she was notified
 * 
 * @author raunig stefan
 */
public class SpaceLogistican extends Logistican implements NotificationListener {

	public SpaceLogistican(){
		
	}
	
	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1, List<? extends Serializable> arg2) {

	}

}
