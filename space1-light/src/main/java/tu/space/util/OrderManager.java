package tu.space.util;

import static tu.space.util.ContainerCreator.key;
import static tu.space.util.ContainerCreator.keySel;

import java.net.URI;
import java.util.Iterator;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import tu.space.components.Computer;
import tu.space.contracts.Order;
import tu.space.util.internal.ContainerUtil;
import tu.space.util.internal.Iterators;

public class OrderManager {
	public OrderManager( URI space, Capi capi ) throws MzsCoreException {
		this.ids    = new ContainerUtil<String>( capi, ContainerCreator.getOrderIdContainer( space, capi ) );
		this.orders = new ContainerUtil<Order>( capi, ContainerCreator.getOrderContainer( space, capi ) );
	}
	
	public void publishOrder( Order o, TransactionReference tx ) throws MzsCoreException {
		ids.write.write( o.id, tx,  VectorCoordinator.newCoordinationData( VectorCoordinator.APPEND ) );
		orders.write.write( o, tx, key(o.id) );
	}
	
	public Iterable<Order> allOrders( final TransactionReference tx ) throws MzsCoreException {
		return new Iterable<Order>() {
			@Override
			public Iterator<Order> iterator() {
				return new Iterators.AbstractIterator<Order>() {
					private int index = 0;
					
					@Override
					protected Order fetchNext() throws MzsCoreException {
						String id;
						try {
							id = ids.read.readOne( tx, VectorCoordinator.newSelector( index ) );							
						} catch ( CountNotMetException e ) {
							return null;
						}
						
						Order o = orders.take.takeOne( tx, keySel(id) );
						index++;
						
						return o;
					}
				};
			}
		};
	}
	// you MUST call this when done with an Order when iterating through all orders with allOrders()
	public void returnOrder( TransactionReference tx, Order o ) throws MzsCoreException {
		orders.write.write( o, tx, key(o.id) );
	}
	
	public void signalPcIsFinished( TransactionReference tx, Computer c ) throws MzsCoreException {
		Order o = orders.take.takeOneNoWait( tx, keySel( c.orderId ) );

		o = o.incFinished();
		
		updateOrder( tx, o );
	}
	public void signalPcIsProduced( TransactionReference tx, Computer c ) throws MzsCoreException {
		Order o = takeOrder( tx, c.orderId );

		o = o.incProduced();
		
		updateOrder( tx, o );
	}
	public void signalPcIsDefect( TransactionReference tx, Computer c ) throws MzsCoreException {
		Order o = takeOrder( tx, c.orderId );

		o = o.decProduced();
		
		updateOrder( tx, o );
	}
	
	//***** PRIVATE
	
	private Order takeOrder( TransactionReference tx, String id ) throws MzsCoreException {
		return orders.take.takeOneNoWait( tx, keySel( id ) );
	}
	private void updateOrder( TransactionReference tx, Order o ) throws MzsCoreException {
		if ( o.isFinished() ) {
			deleteOrderId( tx, o.id );
		} else {
			returnOrder( tx, o );
		}	
	}
	
	private void deleteOrderId( TransactionReference tx, String id ) throws MzsCoreException {
		int idx = 0;
		while ( true ) {
			String str = ids.read.readOne( tx, VectorCoordinator.newSelector( idx ) );

			if ( id.equals( str ) ) { break; }
			
			idx++;
		}
		ids.delete.delete( tx, VectorCoordinator.newSelector( idx ) );
	}
	
	private final ContainerUtil<String> ids;
	private final ContainerUtil<Order>  orders;
}
