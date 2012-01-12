package tu.space.contracts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tu.space.components.Computer;
import tu.space.components.Cpu.Type;
import tu.space.utils.SpaceException;
import tu.space.utils.UUIDGenerator;

/**
 * A order which is carried out by a factory
 * 
 * @author Raunig Stefan
 *
 */
@SuppressWarnings("serial")
public class Order implements Serializable{
	
	public final String  orderId;
	public final Type    cpuType;
	public final int 	 ramQuantity;
	public final boolean gpu;
	
	public final List<String> computerIds = new ArrayList<String>();
	
	public final int 	 quantitiy;
	
	public final boolean finished;
	
	private boolean flag = false;
	
	public Order(final Type type, final int ramQuantity, final boolean gpu, final int quantity){
		UUIDGenerator uuidgen = new UUIDGenerator();
		this.orderId = uuidgen.generate();
		
		this.cpuType 	 = type;
		this.ramQuantity = ramQuantity;
		this.gpu   	     = gpu;
		this.quantitiy   = quantity;
		this.finished	 = false;
	}
	
	private Order(final String orderId, final Type cpuType, final int ramQuantity, final boolean gpu, final int quantity, final boolean finished, final List<String> computerIds, final boolean flag) {
		this.orderId	 = orderId;
		this.cpuType 	 = cpuType;
		this.ramQuantity = ramQuantity;
		this.gpu   	     = gpu;
		this.quantitiy   = quantity;
		this.finished	 = true;
		this.computerIds.addAll(computerIds);
		this.flag = flag;
	}

	/**
	 * Add a computer to the order, and mark finish if 
	 * the size of pc's equals the quantity.
	 * 
	 * @param uuid
	 * @throws SpaceException
	 * 		quantity reached mark finished
	 */
	public synchronized void setComputerId(final String uuid) {
		if(flag) throw new SpaceException("Max quanitiy reached");
		computerIds.add(uuid);
		
		//mark finished
		if( (computerIds.size() - quantitiy) == 0 ){
			throw new SpaceException("Ready to finish order");
		}
	}
	
	public List<String> getComputerId(){
		return computerIds;
	}

	/**
	 * @return the orderId
	 */
	public String getOrderId() {
		return orderId;
	}

	/**
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @param finished the finished to set
	 */
	public Order markFinished() {
		flag = true;
		return new Order(orderId, cpuType, ramQuantity, gpu, quantitiy, true, computerIds, flag);
	}
	
	/**
	 * @return the type
	 */
	public Type getType() {
		return cpuType;
	}

	/**
	 * @return the ramQuantity
	 */
	public int getRamQuantity() {
		return ramQuantity;
	}

	/**
	 * @return the gpu
	 */
	public boolean isGpu() {
		return gpu;
	}

	/**
	 * @return the quantitiy
	 */
	public int getQuantitiy() {
		return quantitiy;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", cpuType=" + cpuType
				+ ", ramQuantity=" + ramQuantity + ", gpu=" + gpu
				+ ", computerIds=" + computerIds + ", quantitiy=" + quantitiy
				+ ", finished=" + finished + ", flag=" + flag + "]";
	}
}
