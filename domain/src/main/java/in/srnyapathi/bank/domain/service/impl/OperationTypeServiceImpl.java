package in.srnyapathi.bank.domain.service.impl;

import in.srnyapathi.bank.domain.adapters.OperationTypeDatabaseAdapter;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.service.OperationTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Default implementation of the {@link OperationTypeService} interface.
 * <p>
 * This service provides reactive access to operation type configurations used
 * throughout the banking system. It acts as a facade over the
 * {@link OperationTypeDatabaseAdapter}, providing a clean service-layer API
 * for retrieving operation type information.
 * </p>
 * <p>
 * Operation types define the various transaction types supported by the system
 * (such as cash purchases, withdrawals, payments, and installment purchases),
 * along with their associated handlers and transaction type classifications.
 * </p>
 * <p>
 * This implementation is stateless and thread-safe, delegating all data
 * retrieval to the underlying adapter layer.
 * </p>
 *
 * @author srnyapathi
 * @version 1.0.0
 * @since 1.0.0
 * @see OperationTypeService
 * @see OperationTypeDatabaseAdapter
 * @see OperationType
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OperationTypeServiceImpl implements OperationTypeService {

    /**
     * Adapter for operation type database operations.
     * <p>
     * This adapter handles the retrieval of operation type configurations from
     * the database, keeping the service layer decoupled from persistence details.
     * </p>
     */
    private final OperationTypeDatabaseAdapter operationTypeDatabase;

    /**
     * Retrieves all operation types configured in the system.
     * <p>
     * This method returns a map of all available operation types indexed by
     * their description (operation name). The map is used by transaction handlers
     * to look up their specific operation type configuration.
     * </p>
     * <p>
     * The operation types include metadata such as:
     * <ul>
     *     <li>Operation type ID (primary key)</li>
     *     <li>Description/name (e.g., "CASH_PURCHASE_TRANSACTION", "WITHDRAWAL")</li>
     *     <li>Handler class name for processing this operation type</li>
     *     <li>Transaction type (DEBIT or CREDIT)</li>
     * </ul>
     * </p>
     *
     * @return a {@link Mono} emitting a map of operation types indexed by their description,
     *         or an empty map if no operation types are configured, or an error signal
     *         if retrieval fails
     * @see OperationTypeDatabaseAdapter#getOperationTypes()
     */
    @Override
    public Mono<Map<String, OperationType>> getOperationType() {
        return operationTypeDatabase.getOperationTypes();
    }
}
