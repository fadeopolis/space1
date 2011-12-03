package tu.space.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tu.space.utils.SpaceException;

public final class Computer implements Serializable {
	public enum TestStatus {
		YES, NO, UNTESTED;
	}
	
	public final UUID   id;
	public final String manufacturerId;
	public final String defectTesterId;
	public final String completenessTesterId;
	public final String logisticianId;
	
	public final Cpu             cpu;
	public final Gpu             gpu;
	public final Mainboard       mainboard;
	public final List<RamModule> ramModules;

	public final TestStatus      defect;
	public final TestStatus      complete;
	public final boolean         finished;
	
	public Computer( UUID id, String manufacturerId, Cpu cpu, Gpu gpu, Mainboard mainboard, RamModule... ram ) {
		this( id, manufacturerId, cpu, gpu, mainboard, Arrays.asList( ram ) );
	}
	public Computer( UUID id, String manufacturerId, Cpu cpu, Gpu gpu, Mainboard mainboard, Collection<RamModule> ram ) {
		this( 
			id, manufacturerId, null, null, null, 
			cpu, gpu, mainboard, ram, 
			TestStatus.UNTESTED, TestStatus.UNTESTED, false
		);		
	}

	private Computer( 
			UUID id, String manufacturerId, String defectTesterId, String completenessTesterId, String logisticianId,
			Cpu cpu, Gpu gpu, Mainboard mainboard, Collection<RamModule> ram,
			TestStatus defect, TestStatus complete, boolean finished
	) {
		this.manufacturerId       = manufacturerId;
		this.defectTesterId       = defectTesterId;
		this.completenessTesterId = completenessTesterId;
		this.logisticianId        = logisticianId;

		if( ram.size() != 1 || ram.size() != 2 || ram.size() != 4 ){
			throw new SpaceException("Computer must have 1,2 or 4 sticks of RAM, not " + ram.size());
		}
		
		this.id         = id;
		this.cpu        = cpu;
		this.gpu        = gpu;
		this.mainboard  = mainboard;
		this.ramModules = Collections.unmodifiableList( new ArrayList<RamModule>( ram ) );
		
		this.complete = complete;
		this.defect   = defect;
		this.finished = finished;
	}
	/** COPY METHODS **/
	// these methods create altered copy of a computer, remember they are immutable
	
	public Computer tagAsTestedForDefect( String testerId, TestStatus status ) {
		assert defect == TestStatus.UNTESTED;
		assert status != TestStatus.UNTESTED;
		
		return new Computer(
			id, manufacturerId, testerId, completenessTesterId, logisticianId,
			cpu, gpu, mainboard, ramModules,
			status, complete, false
		);
	}
	public Computer tagAsTestedForCompleteness( String testerId, TestStatus status ) {
		assert complete == TestStatus.UNTESTED;
		assert status   != TestStatus.UNTESTED;
		
		return new Computer(
			id, manufacturerId, defectTesterId, completenessTesterId, logisticianId,
			cpu, gpu, mainboard, ramModules,
			defect, status, false
		);
	}
	public Computer tagAsFinished( String logisticianId ) {
		return new Computer(
				id, manufacturerId, defectTesterId, completenessTesterId, logisticianId, 
				cpu, gpu, mainboard, ramModules,
				defect, complete, finished
		);
	}
	
	/** TESTS **/
	public boolean hasDefect() {
		boolean defect = false;
		
		defect |= cpu       != null && cpu.hasDefect;
		defect |= gpu       != null && gpu.hasDefect;
		defect |= mainboard != null && mainboard.hasDefect;
		
		for ( RamModule r : ramModules )
			defect |= r != null && r.hasDefect;
		
		return defect;
	}
	public boolean isComplete() {
		boolean complete = true;
		
		complete &= cpu       != null;
		complete &= mainboard != null;
		
		for ( RamModule r : ramModules )
			complete &= r != null;
		
		complete &= ramModules.size() == 1 || ramModules.size() == 2 || ramModules.size() == 4;
		
		return complete;
	}
}
