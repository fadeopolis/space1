package tu.space.components;

import java.util.Random;

import tu.space.components.Cpu.Type;
import tu.space.utils.SpaceException;

/**
 * A component is a part of a computer
 */
@SuppressWarnings("serial")
public abstract class Component extends Product {

	public final String  id;
	public final String  producerId;
	public final boolean hasDefect;
	
	public Component( String id, String producerId, boolean isFaulty ) {
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
		public abstract C make( String id, String producerId, boolean isFaulty );
		public abstract Class<C> getType();
	}
	public static class CpuFactory extends Factory<Cpu> {
		public Cpu make( String id, String producerId, boolean isFaulty ) {
			Type type;
			switch ( new Random().nextInt( 3 ) ) {
				case 0:  type = Type.SINGLE_CORE; break;
				case 1:  type = Type.DUAL_CORE; break;
				default: type = Type.QUAD_CORE; break;
			}
			
			return new Cpu( id, producerId, isFaulty, type );
		}
		public Class<Cpu> getType() { return Cpu.class; }
	}
	public static class GpuFactory extends Factory<Gpu> {
		public Gpu make( String id, String producerId, boolean isFaulty ) {
			return new Gpu( id, producerId, isFaulty );
		}
		public Class<Gpu> getType() { return Gpu.class; }
	}
	public static class MainboardFactory extends Factory<Mainboard> {
		public Mainboard make( String id, String producerId, boolean isFaulty ) {
			return new Mainboard( id, producerId, isFaulty );
		}
		public Class<Mainboard> getType() { return Mainboard.class; }
	}
	public static class RamModuleFactory extends Factory<RamModule> {
		public RamModule make( String id, String producerId, boolean isFaulty ) {
			return new RamModule( id, producerId, isFaulty );
		}
		public Class<RamModule> getType() { return RamModule.class; }
	}

	@Override
	public String bareToString() {
		return 
			"id="         + id         + ", " + 
			"producerId=" + producerId + ", " + 
			"hasDefect="  + hasDefect
		;
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
