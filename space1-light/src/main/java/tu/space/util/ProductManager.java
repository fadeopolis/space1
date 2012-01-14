package tu.space.util;

import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import tu.space.components.Product;

public interface ProductManager<P extends Product> {
	P    take( TransactionReference tx )       throws MzsCoreException;
	void write( TransactionReference tx, P p ) throws MzsCoreException;
	
	void registerNotification( NotificationListener nl, Operation... ops ) throws MzsCoreException, InterruptedException;
	
	void shutdown() throws MzsCoreException;

	String id();
}
