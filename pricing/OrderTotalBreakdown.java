package pricing;

import java.math.BigDecimal;

public record OrderTotalBreakdown(
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal serviceFee,
        BigDecimal discount,
        BigDecimal total
) {}