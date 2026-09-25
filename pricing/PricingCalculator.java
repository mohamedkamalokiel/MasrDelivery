package pricing;

import config.AppConfig;
import domain.*;
import pricing.promotions.FreeDeliveryPromotion;
import pricing.promotions.Promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class PricingCalculator {

    public static OrderTotalBreakdown calculateTotal(
            Customer customer,
            Restaurant restaurant,
            Address deliveryAddress,
            Map<domain.menu.MenuItem, Double> lineItems,
            Promotion promotion) {

        // 1. Subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (var entry : lineItems.entrySet()) {
            subtotal = subtotal.add(entry.getKey().calculateLineTotal(entry.getValue()));
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // 2. Base Delivery Fee
        double distance = DistrictDistanceMatrix.getDistance(restaurant.getDistrict(), deliveryAddress.getDistrict());
        BigDecimal baseDeliveryFee = AppConfig.getInstance().getBaseDeliveryFee();
        if (distance > 3.0) {
            double extraKm = distance - 3.0;
            BigDecimal extraFee = BigDecimal.valueOf(extraKm).multiply(AppConfig.getInstance().getExtraKmFeeRate());
            baseDeliveryFee = baseDeliveryFee.add(extraFee);
        }

        // Apply Customer Loyalty Benefit to Delivery Fee
        LoyaltyTier tier = customer.getLoyaltyTier();
        BigDecimal deliveryFee = baseDeliveryFee;
        if (tier == LoyaltyTier.SILVER) {
            deliveryFee = deliveryFee.multiply(new BigDecimal("0.90"));
        } else if (tier == LoyaltyTier.GOLD) {
            deliveryFee = BigDecimal.ZERO;
        }
        deliveryFee = deliveryFee.setScale(2, RoundingMode.HALF_UP);

        // 3. Service Fee (10% rounded to nearest piastre)
        BigDecimal serviceFee = subtotal.multiply(AppConfig.getInstance().getServiceFeeRate())
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Promotion
        BigDecimal discount = BigDecimal.ZERO;
        if (promotion != null) {
            promotion.validate(customer, restaurant, subtotal);
            if (promotion instanceof FreeDeliveryPromotion) {
                discount = promotion.calculateDiscount(subtotal, deliveryFee);
                deliveryFee = BigDecimal.ZERO; // Free delivery promo zeroes fee
                discount = BigDecimal.ZERO;    // Clean reporting breakdown
            } else {
                discount = promotion.calculateDiscount(subtotal, deliveryFee).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // 5. Total calculation
        BigDecimal total = subtotal.add(deliveryFee).add(serviceFee).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new OrderTotalBreakdown(subtotal, deliveryFee, serviceFee, discount, total.setScale(2, RoundingMode.HALF_UP));
    }
}