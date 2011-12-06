package tu.space.worker;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;

import ch.qos.logback.classic.BasicConfigurator;

public class Server {

	/**
	 * 
	 * TEST SERVER
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configureDefaultContext();
		
		//embedded space on localhost port 9877
		MzsCore core = DefaultMzsCore.newInstance(9877);
		new Capi(core);
	}

}
