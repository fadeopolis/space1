package tu.space.dark;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamList;
import tu.space.components.RamModule;
import tu.space.components.Computer.TestStatus;
import tu.space.contracts.Order;
import tu.space.contracts.PcSpec;
import tu.space.utils.SpaceException;

import static tu.space.dark.JMSMiddleware.*;

public abstract class Marshaller<P extends Product> {
	@SuppressWarnings("unchecked")
	static <P extends Product> Marshaller<P> forName( String name ) {
		try {
			return (Marshaller<P>) Marshaller.class.getField( name ).get( null );
		} catch ( IllegalArgumentException e ) {
			throw new SpaceException( e );
		} catch ( SecurityException e ) {
			throw new SpaceException( e );
		} catch ( IllegalAccessException e ) {
			throw new SpaceException( e );
		} catch ( NoSuchFieldException e ) {
			throw new SpaceException( e );
		}
	}
	@SuppressWarnings("unchecked")
	static <P extends Product> Marshaller<P> forType( Class<P> c ) {
		if ( c == Computer.class  ) return (Marshaller<P>) Computer;
		if ( c == Cpu.class       ) return (Marshaller<P>) Cpu;
		if ( c == Gpu.class       ) return (Marshaller<P>) Gpu;
		if ( c == Mainboard.class ) return (Marshaller<P>) Mainboard;
		if ( c == RamModule.class ) return (Marshaller<P>) RamModule;
		if ( c == Order.class     ) return (Marshaller<P>) Order;
		if ( c == PcSpec.class    ) return (Marshaller<P>) PcSpec;

		return (Marshaller<P>) ByType;
	}
	
	public static final Marshaller<Computer> Computer = new Marshaller<Computer>() {
		@Override
		public void toMessage( Computer c, Message m ) throws JMSException {
			m.setIntProperty( "numRAM", c.ram.size() );
			
			for ( int i = 0; i < c.ram.size(); i++ ) {
				m.setStringProperty(  "ram." + i + ".id",         c.ram.get( i ).id         );
				m.setStringProperty(  "ram." + i + ".producerId", c.ram.get( i ).producerId );
				m.setBooleanProperty( "ram." + i + ".defect",     c.ram.get( i ).hasDefect  );
			}
			
			m.setStringProperty( "id",                   c.id );
			m.setStringProperty( "manufacturerId",       c.manufacturerId );
			m.setStringProperty( "orderId",              c.orderId );
			m.setStringProperty( "cpu.id",               c.cpu.id );
			m.setStringProperty( "cpu.producerId",       c.cpu.producerId );
			m.setBooleanProperty( "cpu.defect",          c.cpu.hasDefect );
			m.setStringProperty( "cpu.type",             c.cpu.type.name() );
			m.setStringProperty( "gpu.id",               c.gpu.id );
			m.setStringProperty( "gpu.producerId",       c.gpu.producerId );
			m.setBooleanProperty( "gpu.defect",          c.gpu.hasDefect );
			m.setStringProperty( "mainboard.id",         c.mainboard.id );
			m.setStringProperty( "mainboard.producerId", c.mainboard.producerId );
			m.setBooleanProperty( "mainboard.defect",    c.mainboard.hasDefect );

			m.setStringProperty( "defectTesterId",       c.defectTesterId );
			m.setStringProperty( "completenessTesterId", c.completenessTesterId );
			m.setStringProperty( "logistician",          c.logisticianId );
			
			m.setBooleanProperty( TESTED_FOR_DEFECT,       c.defect   != TestStatus.UNTESTED );
			m.setBooleanProperty( TESTED_FOR_COMPLETENESS, c.complete != TestStatus.UNTESTED );
		}

		@Override
		public Computer fromMessage( Message m ) throws JMSException {
			int numRams = m.getIntProperty( "numRAM" );
			
			List<RamModule> ram = new ArrayList<RamModule>();
			for ( int i = 0; i < numRams; i++ ) {
				ram.add( new RamModule( 
					m.getStringProperty( "ram." + i + ".id" ),
					m.getStringProperty( "ram." + i + ".producerId" ),
					m.getBooleanProperty( "ram." + i + ".defect" )
				) );
			}
			
			Computer c = new Computer(
				m.getStringProperty( "id" ), 
				m.getStringProperty( "manufacturerId" ),
				m.getStringProperty( "orderId" ),
				new Cpu(
					m.getStringProperty( "cpu.id" ),
					m.getStringProperty( "cpu.producerId" ),
					m.getBooleanProperty( "cpu.defect" ),
					tu.space.components.Cpu.Type.valueOf( m.getStringProperty( "cpu.type" ) )
				),
				new Gpu(
					m.getStringProperty( "gpu.id" ),
					m.getStringProperty( "gpu.producerId" ),
					m.getBooleanProperty( "gpu.defect" )
				),
				new Mainboard(
					m.getStringProperty( "mainboard.id" ),
					m.getStringProperty( "mainboard.producerId" ),
					m.getBooleanProperty( "mainboard.defect" )
				),
				new RamList( ram )
			);
			
			String defectTester = m.getStringProperty( "defectTesterId" );
			if ( defectTester != null ) c = c.tagAsTestedForDefect( defectTester );

			String completenessTester = m.getStringProperty( "completenessTesterId" );
			if ( completenessTester != null ) c = c.tagAsTestedForCompleteness( completenessTester );

			String logistician = m.getStringProperty( "logistician" );
			if ( logistician != null ) c = c.tagAsFinished( logistician );

			return c;
		}
	};

