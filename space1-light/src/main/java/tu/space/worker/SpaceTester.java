package main.java.tu.space.worker;

import java.io.Serializable;
import java.util.List;

import main.java.tu.space.workers.Tester;

import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;


/**
 * 
 * Tester will be notified if a computer is ready for testing, does it and 
 * marks it okay or failure. Afterwards set a notification pc was tested. 
 * 
 * @author raunig stefan
 */
public class SpaceTester extends Tester implements NotificationListener {

	public SpaceTester(){
		
	}
	
	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1, List<? extends Serializable> arg2) {

	}

}
