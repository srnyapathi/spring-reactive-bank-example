package in.srnyapathi.bank.domain.service.handlers;

import in.srnyapathi.bank.domain.adapters.TransactionDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.domain.service.impl.TransactionHandler;
import in.srnyapathi.bank.domain.service.OperationTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handler for cash purchase transactions in the banking system.
 * <p>
 * This handler processes immediate debit transactions for cash purchases where
 * the full purchase amount is immediately deducted from the customer's account.
 * It extends {@link TransactionHandler} to leverage common validation and
 * transaction processing logic.
 * </p>
 * <p>
 * Cash purchases are {@link TransactionType#DEBIT} operations that decrease
 * the account balance. The transaction is recorded with the current timestamp
 * and saved to the database for audit and balance calculation purposes.
 * </p>
 * <p>
 * This handler performs the following operations:
 * <ul>
 *     <li>Validates the account number and transaction amount</li>
 *     <li>Retrieves the operation type configuration</li>
 *     <li>Converts the amount to negative (debit) based on transaction type</li>
 *     <li>Creates and persists the transaction record</li>
 * </ul>
 * </p>
 *
 * @author srnyapathi
 * @version 1.0.0
 * @see TransactionHandler
 * @see TransactionType#DEBIT
 * @since 1.0.0
 */
@Slf4j
@Component
public class CashPurchaseTransactionHandler extends TransactionHandler {

    /**
     * Adapter for persisting transaction data to the database.
     */
    private final TransactionDatabaseAdapter transactionDatabaseAdapter;

    /**
     * Constructs a new CashPurchaseTransactionHandler with required dependencies.
     * <p>
     * This constructor is called by Spring's dependency injection to wire up
     * the handler with its required collaborators.
     * </p>
     *
     * @param transactionDatabaseAdapter the adapter for transaction persistence operations
     * @param operationTypeService       the service for retrieving operation type configurations
     */
    public CashPurchaseTransactionHandler(TransactionDatabaseAdapter transactionDatabaseAdapter,
                                          OperationTypeService operationTypeService) {
        super(operationTypeService);
        this.transactionDatabaseAdapter = transactionDatabaseAdapter;
    }

    /**
     * Returns the unique handler identifier for cash purchase transactions.
     * <p>
     * This identifier is used by the {@link in.srnyapathi.bank.domain.service.factory.TransactionFactory}
     * to route transaction requests to this handler. It must match the corresponding
     * {@link in.srnyapathi.bank.domain.model.SupportedTransactionEnum#CASH_PURCHASE_TRANSACTION} value.
     * </p>
     *
     * @return the string "CASH_PURCHASE_TRANSACTION" identifying this handler type
     */
    @Override
    public String getHandler() {
        return "CASH_PURCHASE_TRANSACTION";
    }

    /**
     * Validates the account and amount for a cash purchase transaction.
     * <p>
     * This method extends the base validation from {@link TransactionHandler#validate(Long, BigDecimal)}
     * to ensure the account and amount meet basic requirements. Currently, no additional
     * validation beyond the parent class is performed for cash purchases, but this method
     * provides an extension point for future business rule validation.
     * </p>
     *
     * @param account the account number to validate, must not be null
     * @param amount  the transaction amount to validate, must be positive and not null
     * @return a {@link Mono} that completes successfully if validation passes,
     * or emits an error signal if validation fails
     */
    @Override
    protected Mono<Void> validate(Long account, BigDecimal amount) {
        return super.validate(account, amount)
                .then(Mono.defer(Mono::empty));
    }


    /**
     * Executes a cash purchase transaction for the specified account and amount.
     * <p>
     * This method orchestrates the complete transaction processing flow:
     * <ol>
     *     <li>Retrieves the operation type configuration for cash purchases</li>
     *     <li>Creates a transaction object with the account, amount, and operation type</li>
     *     <li>Applies the appropriate sign to the amount (negative for debit)</li>
     *     <li>Sets the event date to the current timestamp</li>
     *     <li>Persists the transaction to the database</li>
     * </ol>
     * </p>
     * <p>
     * The method operates reactively, returning a {@link Mono} that emits the
     * saved transaction upon successful completion or an error signal if any
     * step fails.
     * </p>
     *
     * @param account the account number to debit, must be a valid, active account
     * @param amount  the purchase amount to debit (must be positive; will be converted
     *                to negative for debit operation)
     * @return a {@link Mono} emitting the persisted {@link Transaction} with generated
     * ID and timestamps, or an error signal if the operation fails
     */
    @Override
    public Mono<Transaction> execute(Long account, BigDecimal amount) {
        return getOperationType().flatMap(operationType -> {
            log.info("Processing Cash Purchase for account: {} with amount: {}", account, amount);
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
