package tu.space.light;

public class Test {
	public static void main( String[] args ) {
		new Thread() {
			public void run() {
				try {
					while ( true ) {
						
					}
				} catch ( Throwable t ) {
					System.out.println( t );
				}
			}
		}.start();
	}
}
