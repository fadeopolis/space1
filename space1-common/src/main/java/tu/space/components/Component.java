package tu.space.components;

import java.io.Serializable;
import java.util.UUID;

import tu.space.utils.SpaceException;

/**
 * A component is a part of a computer
 */
@SuppressWarnings("serial")
public abstract class Component implements Serializable {

	public final UUID    id;
	public final String  producerId;
	public final boolean hasDefect;
	
	public Component( UUID id, String producerId, boolean isFaulty ) {
		this.id          = id;
		this.producerId  = producerId;
		this.hasDefect   = isFaulty;
	}
	
	public static <C extends Component> Factory<C> makeFactory( Class<C> c ) {
		return makeFactory( c.getSimpleName().toLowerCase() );
	}
	@SuppressWarnings("unchecked")
	public static <C extends Component> Factory<C> makeFactory( String type ) {
		if ( "Cpu".equalsIgnoreCase( type ) )            return (Factory<C>) new CpuFactory();
		else if ( "Gpu".equalsIgnoreCase( type ) )       return (Factory<C>) new GpuFactory();
		else if ( "Mainboard".equalsIgnoreCase( type ) ) return (Factory<C>) new MainboardFactory();
		else if ( "RamModule".equalsIgnoreCase( type ) ) return (Factory<C>) new RamModuleFactory();
		else if ( "Ram".equalsIgnoreCase( type ) )       return (Factory<C>) new RamModuleFactory();
		else throw new SpaceException("Bad component type: " + type);
	}
	
	public static abstract class Factory<C extends Component> {
		public abstract C make( UUID id, String producerId, boolean isFaulty );
		public abstract String getType();
	}
	public static class CpuFactory extends Factory<Cpu> {
		public Cpu make( UUID id, String producerId, boolean isFaulty ) {
			return new Cpu( id, producerId, isFaulty );
		}
		public String getType() { return "cpu"; }
	}
	public static class GpuFactory extends Factory<Gpu> {
		public Gpu make( UUID id, String producerId, boolean isFaulty ) {
			return new Gpu( id, producerId, isFaulty );
		}
		public String getType() { return "gpu"; }
	}
	public static class MainboardFactory extends Factory<Mainboard> {
		public Mainboard make( UUID id, String producerId, boolean isFaulty ) {
			return new Mainboard( id, producerId, isFaulty );
		}
		public String getType() { return "mainboard"; }
	}
	public static class RamModuleFactory extends Factory<RamModule> {
		public RamModule make( UUID id, String producerId, boolean isFaulty ) {
			return new RamModule( id, producerId, isFaulty );
		}
		public String getType() { return "ram"; }
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" +
			"id="         + id         + ", " + 
			"producerId=" + producerId + ", " + 
			"hasDefect="  + hasDefect   +
		"]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hasDefect ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((producerId == null) ? 0 : producerId.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Component other = (Component) obj;
		if ( hasDefect != other.hasDefect )
			return false;
		if ( id == null ) {
			if ( other.id != null )
				return false;
		} else if ( !id.equals( other.id ) )
			return false;
		if ( producerId == null ) {
			if ( other.producerId != null )
				return false;
		} else if ( !producerId.equals( other.producerId ) )
			return false;
		return true;
	}
	
	
}
