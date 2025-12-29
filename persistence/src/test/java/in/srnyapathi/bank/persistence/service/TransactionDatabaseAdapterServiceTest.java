package in.srnyapathi.bank.persistence.service;

import in.srnyapathi.bank.domain.exception.InvalidTransactionObjectException;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.persistence.entity.TransactionEntity;
import in.srnyapathi.bank.persistence.mapper.TransactionMapper;
import in.srnyapathi.bank.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionDatabaseAdapterService Tests")
class TransactionDatabaseAdapterServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionDatabaseAdapterService transactionDatabaseAdapterService;

    private Transaction testTransaction;
    private TransactionEntity testTransactionEntity;
    private AccountNumber testAccountNumber;
    private OperationType testOperationType;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 12, 26, 10, 0);

        testAccountNumber = AccountNumber.builder()
                .id(1001L)
                .build();

        testOperationType = new OperationType(1L, "Normal Purchase", "PURCHASE", TransactionType.DEBIT);

        testTransaction = Transaction.builder()
                .transactionId(100L)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(new BigDecimal("123.45"))
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        testTransactionEntity = new TransactionEntity(
                100L,
                1001L,
                1L,
                new BigDecimal("123.45"),
                testDateTime,
                testDateTime,
                testDateTime,
                true
        );
    }


    @Test
    @DisplayName("Should successfully save transaction with valid data")
    void shouldSaveTransactionSuccessfully() {
        // Given
        when(transactionMapper.toEntity(testTransaction)).thenReturn(testTransactionEntity);
        when(transactionRepository.save(testTransactionEntity)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionMapper.toDomain(testTransactionEntity)).thenReturn(testTransaction);

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(testTransaction);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getTransactionId()).isEqualTo(100L);
                    assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
                    assertThat(transaction.getAccount().getId()).isEqualTo(1001L);
                    assertThat(transaction.getOperationType().getOperationTypeId()).isEqualTo(1L);
                    assertThat(transaction.isActive()).isTrue();
                })
                .verifyComplete();

        verify(transactionMapper, times(1)).toEntity(testTransaction);
        verify(transactionRepository, times(1)).save(testTransactionEntity);
        verify(transactionMapper, times(1)).toDomain(testTransactionEntity);
    }

    @Test
    @DisplayName("Should save transaction with null transaction ID (new transaction)")
    void shouldSaveTransactionWithNullId() {
        // Given
        Transaction newTransaction = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(new BigDecimal("50.00"))
                .eventDate(testDateTime)
                .active(true)
                .build();

        TransactionEntity newEntity = new TransactionEntity(
                null,
                1001L,
                1L,
                new BigDecimal("50.00"),
                testDateTime,
                null,
                null,
                true
        );

        TransactionEntity savedEntity = new TransactionEntity(
                200L,
                1001L,
                1L,
                new BigDecimal("50.00"),
                testDateTime,
                testDateTime,
                testDateTime,
                true
        );

        Transaction savedTransaction = Transaction.builder()
                .transactionId(200L)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(new BigDecimal("50.00"))
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        when(transactionMapper.toEntity(newTransaction)).thenReturn(newEntity);
        when(transactionRepository.save(newEntity)).thenReturn(Mono.just(savedEntity));
        when(transactionMapper.toDomain(savedEntity)).thenReturn(savedTransaction);

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(newTransaction);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getTransactionId()).isEqualTo(200L);
                    assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
                })
                .verifyComplete();

        verify(transactionMapper, times(1)).toEntity(newTransaction);
        verify(transactionRepository, times(1)).save(newEntity);
        verify(transactionMapper, times(1)).toDomain(savedEntity);
    }

    @Test
    @DisplayName("Should save transaction with zero amount")
    void shouldSaveTransactionWithZeroAmount() {
        // Given
        Transaction zeroAmountTransaction = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(BigDecimal.ZERO)
                .eventDate(testDateTime)
                .active(true)
                .build();

        TransactionEntity zeroEntity = new TransactionEntity(
                null,
                1001L,
                1L,
                BigDecimal.ZERO,
                testDateTime,
                null,
                null,
                true
        );

        TransactionEntity savedEntity = new TransactionEntity(
                300L,
                1001L,
                1L,
                BigDecimal.ZERO,
                testDateTime,
                testDateTime,
                testDateTime,
                true
        );

        Transaction savedTransaction = Transaction.builder()
                .transactionId(300L)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(BigDecimal.ZERO)
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        when(transactionMapper.toEntity(zeroAmountTransaction)).thenReturn(zeroEntity);
        when(transactionRepository.save(zeroEntity)).thenReturn(Mono.just(savedEntity));
        when(transactionMapper.toDomain(savedEntity)).thenReturn(savedTransaction);

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(zeroAmountTransaction);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save transaction with very large amount")
    void shouldSaveTransactionWithVeryLargeAmount() {
        // Given
        BigDecimal largeAmount = new BigDecimal("999999999999.99");
        Transaction largeAmountTransaction = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(largeAmount)
                .eventDate(testDateTime)
                .active(true)
                .build();

        TransactionEntity largeEntity = new TransactionEntity(
                null,
                1001L,
                1L,
                largeAmount,
                testDateTime,
                null,
                null,
                true
        );

        TransactionEntity savedEntity = new TransactionEntity(
                400L,
                1001L,
                1L,
                largeAmount,
                testDateTime,
                testDateTime,
                testDateTime,
                true
        );

        Transaction savedTransaction = Transaction.builder()
                .transactionId(400L)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(largeAmount)
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        when(transactionMapper.toEntity(largeAmountTransaction)).thenReturn(largeEntity);
        when(transactionRepository.save(largeEntity)).thenReturn(Mono.just(savedEntity));
        when(transactionMapper.toDomain(savedEntity)).thenReturn(savedTransaction);

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(largeAmountTransaction);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getAmount()).isEqualByComparingTo(largeAmount);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save transaction with very small decimal amount")
    void shouldSaveTransactionWithSmallDecimalAmount() {
        // Given
        BigDecimal smallAmount = new BigDecimal("0.01");
        Transaction smallAmountTransaction = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(smallAmount)
                .eventDate(testDateTime)
                .active(true)
                .build();

        TransactionEntity smallEntity = new TransactionEntity(
                null,
                1001L,
                1L,
                smallAmount,
                testDateTime,
                null,
                null,
                true
        );

        TransactionEntity savedEntity = new TransactionEntity(
                500L,
                1001L,
                1L,
                smallAmount,
                testDateTime,
                testDateTime,
                testDateTime,
                true
        );

        Transaction savedTransaction = Transaction.builder()
                .transactionId(500L)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(smallAmount)
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        when(transactionMapper.toEntity(smallAmountTransaction)).thenReturn(smallEntity);
        when(transactionRepository.save(smallEntity)).thenReturn(Mono.just(savedEntity));
        when(transactionMapper.toDomain(savedEntity)).thenReturn(savedTransaction);

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(smallAmountTransaction);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getAmount()).isEqualByComparingTo(smallAmount);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle repository error during save")
    void shouldHandleRepositoryErrorDuringSave() {
        // Given
        when(transactionMapper.toEntity(testTransaction)).thenReturn(testTransactionEntity);
        when(transactionRepository.save(testTransactionEntity))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(testTransaction);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database connection failed")
                )
                .verify();

        verify(transactionMapper, times(1)).toEntity(testTransaction);
        verify(transactionRepository, times(1)).save(testTransactionEntity);
        verify(transactionMapper, never()).toDomain(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Should handle mapper error during entity conversion")
    void shouldHandleMapperErrorDuringEntityConversion() {
        // Given
        when(transactionMapper.toEntity(testTransaction))
                .thenThrow(new RuntimeException("Mapping error"));

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(testTransaction);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Mapping error")
                )
                .verify();

        verify(transactionMapper, times(1)).toEntity(testTransaction);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Should handle mapper error during domain conversion")
    void shouldHandleMapperErrorDuringDomainConversion() {
        // Given
        when(transactionMapper.toEntity(testTransaction)).thenReturn(testTransactionEntity);
        when(transactionRepository.save(testTransactionEntity)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionMapper.toDomain(testTransactionEntity))
                .thenThrow(new RuntimeException("Domain mapping error"));

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(testTransaction);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Domain mapping error")
                )
                .verify();

        verify(transactionMapper, times(1)).toEntity(testTransaction);
        verify(transactionRepository, times(1)).save(testTransactionEntity);
        verify(transactionMapper, times(1)).toDomain(testTransactionEntity);
    }

    @Test
    @DisplayName("Should handle null transaction during save")
    void shouldHandleNullTransactionDuringSave() {
        StepVerifier.create(transactionDatabaseAdapterService.saveTransaction(null))
                .expectError(InvalidTransactionObjectException.class)
                .verify();
    }

    @Test
    @DisplayName("Should save inactive transaction")
    void shouldSaveInactiveTransaction() {
        // Given
        Transaction inactiveTransaction = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(new BigDecimal("100.00"))
                .eventDate(testDateTime)
                .active(false)
                .build();

        TransactionEntity inactiveEntity = new TransactionEntity(
                null,
                1001L,
                1L,
                new BigDecimal("100.00"),
                testDateTime,
                null,
                null,
                false
        );

        TransactionEntity savedEntity = new TransactionEntity(
                600L,
                1001L,
                1L,
                new BigDecimal("100.00"),
                testDateTime,
                testDateTime,
                testDateTime,
                false
        );

        Transaction savedTransaction = Transaction.builder()
                .transactionId(600L)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(new BigDecimal("100.00"))
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(false)
                .build();

        when(transactionMapper.toEntity(inactiveTransaction)).thenReturn(inactiveEntity);
        when(transactionRepository.save(inactiveEntity)).thenReturn(Mono.just(savedEntity));
        when(transactionMapper.toDomain(savedEntity)).thenReturn(savedTransaction);

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.saveTransaction(inactiveTransaction);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.isActive()).isFalse();
                })
                .verifyComplete();
    }


    @Test
    @DisplayName("Should successfully retrieve transaction by ID")
    void shouldGetTransactionByIdSuccessfully() {
        // Given
        when(transactionRepository.findById(100L)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionMapper.toDomain(testTransactionEntity)).thenReturn(testTransaction);

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.getTransactionById(100L);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getTransactionId()).isEqualTo(100L);
                    assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
                    assertThat(transaction.getAccount().getId()).isEqualTo(1001L);
                })
                .verifyComplete();

        verify(transactionRepository, times(1)).findById(100L);
        verify(transactionMapper, times(1)).toDomain(testTransactionEntity);
    }

    @Test
    @DisplayName("Should return empty when transaction not found")
    void shouldReturnEmptyWhenTransactionNotFound() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Mono.empty());

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.getTransactionById(999L);

        // Then
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(transactionRepository, times(1)).findById(999L);
        verify(transactionMapper, never()).toDomain(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Should handle repository error during get by ID")
    void shouldHandleRepositoryErrorDuringGetById() {
        // Given
        when(transactionRepository.findById(100L))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.getTransactionById(100L);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database error")
                )
                .verify();

        verify(transactionRepository, times(1)).findById(100L);
        verify(transactionMapper, never()).toDomain(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Should handle mapper error during get by ID")
    void shouldHandleMapperErrorDuringGetById() {
        // Given
        when(transactionRepository.findById(100L)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionMapper.toDomain(testTransactionEntity))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.getTransactionById(100L);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Mapping failed")
                )
                .verify();

        verify(transactionRepository, times(1)).findById(100L);
        verify(transactionMapper, times(1)).toDomain(testTransactionEntity);
    }

    @Test
    @DisplayName("Should handle zero ID during get")
    void shouldHandleZeroIdDuringGet() {
        // Given
        when(transactionRepository.findById(0L)).thenReturn(Mono.empty());

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.getTransactionById(0L);

        // Then
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(transactionRepository, times(1)).findById(0L);
    }

    @Test
    @DisplayName("Should handle negative ID during get")
    void shouldHandleNegativeIdDuringGet() {
        // Given
        when(transactionRepository.findById(-1L)).thenReturn(Mono.empty());

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.getTransactionById(-1L);

        // Then
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(transactionRepository, times(1)).findById(-1L);
    }

    @Test
    @DisplayName("Should handle very large ID during get")
    void shouldHandleVeryLargeIdDuringGet() {
        // Given
        when(transactionRepository.findById(Long.MAX_VALUE)).thenReturn(Mono.empty());

        // When
        Mono<Transaction> result = transactionDatabaseAdapterService.getTransactionById(Long.MAX_VALUE);

        // Then
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(transactionRepository, times(1)).findById(Long.MAX_VALUE);
    }


    @Test
    @DisplayName("Should verify method interaction order for save")
    void shouldVerifyMethodInteractionOrderForSave() {
        // Given
        when(transactionMapper.toEntity(testTransaction)).thenReturn(testTransactionEntity);
        when(transactionRepository.save(testTransactionEntity)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionMapper.toDomain(testTransactionEntity)).thenReturn(testTransaction);

        // When
        transactionDatabaseAdapterService.saveTransaction(testTransaction).block();

        // Then
        var inOrder = inOrder(transactionMapper, transactionRepository);
        inOrder.verify(transactionMapper).toEntity(testTransaction);
        inOrder.verify(transactionRepository).save(testTransactionEntity);
        inOrder.verify(transactionMapper).toDomain(testTransactionEntity);
    }

    @Test
    @DisplayName("Should verify method interaction order for get")
    void shouldVerifyMethodInteractionOrderForGet() {
        // Given
        when(transactionRepository.findById(100L)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionMapper.toDomain(testTransactionEntity)).thenReturn(testTransaction);

        // When
        transactionDatabaseAdapterService.getTransactionById(100L).block();

        // Then
        var inOrder = inOrder(transactionRepository, transactionMapper);
        inOrder.verify(transactionRepository).findById(100L);
        inOrder.verify(transactionMapper).toDomain(testTransactionEntity);
    }

    @Test
    @DisplayName("Should handle concurrent save operations")
    void shouldHandleConcurrentSaveOperations() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(new BigDecimal("100.00"))
                .eventDate(testDateTime)
                .active(true)
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(new BigDecimal("200.00"))
                .eventDate(testDateTime)
                .active(true)
                .build();

        TransactionEntity entity1 = new TransactionEntity(null, 1001L, 1L, new BigDecimal("100.00"), testDateTime, null, null, true);
        TransactionEntity entity2 = new TransactionEntity(null, 1001L, 1L, new BigDecimal("200.00"), testDateTime, null, null, true);
        TransactionEntity savedEntity1 = new TransactionEntity(700L, 1001L, 1L, new BigDecimal("100.00"), testDateTime, testDateTime, testDateTime, true);
        TransactionEntity savedEntity2 = new TransactionEntity(800L, 1001L, 1L, new BigDecimal("200.00"), testDateTime, testDateTime, testDateTime, true);

        Transaction saved1 = Transaction.builder().transactionId(700L).account(testAccountNumber).operationType(testOperationType).amount(new BigDecimal("100.00")).eventDate(testDateTime).createdAt(testDateTime).updatedAt(testDateTime).active(true).build();
        Transaction saved2 = Transaction.builder().transactionId(800L).account(testAccountNumber).operationType(testOperationType).amount(new BigDecimal("200.00")).eventDate(testDateTime).createdAt(testDateTime).updatedAt(testDateTime).active(true).build();

        when(transactionMapper.toEntity(transaction1)).thenReturn(entity1);
        when(transactionMapper.toEntity(transaction2)).thenReturn(entity2);
        when(transactionRepository.save(entity1)).thenReturn(Mono.just(savedEntity1));
        when(transactionRepository.save(entity2)).thenReturn(Mono.just(savedEntity2));
        when(transactionMapper.toDomain(savedEntity1)).thenReturn(saved1);
        when(transactionMapper.toDomain(savedEntity2)).thenReturn(saved2);

        // When
        Mono<Transaction> result1 = transactionDatabaseAdapterService.saveTransaction(transaction1);
        Mono<Transaction> result2 = transactionDatabaseAdapterService.saveTransaction(transaction2);

        // Then
        StepVerifier.create(Mono.zip(result1, result2))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isNotNull();
                    assertThat(tuple.getT2()).isNotNull();
                    assertThat(tuple.getT1().getTransactionId()).isEqualTo(700L);
                    assertThat(tuple.getT2().getTransactionId()).isEqualTo(800L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent get operations")
    void shouldHandleConcurrentGetOperations() {
        // Given
        when(transactionRepository.findById(100L)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionRepository.findById(200L)).thenReturn(Mono.just(testTransactionEntity));
        when(transactionMapper.toDomain(testTransactionEntity)).thenReturn(testTransaction);

        // When
        Mono<Transaction> result1 = transactionDatabaseAdapterService.getTransactionById(100L);
        Mono<Transaction> result2 = transactionDatabaseAdapterService.getTransactionById(200L);

        // Then
        StepVerifier.create(Mono.zip(result1, result2))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isNotNull();
                    assertThat(tuple.getT2()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should not call mapper when repository returns empty")
    void shouldNotCallMapperWhenRepositoryReturnsEmpty() {
        // Given
        when(transactionRepository.findById(anyLong())).thenReturn(Mono.empty());

        // When
        transactionDatabaseAdapterService.getTransactionById(999L).block();

        // Then
        verify(transactionMapper, never()).toDomain(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Should handle transaction with negative amount")
    void shouldHandleTransactionWithNegativeAmount() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        Transaction negativeTransaction = Transaction.builder()
                .transactionId(null)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(negativeAmount)
                .eventDate(testDateTime)
                .active(true)
                .build();

        TransactionEntity negativeEntity = new TransactionEntity(
                null,
                1001L,
                1L,
                negativeAmount,
                testDateTime,
                null,
                null,
                true
        );

        TransactionEntity savedEntity = new TransactionEntity(
                900L,
                1001L,
                1L,
                negativeAmount,
                testDateTime,
                testDateTime,
                testDateTime,
                true
        );

        Transaction savedTransaction = Transaction.builder()
                .transactionId(900L)
                .account(testAccountNumber)
                .operationType(testOperationType)
                .amount(negativeAmount)
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        when(transactionMapper.toEntity(negativeTransaction)).thenReturn(negativeEntity);
        when(transactionRepository.save(negativeEntity)).thenReturn(Mono.just(savedEntity));
        when(transactionMapper.toDomain(savedEntity)).thenReturn(savedTransaction);


        StepVerifier.create(transactionDatabaseAdapterService.saveTransaction(negativeTransaction))
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getAmount()).isEqualByComparingTo(negativeAmount);
                })
                .verifyComplete();
    }
}


