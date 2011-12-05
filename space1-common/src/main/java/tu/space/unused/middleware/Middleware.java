package tu.space.unused.middleware;

import java.io.Serializable;
import java.util.UUID;

import tu.space.components.Component;
import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;

public interface Middleware {
	UUID generateId();

	void beginTransaction();
	void commit();
	void rollback();

	// producers write to these
	Category<Cpu>       cpus();
	Category<Gpu>       gpus();
	Category<Mainboard> mainboards();
	Category<RamModule> ramModules();
	// manufacturers write to these
	Category<Computer>  allComputers();
	// testers read from these
	Category<Computer>  computersUntestedForDefect();
	Category<Computer>  computersUntestedForCompleteness();
	// logisticians read/write from/to these
	Category<Computer>  testedComputers();
	Category<Computer>  storage();
	Category<Computer>  trash();
	
	Iterable<Category<Serializable>> allCategories();

	void send( Component c );
	
	public void start();
	public void stop();
}
