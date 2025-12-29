package in.srnyapathi.bank.controller;

import in.srnyapathi.bank.domain.exception.AccountDoesNotExist;
import in.srnyapathi.bank.domain.model.AccountNumber;
import in.srnyapathi.bank.domain.model.OperationType;
import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.domain.service.TransactionService;
import in.srnyapathi.bank.exception.ReactiveGlobalExceptionHandler;
import in.srnyapathi.bank.mapper.TransactionRequestResponseMapperImpl;
import in.srnyapathi.bank.model.TransactionCreationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TransactionController.class)
@ContextConfiguration(classes = {TransactionController.class, TransactionRequestResponseMapperImpl.class, ReactiveGlobalExceptionHandler.class, WebProperties.class})
@Import(ReactiveGlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TransactionService transactionService;


    @Test
    @DisplayName("Should create transaction successfully with valid request")
    void shouldCreateTransactionSuccessfully() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                1L,
                new BigDecimal("50.00")
        );

        Transaction transaction = Transaction.builder()
                .transactionId(1L)
                .account(new AccountNumber(1001L))
                .operationType(new OperationType(1L, null, null, null))
                .amount(new BigDecimal("50.00"))
                .eventDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        when(transactionService.performTransaction(eq(1001L), eq(1L), any(BigDecimal.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(1L)
                .jsonPath("$.account_id").isEqualTo(1001L)
                .jsonPath("$.operation_type_id").isEqualTo(1L)
                .jsonPath("$.amount").isEqualTo(50.00)
                .jsonPath("$.event_date").exists();
    }

    @Test
    @DisplayName("Should return 400 when account_id is null")
    void shouldReturnBadRequestWhenAccountIdIsNull() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                null,
                1L,
                new BigDecimal("50.00")
        );

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return 400 when operation_type_id is null")
    void shouldReturnBadRequestWhenOperationTypeIdIsNull() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                null,
                new BigDecimal("50.00")
        );

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return 400 when amount is null")
    void shouldReturnBadRequestWhenAmountIsNull() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                1L,
                null
        );

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return 400 when request body is empty")
    void shouldReturnBadRequestWhenRequestBodyIsEmpty() {
        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should create cash purchase transaction successfully")
    void shouldCreateCashPurchaseTransactionSuccessfully() {
        // Given - Operation Type 1 = Cash Purchase
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                1L,
                new BigDecimal("100.50")
        );

        Transaction transaction = Transaction.builder()
                .transactionId(2L)
                .account(new AccountNumber(1001L))
                .operationType(new OperationType(1L, "Cash Purchase", null, null))
                .amount(new BigDecimal("-100.50"))
                .eventDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        when(transactionService.performTransaction(eq(1001L), eq(1L), any(BigDecimal.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(2L)
                .jsonPath("$.account_id").isEqualTo(1001L)
                .jsonPath("$.operation_type_id").isEqualTo(1L)
                .jsonPath("$.amount").isEqualTo(-100.50);
    }

    @Test
    @DisplayName("Should create installment purchase transaction successfully")
    void shouldCreateInstallmentPurchaseTransactionSuccessfully() {
        // Given - Operation Type 2 = Installment Purchase
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                2L,
                new BigDecimal("200.75")
        );

        Transaction transaction = Transaction.builder()
                .transactionId(3L)
                .account(new AccountNumber(1001L))
                .operationType(new OperationType(2L, "Installment Purchase", null, null))
                .amount(new BigDecimal("-200.75"))
                .eventDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        when(transactionService.performTransaction(eq(1001L), eq(2L), any(BigDecimal.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(3L)
                .jsonPath("$.account_id").isEqualTo(1001L)
                .jsonPath("$.operation_type_id").isEqualTo(2L)
                .jsonPath("$.amount").isEqualTo(-200.75);
    }

    @Test
    @DisplayName("Should create withdrawal transaction successfully")
    void shouldCreateWithdrawalTransactionSuccessfully() {
        // Given - Operation Type 3 = Withdrawal
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                3L,
                new BigDecimal("150.00")
        );

        Transaction transaction = Transaction.builder()
                .transactionId(4L)
                .account(new AccountNumber(1001L))
                .operationType(new OperationType(3L, "Withdrawal", null, null))
                .amount(new BigDecimal("-150.00"))
                .eventDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        when(transactionService.performTransaction(eq(1001L), eq(3L), any(BigDecimal.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(4L)
                .jsonPath("$.account_id").isEqualTo(1001L)
                .jsonPath("$.operation_type_id").isEqualTo(3L)
                .jsonPath("$.amount").isEqualTo(-150.00);
    }

    @Test
    @DisplayName("Should create payment transaction successfully")
    void shouldCreatePaymentTransactionSuccessfully() {
        // Given - Operation Type 4 = Payment
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                4L,
                new BigDecimal("500.00")
        );

        Transaction transaction = Transaction.builder()
                .transactionId(5L)
                .account(new AccountNumber(1001L))
                .operationType(new OperationType(4L, "Payment", null, null))
                .amount(new BigDecimal("500.00"))
                .eventDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        when(transactionService.performTransaction(eq(1001L), eq(4L), any(BigDecimal.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(5L)
                .jsonPath("$.account_id").isEqualTo(1001L)
                .jsonPath("$.operation_type_id").isEqualTo(4L)
                .jsonPath("$.amount").isEqualTo(500.00);
    }

    @Test
    @DisplayName("Should handle transaction service error")
    void shouldHandleTransactionServiceError() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                1L,
                new BigDecimal("50.00")
        );

        when(transactionService.performTransaction(eq(1001L), eq(1L), any(BigDecimal.class)))
                .thenReturn(Mono.error(new RuntimeException("Transaction processing failed")));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Should handle account not found error")
    void shouldHandleAccountNotFoundError() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                9999L,
                1L,
                new BigDecimal("50.00")
        );

        when(transactionService.performTransaction(eq(9999L), eq(1L), any(BigDecimal.class)))
                .thenReturn(Mono.error(new AccountDoesNotExist("Account not found")));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Account not found");
    }

    @Test
    @DisplayName("Should handle invalid operation type error")
    void shouldHandleInvalidOperationTypeError() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                99L,
                new BigDecimal("50.00")
        );

        when(transactionService.performTransaction(eq(1001L), eq(99L), any(BigDecimal.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid operation type")));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Should handle malformed JSON request")
    void shouldHandleMalformedJsonRequest() {
        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{invalid json}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should create transaction with large amount")
    void shouldCreateTransactionWithLargeAmount() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                4L,
                new BigDecimal("999999.99")
        );

        Transaction transaction = Transaction.builder()
                .transactionId(6L)
                .account(new AccountNumber(1001L))
                .operationType(new OperationType(4L, null, null, null))
                .amount(new BigDecimal("999999.99"))
                .eventDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        when(transactionService.performTransaction(eq(1001L), eq(4L), any(BigDecimal.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(6L)
                .jsonPath("$.amount").isEqualTo(999999.99);
    }

    @Test
    @DisplayName("Should create transaction with decimal precision")
    void shouldCreateTransactionWithDecimalPrecision() {
        // Given
        TransactionCreationRequest request = new TransactionCreationRequest(
                1001L,
                1L,
                new BigDecimal("123.45")
        );

        Transaction transaction = Transaction.builder()
                .transactionId(7L)
                .account(new AccountNumber(1001L))
                .operationType(new OperationType(1L, null, null, null))
                .amount(new BigDecimal("-123.45"))
                .eventDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        when(transactionService.performTransaction(eq(1001L), eq(1L), any(BigDecimal.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(7L)
                .jsonPath("$.amount").isEqualTo(-123.45);
    }
}