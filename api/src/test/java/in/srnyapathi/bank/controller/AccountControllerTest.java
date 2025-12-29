package in.srnyapathi.bank.controller;

import in.srnyapathi.bank.domain.model.Account;
import in.srnyapathi.bank.domain.service.AccountService;
import in.srnyapathi.bank.mapper.AccountRequestResponseMapper;
import in.srnyapathi.bank.mapper.AccountRequestResponseMapperImpl;
import in.srnyapathi.bank.model.AccountCreationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AccountController.class)
@ContextConfiguration(classes = {AccountController.class, AccountRequestResponseMapperImpl.class})
class AccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountService accountService;

    @Spy
    private AccountRequestResponseMapper mapper;

    @Test
    @DisplayName("Should create account successfully with valid request")
    void shouldCreateAccountSuccessfully() {
        // Given
        AccountCreationRequest request = AccountCreationRequest.builder()
                .documentNumber("12345678900")
                .build();

        Account account = Account.builder()
                .documentNumber("12345678900")
                .accountNumber(1001L)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        when(accountService.createAccount(any(Account.class))).thenReturn(Mono.just(account));

        // When & Then
        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.account_id").isEqualTo(1001L)
                .jsonPath("$.document_number").isEqualTo("12345678900");
    }

    @Test
    @DisplayName("Should return 400 when document number is blank")
    void shouldReturnBadRequestWhenDocumentNumberIsBlank() {
        // Given
        AccountCreationRequest request = AccountCreationRequest.builder()
                .documentNumber("")
                .build();

        // When & Then
        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return 400 when document number is null")
    void shouldReturnBadRequestWhenDocumentNumberIsNull() {
        // Given
        AccountCreationRequest request = AccountCreationRequest.builder()
                .documentNumber(null)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/accounts")
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
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should get account successfully by id")
    void shouldGetAccountSuccessfully() {
        // Given
        Long accountId = 1001L;

        Account account = Account.builder()
                .documentNumber("12345678900")
                .accountNumber(accountId)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();



        when(accountService.getAccount(any(Mono.class))).thenReturn(Mono.just(account));

        // When & Then
        webTestClient.get()
                .uri("/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.account_id").isEqualTo(accountId)
                .jsonPath("$.document_number").isEqualTo("12345678900");
    }

    @Test
    @DisplayName("Should handle account service error when creating account")
    void shouldHandleAccountServiceErrorWhenCreating() {
        // Given
        AccountCreationRequest request = AccountCreationRequest.builder()
                .documentNumber("12345678900")
                .build();



        when(accountService.createAccount(any(Account.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Should handle account not found when getting account")
    void shouldHandleAccountNotFound() {
        // Given
        Long accountId = 999L;

        when(accountService.getAccount(any(Mono.class)))
                .thenReturn(Mono.error(new RuntimeException("Account not found")));

        // When & Then
        webTestClient.get()
                .uri("/accounts/{id}", accountId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Should handle invalid path variable format")
    void shouldHandleInvalidPathVariable() {
        // When & Then
        webTestClient.get()
                .uri("/accounts/invalid")
                .exchange()
                .expectStatus().isBadRequest();
    }
}