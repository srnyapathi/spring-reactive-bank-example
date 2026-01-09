package in.srnyapathi.bank.persistence.service;

import in.srnyapathi.bank.domain.adapters.TransactionDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.AccountDoesNotExist;
import in.srnyapathi.bank.domain.exception.InvalidTransactionObjectException;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.persistence.mapper.TransactionMapper;
import in.srnyapathi.bank.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * Service implementation for persisting and retrieving transaction data from the database.
 * <p>
 * This service acts as an adapter between the domain layer and the persistence layer,
 * implementing the {@link TransactionDatabaseAdapter} interface. It handles the conversion
 * between domain objects and database entities using {@link TransactionMapper}, and provides
 * comprehensive logging for transaction operations.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class TransactionDatabaseAdapterService implements TransactionDatabaseAdapter {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Saves a transaction to the database.
     * <p>
     * This method validates the transaction object, converts it to an entity,
     * persists it to the database, and returns the saved transaction as a domain object.
     * All operations are logged for auditing and debugging purposes.
     * </p>
     *
     * @param transaction the transaction domain object to save, must not be null
     * @return a {@link Mono} emitting the saved {@link Transaction} with its generated ID
     * @throws InvalidTransactionObjectException if the transaction is null
     */
    @Override
    public Mono<Transaction> saveTransaction(Transaction transaction) {
        if (Objects.isNull(transaction)) {
            log.error("Transaction object is null");
            return Mono.error(new InvalidTransactionObjectException("Transaction cannot be null"));
        }
        log.info("Saving transaction: {}", transaction.getTransactionId());
        return Mono.just(transaction)
                .map(transactionMapper::toEntity)
                .flatMap(transactionRepository::save)
                .map(transactionMapper::toDomain)
                .doOnSuccess(savedTransaction ->
                        log.info("Transaction saved successfully: {}", savedTransaction.getTransactionId()))
                .doOnError(error ->
                        log.error("Error saving transaction: {}", transaction.getTransactionId(), error));
    }

    @Override
    public Mono<Transaction> updateTransaction(List<Transaction> transaction) {
        if (Objects.isNull(transaction) || transaction.isEmpty()) {
            log.error("Transaction object is null or empty");
            return Mono.error(new InvalidTransactionObjectException("Transaction cannot be null or empty"));
        }
        log.info("Updating transactions: {}", transaction.stream().map(Transaction::getTransactionId).toList());
        return Flux.fromIterable(transaction)
                .map(transactionMapper::toEntity)
                .collectList()
                .flatMapMany(transactionRepository::saveAll)
                .map(transactionMapper::toDomain)
                .collectList()
                .map(updatedTransactions -> {
                    log.info("Transactions updated successfully: {}", updatedTransactions.stream().map(Transaction::getTransactionId).toList());
                    return updatedTransactions;
                })
                .flatMapMany(Flux::fromIterable)
                .next()
                .doOnError(error ->
                        log.error("Error updating transactions: {}", transaction.stream().map(Transaction::getTransactionId).toList(), error));
    }

    /**
     * Retrieves a transaction by its unique identifier.
     * <p>
     * This method fetches a transaction from the database, converts it to a domain object,
     * and logs the operation. If the transaction is not found, the returned Mono will be empty.
     * </p>
     *
     * @param id the unique identifier of the transaction to retrieve
     * @return a {@link Mono} emitting the {@link Transaction} if found, or empty if not found
     */
    @Override
    public Mono<Transaction> getTransactionById(Long id) {
        log.info("Fetching transaction by id: {}", id);
        return transactionRepository.findById(id)
                .map(transactionMapper::toDomain)
                .doOnSuccess(transaction ->
                        log.info("Transaction found: {}", id))
                .doOnError(error ->
                        log.error("Error fetching transaction: {}", id, error));
    }

    @Override
    public Flux<Transaction> getAllTransactionByAccount(Long id) {
        log.info("Fetching trxn for the account id: {}", id);
        return transactionRepository.findAllActiveByAccountId(id).
                map(transactionMapper::toDomain)
                .switchIfEmpty(Flux.error(new AccountDoesNotExist("Account does not exist with id: " + id)));


    }

}
