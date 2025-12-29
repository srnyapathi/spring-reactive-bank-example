package in.srnyapathi.bank.persistence.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a transaction in the banking system.
 * <p>
 * This record maps to the {@code transactions} database table and stores
 * all financial transactions performed on accounts.
 * </p>
 *
 * <p>Foreign Key Relationships:
 * <ul>
 *   <li>account_id -> accounts(account_id) ON DELETE RESTRICT</li>
 *   <li>operation_type_id -> operation_types(operation_id) ON DELETE RESTRICT</li>
 * </ul>
 * </p>
 *
 * @param transactionId   the unique identifier for the transaction, mapped to {@code transaction_id}
 * @param accountId       the ID of the account associated with this transaction
 * @param operationTypeId the ID of the operation type for this transaction
 * @param amount          the monetary amount of the transaction
 * @param eventDate       the date and time when the transaction event occurred
 * @param updatedAt       the timestamp of the last update to this transaction
 * @param createdAt       the timestamp when this transaction was created
 * @param isActive        indicates whether this transaction is currently active
 */
@Table("transactions")
public record TransactionEntity(
        @Id
        @Column("transaction_id")
        Long transactionId,

        @Column("account_id")
        Long accountId,

        @Column("operation_type_id")
        Long operationTypeId,

        @Column("amount")
        BigDecimal amount,

        @Column("event_date")
        LocalDateTime eventDate,

        @Column("updated_at")
        LocalDateTime updatedAt,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("is_active")
        boolean isActive
) {
}
