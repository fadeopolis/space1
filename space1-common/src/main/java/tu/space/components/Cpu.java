package tu.space.components;

public class Cpu extends Component {
	public enum Type {
		SINGLE_CORE,
		DUAL_CORE,
		QUAD_CORE
	}
	
	public Cpu( String id, String producerId, boolean isFaulty, Type type ) {
		super( id, producerId, isFaulty );
		
		this.type = type;
	}
	
	public final Type type;
}
