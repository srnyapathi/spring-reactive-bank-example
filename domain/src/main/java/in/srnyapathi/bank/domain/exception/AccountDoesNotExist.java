package in.srnyapathi.bank.domain.exception;

/**
 * Exception thrown when a requested account does not exist or is inactive in the system.
 * <p>
 * This exception is typically thrown during account lookup operations when:
 * <ul>
 *     <li>An account with the specified account number is not found in the database</li>
 *     <li>An account exists but is marked as inactive</li>
 * </ul>
 * </p>
 * <p>
 * In the REST API layer, this exception is mapped to an HTTP 404 NOT_FOUND response,
 * indicating that the requested account resource cannot be found or accessed.
 * </p>
 * <p>
 * This is a checked exception, requiring explicit handling by calling code in reactive
 * streams, typically via {@code Mono.error()} or similar error signaling mechanisms.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 * @see in.srnyapathi.bank.domain.adapters.AccountDatabaseAdapter#getAccountByAccountNumber(Long)
 */
public class AccountDoesNotExist extends Exception {

    /**
     * Constructs a new AccountDoesNotExist exception with the specified detail message.
     * <p>
     * The detail message should provide specific information about which account
     * was not found, typically including the account number or identifier.
     * </p>
     *
     * @param message the detail message explaining why the account does not exist
     *                (e.g., "Account not found or inactive: 12345")
     */
    public AccountDoesNotExist(String message) {
        super(message);
    }
}
