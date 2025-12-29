package in.srnyapathi.bank.persistence.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity representing an operation type in the banking system.
 * <p>
 * This record maps to the {@code operation_types} database table and defines
 * the various types of banking operations that can be performed, such as
 * deposits, withdrawals, and payments.
 * </p>
 *
 * @param id          the unique identifier for the operation type, mapped to {@code operation_id}
 * @param description a human-readable description of the operation type
 * @param type        the transaction type (CREDIT or DEBIT) for this operation
 * @param handler     the handler class or method name responsible for processing this operation type
 * @param updatedAt   the timestamp of the last update to this operation type
 * @param createdAt   the timestamp when this operation type was created
 * @param isActive    indicates whether this operation type is currently active and available for use
 */

@Table(name = "operation_types")
public record OperationTypeEntity(
        @Id
        @Column("operation_id")
        Long id,

        @Column("description")
        String description,

        @Column("transaction_type")
        String type,

        @Column("handler")
        String handler,

        @Column("updated_at")
        LocalDateTime updatedAt,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("is_active")
        Boolean isActive
) {

}
