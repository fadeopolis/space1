package tu.space.components;

/**
 * A component is a part of a computer
 */
@SuppressWarnings("serial")
public abstract class Component extends Product {
	public enum Type {
		CPU, GPU, MAINBOARD, RAM;
	}
	
	public final String  producerId;
	public final boolean hasDefect;
	
	public Component( String id, String producerId, boolean isFaulty ) {
		super( id );
		this.producerId  = producerId;
		this.hasDefect   = isFaulty;
	}
	
	public abstract Type type();
	
	@Override
	public String bareToString() {
		return 
			"id="         + id         + ", " + 
			"producerId=" + producerId + ", " + 
			"hasDefect="  + hasDefect
		;
	}
	
}
