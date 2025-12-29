package in.srnyapathi.bank.domain.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Domain model representing a bank account in the system.
 * <p>
 * This class encapsulates all essential information about a customer's bank account,
 * including identification, audit trails, and status information. It follows the
 * domain-driven design principles and serves as the core entity in the account
 * management domain.
 * </p>
 * <p>
 * The account supports soft deletion through the {@code active} flag, allowing
 * historical data retention while marking accounts as inactive.
 * </p>
 *
 * @author srnyapathi
 * @since 1.0
 * @see in.srnyapathi.bank.domain.service.AccountService
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Account {

    /**
     * The unique document number of the account holder.
     * <p>
     * This typically represents a government-issued identification number such as:
     * <ul>
     *     <li>Social Security Number (SSN)</li>
     *     <li>Passport number</li>
     *     <li>National ID number</li>
     *     <li>Tax identification number</li>
     * </ul>
     * This field must be unique across all accounts and serves as a business identifier
     * for the account holder.
     * </p>
     */
    private String documentNumber;

    /**
     * The unique account number assigned to this account.
     * <p>
     * This is the primary identifier for the account and is typically generated
     * automatically by the system during account creation. It is used for all
     * transaction processing and account lookups.
     * </p>
     */
    private Long accountNumber;

    /**
     * The timestamp when the account was last updated.
     * <p>
     * This field is automatically populated during update operations and is used
     * for auditing purposes to track when account information was last modified.
     * </p>
     */
    private LocalDateTime updatedAt;

    /**
     * The timestamp when the account was created.
     * <p>
     * This field is automatically populated during account creation and remains
     * immutable throughout the account's lifetime. Used for auditing and reporting.
     * </p>
     */
    private LocalDateTime createdAt;

    /**
     * Indicates whether the account is active or has been soft-deleted.
     * <p>
     * When set to {@code false}, the account is considered inactive and should not
     * be used for new transactions. This supports soft deletion, allowing historical
     * data to be preserved while marking the account as unusable.
     * </p>
     * <p>
     * Default value: {@code true} (active)
     * </p>
     */
    private boolean active;
}
