package tu.space.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Computer extends Product {
	public enum TestStatus {
		YES, NO, UNTESTED;
	}
	
	public final String manufacturerId;
	public final String defectTesterId;
	public final String completenessTesterId;
	public final String logisticianId;
	public final String orderId;
	
	public final Cpu       cpu;
	public final Gpu       gpu;
	public final Mainboard mainboard;
	public final RamList   ram;

	public final TestStatus      defect;
	public final TestStatus      complete;
	public final boolean         finished;
	
	public Computer( String id, String manufacturerId, String orderId ) {
		this( id, manufacturerId, orderId, null, null, null, (RamList) null );
	}
	public Computer( String id, String manufacturerId, String orderId, Cpu cpu, Gpu gpu, Mainboard mainboard, RamModule... ram ) {
		this( id, manufacturerId, orderId, cpu, gpu, mainboard, new RamList( ram ) );
	}
	public Computer( String id, String manufacturerId, String orderId, Cpu cpu, Gpu gpu, Mainboard mainboard, RamList ram ) {
		this( 
			id, manufacturerId, orderId, null, null, null,
			cpu, gpu, mainboard, ram, 
			TestStatus.UNTESTED, TestStatus.UNTESTED, false
		);		
	}

	private Computer( 
			String id, String manufacturerId, String orderId, String defectTesterId, String completenessTesterId, String logisticianId,
			Cpu cpu, Gpu gpu, Mainboard mainboard, RamList ram,
			TestStatus defect, TestStatus complete, boolean finished
	) {
		super( id );
		this.manufacturerId       = manufacturerId;
		this.defectTesterId       = defectTesterId;
		this.completenessTesterId = completenessTesterId;
		this.logisticianId        = logisticianId;
		this.orderId 			  = orderId;

		this.cpu       = cpu;
		this.gpu       = gpu;
		this.mainboard = mainboard;
		this.ram       = ram;
		
		this.complete = complete;
		this.defect   = defect;
		this.finished = finished;
	}
	/** COPY METHODS **/
	// these methods create altered copy of a computer, remember they are immutable
	
	public Computer tagAsTestedForDefect( String testerId ) {
		return new Computer(
			id, manufacturerId, orderId, testerId, completenessTesterId, logisticianId,
			cpu, gpu, mainboard, ram,
			hasDefect() ? TestStatus.YES : TestStatus.NO, complete, false
		);
	}
	public Computer tagAsTestedForCompleteness( String testerId ) {
		return new Computer(
			id, manufacturerId, orderId, defectTesterId, testerId, logisticianId, 
			cpu, gpu, mainboard, ram,
			defect, isComplete() ? TestStatus.YES : TestStatus.NO, false
		);
	}
	public Computer tagAsFinished( String logisticianId ) {
		return new Computer(
				id, manufacturerId, orderId, defectTesterId, completenessTesterId, logisticianId,
				cpu, gpu, mainboard, ram,
				defect, complete, true
		);
	}
	
	/** TESTS **/
	public boolean hasDefect() {
		boolean defect = false;
		
		defect |= cpu       != null && cpu.hasDefect;
		defect |= gpu       != null && gpu.hasDefect;
		defect |= mainboard != null && mainboard.hasDefect;
		
		for ( RamModule r : ram )
			defect |= r != null && r.hasDefect;
		
		return defect;
	}
	public boolean isComplete() {
		boolean complete = true;
		
		complete &= cpu       != null;
		complete &= mainboard != null;
		
		for ( RamModule r : ram )
			complete &= r != null;
		
		complete &= ram.size() == 1 || ram.size() == 2 || ram.size() == 4;
		
		return complete;
	}
	
	public Iterator<Component> iterator() {
		List<Component> cs = new ArrayList<Component>( 7 );
		
		cs.add( cpu );
		cs.add( gpu );
		cs.add( mainboard );
		for ( RamModule ram : this.ram ) cs.add( ram );

		return cs.iterator();
	}
	
	@Override
	public String bareToString() {
		return "id=" + id
		   + ", manufacturerId=" + manufacturerId
		   + ", defectTesterId=" + defectTesterId
		   + ", completenessTesterId=" + completenessTesterId
		   + ", logisticianId=" + logisticianId
		   + ", orderId=" + orderId
		   + ", cpu=" + cpu
		   + ", gpu=" + gpu
		   + ", mainboard=" + mainboard
		   + ", ramModules=" + ram
		   + ", defect=" + defect
		   + ", complete=" + complete
		   + ", finished=" + finished;
	}

}
