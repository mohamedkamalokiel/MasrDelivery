package pricing.promotions;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FreeDeliveryPromotion extends Promotion {
    public FreeDeliveryPromotion(String code, LocalDate expiryDate, BigDecimal minSubtotal, String restrictedDistrict, boolean firstTimeCustomerOnly) {
        super(code, expiryDate, minSubtotal, restrictedDistrict, firstTimeCustomerOnly);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee) {
        return deliveryFee;
    }
}