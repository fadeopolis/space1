package tu.space.utils;

@SuppressWarnings("serial")
public class SpaceException extends RuntimeException {

	public SpaceException() {
		super();
	}
	public SpaceException( String msg ){
		super( msg );
	}

	public SpaceException( String message, Throwable cause ) {
		super( message, cause );
	}

	public SpaceException( Throwable cause ) {
		super( cause );
	}	
}
