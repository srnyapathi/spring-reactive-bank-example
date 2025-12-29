package in.srnyapathi.bank.persistence.repository;

import in.srnyapathi.bank.persistence.entity.OperationTypeEntity;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OperationTypeRepository extends ReactiveCrudRepository<OperationTypeEntity, Long> {

    @Query("SELECT * FROM operation_types WHERE is_active = true")
    Flux<OperationTypeEntity> findAllActive();

    @Modifying
    @Query("UPDATE operation_types SET is_active = false WHERE operation_id = $1 AND is_active = true")
    Mono<Integer> softDeleteById(Long id);

}
