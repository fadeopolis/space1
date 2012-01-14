package tu.space.util.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;

import tu.space.utils.SpaceException;

public class Iterators {
	public static abstract class AbstractIterator<E> implements Iterator<E> {
		private E next;
		
		@Override
		public boolean hasNext() {
			if ( next == null )
				try {
					next = fetchNext();
				} catch ( Exception e ) {
					throw new SpaceException( e );
				}
			
			return next != null;
		}

		@Override
		public E next() {
			if ( !hasNext() ) throw new NoSuchElementException();
			
			E e = next;
			next = null;
			
			return e;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		protected abstract E fetchNext() throws Exception;
	}
}
