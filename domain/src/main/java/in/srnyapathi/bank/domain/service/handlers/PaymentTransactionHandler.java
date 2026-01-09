package in.srnyapathi.bank.domain.service.handlers;

import in.srnyapathi.bank.domain.adapters.TransactionDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.domain.service.OperationTypeService;
import in.srnyapathi.bank.domain.service.impl.TransactionHandler;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler for payment transactions in the banking system.
 * <p>
 * This handler processes credit transactions for payments and deposits that
 * increase the account balance. Payment transactions represent money being
 * added to the account, such as salary deposits, bill payment receipts,
 * or manual deposits.
 * </p>
 * <p>
 * Payments are {@link in.srnyapathi.bank.domain.model.TransactionType#CREDIT}
 * operations that increase the account balance. The handler validates that
 * amounts are positive (not negative) to ensure only valid credits are processed.
 * </p>
 * <p>
 * Key operations performed by this handler:
 * <ul>
 *     <li>Validates account number and ensures payment amount is positive</li>
 *     <li>Retrieves operation type configuration for payments</li>
 *     <li>Creates transaction with positive amount (credit operation)</li>
 *     <li>Records transaction with current timestamp for audit purposes</li>
 *     <li>Persists transaction to database</li>
 * </ul>
 * </p>
 *
 * @author srnyapathi
 * @version 1.0.0
 * @see TransactionHandler
 * @see in.srnyapathi.bank.domain.model.TransactionType#CREDIT
 * @since 1.0.0
 */
@Slf4j
@Component
public class PaymentTransactionHandler extends TransactionHandler {

    /**
     * Adapter for persisting transaction data to the database.
     */
    private final TransactionDatabaseAdapter transactionDatabaseAdapter;


    /**
     * Constructs a new PaymentTransactionHandler with required dependencies.
     * <p>
     * This constructor is invoked by Spring's dependency injection framework
     * to wire up the handler with its collaborators for transaction processing.
     * </p>
     *
     * @param transactionDatabaseAdapter the adapter for transaction persistence operations
     * @param operationTypeService       the service for retrieving operation type configurations
     */
    public PaymentTransactionHandler(TransactionDatabaseAdapter transactionDatabaseAdapter, OperationTypeService operationTypeService) {
        super(operationTypeService);
        this.transactionDatabaseAdapter = transactionDatabaseAdapter;
    }


    /**
     * Returns the unique handler identifier for payment transactions.
     * <p>
     * This identifier is used by the {@link in.srnyapathi.bank.domain.service.factory.TransactionFactory}
     * to route transaction requests to this handler. It must match the corresponding
     * {@link in.srnyapathi.bank.domain.model.SupportedTransactionEnum#PAYMENT} value.
     * </p>
     *
     * @return the string "PAYMENT" identifying this handler type
     */
    @Override
    protected String getHandler() {
        return "PAYMENT";
    }


    /**
     * Executes a payment transaction for the specified account and amount.
     * <p>
     * This method orchestrates the complete payment processing workflow:
     * <ol>
     *     <li>Retrieves the operation type configuration for payments</li>
     *     <li>Constructs a transaction object with account, amount, and operation type</li>
     *     <li>Keeps amount positive for credit operation</li>
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
     * @param account the account number to credit with the payment, must be valid and active
     * @param amount  the payment amount to credit (must be positive)
     * @return a {@link Mono} emitting the persisted {@link Transaction} with generated
     * ID and timestamps, or an error signal if the operation fails
     */
    @Override
    public Mono<Transaction> execute(Long account, BigDecimal amount) {
        return getOperationType()
                .flatMap(operationType ->

                        transactionDatabaseAdapter.getAllTransactionByAccount(account)
                                .collectList()
                                .flatMap(txnList ->
                                        getUpdatedTransactions(amount, operationType, txnList)

                                ).flatMap(txnAmount -> {
                                    log.info("Processing Payment for account: {} with amount: {}", account, amount);
                                    var tran = Transaction.builder()
                                            .account(new AccountNumber(account))
                                            .amount(amount)  // Keep original payment amount positive for CREDIT
                                            .balance(txnAmount)  // Remaining balance after paying debts
                                            .operationType(operationType)
                                            .eventDate(LocalDateTime.now())
                                            .active(true)
                                            .build();
                                    log.info("Saving Transaction: {}", tran);
                                    return transactionDatabaseAdapter.saveTransaction(tran);
                                }));

    }

    private @NonNull Mono<BigDecimal> getUpdatedTransactions(
            BigDecimal amt,
            OperationType operationType,
            List<Transaction> txnList
    ) {
        if (operationType.getTransactionType() != TransactionType.CREDIT) {
            return Mono.just(BigDecimal.ZERO);
        }

        BigDecimal remaining = amt == null ? BigDecimal.ZERO : amt;
        if (remaining.signum() <= 0) {
            return Mono.just(BigDecimal.ZERO);
        }

        List<Transaction> debts = txnList.stream()
                .filter(txn -> txn.getBalance() != null && txn.getBalance().signum() < 0) // debit outstanding
                .sorted(java.util.Comparator.comparing(Transaction::getTransactionId))     // FIFO
                .toList();

        List<Transaction> toUpdate = new ArrayList<>();

        for (Transaction txn : debts) {
            if (remaining.signum() <= 0) break;

            // debit balance is negative (e.g., -20 means 20 outstanding)
            var outstanding = txn.getBalance().abs();               // 20
            var paid = remaining.min(outstanding);                  // pay only what you can
            var newBalance = txn.getBalance().add(paid);            // -20 + 5 => -15, -15 + 15 => 0

            txn.setBalance(newBalance);                                    // ONLY balance changes
            toUpdate.add(txn);

            remaining = remaining.subtract(paid);                          // subtract ONLY what was paid
        }

        Mono<Transaction> updateMono = toUpdate.isEmpty()
                ? Mono.empty()
                : transactionDatabaseAdapter.updateTransaction(toUpdate);

        // carry-forward credit (>= 0)
        return updateMono.thenReturn(remaining.max(BigDecimal.ZERO));
    }


    /**
     * Validates the account and amount for a payment transaction.
     * <p>
     * This method extends the base validation from {@link TransactionHandler#validate(Long, BigDecimal)}
     * and adds specific validation to ensure the payment amount is positive (not negative).
     * Negative amounts are rejected because payments represent money being added to
     * the account, which should always be expressed as positive values.
     * </p>
     *
     * @param account the account number to validate, must not be null
     * @param amount  the payment amount to validate, must be positive (greater than zero)
     * @return a {@link Mono} that completes successfully if validation passes,
     * or emits an {@link InvalidAmountException} if the amount is negative
     */
    @Override
    protected Mono<Void> validate(Long account, BigDecimal amount) {
        return super.validate(account, amount).then(Mono.defer(() -> {
            if (isNegativeAmount(amount)) {
                return Mono.error(new InvalidAmountException("Payment amount must be positive"));
            }
            return Mono.empty();
        }));
    }
}
