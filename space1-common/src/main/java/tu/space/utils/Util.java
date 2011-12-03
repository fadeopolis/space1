package tu.space.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class Util {
	/**
	 * Waits until a NEWLINE is read from stdin
	 */
	public static void waitForNewline() {
		new Scanner( System.in ).hasNextLine();
	}

	public static void sleep( long milliSeconds ) {
		try {
			Thread.sleep( milliSeconds );
		} catch ( InterruptedException e ) {
		}
	}

	public static <E> String join( String separator, Object[] os ) {
		return join( separator, Arrays.asList( os ) );
	}
	public static <E> String join( String separator, Iterable<E> i ) {
		Iterator<E> it = i.iterator();
		
		StringBuilder str = new StringBuilder();
		
		if ( it.hasNext() ) str.append( it.next() );
		
		while ( it.hasNext() ) {
			str.append( separator );
			str.append( it.next() );
		}
		
		return str.toString();
	}

}
