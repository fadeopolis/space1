package tu.space.middleware;

import tu.space.components.RamList;

public interface RamInput extends Input<RamList> {
	// take exactly amount or fail and return null
	RamList take( int amount );
}
