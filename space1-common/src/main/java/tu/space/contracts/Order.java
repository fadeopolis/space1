package tu.space.contracts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tu.space.components.Cpu.Type;
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
	
	public Order(final Type type, final int ramQuantity, final boolean gpu, final int quantity){
		UUIDGenerator uuidgen = new UUIDGenerator();
		this.orderId = uuidgen.generate();
		
		this.cpuType 	 = type;
		this.ramQuantity = ramQuantity;
		this.gpu   	     = gpu;
		this.quantitiy   = quantity;
		this.finished	 = false;
	}
	
	private Order(final String orderId, final Type cpuType, final int ramQuantity, final boolean gpu, final int quantity, final boolean finished, final List<String> computerIds) {
		this.orderId	 = orderId;
		this.cpuType 	 = cpuType;
		this.ramQuantity = ramQuantity;
		this.gpu   	     = gpu;
		this.quantitiy   = quantity;
		this.finished	 = true;
		this.computerIds.addAll(computerIds);
	}

	public void setComputerId(final String uuid){
		computerIds.add(uuid);
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
		return new Order(orderId, cpuType, ramQuantity, gpu, quantitiy, true, computerIds);
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
}
