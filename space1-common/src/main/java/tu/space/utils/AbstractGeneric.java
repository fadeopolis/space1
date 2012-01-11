package tu.space.utils;

public class AbstractGeneric<E> implements Generic<E> {
	public AbstractGeneric( Class<E> c ) {
		this.c = c;
	}
	
	@Override
	public Class<? extends E> getType() {
		return c;
	}

	private final Class<E> c;
}
