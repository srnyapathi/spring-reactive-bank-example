package in.srnyapathi.bank.domain.service.impl;

import in.srnyapathi.bank.domain.adapters.AccountDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.DuplicateAccountException;
import in.srnyapathi.bank.domain.model.Account;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

/**
 * Default reactive implementation of {@link AccountService}.
 * Delegates persistence to {@link AccountDatabaseAdapter} and maps database errors to domain errors.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    /**
     * Adapter for account database operations.
     * <p>
     * This adapter handles the actual persistence operations while keeping the
     * service layer decoupled from database implementation details.
     * </p>
     */
    private final AccountDatabaseAdapter accountDatabaseAdapter;

    /**
     * Creates a new bank account.
     *
     * Workflow:
     * <ol>
     *     <li>Wrap the incoming {@link Account} in a {@link Mono}</li>
     *     <li>Persist via {@link AccountDatabaseAdapter#createAccount(Account)}</li>
     *     <li>Log success with the generated account number</li>
     *     <li>Map {@link DuplicateKeyException} to {@link DuplicateAccountException}</li>
     * </ol>
     *
     * @param account the account to create; must contain a unique document number
     * @return a {@link Mono} emitting the persisted {@link Account}
     * @throws DuplicateAccountException if an account already exists for the given document number
     */
    @Override
    public Mono<Account> createAccount(Account account) {
        return Mono.just(account)
                .flatMap(accountDatabaseAdapter::createAccount)
                .doOnSuccess(savedAccount -> log.info("Account created successfully: {}", savedAccount.getAccountNumber()))
                .onErrorMap(DuplicateKeyException.class, ex -> {
                    log.error("Duplicate document number: {}", account.getDocumentNumber());
                    return new DuplicateAccountException("Account with document number already exists");
                })
                .doOnError(error -> log.error("Error creating account", error));
    }

    /**
     * Retrieves an account by account number.
     *
     * @param accountId a {@link Mono} emitting the account number to fetch
     * @return a {@link Mono} emitting the {@link Account} if found; empty or error otherwise
     */
    @Override
    public Mono<Account> getAccount(Mono<Long> accountId) {
        return accountId
                .flatMap(accountDatabaseAdapter::getAccountByAccountNumber)
                .doOnSuccess(account -> log.info("Account retrieved successfully: {}", account))
                .doOnError(error -> log.error("Error retrieving account", error));
    }
}
