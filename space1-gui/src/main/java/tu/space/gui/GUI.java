package tu.space.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Cpu.Type;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamList;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;
import tu.space.middleware.Middleware.Operation;
import tu.space.middleware.OrderItemListener;
import tu.space.utils.Logger;
import tu.space.worker.Producer;

public class GUI {
	public GUI( final Middleware mw ) throws Exception {
		Logger.configure();
		
		JFrame frame = new JFrame("Factory Browser");

		Box mainPanel = Box.createVerticalBox();
		frame.add( mainPanel );
		
		JPanel producers = new JPanel();

		producers.setLayout( new GridLayout( 4, 5 ) );
		
		//Order area
		final JComboBox<Cpu.Type> jcOrder    = new JComboBox<Cpu.Type>( Cpu.Type.values() );
		final SpinnerModel        smPcAmount = new SpinnerNumberModel( 5, 0, Integer.MAX_VALUE, 1 );
		final SpinnerModel        smRam      = new SpinnerNumberModel( 2, 1,                 4, 1 );
		
		//gpu
		final JCheckBox cbGpu = new JCheckBox("Gpu");
		cbGpu.setSelected(true);
		
		JButton jbOrder = new JButton( "Place order" );
		jbOrder.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				Type type = (Type) jcOrder.getSelectedItem();
				int quota = (Integer) smPcAmount.getValue();
				int ramAmount = (Integer) smRam.getValue();
				boolean gpu = (Boolean) cbGpu.isSelected();
				
				mw.placeOrder(type, ramAmount, gpu, quota);
			}
		});
		
		//Producer area
		final JComboBox<Component.Type> jc             = new JComboBox<Component.Type>( Component.Type.values() );
		final SpinnerModel              quotaModel     = new SpinnerNumberModel( 5,   0, Integer.MAX_VALUE, 1    );
		final SpinnerModel              errorRateModel = new SpinnerNumberModel( 0.5, 0,               1.0, 0.01 );

		JButton jb = new JButton( "Start producer" );
		jb.addActionListener( new ActionListener() {
			@Override
			@SuppressWarnings("rawtypes")
			public void actionPerformed( ActionEvent e ) {
				Component.Type type      = (Component.Type) jc.getSelectedItem();
				int            quota     = (Integer)        quotaModel.getValue();
				double         errorRate = (Double)         errorRateModel.getValue();
				
				new Thread( new Producer( mw.genId(), mw, type, quota, errorRate ) ).start();
			}
		});

		// labels for order
		producers.add( new JLabel( "Cpu-Type" ) );
		producers.add( new JLabel( "PC-Quanitiy" ) );
		producers.add( new JLabel( "Ram-Quantity" ) );
		producers.add( new JLabel( "" ) );
		producers.add( new JLabel( "" ) );
		
		// user input order
		producers.add( jcOrder );
		producers.add( new JSpinner( smPcAmount ) );
		producers.add( new JSpinner( smRam ) );
		producers.add( cbGpu );
		producers.add( jbOrder );
		
		//labels for producer
		producers.add( new JLabel( "Type" ) );
		producers.add( new JLabel( "Quota" ) );
		producers.add( new JLabel( "Error rate" ) );
		producers.add( new JLabel( "" ) );
		producers.add( new JLabel( "" ) );
		
		// user input producer
		producers.add( jc );
		producers.add( new JSpinner( quotaModel ) );
		producers.add( new JSpinner( errorRateModel ) );
		producers.add( new JLabel( "" ) );
		producers.add( jb );
		
		mainPanel.add( producers );
		
		JTabbedPane tabs  = new JTabbedPane();
		tabs.addTab( "Orders",    makeTable( new OrderTable( mw ) )    );
		tabs.addTab( "Computers", makeTable( mw, Computer.class )      );
		tabs.addTab( "CPU",       makeTable( mw, Cpu.class       )     );
		tabs.addTab( "GPU",       makeTable( mw, Gpu.class       )     );
		tabs.addTab( "Mainboard", makeTable( mw, Mainboard.class )     );
		tabs.addTab( "RAM",       makeTable( mw, RamModule.class )     );
		tabs.addTab( "Storage",   makeTable( new StorageTable( mw ) )  );
		tabs.addTab( "Trash",     makeTable( new TrashTable( mw ) )    );
		
		mainPanel.add( tabs );
		
		detailView = new JTable();

		JScrollPane scroller = new JScrollPane( detailView );
		scroller.setPreferredSize( new Dimension( 640, 100 ) );
		
		mainPanel.add( scroller );
		
		frame.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosed( WindowEvent e ) {
				mw.shutdown();
			}
		} );

		frame.setPreferredSize( new Dimension( 640, 480 ) );
		frame.pack();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setVisible( true );
	}

	private final JTable detailView;
	
	private <P extends Product> JComponent makeTable( final Middleware mw, final Class<P> c ) {
		return makeTable( new GUITableModel<P>( mw, c ) );
	}
	private <P extends Product> JComponent makeTable( TableModel model ) {
		final JTable table = new JTable( model );
		
		ListSelectionListener l = new ListSelectionListener() {
			private int row = -1, col = -1;
			
			@Override
			public void valueChanged( ListSelectionEvent e ) {
				row = table.getSelectedRow();
				col = table.getSelectedColumn();
				
				if ( row < 0 || col < 0 ) return;
				
				Object o = table.getValueAt( row, col );
				
				if ( o instanceof RamList ) {
					detailView.setModel( new RamListViewerModel( (RamList) o ) );
				} else if ( o instanceof Product ) {
					detailView.setModel( new ProductViewerModel( (Product) o ) );
				} else {
					detailView.setModel( new StringViewerModel( String.valueOf( o ) ) );
				}
				
				detailView.validate();
				detailView.invalidate();
			}
		};
		table.getSelectionModel().addListSelectionListener( l );
		table.getColumnModel().getSelectionModel().addListSelectionListener( l );
		
		JScrollPane scroller = new JScrollPane( table );
		scroller.setPreferredSize( new Dimension( 640, 250 ) );
		
		return scroller;
	}

	static class GUITableModel<P extends Product> extends AbstractTableModel {
		protected final List<P>  data;
		protected final Class<P> type;
		protected final Field[]  fields;
			
		public GUITableModel( Middleware mw, Class<P> c ){
			data   = new Vector<P>();
			fields = c.getFields();
			type   = c;
			
			registerListeners( mw );
		}
		
		protected void registerListeners( Middleware mw ) {
			mw.registerListener( type, Operation.CREATED, new Listener<P>() {
				@Override public synchronized void onEvent( P p ) { onElemCreated( p ); }
			});
			mw.registerListener( type, Operation.REMOVED, new Listener<P>() {
				@Override public synchronized void onEvent( P p ) { onElemRemoved( p ); }
			});
		}
			
		protected void onElemCreated( P p ) {
			synchronized ( data ) {
				if ( !data.contains( p ) ) data.add( p );				
			}
			GUITableModel.this.fireTableRowsInserted( 0, getRowCount() );			
		}
		protected void onElemRemoved( P p ) {
			data.remove( p );			
			GUITableModel.this.fireTableRowsDeleted( 0, getRowCount() );
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
		public String getColumnName( int columnIndex ) {
			if ( columnIndex == 0 ) return "#";
			
			return fields[columnIndex - 1].getName();
		}

		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			if ( columnIndex == 0 ) return rowIndex + 1;
			columnIndex--;
			
			if ( rowIndex    < 0 || rowIndex    >= data.size()   ) return null;
			if ( columnIndex < 0 || columnIndex >= fields.length ) return null;

			try {
				Object o = fields[columnIndex].get( data.get( rowIndex ) );

				return o;
//				if ( o instanceof Product ) {
//					return ((Product) o).bareToString();
//				} else {
//					return o;
//				}
			} catch ( IllegalArgumentException e ) {
				return "ERROR: " + e;
			} catch ( IllegalAccessException e ) {
				return "ERROR: " + e;
			}			
		}
	}
		
	static class OrderTable extends GUITableModel<Order> {

		public OrderTable( Middleware mw ) {
			super( mw, Order.class );
		}
	
		protected void registerListeners( Middleware mw ) {
			mw.registerOrderListener( Operation.CREATED, new Listener<Order>() {
				@Override public synchronized void onEvent( Order p ) { onElemCreated( p ); }
			});
			mw.registerOrderListener( Operation.REMOVED, new Listener<Order>() {
				@Override public synchronized void onEvent( Order p ) { onElemRemoved( p ); }
			});
			
			mw.setOrderItemListener( new OrderItemListener() {
				@Override
				public void onOrderItemProduced( String orderId ) {
					synchronized ( data ) {
						for ( int i = 0; i < data.size(); i++ ) {
							Order o = data.get( 0 );
						
							if ( o.id.equals( orderId ) ) data.set( i, o.incProduced() );
						}
						OrderTable.this.fireTableRowsUpdated( 0, data.size() );
					}
				}
				
				@Override
				public void onOrderItemFinished( String orderId ) {
					for ( int i = 0; i < data.size(); i++ ) {
						Order o = data.get( 0 );
					
						if ( o.id.equals( orderId ) ) data.set( i, o.incFinished() );
					}
					OrderTable.this.fireTableRowsUpdated( 0, data.size() );
				}
				
				@Override
				public void onOrderItemDefect( String orderId ) {
					for ( int i = 0; i < data.size(); i++ ) {
						Order o = data.get( 0 );
					
						if ( o.id.equals( orderId ) ) data.set( i, o.decProduced() );
					}
					OrderTable.this.fireTableRowsUpdated( 0, data.size() );
				}
			});
		}
	}	

	static class StorageTable extends GUITableModel<Computer> {

		public StorageTable( Middleware mw ) {
			super( mw, Computer.class );
		}

		@Override
		protected void registerListeners( Middleware mw ) {
			mw.registerStorageListener( Operation.CREATED, new Listener<Computer>() {
				@Override public synchronized void onEvent( Computer p ) { 
					onElemCreated( p ); }
			});
			mw.registerStorageListener( Operation.REMOVED, new Listener<Computer>() {
				@Override public synchronized void onEvent( Computer p ) { onElemRemoved( p ); }
			});
		}	
	}
	
	static class TrashTable extends GUITableModel<Product> {
		public TrashTable( Middleware mw ) {
			super( mw, Product.class );
		}
		
		@Override
		protected void registerListeners( Middleware mw ) {
			mw.registerTrashListener( Operation.CREATED, new Listener<Product>() {
				@Override public synchronized void onEvent( Product p ) { onElemCreated( p ); }
			});
			mw.registerTrashListener( Operation.REMOVED, new Listener<Product>() {
				@Override public synchronized void onEvent( Product p ) { onElemRemoved( p ); }
			});
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName( int columnIndex ) {
			switch ( columnIndex ) {
				case 0:  return "#";
				case 1:  return "TYPE";
				case 2:  return "VALUE";
				default: return "";
			}
		}

		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			switch ( columnIndex ) {
				case 0:  return rowIndex + 1 ;
				case 1:  return data.get( rowIndex ).getClass().getSimpleName();
				case 2:  return data.get( rowIndex );
				default: return null;
			}
		}
	}
	
	private static class StringViewerModel extends AbstractTableModel {
		public StringViewerModel( String str ) {
			this.str = str;
		}
		
		@Override
		public String getColumnName(int column) {
			return "VALUE";
		}
		
		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			return str;
		}
		
		private final String str;
	}
	private static class RamListViewerModel extends AbstractTableModel {
		public RamListViewerModel( RamList ram ) {
			this.ram = ram;
		}
		
		@Override
		public String getColumnName(int column) {
			if ( column == 0 ) return "TYPE";
			column--;

			return fields[column].getName();
		}

		@Override
		public int getRowCount() {
			return ram.size();
		}

		@Override
		public int getColumnCount() {
			return fields.length + 1;
		}

		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			if ( columnIndex == 0 ) return ram.getClass().getSimpleName();
			
			columnIndex--;
			
			try {
				return fields[columnIndex].get( ram.get( rowIndex ) );
			} catch ( IllegalArgumentException e ) {
				return "ERROR: " + e;
			} catch ( IllegalAccessException e ) {
				return "ERROR: " + e;
			}
		}
		
		private final RamList ram;
		static final Field[] fields = RamModule.class.getFields();
	}
	private static class ProductViewerModel extends AbstractTableModel {
		public ProductViewerModel( Product p ) {
			this.p      = p;
			this.fields = p.getClass().getFields();
		}
		
		@Override
		public String getColumnName(int column) {
			if ( column == 0 ) return "TYPE";
			column--;
			
			return fields[column].getName();
		}
		
		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount() {
			return fields.length + 1;
		}

		@Override
		public Object getValueAt( int rowIndex, int columnIndex ) {
			if ( columnIndex == 0 ) return p.getClass().getSimpleName();
			
			columnIndex--;
			
			try {
				return fields[columnIndex].get( p );
			} catch ( IllegalArgumentException e ) {
				return "ERROR: " + e;
			} catch ( IllegalAccessException e ) {
				return "ERROR: " + e;
			}
		}
		
		private final Product p;
		private final Field[] fields;
	}
}
