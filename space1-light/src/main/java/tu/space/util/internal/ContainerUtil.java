package tu.space.util.internal;

import java.io.Serializable;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;

public class ContainerUtil<E extends Serializable> {
	public ContainerUtil( Capi capi, ContainerReference cref ) throws MzsCoreException {
		read   = new ContainerReader<E>( capi, cref );
		take   = new ContainerTaker<E>( capi, cref );
		write  = new ContainerWriter<E>( capi, cref );
		delete = new ContainerDeleter<E>( capi, cref );
	}

	public final ContainerReader<E>  read;
	public final ContainerTaker<E>   take;
	public final ContainerWriter<E>  write;
	public final ContainerDeleter<E> delete;
}
