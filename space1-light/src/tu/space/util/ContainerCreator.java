package tu.space.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;

/**
 * This creator manages ContainerReferences
 * 
 * @author raunig stefan
 */
public abstract class ContainerCreator{

	/**
	 * This container holds CPU's, it will be looked up if created, else 
	 * it will be created.
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 				Container for CPU'S
	 * @throws MzsCoreException
	 */
	public static ContainerReference getCpuContainer(final URI space, final Capi capi) throws MzsCoreException{
		ContainerReference cref;
		try {
			//Log lookup for Container named CpuContainer
			cref = capi.lookupContainer("CpuContainer", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			//Log not found create it
			List<AnyCoordinator> oblicoord = new ArrayList<AnyCoordinator>();
			oblicoord.add(new AnyCoordinator());
			//Default coordinator is AnyCoordinaor
			cref = capi.createContainer("CpuContainer", space, Container.UNBOUNDED, oblicoord, null, null);
		}
		return cref;
	}
	
	/**
	 * This container holds GPU's, it will be looked up if created, else 
	 * it will be created.
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 				Container for GPU'S
	 * @throws MzsCoreException
	 */
	public static ContainerReference getGpuContainer(final URI space, final Capi capi) throws MzsCoreException{
		ContainerReference cref;
		try {
			//Log lookup for Container named CpuContainer
			cref = capi.lookupContainer("GpuContainer", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			//Log not found create it
			List<AnyCoordinator> oblicoord = new ArrayList<AnyCoordinator>();
			oblicoord.add(new AnyCoordinator());
			//Default coordinator is AnyCoordinaor
			cref = capi.createContainer("GpuContainer", space, Container.UNBOUNDED, oblicoord, null, null);
		}
		return cref;
	}
	
	/**
	 * This container holds RAM's, it will be looked up if created, else 
	 * it will be created.
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 				Container for RAM's
	 * @throws MzsCoreException
	 */
	public static ContainerReference getRamContainer(final URI space, final Capi capi) throws MzsCoreException{
		ContainerReference cref;
		try {
			//Log lookup for Container named CpuContainer
			cref = capi.lookupContainer("RamContainer", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			//Log not found create it
			List<AnyCoordinator> oblicoord = new ArrayList<AnyCoordinator>();
			oblicoord.add(new AnyCoordinator());
			//Default coordinator is AnyCoordinaor
			cref = capi.createContainer("RamContainer", space, Container.UNBOUNDED, oblicoord, null, null);
		}
		return cref;
	}
	
	/**
	 * This container holds Mainboards, it will be looked up if created, else 
	 * it will be created. The coordination is first in first out
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 				Container for Mainboards
	 * @throws MzsCoreException
	 */
	public static ContainerReference getMainboardContainer(final URI space, final Capi capi) throws MzsCoreException{
		ContainerReference cref;
		try {
			//Log lookup for Container named CpuContainer
			cref = capi.lookupContainer("MainboardContainer", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			//Log not found create it
			List<FifoCoordinator> oblicoord = new ArrayList<FifoCoordinator>();
			oblicoord.add(new FifoCoordinator());
			//Default coordinator is FIFOCoordinator
			cref = capi.createContainer("MainboardContainer", space, Container.UNBOUNDED, oblicoord, null, null);
		}
		return cref;
	}
	
	/**
	 * This container holds Pc's, it will be looked up if created, else 
	 * it will be created.
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 				Container for Mainboards
	 * @throws MzsCoreException
	 */
	public static ContainerReference getPcContainer(final URI space, final Capi capi) throws MzsCoreException{
		ContainerReference cref;
		try {
			//Log lookup for Container named CpuContainer
			cref = capi.lookupContainer("PcContainer", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			//Log not found create it
			List<Coordinator> oblicoord = new ArrayList<Coordinator>();
			oblicoord.add(new LabelCoordinator());
			cref = capi.createContainer("PcContainer", space, Container.UNBOUNDED, oblicoord, null, null);
		}
		return cref;
	}
	
	/**
	 * This container holds defect Pc's, it will be looked up if created, else 
	 * it will be created.
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 				Container for Mainboards
	 * @throws MzsCoreException
	 */
	public static ContainerReference getPcDefectContainer(final URI space, final Capi capi) throws MzsCoreException{
		ContainerReference cref;
		try {
			//Log lookup for Container named CpuContainer
			cref = capi.lookupContainer("PcDefectContainer", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			//Log not found create it
			List<AnyCoordinator> oblicoord = new ArrayList<AnyCoordinator>();
			oblicoord.add(new AnyCoordinator());
			//Default coordinator is FIFOCoordinator
			cref = capi.createContainer("PcDefectContainer", space, Container.UNBOUNDED, oblicoord, null, null);
		}
		return cref;
	}
	
	/**
	 * This container holds defect Pc's, it will be looked up if created, else 
	 * it will be created.
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 				Container for Mainboards
	 * @throws MzsCoreException
	 */
	public static ContainerReference getEventContainer(final URI space, final Capi capi) throws MzsCoreException{
		ContainerReference cref;
		try {
			//Log lookup for Container named CpuContainer
			cref = capi.lookupContainer("SpaceEvent", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			//Log not found create it
			List<FifoCoordinator> oblicoord = new ArrayList<FifoCoordinator>();
			oblicoord.add(new FifoCoordinator());
			//Default coordinator is FIFOCoordinator
			cref = capi.createContainer("SpaceEvent", space, Container.UNBOUNDED, oblicoord, null, null);
		}
		return cref;
	}

}
