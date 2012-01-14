package tu.space.utils;

public final class UUIDGenerator {
	public String generate() {
		return Integer.toHexString( (int) System.nanoTime() );
//		return _gen.generate().toString();
	}
	
//	private final TimeBasedGenerator _gen = Generators.timeBasedGenerator();
}
