package in.srnyapathi.bank.domain.exception;

/**
 * Exception thrown when an invalid or unsupported operation type is encountered during transaction processing.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *     <li>A transaction handler cannot be found for the specified operation type</li>
 *     <li>The operation type does not exist in the system's operation type registry</li>
 *     <li>The operation type map is empty or does not contain the required operation type</li>
 *     <li>An operation type validation fails during transaction processing</li>
 * </ul>
 * </p>
 * <p>
 * In the REST API layer, this exception is mapped to an HTTP 400 BAD_REQUEST response,
 * indicating that the client specified an operation type that is not recognized or supported
 * by the system.
 * </p>
 * <p>
 * This is a checked exception, requiring explicit handling by calling code in reactive
 * streams, typically via {@code Mono.error()} or similar error signaling mechanisms.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 */
public class InvalidOperationTypeException extends Exception {

    /**
     * Constructs a new InvalidOperationTypeException with the specified detail message.
     * <p>
     * The detail message should provide specific information about which operation type
     * was not found or why it is invalid.
     * </p>
     *
     * @param message the detail message explaining why the operation type is invalid
     *                (e.g., "Unable to find the transaction handler for operation type: PAYMENT")
     */
    public InvalidOperationTypeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidOperationTypeException with the specified detail message and cause.
     * <p>
     * This constructor is useful when wrapping another exception that occurred while
     * trying to retrieve or validate the operation type.
     * </p>
     *
     * @param message the detail message explaining why the operation type is invalid
     * @param cause the underlying cause of this exception (e.g., database error, mapping error)
     */
    public InvalidOperationTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
