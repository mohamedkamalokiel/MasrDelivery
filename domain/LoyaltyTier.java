package domain;

public enum LoyaltyTier {
    BRONZE,
    SILVER,
    GOLD;

    public static LoyaltyTier fromCompletedOrders(int count) {
        if (count >= 30) return GOLD;
        if (count >= 10) return SILVER;
        return BRONZE;
    }
}