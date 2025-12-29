package in.srnyapathi.bank.domain.service.handlers;

import in.srnyapathi.bank.domain.adapters.TransactionDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.service.OperationTypeService;
import in.srnyapathi.bank.domain.service.impl.TransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handler for withdrawal transactions in the banking system.
 * <p>
 * This handler processes debit transactions for cash withdrawals from customer
 * accounts. Withdrawals represent money being taken out of the account, such as
 * ATM withdrawals, over-the-counter cash withdrawals, or transfer withdrawals.
 * </p>
 * <p>
 * Withdrawals are {@link in.srnyapathi.bank.domain.model.TransactionType#DEBIT}
 * operations that decrease the account balance. The handler validates that amounts
 * are positive (not negative) before applying the debit sign during transaction creation.
 * </p>
 * <p>
 * Key operations performed by this handler:
 * <ul>
 *     <li>Validates account number and ensures withdrawal amount is positive</li>
 *     <li>Retrieves operation type configuration for withdrawals</li>
 *     <li>Creates transaction with proper debit amount (negative value)</li>
 *     <li>Records transaction with current timestamp for audit trail</li>
 *     <li>Persists transaction to database</li>
 * </ul>
 * </p>
 * <p>
 * Note: This handler does not perform balance checking. Balance validation
 * should be handled by higher-level services or business rules if required.
 * </p>
 *
 * @author srnyapathi
 * @version 1.0.0
 * @since 1.0.0
 * @see TransactionHandler
 * @see in.srnyapathi.bank.domain.model.TransactionType#DEBIT
 */
@Slf4j
@Component
public class WithdrawalTransactionHandler extends TransactionHandler {

    /**
     * Adapter for persisting transaction data to the database.
     */
    private final TransactionDatabaseAdapter transactionDatabaseAdapter;

    /**
     * Constructs a new WithdrawalTransactionHandler with required dependencies.
     * <p>
     * This constructor is invoked by Spring's dependency injection framework
     * to wire up the handler with necessary collaborators for transaction processing.
     * </p>
     *
     * @param transactionDatabaseAdapter the adapter for transaction persistence operations
     * @param operationTypeService the service for retrieving operation type configurations
     */
    public WithdrawalTransactionHandler(TransactionDatabaseAdapter transactionDatabaseAdapter, OperationTypeService operationTypeService) {
        super(operationTypeService);
        this.transactionDatabaseAdapter = transactionDatabaseAdapter;

    }

    /**
     * Returns the unique handler identifier for withdrawal transactions.
     * <p>
     * This identifier is used by the {@link in.srnyapathi.bank.domain.service.factory.TransactionFactory}
     * to route transaction requests to this handler. It must match the corresponding
     * {@link in.srnyapathi.bank.domain.model.SupportedTransactionEnum#WITHDRAWAL} value.
     * </p>
     *
     * @return the string "WITHDRAWAL" identifying this handler type
     */
    @Override
    protected String getHandler() {
        return "WITHDRAWAL";
    }


    /**
     * Validates the account and amount for a withdrawal transaction.
     * <p>
     * This method extends the base validation from {@link TransactionHandler#validate(Long, BigDecimal)}
     * and adds specific validation to ensure the withdrawal amount is positive (not negative).
     * Negative amounts are rejected because withdrawals represent money being removed
     * from the account, which should always be expressed as positive values before
     * applying the debit sign.
     * </p>
     *
     * @param account the account number to validate, must not be null
     * @param amount the withdrawal amount to validate, must be positive (greater than zero)
     * @return a {@link Mono} that completes successfully if validation passes,
     *         or emits an {@link InvalidAmountException} if the amount is negative
     */
    @Override
    protected Mono<Void> validate(Long account, BigDecimal amount) {
        return super.validate(account, amount)
                .then(Mono.defer(() -> {
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new InvalidAmountException(
                                "Withdrawal amount must be positive"
                        ));
                    }
                    return Mono.empty();
                }));
    }

    /**
     * Executes a withdrawal transaction for the specified account and amount.
     * <p>
     * This method orchestrates the complete withdrawal processing workflow:
     * <ol>
     *     <li>Retrieves the operation type configuration for withdrawals</li>
     *     <li>Constructs a transaction object with account, amount, and operation type</li>
     *     <li>Applies the debit sign to the amount (converts positive to negative)</li>
     *     <li>Sets the event date to the current timestamp</li>
     *     <li>Marks the transaction as active</li>
     *     <li>Persists the transaction to the database</li>
     * </ol>
     * </p>
     * <p>
     * This method operates reactively, returning a {@link Mono} that emits the
     * saved transaction upon successful completion or propagates an error signal
     * if any step in the processing chain fails.
     * </p>
     *
     * @param account the account number to debit for the withdrawal, must be valid and active
     * @param amount the withdrawal amount (must be positive; will be converted
     *               to negative for debit operation)
     * @return a {@link Mono} emitting the persisted {@link Transaction} with generated
     *         ID and timestamps, or an error signal if the operation fails
     */
    @Override
    public Mono<Transaction> execute(Long account, BigDecimal amount) {
        return getOperationType()
                .flatMap(operationType -> {
                    log.info("Processing InstallmentPurchase for account: {} with amount: {}", account, amount);
                    var tran = Transaction.builder()
                            .account(new AccountNumber(account))
                            .amount(getAmount(amount, operationType.getTransactionType()))
                            .operationType(operationType)
                            .eventDate(LocalDateTime.now())
                            .active(true)
                            .build();
                    log.info("Saving Transaction: {}", tran);
                    return transactionDatabaseAdapter.saveTransaction(tran);
                });
    }
}

