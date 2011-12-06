package tu.space.worker;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;

public abstract class SpaceEventListener implements NotificationListener{

	private final URI space;
	private final Capi capi;
	
	private ContainerReference crefmain;
	private ContainerReference crefcpu;
	private ContainerReference crefgpu;
	private ContainerReference crefram;
	private ContainerReference crefpc;
	private ContainerReference creftrash;
	
	public SpaceEventListener(){
		Logger log = Logger.make(SpaceEventListener.class);
		
		MzsCore core = DefaultMzsCore.newInstance(0);
		capi = new Capi(core);
		
		space = URI.create("xvsm://localhost:9877");
		
		
		NotificationManager nManager = new NotificationManager(core);
		
		try {
			//lookup container
			crefmain = ContainerCreator.getMainboardContainer( space, capi );
			crefcpu = ContainerCreator.getCpuContainer( space, capi );
			crefgpu = ContainerCreator.getGpuContainer( space, capi );
			crefram = ContainerCreator.getRamContainer( space, capi );
			crefpc = ContainerCreator.getPcContainer( space, capi );
			creftrash = ContainerCreator.getPcDefectContainer( space, capi );
			
			//create Notifications
//			nManager.createNotification(cref, this, Operation.WRITE, Operation.DELETE);
//			
//			//take what is in space
//			List<Serializable> takeEvents = capi.take(cref, Arrays.asList(FifoCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX)), MzsConstants.RequestTimeout.DEFAULT, null);
			
			//fire callback
//			getAll(takeEvents);
		} catch (MzsCoreException e) {
			log.info("Lookup for SpaceEvent failed");
//			cref = null;
//		} catch (InterruptedException e) {
//			e.printStackTrace();
		}
	}
	
	@Override
	public void entryOperationFinished(Notification source, Operation operation, List<? extends Serializable> entries) {
		
		Serializable entry = entries.get(0);
		
//		if(entry instanceof Computer)
		
		if(operation.equals(Operation.WRITE)){
			onCreated(entry);
		} else if(operation.equals(Operation.TAKE)){
			onRemoved(entry);
		}
	}
	
	public abstract void getAll(List<Serializable> objs);
	public abstract void onCreated(Object obj);
	public abstract void onRemoved(Object obj);

}
