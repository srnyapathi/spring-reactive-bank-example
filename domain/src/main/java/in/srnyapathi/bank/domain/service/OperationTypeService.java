package in.srnyapathi.bank.domain.service;

import in.srnyapathi.bank.domain.model.OperationType;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service interface for managing operation types in the banking domain.
 * Provides reactive operations for retrieving operation type information.
 */
public interface OperationTypeService {
    /**
     * Retrieves all available operation types as a map.
     *
     * @return a {@link Mono} emitting a map where keys are operation type identifiers
     *         and values are {@link OperationType} objects
     */
    Mono<Map<String, OperationType>> getOperationType();

}
