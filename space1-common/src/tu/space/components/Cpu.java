package tu.space.components;

/**
 * @author raunig stefan
 */
public class Cpu implements Component {
	public final String id;
	public final String workerId;
	/**
	 * error true if this component has a production failure
	 */
	public final boolean error;
	
	public Cpu(final String id, final String workerId, final boolean error){
		this.id = id;
		this.workerId = workerId;
		this.error = error;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getWorkerId() {
		return workerId;
	}

	@Override
	public boolean getError() {
		return error;
	}
}
