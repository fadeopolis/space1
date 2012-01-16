package tu.space.middleware;

import tu.space.components.Product;

public interface Input<P extends Product> {
	// does NOT block
	// take 1 or fail and return null
	P take();
}
