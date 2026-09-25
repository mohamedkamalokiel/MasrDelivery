package search;

import domain.Restaurant;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class RestaurantSearchService {

    public static List<Restaurant> search(Collection<Restaurant> restaurants, SearchCriteria criteria) {
        return restaurants.stream()
                .filter(r -> r.isOpen())
                .filter(r -> criteria.district() == null || r.getDistrict().equalsIgnoreCase(criteria.district()))
                .filter(r -> criteria.cuisine() == null || r.getCuisineCategories().stream().anyMatch(c -> c.equalsIgnoreCase(criteria.cuisine())))
                .filter(r -> criteria.minRating() == null || r.getAverageRating() >= criteria.minRating())
                .filter(r -> criteria.priceCeiling() == null || r.getMenu().stream().anyMatch(item -> item.getPrice().compareTo(criteria.priceCeiling()) <= 0))
                .sorted(Comparator.comparing(Restaurant::getAverageRating).reversed()
                        .thenComparing(Restaurant::getDisplayName))
                .toList();
    }
}