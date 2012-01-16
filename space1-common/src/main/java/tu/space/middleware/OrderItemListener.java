package tu.space.middleware;

public interface OrderItemListener {
	void onOrderItemProduced( String orderId );
	void onOrderItemDefect( String orderId );
	void onOrderItemFinished( String orderId );
}
