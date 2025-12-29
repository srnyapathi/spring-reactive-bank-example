package in.srnyapathi.bank.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create an account that already exists in the system.
 * <p>
 * This exception is typically thrown during account creation operations when a database
 * constraint violation occurs, specifically when:
 * <ul>
 *     <li>An account with the same document number already exists in the database</li>
 *     <li>A unique constraint violation is detected (mapped from {@link org.springframework.dao.DuplicateKeyException})</li>
 * </ul>
 * </p>
 * <p>
 * In the REST API layer, this exception is mapped to an HTTP 409 CONFLICT response,
 * indicating that the request conflicts with the current state of the resource on the server.
 * The {@code @ResponseStatus} annotation provides a fallback HTTP status and reason phrase
 * for non-reactive endpoints.
 * </p>
 * <p>
 * This is a checked exception, requiring explicit handling by calling code in reactive
 * streams, typically via {@code onErrorMap()} or similar error mapping mechanisms.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 * @see in.srnyapathi.bank.domain.service.AccountService#createAccount(in.srnyapathi.bank.domain.model.Account)
 * @see org.springframework.dao.DuplicateKeyException
 */
@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Account with given details already exists")
public class DuplicateAccountException extends Exception {

    /**
     * Constructs a new DuplicateAccountException with the specified detail message.
     * <p>
     * The detail message should provide specific information about which account
     * attribute caused the duplication, typically the document number.
     * </p>
     *
     * @param message the detail message explaining why the account is considered a duplicate
     *                (e.g., "Account with document number already exists")
     */
    public DuplicateAccountException(String message) {
        super(message);
    }
}
