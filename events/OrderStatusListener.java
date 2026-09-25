package events;

public interface OrderStatusListener {
    void onOrderStatusChanged(OrderStatusChangeEvent event);
}