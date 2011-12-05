package tu.space.worker;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.gui.DataProvider;

public class SpaceDataProvider implements DataProvider {
	public SpaceDataProvider() {
		
	}
	
	@Override
	public TableModel cpus() throws Exception {
		return new SpaceTableModel(Cpu.class);
	}

	@Override
	public TableModel gpus() throws Exception{
		return new SpaceTableModel(Gpu.class);
	}

	@Override
	public TableModel mainboards() throws Exception {
		return new SpaceTableModel(Mainboard.class);
	}

	@Override
	public TableModel ramModules() throws Exception {
		return new SpaceTableModel(RamModule.class, "ram", null);
	}

	@Override
	public TableModel computers() throws Exception {
		return new SpaceTableModel(Computer.class);
	}

	@Override
	public TableModel storage() throws Exception {
		return new SpaceTableModel(Computer.class, null);
	}

	@Override
	public TableModel trash() throws Exception {
		return new SpaceTableModel(Computer.class, null);

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
			new SpaceProducer(id, errorRate, msg, quota).start();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("serial")
	private class SpaceTableModel extends AbstractTableModel {
		public SpaceTableModel( Class<?> clazz ) throws Exception {
			this( clazz, null );
		}
		public SpaceTableModel( Class<?> clazz, String selector ) throws Exception {
			this( clazz, clazz.getSimpleName().toLowerCase(), selector );
		}
		public SpaceTableModel( Class<?> clazz, String name, String selector ) throws Exception {
			this.fields = clazz.getFields();
			this.data   = new Vector<Object>();
						
			//read all
			
			new SpaceEventListener() {
				@Override
				public void getAll(List<Object> objs){
					for(Object obj: objs){
						data.add(obj);		
					}
					SpaceTableModel.this.fireTableStructureChanged();
				}
				
				@Override
				public void onRemoved(Object obj) {
					data.remove(0);
					SpaceTableModel.this.fireTableStructureChanged();
				}
				
				@Override
				public void onCreated(Object obj) {
					data.add(obj);
					SpaceTableModel.this.fireTableStructureChanged();
				}
			};
		}
		
		@Override
	    public String getColumnName( int column ) {
			return fields[column].getName();
	    }
		
		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return fields.length;
		}

		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			try {
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
