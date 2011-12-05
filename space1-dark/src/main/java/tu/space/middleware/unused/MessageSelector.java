package tu.space.middleware.unused;

public final class MessageSelector {
	public final String  key;
	public final boolean val;
	
	public MessageSelector( String key, boolean val ) {
		super();
		this.key = key;
		this.val = val;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + (val ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		MessageSelector other = (MessageSelector) obj;
		if ( key == null ) {
			if ( other.key != null )
				return false;
		} else if ( !key.equals( other.key ) )
			return false;
		if ( val != other.val )
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return key + "=" + val;
	}
}
