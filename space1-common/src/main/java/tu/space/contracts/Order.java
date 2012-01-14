package tu.space.contracts;

import tu.space.components.Computer;
import tu.space.components.Cpu.Type;
import tu.space.components.Product;

/**
 * A order which is carried out by a factory
 * 
 * @author Raunig Stefan
 *
 */
@SuppressWarnings("serial")
public class Order extends Product {
	
	public final String  id;
	public final Type    cpuType;
	public final int 	 ramQuantity;
	public final boolean gpu;
	
	public final int quantity;
	public final int produced;
	public final int finished;
	
	public Order( String id,  Type type,  int ramQuantity,  boolean gpu,  int quantity ){
		this( id, type, ramQuantity, gpu, quantity, 0, 0 );
	}
	
	private Order( String id,  Type cpuType,  int ramQuantity,  boolean gpu,  int quantity, int produced, int finished ) {
		this.id	         = id;
		this.cpuType 	 = cpuType;
		this.ramQuantity = ramQuantity;
		this.gpu   	     = gpu;
		this.quantity   = quantity;

		this.produced    = produced;
		this.finished	 = finished;
	}

	public Order incProduced(){
		return new Order( id, cpuType, ramQuantity, gpu, quantity, produced + 1, finished );
	}
	
	public Order decProduced(){
		return new Order( id, cpuType, ramQuantity, gpu, quantity, produced - 1, finished );
	}
	
	public Order incFinished(){
		return new Order( id, cpuType, ramQuantity, gpu, quantity, produced, finished + 1 );
	}

	public Order decFinished(){
		return new Order( id, cpuType, ramQuantity, gpu, quantity, produced, finished - 1 );
	}

	public boolean isFinished() {
		return quantity - finished == 0;
	}

	public boolean shouldBuildMore() {
		return quantity - produced > 0;
	}
	
	/**
	 * Check if pc is equals to the spec.
	 * 
	 * @param pc
	 * @return boolean
	 */
	public boolean equals(final Computer pc){
		if(!(pc.cpu.type.equals(cpuType))) return false;
		if(pc.ramModules.size() != ramQuantity) return false;
		if( (gpu && (pc.gpu == null)) || (!gpu && (pc.gpu != null)) ) return false;
		
		return true;
	}

	@Override
	public String bareToString() {
		return "id="          + id
		   + ", cpuType="     + cpuType
		   + ", ramQuantity=" + ramQuantity
		   + ", gpu="         + gpu
		   + ", quantitiy="   + quantity
		   + ", produced="    + produced
		   + ", finished="    + finished;
	}
}
