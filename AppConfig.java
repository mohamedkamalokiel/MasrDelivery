package config;

import java.math.BigDecimal;

/**
 * Thread-safe Singleton managing global platform configurations.
 */
public final class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();

    private final BigDecimal baseDeliveryFee = new BigDecimal("15.00");
    private final BigDecimal extraKmFeeRate = new BigDecimal("3.00");
    private final BigDecimal serviceFeeRate = new BigDecimal("0.10");

    private AppConfig() {}

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public BigDecimal getBaseDeliveryFee() { return baseDeliveryFee; }
    public BigDecimal getExtraKmFeeRate() { return extraKmFeeRate; }
    public BigDecimal getServiceFeeRate() { return serviceFeeRate; }
}