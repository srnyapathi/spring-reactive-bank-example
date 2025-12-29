package in.srnyapathi.bank.persistence.repository;

import in.srnyapathi.bank.persistence.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, Long> {
    @Query("SELECT * FROM transactions WHERE is_active = true")
    Flux<TransactionEntity> findAllActive();

    @Modifying
    @Query("UPDATE transactions SET is_active = false WHERE transaction_id = $1 AND is_active = true")
    Mono<Integer> softDeleteById(Long id);
}
