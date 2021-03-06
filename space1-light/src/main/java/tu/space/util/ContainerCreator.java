package tu.space.util;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoData;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.KeyCoordinator.KeyData;
import org.mozartspaces.capi3.KeyCoordinator.KeySelector;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

import tu.space.components.Cpu.Type;

/**
 * This creator manages ContainerReferences
 * 
 * @author raunig stefan
 */
public abstract class ContainerCreator{
	
	public static final String STR_TESTED_FOR_DEFECT         = "TESTED_FOR_DEFECT";
	public static final String STR_UNTESTED_FOR_DEFECT       = "UNTESTED_FOR_DEFECT";
	public static final String STR_TESTED_FOR_COMPLETENESS   = "TESTED_FOR_COMPLETENESS";
	public static final String STR_UNTESTED_FOR_COMPLETENESS = "UNTESTED_FOR_COMPLETENESS";

	//CPU-Type Label
	public static final String STR_SINGLE_CORE = Type.SINGLE_CORE.name();
	public static final String STR_DUAL_CORE   = Type.DUAL_CORE.name();
	public static final String STR_QUAD_CORE   = Type.QUAD_CORE.name();
	
	public static final LabelData LABEL_SINGLE_CORE = label( STR_SINGLE_CORE );
	public static final LabelData LABEL_DUAL_CORE   = label( STR_DUAL_CORE );
	public static final LabelData LABEL_QUAD_CORE   = label( STR_QUAD_CORE );

	public static final LabelSelector SELECTOR_SINGLE_CORE = selector( STR_SINGLE_CORE );
	public static final LabelSelector SELECTOR_DUAL_CORE   = selector( STR_DUAL_CORE );
	public static final LabelSelector SELECTOR_QUAD_CORE   = selector( STR_QUAD_CORE );
	public static final List<LabelSelector> SELECTOR_ANY_CPU = Arrays.asList( SELECTOR_SINGLE_CORE, SELECTOR_DUAL_CORE, SELECTOR_QUAD_CORE );
	
	public static final LabelData LABEL_TESTED_FOR_COMPLETENESS   = label( STR_TESTED_FOR_COMPLETENESS   );
	public static final LabelData LABEL_UNTESTED_FOR_COMPLETENESS = label( STR_UNTESTED_FOR_COMPLETENESS );
	public static final LabelData LABEL_TESTED_FOR_DEFECT         = label( STR_TESTED_FOR_DEFECT         );
	public static final LabelData LABEL_UNTESTED_FOR_DEFECT       = label( STR_UNTESTED_FOR_DEFECT       );

	public static final LabelSelector SELECTOR_TESTED_FOR_COMPlETENESS   = selector( STR_TESTED_FOR_COMPLETENESS   );
	public static final LabelSelector SELECTOR_UNTESTED_FOR_COMPLETENESS = selector( STR_UNTESTED_FOR_COMPLETENESS );
	public static final LabelSelector SELECTOR_TESTED_FOR_DEFECT         = selector( STR_TESTED_FOR_DEFECT         );
	public static final LabelSelector SELECTOR_UNTESTED_FOR_DEFECT       = selector( STR_UNTESTED_FOR_DEFECT       );
	
	public static AnySelector any( int i ) {
		return AnyCoordinator.newSelector( i );
	}
	public static final AnySelector ANY_MAX = AnyCoordinator.newSelector( AnySelector.COUNT_MAX );

	public static FifoData fifo() {
		return FifoCoordinator.newCoordinationData();
	}
	public static FifoSelector fifoSel( int i ) {
		return FifoCoordinator.newSelector( i );
	}
		
	public static final FifoSelector FIFO_MAX = FifoCoordinator.newSelector( FifoSelector.COUNT_MAX );
	
	public static final int DEFAULT_TX_TIMEOUT = 300000000; // set very high for debugging
	
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
	
	public static OrderManager getOrders( URI space, Capi capi ) throws MzsCoreException {
		return new OrderManager( space, capi );
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
		return getContainer( capi, space, "CpuContainer", new LabelCoordinator() );
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
		return getContainer( capi, space, "GpuContainer", new AnyCoordinator() );
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
		return getContainer( capi, space, "RamCoordinator", new AnyCoordinator() );
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
	 * Storage for orders, as every factory has to carry out orders
	 * 
	 * @param space
	 * @param capi
	 * @return
	 * @throws MzsCoreException
	 */
	public static ContainerReference getOrderContainer( final URI space, final Capi capi ) throws MzsCoreException{
		return getContainer( capi, space, "OrderContainer", new KeyCoordinator() );
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
	public static ContainerReference getTrashContainer( final URI space, final Capi capi ) throws MzsCoreException{
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

	public static ContainerReference getOrderIdContainer( final URI space, final Capi capi ) throws MzsCoreException {
		return getContainer( capi, space, "Order.id", new VectorCoordinator() );
	}
	
	public static ContainerReference getContainer( Capi capi, URI space, String name, Coordinator... cs ) throws MzsCoreException {
		try {
			// try looking up container
			return capi.lookupContainer( name, space, RequestTimeout.DEFAULT, null);
		} catch (MzsCoreException e) {
			// lookup failed, create container
			return capi.createContainer( name, space, Container.UNBOUNDED, Arrays.asList( cs ), null, null);
		}
	}

	public final static LabelData label( String label ) {
		return LabelCoordinator.newCoordinationData( label );
	}
	public final static LabelSelector selector( String label ) {
		return LabelCoordinator.newSelector( label );
	}

	public final static KeyData key( String label ) {
		return KeyCoordinator.newCoordinationData( label );
	}
	public final static KeySelector keySel( String label ) {
		return KeyCoordinator.newSelector( label );
	}

	public final static List<Selector> selectors( Selector... s ) {
		return Arrays.asList( s );
	}
	
	/**
	 * Retrun the corresponding labeldata for cpu type
	 * 
	 * @param cpuType
	 * @return Label for 
	 * 		SINGEL_CORE
	 * 		DUAL_CORE
	 * 		QUAD_CORE
	 */
	public static LabelData labelForCpuType( final Type cpuType ){
		if(cpuType.equals( Type.SINGLE_CORE) ){ 
					return label( STR_SINGLE_CORE );
		} else if (cpuType.equals( Type.DUAL_CORE) ){
					return label( STR_DUAL_CORE );
		} else if (cpuType.equals( Type.QUAD_CORE) ){
				return label( STR_QUAD_CORE );					
		} else return null;
	}
}
