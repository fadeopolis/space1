package tu.space.components;

import java.io.Serializable;
import java.util.ArrayList;

import tu.space.utils.SpaceException;

public class Computer implements Serializable {

	public String workerId;
	
	public final Component cpu;
	public final Component gpu;
	public final Component mainboard;
	public final ArrayList<Component> ramModules = new ArrayList<Component>();
	
	public boolean complete = false;
	
	public Computer(Component cpu, Component mainboard, Component gpu, ArrayList<Component> ramModules) throws SpaceException{
		if(ramModules.size() > 4){
			throw new SpaceException("Not more then 4 ram modules allowed!");
		}
		
		this.cpu = cpu;
		this.gpu = gpu;
		this.mainboard = mainboard;
		this.ramModules.addAll(ramModules);	
		//if core components are set the PC is complete
		if(cpu != null && mainboard != null && !(ramModules.isEmpty())){
			complete = true;
		}
	}
	
	/**
	 * precondition only 4 slots for ram available,
	 * if > 4 no more allowed!
	 * @param ram
	 * @throws SpaceException 
	 */
	
	public void addRamModule(Component ram) throws SpaceException{
		if(!(ram instanceof RamModule)){
			throw new SpaceException("This component is not a ram module");
		}
		
		if(ramModules.size() <= 4){
			ramModules.add(ram);
		} else {
			//TODO log
			throw new SpaceException("Not more then 4 ram modules allowed!");
		}
	}
}
