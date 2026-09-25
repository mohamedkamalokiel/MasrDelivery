package domain;

import domain.menu.MenuItem;
import exceptions.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.*;

public class Customer {
    private final String id;
    private final String name;
    private final String mobileNumber;
    private final List<Address> savedAddresses = new ArrayList<>();
    private BigDecimal walletBalance;
    private int completedOrderCount = 0;
    private final LinkedList<String> recentSearches = new LinkedList<>();

    public Customer(String id, String name, String mobileNumber, BigDecimal initialWallet) {
        validateMobile(mobileNumber);
        this.id = id;
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.walletBalance = initialWallet != null ? initialWallet : BigDecimal.ZERO;
    }

    private void validateMobile(String mobile) {
        if (mobile == null || !mobile.matches("^01[0125]\\d{8}$")) {
            throw new IllegalArgumentException("Mobile number must be 11 digits starting with 010, 011, 012, or 015.");
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMobileNumber() { return mobileNumber; }
    public BigDecimal getWalletBalance() { return walletBalance; }
    public int getCompletedOrderCount() { return completedOrderCount; }
    public LoyaltyTier getLoyaltyTier() { return LoyaltyTier.fromCompletedOrders(completedOrderCount); }

    public List<Address> getSavedAddresses() {
        return Collections.unmodifiableList(savedAddresses);
    }

    public void addAddress(Address address) {
        if (!savedAddresses.contains(address)) savedAddresses.add(address);
    }

    public void incrementCompletedOrders() { this.completedOrderCount++; }

    public void deductWallet(BigDecimal amount) {
        if (walletBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Wallet balance (" + walletBalance + " EGP) insufficient for charge of " + amount + " EGP.");
        }
        walletBalance = walletBalance.subtract(amount);
    }

    public void refundWallet(BigDecimal amount) {
        walletBalance = walletBalance.add(amount);
    }

    public void addSearchQuery(String query) {
        recentSearches.remove(query);
        recentSearches.addFirst(query);
        if (recentSearches.size() > 5) recentSearches.removeLast();
    }

    public List<String> getRecentSearches() {
        return Collections.unmodifiableList(recentSearches);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}