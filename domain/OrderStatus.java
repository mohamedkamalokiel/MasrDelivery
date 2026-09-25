package domain;

import java.util.Set;

public enum OrderStatus {
    PLACED,
    ACCEPTED,
    PREPARING,
    READY,
    ASSIGNED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == CANCELLED || this == DELIVERED) return false;
        if (next == CANCELLED) {
            return this != OUT_FOR_DELIVERY && this != DELIVERED;
        }
        return switch (this) {
            case PLACED -> next == ACCEPTED;
            case ACCEPTED -> next == PREPARING;
            case PREPARING -> next == READY;
            case READY -> next == ASSIGNED;
            case ASSIGNED -> next == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == DELIVERED;
            default -> false;
        };
    }
}