package tu.space.provider;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Product;
import tu.space.components.Cpu.Type;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.gui.DataProvider;
import tu.space.util.ContainerCreator;
import tu.space.util.CpuManager;
import tu.space.util.GpuManager;
import tu.space.util.MainboardManager;
import tu.space.util.ProductManager;
import tu.space.util.RamManager;
import tu.space.utils.UUIDGenerator;

import tu.space.light.Producer;

public class SpaceDataProvider implements DataProvider {
	
	private final int     port;
	private final MzsCore core  = DefaultMzsCore.newInstance(0);
	private final Capi    capi  = new Capi(core);
	private final URI     space;	
	
	/**
	 * Constructor for bsp1 default on port 9877
	 */
	public SpaceDataProvider() {
		this.port = 9877;
		space = URI.create("xvsm://localhost:" + port);
	}
	
	/**
	 * Constructor
	 * 
	 * @param factoryId
	 * @param factorySpace
	 */
	public SpaceDataProvider(final String factoryId, final int port, final String factorySpace){
		this.port = port;
		space = URI.create(factorySpace);
	}
	
	@Override
	public TableModel cpus() throws Exception {
		ContainerReference cref = ContainerCreator.getCpuContainer( space, capi );
		return new SpaceTableModel(Cpu.class, core, cref );
	}

	@Override
	public TableModel gpus() throws Exception{
		ContainerReference cref = ContainerCreator.getGpuContainer( space, capi );
		return new SpaceTableModel(Gpu.class, core, cref );
	}

	@Override
	public TableModel mainboards() throws Exception {
		ContainerReference cref = ContainerCreator.getMainboardContainer( space, capi );	
		return new SpaceTableModel(Mainboard.class, core, cref );
	}

	@Override
	public TableModel ramModules() throws Exception {
		ContainerReference cref = ContainerCreator.getRamContainer( space, capi );
		return new SpaceTableModel(RamModule.class, core, cref );
	}

	@Override
	public TableModel computers() throws Exception {
		ContainerReference cref = ContainerCreator.getPcContainer( space, capi );
		return new SpaceTableModel(Computer.class, core, cref );
	}

	@Override
	public TableModel storage() throws Exception {
		return new SpaceTableModel(Computer.class, core, ContainerCreator.getStorageContainer( space, capi ) );
	}

	@Override
	public TableModel trash() throws Exception {
		ContainerReference cref = ContainerCreator.getTrashContainer( space, capi );	
		return new SpaceTableModel(Computer.class, core, cref );
	}

	@Override
	public TableModel orders() throws Exception {
		return new SpaceTableModel(Order.class, core, ContainerCreator.getOrderContainer( space, capi ) );
	}
	
	@Override
	public WindowListener windowListener() {
		return new WindowAdapter() {
			@Override
			public void windowClosed( WindowEvent e ) {
				//close server
			}
		};
	}
	
	/**
	 * A order to be sended to space ^^
	 */
	@Override
	public void placeOrder(Type cpuType, int ramAmount, boolean gpu, int quanitity) {
		try {
			String uuid = uuids.generate();
			
			ContainerCreator.getOrders( space, capi ).publishOrder( 
				new Order( uuid, cpuType, ramAmount, gpu, quanitity ),
				null
			);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void startProducer( String id, Class<? extends Component> type, int quota, double errorRate ) {
		try {
			ProductManager p = null;
			if ( type == Cpu.class ) {
				p = new CpuManager( space, capi );
			} else if ( type == Gpu.class ) {
				p = new GpuManager( space, capi );
			} else if ( type == Mainboard.class ) {
				p = new MainboardManager( space, capi );
			} else if ( type == RamModule.class ) {
				p = new RamManager( space, capi );
			}
			
			new Thread(
				new Producer( id, capi, port, quota, errorRate, Component.makeFactory( type ), p ) 
			).start();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("serial")
	private class SpaceTableModel extends AbstractTableModel {
		public SpaceTableModel( Class<?> clazz, MzsCore core, ContainerReference cref ) throws Exception {
			this.fields = clazz.getFields();
			this.data   = new Vector<Object>();
						
			NotificationManager nManager = new NotificationManager(core);
						
			nManager.createNotification( cref, new NotificationListener() {
				@Override
				public void entryOperationFinished( Notification source,
						Operation operation, List<? extends Serializable> entries ) {
					
					for(Serializable comp: entries){
						if ( comp instanceof Entry ){
							comp = ((Entry) comp).getValue(); 
						}
					
						data.add( comp );
						SpaceTableModel.this.fireTableRowsInserted( 0, getRowCount() );
						}
					}
				}
			
			, Operation.WRITE
			);
			
			nManager.createNotification( cref, new NotificationListener() {
				@Override
				public void entryOperationFinished( Notification source,
						Operation operation, List<? extends Serializable> entries ) {
					
					for(Serializable comp: entries){
					
						if ( comp instanceof Entry ) comp = ((Entry) comp).getValue(); 
						
						if ( comp instanceof Order ) {
							synchronized ( data ) {
								Iterator<Object> it = data.iterator();
								while ( it.hasNext() ) {
									Object o = it.next();
									if ( o instanceof Order ) {
										Order o1 = (Order) comp;
										Order o2 = (Order) o;
										
										if ( o1.id.equals( o2.id ) ) it.remove();
									}
								}
							}
						}
						
						data.remove( comp );
						SpaceTableModel.this.fireTableRowsDeleted( 0, getRowCount() );
					}
				}
			}
			, Operation.TAKE, Operation.DELETE
			);
		}
		
		@Override
	    public String getColumnName( int column ) {
			if ( column == 0 ) return "Index";
			column--;
			
			return fields[column].getName();
	    }
		
		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return fields.length + 1;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			try {
				if ( columnIndex == 0 ) return rowIndex;
				columnIndex--;
				
				if ( rowIndex    < 0 || rowIndex    >= data.size()   ) return null;
				if ( columnIndex < 0 || columnIndex >= fields.length ) return null;
				
				Object o = fields[columnIndex].get( data.get( rowIndex ) );
				
				if ( o == null ) return o;
				
				if ( o instanceof Product ) 
					return ((Product) o).bareToString();
				else if ( o instanceof List ) {
					@SuppressWarnings("rawtypes")
					List<Product> l = (List) o;
					if ( l.isEmpty() ) return l;
					if ( !(l.get( 0 ) instanceof Product) ) return l;

					String str = "";
					for ( Product p : l ) str += p.bareToString() + "; ";
					return str;
				} else
					return o;
			} catch ( IllegalArgumentException e ) {
				return "ERROR: " + e;
			} catch ( IllegalAccessException e ) {
				return "ERROR: " + e;
			}
		}
		
		private final Field[]      fields;
		private final List<Object> data;
	}

	private final UUIDGenerator uuids = new UUIDGenerator();
}
