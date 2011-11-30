package tu.space.components;

import java.io.Serializable;

/**
 * 
 * Define a component
 * 
 * @author raunig stefan
 */
public interface Component extends Serializable {
	//A component is a part of a computer
	public String getId();
	public String getWorkerId();
	public boolean getError();
}
