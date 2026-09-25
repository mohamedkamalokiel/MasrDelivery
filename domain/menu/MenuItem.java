package domain.menu;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class MenuItem {
    private final String id;
    private final String name;
    private final BigDecimal price;
    private final String category;
    private final int prepTimeMinutes;
    private boolean available;

    public MenuItem(String id, String name, BigDecimal price, String category, int prepTimeMinutes, boolean available) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID cannot be empty");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Price must be > 0");
        if (prepTimeMinutes <= 0) throw new IllegalArgumentException("Prep time must be > 0");

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.prepTimeMinutes = prepTimeMinutes;
        this.available = available;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public abstract BigDecimal calculateLineTotal(double quantity);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}