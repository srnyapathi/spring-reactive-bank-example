package in.srnyapathi.bank.domain.service;

import in.srnyapathi.bank.domain.model.Account;
import reactor.core.publisher.Mono;

/**
 * Reactive service for managing bank accounts.
 * All operations are non-blocking and use {@link Mono} for asynchronous processing.
 */
public interface AccountService {

    /**
     * Creates a new bank account.
     *
     * @param account the account aggregate to persist; must contain a unique document number
     * @return a {@link Mono} emitting the saved {@link Account} with generated account number
     */
    Mono<Account> createAccount(Account account);

    /**
     * Retrieves account details by account number.
     *
     * @param accountNumber a {@link Mono} emitting the account number to look up
     * @return a {@link Mono} emitting the {@link Account} if found; empty if not found
     */
    Mono<Account> getAccount(Mono<Long> accountNumber);
}
