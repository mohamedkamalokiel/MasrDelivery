package domain.menu;

import java.math.BigDecimal;

public class StandardMenuItem extends MenuItem {
    public StandardMenuItem(String id, String name, BigDecimal price, String category, int prepTimeMinutes, boolean available) {
        super(id, name, price, category, prepTimeMinutes, available);
    }

    @Override
    public BigDecimal calculateLineTotal(double quantity) {
        return getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}