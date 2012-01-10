package tu.space.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Computer implements Serializable {
	public enum TestStatus {
		YES, NO, UNTESTED;
	}
	
	public final String id;
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
	
	public Computer( String id, String manufacturerId, Cpu cpu, Gpu gpu, Mainboard mainboard, RamModule... ram ) {
		this( id, manufacturerId, cpu, gpu, mainboard, Arrays.asList( ram ) );
	}
	public Computer( String id, String manufacturerId, Cpu cpu, Gpu gpu, Mainboard mainboard, Collection<RamModule> ram ) {
		this( 
			id, manufacturerId, null, null, null, 
			cpu, gpu, mainboard, ram, 
			TestStatus.UNTESTED, TestStatus.UNTESTED, false
		);		
	}

	private Computer( 
			String id, String manufacturerId, String defectTesterId, String completenessTesterId, String logisticianId,
			Cpu cpu, Gpu gpu, Mainboard mainboard, Collection<RamModule> ram,
			TestStatus defect, TestStatus complete, boolean finished
	) {
		this.manufacturerId       = manufacturerId;
		this.defectTesterId       = defectTesterId;
		this.completenessTesterId = completenessTesterId;
		this.logisticianId        = logisticianId;

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
			id, manufacturerId, defectTesterId, testerId, logisticianId,
			cpu, gpu, mainboard, ramModules,
			defect, status, false
		);
	}
	public Computer tagAsFinished( String logisticianId ) {
		return new Computer(
				id, manufacturerId, defectTesterId, completenessTesterId, logisticianId, 
				cpu, gpu, mainboard, ramModules,
				defect, complete, true
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
	
	public Iterator<Component> iterator() {
		List<Component> cs = new ArrayList<Component>( 7 );
		
		cs.add( cpu );
		cs.add( gpu );
		cs.add( mainboard );
		cs.addAll( ramModules );

		return cs.iterator();
	}
	
	@Override
	public String toString() {
		return "Computer [id=" + id + ", manufacturerId=" + manufacturerId
				+ ", defectTesterId=" + defectTesterId
				+ ", completenessTesterId=" + completenessTesterId
				+ ", logisticianId=" + logisticianId + ", cpu=" + cpu
				+ ", gpu=" + gpu + ", mainboard=" + mainboard + ", ramModules="
				+ ramModules + ", defect=" + defect + ", complete=" + complete
				+ ", finished=" + finished + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((complete == null) ? 0 : complete.hashCode());
		result = prime
				* result
				+ ((completenessTesterId == null) ? 0 : completenessTesterId
						.hashCode());
		result = prime * result + ((cpu == null) ? 0 : cpu.hashCode());
		result = prime * result + ((defect == null) ? 0 : defect.hashCode());
		result = prime * result
				+ ((defectTesterId == null) ? 0 : defectTesterId.hashCode());
		result = prime * result + (finished ? 1231 : 1237);
		result = prime * result + ((gpu == null) ? 0 : gpu.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((logisticianId == null) ? 0 : logisticianId.hashCode());
		result = prime * result
				+ ((mainboard == null) ? 0 : mainboard.hashCode());
		result = prime * result
				+ ((manufacturerId == null) ? 0 : manufacturerId.hashCode());
		result = prime * result
				+ ((ramModules == null) ? 0 : ramModules.hashCode());
		return result;
	}
	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Computer other = (Computer) obj;
		if ( complete != other.complete )
			return false;
		if ( completenessTesterId == null ) {
			if ( other.completenessTesterId != null )
				return false;
		} else if ( !completenessTesterId.equals( other.completenessTesterId ) )
			return false;
		if ( cpu == null ) {
			if ( other.cpu != null )
				return false;
		} else if ( !cpu.equals( other.cpu ) )
			return false;
		if ( defect != other.defect )
			return false;
		if ( defectTesterId == null ) {
			if ( other.defectTesterId != null )
				return false;
		} else if ( !defectTesterId.equals( other.defectTesterId ) )
			return false;
		if ( finished != other.finished )
			return false;
		if ( gpu == null ) {
			if ( other.gpu != null )
				return false;
		} else if ( !gpu.equals( other.gpu ) )
			return false;
		if ( id == null ) {
			if ( other.id != null )
				return false;
		} else if ( !id.equals( other.id ) )
			return false;
		if ( logisticianId == null ) {
			if ( other.logisticianId != null )
				return false;
		} else if ( !logisticianId.equals( other.logisticianId ) )
			return false;
		if ( mainboard == null ) {
			if ( other.mainboard != null )
				return false;
		} else if ( !mainboard.equals( other.mainboard ) )
			return false;
		if ( manufacturerId == null ) {
			if ( other.manufacturerId != null )
				return false;
		} else if ( !manufacturerId.equals( other.manufacturerId ) )
			return false;
		if ( ramModules == null ) {
			if ( other.ramModules != null )
				return false;
		} else if ( !ramModules.equals( other.ramModules ) )
			return false;
		return true;
	}
}
