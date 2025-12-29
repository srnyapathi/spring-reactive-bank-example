package in.srnyapathi.bank.domain.service.impl;

import in.srnyapathi.bank.domain.exception.InvalidAccountException;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.exception.InvalidOperationTypeException;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.domain.service.OperationTypeService;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Abstract base class for all transaction handlers in the banking system.
 * <p>
 * This class provides the foundation for the Template Method pattern implementation,
 * defining the common transaction processing workflow while allowing concrete handlers
 * to customize specific aspects of transaction execution. It encapsulates shared
 * validation logic, operation type retrieval, and amount calculation rules.
 * </p>
 * <p>
 * The handler supports both debit and credit transactions by applying appropriate
 * sign conversions to transaction amounts based on the {@link TransactionType}.
 * All operations are reactive, leveraging Project Reactor for non-blocking,
 * asynchronous processing.
 * </p>
 * <p>
 * Concrete subclasses must implement:
 * <ul>
 *     <li>{@link #getHandler()} - to provide the handler identifier</li>
 *     <li>{@link #execute(Long, BigDecimal)} - to define transaction-specific processing logic</li>
 * </ul>
 * Subclasses may optionally override {@link #validate(Long, BigDecimal)} to add
 * additional validation rules specific to their transaction type.
 * </p>
 *
 * @author srnyapathi
 * @version 1.0.0
 * @since 1.0.0
 * @see TransactionType
 * @see OperationType
 * @see Transaction
 */
public abstract class TransactionHandler {

    /**
     * Service for retrieving operation type configurations.
     * <p>
     * This service provides access to operation type metadata including handler
     * names, transaction types (DEBIT/CREDIT), and operation type IDs.
     * </p>
     */
    protected final OperationTypeService operationTypeService;

    /**
     * Checks if the given amount is negative (less than zero).
     * <p>
     * This utility method is used by handlers to validate that input amounts
     * are positive before processing. Most transaction types require positive
     * amounts as input, with the sign being applied based on the transaction type.
     * </p>
     *
     * @param amount the amount to check, must not be null
     * @return {@code true} if the amount is less than zero, {@code false} otherwise
     */
    protected boolean isNegativeAmount(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Applies the appropriate sign to the transaction amount based on transaction type.
     * <p>
     * This method implements the business rule for amount signing:
     * <ul>
     *     <li>{@link TransactionType#DEBIT} - negates the amount (positive becomes negative)</li>
     *     <li>{@link TransactionType#CREDIT} - keeps the amount positive</li>
     * </ul>
     * This ensures consistent handling of debits (withdrawals, purchases) and
     * credits (deposits, payments) across all transaction types.
     * </p>
     *
     * @param amount the positive amount to process, must not be null
     * @param transactionType the type of transaction (DEBIT or CREDIT), must not be null
     * @return the amount with the appropriate sign applied (negative for debits, positive for credits)
     */
    protected BigDecimal getAmount(BigDecimal amount, TransactionType transactionType) {
        return transactionType == TransactionType.DEBIT ? amount.negate() : amount;
    }

    /**
     * Constructs a new TransactionHandler with the required operation type service.
     * <p>
     * This constructor is called by all concrete handler implementations to
     * initialize the base handler with access to operation type configurations.
     * </p>
     *
     * @param operationTypeService the service for retrieving operation types, must not be null
     */
    protected TransactionHandler(OperationTypeService operationTypeService) {
        this.operationTypeService = operationTypeService;
    }

    /**
     * Retrieves the operation type configuration for this handler.
     * <p>
     * This method performs the following:
     * <ol>
     *     <li>Fetches all operation types from the service</li>
     *     <li>Filters for the specific operation type matching this handler's identifier</li>
     *     <li>Returns the operation type if found</li>
     *     <li>Returns an error if the operation type is not found or the map is empty</li>
     * </ol>
     * </p>
     * <p>
     * The operation type contains crucial metadata including the transaction type
     * (DEBIT/CREDIT) which determines how amounts are signed.
     * </p>
     *
     * @return a {@link Mono} emitting the {@link OperationType} for this handler,
     *         or an error signal with {@link InvalidOperationTypeException} if not found
     */
    protected Mono<OperationType> getOperationType() {
        return operationTypeService.getOperationType()
                .filter(map ->
                        !ObjectUtils.isEmpty(map) && map.containsKey(getHandler()))
                .flatMap(map -> Mono.just(map.get(getHandler())))
                .switchIfEmpty(
                        Mono.defer(() ->
                                Mono.error(new InvalidOperationTypeException(
                                        "Unable to find the transaction handler for operation type: " + getHandler())
                                )
                        )
                );
    }

    /**
     * Returns the unique identifier for this handler.
     * <p>
     * This identifier is used to:
     * <ul>
     *     <li>Look up the operation type configuration</li>
     *     <li>Route transaction requests to the correct handler via the factory</li>
     *     <li>Match against {@link in.srnyapathi.bank.domain.model.SupportedTransactionEnum} values</li>
     * </ul>
     * </p>
     *
     * @return the handler identifier (e.g., "CASH_PURCHASE_TRANSACTION", "WITHDRAWAL", "PAYMENT")
     */
    protected abstract String getHandler();

    /**
     * Executes the transaction with handler-specific business logic.
     * <p>
     * This method must be implemented by concrete handlers to define their
     * specific transaction processing workflow. Implementations typically:
     * <ol>
     *     <li>Retrieve the operation type configuration</li>
     *     <li>Build a transaction object with the account, amount, and operation type</li>
     *     <li>Apply the appropriate amount sign based on transaction type</li>
     *     <li>Persist the transaction to the database</li>
     * </ol>
     * </p>
     *
     * @param account the account number to process the transaction for, must be valid and active
     * @param amount the transaction amount (typically positive; sign applied based on operation type)
     * @return a {@link Mono} emitting the persisted {@link Transaction} with generated ID and timestamps,
     *         or an error signal if execution fails
     */
    public abstract Mono<Transaction> execute(Long account, BigDecimal amount);

    /**
     * Validates the transaction parameters before execution.
     * <p>
     * This method performs base validation that applies to all transaction types:
     * <ul>
     *     <li>Account number must not be null</li>
     *     <li>Amount must not be null</li>
     *     <li>Amount must be greater than zero</li>
     * </ul>
     * </p>
     * <p>
     * Concrete handlers may override this method to add additional validation
     * rules specific to their transaction type. Overriding methods should typically
     * call {@code super.validate(account, amount)} first to preserve base validation.
     * </p>
     *
     * @param account the account number to validate, must not be null
     * @param amount the transaction amount to validate, must be positive and not null
     * @return a {@link Mono} that completes successfully if validation passes,
     *         or emits an error signal ({@link InvalidAccountException} or
     *         {@link InvalidAmountException}) if validation fails
     */
    protected Mono<Void> validate(Long account, BigDecimal amount) {
        return Mono.defer(() -> {
            if (Objects.isNull(account)) {
                return Mono.error(new InvalidAccountException("Account identifier cannot be null or empty"));
            }
            if (Objects.isNull(amount)) {
                return Mono.error(new InvalidAmountException("Amount cannot be null"));
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Mono.error(new InvalidAmountException("Amount must be greater than zero"));
            }
            return Mono.empty();
        });
    }

    /**
     * Performs the complete transaction workflow with validation.
     * <p>
     * This is the main entry point for transaction processing, implementing the
     * Template Method pattern. It orchestrates the transaction flow:
     * <ol>
     *     <li>Validates the account and amount</li>
     *     <li>If validation passes, executes the transaction via {@link #execute(Long, BigDecimal)}</li>
     *     <li>Returns the result or propagates any errors</li>
     * </ol>
     * </p>
     * <p>
     * This method ensures that all transactions go through proper validation
     * before execution, maintaining data integrity and business rule compliance.
     * </p>
     *
     * @param account the account number to process the transaction for, must not be null
     * @param amount the transaction amount, must be positive and greater than zero
     * @return a {@link Mono} emitting the completed {@link Transaction} if successful,
     *         or an error signal if validation or execution fails
     */
    public Mono<Transaction> performTransaction(Long account, BigDecimal amount) {
        return validate(account, amount)
                .then(execute(account, amount));
    }
}

