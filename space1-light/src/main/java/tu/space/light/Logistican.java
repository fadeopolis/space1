package tu.space.light;

import static tu.space.util.ContainerCreator.*;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

import tu.space.components.Computer;
import tu.space.util.ContainerCreator;
import tu.space.util.LogBack;
import tu.space.utils.Logger;

public class Logistican extends Worker {

	public static void main(String... args) throws MzsCoreException {
		Logger.configure();
		LogBack.configure();

		new Logistican(args).run();
	}

	public Logistican(String... args) throws MzsCoreException {
		super(args);

		pcs = ContainerCreator.getPcContainer(this.space, capi);
		trash = ContainerCreator.getPcDefectContainer(this.space, capi);
		storage = ContainerCreator.getStorageContainer(this.space, capi);
	}

	public Logistican(String id, Capi capi, int space) throws MzsCoreException {
		super(id, capi, space);

		pcs = ContainerCreator.getPcContainer(this.space, capi);
		trash = ContainerCreator.getPcDefectContainer(this.space, capi);
		storage = ContainerCreator.getStorageContainer(this.space, capi);
	}

	public void run() {
		TransactionReference tx = null;
		try {
			while (true) {
				tx = capi.createTransaction(DEFAULT_TX_TIMEOUT, space);

				Computer pc;
				try {
					pc = (Computer) capi.take(pcs, selector("DUMMY"),
							RequestTimeout.INFINITE, tx).get(0);
				} catch (CountNotMetException e) {
					// no part available
					rollback(tx);
					continue;
				}

				pc = pc.tagAsFinished(workerId);

				if (!pc.isComplete() || pc.hasDefect()) {
					log.info("%s: Got an bad PC %s", this, pc.id);

					capi.write(trash, new Entry(pc));
				} else {
					log.info("%s: Got a mighty fine PC %s", this, pc.id);

					capi.write(storage, new Entry(pc));
				}

				capi.commitTransaction(tx);
			}
		} catch (Exception e) {
			rollback(tx);
			e.printStackTrace();
		} finally {
			clean();
		}
	}

	private final ContainerReference pcs;
	private final ContainerReference trash;
	private final ContainerReference storage;
}
