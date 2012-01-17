package tu.space.dark.worker;

import java.util.Random;

import tu.space.components.Component;
import tu.space.components.Component.Type;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.middleware.Middleware;
import tu.space.middleware.Output;
import tu.space.utils.SpaceException;
import tu.space.worker.Worker;

public class Producer<C extends Component> extends Worker implements Runnable {
	public Producer( String id, Middleware m, Type type, int quantity, double errorRate ) {
		super( id, m );
		
		this.quantity  = quantity;
		this.errorRate = errorRate;
		this.type      = type;
		this.maker     = makerForType( type );
		this.out       = outputForType( type );
	}

	@Override
	public void run() {
		for( int i = quantity; i > 0; i-- ) {		
			sleep();

			boolean defect    = randomDouble() < errorRate; // bernoulli experiment
			String  productId = genUUID();
				
			C c = maker.make( productId, id, defect );
				
			assert c.id         == productId;
			assert c.producerId == id;
			assert c.hasDefect  == defect;
				
			mw.beginTransaction();
				out.write( c );
			mw.commitTransaction();
			log.debug( "%s: made a %s, %s to go" , this, type, i );
		}

		//finish and go home
		log.debug( "%s: Finished work going home!", this );
	}

	private final int       quantity;
	private final double    errorRate;
	private final Type      type;
	private final Maker<C>  maker;
	private final Output<C> out;
	
	@SuppressWarnings("unchecked")
	private static <C extends Component> Maker<C> makerForType( Type type ) {
		switch ( type ) {
			case CPU:       return (Maker<C>) new CpuMaker();
			case GPU:       return (Maker<C>) new GpuMaker();
			case MAINBOARD: return (Maker<C>) new MainboardMaker();
			case RAM:       return (Maker<C>) new RamMaker();
			default:        throw new SpaceException();
		}
	}
	@SuppressWarnings("unchecked")
	private Output<C> outputForType( Type type ) {
		switch ( type ) {
			case CPU:       return (Output<C>) mw.getCpuOutput();
			case GPU:       return (Output<C>) mw.getGpuOutput();
			case MAINBOARD: return (Output<C>) mw.getMainboardOutput();
			case RAM:       return (Output<C>) mw.getRamOutput();
			default:        throw new SpaceException();
		}
	}
	
	private interface Maker<C> {
		C make( String productId, String producerId, boolean defect );
	}
	private static class CpuMaker implements Maker<Cpu> {
		@Override
		public Cpu make( String productId, String producerId, boolean defect ) {
			Cpu.Type type;
			switch ( rand.nextInt( 3 ) ) {
				case 0:  type = Cpu.Type.SINGLE_CORE; break;
				case 1:  type = Cpu.Type.DUAL_CORE; break;
				default: type = Cpu.Type.QUAD_CORE; break;
			}
			
			return new Cpu( productId, producerId, defect, type );
		}
		
		private final Random rand = new Random();
	}
	private static class GpuMaker implements Maker<Gpu> {
		@Override
		public Gpu make( String productId, String producerId, boolean defect ) {
			return new Gpu( productId, producerId, defect );
		}
	}
	private static class MainboardMaker implements Maker<Mainboard> {
		@Override
		public Mainboard make( String productId, String producerId, boolean defect ) {
			return new Mainboard( productId, producerId, defect );
		}
	}	
	private static class RamMaker implements Maker<RamModule> {
		@Override
		public RamModule make( String productId, String producerId, boolean defect ) {
			return new RamModule( productId, producerId, defect );
		}
	}
}
