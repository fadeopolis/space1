package tu.space.components;


public class RamModule extends Component {
	public RamModule( String id, String producerId, boolean isFaulty ) {
		super( id, producerId, isFaulty );
	}

	@Override public Type type() { return Type.RAM; }
}
