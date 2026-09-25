package reports;

import domain.Order;
import domain.OrderStatus;
import domain.Restaurant;
import domain.Rider;
import domain.Customer;
import domain.menu.MenuItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {

    // 1. Total revenue for a given date range
    public BigDecimal calculateTotalRevenue(List<Order> orders, LocalDateTime start, LocalDateTime end) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> !o.getPlacedAt().isBefore(start) && !o.getPlacedAt().isAfter(end))
                .map(o -> o.getPriceBreakdown().total())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 2. Top five restaurants by revenue for a given month
    public List<Map.Entry<Restaurant, BigDecimal>> getTopFiveRestaurantsByRevenue(List<Order> orders, YearMonth month) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> YearMonth.from(o.getPlacedAt()).equals(month))
                .collect(Collectors.groupingBy(Order::getRestaurant,
                        Collectors.reducing(BigDecimal.ZERO, o -> o.getPriceBreakdown().total(), BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<Restaurant, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .toList();
    }

    // 3. Average order value per district
    public Map<String, Double> getAverageOrderValuePerDistrict(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(
                        o -> o.getRestaurant().getDistrict(),
                        Collectors.averagingDouble(o -> o.getPriceBreakdown().total().doubleValue())
                ));
    }

    // 4. Restaurants with rating > 4.5 and at least 20 completed orders
    public List<Restaurant> getHighlyRatedPopularRestaurants(List<Restaurant> restaurants, List<Order> orders) {
        Map<Restaurant, Long> completedCounts = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(Order::getRestaurant, Collectors.counting()));

        return restaurants.stream()
                .filter(r -> r.getAverageRating() > 4.5)
                .filter(r -> completedCounts.getOrDefault(r, 0L) >= 20)
                .toList();
    }

    // 5. Count of orders grouped by current status
    public Map<OrderStatus, Long> getOrderCountByStatus(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    // 6. Each rider's completed deliveries and average duration (sorted by deliveries descending)
    public List<RiderPerformance> getRiderPerformance(List<Rider> riders, List<Order> orders) {
        Map<Rider, List<Order>> riderOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getAssignedRider() != null)
                .collect(Collectors.groupingBy(Order::getAssignedRider));

        return riders.stream()
                .map(r -> {
                    List<Order> completed = riderOrders.getOrDefault(r, List.of());
                    double avgDurationMins = completed.stream()
                            .mapToLong(o -> o.getDeliveryDuration().toMinutes())
                            .average().orElse(0.0);
                    return new RiderPerformance(r.getName(), r.getCompletedDeliveries(), avgDurationMins);
                })
                .sorted(Comparator.comparing(RiderPerformance::completedDeliveries).reversed())
                .toList();
    }

    public record RiderPerformance(String riderName, int completedDeliveries, double avgDurationMinutes) {}

    // 7. Most frequently ordered menu item (Optional safely returned)
    public Optional<Map.Entry<MenuItem, Long>> getMostFrequentlyOrderedMenuItem(List<Order> orders) {
        return orders.stream()
                .flatMap(o -> o.getLineItems().keySet().stream())
                .collect(Collectors.groupingBy(item -> item, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    // 8. Customer's full order history, newest first, with total spent
    public CustomerOrderHistory getCustomerOrderHistory(Customer customer, List<Order> orders) {
        List<Order> customerOrders = orders.stream()
                .filter(o -> o.getCustomer().equals(customer))
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .toList();

        BigDecimal totalSpent = customerOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(o -> o.getPriceBreakdown().total())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerOrderHistory(customerOrders, totalSpent);
    }

    public record CustomerOrderHistory(List<Order> orders, BigDecimal totalSpent) {}

    // 9. Peak ordering hour of the day
    public Optional<Integer> getPeakOrderingHour(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(o -> o.getPlacedAt().getHour(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    // 10. Customers inactive in the last 30 days
    public List<Customer> getInactiveCustomers(List<Customer> customers, List<Order> orders) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        Set<Customer> activeCustomers = orders.stream()
                .filter(o -> o.getPlacedAt().isAfter(thirtyDaysAgo))
                .map(Order::getCustomer)
                .collect(Collectors.toSet());

        return customers.stream()
                .filter(c -> !activeCustomers.contains(c))
                .toList();
    }
}