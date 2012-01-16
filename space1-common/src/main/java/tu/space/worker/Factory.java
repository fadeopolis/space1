package tu.space.worker;

import tu.space.components.Component;
import tu.space.middleware.Middleware;

public class Factory extends Worker {

	public Factory( String id, Middleware m ) {
		super( id, m );
	}

	public <C extends Component> void startProducer( Component.Type t ) {
		switch ( t ) {
			case CPU:
				
			case GPU:
			case MAINBOARD:
			case RAM:
		}
	}
	
}
