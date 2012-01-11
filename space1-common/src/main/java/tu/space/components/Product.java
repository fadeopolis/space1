package tu.space.components;

import java.io.Serializable;

public abstract class Product implements Serializable {
	public abstract String bareToString();

	@Override
	public final String toString() {
		return getClass().getSimpleName() + "[" + bareToString() + "]";
	}
}
