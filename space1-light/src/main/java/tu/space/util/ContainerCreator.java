package tu.space.util;

import java.net.URI;
import java.util.Arrays;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

/**
 * This creator manages ContainerReferences
 * 
 * @author raunig stefan
 */
public abstract class ContainerCreator{
	
	public static final String STR_UNTESTED_FOR_COMPLETENESS = "UNTESTED_FOR_COMPLETENESS";
	public static final String STR_UNTESTED_FOR_DEFECT       = "UNTESTED_FOR_DEFECT";
	public static final String STR_DEFECT                    = "DEFECT";
	public static final String STR_INCOMPLETE                = "INCOMPLETE";
	public static final String STR_OK                        = "OK";
	
	public static final LabelData LABEL_UNTESTED_FOR_COMPLETENESS = label( STR_UNTESTED_FOR_COMPLETENESS );
	public static final LabelData LABEL_UNTESTED_FOR_DEFECT       = label( STR_UNTESTED_FOR_DEFECT       );
	public static final LabelData LABEL_DEFECT                    = label( STR_DEFECT                    );
	public static final LabelData LABEL_INCOMPLETE                = label( STR_INCOMPLETE                );
	public static final LabelData LABEL_OK                        = label( STR_OK                        );

	public static final LabelSelector SELECTOR_UNTESTED_FOR_COMPLETENESS = selector( STR_UNTESTED_FOR_COMPLETENESS );
	public static final LabelSelector SELECTOR_UNTESTED_FOR_DEFECT       = selector( STR_UNTESTED_FOR_DEFECT       );
	public static final LabelSelector SELECTOR_DEFECT                    = selector( STR_DEFECT                    );
	public static final LabelSelector SELECTOR_INCOMPlETE                = selector( STR_INCOMPLETE                );
	public static final LabelSelector SELECTOR_OK                        = selector( STR_OK                        );

	public static final AnySelector SELECT_1   = AnyCoordinator.newSelector( 1 );
	public static final AnySelector SELECT_2   = AnyCoordinator.newSelector( 1 );
	public static final AnySelector SELECT_4   = AnyCoordinator.newSelector( 1 );
	public static final AnySelector SELECT_MAX = AnyCoordinator.newSelector( AnySelector.COUNT_MAX );
	
	public static final int DEFAULT_TX_TIMEOUT = 5000;
	
	private static final int    DEFAULT_SPACE_PORT = 9877;
	private static final String DEFAULT_SPACE_URI = "xvsm://localhost:";
	
	public static MzsCore getCore() {
		return getCore( 0 );
	}
	public static MzsCore getCore( int port ) {
		return DefaultMzsCore.newInstance( port );
	}
	public static MzsCore getDefaultCore() {
		return getCore( DEFAULT_SPACE_PORT );
	}
	
	public static URI getSpaceURI( int port ) {
		return URI.create( DEFAULT_SPACE_URI + port );
	}
	
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
	public static ContainerReference getCpuContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "CpuContainer", new FifoCoordinator() );
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
	public static ContainerReference getGpuContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "CpuContainer", new FifoCoordinator() );
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
	public static ContainerReference getRamContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "RamCoordinator", new FifoCoordinator() );
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
	public static ContainerReference getMainboardContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "MainboardContainer", new FifoCoordinator() );
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
	public static ContainerReference getPcContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "PcContainer", new LabelCoordinator() );
	}
	
	/**
	 * This container holds defect Pc's, it will be looked up if created, else 
	 * it will be created.
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 *
	 * @throws MzsCoreException
	 */
	public static ContainerReference getPcDefectContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "PcDefectContainer", new AnyCoordinator() );
	}
	
	/**
	 * 
	 * Tis is the end storage, every pc is tagged finished
	 * 
	 * @param space
	 * @param capi
	 * @return cref
	 * 
	 * @throws MzsCoreException
	 */
	public static ContainerReference getStorageContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "Storage", new AnyCoordinator() );
	}
	
	private static ContainerReference getContainer( Capi capi, URI space, String name, Coordinator... cs ) throws MzsCoreException {
		try {
			// try looking up container
			return capi.lookupContainer("Storage", space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			// lookup failed, create container
			return capi.createContainer("Storage", space, Container.UNBOUNDED, Arrays.asList( cs ), null, null);
		}
	}

	private final static LabelData label( String label ) {
		return LabelCoordinator.newCoordinationData( label );
	}
	private final static LabelSelector selector( String label ) {
		return LabelCoordinator.newSelector( label );
	}
}
