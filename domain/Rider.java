package domain;

import exceptions.RiderBusyException;

import java.util.Objects;

public class Rider {
    private final String id;
    private final String name;
    private final VehicleType vehicleType;
    private String currentDistrict;
    private boolean available = true;
    private int completedDeliveries = 0;
    private Order activeOrder = null;

    public Rider(String id, String name, VehicleType vehicleType, String currentDistrict) {
        this.id = id;
        this.name = name;
        this.vehicleType = vehicleType;
        this.currentDistrict = currentDistrict;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getCurrentDistrict() { return currentDistrict; }
    public void setCurrentDistrict(String district) { this.currentDistrict = district; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getCompletedDeliveries() { return completedDeliveries; }
    public Order getActiveOrder() { return activeOrder; }

    public void assignOrder(Order order) {
        if (activeOrder != null || !available) {
            throw new RiderBusyException("Rider " + id + " is currently busy with another order.");
        }
        this.activeOrder = order;
        this.available = false;
    }

    public void completeActiveOrder() {
        if (this.activeOrder != null) {
            this.completedDeliveries++;
            this.activeOrder = null;
            this.available = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rider rider = (Rider) o;
        return Objects.equals(id, rider.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}