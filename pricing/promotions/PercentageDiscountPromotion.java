package pricing.promotions;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PercentageDiscountPromotion extends Promotion {
    private final BigDecimal percentage; // e.g., 0.20
    private final BigDecimal cap;

    public PercentageDiscountPromotion(String code, LocalDate expiryDate, BigDecimal minSubtotal, String restrictedDistrict, boolean firstTimeCustomerOnly, BigDecimal percentage, BigDecimal cap) {
        super(code, expiryDate, minSubtotal, restrictedDistrict, firstTimeCustomerOnly);
        this.percentage = percentage;
        this.cap = cap;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee) {
        BigDecimal discount = subtotal.multiply(percentage);
        if (cap != null && discount.compareTo(cap) > 0) {
            return cap;
        }
        return discount;
    }
}