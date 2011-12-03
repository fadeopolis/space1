package tu.space.middleware;

public interface Listener<E> {
	void handle( E e );
}
