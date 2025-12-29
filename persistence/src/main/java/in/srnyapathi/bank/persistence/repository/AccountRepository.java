package in.srnyapathi.bank.persistence.repository;

import in.srnyapathi.bank.persistence.entity.AccountEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, Long> {
    @Query("SELECT * FROM accounts WHERE is_active = true")
    Flux<AccountEntity> findAllActive();

    @Query("SELECT * FROM accounts WHERE account_id = $1 AND is_active = true")
    Mono<AccountEntity> findActiveAccount(Long id);

    @Modifying
    @Query("UPDATE accounts SET is_active = false WHERE account_id = $1 AND is_active = true")
    Mono<Integer> softDeleteById(Long id);
}
