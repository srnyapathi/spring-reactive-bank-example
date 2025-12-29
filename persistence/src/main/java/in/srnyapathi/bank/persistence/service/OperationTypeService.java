package in.srnyapathi.bank.persistence.service;

import in.srnyapathi.bank.domain.adapters.OperationTypeDatabaseAdapter;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.persistence.mapper.OperationTypeMapper;
import in.srnyapathi.bank.persistence.repository.OperationTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation for managing operation types with in-memory caching.
 * <p>
 * This service provides a cached layer over the operation type repository, ensuring
 * fast access to operation types while maintaining consistency with the database.
 * The cache is initialized eagerly on application startup and updated synchronously
 * with database modifications.
 * </p>
 * <p>
 * Thread-safe operations are ensured through the use of {@link ConcurrentHashMap}
 * for the cache and reactive programming patterns for database operations.
 * </p>
 *
 * @see OperationTypeDatabaseAdapter
 * @see OperationType
 */
@Service
@Slf4j
public class OperationTypeService implements OperationTypeDatabaseAdapter {

    private final OperationTypeRepository operationTypeRepository;
    private final OperationTypeMapper mapper;

    /**
     * In-memory cache storing operation types indexed by their handler name.
     * Provides fast lookup for operation type validation and processing.
     */
    private final ConcurrentHashMap<String, OperationType> operationTypeCache = new ConcurrentHashMap<>();

    /**
     * Cached mono that handles the initialization of the operation type cache.
     * This ensures that the cache is loaded only once and shared across all subscribers.
     */
    private final Mono<Void> initMono;

    /**
     * Constructs a new OperationTypeService with the specified repository and mapper.
     * <p>
     * Initializes the cache loading mechanism using a deferred, cached Mono that will
     * execute once upon the first subscription and share the result with subsequent subscribers.
     * </p>
     *
     * @param operationTypeRepository the repository for accessing operation type data
     * @param mapper the mapper for converting between entity and domain models
     */
    public OperationTypeService(OperationTypeRepository operationTypeRepository,
                                OperationTypeMapper mapper) {
        this.operationTypeRepository = operationTypeRepository;
        this.mapper = mapper;
        this.initMono = Mono.defer(this::reloadCache)
                .doOnSubscribe(s -> log.info("Starting operation type cache initialization"))
                .doOnSuccess(v -> log.info("Successfully loaded operation types into cache"))
                .doOnError(ex -> log.error("Failed to initialize operation type cache", ex))
                .cache();
    }

    /**
     * Reloads the operation type cache from the database.
     * <p>
     * Fetches all active operation types from the repository, converts them to domain models,
     * and updates the in-memory cache. This operation clears existing cache entries to avoid
     * stale data and populates the cache with the latest data from the database.
     * </p>
     *
     * @return a Mono that completes when the cache has been successfully reloaded
     */
    private Mono<Void> reloadCache() {
        return operationTypeRepository.findAllActive()
                .map(mapper::toDomain)
                .collectMap(OperationType::getHandler)
                .doOnNext(map -> {
                    operationTypeCache.clear();        // avoids stale entries
                    operationTypeCache.putAll(map);
                })
                .then();
    }

    /**
     * Initializes the operation type cache on application startup.
     * <p>
     * This method is automatically invoked after the bean has been constructed and all
     * dependencies have been injected. It triggers the cache loading process by subscribing
     * to the initialization Mono. Any errors during initialization are logged but do not
     * prevent the application from starting.
     * </p>
     */
    @PostConstruct
    public void init() {
        initMono.subscribe(
                null,
                ex -> log.error("Error loading cache", ex)
        );
    }

    /**
     * Creates an immutable snapshot of the current operation type cache.
     * <p>
     * Returns a defensive copy of the cache to prevent external modifications
     * and ensure thread-safe read access to the cached data.
     * </p>
     *
     * @return a Mono emitting an immutable map of operation types indexed by handler name
     */
    private Mono<Map<String, OperationType>> snapshot() {
        return Mono.fromSupplier(() -> Map.copyOf(operationTypeCache));
    }

    /**
     * Retrieves all operation types from the cache.
     * <p>
     * Ensures the cache is initialized before returning the snapshot. If the cache
     * is not yet initialized, this method will trigger initialization and wait for
     * its completion.
     * </p>
     *
     * @return a Mono emitting an immutable map of all cached operation types indexed by handler name
     */
    @Override
    public Mono<Map<String, OperationType>> getOperationTypes() {
        return initMono.then(snapshot());
    }

    /**
     * Saves an operation type to the database and updates the cache.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Validates that the operation type is not null</li>
     *   <li>Ensures the cache is initialized</li>
     *   <li>Persists the operation type to the database</li>
     *   <li>Updates the cache with the saved operation type</li>
     *   <li>Returns an immutable snapshot of the updated cache</li>
     * </ol>
     * </p>
     *
     * @param operationType the operation type to save
     * @return a Mono emitting an immutable map of all operation types after the save operation
     * @throws IllegalArgumentException if the operation type is null
     */
    private Mono<Map<String, OperationType>> saveAndCache(OperationType operationType) {
        if (operationType == null) {
            return Mono.error(new IllegalArgumentException("OperationType cannot be null"));
        }

        return initMono.then(
                Mono.just(operationType)
                        .flatMap(op ->
                                operationTypeRepository.save(mapper.toEntity(op))
                                        .map(mapper::toDomain)
                                        .doOnNext(saved -> operationTypeCache.put(saved.getHandler(), saved))
                                        .then(snapshot())
                        )
        );
    }

    /**
     * Adds a new operation type to the system.
     * <p>
     * Creates a new operation type entry in the database and updates the cache.
     * The operation type will be indexed by its handler name for fast retrieval.
     * </p>
     *
     * @param operationType the operation type to add
     * @return a Mono emitting an immutable map of all operation types after the addition
     * @throws IllegalArgumentException if the operation type is null
     */
    @Override
    public Mono<Map<String, OperationType>> addOperationType(OperationType operationType) {
        return saveAndCache(operationType);
    }

    /**
     * Updates an existing operation type in the system.
     * <p>
     * Modifies an operation type entry in the database and refreshes the cache
     * with the updated information. If the handler name changes, the cache key
     * will be updated accordingly.
     * </p>
     *
     * @param operationType the operation type with updated values
     * @return a Mono emitting an immutable map of all operation types after the update
     * @throws IllegalArgumentException if the operation type is null
     */
    @Override
    public Mono<Map<String, OperationType>> updateOperationTypes(OperationType operationType) {
        return saveAndCache(operationType);
    }

    /**
     * Deletes an operation type from the system.
     * <p>
     * Removes the operation type from the database and evicts it from the cache.
     * If the operation type with the given ID does not exist, the cache remains
     * unchanged and an updated snapshot is still returned.
     * </p>
     *
     * @param id the unique identifier of the operation type to delete
     * @return a Mono emitting an immutable map of all operation types after the deletion
     */
    @Override
    public Mono<Map<String, OperationType>> deleteOperationType(Long id) {
        return initMono.then(
                operationTypeRepository.findById(id)
                        .map(mapper::toDomain)
                        .flatMap(operationType ->
                                operationTypeRepository.deleteById(id)
                                        .doOnSuccess(v -> operationTypeCache.remove(operationType.getHandler()))
                                        .then(snapshot())
                        )
                        .switchIfEmpty(snapshot())
        );
    }

}