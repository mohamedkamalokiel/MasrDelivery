package pricing.promotions;

import domain.Customer;
import domain.Restaurant;
import exceptions.InvalidPromotionException;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Promotion {
    private final String code;
    private final LocalDate expiryDate;
    private final BigDecimal minSubtotal;
    private final String restrictedDistrict;
    private final boolean firstTimeCustomerOnly;

    public Promotion(String code, LocalDate expiryDate, BigDecimal minSubtotal, String restrictedDistrict, boolean firstTimeCustomerOnly) {
        this.code = code.toUpperCase();
        this.expiryDate = expiryDate;
        this.minSubtotal = minSubtotal != null ? minSubtotal : BigDecimal.ZERO;
        this.restrictedDistrict = restrictedDistrict;
        this.firstTimeCustomerOnly = firstTimeCustomerOnly;
    }

    public String getCode() { return code; }

    public void validate(Customer customer, Restaurant restaurant, BigDecimal subtotal) {
        if (expiryDate != null && LocalDate.now().isAfter(expiryDate)) {
            throw new InvalidPromotionException("Promotion code expired.");
        }
        if (subtotal.compareTo(minSubtotal) < 0) {
            throw new InvalidPromotionException("Order subtotal " + subtotal + " EGP is below the minimum required " + minSubtotal + " EGP.");
        }
        if (restrictedDistrict != null && !restrictedDistrict.equalsIgnoreCase(restaurant.getDistrict())) {
            throw new InvalidPromotionException("Promotion is restricted to district: " + restrictedDistrict);
        }
        if (firstTimeCustomerOnly && customer.getCompletedOrderCount() > 0) {
            throw new InvalidPromotionException("Promotion is valid for first-time customers only.");
        }
    }

    public abstract BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee);
}