package in.srnyapathi.bank.domain.exception;

/**
 * Exception thrown when an invalid or null account is provided during transaction or account operations.
 * <p>
 * This exception is typically thrown during validation when:
 * <ul>
 *     <li>An account object is null when a valid account is expected</li>
 *     <li>An account identifier (account number) is null or empty</li>
 *     <li>An account fails validation checks before processing</li>
 * </ul>
 * </p>
 * <p>
 * In the REST API layer, this exception is mapped to an HTTP 400 BAD_REQUEST response,
 * indicating that the client sent invalid account data that cannot be processed.
 * </p>
 * <p>
 * This is a checked exception, requiring explicit handling by calling code in reactive
 * streams, typically via {@code Mono.error()} or similar error signaling mechanisms.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 */
public class InvalidAccountException extends Exception {

    /**
     * Constructs a new InvalidAccountException with a default error message.
     * <p>
     * The default message is "Invalid Account provided", indicating a generic
     * validation failure for an account.
     * </p>
     */
    public InvalidAccountException() {
        super("Invalid Account provided");
    }

    /**
     * Constructs a new InvalidAccountException with the specified detail message.
     * <p>
     * The detail message should provide specific information about what made
     * the account invalid, such as which field was null or what validation failed.
     * </p>
     *
     * @param message the detail message explaining why the account is invalid
     *                (e.g., "Account cannot be null", "Account identifier cannot be null or empty")
     */
    public InvalidAccountException(String message) {
        super(message);
    }
}
