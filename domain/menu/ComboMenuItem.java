package domain.menu;

import java.math.BigDecimal;
import java.util.List;

public class ComboMenuItem extends MenuItem {
    private final List<MenuItem> bundledItems;

    public ComboMenuItem(String id, String name, BigDecimal comboPrice, String category, int prepTimeMinutes, boolean available, List<MenuItem> bundledItems) {
        super(id, name, comboPrice, category, prepTimeMinutes, available);
        this.bundledItems = List.copyOf(bundledItems);
    }

    public List<MenuItem> getBundledItems() { return bundledItems; }

    @Override
    public BigDecimal calculateLineTotal(double quantity) {
        return getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}