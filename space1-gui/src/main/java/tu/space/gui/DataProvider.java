package tu.space.gui;

import java.awt.event.WindowListener;

import javax.swing.table.TableModel;

import tu.space.components.Component;
import tu.space.components.Cpu.Type;

public interface DataProvider {
	TableModel orders()		throws Exception;
	TableModel cpus()       throws Exception;
	TableModel gpus()       throws Exception;
	TableModel mainboards() throws Exception;
	TableModel ramModules() throws Exception;
	TableModel computers()  throws Exception;
	TableModel storage()    throws Exception;
	TableModel trash()      throws Exception;
	
	WindowListener windowListener();
	
	void startProducer( String id, Class<? extends Component> type, int quota, double errorRate );
	void placeOrder( final Type cpuType, final int ramAmount, final boolean gpu, final int quanitity );
}