	public static final Marshaller<Cpu> Cpu = new Marshaller<Cpu>() {
		@Override
		public Cpu fromMessage( Message m ) throws JMSException {
			return new Cpu(
				m.getStringProperty( "id" ),
				m.getStringProperty( "producerId" ),
				m.getBooleanProperty( "defect" ),
				tu.space.components.Cpu.Type.valueOf( m.getStringProperty( "CPU" ) )
			);
		}

		@Override
		protected void toMessage( Cpu p, Message m ) throws JMSException {
			m.setStringProperty( "id", p.id );
			m.setStringProperty( "producerId", p.producerId );
			m.setBooleanProperty( "defect", p.hasDefect );
			m.setStringProperty( "CPU", p.type.name() );
		}
	};	
	
	public static final Marshaller<Gpu> Gpu = new Marshaller<Gpu>() {
		@Override
		public Gpu fromMessage( Message m ) throws JMSException {
			return new Gpu(
				m.getStringProperty( "id" ),
				m.getStringProperty( "producerId" ),
				m.getBooleanProperty( "defect" )
			);
		}

		@Override
		protected void toMessage( Gpu p, Message m ) throws JMSException {
			m.setStringProperty( "id", p.id );
			m.setStringProperty( "producerId", p.producerId );
			m.setBooleanProperty( "defect", p.hasDefect );
		}
	};	
	
	public static final Marshaller<Mainboard> Mainboard = new Marshaller<Mainboard>() {
		@Override
		public Mainboard fromMessage( Message m ) throws JMSException {
			return new Mainboard(
				m.getStringProperty( "id" ),
				m.getStringProperty( "producerId" ),
				m.getBooleanProperty( "defect" )
			);
		}

		@Override
		protected void toMessage( Mainboard p, Message m ) throws JMSException {
			m.setStringProperty( "id", p.id );
			m.setStringProperty( "producerId", p.producerId );
			m.setBooleanProperty( "defect", p.hasDefect );
		}
	};	
	
	public static final Marshaller<RamModule> RamModule = new Marshaller<RamModule>() {
		@Override
		public RamModule fromMessage( Message m ) throws JMSException {
			return new RamModule(
				m.getStringProperty( "id" ),
				m.getStringProperty( "producerId" ),
				m.getBooleanProperty( "defect" )
			);
		}

		@Override
		protected void toMessage( RamModule p, Message m ) throws JMSException {
			m.setStringProperty( "id", p.id );
			m.setStringProperty( "producerId", p.producerId );
			m.setBooleanProperty( "defect", p.hasDefect );
		}
	};	
	
	public static final Marshaller<Order> Order = new Marshaller<Order>() {
		@Override
		public Order fromMessage( Message m ) throws JMSException {
			return new Order(
				m.getStringProperty( "id" ),
				tu.space.components.Cpu.Type.valueOf( m.getStringProperty( "cpuType" ) ),
				m.getIntProperty( "nrRAM" ),
				m.getBooleanProperty( "gpu" ),
				m.getIntProperty( "quantity" )
			);
		}

		@Override
		protected void toMessage( Order p, Message m ) throws JMSException {
			m.setStringProperty( "id", p.id );
			m.setStringProperty( "cpuType", p.cpuType.name() );
			m.setIntProperty( "nrRAM", p.ramQuantity );
			m.setBooleanProperty( "gpu", p.gpu );
			m.setIntProperty( "quantity", p.quantity );
		}
	};	
	
	public static final Marshaller<PcSpec> PcSpec = new Marshaller<PcSpec>() {
		@Override
		public PcSpec fromMessage( Message m ) throws JMSException {
			return new PcSpec(
				m.getStringProperty( "orderId" ),
				tu.space.components.Cpu.Type.valueOf( m.getStringProperty( "cpuType" ) ),
				m.getBooleanProperty( "gpu" ),
				m.getIntProperty( "nrRAM" )
			);
		}

		@Override
		protected void toMessage( PcSpec p, Message m ) throws JMSException {
			m.setStringProperty( "orderId", p.orderId );
			m.setStringProperty( "cpuType", p.cpuType.name() );
			m.setBooleanProperty( "gpu", p.needGpu );
			m.setIntProperty( "nrRAM", p.numRams );
		}
	};	
	
	public static final Marshaller<Product> ByType = new Marshaller<Product>() {
		@Override
		public Product fromMessage( Message m ) throws JMSException {
			return Marshaller.forName( m.getStringProperty( "__TYPE__" ) ).fromMessage( m );
		}

		@Override
		protected void toMessage( Product p, Message m ) throws JMSException {
			@SuppressWarnings("unchecked")
			Marshaller<Product> marsh = (Marshaller<Product>) Marshaller.forType( p.getClass() );
			
			marsh.toMessage( p, m );
		}
	};
	
	public Message toMessage( Session s, P p ) throws JMSException {
		Message m = s.createMessage();
		
		toMessage( p, m );

		m.setStringProperty( "__TYPE__", p.getClass().getSimpleName() );
		
		return m;
	}
	public abstract P fromMessage( Message m ) throws JMSException;
	
	protected abstract void toMessage( P p, Message m ) throws JMSException;
}
