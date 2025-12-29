package in.srnyapathi.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration representing the type of transaction based on its effect on account balance.
 * Each transaction type has an associated symbol for display and identification purposes.
 *
 * @author srnyapathi
 * @version 1.0.0
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum TransactionType {

    /**
     * Represents a credit transaction that increases the account balance.
     * Symbol: "+"
     */
    CREDIT("CR"),

    /**
     * Represents a debit transaction that decreases the account balance.
     * Symbol: "-"
     */
    DEBIT("DR");

    /**
     * The symbol associated with this transaction type.
     * Used for display and identification purposes.
     */
    private final String name;
}
