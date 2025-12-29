package in.srnyapathi.bank.domain.model;

/**
 * Enumeration of all transaction types supported by the banking system.
 * <p>
 * This enum defines the complete set of transaction operations that can be
 * performed on customer accounts. Each enum constant represents a specific
 * type of banking transaction with its own processing rules and handler.
 * </p>
 * <p>
 * These transaction types map to {@link OperationType} entities stored in
 * the database and are used throughout the application for transaction
 * processing and validation.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 * @see OperationType
 * @see TransactionType
 */
public enum SupportedTransactionEnum {

    /**
     * Represents a cash purchase transaction.
     * <p>
     * Used for direct purchases where the full amount is immediately debited
     * from the account. This is a {@link TransactionType#DEBIT} operation.
     * </p>
     */
    CASH_PURCHASE_TRANSACTION,

    /**
     * Represents an installment purchase transaction.
     * <p>
     * Used for purchases that will be paid in multiple installments over time.
     * The initial transaction records the purchase, and subsequent installments
     * are processed as separate debit operations.
     * </p>
     */
    INSTALLMENT_PURCHASE,

    /**
     * Represents a withdrawal transaction.
     * <p>
     * Used for cash withdrawals from the account, such as ATM withdrawals or
     * over-the-counter cash withdrawals. This is a {@link TransactionType#DEBIT}
     * operation that decreases the account balance.
     * </p>
     */
    WITHDRAWAL,

    /**
     * Represents a payment transaction.
     * <p>
     * Used for payments made to the account, such as deposits, salary credits,
     * or bill payments received. This is a {@link TransactionType#CREDIT}
     * operation that increases the account balance.
     * </p>
     */
    PAYMENT
}
