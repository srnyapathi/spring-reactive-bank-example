package in.srnyapathi.bank.persistence.service;

import in.srnyapathi.bank.domain.exception.AccountDoesNotExist;
import in.srnyapathi.bank.domain.exception.InvalidAccountException;
import in.srnyapathi.bank.domain.model.Account;
import in.srnyapathi.bank.persistence.entity.AccountEntity;
import in.srnyapathi.bank.persistence.mapper.AccountMapper;
import in.srnyapathi.bank.persistence.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountDatabaseAdapterService Tests")
class AccountDatabaseAdapterServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountDatabaseAdapterService accountDatabaseAdapterService;

    private Account testAccount;
    private AccountEntity testAccountEntity;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 12, 26, 10, 0);

        testAccount = Account.builder()
                .documentNumber("12345678900")
                .accountNumber(1001L)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        testAccountEntity = new AccountEntity(
                1001L,
                "12345678900",
                testDateTime,
                testDateTime,
                true
        );
    }



    @Test
    @DisplayName("Should successfully create account with valid data")
    void shouldCreateAccountSuccessfully() {
        // Given
        when(accountMapper.toEntity(any(Account.class))).thenReturn(testAccountEntity);
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(Mono.just(testAccountEntity));
        when(accountMapper.toDomain(any(AccountEntity.class))).thenReturn(testAccount);

        // When & Then
        StepVerifier.create(accountDatabaseAdapterService.createAccount(testAccount))
                .assertNext(account -> {
                    assertThat(account).isNotNull();
                    assertThat(account.getAccountNumber()).isEqualTo(1001L);
                    assertThat(account.getDocumentNumber()).isEqualTo("12345678900");
                    assertThat(account.isActive()).isTrue();
                })
                .verifyComplete();

        verify(accountMapper, times(1)).toEntity(testAccount);
        verify(accountRepository, times(1)).save(testAccountEntity);
        verify(accountMapper, times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("Should create account with null ID (new account)")
    void shouldCreateAccountWithNullId() {
        // Given
        Account newAccount = Account.builder()
                .documentNumber("98765432100")
                .accountNumber(null)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        AccountEntity newEntity = new AccountEntity(
                null,
                "98765432100",
                testDateTime,
                testDateTime,
                true
        );

        AccountEntity savedEntity = new AccountEntity(
                2001L,
                "98765432100",
                testDateTime,
                testDateTime,
                true
        );

        Account savedAccount = Account.builder()
                .documentNumber("98765432100")
                .accountNumber(2001L)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        when(accountMapper.toEntity(newAccount)).thenReturn(newEntity);
        when(accountRepository.save(newEntity)).thenReturn(Mono.just(savedEntity));
        when(accountMapper.toDomain(savedEntity)).thenReturn(savedAccount);


        StepVerifier.create(accountDatabaseAdapterService.createAccount(newAccount))
                .assertNext(account -> {
                    assertThat(account).isNotNull();
                    assertThat(account.getAccountNumber()).isEqualTo(2001L);
                    assertThat(account.getDocumentNumber()).isEqualTo("98765432100");
                })
                .verifyComplete();

        verify(accountMapper, times(1)).toEntity(newAccount);
        verify(accountRepository, times(1)).save(newEntity);
    }

    @Test
    @DisplayName("Should handle repository error during account creation")
    void shouldHandleRepositoryErrorDuringCreation() {
        when(accountMapper.toEntity(any(Account.class))).thenReturn(testAccountEntity);
        when(accountRepository.save(any(AccountEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));


        StepVerifier.create(accountDatabaseAdapterService.createAccount(testAccount))
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Database connection failed")
                )
                .verify();

        verify(accountMapper, times(1)).toEntity(testAccount);
        verify(accountRepository, times(1)).save(testAccountEntity);
        verify(accountMapper, never()).toDomain(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should handle mapper error during entity conversion")
    void shouldHandleMapperErrorDuringEntityConversion() {
        // Given
        when(accountMapper.toEntity(any(Account.class)))
                .thenThrow(new RuntimeException("Mapping error"));

        // When
        Mono<Account> result = accountDatabaseAdapterService.createAccount(testAccount);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Mapping error")
                )
                .verify();

        verify(accountMapper, times(1)).toEntity(testAccount);
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should handle null account during creation")
    void shouldHandleNullAccountDuringCreation() {
        // When
        Mono<Account> result = accountDatabaseAdapterService.createAccount(null);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidAccountException &&
                    throwable.getMessage().equals("Account cannot be null")
                )
                .verify();

        verify(accountMapper, never()).toEntity(any(Account.class));
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should create account with minimum required fields")
    void shouldCreateAccountWithMinimumFields() {
        Account minimalAccount = Account.builder()
                .documentNumber("11111111111")
                .active(true)
                .build();

        AccountEntity minimalEntity = new AccountEntity(
                null,
                "11111111111",
                null,
                null,
                true
        );

        AccountEntity savedEntity = new AccountEntity(
                3001L,
                "11111111111",
                testDateTime,
                testDateTime,
                true
        );

        Account savedAccount = Account.builder()
                .accountNumber(3001L)
                .documentNumber("11111111111")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .active(true)
                .build();

        when(accountMapper.toEntity(minimalAccount)).thenReturn(minimalEntity);
        when(accountRepository.save(minimalEntity)).thenReturn(Mono.just(savedEntity));
        when(accountMapper.toDomain(savedEntity)).thenReturn(savedAccount);

        // Then
        StepVerifier.create(accountDatabaseAdapterService.createAccount(minimalAccount))
                .assertNext(account -> {
                    assertThat(account).isNotNull();
                    assertThat(account.getAccountNumber()).isEqualTo(3001L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully retrieve account by account number")
    void shouldGetAccountByAccountNumberSuccessfully() {
        // Given
        when(accountRepository.findActiveAccount(1001L)).thenReturn(Mono.just(testAccountEntity));
        when(accountMapper.toDomain(testAccountEntity)).thenReturn(testAccount);


        // Then
        StepVerifier.create(accountDatabaseAdapterService.getAccountByAccountNumber(1001L))
                .assertNext(account -> {
                    assertThat(account).isNotNull();
                    assertThat(account.getAccountNumber()).isEqualTo(1001L);
                    assertThat(account.getDocumentNumber()).isEqualTo("12345678900");
                    assertThat(account.isActive()).isTrue();
                })
                .verifyComplete();

        verify(accountRepository, times(1)).findActiveAccount(1001L);
        verify(accountMapper, times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("Should throw AccountDoesNotExist when account is not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        // Given
        Long nonExistentAccountId = 9999L;
        when(accountRepository.findActiveAccount(nonExistentAccountId)).thenReturn(Mono.empty());

        // Then
        StepVerifier.create(accountDatabaseAdapterService.getAccountByAccountNumber(nonExistentAccountId))
                .expectErrorMatches(throwable ->
                    throwable instanceof AccountDoesNotExist &&
                    throwable.getMessage().contains("Account not found or inactive: " + nonExistentAccountId)
                )
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(nonExistentAccountId);
        verify(accountMapper, never()).toDomain(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should throw AccountDoesNotExist when account is inactive")
    void shouldThrowExceptionWhenAccountIsInactive() {
        // Given
        Long inactiveAccountId = 1001L;
        when(accountRepository.findActiveAccount(inactiveAccountId)).thenReturn(Mono.empty());


        // Then
        StepVerifier.create(accountDatabaseAdapterService.getAccountByAccountNumber(inactiveAccountId))
                .expectErrorMatches(throwable ->
                    throwable instanceof AccountDoesNotExist &&
                    throwable.getMessage().contains("Account not found or inactive: " + inactiveAccountId)
                )
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(inactiveAccountId);
    }

    @Test
    @DisplayName("Should handle repository error during account retrieval")
    void shouldHandleRepositoryErrorDuringRetrieval() {
        Long accountId = 1001L;
        when(accountRepository.findActiveAccount(accountId))
                .thenReturn(Mono.error(new RuntimeException("Database timeout")));


        StepVerifier.create(accountDatabaseAdapterService.getAccountByAccountNumber(accountId))
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Database timeout")
                )
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(accountId);
        verify(accountMapper, never()).toDomain(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should handle mapper error during domain conversion")
    void shouldHandleMapperErrorDuringDomainConversion() {
        when(accountRepository.findActiveAccount(1001L)).thenReturn(Mono.just(testAccountEntity));
        when(accountMapper.toDomain(testAccountEntity))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When

        // Then
        StepVerifier.create(accountDatabaseAdapterService.getAccountByAccountNumber(1001L))
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Mapping failed")
                )
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(1001L);
        verify(accountMapper, times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("Should handle null account ID")
    void shouldHandleNullAccountId() {
        // Given
        when(accountRepository.findActiveAccount(null)).thenReturn(Mono.empty());

        // When
        Mono<Account> result = accountDatabaseAdapterService.getAccountByAccountNumber(null);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(AccountDoesNotExist.class::isInstance)
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(null);
    }

    @Test
    @DisplayName("Should handle zero account ID")
    void shouldHandleZeroAccountId() {
        // Given
        when(accountRepository.findActiveAccount(0L)).thenReturn(Mono.empty());

        // When
        Mono<Account> result = accountDatabaseAdapterService.getAccountByAccountNumber(0L);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof AccountDoesNotExist &&
                    throwable.getMessage().contains("Account not found or inactive: 0")
                )
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(0L);
    }

    @Test
    @DisplayName("Should handle negative account ID")
    void shouldHandleNegativeAccountId() {
        // Given
        Long negativeId = -1L;
        when(accountRepository.findActiveAccount(negativeId)).thenReturn(Mono.empty());

        // When
        Mono<Account> result = accountDatabaseAdapterService.getAccountByAccountNumber(negativeId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof AccountDoesNotExist &&
                    throwable.getMessage().contains("Account not found or inactive: -1")
                )
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(negativeId);
    }

    @Test
    @DisplayName("Should handle very large account ID")
    void shouldHandleVeryLargeAccountId() {
        // Given
        Long largeId = Long.MAX_VALUE;
        when(accountRepository.findActiveAccount(largeId)).thenReturn(Mono.empty());

        // When
        Mono<Account> result = accountDatabaseAdapterService.getAccountByAccountNumber(largeId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof AccountDoesNotExist &&
                    throwable.getMessage().contains("Account not found or inactive: " + largeId)
                )
                .verify();

        verify(accountRepository, times(1)).findActiveAccount(largeId);
    }

    // ========== INTEGRATION/EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should verify method interactions are in correct order for createAccount")
    void shouldVerifyCreateAccountMethodInteractionOrder() {
        // Given
        when(accountMapper.toEntity(any(Account.class))).thenReturn(testAccountEntity);
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(Mono.just(testAccountEntity));
        when(accountMapper.toDomain(any(AccountEntity.class))).thenReturn(testAccount);

        // When
        accountDatabaseAdapterService.createAccount(testAccount).block();

        // Then
        var inOrder = inOrder(accountMapper, accountRepository);
        inOrder.verify(accountMapper).toEntity(testAccount);
        inOrder.verify(accountRepository).save(testAccountEntity);
        inOrder.verify(accountMapper).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("Should verify method interactions are in correct order for getAccountByAccountNumber")
    void shouldVerifyGetAccountMethodInteractionOrder() {
        // Given
        when(accountRepository.findActiveAccount(1001L)).thenReturn(Mono.just(testAccountEntity));
        when(accountMapper.toDomain(testAccountEntity)).thenReturn(testAccount);

        // When
        accountDatabaseAdapterService.getAccountByAccountNumber(1001L).block();

        // Then
        var inOrder = inOrder(accountRepository, accountMapper);
        inOrder.verify(accountRepository).findActiveAccount(1001L);
        inOrder.verify(accountMapper).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("Should not call mapper.toDomain when repository returns empty")
    void shouldNotCallMapperWhenRepositoryReturnsEmpty() {
        // Given
        when(accountRepository.findActiveAccount(anyLong())).thenReturn(Mono.empty());

        // When
        try {
            accountDatabaseAdapterService.getAccountByAccountNumber(1001L).block();
        } catch (Exception e) {
            // Expected exception
        }

        // Then
        verify(accountMapper, never()).toDomain(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should handle concurrent account creation requests")
    void shouldHandleConcurrentAccountCreation() {
        // Given
        when(accountMapper.toEntity(any(Account.class))).thenReturn(testAccountEntity);
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(Mono.just(testAccountEntity));
        when(accountMapper.toDomain(any(AccountEntity.class))).thenReturn(testAccount);

        // When
        Mono<Account> result1 = accountDatabaseAdapterService.createAccount(testAccount);
        Mono<Account> result2 = accountDatabaseAdapterService.createAccount(testAccount);

        // Then
        StepVerifier.create(Mono.zip(result1, result2))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isNotNull();
                    assertThat(tuple.getT2()).isNotNull();
                })
                .verifyComplete();

        verify(accountMapper, times(2)).toEntity(testAccount);
        verify(accountRepository, times(2)).save(testAccountEntity);
    }
}