package tu.space.worker;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.gui.DataProvider;
import tu.space.util.ContainerCreator;
import tu.space.utils.SpaceException;

public class SpaceDataProvider implements DataProvider {
	public SpaceDataProvider() {
		
	}
	
	@Override
	public TableModel cpus() throws Exception {
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi    capi = new Capi(core);
		URI     space = URI.create("xvsm://localhost:9877");			

		ContainerReference cref = ContainerCreator.getCpuContainer( space, capi );
		
		return new SpaceTableModel(Cpu.class, core, cref );
	}

	@Override
	public TableModel gpus() throws Exception{
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi    capi = new Capi(core);
		URI     space = URI.create("xvsm://localhost:9877");			

		ContainerReference cref = ContainerCreator.getGpuContainer( space, capi );
		
		return new SpaceTableModel(Gpu.class, core, cref );
	}

	@Override
	public TableModel mainboards() throws Exception {
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi    capi = new Capi(core);
		URI     space = URI.create("xvsm://localhost:9877");			

		ContainerReference cref = ContainerCreator.getMainboardContainer( space, capi );
		
		return new SpaceTableModel(Mainboard.class, core, cref );

	}

	@Override
	public TableModel ramModules() throws Exception {
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi    capi = new Capi(core);
		URI     space = URI.create("xvsm://localhost:9877");			

		ContainerReference cref = ContainerCreator.getRamContainer( space, capi );
		
		return new SpaceTableModel(RamModule.class, core, cref );
	}

	@Override
	public TableModel computers() throws Exception {
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi    capi = new Capi(core);
		URI     space = URI.create("xvsm://localhost:9877");			

		ContainerReference cref = ContainerCreator.getPcContainer( space, capi );
		
		return new SpaceTableModel(Computer.class, core, cref );
	}

	@Override
	public TableModel storage() throws Exception {
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi    capi = new Capi(core);
		URI     space = URI.create("xvsm://localhost:9877");			

		ContainerReference cref = ContainerCreator.getStorageContainer( space, capi );
		
		return new SpaceTableModel(Computer.class, core, cref );
	}

	@Override
	public TableModel trash() throws Exception {
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi    capi = new Capi(core);
		URI     space = URI.create("xvsm://localhost:9877");			

		ContainerReference cref = ContainerCreator.getPcDefectContainer( space, capi );
		
		return new SpaceTableModel(Computer.class, core, cref );
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
	
	@Override
	public void startProducer( String id, Class<? extends Component> type, int quota, double errorRate ) {
		try {
			String msg;
			if(type.getSimpleName().toLowerCase().equals("rammodule")){
				msg = "ram";
			} else {
				msg = type.getSimpleName().toLowerCase();
			}
			SpaceProducer.make(id, errorRate, msg, quota).start();
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
					Serializable s = entries.get( 0 );
					
					if ( s instanceof Entry )
						s = ((Entry) s).getValue(); 
					
					data.add( s );
					SpaceTableModel.this.fireTableRowsInserted( 0, getRowCount() );
					
					}
				},
				Operation.WRITE
			);
			nManager.createNotification( cref, new NotificationListener() {
				@Override
				public void entryOperationFinished( Notification source,
						Operation operation, List<? extends Serializable> entries ) {
					Serializable s = entries.get( 0 );
					
					if ( s instanceof Entry )
						s = ((Entry) s).getValue(); 
					
					data.remove( s );
					SpaceTableModel.this.fireTableRowsDeleted( 0, getRowCount() );
				}
				},
				Operation.TAKE
			);
			
			//read all
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

		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			try {
				if ( columnIndex == 0 ) return rowIndex;
				columnIndex--;
				
				if ( rowIndex    < 0 || rowIndex    >= data.size()   ) return null;
				if ( columnIndex < 0 || columnIndex >= fields.length ) return null;
				
				return fields[columnIndex].get( data.get( rowIndex ) );
			} catch ( IllegalArgumentException e ) {
				return "ERROR: " + e;
			} catch ( IllegalAccessException e ) {
				return "ERROR: " + e;
			}
		}
		
		private final Field[]        fields;
		private final Vector<Object> data;
	}
}
