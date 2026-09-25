package dispatch;

import domain.LoyaltyTier;
import domain.Order;

import java.util.Comparator;
import java.util.PriorityQueue;

public class DispatchService {

    // Comparator prioritizing Gold customers first, then by placement timestamp FIFO
    private final PriorityQueue<Order> unassignedReadyOrders = new PriorityQueue<>(
            Comparator.comparing((Order o) -> o.getCustomer().getLoyaltyTier() != LoyaltyTier.GOLD)
                    .thenComparing(Order::getPlacedAt)
    );

    public synchronized void addReadyOrder(Order order) {
        unassignedReadyOrders.offer(order);
    }

    public synchronized Order pollNextOrderForDispatch() {
        return unassignedReadyOrders.poll();
    }

    public synchronized int getQueueSize() {
        return unassignedReadyOrders.size();
    }
}