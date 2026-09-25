package domain.menu;

import java.math.BigDecimal;
import java.util.List;

public class MenuItemFactory {
    public static MenuItem createMenuItem(String type, String id, String name, BigDecimal price, String category, int prepTime, boolean available, List<MenuItem> bundled) {
        return switch (type.toUpperCase()) {
            case "STANDARD" -> new StandardMenuItem(id, name, price, category, prepTime, available);
            case "COMBO" -> new ComboMenuItem(id, name, price, category, prepTime, available, bundled != null ? bundled : List.of());
            case "WEIGHTED" -> new WeightedMenuItem(id, name, price, category, prepTime, available);
            default -> throw new IllegalArgumentException("Unknown menu item type: " + type);
        };
    }
}