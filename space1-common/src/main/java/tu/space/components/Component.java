package tu.space.components;

import java.io.Serializable;
import java.util.UUID;

/**
 * A component is a part of a computer
 */
public abstract class Component implements Serializable {

	public final UUID    id;
	public final String  producerId;
	public final boolean hasDefect;
	
	public Component( UUID id, String producerId, boolean isFaulty ) {
		this.id         = id;
		this.producerId = producerId;
		this.hasDefect   = isFaulty;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" +
			"id="         + id         + ", " + 
			"producerId=" + producerId + ", " + 
			"isFaulty="   + hasDefect   +
		"]";
	}

	public static abstract class Factory<C extends Component> {
		public abstract C make( UUID id, String producerId, boolean isFaulty );
	}
	public static class CpuFactory extends Factory<Cpu> {
		public Cpu make( UUID id, String producerId, boolean isFaulty ) {
			return new Cpu( id, producerId, isFaulty );
		}
	}
	public static class GpuFactory extends Factory<Gpu> {
		public Gpu make( UUID id, String producerId, boolean isFaulty ) {
			return new Gpu( id, producerId, isFaulty );
		}
	}
	public static class MainboardFactory extends Factory<Mainboard> {
		public Mainboard make( UUID id, String producerId, boolean isFaulty ) {
			return new Mainboard( id, producerId, isFaulty );
		}
	}
	public static class RamModuleFactory extends Factory<RamModule> {
		public RamModule make( UUID id, String producerId, boolean isFaulty ) {
			return new RamModule( id, producerId, isFaulty );
		}
	}
	
}
