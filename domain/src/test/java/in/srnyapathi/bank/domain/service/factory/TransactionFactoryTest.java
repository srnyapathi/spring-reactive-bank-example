package in.srnyapathi.bank.domain.service.factory;

import in.srnyapathi.bank.domain.exception.InvalidOperationTypeException;
import in.srnyapathi.bank.domain.service.handlers.CashPurchaseTransactionHandler;
import in.srnyapathi.bank.domain.service.handlers.InstallmentPurchaseTransactionHandler;
import in.srnyapathi.bank.domain.service.handlers.PaymentTransactionHandler;
import in.srnyapathi.bank.domain.service.handlers.WithdrawalTransactionHandler;
import in.srnyapathi.bank.domain.service.impl.TransactionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static in.srnyapathi.bank.domain.model.SupportedTransactionEnum.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionFactory.
 * Tests the factory's ability to return the correct handler for each operation type.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionFactory Tests")
class TransactionFactoryTest {

    @Mock
    private CashPurchaseTransactionHandler cashPurchaseTransactionHandler;

    @Mock
    private InstallmentPurchaseTransactionHandler installmentPurchaseTransactionHandler;

    @Mock
    private PaymentTransactionHandler paymentTransactionHandler;

    @Mock
    private WithdrawalTransactionHandler withdrawalTransactionHandler;

    @InjectMocks
    private TransactionFactory transactionFactory;



    @Test
    @DisplayName("Should return CashPurchaseTransactionHandler for CASH_PURCHASE_TRANSACTION")
    void testGetHandlerReactiveForCashPurchase() {
        StepVerifier.create(transactionFactory.getHandlerReactive(CASH_PURCHASE_TRANSACTION.toString()))
                .expectNext(cashPurchaseTransactionHandler)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return InstallmentPurchaseTransactionHandler for INSTALLMENT_PURCHASE")
    void testGetHandlerReactiveForInstallmentPurchase() {
        StepVerifier.create(transactionFactory.getHandlerReactive(INSTALLMENT_PURCHASE.toString()))
                .expectNext(installmentPurchaseTransactionHandler)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return WithdrawalTransactionHandler for WITHDRAWAL")
    void testGetHandlerReactiveForWithdrawal() {
        StepVerifier.create(transactionFactory.getHandlerReactive(WITHDRAWAL.toString()))
                .expectNext(withdrawalTransactionHandler)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return PaymentTransactionHandler for PAYMENT")
    void testGetHandlerReactiveForPayment() {
        StepVerifier.create(transactionFactory.getHandlerReactive(PAYMENT.toString()))
                .expectNext(paymentTransactionHandler)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should emit error when invalid operation type is provided")
    void testGetHandlerReactiveWithInvalidOperationType() {
        StepVerifier.create(transactionFactory.getHandlerReactive("INVALID_OPERATION_TYPE"))
                .expectError(InvalidOperationTypeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should emit error when null operation type is provided")
    void testGetHandlerReactiveWithNullOperationType() {
        StepVerifier.create(transactionFactory.getHandlerReactive(null))
                .expectError(InvalidOperationTypeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should emit error when empty string is provided")
    void testGetHandlerReactiveWithEmptyString() {
        StepVerifier.create(transactionFactory.getHandlerReactive(""))
                .expectError(InvalidOperationTypeException.class)
                .verify();
    }


    @Test
    @DisplayName("Should handle multiple concurrent requests correctly")
    void testGetHandlerReactiveConcurrency() {
        Mono<TransactionHandler> handler1 = transactionFactory.getHandlerReactive(CASH_PURCHASE_TRANSACTION.toString());
        Mono<TransactionHandler> handler2 = transactionFactory.getHandlerReactive(PAYMENT.toString());
        Mono<TransactionHandler> handler3 = transactionFactory.getHandlerReactive(WITHDRAWAL.toString());

        StepVerifier.create(Mono.zip(handler1, handler2, handler3))
                .assertNext(tuple -> {
                    assertSame(cashPurchaseTransactionHandler, tuple.getT1());
                    assertSame(paymentTransactionHandler, tuple.getT2());
                    assertSame(withdrawalTransactionHandler, tuple.getT3());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should properly chain with other reactive operations")
    void testGetHandlerReactiveChaining() {
        StepVerifier.create(
                Mono.just(CASH_PURCHASE_TRANSACTION.toString())
                        .flatMap(transactionFactory::getHandlerReactive)
                        .map(handler -> handler.getClass().getSimpleName())
        )
                .expectNext("CashPurchaseTransactionHandler")
                .verifyComplete();
    }

}

