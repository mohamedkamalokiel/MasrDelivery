package domain.menu;

import java.math.BigDecimal;

public class WeightedMenuItem extends MenuItem {
    public WeightedMenuItem(String id, String name, BigDecimal pricePerKg, String category, int prepTimeMinutes, boolean available) {
        super(id, name, pricePerKg, category, prepTimeMinutes, available);
    }

    @Override
    public BigDecimal calculateLineTotal(double weightInKg) {
        return getPrice().multiply(BigDecimal.valueOf(weightInKg));
    }
}