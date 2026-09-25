package domain;

import domain.menu.MenuItem;

import java.util.*;

public class Restaurant {
    private final String id;
    private final String displayName;
    private final String district;
    private final Set<String> cuisineCategories = new HashSet<>();
    private double averageRating;
    private boolean open;
    private final List<MenuItem> menu = new ArrayList<>();

    public Restaurant(String id, String displayName, String district, double averageRating, boolean open) {
        if (averageRating < 0.0 || averageRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }
        this.id = id;
        this.displayName = displayName;
        this.district = district;
        this.averageRating = averageRating;
        this.open = open;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDistrict() { return district; }
    public double getAverageRating() { return averageRating; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public Set<String> getCuisineCategories() {
        return Collections.unmodifiableSet(cuisineCategories);
    }

    public void addCuisineCategory(String category) {
        this.cuisineCategories.add(category);
    }

    public List<MenuItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    public void addMenuItem(MenuItem item) {
        this.menu.add(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}