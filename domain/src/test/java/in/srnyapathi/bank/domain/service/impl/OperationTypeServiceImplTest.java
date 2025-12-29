package in.srnyapathi.bank.domain.service.impl;

import in.srnyapathi.bank.domain.adapters.OperationTypeDatabaseAdapter;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperationTypeServiceImpl Tests")
class OperationTypeServiceImplTest {

    @Mock
    private OperationTypeDatabaseAdapter operationTypeDatabaseAdapter;

    @InjectMocks
    private OperationTypeServiceImpl operationTypeService;

    private Map<String, OperationType> operationTypesMap;
    private OperationType cashPurchaseOperationType;
    private OperationType paymentOperationType;
    private OperationType installmentPurchaseOperationType;
    private OperationType withdrawalOperationType;

    @BeforeEach
    void setUp() {
        // Setup operation types with various transaction types
        cashPurchaseOperationType = new OperationType(
                1L,
                "Cash Purchase",
                "CASH_PURCHASE_TRANSACTION",
                TransactionType.DEBIT
        );

        paymentOperationType = new OperationType(
                2L,
                "Payment",
                "PAYMENT",
                TransactionType.CREDIT
        );

        installmentPurchaseOperationType = new OperationType(
                3L,
                "Installment Purchase",
                "INSTALLMENT_PURCHASE",
                TransactionType.DEBIT
        );

        withdrawalOperationType = new OperationType(
                4L,
                "Withdrawal",
                "WITHDRAWAL",
                TransactionType.DEBIT
        );

        operationTypesMap = new HashMap<>();
        operationTypesMap.put("CASH_PURCHASE_TRANSACTION", cashPurchaseOperationType);
        operationTypesMap.put("PAYMENT", paymentOperationType);
        operationTypesMap.put("INSTALLMENT_PURCHASE", installmentPurchaseOperationType);
        operationTypesMap.put("WITHDRAWAL", withdrawalOperationType);
    }

    @Test
    @DisplayName("Should successfully retrieve operation types when database returns valid data")
    void shouldRetrieveOperationTypesSuccessfully() {
        // Given
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(4);
                    assertThat(result).containsKeys(
                            "CASH_PURCHASE_TRANSACTION",
                            "PAYMENT",
                            "INSTALLMENT_PURCHASE",
                            "WITHDRAWAL"
                    );
                    assertThat(result.get("CASH_PURCHASE_TRANSACTION")).isEqualTo(cashPurchaseOperationType);
                    assertThat(result.get("PAYMENT")).isEqualTo(paymentOperationType);
                    assertThat(result.get("INSTALLMENT_PURCHASE")).isEqualTo(installmentPurchaseOperationType);
                    assertThat(result.get("WITHDRAWAL")).isEqualTo(withdrawalOperationType);
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle empty map returned from database")
    void shouldHandleEmptyOperationTypesMap() {
        Map<String, OperationType> emptyMap = Collections.emptyMap();
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(emptyMap));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).isEmpty();
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle single operation type in map")
    void shouldHandleSingleOperationType() {
        Map<String, OperationType> singleTypeMap = new HashMap<>();
        singleTypeMap.put("PAYMENT", paymentOperationType);
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(singleTypeMap));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(1);
                    assertThat(result).containsKey("PAYMENT");
                    assertThat(result.get("PAYMENT")).isEqualTo(paymentOperationType);
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should propagate error when database adapter throws exception")
    void shouldPropagateErrorWhenDatabaseAdapterFails() {
        RuntimeException expectedException = new RuntimeException("Database connection failed");
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.error(expectedException));

