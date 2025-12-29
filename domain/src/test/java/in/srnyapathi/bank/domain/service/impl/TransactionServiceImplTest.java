package in.srnyapathi.bank.domain.service.impl;

import in.srnyapathi.bank.domain.adapters.OperationTypeDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidOperationTypeException;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.domain.service.factory.TransactionFactory;
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
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionFactory transactionFactory;

    @Mock
    private OperationTypeDatabaseAdapter operationTypeDatabaseAdapter;

    @Mock
    private TransactionHandler mockHandler;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Map<String, OperationType> operationTypesMap;
    private OperationType cashPurchaseOperationType;
    private OperationType paymentOperationType;
    private OperationType withdrawalOperationType;
    private Transaction testTransaction;
    private AccountNumber testAccountNumber;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 12, 26, 10, 0);

        // Setup operation types
        cashPurchaseOperationType = new OperationType(1L, "Cash Purchase", "CASH_PURCHASE_TRANSACTION", TransactionType.DEBIT);
        paymentOperationType = new OperationType(2L, "Payment", "PAYMENT", TransactionType.CREDIT);
        withdrawalOperationType = new OperationType(3L, "Withdrawal", "WITHDRAWAL", TransactionType.DEBIT);

        operationTypesMap = new HashMap<>();
        operationTypesMap.put("CASH_PURCHASE_TRANSACTION", cashPurchaseOperationType);
        operationTypesMap.put("PAYMENT", paymentOperationType);
        operationTypesMap.put("WITHDRAWAL", withdrawalOperationType);

        testAccountNumber = AccountNumber.builder()
                .id(1001L)
                .build();

        testTransaction = Transaction.builder()
                .transactionId(100L)
                .account(testAccountNumber)
                .operationType(cashPurchaseOperationType)
                .amount(new BigDecimal("123.45"))
                .eventDate(testDateTime)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();
    }


    @Test
    @DisplayName("Should successfully perform transaction with valid inputs")
    void shouldPerformTransactionSuccessfully() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("123.45");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));


        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getTransactionId()).isEqualTo(100L);
                    assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
                    assertThat(transaction.getAccount().getId()).isEqualTo(1001L);
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
        verify(transactionFactory, times(1)).getHandlerReactive("CASH_PURCHASE_TRANSACTION");
        verify(mockHandler, times(1)).performTransaction(accountId, amount);
    }

    @Test
    @DisplayName("Should successfully perform payment transaction")
    void shouldPerformPaymentTransaction() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 2L;
        BigDecimal amount = new BigDecimal("50.00");

        Transaction paymentTransaction = Transaction.builder()
                .transactionId(200L)
                .account(testAccountNumber)
                .operationType(paymentOperationType)
                .amount(amount)
                .eventDate(testDateTime)
                .active(true)
                .build();

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("PAYMENT")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(paymentTransaction));


        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getOperationType().getOperationTypeId()).isEqualTo(2L);
                    assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
                })
                .verifyComplete();

        verify(transactionFactory, times(1)).getHandlerReactive("PAYMENT");
    }

    @Test
    @DisplayName("Should successfully perform withdrawal transaction")
    void shouldPerformWithdrawalTransaction() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 3L;
        BigDecimal amount = new BigDecimal("100.00");

        Transaction withdrawalTransaction = Transaction.builder()
                .transactionId(300L)
                .account(testAccountNumber)
                .operationType(withdrawalOperationType)
                .amount(amount)
                .eventDate(testDateTime)
                .active(true)
                .build();

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("WITHDRAWAL")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(withdrawalTransaction));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> {
                    assertThat(transaction).isNotNull();
                    assertThat(transaction.getOperationType().getOperationTypeId()).isEqualTo(3L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle zero amount transaction")
    void shouldHandleZeroAmountTransaction() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = BigDecimal.ZERO;

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));

        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> assertThat(transaction).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle very large amount")
    void shouldHandleVeryLargeAmount() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("999999999999.99");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));

        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> assertThat(transaction).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle small decimal amount")
    void shouldHandleSmallDecimalAmount() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("0.01");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> assertThat(transaction).isNotNull())
                .verifyComplete();
    }


    @Test
    @DisplayName("Should fail when operation type ID does not exist")
    void shouldFailWhenOperationTypeIdDoesNotExist() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 999L; // Non-existent
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));


        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationTypeException &&
                        throwable.getMessage().contains("Invalid Operation Type ID: 999")
                )
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
        verify(transactionFactory, never()).getHandlerReactive(anyString());
        verify(mockHandler, never()).performTransaction(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should fail when operation type ID is null")
    void shouldFailWhenOperationTypeIdIsNull() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = null;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));

        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationTypeException &&
                        throwable.getMessage().contains("Invalid Operation Type ID: null")
                )
                .verify();
    }

    @Test
    @DisplayName("Should fail when operation type ID is zero")
    void shouldFailWhenOperationTypeIdIsZero() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 0L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));

        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationTypeException &&
                        throwable.getMessage().contains("Invalid Operation Type ID: 0")
                )
                .verify();
    }

    @Test
    @DisplayName("Should fail when operation type ID is negative")
    void shouldFailWhenOperationTypeIdIsNegative() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = -1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationTypeException &&
                        throwable.getMessage().contains("Invalid Operation Type ID: -1")
                )
                .verify();
    }

    @Test
    @DisplayName("Should fail when operation types map is empty")
    void shouldFailWhenOperationTypesMapIsEmpty() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(new HashMap<>()));

        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationTypeException &&
                        throwable.getMessage().contains("Invalid Operation Type ID")
                )
                .verify();
    }


    @Test
    @DisplayName("Should handle database adapter error")
    void shouldHandleDatabaseAdapterError() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes())
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database connection failed")
                )
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
        verify(transactionFactory, never()).getHandlerReactive(anyString());
    }

    @Test
    @DisplayName("Should handle database adapter returning empty")
    void shouldHandleDatabaseAdapterReturningEmpty() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.empty());


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectComplete()
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }


    @Test
    @DisplayName("Should handle transaction factory error")
    void shouldHandleTransactionFactoryError() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION"))
                .thenReturn(Mono.error(new InvalidOperationTypeException("Handler not found")));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationTypeException &&
                        throwable.getMessage().equals("Handler not found")
                )
                .verify();

        verify(transactionFactory, times(1)).getHandlerReactive("CASH_PURCHASE_TRANSACTION");
        verify(mockHandler, never()).performTransaction(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should handle transaction factory returning empty")
    void shouldHandleTransactionFactoryReturningEmpty() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.empty());


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectComplete()
                .verify();
    }


    @Test
    @DisplayName("Should handle transaction handler error")
    void shouldHandleTransactionHandlerError() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount))
                .thenReturn(Mono.error(new RuntimeException("Insufficient balance")));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Insufficient balance")
                )
                .verify();

        verify(mockHandler, times(1)).performTransaction(accountId, amount);
    }

    @Test
    @DisplayName("Should handle transaction handler returning empty")
    void shouldHandleTransactionHandlerReturningEmpty() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.empty());


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectComplete()
                .verify();
    }


    @Test
    @DisplayName("Should handle Long.MAX_VALUE account ID")
    void shouldHandleMaxValueAccountId() {
        // Given
        Long accountId = Long.MAX_VALUE;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> assertThat(transaction).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle Long.MAX_VALUE operation type ID")
    void shouldHandleMaxValueOperationTypeId() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = Long.MAX_VALUE;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));

        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .expectErrorMatches(InvalidOperationTypeException.class::isInstance
                )
                .verify();
    }


    @Test
    @DisplayName("Should verify correct method interaction order")
    void shouldVerifyCorrectMethodInteractionOrder() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));

        // When
        transactionService.performTransaction(accountId, operationTypeId, amount).block();

        // Then
        var inOrder = inOrder(operationTypeDatabaseAdapter, transactionFactory, mockHandler);
        inOrder.verify(operationTypeDatabaseAdapter).getOperationTypes();
        inOrder.verify(transactionFactory).getHandlerReactive("CASH_PURCHASE_TRANSACTION");
        inOrder.verify(mockHandler).performTransaction(accountId, amount);
    }


    @Test
    @DisplayName("Should handle concurrent transaction requests")
    void shouldHandleConcurrentTransactionRequests() {
        // Given
        Long accountId1 = 1001L;
        Long accountId2 = 1002L;
        Long operationTypeId = 1L;
        BigDecimal amount1 = new BigDecimal("100.00");
        BigDecimal amount2 = new BigDecimal("200.00");

        Transaction transaction1 = Transaction.builder()
                .transactionId(100L)
                .account(testAccountNumber)
                .amount(amount1)
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId(200L)
                .account(testAccountNumber)
                .amount(amount2)
                .build();

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId1, amount1)).thenReturn(Mono.just(transaction1));
        when(mockHandler.performTransaction(accountId2, amount2)).thenReturn(Mono.just(transaction2));

        // When
        Mono<Transaction> result1 = transactionService.performTransaction(accountId1, operationTypeId, amount1);
        Mono<Transaction> result2 = transactionService.performTransaction(accountId2, operationTypeId, amount2);

        // Then
        StepVerifier.create(Mono.zip(result1, result2))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isNotNull();
                    assertThat(tuple.getT2()).isNotNull();
                    assertThat(tuple.getT1().getTransactionId()).isEqualTo(100L);
                    assertThat(tuple.getT2().getTransactionId()).isEqualTo(200L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle different operation types concurrently")
    void shouldHandleDifferentOperationTypesConcurrently() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("100.00");

        TransactionHandler paymentHandler = mock(TransactionHandler.class);
        TransactionHandler withdrawalHandler = mock(TransactionHandler.class);

        Transaction paymentTx = Transaction.builder().transactionId(200L).build();
        Transaction withdrawalTx = Transaction.builder().transactionId(300L).build();

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("PAYMENT")).thenReturn(Mono.just(paymentHandler));
        when(transactionFactory.getHandlerReactive("WITHDRAWAL")).thenReturn(Mono.just(withdrawalHandler));
        when(paymentHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(paymentTx));
        when(withdrawalHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(withdrawalTx));

        // When
        Mono<Transaction> result1 = transactionService.performTransaction(accountId, 2L, amount);
        Mono<Transaction> result2 = transactionService.performTransaction(accountId, 3L, amount);

        // Then
        StepVerifier.create(Mono.zip(result1, result2))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1().getTransactionId()).isEqualTo(200L);
                    assertThat(tuple.getT2().getTransactionId()).isEqualTo(300L);
                })
                .verifyComplete();
    }


    @Test
    @DisplayName("Should handle null account ID")
    void shouldHandleNullAccountId() {
        // Given
        Long accountId = null;
        Long operationTypeId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));

        // When - Service doesn't validate account ID, delegates to handler
        Mono<Transaction> result = transactionService.performTransaction(accountId, operationTypeId, amount);

        // Then
        StepVerifier.create(result)
                .assertNext(transaction -> assertThat(transaction).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null amount")
    void shouldHandleNullAmount() {
        // Given
        Long accountId = 1001L;
        Long operationTypeId = 1L;
        BigDecimal amount = null;

        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));
        when(transactionFactory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")).thenReturn(Mono.just(mockHandler));
        when(mockHandler.performTransaction(accountId, amount)).thenReturn(Mono.just(testTransaction));


        // Then
        StepVerifier.create(transactionService.performTransaction(accountId, operationTypeId, amount))
                .assertNext(transaction -> assertThat(transaction).isNotNull())
                .verifyComplete();
    }
}

