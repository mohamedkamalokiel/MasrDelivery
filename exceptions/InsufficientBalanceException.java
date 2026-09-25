package exceptions;

public class InsufficientBalanceException extends MasrDeliveryException {
    public InsufficientBalanceException(String message) { super(message); }
}