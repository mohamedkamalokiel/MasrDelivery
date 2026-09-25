package events;

import java.util.ArrayList;
import java.util.List;

public abstract class OrderSubject {
    private final List<OrderStatusListener> listeners = new ArrayList<>();

    public void addListener(OrderStatusListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OrderStatusListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(OrderStatusChangeEvent event) {
        for (OrderStatusListener listener : listeners) {
            listener.onOrderStatusChanged(event);
        }
    }
}