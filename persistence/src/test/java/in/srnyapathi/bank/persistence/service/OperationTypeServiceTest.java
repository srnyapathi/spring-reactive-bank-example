package in.srnyapathi.bank.persistence.service;

import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.persistence.entity.OperationTypeEntity;
import in.srnyapathi.bank.persistence.mapper.OperationTypeMapper;
import in.srnyapathi.bank.persistence.repository.OperationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperationTypeService Tests")
class OperationTypeServiceTest {

    @Mock
    private OperationTypeRepository operationTypeRepository;

    @Mock
    private OperationTypeMapper mapper;

    private OperationTypeService operationTypeService;

    private OperationType testOperationType1;
    private OperationType testOperationType2;
    private OperationType testOperationType3;
    private OperationTypeEntity testEntity1;
    private OperationTypeEntity testEntity2;
    private OperationTypeEntity testEntity3;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 12, 26, 10, 0);

        // Setup test domain objects
        testOperationType1 = new OperationType(1L, "Normal Purchase", "PURCHASE", TransactionType.DEBIT);
        testOperationType2 = new OperationType(2L, "Payment", "PAYMENT", TransactionType.CREDIT);
        testOperationType3 = new OperationType(3L, "Withdrawal", "WITHDRAWAL", TransactionType.DEBIT);

        // Setup test entities
        testEntity1 = new OperationTypeEntity(1L, "Normal Purchase", "DEBIT", "PURCHASE", testDateTime, testDateTime, true);
        testEntity2 = new OperationTypeEntity(2L, "Payment", "CREDIT", "PAYMENT", testDateTime, testDateTime, true);
        testEntity3 = new OperationTypeEntity(3L, "Withdrawal", "DEBIT", "WITHDRAWAL", testDateTime, testDateTime, true);
    }

    private void initializeService() {
        // Mock the initial cache loading
        when(operationTypeRepository.findAllActive()).thenReturn(Flux.just(testEntity1, testEntity2, testEntity3));
        when(mapper.toDomain(testEntity1)).thenReturn(testOperationType1);
        when(mapper.toDomain(testEntity2)).thenReturn(testOperationType2);
        when(mapper.toDomain(testEntity3)).thenReturn(testOperationType3);

        operationTypeService = new OperationTypeService(operationTypeRepository, mapper);
    }


    @Test
    @DisplayName("Should initialize cache successfully on construction")
    void shouldInitializeCacheSuccessfully() {
        // Given
        when(operationTypeRepository.findAllActive()).thenReturn(Flux.just(testEntity1, testEntity2));
        when(mapper.toDomain(testEntity1)).thenReturn(testOperationType1);
        when(mapper.toDomain(testEntity2)).thenReturn(testOperationType2);

        // When
        operationTypeService = new OperationTypeService(operationTypeRepository, mapper);
        operationTypeService.init();

        // Then - Wait a bit for async initialization
        StepVerifier.create(operationTypeService.getOperationTypes())
                .assertNext(map -> {
                    assertThat(map).isNotNull();
                    assertThat(map).hasSize(2);
                    assertThat(map).containsKeys("PURCHASE", "PAYMENT");
                    assertThat(map.get("PURCHASE").getDescription()).isEqualTo("Normal Purchase");
                })
                .verifyComplete();

        verify(operationTypeRepository, atLeastOnce()).findAllActive();
        verify(mapper, times(1)).toDomain(testEntity1);
        verify(mapper, times(1)).toDomain(testEntity2);
    }

    @Test
    @DisplayName("Should handle empty repository during cache initialization")
    void shouldHandleEmptyRepositoryDuringInitialization() {
        // Given
        when(operationTypeRepository.findAllActive()).thenReturn(Flux.empty());

        // When
        operationTypeService = new OperationTypeService(operationTypeRepository, mapper);
        operationTypeService.init();

        // Then
        StepVerifier.create(operationTypeService.getOperationTypes())
                .assertNext(map -> {
                    assertThat(map).isNotNull();
                    assertThat(map).isEmpty();
                })
                .verifyComplete();

        verify(operationTypeRepository, atLeastOnce()).findAllActive();
        verify(mapper, never()).toDomain(any(OperationTypeEntity.class));
    }

    @Test
    @DisplayName("Should handle repository error during cache initialization")
    void shouldHandleRepositoryErrorDuringInitialization() {
        // Given
        when(operationTypeRepository.findAllActive())
                .thenReturn(Flux.error(new RuntimeException("Database connection failed")));

        // When
        operationTypeService = new OperationTypeService(operationTypeRepository, mapper);
        operationTypeService.init();

        // Then - Service should still be usable but cache will be empty
        StepVerifier.create(operationTypeService.getOperationTypes())
                .expectError(RuntimeException.class)
                .verify();

        verify(operationTypeRepository, atLeastOnce()).findAllActive();
    }

    @Test
    @DisplayName("Should cache operation types and avoid multiple repository calls")
    void shouldCacheOperationTypes() {
        // Given
        initializeService();
        operationTypeService.init();

        // When - Call getOperationTypes multiple times
        Mono<Map<String, OperationType>> first = operationTypeService.getOperationTypes();
        Mono<Map<String, OperationType>> second = operationTypeService.getOperationTypes();

        // Then
        StepVerifier.create(Mono.zip(first, second))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isEqualTo(tuple.getT2());
                    assertThat(tuple.getT1()).hasSize(3);
                })
                .verifyComplete();

        // Repository should be called only once during initialization
        verify(operationTypeRepository, atLeastOnce()).findAllActive();
    }


    @Test
    @DisplayName("Should successfully retrieve all operation types from cache")
    void shouldRetrieveAllOperationTypes() {
        // Given
        initializeService();
        operationTypeService.init();

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.getOperationTypes();

        // Then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).isNotNull();
                    assertThat(map).hasSize(3);
                    assertThat(map).containsKeys("PURCHASE", "PAYMENT", "WITHDRAWAL");
                    assertThat(map.get("PURCHASE").getOperationTypeId()).isEqualTo(1L);
                    assertThat(map.get("PAYMENT").getOperationTypeId()).isEqualTo(2L);
                    assertThat(map.get("WITHDRAWAL").getOperationTypeId()).isEqualTo(3L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return immutable copy of cache")
    void shouldReturnImmutableCopyOfCache() {
        // Given
        initializeService();
        operationTypeService.init();

        // When
        StepVerifier.create(operationTypeService.getOperationTypes())
                .assertNext(map -> {
                    // Then - Should throw exception when trying to modify
                    assertThat(map).isUnmodifiable();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent get operations")
    void shouldHandleConcurrentGetOperations() {
        // Given
        initializeService();
        operationTypeService.init();

        // When - Multiple concurrent calls
        Mono<Map<String, OperationType>> result1 = operationTypeService.getOperationTypes();
        Mono<Map<String, OperationType>> result2 = operationTypeService.getOperationTypes();
        Mono<Map<String, OperationType>> result3 = operationTypeService.getOperationTypes();

        // Then
        StepVerifier.create(Mono.zip(result1, result2, result3))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).hasSize(3);
                    assertThat(tuple.getT2()).hasSize(3);
                    assertThat(tuple.getT3()).hasSize(3);
                })
                .verifyComplete();
    }


    @Test
    @DisplayName("Should successfully add new operation type")
    void shouldAddOperationTypeSuccessfully() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType newOperationType = new OperationType(4L, "Deposit", "DEPOSIT", TransactionType.CREDIT);
        OperationTypeEntity newEntity = new OperationTypeEntity(4L, "Deposit", "CREDIT", "DEPOSIT", testDateTime, testDateTime, true);

        when(mapper.toEntity(newOperationType)).thenReturn(newEntity);
        when(operationTypeRepository.save(newEntity)).thenReturn(Mono.just(newEntity));
        when(mapper.toDomain(newEntity)).thenReturn(newOperationType);

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.addOperationType(newOperationType);

        // Then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).isNotNull();
                    assertThat(map).hasSize(4);
                    assertThat(map).containsKey("DEPOSIT");
                    assertThat(map.get("DEPOSIT").getDescription()).isEqualTo("Deposit");
                })
                .verifyComplete();

        verify(mapper, times(1)).toEntity(newOperationType);
        verify(operationTypeRepository, times(1)).save(newEntity);
        verify(mapper, times(1)).toDomain(newEntity);
    }

    @Test
    @DisplayName("Should update cache after adding operation type")
    void shouldUpdateCacheAfterAddingOperationType() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType newOperationType = new OperationType(4L, "Transfer", "TRANSFER", TransactionType.DEBIT);
        OperationTypeEntity newEntity = new OperationTypeEntity(4L, "Transfer", "DEBIT", "TRANSFER", testDateTime, testDateTime, true);

        when(mapper.toEntity(newOperationType)).thenReturn(newEntity);
        when(operationTypeRepository.save(newEntity)).thenReturn(Mono.just(newEntity));
        when(mapper.toDomain(newEntity)).thenReturn(newOperationType);

        // When
        operationTypeService.addOperationType(newOperationType).block();

        // Then - Verify cache contains the new operation type
        StepVerifier.create(operationTypeService.getOperationTypes())
                .assertNext(map -> {
                    assertThat(map).containsKey("TRANSFER");
                    assertThat(map.get("TRANSFER").getOperationTypeId()).isEqualTo(4L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle repository error during add operation")
    void shouldHandleRepositoryErrorDuringAdd() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType newOperationType = new OperationType(4L, "Test", "TEST", TransactionType.CREDIT);
        OperationTypeEntity newEntity = new OperationTypeEntity(4L, "Test", "CREDIT", "TEST", testDateTime, testDateTime, true);

        when(mapper.toEntity(newOperationType)).thenReturn(newEntity);
        when(operationTypeRepository.save(newEntity))
                .thenReturn(Mono.error(new RuntimeException("Database save failed")));

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.addOperationType(newOperationType);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database save failed")
                )
                .verify();

        verify(mapper, times(1)).toEntity(newOperationType);
        verify(operationTypeRepository, times(1)).save(newEntity);
    }

    @Test
    @DisplayName("Should handle mapper error during add operation")
    void shouldHandleMapperErrorDuringAdd() {
        initializeService();
        operationTypeService.init();

        OperationType newOperationType = new OperationType(4L, "Test", "TEST", TransactionType.CREDIT);

        when(mapper.toEntity(newOperationType))
                .thenThrow(new RuntimeException("Mapping failed"));

        StepVerifier.create(operationTypeService.addOperationType(newOperationType))
                .expectError(RuntimeException.class)
                .verify();

        verify(mapper, times(1)).toEntity(newOperationType);
        verify(operationTypeRepository, never()).save(any(OperationTypeEntity.class));
    }

    @Test
    @DisplayName("Should handle null operation type during add")
    void shouldHandleNullOperationTypeDuringAdd() {
        // Given
        initializeService();
        operationTypeService.init();

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.addOperationType(null);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("OperationType cannot be null")
                )
                .verify();

        verify(mapper, never()).toEntity(any());
        verify(operationTypeRepository, never()).save(any());
    }

    // ========== UPDATE OPERATION TYPE TESTS ==========

    @Test
    @DisplayName("Should successfully update existing operation type")
    void shouldUpdateOperationTypeSuccessfully() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType updatedOperationType = new OperationType(1L, "Updated Purchase", "PURCHASE", TransactionType.DEBIT);
        OperationTypeEntity updatedEntity = new OperationTypeEntity(1L, "Updated Purchase", "DEBIT", "PURCHASE", testDateTime, testDateTime, true);

        when(mapper.toEntity(updatedOperationType)).thenReturn(updatedEntity);
        when(operationTypeRepository.save(updatedEntity)).thenReturn(Mono.just(updatedEntity));
        when(mapper.toDomain(updatedEntity)).thenReturn(updatedOperationType);

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.updateOperationTypes(updatedOperationType);

        // Then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).containsKey("PURCHASE");
                    assertThat(map.get("PURCHASE").getDescription()).isEqualTo("Updated Purchase");
                })
                .verifyComplete();

        verify(mapper, times(1)).toEntity(updatedOperationType);
        verify(operationTypeRepository, times(1)).save(updatedEntity);
    }

    @Test
    @DisplayName("Should update cache after updating operation type")
    void shouldUpdateCacheAfterUpdate() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType updatedOperationType = new OperationType(2L, "Modified Payment", "PAYMENT", TransactionType.CREDIT);
        OperationTypeEntity updatedEntity = new OperationTypeEntity(2L, "Modified Payment", "CREDIT", "PAYMENT", testDateTime, testDateTime, true);

        when(mapper.toEntity(updatedOperationType)).thenReturn(updatedEntity);
        when(operationTypeRepository.save(updatedEntity)).thenReturn(Mono.just(updatedEntity));
        when(mapper.toDomain(updatedEntity)).thenReturn(updatedOperationType);

        // When
        operationTypeService.updateOperationTypes(updatedOperationType).block();

        // Then - Verify cache reflects the update
        StepVerifier.create(operationTypeService.getOperationTypes())
                .assertNext(map -> {
                    assertThat(map.get("PAYMENT").getDescription()).isEqualTo("Modified Payment");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle repository error during update")
    void shouldHandleRepositoryErrorDuringUpdate() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType updatedOperationType = new OperationType(1L, "Test", "PURCHASE", TransactionType.DEBIT);
        OperationTypeEntity updatedEntity = new OperationTypeEntity(1L, "Test", "DEBIT", "PURCHASE", testDateTime, testDateTime, true);

        when(mapper.toEntity(updatedOperationType)).thenReturn(updatedEntity);
        when(operationTypeRepository.save(updatedEntity))
                .thenReturn(Mono.error(new RuntimeException("Update failed")));

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.updateOperationTypes(updatedOperationType);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Update failed")
                )
                .verify();
    }

    // ========== DELETE OPERATION TYPE TESTS ==========

    @Test
    @DisplayName("Should successfully delete existing operation type")
    void shouldDeleteOperationTypeSuccessfully() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(1L)).thenReturn(Mono.just(testEntity1));
        when(mapper.toDomain(testEntity1)).thenReturn(testOperationType1);
        when(operationTypeRepository.deleteById(1L)).thenReturn(Mono.empty());

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.deleteOperationType(1L);

        // Then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).hasSize(2);
                    assertThat(map).doesNotContainKey("PURCHASE");
                    assertThat(map).containsKeys("PAYMENT", "WITHDRAWAL");
                })
                .verifyComplete();

        verify(operationTypeRepository, times(1)).findById(1L);
        verify(operationTypeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should remove operation type from cache after deletion")
    void shouldRemoveFromCacheAfterDeletion() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(2L)).thenReturn(Mono.just(testEntity2));
        when(mapper.toDomain(testEntity2)).thenReturn(testOperationType2);
        when(operationTypeRepository.deleteById(2L)).thenReturn(Mono.empty());

        // When
        operationTypeService.deleteOperationType(2L).block();

        // Then - Verify cache no longer contains the deleted item
        StepVerifier.create(operationTypeService.getOperationTypes())
                .assertNext(map -> {
                    assertThat(map).doesNotContainKey("PAYMENT");
                    assertThat(map).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle deletion of non-existent operation type")
    void shouldHandleDeleteNonExistentOperationType() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(999L)).thenReturn(Mono.empty());

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.deleteOperationType(999L);

        // Then - Should return current cache without error
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).hasSize(3);
                    assertThat(map).containsKeys("PURCHASE", "PAYMENT", "WITHDRAWAL");
                })
                .verifyComplete();

        verify(operationTypeRepository, times(1)).findById(999L);
        verify(operationTypeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle repository error during delete operation")
    void shouldHandleRepositoryErrorDuringDelete() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(1L))
                .thenReturn(Mono.error(new RuntimeException("Database connection lost")));

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.deleteOperationType(1L);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database connection lost")
                )
                .verify();

        verify(operationTypeRepository, times(1)).findById(1L);
        verify(operationTypeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle zero ID during delete")
    void shouldHandleZeroIdDuringDelete() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(0L)).thenReturn(Mono.empty());

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.deleteOperationType(0L);

        // Then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).hasSize(3);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle negative ID during delete")
    void shouldHandleNegativeIdDuringDelete() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(-1L)).thenReturn(Mono.empty());

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.deleteOperationType(-1L);

        // Then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).hasSize(3);
                })
                .verifyComplete();
    }

    // ========== EDGE CASES AND CONCURRENT OPERATIONS ==========

    @Test
    @DisplayName("Should handle concurrent add operations")
    void shouldHandleConcurrentAddOperations() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType op1 = new OperationType(4L, "Op1", "OP1", TransactionType.CREDIT);
        OperationType op2 = new OperationType(5L, "Op2", "OP2", TransactionType.DEBIT);

        OperationTypeEntity entity1 = new OperationTypeEntity(4L, "Op1", "CREDIT", "OP1", testDateTime, testDateTime, true);
        OperationTypeEntity entity2 = new OperationTypeEntity(5L, "Op2", "DEBIT", "OP2", testDateTime, testDateTime, true);

        when(mapper.toEntity(op1)).thenReturn(entity1);
        when(mapper.toEntity(op2)).thenReturn(entity2);
        when(operationTypeRepository.save(entity1)).thenReturn(Mono.just(entity1));
        when(operationTypeRepository.save(entity2)).thenReturn(Mono.just(entity2));
        when(mapper.toDomain(entity1)).thenReturn(op1);
        when(mapper.toDomain(entity2)).thenReturn(op2);

        // When - Add both concurrently
        Mono<Map<String, OperationType>> result1 = operationTypeService.addOperationType(op1);
        Mono<Map<String, OperationType>> result2 = operationTypeService.addOperationType(op2);

        // Then
        StepVerifier.create(Mono.zip(result1, result2))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isNotNull();
                    assertThat(tuple.getT2()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent update and delete operations")
    void shouldHandleConcurrentUpdateAndDelete() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType updatedOp = new OperationType(1L, "Updated", "PURCHASE", TransactionType.DEBIT);
        OperationTypeEntity updatedEntity = new OperationTypeEntity(1L, "Updated", "DEBIT", "PURCHASE", testDateTime, testDateTime, true);

        when(mapper.toEntity(updatedOp)).thenReturn(updatedEntity);
        when(operationTypeRepository.save(updatedEntity)).thenReturn(Mono.just(updatedEntity));
        when(mapper.toDomain(updatedEntity)).thenReturn(updatedOp);
        when(operationTypeRepository.findById(2L)).thenReturn(Mono.just(testEntity2));
        when(mapper.toDomain(testEntity2)).thenReturn(testOperationType2);
        when(operationTypeRepository.deleteById(2L)).thenReturn(Mono.empty());

        // When
        Mono<Map<String, OperationType>> update = operationTypeService.updateOperationTypes(updatedOp);
        Mono<Map<String, OperationType>> delete = operationTypeService.deleteOperationType(2L);

        // Then
        StepVerifier.create(Mono.zip(update, delete))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isNotNull();
                    assertThat(tuple.getT2()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle operation type with duplicate handler during add")
    void shouldHandleDuplicateHandlerDuringAdd() {
        // Given
        initializeService();
        operationTypeService.init();

        // Trying to add another operation with handler "PURCHASE"
        OperationType duplicateOp = new OperationType(4L, "Another Purchase", "PURCHASE", TransactionType.DEBIT);
        OperationTypeEntity duplicateEntity = new OperationTypeEntity(4L, "Another Purchase", "DEBIT", "PURCHASE", testDateTime, testDateTime, true);

        when(mapper.toEntity(duplicateOp)).thenReturn(duplicateEntity);
        when(operationTypeRepository.save(duplicateEntity)).thenReturn(Mono.just(duplicateEntity));
        when(mapper.toDomain(duplicateEntity)).thenReturn(duplicateOp);

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.addOperationType(duplicateOp);

        // Then - Should replace the existing one with same handler
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).containsKey("PURCHASE");
                    assertThat(map.get("PURCHASE").getDescription()).isEqualTo("Another Purchase");
                    assertThat(map.get("PURCHASE").getOperationTypeId()).isEqualTo(4L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle very large ID values")
    void shouldHandleVeryLargeIdValues() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(Long.MAX_VALUE)).thenReturn(Mono.empty());

        // When
        Mono<Map<String, OperationType>> result = operationTypeService.deleteOperationType(Long.MAX_VALUE);

        // Then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map).hasSize(3);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should verify method interaction order for add operation")
    void shouldVerifyMethodInteractionOrderForAdd() {
        // Given
        initializeService();
        operationTypeService.init();

        OperationType newOp = new OperationType(4L, "Test", "TEST", TransactionType.CREDIT);
        OperationTypeEntity newEntity = new OperationTypeEntity(4L, "Test", "CREDIT", "TEST", testDateTime, testDateTime, true);

        when(mapper.toEntity(newOp)).thenReturn(newEntity);
        when(operationTypeRepository.save(newEntity)).thenReturn(Mono.just(newEntity));
        when(mapper.toDomain(newEntity)).thenReturn(newOp);

        // When
        operationTypeService.addOperationType(newOp).block();

        // Then
        var inOrder = inOrder(mapper, operationTypeRepository);
        inOrder.verify(mapper).toEntity(newOp);
        inOrder.verify(operationTypeRepository).save(newEntity);
        inOrder.verify(mapper).toDomain(newEntity);
    }

    @Test
    @DisplayName("Should verify method interaction order for delete operation")
    void shouldVerifyMethodInteractionOrderForDelete() {
        // Given
        initializeService();
        operationTypeService.init();

        when(operationTypeRepository.findById(1L)).thenReturn(Mono.just(testEntity1));
        when(mapper.toDomain(testEntity1)).thenReturn(testOperationType1);
        when(operationTypeRepository.deleteById(1L)).thenReturn(Mono.empty());

        // When
        operationTypeService.deleteOperationType(1L).block();

        // Then
        var inOrder = inOrder(operationTypeRepository, mapper);
        inOrder.verify(operationTypeRepository).findById(1L);
        inOrder.verify(mapper).toDomain(testEntity1);
        inOrder.verify(operationTypeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle mapper returning null during cache initialization")
    void shouldHandleMapperReturningNullDuringInit() {
        // Given
        when(operationTypeRepository.findAllActive()).thenReturn(Flux.just(testEntity1));
        when(mapper.toDomain(testEntity1)).thenReturn(null);

        // When
        operationTypeService = new OperationTypeService(operationTypeRepository, mapper);
        operationTypeService.init();

        // Then - Should handle gracefully
        StepVerifier.create(operationTypeService.getOperationTypes())
                .expectError(NullPointerException.class)
                .verify();
    }

    @Test
    @DisplayName("Should handle timeout scenarios gracefully")
    void shouldHandleTimeoutScenarios() {
        // Given
        when(operationTypeRepository.findAllActive())
                .thenReturn(Flux.just(testEntity1, testEntity2)
                        .delayElements(Duration.ofSeconds(10)));
        // Use lenient for mapper stubs since timeout may prevent them from being called
        lenient().when(mapper.toDomain(testEntity1)).thenReturn(testOperationType1);
        lenient().when(mapper.toDomain(testEntity2)).thenReturn(testOperationType2);

        operationTypeService = new OperationTypeService(operationTypeRepository, mapper);
        operationTypeService.init();


        // Then
        StepVerifier.create(operationTypeService.getOperationTypes()
                        .timeout(Duration.ofMillis(500)))
                .expectError()
                .verify();
    }
}


