package tu.space.middleware;

import tu.space.components.Product;

public interface Output<P extends Product> {
	void write( P p );
}
