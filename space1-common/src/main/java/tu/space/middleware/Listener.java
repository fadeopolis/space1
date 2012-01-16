package tu.space.middleware;

import tu.space.components.Product;

public interface Listener<P extends Product> {
	void onEvent( P p );
}
