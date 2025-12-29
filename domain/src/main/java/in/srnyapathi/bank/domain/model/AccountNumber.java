package in.srnyapathi.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain model representing an account number identifier.
 * <p>
 * This class serves as a value object that encapsulates the account number,
 * providing a type-safe way to reference accounts throughout the domain.
 * It is primarily used in transaction processing to identify which account
 * a transaction belongs to.
 * </p>
 * <p>
 * This is a simple wrapper around the account number ID, providing semantic
 * meaning and preventing primitive obsession in the domain model.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 * @see Transaction
 * @see Account
 */
@Slf4j
@AllArgsConstructor
@Data
@ToString
@Builder
public class AccountNumber {

    /**
     * The unique identifier representing the account number.
     * <p>
     * This ID corresponds to the account number in the {@link Account} entity
     * and is used to link transactions to specific accounts.
     * </p>
     */
    private Long id;

    /**
     * Retrieves the account number.
     * <p>
     * This method provides convenient access to the underlying account number ID,
     * maintaining backward compatibility and clarity in the API.
     * </p>
     *
     * @return the account number as a Long value
     */
    public Long getAccountNumber() {
        return id;
    }
}
