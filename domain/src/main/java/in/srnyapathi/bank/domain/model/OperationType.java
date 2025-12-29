package in.srnyapathi.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a type of banking operation or transaction.
 * <p>
 * This class defines the characteristics of different transaction operations
 * supported by the banking system, such as cash purchases, withdrawals, payments,
 * and installment purchases. Each operation type has a unique handler that
 * processes transactions of that type according to specific business rules.
 * </p>
 * <p>
 * Operation types are configured in the database and loaded at runtime to support
 * flexible addition of new transaction types without code changes.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 * @see Transaction
 * @see TransactionType
 * @see SupportedTransactionEnum
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationType {

    /**
     * The unique identifier for this operation type.
     * <p>
     * This ID is used to reference the operation type in transactions and
     * serves as the primary key in the persistence layer.
     * </p>
     */
    private Long operationTypeId;

    /**
     * A human-readable description of the operation type.
     * <p>
     * Examples include:
     * <ul>
     *     <li>"CASH_PURCHASE" - Direct purchase using cash/debit</li>
     *     <li>"INSTALLMENT_PURCHASE" - Purchase paid in installments</li>
     *     <li>"WITHDRAWAL" - Cash withdrawal from account</li>
     *     <li>"PAYMENT" - Payment towards account balance</li>
     * </ul>
     * This description is also used as a key to lookup operation types in maps.
     * </p>
     */
    private String description;

    /**
     * The name of the handler class responsible for processing this operation type.
     * <p>
     * This field identifies which {@code TransactionHandler} implementation should
     * be used to process transactions of this type. The handler contains the specific
     * business logic and validation rules for the operation.
     * </p>
     * <p>
     * Example values:
     * <ul>
     *     <li>"CashPurchaseTransactionHandler"</li>
     *     <li>"WithdrawalTransactionHandler"</li>
     *     <li>"PaymentTransactionHandler"</li>
     * </ul>
     * </p>
     */
    private String handler;

    /**
     * The transaction type indicating whether this operation is a debit or credit.
     * <p>
     * This determines how the transaction amount affects the account balance:
     * <ul>
     *     <li>{@link TransactionType#DEBIT} - Decreases the account balance (withdrawals, purchases)</li>
     *     <li>{@link TransactionType#CREDIT} - Increases the account balance (deposits, payments)</li>
     * </ul>
     * </p>
     */
    private TransactionType transactionType;

}
