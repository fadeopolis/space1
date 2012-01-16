package tu.space.worker;

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Product;
import tu.space.components.RamModule;
import tu.space.middleware.Input;
import tu.space.middleware.Listener;
import tu.space.middleware.Middleware;
import tu.space.middleware.Middleware.Operation;
import tu.space.middleware.Output;

public abstract class Tester extends Worker {

	public Tester( String id, Middleware m ) {
		super( id, m );
		
		pcIn  = getInput();
		pcOut = m.getComputerOutput();
		cpus  = m.getCpuOutput();
		gpus  = m.getGpuOutput();
		mbds  = m.getMainboardOutput();
		rams  = m.getRamOutput();
		trash = m.getTrash();
		
		// test on startup
		while ( true ) {
			mw.beginTransaction();
			
			Computer pc = pcIn.take();
			
			if ( pc == null ) {
				mw.rollbackTransaction();
				break;
			}
			
			pc = tag( pc );
			
			if ( isOK( pc ) ) {
				log.info( "%s: Got a working PC %s", this, pc );

				pcOut.write( pc );
			} else {
				log.info( "%s: Got a defect  PC %s", this, pc );

				mw.signalPcForOrderDefect( pc );
				
				// recycle
				if ( pc.cpu       != null )
					if ( pc.cpu.hasDefect) { trash.write( pc.cpu ); }
					else                   { cpus.write( pc.cpu );  }

				if ( pc.gpu       != null )
					if ( pc.gpu.hasDefect) { trash.write( pc.gpu ); }
					else                   { gpus.write( pc.gpu );  }
					
				if ( pc.mainboard != null )
					if ( pc.mainboard.hasDefect) { trash.write( pc.mainboard ); }
					else                         { mbds.write( pc.mainboard );  }

				for ( RamModule ram : pc.ram ) 
					if ( ram.hasDefect ) { trash.write( ram ); }
					else                 { rams.write( ram );  }
			}
			
			mw.commitTransaction();
		}
		
		// notifications
		m.registerListener( Computer.class, Operation.CREATED, new Listener<Computer>() {
			@Override
			public synchronized void onEvent( Computer pc ) {
				test( pc );
			}
		});
	}
	
	private void test( Computer pc ) {
		if ( isUntested( pc ) ) {
			mw.beginTransaction();
			
			pc = pcIn.take();
			
			if ( pc == null ) {
				mw.rollbackTransaction();
				return;
			}
			
			pc = tag( pc );
			
			if ( isOK( pc ) ) {
				log.info( "%s: Got a working PC %s", this, pc );

				pcOut.write( pc );
			} else {
				log.info( "%s: Got a defect  PC %s", this, pc );

				mw.signalPcForOrderDefect( pc );
				
				// recycle
				if ( pc.cpu       != null )
					if ( pc.cpu.hasDefect) { trash.write( pc.cpu ); }
					else                   { cpus.write( pc.cpu );  }

				if ( pc.gpu       != null )
					if ( pc.gpu.hasDefect) { trash.write( pc.gpu ); }
					else                   { gpus.write( pc.gpu );  }
					
				if ( pc.mainboard != null )
					if ( pc.mainboard.hasDefect) { trash.write( pc.mainboard ); }
					else                         { mbds.write( pc.mainboard );  }

				for ( RamModule ram : pc.ram ) 
					if ( ram.hasDefect ) { trash.write( ram ); }
					else                 { rams.write( ram );  }
			}
			
			mw.commitTransaction();
		}
	}

	protected abstract Input<Computer> getInput();
	protected abstract boolean         isUntested( Computer pc );
	protected abstract boolean         isOK( Computer c );
	protected abstract Computer        tag( Computer c );
	
	private final Input<Computer>   pcIn;
	private final Output<Computer>  pcOut;
	private final Output<Cpu>       cpus;
	private final Output<Gpu>       gpus;
	private final Output<Mainboard> mbds;
	private final Output<RamModule> rams;
	private final Output<Product>   trash;
}
