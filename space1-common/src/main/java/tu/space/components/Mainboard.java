package tu.space.components;

public class Mainboard extends Component {
	public Mainboard( String id, String producerId, boolean isFaulty ) {
		super( id, producerId, isFaulty );
	}

	@Override public Type type() { return Type.MAINBOARD; }
}
