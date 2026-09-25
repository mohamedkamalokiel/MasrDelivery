package domain;

public enum VehicleType {
    MOTORCYCLE,
    BICYCLE,
    CAR;

    public boolean canHandleDistance(double distanceKm) {
        return switch (this) {
            case BICYCLE -> distanceKm <= 5.0;
            case MOTORCYCLE -> distanceKm <= 15.0;
            case CAR -> true;
        };
    }
}