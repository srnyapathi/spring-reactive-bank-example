package in.srnyapathi.bank.domain.exception;

/**
 * Exception thrown when an invalid or null transaction object is provided during persistence operations.
 * <p>
 * This exception is typically thrown at the persistence layer when:
 * <ul>
 *     <li>A null transaction is passed to the save/persist operation</li>
 *     <li>A transaction object fails basic validation before database persistence</li>
 *     <li>Required transaction fields are missing or invalid</li>
 * </ul>
 * </p>
 * <p>
 * This exception serves as a guard at the persistence boundary, ensuring that only
 * valid transaction objects are attempted to be saved to the database. It prevents
 * null pointer exceptions and maintains data integrity.
 * </p>
 * <p>
 * This is a checked exception, requiring explicit handling by calling code in reactive
 * streams, typically via {@code Mono.error()} or similar error signaling mechanisms.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 */
public class InvalidTransactionObjectException extends Exception {

    /**
     * Constructs a new InvalidTransactionObjectException with the specified detail message.
     * <p>
     * The detail message should provide specific information about what made the
     * transaction object invalid or which validation failed.
     * </p>
     *
     * @param message the detail message explaining why the transaction object is invalid
     *                (e.g., "Transaction cannot be null")
     */
    public InvalidTransactionObjectException(String message) {
        super(message);
    }
}
