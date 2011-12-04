package tu.space.gui;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import tu.space.utils.Logger;

public class Browser {
	public Browser( DataProvider data ) throws Exception {
		Logger.configure();
		
		JFrame      frame = new JFrame("dark browser");
		JTabbedPane tabs  = new JTabbedPane();
		
		tabs.addTab( "Computers", makeTable( data.computers() )  );
		tabs.addTab( "CPU",       makeTable( data.cpus() )       );
		tabs.addTab( "GPU",       makeTable( data.gpus() )       );
		tabs.addTab( "Mainboard", makeTable( data.mainboards() ) );
		tabs.addTab( "RAM",       makeTable( data.ramModules() ) );
		tabs.addTab( "Storage",   makeTable( data.storage() )    );
		tabs.addTab( "Trash",     makeTable( data.trash() )      );
		
		frame.add( tabs );
		
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
