package tu.space;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Vector;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.gui.DataProvider;

public class DarkDataProvider implements DataProvider {
	public DarkDataProvider() throws JMSException {
		conn = JMS.openConnection();
		
		conn.start();
	}
	
	@Override
	public TableModel cpus() throws Exception {
		return new DarkTableModel( Cpu.class );
	}

	@Override
	public TableModel gpus() throws JMSException {
		return new DarkTableModel( Gpu.class );
	}

	@Override
	public TableModel mainboards() throws JMSException {
		return new DarkTableModel( Gpu.class );
	}

	@Override
	public TableModel ramModules() throws JMSException {
		return new DarkTableModel( Mainboard.class );
	}

	@Override
	public TableModel computers() throws JMSException {
		return new DarkTableModel( Computer.class );
	}

	@Override
	public TableModel storage() throws JMSException {
		return new DarkTableModel( Computer.class, DarkLogistician.SELECTOR );
	}

	@Override
	public TableModel trash() throws JMSException {
		return new DarkTableModel( Computer.class, "NOT (" + DarkLogistician.SELECTOR + ")" );
	}

	@Override
	public WindowListener windowListener() {
		return new WindowAdapter() {
			@Override
			public void windowClosed( WindowEvent e ) {
				JMS.close( conn );
			}
		};
	}
	
	private final Connection conn;

	private class DarkTableModel extends AbstractTableModel {
		public DarkTableModel( Class<?> clazz ) throws JMSException {
			this( clazz, null );
		}
		public DarkTableModel( Class<?> clazz, String selector ) throws JMSException {
			this.fields = clazz.getFields();
			this.data   = new Vector<Object>();

			final Session sess = JMS.createSessionWithoutTransactions( conn );
			
			String name = clazz.getSimpleName().toLowerCase();
			
			Queue queue = sess.createQueue( name );
			
			@SuppressWarnings("unchecked")
			Enumeration<ObjectMessage> browser = sess.createBrowser( queue, selector ).getEnumeration();
			while ( browser.hasMoreElements() ) {
				this.data.add( browser.nextElement().getObject() );
			}
			
			new JMSTopicListener( sess, name, selector ) {
				@Override
				public void onRemoved( Object o, Message msg ) {
					data.remove( 0 );
					DarkTableModel.this.fireTableStructureChanged();
				}
				
				@Override
				public void onCreated( Object o, Message msg ) {
					data.add( o );
					DarkTableModel.this.fireTableStructureChanged();
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
