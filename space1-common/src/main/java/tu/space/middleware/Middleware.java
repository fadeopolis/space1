package tu.space.middleware;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamModule;
import tu.space.contracts.Order;
import tu.space.contracts.PcSpec;

public interface Middleware {
	public enum Operation {
		CREATED, REMOVED;
	}
	
	void beginTransaction();
	void commitTransaction();
	void rollbackTransaction();
	
	// use this to find out when orders are published, finished
	void registerOrderListener( Operation o, Listener<Order> l );
	// use this to find out when pcs are finished
	void registerStorageListener( Operation o, Listener<Computer> l );
	// use this to find out when stuff is trashed
	void registerTrashListener( Operation o, Listener<Product> l );

	// use this to find out when stuff is produced
//	void registerComponentListener( Operation o, Listener<Component> l );
//	void registerComputerListener( Operation o, Listener<Computer> l );
//
//	void registerListenerForComputersUntestedForDefect( Operation o, Listener<Computer> l );
//	void registerListenerForComputersUntestedForCompleteness( Operation o, Listener<Computer> l );
//	
//	void registerTestedComputerListener( Operation o, Listener<Computer> l );
	
	<P extends Product> void registerListener( Class<P> c, Operation o, Listener<P> l );

	// use this to find out when items for an order are produced/marked as broken/finished
	void setOrderItemListener( OrderItemListener l );
	
	void signalPcForOrderDefect( Computer c );
	void signalOrderIsDone( Order o );
	
	Iterable<Input<PcSpec>> orders();
	
	Input<Computer>  getComputerInput();
	Input<Computer>  getComputersUntestedForDefect();
	Input<Computer>  getComputersUntestedForCompleteness();
	Input<Computer>  getTestedComputers();
	
	Output<Computer> getComputerOutput();
	
	CpuInput         getCpuInput();
	Input<Gpu>       getGpuInput();
	Input<Mainboard> getMainboardInput();
	RamInput         getRamInput();

	Output<Cpu>       getCpuOutput();
	Output<Gpu>       getGpuOutput();
	Output<Mainboard> getMainboardOutput();
	Output<RamModule> getRamOutput();

	Output<Computer> getStorage();
	Output<Product>  getTrash();

	String genId();
	
	void placeOrder( Cpu.Type cpuType, int ramAmount, boolean gpu, int quanitity );
	
	void shutdown();
}
