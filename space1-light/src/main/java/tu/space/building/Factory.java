package tu.space.building;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import tu.space.gui.Browser;
import tu.space.util.LogBack;
import tu.space.util.ContainerCreator;
import tu.space.utils.Logger;
import tu.space.utils.UUIDGenerator;
import tu.space.provider.SpaceDataProvider;

import ch.qos.logback.classic.BasicConfigurator;

/**
 * 
 * The factory produces pc's for contracts based on orders
 * 
 * @author Raunig Stefan
 */

public class Factory implements NotificationListener{

	private final String factoryId;	
	private final Logger log = Logger.make(getClass());
	
	private final Capi capi;
	public String factorySpace = "xvsm://localhost:";
	
	/**
	 * Constructor - init space
	 */
	public Factory(final int port){
		//Gen. id
		UUIDGenerator uuids = new UUIDGenerator();
		this.factoryId = uuids.generate();
		
		Logger.configure();
		BasicConfigurator.configureDefaultContext();
	
		log.info("Creating space on %d, for factory", port);
		
		factorySpace += port;
		
		//embedded space on localhost given port
		MzsCore core = DefaultMzsCore.newInstance(port);
		capi = new Capi(core);
		
		try {
			ContainerCreator.getOrderContainer(URI.create(factorySpace), capi);
		} catch (MzsCoreException e) {
			log.error("Lookup of order container failed");
		}
	}

	/**
	 * Main
	 * @param arg (int) port
	 */
	public static void main(String[] args) {	
		LogBack.configure();
		//args
		int port;
		
		if(args.length != 1){
			System.err.println("Usage: ./Factory port");
			System.exit(1);
		} else {
			try{
				port = Integer.parseInt(args[0]);
				Factory fac = new Factory(port);
				new Browser(new SpaceDataProvider(fac.getId(), port, fac.getFactorySpace()));
			} catch (NumberFormatException e){
				//arg not a number
				System.err.println("Port is not a number!\nUsage: ./Factory port");
				System.exit(1);
			} catch (Exception e) {
				System.err.println("Error in Browser!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Getter UUID id of the factory
	 * 
	 * @return the factoryId
	 */
	public String getId() {
		return factoryId;
	}
	
	/**
	 * @return the factorySpace
	 */
	public String getFactorySpace() {
		return factorySpace;
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation, List<? extends Serializable> entries) {
		//do stuff with new orders
	}
}
