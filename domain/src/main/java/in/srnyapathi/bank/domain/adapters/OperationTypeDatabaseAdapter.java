package in.srnyapathi.bank.domain.adapters;

import in.srnyapathi.bank.domain.model.OperationType;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Adapter interface for operation type database operations in the hexagonal architecture.
 * <p>
 * This interface defines the contract for managing operation type data, including CRUD
 * operations for operation types used in banking transactions. It acts as a port in the
 * hexagonal architecture pattern, allowing the domain layer to interact with the persistence
 * layer without direct coupling.
 * </p>
 * <p>
 * All operations are reactive and return {@link Mono} containing a map of operation types
 * indexed by their description strings, supporting non-blocking, asynchronous processing.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 */
public interface OperationTypeDatabaseAdapter {

    /**
     * Retrieves all available operation types from the database.
     * <p>
     * Fetches the complete collection of operation types that can be used for
     * banking transactions. The returned map uses the operation type description
     * as the key for quick lookups.
     * </p>
     *
     * @return a {@link Mono} emitting a map of operation types indexed by their description,
     *         or an empty map if no operation types exist, or an error signal if the retrieval fails
     */
    Mono<Map<String, OperationType>> getOperationTypes();

    /**
     * Updates an existing operation type in the database.
     * <p>
     * Modifies the specified operation type with new values. The operation type
     * must already exist in the database. After the update, returns the complete
     * updated collection of all operation types.
     * </p>
     *
     * @param operationType the operation type with updated values, must not be null
     * @return a {@link Mono} emitting the updated map of all operation types indexed by their description,
     *         or an error signal if the update fails or the operation type does not exist
     * @throws IllegalArgumentException if the operationType parameter is null
     */
    Mono<Map<String, OperationType>> updateOperationTypes(OperationType operationType);

    /**
     * Adds a new operation type to the database.
     * <p>
     * Creates a new operation type entry in the database. The operation type must not
     * already exist. After adding the new type, returns the complete collection of all
     * operation types including the newly added one.
     * </p>
     *
     * @param operationType the new operation type to add, must not be null
     * @return a {@link Mono} emitting the updated map of all operation types indexed by their description,
     *         or an error signal if the addition fails or the operation type already exists
     * @throws IllegalArgumentException if the operationType parameter is null
     */
    Mono<Map<String, OperationType>> addOperationType(OperationType operationType);

    /**
     * Deletes an operation type from the database by its unique identifier.
     * <p>
     * Removes the operation type with the specified ID from the database. After deletion,
     * returns the remaining collection of all operation types. This operation should
     * be used with caution as it may affect existing transactions that reference this
     * operation type.
     * </p>
     *
     * @param id the unique identifier of the operation type to delete, must not be null
     * @return a {@link Mono} emitting the updated map of remaining operation types indexed by their description,
     *         or an error signal if the deletion fails or the operation type does not exist
     * @throws IllegalArgumentException if the id parameter is null
     */
    Mono<Map<String, OperationType>> deleteOperationType(Long id);

}
