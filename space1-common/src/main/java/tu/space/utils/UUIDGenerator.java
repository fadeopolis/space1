package tu.space.utils;

import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public final class UUIDGenerator {
	public UUID generate() {
		return _gen.generate();
	}
	
	private final TimeBasedGenerator _gen = Generators.timeBasedGenerator();
}
