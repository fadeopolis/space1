package tu.space.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tu.space.utils.SpaceException;

/**
 * A computer with core components: cpu, mainboard and 1,2 or 4 ram and optional a gpu
 * 
 * @author raunig stefan
 */
public class Computer implements Serializable {

	private static final long serialVersionUID = 7215705354327276440L;

	public final String workerId;
	
	public final Component cpu;
	public final Component gpu;
	public final Component mainboard;
	public final List<Component> ramModules = new ArrayList<Component>();
	
	public final boolean error;
	
	public Computer(String workerId, Cpu cpu, Mainboard mainboard, Gpu gpu, List<RamModule> ramModules) throws SpaceException{
		//core components cpu and mainboard and ram needed
		if(cpu == null || mainboard == null){
			throw new SpaceException("Core component missing!");
		}
		//the amount of ram must be 1,2 or 4
		if(ramModules.size() < 1 || ramModules.size() == 3 || ramModules.size() > 4){
			throw new SpaceException("Not valide amount of ram components!");
		}
		
		this.workerId = workerId;
		this.cpu = cpu;
		this.gpu = gpu;
		this.mainboard = mainboard;
		this.ramModules.addAll(ramModules);
		
		//check if a component has an error thus the computer has an error
		if(cpu.getError() || gpu.error || mainboard.getError()) {
			error = true;
			return;
		} else {
			for(RamModule ram: ramModules){
				if(ram.getError()) {
					error = true;
					return;
				}
			}
			error = false;
		}
	}
}
