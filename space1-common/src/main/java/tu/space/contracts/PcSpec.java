package tu.space.contracts;

import tu.space.components.Cpu;
import tu.space.components.Cpu.Type;
import tu.space.components.Product;

public class PcSpec extends Product {
	public final String orderId;
	
	public final Cpu.Type cpuType;
	public final boolean  needGpu;
	public final int      numRams;
	
	public PcSpec( String orderId, Type cpuType, boolean needGpu, int numRams ) {
		super( null );
		
		this.orderId = orderId;
		this.cpuType = cpuType;
		this.needGpu = needGpu;
		this.numRams = numRams;
	}
	
	@Override
	public String bareToString() {
		return "cpuType=" + cpuType
		   + ", needGpu=" + needGpu
		   + ", numRams=" + numRams;
	}
}
