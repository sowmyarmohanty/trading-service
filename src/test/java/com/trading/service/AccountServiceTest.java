package com.trading.service;

import com.trading.model.Account;
import com.trading.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private static final Long USER_ID = 42L;
    private static final String ACCOUNT_TYPE = "CASH";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100.00");
    private static final Pattern ACC_NUMBER_PATTERN = Pattern.compile("^ACC[A-F0-9]{8}$");

    @BeforeEach
    void setUp() {
        // no global setup required beyond InjectMocks and Mock annotations
    }

    @Test
    void createAccount_validInputs_shouldSaveAndReturnAccountWithGeneratedAccountNumberAndActiveStatus() {
        // Arrange
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.createAccount(USER_ID, ACCOUNT_TYPE, INITIAL_BALANCE);

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertNotNull(saved);
                    assertEquals(USER_ID, saved.getUserId());
                    assertEquals(ACCOUNT_TYPE, saved.getAccountType());
                    assertEquals(INITIAL_BALANCE, saved.getBalance());
                    assertEquals("ACTIVE", saved.getStatus());
                    assertNotNull(saved.getCreatedAt());
                    assertNotNull(saved.getAccountNumber());
                    assertEquals(11, saved.getAccountNumber().length());
                    assertTrue(ACC_NUMBER_PATTERN.matcher(saved.getAccountNumber()).matches(), "accountNumber format should match ACC + 8 hex chars");
                })
                .verifyComplete();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(captor.capture());
        Account captured = captor.getValue();
        assertEquals(USER_ID, captured.getUserId());
        assertEquals(ACCOUNT_TYPE, captured.getAccountType());
        assertEquals(INITIAL_BALANCE, captured.getBalance());
        assertEquals("ACTIVE", captured.getStatus());
        assertNotNull(captured.getAccountNumber());
    }

    @Test
    void createAccount_nullInitialBalance_shouldAllowNullBalanceAndReturnSavedAccount() {
        // Arrange
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.createAccount(USER_ID, ACCOUNT_TYPE, null);

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertNull(saved.getBalance());
                    assertEquals("ACTIVE", saved.getStatus());
                })
                .verifyComplete();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(captor.capture());
        assertNull(captor.getValue().getBalance());
    }

    @Test
    void findById_existing_shouldReturnAccount() {
        // Arrange
        Account account = new Account(1L, "ACC12345678", new BigDecimal("10"), "CASH", "ACTIVE");
        when(accountRepository.findById(1L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.findById(1L);

        // Assert
        StepVerifier.create(result)
                .expectNext(account)
                .verifyComplete();

        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void findById_notFound_shouldReturnEmptyMono() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.findById(99L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(accountRepository, times(1)).findById(99L);
    }

    @Test
    void findByUserId_multipleAccounts_shouldReturnAllAccountsInOrder() {
        // Arrange
        Account a1 = new Account(1L, "ACC11111111", new BigDecimal("1"), "CASH", "ACTIVE");
        Account a2 = new Account(1L, "ACC22222222", new BigDecimal("2"), "MARGIN", "ACTIVE");
        when(accountRepository.findByUserId(1L)).thenReturn(Flux.just(a1, a2));

        // Act
        Flux<Account> result = accountService.findByUserId(1L);

        // Assert
        StepVerifier.create(result)
                .expectNext(a1)
                .expectNext(a2)
                .verifyComplete();

        verify(accountRepository, times(1)).findByUserId(1L);
    }

    @Test
    void findByUserId_noAccounts_shouldReturnEmptyFlux() {
        // Arrange
        when(accountRepository.findByUserId(99L)).thenReturn(Flux.empty());

        // Act
        Flux<Account> result = accountService.findByUserId(99L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(accountRepository, times(1)).findByUserId(99L);
    }

    @Test
    void findByAccountNumber_existing_shouldReturnAccount() {
        // Arrange
        Account account = new Account(5L, "ACCABCDEF12", new BigDecimal("5"), "CASH", "ACTIVE");
        when(accountRepository.findByAccountNumber("ACCABCDEF12")).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.findByAccountNumber("ACCABCDEF12");

        // Assert
        StepVerifier.create(result)
                .expectNext(account)
                .verifyComplete();

        verify(accountRepository, times(1)).findByAccountNumber("ACCABCDEF12");
    }

    @Test
    void findByAccountNumber_notFound_shouldReturnEmptyMono() {
        // Arrange
        when(accountRepository.findByAccountNumber("NOPE")).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.findByAccountNumber("NOPE");

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(accountRepository, times(1)).findByAccountNumber("NOPE");
    }

    @Test
    void deposit_success_activeAccount_shouldIncreaseBalanceAndSave() {
        // Arrange
        BigDecimal original = new BigDecimal("100.00");
        BigDecimal amount = new BigDecimal("25.00");
        Account account = new Account(10L, "ACC00000001", original, ACCOUNT_TYPE, "ACTIVE");

        when(accountRepository.findById(10L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.deposit(10L, amount);

        // Assert
        StepVerifier.create(result)
                .assertNext(updated -> {
                    assertEquals(original.add(amount), updated.getBalance());
                })
                .verifyComplete();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).findById(10L);
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(original.add(amount), captor.getValue().getBalance());
    }

    @Test
    void deposit_accountNotFound_shouldReturnEmptyMonoAndNotCallSave() {
        // Arrange
        when(accountRepository.findById(123L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.deposit(123L, new BigDecimal("10"));

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(accountRepository, times(1)).findById(123L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_inactiveAccount_shouldReturnIllegalStateException() {
        // Arrange
        Account account = new Account(11L, "ACCINACTIVE", new BigDecimal("50"), ACCOUNT_TYPE, "SUSPENDED");
        when(accountRepository.findById(11L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.deposit(11L, new BigDecimal("10"));

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof IllegalStateException);
                    assertEquals("Account is not active", err.getMessage());
                })
                .verify();

        verify(accountRepository, times(1)).findById(11L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_zeroAmount_shouldCallSaveAndKeepBalanceUnchanged() {
        // Arrange
        BigDecimal original = new BigDecimal("20.00");
        BigDecimal amount = BigDecimal.ZERO;
        Account account = new Account(12L, "ACCZERO", original, ACCOUNT_TYPE, "ACTIVE");

        when(accountRepository.findById(12L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.deposit(12L, amount);

        // Assert
        StepVerifier.create(result)
                .assertNext(updated -> assertEquals(original, updated.getBalance()))
                .verifyComplete();

        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void deposit_negativeAmount_shouldDecreaseBalanceAndSave_currentBehaviorAllowsIt() {
        // Arrange
        BigDecimal original = new BigDecimal("200.00");
        BigDecimal amount = new BigDecimal("-50.00");
        Account account = new Account(13L, "ACCNEGDEP", original, ACCOUNT_TYPE, "ACTIVE");

        when(accountRepository.findById(13L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.deposit(13L, amount);

        // Assert
        StepVerifier.create(result)
                .assertNext(updated -> assertEquals(original.add(amount), updated.getBalance()))
                .verifyComplete();

        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void withdraw_success_activeAndSufficient_shouldDecreaseBalanceAndSave() {
        // Arrange
        BigDecimal original = new BigDecimal("150.00");
        BigDecimal amount = new BigDecimal("50.00");
        Account account = new Account(20L, "ACCW1", original, ACCOUNT_TYPE, "ACTIVE");

        when(accountRepository.findById(20L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.withdraw(20L, amount);

        // Assert
        StepVerifier.create(result)
                .assertNext(updated -> assertEquals(original.subtract(amount), updated.getBalance()))
                .verifyComplete();

        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void withdraw_accountNotFound_shouldReturnEmptyMono() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.withdraw(999L, new BigDecimal("1"));

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(accountRepository, times(1)).findById(999L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_inactiveAccount_shouldReturnIllegalStateException() {
        // Arrange
        Account account = new Account(21L, "ACCW_INACT", new BigDecimal("100"), ACCOUNT_TYPE, "CLOSED");
        when(accountRepository.findById(21L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.withdraw(21L, new BigDecimal("10"));

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof IllegalStateException);
                    assertEquals("Account is not active", err.getMessage());
                })
                .verify();

        verify(accountRepository, times(1)).findById(21L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_insufficientFunds_shouldReturnIllegalArgumentException() {
        // Arrange
        Account account = new Account(22L, "ACCW_LOW", new BigDecimal("30.00"), ACCOUNT_TYPE, "ACTIVE");
        when(accountRepository.findById(22L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.withdraw(22L, new BigDecimal("50.00"));

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof IllegalArgumentException);
                    assertEquals("Insufficient balance", err.getMessage());
                })
                .verify();

        verify(accountRepository, times(1)).findById(22L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_zeroAmount_shouldKeepBalanceAndCallSave() {
        // Arrange
        BigDecimal original = new BigDecimal("75.00");
        BigDecimal amount = BigDecimal.ZERO;
        Account account = new Account(23L, "ACCW_ZERO", original, ACCOUNT_TYPE, "ACTIVE");

        when(accountRepository.findById(23L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.withdraw(23L, amount);

        // Assert
        StepVerifier.create(result)
                .assertNext(updated -> assertEquals(original, updated.getBalance()))
                .verifyComplete();

        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void withdraw_negativeAmount_shouldIncreaseBalance_currentBehaviorAllowsIt() {
        // Arrange
        BigDecimal original = new BigDecimal("60.00");
        BigDecimal amount = new BigDecimal("-40.00");
        Account account = new Account(24L, "ACCW_NEG", original, ACCOUNT_TYPE, "ACTIVE");

        when(accountRepository.findById(24L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.withdraw(24L, amount);

        // Assert
        StepVerifier.create(result)
                .assertNext(updated -> assertEquals(original.subtract(amount), updated.getBalance()))
                .verifyComplete();

        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void updateBalance_success_existingAccount_shouldSetNewBalanceAndSave() {
        // Arrange
        Account account = new Account(30L, "ACC_UPD", new BigDecimal("5"), ACCOUNT_TYPE, "ACTIVE");
        BigDecimal newBalance = new BigDecimal("999.99");
        when(accountRepository.findById(30L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.updateBalance(30L, newBalance);

        // Assert
        StepVerifier.create(result)
                .assertNext(updated -> assertEquals(newBalance, updated.getBalance()))
                .verifyComplete();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(newBalance, captor.getValue().getBalance());
    }

    @Test
    void updateBalance_accountNotFound_shouldReturnEmptyMonoAndNotCallSave() {
        // Arrange
        when(accountRepository.findById(404L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.updateBalance(404L, new BigDecimal("10"));

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(accountRepository, times(1)).findById(404L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void repositorySave_errorPropagation_createAccountShouldPropagateError() {
        // Arrange
        RuntimeException ex = new RuntimeException("DB down");
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.error(ex));

        // Act
        Mono<Account> result = accountService.createAccount(USER_ID, ACCOUNT_TYPE, INITIAL_BALANCE);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(err -> err == ex || err.getMessage().equals("DB down"))
                .verify();

        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void repositorySave_errorPropagation_depositShouldPropagateError() {
        // Arrange
        Account account = new Account(40L, "ACCDIE", new BigDecimal("10"), ACCOUNT_TYPE, "ACTIVE");
        when(accountRepository.findById(40L)).thenReturn(Mono.just(account));
        RuntimeException ex = new RuntimeException("save failed");
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.error(ex));

        // Act
        Mono<Account> result = accountService.deposit(40L, new BigDecimal("5"));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(err -> err == ex || err.getMessage().equals("save failed"))
                .verify();

        verify(accountRepository, times(1)).findById(40L);
        verify(accountRepository, times(1)).save(any());
    }
}
