package in.srnyapathi.bank.domain.service.impl;

import in.srnyapathi.bank.domain.adapters.OperationTypeDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidOperationTypeException;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.service.TransactionService;
import in.srnyapathi.bank.domain.service.factory.TransactionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link TransactionService} interface.
 * <p>
 * This service orchestrates transaction processing by coordinating between the
 * operation type configuration, transaction factory, and appropriate transaction
 * handlers. It implements the Strategy pattern through dynamic handler selection
 * based on the operation type ID.
 * </p>
 * <p>
 * The transaction processing workflow involves:
 * <ol>
 *     <li>Retrieving all operation type configurations from the database</li>
 *     <li>Looking up the specific operation type by ID</li>
 *     <li>Extracting the handler name from the operation type</li>
 *     <li>Resolving the appropriate transaction handler via the factory</li>
 *     <li>Delegating transaction execution to the resolved handler</li>
 *     <li>Logging the transaction result or error</li>
 * </ol>
 * </p>
 * <p>
 * This implementation is reactive and non-blocking, leveraging Project Reactor
 * for asynchronous processing. All operations return {@link Mono} to support
 * integration with reactive streams.
 * </p>
 *
 * @author srnyapathi
 * @version 1.0.0
 * @since 1.0.0
 * @see TransactionService
 * @see TransactionFactory
 * @see TransactionHandler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    /**
     * Factory for resolving transaction handlers based on operation type.
     * <p>
     * This factory provides the handler lookup mechanism, mapping operation type
     * names to their corresponding handler implementations.
     * </p>
     */
    private final TransactionFactory transactionFactory;

    /**
     * Adapter for retrieving operation type configurations from the database.
     * <p>
     * This adapter provides access to operation type metadata including IDs,
     * descriptions, handler names, and transaction types (DEBIT/CREDIT).
     * </p>
     */
    private final OperationTypeDatabaseAdapter operationTypeDatabaseAdapter;

    /**
     * Performs a banking transaction for the specified account and operation type.
     * <p>
     * This method implements the core transaction processing workflow:
     * <ol>
     *     <li>Fetches all operation types from the database</li>
     *     <li>Transforms the map to be indexed by operation type ID for efficient lookup</li>
     *     <li>Validates that the provided operation type ID exists</li>
     *     <li>Extracts the handler name from the operation type configuration</li>
     *     <li>Uses the transaction factory to resolve the appropriate handler</li>
     *     <li>Delegates transaction execution (with validation) to the handler</li>
     *     <li>Logs success or failure for audit purposes</li>
     * </ol>
     * </p>
     * <p>
     * The method supports all transaction types configured in the system, including:
     * <ul>
     *     <li>Cash purchases</li>
     *     <li>Installment purchases</li>
     *     <li>Withdrawals</li>
     *     <li>Payments</li>
     * </ul>
     * </p>
     *
     * @param account the account number to process the transaction for, must not be null
     *                and must reference an existing, active account
     * @param operationTypeId the unique identifier of the operation type, must match
     *                        a valid operation type configured in the system
     * @param amount the transaction amount, must be positive (greater than zero);
     *               the appropriate sign will be applied based on the transaction type
     * @return a {@link Mono} emitting the completed and persisted {@link Transaction}
     *         with generated ID and timestamps, or an error signal if:
     *         <ul>
     *             <li>The operation type ID is invalid</li>
     *             <li>The handler cannot be found</li>
     *             <li>Validation fails (null account, invalid amount, etc.)</li>
     *             <li>Transaction execution fails</li>
     *         </ul>
     * @throws InvalidOperationTypeException if the operation type ID does not match
     *                                       any configured operation type
     * @see TransactionFactory#getHandlerReactive(String)
     * @see TransactionHandler#performTransaction(Long, BigDecimal)
     */
    @Override
    public Mono<Transaction> performTransaction(Long account, Long operationTypeId, BigDecimal amount) {
        return operationTypeDatabaseAdapter.getOperationTypes()
                .flatMap(map -> {
                    OperationType operationType = getOperationTypesMap(map).get(operationTypeId);
                    return ObjectUtils.isEmpty(operationType)
                            ? Mono.error(new InvalidOperationTypeException("Invalid Operation Type ID: " + operationTypeId))
                            : Mono.just(operationType.getHandler());
                })
                .flatMap(transactionFactory::getHandlerReactive)
                .flatMap(handler -> handler.performTransaction(account, amount))
                .doOnSuccess(result ->
                        log.info("Transaction completed successfully - Account: {}, Result: {}",
                                account, result))
                .doOnError(error ->
                        log.error("Transaction failed - Account: {}",
                                account, error));
    }

    /**
     * Transforms the operation types map to be indexed by operation type ID.
     * <p>
     * This utility method converts a map indexed by description (operation name)
     * to a map indexed by operation type ID. This transformation enables efficient
     * lookup by ID when processing transactions, as the API accepts operation type
     * IDs from clients.
     * </p>
     * <p>
     * Example transformation:
     * <pre>
     * Input:  {"CASH_PURCHASE_TRANSACTION" -> OperationType{id=1, ...}, ...}
     * Output: {1 -> OperationType{id=1, ...}, ...}
     * </pre>
     * </p>
     *
     * @param operationTypeMap the map of operation types indexed by description,
     *                        typically retrieved from the database adapter
     * @return a new map with the same operation types but indexed by their ID
     */
    private Map<Long, OperationType> getOperationTypesMap(Map<String, OperationType> operationTypeMap) {
        return operationTypeMap.values().stream()
                .collect(Collectors.toMap(OperationType::getOperationTypeId,
                        operationType -> operationType));
    }
}

