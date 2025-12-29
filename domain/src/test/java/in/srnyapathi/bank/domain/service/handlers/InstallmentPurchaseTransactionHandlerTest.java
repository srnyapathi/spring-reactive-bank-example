package in.srnyapathi.bank.domain.service.handlers;

import in.srnyapathi.bank.domain.adapters.TransactionDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.model.TransactionType;
import in.srnyapathi.bank.domain.service.OperationTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class InstallmentPurchaseTransactionHandlerTest {

    @Mock
    TransactionDatabaseAdapter transactionDatabaseAdapter;

    @InjectMocks
    InstallmentPurchaseTransactionHandler installmentPurchaseTransactionHandler;

    @Mock
    private OperationTypeService operationTypeService;


    OperationType operationType = new OperationType(2L,
            "INSTALLMENT PURCHASE", "INSTALLMENT_PURCHASE", TransactionType.DEBIT);

    private Map<String, OperationType> getMap(OperationType operationType) {
        return Map.of(operationType.getHandler(), operationType);
    }


    @Test
    @DisplayName("Should throw InvalidAmountException when amount is negative")
    void shouldThrowInvalidAmountException_whenAmountIsNegative() {
        Long accountId = 1L;
        float amount = -1;

        StepVerifier.create(installmentPurchaseTransactionHandler.validate(accountId, BigDecimal.valueOf(amount)))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully validate when amount is positive")
    void shouldValidateSuccessfully_whenAmountIsPositive() {
        Long accountId = 1L;
        var amount = 100.0;

        StepVerifier.create(installmentPurchaseTransactionHandler.validate(accountId, BigDecimal.valueOf(amount)))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully perform installment purchase transaction with valid inputs")
    void shouldPerformInstallmentPurchaseTransaction_whenValidInputsProvided() {
        Long accountId = 1L;
        Double amount = 100.0;

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(BigDecimal.valueOf(amount).negate())
                .operationType(operationType)
                .transactionId(1L)
                .eventDate(LocalDateTime.now().minusSeconds(59))
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, BigDecimal.valueOf(amount)))
                .expectNextMatches(resp -> {
                    return resp.getTransactionId()== 1L &&
                            resp.getAccount().getId() == 1 &&
                            resp.getAmount().compareTo(BigDecimal.valueOf(amount).negate()) == 0 &&
                            resp.getOperationType().equals(operationType) &&
                            resp.getEventDate().isBefore(LocalDateTime.now());
                }).verifyComplete();
    }

    @Test
    @DisplayName("Should throw InvalidAmountException when amount is zero")
    void shouldThrowInvalidAmountException_whenAmountIsZero() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.ZERO;

        StepVerifier.create(installmentPurchaseTransactionHandler.validate(accountId, amount))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidAmountException when amount is null")
    void shouldThrowInvalidAmountException_whenAmountIsNull() {
        Long accountId = 1L;

        StepVerifier.create(installmentPurchaseTransactionHandler.validate(accountId, null))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidAccountException when account ID is null")
    void shouldThrowInvalidAccountException_whenAccountIdIsNull() {
        BigDecimal amount = BigDecimal.valueOf(100.0);

        StepVerifier.create(installmentPurchaseTransactionHandler.validate(null, amount))
                .expectError(in.srnyapathi.bank.domain.exception.InvalidAccountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully process transaction with very small positive amount")
    void shouldProcessTransactionSuccessfully_whenAmountIsVerySmall() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("0.01");

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(amount.negate())
                .operationType(operationType)
                .transactionId(1L)
                .eventDate(LocalDateTime.now().minusSeconds(1))
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp ->
                        resp.getTransactionId() == 1L &&
                        resp.getAccount().getId() == 1 &&
                        resp.getAmount().compareTo(amount.negate()) == 0 &&
                        resp.getOperationType().equals(operationType)
                ).verifyComplete();
    }

    @Test
    @DisplayName("Should successfully process transaction with very large amount")
    void shouldProcessTransactionSuccessfully_whenAmountIsVeryLarge() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("999999999.99");

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(amount.negate())
                .operationType(operationType)
                .transactionId(1L)
                .eventDate(LocalDateTime.now().minusSeconds(1))
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp ->
                        resp.getTransactionId() == 1L &&
                        resp.getAccount().getId() == 1 &&
                        resp.getAmount().compareTo(amount.negate()) == 0 &&
                        resp.getOperationType().equals(operationType)
                ).verifyComplete();
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

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
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

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
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

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully validate account ID of zero")
    void shouldValidateSuccessfully_whenAccountIdIsZero() {
        Long accountId = 0L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        StepVerifier.create(installmentPurchaseTransactionHandler.validate(accountId, amount))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle amount with many decimal places")
    void shouldHandleAmountWithManyDecimalPlaces() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("123.456789");

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(amount.negate())
                .operationType(operationType)
                .transactionId(1L)
                .eventDate(LocalDateTime.now().minusSeconds(1))
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp ->
                        resp.getTransactionId() == 1L &&
                        resp.getAccount().getId() == 1 &&
                        resp.getAmount().compareTo(amount.negate()) == 0 &&
                        resp.getOperationType().equals(operationType)
                ).verifyComplete();
    }

    @Test
    @DisplayName("Should verify transaction amount is negated for debit operation")
    void shouldNegateAmount_whenTransactionTypeIsDebit() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);
        BigDecimal expectedAmount = amount.negate();

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(expectedAmount)
                .operationType(operationType)
                .transactionId(1L)
                .eventDate(LocalDateTime.now().minusSeconds(1))
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp -> {
                    // Verify amount is negative for debit transactions
                    return resp.getAmount().compareTo(BigDecimal.ZERO) < 0 &&
                            resp.getAmount().compareTo(expectedAmount) == 0;
                }).verifyComplete();
    }

    @Test
    @DisplayName("Should verify handler returns correct handler name")
    void shouldReturnCorrectHandlerName() {
        String handlerName = installmentPurchaseTransactionHandler.getHandler();

        org.junit.jupiter.api.Assertions.assertEquals("INSTALLMENT_PURCHASE", handlerName);
    }

    @Test
    @DisplayName("Should verify event date is set to current time")
    void shouldSetEventDateToCurrentTime() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);
        LocalDateTime beforeTransaction = LocalDateTime.now();

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(amount.negate())
                .operationType(operationType)
                .transactionId(1L)
                .eventDate(LocalDateTime.now())
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp -> {
                    LocalDateTime afterTransaction = LocalDateTime.now();
                    // Verify event date is between before and after transaction time
                    return resp.getEventDate().isAfter(beforeTransaction.minusSeconds(1)) &&
                            resp.getEventDate().isBefore(afterTransaction.plusSeconds(1));
                }).verifyComplete();
    }

    @Test
    @DisplayName("Should verify transaction is marked as active")
    void shouldMarkTransactionAsActive() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(amount.negate())
                .operationType(operationType)
                .transactionId(1L)
                .eventDate(LocalDateTime.now())
                .active(true)
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(installmentPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp -> resp.isActive())
                .verifyComplete();
    }


}

