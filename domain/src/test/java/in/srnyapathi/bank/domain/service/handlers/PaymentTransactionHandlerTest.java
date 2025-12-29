package in.srnyapathi.bank.domain.service.handlers;

import in.srnyapathi.bank.domain.adapters.TransactionDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.domain.service.OperationTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Map;


@ExtendWith(MockitoExtension.class)
class PaymentTransactionHandlerTest {

    @Mock
    private TransactionDatabaseAdapter transactionDatabaseAdapter;

    @Mock
    private OperationTypeService operationTypeService;

    @InjectMocks
    private PaymentTransactionHandler paymentTransactionHandler;


    OperationType operationType = new OperationType(3L,
            "INSTALLMENT PAYMENT", "PAYMENT", TransactionType.CREDIT);

    private Map<String, OperationType> getMap(OperationType operationType) {
        return Map.of(operationType.getHandler(), operationType);
    }


    @Test
    @DisplayName("Should throw InvalidAmountException when amount is negative")
    void shouldThrowInvalidAmountException_whenAmountIsNegative() {
        Long accountId = 1L;
        float amount = -1;

        StepVerifier.create(paymentTransactionHandler.validate(accountId, BigDecimal.valueOf(amount)))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully validate when amount is positive")
    void shouldValidateSuccessfully_whenAmountIsPositive() {
        Long accountId = 1L;
        var amount = 100.0;

        StepVerifier.create(paymentTransactionHandler.validate(accountId, BigDecimal.valueOf(amount)))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw InvalidAmountException when validating negative amount")
    void shouldThrowInvalidAmountException_whenValidatingNegativeAmount() {
        Long accountId = 1L;
        var amount = -100.0;

        StepVerifier.create(paymentTransactionHandler.validate(accountId, BigDecimal.valueOf(amount)))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @ParameterizedTest(name = "Should successfully process payment with amount: {0}")
    @CsvSource({
            "100.0",
            "0.01",
            "999999999.99",
            "123.456789"
    })
    @DisplayName("Should successfully perform payment transaction with various amounts")
    void shouldPerformPaymentTransaction_withVariousAmounts(String amountValue) {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal(amountValue);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    return Mono.just(Transaction.builder()
                            .transactionId(1L)
                            .account(transaction.getAccount())
                            .amount(transaction.getAmount())
                            .operationType(transaction.getOperationType())
                            .build());
                });

        StepVerifier.create(paymentTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp ->
                        resp.getTransactionId().equals(1L) &&
                        resp.getAccount().getId() == 1 &&
                        resp.getAmount().compareTo(amount) == 0 &&
                        resp.getOperationType().equals(operationType)
                ).verifyComplete();
    }

    @Test
    @DisplayName("Should throw InvalidAmountException when amount is zero")
    void shouldThrowInvalidAmountException_whenAmountIsZero() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.ZERO;

        StepVerifier.create(paymentTransactionHandler.validate(accountId, amount))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidAmountException when amount is null")
    void shouldThrowInvalidAmountException_whenAmountIsNull() {
        Long accountId = 1L;

        StepVerifier.create(paymentTransactionHandler.validate(accountId, null))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidAccountException when account ID is null")
    void shouldThrowInvalidAccountException_whenAccountIdIsNull() {
        BigDecimal amount = BigDecimal.valueOf(100.0);

        StepVerifier.create(paymentTransactionHandler.validate(null, amount))
                .expectError(in.srnyapathi.bank.domain.exception.InvalidAccountException.class)
                .verify();
    }


    @Test
    @DisplayName("Should propagate error when database save fails")
    void shouldPropagateError_whenDatabaseSaveFails() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        StepVerifier.create(paymentTransactionHandler.performTransaction(accountId, amount))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidOperationTypeException when operation type service returns empty map")
    void shouldThrowInvalidOperationTypeException_whenOperationTypeServiceReturnsEmptyMap() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(Map.of()));

        StepVerifier.create(paymentTransactionHandler.performTransaction(accountId, amount))
                .expectError(in.srnyapathi.bank.domain.exception.InvalidOperationTypeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw error when operation type service fails")
    void shouldThrowError_whenOperationTypeServiceFails() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        StepVerifier.create(paymentTransactionHandler.performTransaction(accountId, amount))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully validate account ID of zero")
    void shouldValidateSuccessfully_whenAccountIdIsZero() {
        Long accountId = 0L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        StepVerifier.create(paymentTransactionHandler.validate(accountId, amount))
                .verifyComplete();
    }


    @Test
    @DisplayName("Should verify transaction amount is positive for credit operation")
    void shouldKeepAmountPositive_whenTransactionTypeIsCredit() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    return Mono.just(Transaction.builder()
                            .transactionId(1L)
                            .account(transaction.getAccount())
                            .amount(transaction.getAmount())
                            .operationType(transaction.getOperationType())
                            .build());
                });

        StepVerifier.create(paymentTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp -> {
                    // Verify amount is positive for credit transactions
                    return resp.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
                            resp.getAmount().compareTo(amount) == 0;
                }).verifyComplete();
    }

    @Test
    @DisplayName("Should verify handler returns correct handler name")
    void shouldReturnCorrectHandlerName() {
        String handlerName = paymentTransactionHandler.getHandler();

        org.junit.jupiter.api.Assertions.assertEquals("PAYMENT", handlerName);
    }

    @Test
    @DisplayName("Should verify event date is set to current time")
    void shouldSetEventDateToCurrentTime() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    // Verify the transaction has an event date
                    org.junit.jupiter.api.Assertions.assertNotNull(transaction.getEventDate());
                    return Mono.just(Transaction.builder()
                            .transactionId(1L)
                            .account(transaction.getAccount())
                            .amount(transaction.getAmount())
                            .operationType(transaction.getOperationType())
                            .eventDate(transaction.getEventDate())
                            .build());
                });

        StepVerifier.create(paymentTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp -> resp.getEventDate() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should verify transaction is marked as active")
    void shouldMarkTransactionAsActive() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    // Verify the transaction is marked as active
                    org.junit.jupiter.api.Assertions.assertTrue(transaction.isActive());
                    return Mono.just(Transaction.builder()
                            .transactionId(1L)
                            .account(transaction.getAccount())
                            .amount(transaction.getAmount())
                            .operationType(transaction.getOperationType())
                            .active(transaction.isActive())
                            .build());
                });

        StepVerifier.create(paymentTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(Transaction::isActive)
                .verifyComplete();
    }


}

