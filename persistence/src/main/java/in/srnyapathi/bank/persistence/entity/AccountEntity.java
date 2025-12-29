package in.srnyapathi.bank.persistence.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity representing a bank account in the banking system.
 * <p>
 * This record maps to the {@code accounts} database table and stores
 * information about customer accounts including their identification
 * and status.
 * </p>
 *
 * @param id             the unique identifier for the account, mapped to {@code account_id}
 * @param documentNumber the customer's document number (e.g., SSN, national ID)
 * @param updatedAt      the timestamp of the last update to this account
 * @param createdAt      the timestamp when this account was created
 * @param isActive       indicates whether this account is currently active
 */
@Table(name = "accounts")
public record AccountEntity(
        @Id
        @Column("account_id")
        Long id,
        @Column("document_number")
        String documentNumber,

        @Column("updated_at")
        LocalDateTime updatedAt,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("is_active")
        boolean isActive
) {
}
