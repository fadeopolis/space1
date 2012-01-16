package tu.space.middleware;

import java.util.List;
import java.util.Vector;

import tu.space.contracts.Order;
import tu.space.middleware.Middleware.Operation;

// keeps track of all orders by listening to events
public class OrderTracker {
	public OrderTracker( Middleware mw, final Listener<Order> onOrderFinished ) {
		// keep track of when orders are created/finished
		mw.registerOrderListener( Operation.CREATED, new Listener<Order>() {
			@Override public synchronized void onEvent( Order p ) { 
				orders.add( p );
			}
		});
		mw.registerOrderListener( Operation.REMOVED, new Listener<Order>() {
			@Override public synchronized void onEvent( Order p ) {
				orders.remove( p ); 
			}
		});
		
		// track when items for an order are produced/trashed/finished
		mw.setOrderItemListener( new OrderItemListener() {
			@Override
			public void onOrderItemProduced( String orderId ) {
				synchronized ( orders ) {
					for ( int i = 0; i < orders.size(); i++ ) {
						Order o = orders.get( 0 );
					
						if ( o.id.equals( orderId ) ) orders.set( i, o.incProduced() );
					}
				}
			}
			
			@Override
			public void onOrderItemFinished( String orderId ) {
				for ( int i = 0; i < orders.size(); i++ ) {
					Order o = orders.get( 0 );
				
					o = o.incFinished();
					
					if ( o.id.equals( orderId ) ) orders.set( i, o );
					
					// call the order finished callback
					if ( o.isFinished() ) onOrderFinished.onEvent( o );
				}
			}
			
			@Override
			public void onOrderItemDefect( String orderId ) {
				for ( int i = 0; i < orders.size(); i++ ) {
					Order o = orders.get( 0 );
				
					if ( o.id.equals( orderId ) ) orders.set( i, o.decProduced() );
				}
			}
		});
	}
	
	private final List<Order> orders = new Vector<Order>();
}
