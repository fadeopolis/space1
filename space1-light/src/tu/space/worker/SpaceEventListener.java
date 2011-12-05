package tu.space.worker;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;

public abstract class SpaceEventListener implements NotificationListener{

	private final URI space;
	private final Capi capi;
	
	private ContainerReference cref;
	
	public SpaceEventListener(){
		Logger log = Logger.make(SpaceEventListener.class);
		
		MzsCore core = DefaultMzsCore.newInstance();
		capi = new Capi(core);
		
		space = URI.create("xvsm://localhost:9877");
		
		try {
			cref = ContainerCreator.getEventContainer(space, capi);
		} catch (MzsCoreException e) {
			log.info("Lookup for SpaceEvent failed");
			cref = null;
		}
	}
	
	@Override
	public void entryOperationFinished(Notification source, Operation operation, List<? extends Serializable> entries) {
		
	}
	
	public abstract void getAll(List<Object> objs);
	public abstract void onCreated(Object obj);
	public abstract void onRemoved(Object obj);

}
