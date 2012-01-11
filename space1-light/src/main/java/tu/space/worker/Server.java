package tu.space.worker;

import java.util.concurrent.atomic.AtomicInteger;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;

import tu.space.util.LogBack;
import tu.space.utils.Logger;
import ch.qos.logback.classic.Level;

public class Server {

	/**
	 * 
	 * TEST SERVER
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.configure();
		LogBack.configure( Level.INFO );

		//embedded space on localhost port 9877
		MzsCore core = DefaultMzsCore.newInstance(9877);
		new Capi(core);
	}

}
