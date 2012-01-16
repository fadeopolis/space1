package tu.space.middleware;

import tu.space.components.Cpu;

public interface CpuInput extends Input<Cpu> {
	Cpu take( Cpu.Type type );
}
