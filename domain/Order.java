package domain;

import events.OrderStatusChangeEvent;
import events.OrderSubject;
import exceptions.*;
import pricing.OrderTotalBreakdown;
import pricing.promotions.Promotion;
import pricing.PricingCalculator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Order extends OrderSubject {
    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Address deliveryAddress;
    private final Map<domain.menu.MenuItem, Double> lineItems;
    private final Promotion promotion;
    private final String deliveryNotes;
    private final LocalDateTime placedAt;
    private LocalDateTime deliveredAt;
    private OrderStatus status;
    private final OrderTotalBreakdown priceBreakdown;
    private Rider assignedRider;

    private Order(Builder builder) {
        this.id = builder.id;
        this.customer = builder.customer;
        this.restaurant = builder.restaurant;
        this.deliveryAddress = builder.deliveryAddress;
        this.lineItems = Collections.unmodifiableMap(builder.lineItems);
        this.promotion = builder.promotion;
        this.deliveryNotes = builder.deliveryNotes;
        this.placedAt = LocalDateTime.now();
        this.status = OrderStatus.PLACED;

        // Perform pricing validation upon building
        this.priceBreakdown = PricingCalculator.calculateTotal(customer, restaurant, deliveryAddress, lineItems, promotion);
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public Address getDeliveryAddress() { return deliveryAddress; }
    public Map<domain.menu.MenuItem, Double> getLineItems() { return lineItems; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public OrderStatus getStatus() { return status; }
    public OrderTotalBreakdown getPriceBreakdown() { return priceBreakdown; }
    public Rider getAssignedRider() { return assignedRider; }

    public void setAssignedRider(Rider rider) {
        this.assignedRider = rider;
    }

    public synchronized void transitionTo(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalOrderTransitionException("Cannot transition order " + id + " from " + status + " to " + newStatus);
        }
        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        if (newStatus == OrderStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
            this.customer.incrementCompletedOrders();
            if (this.assignedRider != null) {
                this.assignedRider.completeActiveOrder();
            }
        }
        notifyListeners(new OrderStatusChangeEvent(this, oldStatus, newStatus));
    }

    public Duration getDeliveryDuration() {
        if (deliveredAt == null) return Duration.ZERO;
        return Duration.between(placedAt, deliveredAt);
    }

    // Builder Pattern
    public static class Builder {
        private final String id;
        private final Customer customer;
        private final Restaurant restaurant;
        private final Address deliveryAddress;
        private final Map<domain.menu.MenuItem, Double> lineItems = new LinkedHashMap<>();
        private Promotion promotion;
        private String deliveryNotes;

        public Builder(String id, Customer customer, Restaurant restaurant, Address deliveryAddress) {
            if (customer == null || restaurant == null || deliveryAddress == null) {
                throw new IllegalArgumentException("Customer, restaurant, and address are mandatory");
            }
            if (!restaurant.isOpen()) {
                throw new ClosedRestaurantException("Cannot place order: Restaurant " + restaurant.getDisplayName() + " is closed.");
            }
            if (!customer.getSavedAddresses().contains(deliveryAddress)) {
                throw new IllegalArgumentException("Delivery address must belong to customer's saved addresses.");
            }
            this.id = id;
            this.customer = customer;
            this.restaurant = restaurant;
            this.deliveryAddress = deliveryAddress;
        }

        public Builder addLineItem(domain.menu.MenuItem item, double quantity) {
            if (!item.isAvailable()) {
                throw new ItemUnavailableException("Item " + item.getName() + " is currently unavailable.");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Item quantity/weight must be > 0");
            }
            lineItems.put(item, lineItems.getOrDefault(item, 0.0) + quantity);
            return this;
        }

        public Builder promotion(Promotion promotion) {
            this.promotion = promotion;
            return this;
        }

        public Builder deliveryNotes(String notes) {
            this.deliveryNotes = notes;
            return this;
        }

        public Order build() {
            if (lineItems.isEmpty()) {
                throw new IllegalArgumentException("An order must contain at least one line item.");
            }
            return new Order(this);
        }
    }
}