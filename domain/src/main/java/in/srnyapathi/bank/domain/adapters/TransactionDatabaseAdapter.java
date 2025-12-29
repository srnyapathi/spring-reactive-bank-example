package in.srnyapathi.bank.domain.adapters;

import in.srnyapathi.bank.domain.model.Transaction;
import reactor.core.publisher.Mono;

/**
 * Adapter interface for transaction database operations in the hexagonal architecture.
 * <p>
 * This interface defines the contract for persisting and retrieving transaction data,
 * acting as a port in the hexagonal architecture pattern. Implementations of this
 * interface should handle the actual database interactions while keeping the domain
 * logic independent of persistence details.
 * </p>
 * <p>
 * All operations are reactive and return {@link Mono} to support non-blocking,
 * asynchronous processing using Project Reactor.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 */
public interface TransactionDatabaseAdapter {

    /**
     * Saves a transaction to the database.
     * <p>
     * Persists the provided transaction entity to the underlying data store.
     * The returned transaction may contain additional information such as
     * generated identifiers or timestamps added during persistence.
     * </p>
     *
     * @param transaction the transaction to be saved, must not be null
     * @return a {@link Mono} emitting the saved transaction with any generated values,
     *         or an error signal if the save operation fails
     * @throws IllegalArgumentException if the transaction parameter is null
     */
    Mono<Transaction> saveTransaction(Transaction transaction);

    /**
     * Retrieves a transaction by its unique identifier.
     * <p>
     * Queries the database for a transaction matching the specified ID.
     * If no transaction is found, the returned Mono will complete empty.
     * </p>
     *
     * @param id the unique identifier of the transaction to retrieve, must not be null
     * @return a {@link Mono} emitting the transaction if found, empty if not found,
     *         or an error signal if the retrieval fails
     * @throws IllegalArgumentException if the id parameter is null
     */
    Mono<Transaction> getTransactionById(Long id);
}
