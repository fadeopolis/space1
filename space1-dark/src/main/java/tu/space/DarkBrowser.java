package tu.space;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;

import tu.space.middleware.JMSMiddlewareFactory;
import tu.space.middleware.JMSMiddleware;
import tu.space.utils.Logger;

public class DarkBrowser {
	public static void main( String... args ) {
		Logger.configure();
		
		JFrame frame = new JFrame("dark browser");
		
		JList list = new JList( list() );
		
		frame.add( list );
		
		frame.setSize( new Dimension( 640, 480 ) );
		frame.pack();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setVisible( true );
	}
	
	static ListModel list() {
		return new AbstractListModel() {			
			private final List<Object> data = new LinkedList<Object>();

			{
				JMSMiddleware m = new JMSMiddlewareFactory().make();
				m.start();
				
				for ( Object o : m.cpus().browse() )       data.add( o );
				for ( Object o : m.gpus().browse() )       data.add( o );
				for ( Object o : m.mainboards().browse() ) data.add( o );
				for ( Object o : m.ramModules().browse() ) data.add( o );
				
				m.stop();
			}
			
			@Override
			public int getSize() {
				return data.size();
			}
			
			@Override
			public Object getElementAt( int index ) {
				return data.get( index );
			}
		};
	}
}
