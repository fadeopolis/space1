package tu.space.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableModel;

import tu.space.components.Component;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.utils.Logger;

public class Browser {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Browser( final DataProvider data ) throws Exception {
		Logger.configure();
		
		JFrame      frame = new JFrame("dark browser");

		Box mainPanel = Box.createVerticalBox();
		frame.add( mainPanel );
		
		JPanel producers = new JPanel();

		producers.setLayout( new GridLayout( 2, 4 ) );
		
		final ComboBoxModel componentType = new DefaultComboBoxModel( 
			new Object[] {Cpu.class, Gpu.class, Mainboard.class, RamModule.class}
		);
		final SpinnerModel quotaModel     = new SpinnerNumberModel( 5,   0, Integer.MAX_VALUE, 1    );
		final SpinnerModel errorRateModel = new SpinnerNumberModel( 0.5, 0,               1.0, 0.01 );

		JComboBox jc = new JComboBox( componentType );
		jc.setRenderer( new ListCellRenderer() {
			@Override
			public java.awt.Component getListCellRendererComponent( JList list,
					Object value, int index, boolean isSelected, boolean cellHasFocus ) {
				if ( value == null) return new JLabel();
				
				return new JLabel( ((Class<?>) value).getSimpleName()  );
			}
		});
		JButton jb = new JButton( "Start producer" );
		jb.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				String uuid = UUID.randomUUID().toString().substring( 0, 8 );
				
				Class<? extends Component> type      = (Class<? extends Component>) componentType.getSelectedItem();
				int                        quota     = (Integer) quotaModel.getValue();
				double                     errorRate = (Double)  errorRateModel.getValue();
				
				data.startProducer( type.getSimpleName() + "-producer-" + uuid, type, quota, errorRate );
			}
		});

		// labels
		producers.add( new JLabel( "Type" ) );
		producers.add( new JLabel( "Quota" ) );
		producers.add( new JLabel( "Error rate" ) );
		producers.add( new JLabel( "" ) );
		// user input
		producers.add( jc );
		producers.add( new JSpinner( quotaModel ) );
		producers.add( new JSpinner( errorRateModel ) );
		producers.add( jb );		
		
		mainPanel.add( producers );
		
		JTabbedPane tabs  = new JTabbedPane();
		tabs.addTab( "Computers", makeTable( data.computers() )  );
		tabs.addTab( "CPU",       makeTable( data.cpus() )       );
		tabs.addTab( "GPU",       makeTable( data.gpus() )       );
		tabs.addTab( "Mainboard", makeTable( data.mainboards() ) );
		tabs.addTab( "RAM",       makeTable( data.ramModules() ) );
		tabs.addTab( "Storage",   makeTable( data.storage() )    );
		tabs.addTab( "Trash",     makeTable( data.trash() )      );
		
		mainPanel.add( tabs );
		
		frame.setMinimumSize( new Dimension( 640, 480 ) );
		frame.pack();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setVisible( true );
		
		frame.addWindowListener( data.windowListener() );
	}
		
	static JComponent makeTable( TableModel model ) {
		return new JScrollPane( new JTable( model ) );
	}
}
