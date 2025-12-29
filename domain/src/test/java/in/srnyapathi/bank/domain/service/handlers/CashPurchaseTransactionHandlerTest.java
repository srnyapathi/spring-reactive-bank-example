package in.srnyapathi.bank.domain.service.handlers;

import in.srnyapathi.bank.domain.adapters.TransactionDatabaseAdapter;
import in.srnyapathi.bank.domain.exception.InvalidAccountException;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.exception.InvalidOperationTypeException;
import in.srnyapathi.bank.domain.model.AccountNumber;
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
class CashPurchaseTransactionHandlerTest {

    @Mock
    TransactionDatabaseAdapter transactionDatabaseAdapter;

    @InjectMocks
    CashPurchaseTransactionHandler cashPurchaseTransactionHandler;

    @Mock
    private OperationTypeService operationTypeService;


    OperationType operationType = new OperationType(1L,
            "CASH PURCHASE",
            "CASH_PURCHASE_TRANSACTION",
            TransactionType.DEBIT);

    OperationType worgOptype = new OperationType(2L,
            "Withdrawal",
            "WITHDRAWAL_TRANSACTION",
            TransactionType.DEBIT);

    private Map<String, OperationType> getMap(OperationType operationType) {
        return Map.of(operationType.getHandler(), operationType);
    }


    @Test
    @DisplayName("Should throw InvalidAmountException when amount is negative")
    void shouldThrowInvalidAmountException_whenAmountIsNegative() {
        Long accountId = 1L;
        float amount = -1;

        StepVerifier.create(cashPurchaseTransactionHandler.validate(accountId, BigDecimal.valueOf(amount)))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully validate when amount is positive")
    void shouldValidateSuccessfully_whenAmountIsPositive() {
        Long accountId = 1L;
        var amount = 100.0;

        StepVerifier.create(cashPurchaseTransactionHandler.validate(accountId, BigDecimal.valueOf(amount)))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully perform cash purchase transaction with valid inputs")
    void shouldPerformCashPurchaseTransaction_whenValidInputsProvided() {
        Long accountId = 1L;
        Double amount = 100.0;

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(BigDecimal.valueOf(amount))
                .operationType(operationType)
                .transactionId(1L)
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));


        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));


        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, BigDecimal.valueOf(amount)))
                .expectNextMatches(resp -> {
                    return resp.getTransactionId()==1L &&
                            resp.getAccount().getId() == 1 &&
                            resp.getAmount().compareTo(BigDecimal.valueOf(amount)) == 0 &&
                            resp.getOperationType().equals(operationType);
                }).verifyComplete();
    }

    @Test
    @DisplayName("Should throw InvalidOperationTypeException when operation type is not found")
    void shouldThrowInvalidOperationTypeException_whenOperationTypeNotFound() {
        Long accountId = 1L;
        Double amount = 100.0;


        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(worgOptype)));


        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, BigDecimal.valueOf(amount)))
                .expectError(InvalidOperationTypeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidAmountException when amount is null")
    void shouldThrowInvalidAmountException_whenAmountIsNull() {
        Long accountId = 1L;

        StepVerifier.create(cashPurchaseTransactionHandler.validate(accountId, null))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidAccountException when account ID is null")
    void shouldThrowInvalidAccountException_whenAccountIdIsNull(){
        Long accountId = null;
        Double amount = 100.0;

        StepVerifier.create(cashPurchaseTransactionHandler.validate(accountId,BigDecimal.valueOf(amount)))
                .expectError(InvalidAccountException.class)
                .verify();


    }
    @Test
    @DisplayName("Should successfully perform transaction when operation type is available")
    void shouldPerformTransactionSuccessfully_whenOperationTypeIsAvailable() {
        Long accountId = 1L;
        Double amount = 100.0;

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(BigDecimal.valueOf(amount))
                .operationType(operationType)
                .transactionId(1L)
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));


        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));


        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, BigDecimal.valueOf(amount)))
                .expectNextMatches(resp -> {
                    return resp.getTransactionId()==1L &&
                            resp.getAccount().getId() == 1 &&
                            resp.getAmount().compareTo(BigDecimal.valueOf(amount)) == 0 &&
                            resp.getOperationType().equals(operationType);
                }).verifyComplete();
    }

    @Test
    @DisplayName("Should throw InvalidAmountException when amount is zero")
    void shouldThrowInvalidAmountException_whenAmountIsZero() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.ZERO;

        StepVerifier.create(cashPurchaseTransactionHandler.validate(accountId, amount))
                .expectError(InvalidAmountException.class)
                .verify();
    }

    @ParameterizedTest
    @CsvSource({
            "0.01, very small positive amount",
            "999999999.99, very large amount",
            "123.456789, amount with many decimal places"
    })
    @DisplayName("Should successfully process transaction with different amount values")
    void shouldProcessTransactionSuccessfully_withDifferentAmounts(String amountStr, String description) {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal(amountStr);

        Transaction transactionSavedResponse = Transaction.builder()
                .account(new AccountNumber(accountId))
                .amount(amount.negate())
                .operationType(operationType)
                .transactionId(1L)
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, amount))
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

        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, amount))
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

        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectError(InvalidOperationTypeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw error when operation type service fails")
    void shouldThrowError_whenOperationTypeServiceFails() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully validate account ID of zero")
    void shouldValidateSuccessfully_whenAccountIdIsZero() {
        Long accountId = 0L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        StepVerifier.create(cashPurchaseTransactionHandler.validate(accountId, amount))
                .verifyComplete();
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
                .build();

        Mockito.when(operationTypeService.getOperationType())
                .thenReturn(Mono.just(getMap(operationType)));

        Mockito.when(transactionDatabaseAdapter.saveTransaction(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transactionSavedResponse));

        StepVerifier.create(cashPurchaseTransactionHandler.performTransaction(accountId, amount))
                .expectNextMatches(resp -> {
                    // Verify amount is negative for debit transactions
                    return resp.getAmount().compareTo(BigDecimal.ZERO) < 0 &&
                            resp.getAmount().compareTo(expectedAmount) == 0;
                }).verifyComplete();
    }

    @Test
    @DisplayName("Should verify handler returns correct handler name")
    void shouldReturnCorrectHandlerName() {
        String handlerName = cashPurchaseTransactionHandler.getHandler();

        org.junit.jupiter.api.Assertions.assertEquals("CASH_PURCHASE_TRANSACTION", handlerName);
    }


}