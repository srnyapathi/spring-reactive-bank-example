package in.srnyapathi.bank.persistence.service;

import in.srnyapathi.bank.domain.adapters.AccountDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.AccountDoesNotExist;
import in.srnyapathi.bank.domain.exception.InvalidAccountException;
import in.srnyapathi.bank.domain.model.Account;

import in.srnyapathi.bank.persistence.mapper.AccountMapper;
import in.srnyapathi.bank.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Service implementation for persisting and retrieving account data from the database.
 * <p>
 * This service acts as an adapter between the domain layer and the persistence layer,
 * implementing the {@link AccountDatabaseAdapter} interface. It handles the conversion
 * between domain objects and database entities using {@link AccountMapper}.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AccountDatabaseAdapterService implements AccountDatabaseAdapter {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    /**
     * Creates a new account in the database.
     * <p>
     * This method validates the account object, converts it to an entity,
     * persists it to the database, and returns the saved account as a domain object.
     * </p>
     *
     * @param account the account domain object to create, must not be null
     * @return a {@link Mono} emitting the created {@link Account} with its generated ID
     * @throws InvalidAccountException if the account is null
     */
    @Override
    public Mono<Account> createAccount(Account account) {
        if (Objects.isNull(account)) {
            return Mono.error(new InvalidAccountException("Account cannot be null"));
        }

        return Mono.just(account)
                .map(accountMapper::toEntity)
                .flatMap(accountRepository::save)
                .map(accountMapper::toDomain);
    }

    /**
     * Retrieves an active account by its account number.
     * <p>
     * This method fetches only active accounts from the database. If the account
     * is not found or is inactive, it returns an error.
     * </p>
     *
     * @param accountId the unique identifier of the account to retrieve
     * @return a {@link Mono} emitting the {@link Account} if found and active
     * @throws AccountDoesNotExist if the account is not found or is inactive
     */
    @Override
    public Mono<Account> getAccountByAccountNumber(Long accountId) {
        return accountRepository.findActiveAccount(accountId)
                .map(accountMapper::toDomain)
                .switchIfEmpty(Mono.error(() -> {
                    log.error("Account not found or inactive: {}", accountId);
                    return new AccountDoesNotExist("Account not found or inactive: " + accountId);
                }));
    }
}
