package in.srnyapathi.bank.domain.service;

import in.srnyapathi.bank.domain.model.Transaction;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Service interface for performing banking transactions.
 *
 * @author srnyapathi
 * @version 1.0.0
 * @since 1.0.0
 */
public interface TransactionService {

    Mono<Transaction> performTransaction(Long account, Long operationId, BigDecimal amount);
}
