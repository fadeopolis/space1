package tu.space.gui;

import java.awt.event.WindowListener;

import javax.swing.table.TableModel;

public interface DataProvider {
	TableModel cpus()       throws Exception;
	TableModel gpus()       throws Exception;
	TableModel mainboards() throws Exception;
	TableModel ramModules() throws Exception;
	TableModel computers()  throws Exception;
	TableModel storage()    throws Exception;
	TableModel trash()      throws Exception;
	
	WindowListener windowListener();
}
