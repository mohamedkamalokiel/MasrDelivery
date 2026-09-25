package events;

import domain.Order;
import domain.OrderStatus;

public record OrderStatusChangeEvent(Order order, OrderStatus previousStatus, OrderStatus newStatus) {}