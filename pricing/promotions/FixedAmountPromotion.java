package pricing.promotions;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FixedAmountPromotion extends Promotion {
    private final BigDecimal amount;

    public FixedAmountPromotion(String code, LocalDate expiryDate, BigDecimal minSubtotal, String restrictedDistrict, boolean firstTimeCustomerOnly, BigDecimal amount) {
        super(code, expiryDate, minSubtotal, restrictedDistrict, firstTimeCustomerOnly);
        this.amount = amount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee) {
        return amount.min(subtotal);
    }
}