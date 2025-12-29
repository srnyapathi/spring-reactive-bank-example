package in.srnyapathi.bank.domain.exception;

/**
 * Exception thrown when an invalid transaction amount is provided during transaction processing.
 * <p>
 * This exception is typically thrown during transaction validation when:
 * <ul>
 *     <li>The transaction amount is null</li>
 *     <li>The transaction amount is negative (less than zero)</li>
 *     <li>The transaction amount is zero or less (when a positive amount is required)</li>
 *     <li>The amount fails business rule validation for the specific transaction type</li>
 * </ul>
 * </p>
 * <p>
 * In the REST API layer, this exception is mapped to an HTTP 400 BAD_REQUEST response,
 * indicating that the client provided an invalid amount that cannot be processed according
 * to business rules.
 * </p>
 * <p>
 * This is a checked exception, requiring explicit handling by calling code in reactive
 * streams, typically via {@code Mono.error()} or similar error signaling mechanisms.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 */
public class InvalidAmountException extends Exception {

    /**
     * Constructs a new InvalidAmountException with a default error message.
     * <p>
     * The default message is "Invalid amount provided", indicating a generic
     * validation failure for a transaction amount.
     * </p>
     */
    public InvalidAmountException() {
        super("Invalid amount provided");
    }

    /**
     * Constructs a new InvalidAmountException with the specified detail message.
     * <p>
     * The detail message should provide specific information about what made
     * the amount invalid, such as the actual constraint that was violated.
     * </p>
     *
     * @param message the detail message explaining why the amount is invalid
     *                (e.g., "Amount cannot be null", "Amount must be greater than zero")
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}
