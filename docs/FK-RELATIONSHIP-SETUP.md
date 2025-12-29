# Foreign Key Relationship Setup - Transaction to Account

## Overview
The foreign key relationship between the `transactions` table and the `accounts` table has been properly established at both the database and application levels.

## Database Level (SQL Schema)

### Location
`persistence/src/main/resources/DDL/01_Table_Create_script.sql`

### Foreign Key Constraint
```sql
CREATE TABLE transactions (
    transaction_id    BIGSERIAL PRIMARY KEY,
    account_id        BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE RESTRICT,
    operation_type_id BIGINT NOT NULL REFERENCES operation_types(operation_id) ON DELETE RESTRICT,
    amount            NUMERIC(12,2) NOT NULL,
    event_date        TIMESTAMP(6) NOT NULL,
    created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE
);
```

### Key Points:
- **FK Column**: `account_id` references `accounts(account_id)`
- **Delete Rule**: `ON DELETE RESTRICT` - prevents deletion of accounts that have associated transactions
- **Not Null**: The FK column is NOT NULL, ensuring referential integrity
- **Additional FK**: `operation_type_id` also references `operation_types(operation_id)`

### Indexes
The following indexes support the FK relationships:
```sql
CREATE INDEX idx_tx_account_date ON transactions (account_id, event_date);
CREATE INDEX idx_tx_account_operation ON transactions (account_id, operation_type_id);
```

## Application Level (Spring Data R2DBC)

### Entity Mapping
`persistence/src/main/java/in/srnyapathi/bank/persistence/entity/TransactionEntity.java`

```java
@Table("transactions")
public record TransactionEntity(
        @Id
        @Column("transaction_id")
        Long transactionId,

        @Column("account_id")
        Long accountId,

        @Column("operation_type_id")
        Long operationTypeId,
        
        // ... other fields
) {
}
```

### Important Notes:

1. **Spring Data R2DBC Limitation**: Unlike JPA, R2DBC does not support automatic relationship loading or navigation properties. The FK is enforced at the database level only.

2. **Referential Integrity**: The database ensures that:
   - Every transaction must reference a valid account
   - Accounts with transactions cannot be deleted (RESTRICT constraint)
   - Invalid account_id values will be rejected by the database

3. **Manual Joins**: If you need to join transactions with accounts, you must:
   - Use custom queries in the repository with JOIN clauses
   - Or fetch the account separately using the accountId

## Database Initialization

The SQL scripts are automatically executed when using Docker Compose:

```yaml
# docker-compose.yml
postgres:
  volumes:
    - ./persistence/src/main/resources/DDL:/docker-entrypoint-initdb.d
```

PostgreSQL automatically runs all SQL files in `/docker-entrypoint-initdb.d` on first initialization.

## Verification

### To verify the FK constraint exists:
```sql
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'transactions';
```

### Test the FK constraint:
```sql
-- This should fail if account doesn't exist
INSERT INTO transactions (account_id, operation_type_id, amount, event_date)
VALUES (99999, 1, 100.00, NOW());

-- This should fail due to RESTRICT constraint
DELETE FROM accounts WHERE account_id = 1;  -- If transactions exist for this account
```

## Repository Usage Example

```java
@Repository
public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, Long> {
    
    // Find all transactions for a specific account
    @Query("SELECT * FROM transactions WHERE account_id = :accountId AND is_active = true")
    Flux<TransactionEntity> findByAccountId(Long accountId);
    
    // Join with account if needed
    @Query("""
        SELECT t.* 
        FROM transactions t
        INNER JOIN accounts a ON t.account_id = a.account_id
        WHERE a.document_number = :documentNumber 
        AND t.is_active = true
        """)
    Flux<TransactionEntity> findByAccountDocumentNumber(String documentNumber);
}
```

## Related Files
- SQL Schema: `persistence/src/main/resources/DDL/01_Table_Create_script.sql`
- Transaction Entity: `persistence/src/main/java/in/srnyapathi/bank/persistence/entity/TransactionEntity.java`
- Account Entity: `persistence/src/main/java/in/srnyapathi/bank/persistence/entity/AccountEntity.java`
- Transaction Repository: `persistence/src/main/java/in/srnyapathi/bank/persistence/repository/TransactionRepository.java`
- Docker Compose: `docker-compose.yml`

## Build Status
✅ Project builds successfully with the FK relationship in place.

Last Updated: December 29, 2025

