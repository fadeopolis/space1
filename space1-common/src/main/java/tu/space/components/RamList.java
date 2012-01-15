package tu.space.components;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class RamList extends Product implements Iterable<RamModule> {

	public RamList( RamModule... ram ) {
		this( Arrays.asList( ram ) );
	}
	public RamList( List<RamModule> ram ) {
		super( null );
		
		if ( ram.size() != 1 && ram.size() != 2 && ram.size() != 4 ) {
			throw new InvalidParameterException( "RAM List must have 1,2 or 4 elements, not " + ram.size() );
		}
		
		this.ram = Collections.unmodifiableList( new ArrayList<RamModule>( ram ) );
	}
	
	@Override
	public Iterator<RamModule> iterator() {
		return ram.iterator();
	}

	public RamModule get( int idx ) {
		return ram.get( idx );
	}
	
	public int size() {
		return ram.size();
	}
	
	@Override
	public String bareToString() {
		String str = "(" + ram.get( 0 ).bareToString() + ")";
		
		for ( int i = 1; i < ram.size(); i++ ) {
			str += ", (" + ram.get( i ).bareToString() + ")";
		}
		
		return str;
	}
//	@Override
//	public String toString() {
//		return "Ram*" + ram.size() + "[" + bareToString() + "]";
//	}
	
	private final List<RamModule> ram;
}
