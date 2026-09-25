package exceptions;

/**
 * Base custom unchecked exception for business/domain failures.
 */
public class MasrDeliveryException extends RuntimeException {
    public MasrDeliveryException(String message) {
        super(message);
    }
}