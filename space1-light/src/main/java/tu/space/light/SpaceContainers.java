package tu.space.light;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamModule;
import tu.space.utils.SpaceException;

final class SpaceContainers {
	SpaceContainers( Capi capi, URI space ) {
		this.capi  = capi;
		this.space = space;
	}

	public <P extends Product> ContainerReference getContainer( Class<P> c ) {
		if ( c == Computer.class  ) return getPcs();
		if ( c == Cpu.class       ) return getCpus();
		if ( c == Gpu.class       ) return getGpus();
		if ( c == Mainboard.class ) return getMainboards();
		if ( c == RamModule.class ) return getRams();
		
		throw new SpaceException( "Bad type: " + c );
	}
	
	public ContainerReference getPcs() {
		if ( pcs == null ) pcs = getContainer( "Computer", new AnyCoordinator(), new LabelCoordinator() );
		
		return pcs;
	}
	public ContainerReference getStorage() {
		if ( storage == null ) storage = getContainer( "Storage", null, null );
		
		return storage;
	}
	public ContainerReference getTrash() {
		if ( trash == null ) trash = getContainer( "Trash", null, null );
		
		return trash;
	}
	
	public ContainerReference getCpus() {
		if ( cpus == null ) cpus = getContainer( "Cpu", new AnyCoordinator(), new LabelCoordinator() );
		
		return cpus;
	}
	public ContainerReference getGpus() {
		if ( gpus == null ) gpus = getContainer( "Gpu", null, null );
		
		return gpus;
	}
	public ContainerReference getMainboards() {
		if ( mbds == null ) mbds = getContainer( "Mainboard", new FifoCoordinator(), null );
		
		return mbds;
	}
	public ContainerReference getRams() {
		if ( rams == null ) rams = getContainer( "RAM", null, null );
		
		return rams;
	}
	public ContainerReference getOrders() {
		if ( orders == null ) orders = getContainer( "Order", new AnyCoordinator(), new KeyCoordinator() );
		
		return orders;
	}
	public ContainerReference getPcSpecs() {
		if ( pcSpecs == null ) pcSpecs = getContainer( "PcSpec", new LabelCoordinator(), null );
		
		return pcSpecs;
	}
	
	private ContainerReference getContainer( String name, Coordinator oblig, Coordinator optional ) {
		try {
			// try looking up container
			return capi.lookupContainer( name, space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			// lookup failed, create container
			try {
				List<Coordinator> obligCS = oblig    == null ? null : Arrays.asList( oblig );
				List<Coordinator> optioCS = optional == null ? null : Arrays.asList( optional );
				
				return capi.createContainer( name, space, Container.UNBOUNDED, obligCS, optioCS, null);
			} catch ( MzsCoreException e1 ) {
				throw new SpaceException( e1 );
			}
		}
	}
	
	private ContainerReference  pcs;
	private ContainerReference  cpus;
	private ContainerReference  gpus;
	private ContainerReference  mbds;
	private ContainerReference  rams;
	private ContainerReference  storage;
	private ContainerReference  trash;
	private ContainerReference  orders;
	private ContainerReference  pcSpecs;
	
	private final Capi capi;
	private final URI  space;
}
