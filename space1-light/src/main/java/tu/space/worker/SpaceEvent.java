package tu.space.worker;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import tu.space.util.ContainerCreator;

public abstract class SpaceEvent {
	
	private final Capi capi;
	private ContainerReference crefEvent;
	protected URI space = URI.create("xvsm://localhost:9877");
	
	public SpaceEvent(final Capi capi){
		this.capi = capi;
		try {
			//lookup container
			crefEvent = ContainerCreator.getEventContainer(space, capi);
			
		} catch (MzsCoreException e) {
			crefEvent = null;
		}
	}
	
	public void createEvent(final Serializable s){
		try {
			Entry entry = new Entry(s);
			capi.write(crefEvent, 5000, null, entry);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}
	public void deleteEvent(final Serializable s){
		try {
			//TODO select ????
			capi.delete(crefEvent, Arrays.asList(FifoCoordinator.newSelector()), 5000, null);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

}
