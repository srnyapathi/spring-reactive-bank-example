package in.srnyapathi.bank.domain.adapters;

import in.srnyapathi.bank.domain.model.Account;
import reactor.core.publisher.Mono;

/**
 * Adapter interface for account database operations in the hexagonal architecture.
 * <p>
 * This interface defines the contract for persisting and retrieving account data,
 * acting as a port in the hexagonal architecture pattern. Implementations of this
 * interface should handle the actual database interactions while keeping the domain
 * logic independent of persistence details.
 * </p>
 * <p>
 * All operations are reactive and return {@link Mono} to support non-blocking,
 * asynchronous processing.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 */
public interface AccountDatabaseAdapter {

    /**
     * Creates a new account in the database.
     * <p>
     * Persists the provided account entity to the underlying data store.
     * The returned account may contain additional information such as
     * generated identifiers or timestamps added during persistence.
     * </p>
     *
     * @param account the account to be created, must not be null
     * @return a {@link Mono} emitting the created account with any generated values,
     *         or an error signal if the creation fails
     * @throws IllegalArgumentException if the account parameter is null
     */
    Mono<Account> createAccount(Account account);

    /**
     * Retrieves an account by its unique account number.
     * <p>
     * Queries the database for an account matching the specified account number.
     * If no account is found, the returned Mono will complete empty.
     * </p>
     *
     * @param accountNumber the unique identifier of the account to retrieve, must not be null
     * @return a {@link Mono} emitting the account if found, empty if not found,
     *         or an error signal if the retrieval fails
     * @throws IllegalArgumentException if the accountNumber parameter is null
     */
    Mono<Account> getAccountByAccountNumber(Long accountNumber);

}
