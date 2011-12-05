package tu.space.worker;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;

public class Server {

	/**
	 * 
	 * TEST SERVER
	 * @param args
	 */
	public static void main(String[] args) {
		//embedded space on localhost port 9877
		MzsCore core = DefaultMzsCore.newInstance(9877);
		new Capi(core);
	}

}
