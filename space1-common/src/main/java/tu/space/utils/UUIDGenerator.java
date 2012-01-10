package tu.space.utils;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public final class UUIDGenerator {
	public String generate() {
		return _gen.generate().toString();
	}
	
	private final TimeBasedGenerator _gen = Generators.timeBasedGenerator();
}
