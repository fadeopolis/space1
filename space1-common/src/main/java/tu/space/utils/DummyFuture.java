package tu.space.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DummyFuture<E> implements Future<E> {
	public static <T> Future<T> make( T t ) {
		return new DummyFuture<T>( t );
	}
	
	public DummyFuture() {
		this( null );
	}
	public DummyFuture( E e ) {
		this.data = e;
	}
	
	@Override
	public boolean cancel( boolean mayInterruptIfRunning ) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public E get() throws InterruptedException, ExecutionException {
		return data;
	}

	@Override
	public E get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		return data;
	}	

	private final E data;
}