        StepVerifier.create(operationTypeService.getOperationType())
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database connection failed")
                )
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle null values in operation type fields")
    void shouldHandleOperationTypeWithNullFields() {
        OperationType operationTypeWithNulls = new OperationType(null, null, "NULL_HANDLER", null);
        Map<String, OperationType> mapWithNulls = new HashMap<>();
        mapWithNulls.put("NULL_HANDLER", operationTypeWithNulls);
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(mapWithNulls));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(1);
                    assertThat(result.get("NULL_HANDLER")).isNotNull();
                    assertThat(result.get("NULL_HANDLER").getOperationTypeId()).isNull();
                    assertThat(result.get("NULL_HANDLER").getDescription()).isNull();
                    assertThat(result.get("NULL_HANDLER").getTransactionType()).isNull();
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle database returning empty mono")
    void shouldHandleDatabaseReturningEmptyMono() {
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.empty());

        StepVerifier.create(operationTypeService.getOperationType())
                .expectComplete()
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle large number of operation types")
    void shouldHandleLargeNumberOfOperationTypes() {
        Map<String, OperationType> largeMap = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            OperationType operationType = new OperationType(
                    (long) i,
                    "Description " + i,
                    "HANDLER_" + i,
                    i % 2 == 0 ? TransactionType.DEBIT : TransactionType.CREDIT
            );
            largeMap.put("HANDLER_" + i, operationType);
        }
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(largeMap));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(1000);
                    assertThat(result).containsKey("HANDLER_0");
                    assertThat(result).containsKey("HANDLER_999");
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle operation types with special characters in handler names")
    void shouldHandleSpecialCharactersInHandlerNames() {
        OperationType specialCharType = new OperationType(
                100L,
                "Special Purchase",
                "SPECIAL-PURCHASE_2024.v1",
                TransactionType.DEBIT
        );
        Map<String, OperationType> specialMap = new HashMap<>();
        specialMap.put("SPECIAL-PURCHASE_2024.v1", specialCharType);
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(specialMap));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(1);
                    assertThat(result).containsKey("SPECIAL-PURCHASE_2024.v1");
                    assertThat(result.get("SPECIAL-PURCHASE_2024.v1")).isEqualTo(specialCharType);
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should maintain reactive chain and not block")
    void shouldMaintainReactiveChain() {
        when(operationTypeDatabaseAdapter.getOperationTypes())
                .thenReturn(Mono.just(operationTypesMap).delayElement(Duration.ofMillis(100)));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(4);
                })
                .expectComplete()
                .verify(Duration.ofSeconds(1));

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle timeout from database adapter")
    void shouldHandleTimeout() {
        when(operationTypeDatabaseAdapter.getOperationTypes())
                .thenReturn(Mono.never()); // Simulates a never-completing operation

        StepVerifier.create(operationTypeService.getOperationType().timeout(Duration.ofMillis(100)))
                .expectError(java.util.concurrent.TimeoutException.class)
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle both DEBIT and CREDIT transaction types")
    void shouldHandleBothTransactionTypes() {
        Map<String, OperationType> mixedTypesMap = new HashMap<>();
        mixedTypesMap.put("CASH_PURCHASE_TRANSACTION", cashPurchaseOperationType); // DEBIT
        mixedTypesMap.put("PAYMENT", paymentOperationType); // CREDIT
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(mixedTypesMap));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(2);
                    assertThat(result.get("CASH_PURCHASE_TRANSACTION").getTransactionType())
                            .isEqualTo(TransactionType.DEBIT);
                    assertThat(result.get("PAYMENT").getTransactionType())
                            .isEqualTo(TransactionType.CREDIT);
                })
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should only call database adapter once per request")
    void shouldCallDatabaseAdapterOnlyOnce() {
        when(operationTypeDatabaseAdapter.getOperationTypes()).thenReturn(Mono.just(operationTypesMap));

        StepVerifier.create(operationTypeService.getOperationType())
                .assertNext(types -> assertThat(types).hasSize(4))
                .verifyComplete();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
        verifyNoMoreInteractions(operationTypeDatabaseAdapter);
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException from database adapter")
    void shouldHandleIllegalArgumentException() {
        when(operationTypeDatabaseAdapter.getOperationTypes())
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid operation type configuration")));

        StepVerifier.create(operationTypeService.getOperationType())
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Invalid operation type configuration")
                )
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }

    @Test
    @DisplayName("Should handle NullPointerException from database adapter")
    void shouldHandleNullPointerException() {
        when(operationTypeDatabaseAdapter.getOperationTypes())
                .thenReturn(Mono.error(new NullPointerException("Operation type map is null")));

        StepVerifier.create(operationTypeService.getOperationType())
                .expectErrorMatches(throwable ->
                        throwable instanceof NullPointerException &&
                        throwable.getMessage().equals("Operation type map is null")
                )
                .verify();

        verify(operationTypeDatabaseAdapter, times(1)).getOperationTypes();
    }
}