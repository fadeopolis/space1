package tu.space.unused.middleware;

import java.io.Serializable;

public interface Category<E extends Serializable> {
	Iterable<E> browse();

	void setConsumingListener( final Listener<E> l );

	void setNonConsumingListener( Listener<E> l );

	E receive();
	
	void send( E e );
	
	String name();
}