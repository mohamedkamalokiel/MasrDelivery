import dispatch.DispatchService;
import domain.*;
import domain.menu.*;
import events.OrderStatusListener;
import pricing.OrderTotalBreakdown;
import pricing.promotions.PercentageDiscountPromotion;
import pricing.promotions.Promotion;
import reports.AnalyticsService;
import search.RestaurantSearchService;
import search.SearchCriteria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainConsoleApp {
    private static final Map<String, Restaurant> restaurants = new LinkedHashMap<>();
    private static final Map<String, Customer> customers = new HashMap<>();
    private static final Map<String, Rider> riders = new HashMap<>();
    private static final Map<String, Order> orders = new HashMap<>();
    private static final Map<String, Promotion> promotions = new HashMap<>();

    private static final DispatchService dispatchService = new DispatchService();
    private static final AnalyticsService analyticsService = new AnalyticsService();
    private static final Scanner scanner = new Scanner(System.in);
    private static int orderSeq = 1;

    public static void main(String[] args) {
        seedSampleData();

        while (true) {
            System.out.println("\n=======================================");
            System.out.println("       MASR DELIVERY Main Menu");
            System.out.println("=======================================");
            System.out.println("1. Customer Area");
            System.out.println("2. Restaurant Area");
            System.out.println("3. Rider Area");
            System.out.println("4. Admin & Reports");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> customerMenu();
                case "2" -> restaurantMenu();
                case "3" -> riderMenu();
                case "4" -> adminMenu();
                case "0" -> {
                    System.out.println("Exiting Masr Delivery platform. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option. Please re-enter.");
            }
        }
    }

    private static void customerMenu() {
        System.out.print("Enter Customer ID (e.g., C101): ");
        String cid = scanner.nextLine().trim();
        Customer customer = customers.get(cid);
        if (customer == null) {
            System.out.println("Customer non-existent.");
            return;
        }

        while (true) {
            System.out.println("\n--- Customer Area (" + customer.getName() + " - Tier: " + customer.getLoyaltyTier() + ") ---");
            System.out.println("1. Browse Restaurants");
            System.out.println("2. Search Restaurants");
            System.out.println("3. View Restaurant Menu");
            System.out.println("4. Place Order");
            System.out.println("5. Pay / Top Up Wallet");
            System.out.println("6. Track Order");
            System.out.println("7. Cancel Order");
            System.out.println("8. Order History");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1" -> {
                    List<Restaurant> list = RestaurantSearchService.search(restaurants.values(), new SearchCriteria(null, null, null, null));
                    int idx = 1;
                    for (Restaurant r : list) {
                        System.out.printf("%d. %s [%s] - Rating: %.1f - Cuisines: %s\n", idx++, r.getDisplayName(), r.getDistrict(), r.getAverageRating(), r.getCuisineCategories());
                    }
                }
                case "2" -> {
                    System.out.print("Enter search term (name or cuisine): ");
                    String term = scanner.nextLine().trim();
                    customer.addSearchQuery(term);
                    List<Restaurant> matches = restaurants.values().stream()
                            .filter(r -> r.getDisplayName().toLowerCase().contains(term.toLowerCase()) ||
                                    r.getCuisineCategories().stream().anyMatch(c -> c.toLowerCase().contains(term.toLowerCase())))
                            .toList();
                    if (matches.isEmpty()) {
                        System.out.println("Nothing found matching: " + term);
                    } else {
                        matches.forEach(r -> System.out.println("• " + r.getDisplayName() + " (" + r.getDistrict() + ")"));
                    }
                }
                case "3" -> {
                    System.out.print("Enter Restaurant ID (e.g., R1): ");
                    String rid = scanner.nextLine().trim();
                    Restaurant r = restaurants.get(rid);
                    if (r != null) {
                        System.out.println("\nMenu for " + r.getDisplayName() + ":");
                        r.getMenu().forEach(item -> System.out.printf(" - %s (%s): %.2f EGP %s\n",
                                item.getName(), item.getId(), item.getPrice(), item.isAvailable() ? "" : "[UNAVAILABLE]"));
                    } else {
                        System.out.println("Restaurant not found.");
                    }
                }
                case "4" -> placeOrderFlow(customer);
                case "5" -> {
                    System.out.println("Current Wallet Balance: " + customer.getWalletBalance() + " EGP");
                    System.out.print("Enter amount to add: ");
                    try {
                        BigDecimal add = new BigDecimal(scanner.nextLine().trim());
                        customer.refundWallet(add);
                        System.out.println("Updated Balance: " + customer.getWalletBalance() + " EGP");
                    } catch (Exception e) {
                        System.out.println("Invalid amount.");
                    }
                }
                case "6" -> {
                    System.out.print("Enter Order ID: ");
                    String oid = scanner.nextLine().trim();
                    Order o = orders.get(oid);
                    if (o != null && o.getCustomer().equals(customer)) {
                        System.out.println("Status: " + o.getStatus() + " (Placed at: " + o.getPlacedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
                    } else {
                        System.out.println("Order not found.");
                    }
                }
                case "7" -> {
                    System.out.print("Enter Order ID to cancel: ");
                    String oid = scanner.nextLine().trim();
                    Order o = orders.get(oid);
                    if (o != null && o.getCustomer().equals(customer)) {
                        try {
                            o.transitionTo(OrderStatus.CANCELLED);
                            customer.refundWallet(o.getPriceBreakdown().total());
                            System.out.println("Order cancelled successfully. Wallet refunded.");
                        } catch (Exception e) {
                            System.out.println("Cancellation failed: " + e.getMessage());
                        }
                    }
                }
                case "8" -> {
                    var history = analyticsService.getCustomerOrderHistory(customer, new ArrayList<>(orders.values()));
                    System.out.println("Lifetime Total Spent: " + history.totalSpent() + " EGP");
                    history.orders().forEach(o -> System.out.println("Order #" + o.getId() + " | Status: " + o.getStatus() + " | Total: " + o.getPriceBreakdown().total() + " EGP"));
                }
                case "0" -> { return; }
            }
        }
    }

    private static void placeOrderFlow(Customer customer) {
        try {
            System.out.print("Enter Restaurant ID: ");
            String rid = scanner.nextLine().trim();
            Restaurant restaurant = restaurants.get(rid);
            if (restaurant == null) throw new IllegalArgumentException("Restaurant not found");

            Address address = customer.getSavedAddresses().get(0); // Select first saved address
            Order.Builder builder = new Order.Builder("ORD" + (orderSeq++), customer, restaurant, address);

            System.out.print("Enter Item ID to order: ");
            String itemId = scanner.nextLine().trim();
            MenuItem item = restaurant.getMenu().stream().filter(m -> m.getId().equalsIgnoreCase(itemId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));

            System.out.print("Enter quantity/weight in kg: ");
            double qty = Double.parseDouble(scanner.nextLine().trim());
            builder.addLineItem(item, qty);

            System.out.print("Enter Promo Code (or press Enter to skip): ");
            String promoCode = scanner.nextLine().trim();
            if (!promoCode.isBlank()) {
                Promotion promo = promotions.get(promoCode.toUpperCase());
                if (promo != null) builder.promotion(promo);
            }

            Order order = builder.build();
            OrderTotalBreakdown breakdown = order.getPriceBreakdown();

            System.out.println("\n--- Order Breakdown ---");
            System.out.println("Subtotal: " + breakdown.subtotal() + " EGP");
            System.out.println("Delivery Fee: " + breakdown.deliveryFee() + " EGP");
            System.out.println("Service Fee: " + breakdown.serviceFee() + " EGP");
            System.out.println("Discount: -" + breakdown.discount() + " EGP");
            System.out.println("TOTAL: " + breakdown.total() + " EGP");

            // Deduct payment
            customer.deductWallet(breakdown.total());

            // Attach event listeners for notifications / audit logging
            order.addListener(event -> System.out.println("[AUDIT LOG] Order " + event.order().getId() + " shifted: " + event.previousStatus() + " -> " + event.newStatus()));

            orders.put(order.getId(), order);
            System.out.println("Order placed successfully! Reference: " + order.getId());

        } catch (Exception e) {
            System.out.println("Order placement failed: " + e.getMessage());
        }
    }

    private static void restaurantMenu() {
        System.out.print("Enter Restaurant ID: ");
        String rid = scanner.nextLine().trim();
        Restaurant restaurant = restaurants.get(rid);
        if (restaurant == null) {
            System.out.println("Restaurant non-existent.");
            return;
        }

        System.out.println("\n--- Restaurant Portal (" + restaurant.getDisplayName() + ") ---");
        System.out.println("1. Toggle Open/Closed Status");
        System.out.println("2. Accept Pending Orders");
        System.out.print("Choose: ");
        String opt = scanner.nextLine().trim();
        if ("1".equals(opt)) {
            restaurant.setOpen(!restaurant.isOpen());
            System.out.println("Restaurant open status is now: " + restaurant.isOpen());
        } else if ("2".equals(opt)) {
            orders.values().stream()
                    .filter(o -> o.getRestaurant().equals(restaurant) && o.getStatus() == OrderStatus.PLACED)
                    .forEach(o -> {
                        o.transitionTo(OrderStatus.ACCEPTED);
                        o.transitionTo(OrderStatus.PREPARING);
                        o.transitionTo(OrderStatus.READY);
                        dispatchService.addReadyOrder(o);
                        System.out.println("Order #" + o.getId() + " is ACCEPTED -> PREPARING -> READY and pushed to Dispatch.");
                    });
        }
    }

    private static void riderMenu() {
        System.out.print("Enter Rider ID (e.g., K1): ");
        String rId = scanner.nextLine().trim();
        Rider rider = riders.get(rId);
        if (rider == null) {
            System.out.println("Rider non-existent.");
            return;
        }

        System.out.println("\n--- Rider Portal (" + rider.getName() + " - " + rider.getVehicleType() + ") ---");
        System.out.println("1. Dispatch / Accept Longest Waiting Order");
        System.out.println("2. Mark Active Order Delivered");
        System.out.print("Choose: ");
        String opt = scanner.nextLine().trim();
        if ("1".equals(opt)) {
            Order nextOrder = dispatchService.pollNextOrderForDispatch();
            if (nextOrder != null) {
                rider.assignOrder(nextOrder);
                nextOrder.setAssignedRider(rider);
                nextOrder.transitionTo(OrderStatus.ASSIGNED);
                nextOrder.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
                System.out.println("Assigned Order #" + nextOrder.getId() + " to rider " + rider.getName());
            } else {
                System.out.println("No pending READY orders in dispatch queue.");
            }
        } else if ("2".equals(opt)) {
            if (rider.getActiveOrder() != null) {
                Order active = rider.getActiveOrder();
                active.transitionTo(OrderStatus.DELIVERED);
                System.out.println("Order #" + active.getId() + " delivered successfully!");
            } else {
                System.out.println("No active order currently assigned.");
            }
        }
    }

    private static void adminMenu() {
        System.out.println("\n--- Admin & Reports ---");
        System.out.println("1. Total Revenue");
        System.out.println("2. Count Orders by Status");
        System.out.println("3. Peak Ordering Hour");
        System.out.println("4. Most Frequently Ordered Item");
        System.out.print("Choose: ");
        String opt = scanner.nextLine().trim();
        if ("1".equals(opt)) {
            BigDecimal rev = analyticsService.calculateTotalRevenue(new ArrayList<>(orders.values()), LocalDateTime.now().minusDays(7), LocalDateTime.now());
            System.out.println("Revenue (Last 7 Days): " + rev + " EGP");
        } else if ("2".equals(opt)) {
            System.out.println(analyticsService.getOrderCountByStatus(new ArrayList<>(orders.values())));
        } else if ("3".equals(opt)) {
            analyticsService.getPeakOrderingHour(new ArrayList<>(orders.values()))
                    .ifPresentOrElse(h -> System.out.println("Peak Hour: " + h + ":00"), () -> System.out.println("No orders logged."));
        } else if ("4".equals(opt)) {
            analyticsService.getMostFrequentlyOrderedMenuItem(new ArrayList<>(orders.values()))
                    .ifPresentOrElse(entry -> System.out.println("Top Item: " + entry.getKey().getName() + " (" + entry.getValue() + " orders)"),
                            () -> System.out.println("No menu items ordered yet."));
        }
    }

    private static void seedSampleData() {
        // Customer
        Customer c1 = new Customer("C101", "Ahmed Ali", "01012345678", new BigDecimal("500.00"));
        c1.addAddress(new Address("Faisal", "12 El-Tahrir Street"));
        customers.put(c1.getId(), c1);

        // Restaurant
        Restaurant r1 = new Restaurant("R1", "Kebabgy El Shark", "Maadi", 4.8, true);
        r1.addCuisineCategory("Grill");
        r1.addMenuItem(MenuItemFactory.createMenuItem("STANDARD", "M1", "Kofta Plate", new BigDecimal("120.00"), "Main", 20, true, null));
        r1.addMenuItem(MenuItemFactory.createMenuItem("WEIGHTED", "M2", "Grilled Meat (per Kg)", new BigDecimal("400.00"), "Grill", 30, true, null));
        restaurants.put(r1.getId(), r1);

        // Rider
        Rider rider1 = new Rider("K1", "Hassan Rider", VehicleType.MOTORCYCLE, "Maadi");
        riders.put(rider1.getId(), rider1);

        // Promotion (NILE20 test promo: 20% capped at 50 EGP)
        Promotion promo = new PercentageDiscountPromotion("NILE20", LocalDate.now().plusDays(10), BigDecimal.ZERO, null, false, new BigDecimal("0.20"), new BigDecimal("50.00"));
        promotions.put(promo.getCode(), promo);
    }
}