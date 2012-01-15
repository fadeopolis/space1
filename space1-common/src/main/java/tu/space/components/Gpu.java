package tu.space.components;

public class Gpu extends Component{
	public Gpu( String id, String producerId, boolean isFaulty ) {
		super( id, producerId, isFaulty );
	}

	@Override public Type type() { return Type.GPU; }
}
