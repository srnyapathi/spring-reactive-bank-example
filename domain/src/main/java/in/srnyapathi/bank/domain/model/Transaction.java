package in.srnyapathi.bank.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a financial transaction in the banking system.
 * <p>
 * This class encapsulates all information about a single banking transaction,
 * including the account involved, operation type, amount, and timestamps.
 * Transactions are immutable once created and serve as the foundation for
 * account balance calculations and financial reporting.
 * </p>
 * <p>
 * Each transaction is associated with an {@link OperationType} that determines
 * how the transaction amount affects the account balance (debit or credit).
 * The transaction supports soft deletion through the {@code active} flag.
 * </p>
 * <p>
 * Transactions follow event sourcing principles where the {@code eventDate}
 * represents when the transaction actually occurred, which may differ from
 * when it was recorded in the system ({@code createdAt}).
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 * @see Account
 * @see OperationType
 * @see TransactionType
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Transaction {

    /**
     * The unique identifier for this transaction.
     * <p>
     * This ID is automatically generated during transaction creation and serves
     * as the primary key for transaction lookups and references.
     * </p>
     */
    private Long transactionId;

    /**
     * The account number associated with this transaction.
     * <p>
     * This identifies which account the transaction is applied to. Every
     * transaction must be associated with a valid, active account.
     * </p>
     */
    private AccountNumber account;

    /**
     * The type of operation performed in this transaction.
     * <p>
     * This determines:
     * <ul>
     *     <li>The business rules applied during transaction processing</li>
     *     <li>Whether the transaction is a debit or credit operation</li>
     *     <li>Which handler processes the transaction</li>
     * </ul>
     * Examples include cash purchases, withdrawals, payments, and installment purchases.
     * </p>
     */
    private OperationType operationType;

    /**
     * The monetary amount of the transaction.
     * <p>
     * The amount is stored as a {@link BigDecimal} to ensure precision in
     * financial calculations. The sign of the amount is determined by the
     * {@link TransactionType}:
     * <ul>
     *     <li>Positive values for {@link TransactionType#CREDIT} operations</li>
     *     <li>Negative values for {@link TransactionType#DEBIT} operations</li>
     * </ul>
     * The amount must always be greater than zero before sign adjustment.
     * </p>
     */
    private BigDecimal amount;

    /**
     * The date and time when the transaction event occurred.
     * <p>
     * This represents the business effective date of the transaction, which may
     * differ from the system recording time. Used for:
     * <ul>
     *     <li>Transaction ordering and sequencing</li>
     *     <li>Balance calculations at specific points in time</li>
     *     <li>Financial reporting and reconciliation</li>
     * </ul>
     * </p>
     */
    private LocalDateTime eventDate;

    /**
     * The timestamp when the transaction was created in the system.
     * <p>
     * This field is automatically populated during transaction creation and
     * represents when the transaction was recorded, which may differ from
     * the {@code eventDate}. Used for auditing and system tracking.
     * </p>
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the transaction was last updated.
     * <p>
     * This field is automatically updated whenever the transaction record is
     * modified. Used for auditing purposes to track transaction modifications.
     * </p>
     */
    private LocalDateTime updatedAt;

    /**
     * Indicates whether the transaction is active or has been soft-deleted.
     * <p>
     * When set to {@code false}, the transaction is considered inactive and
     * should be excluded from balance calculations. This supports soft deletion,
     * allowing historical data to be preserved while marking transactions as void.
     * </p>
     * <p>
     * Default value: {@code true} (active)
     * </p>
     */
    private boolean active;
}
