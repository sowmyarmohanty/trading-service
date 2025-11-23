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
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        // no heavy setup - keep tests isolated
        reset(accountRepository);
    }

    @Test
    void createAccount_success() {
        // Arrange
        Long userId = 123L;
        String accountType = "CASH";
        BigDecimal initialBalance = BigDecimal.valueOf(1000);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.createAccount(userId, accountType, initialBalance);

        // Assert
        StepVerifier.create(result)
                .assertNext(account -> {
                    assertThat(account.getUserId()).isEqualTo(userId);
                    assertThat(account.getAccountType()).isEqualTo(accountType);
                    assertThat(account.getBalance()).isEqualByComparingTo(initialBalance);
                    assertThat(account.getStatus()).isEqualTo("ACTIVE");
                    String acctNum = account.getAccountNumber();
                    assertThat(acctNum).startsWith("ACC");
                    assertThat(acctNum.substring(3)).hasSize(8);
                    assertThat(acctNum.substring(3)).matches("[A-Z0-9]{8}");
                })
                .verifyComplete();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(captor.capture());
        Account savedArg = captor.getValue();
        assertThat(savedArg.getUserId()).isEqualTo(userId);
    }

    @Test
    void createAccount_generatesUniqueAccountNumber_format() {
        // Arrange
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> first = accountService.createAccount(1L, "CASH", BigDecimal.ZERO);
        Mono<Account> second = accountService.createAccount(2L, "MARGIN", BigDecimal.ZERO);

        // Assert - collect both
        StepVerifier.create(first).expectNextMatches(a -> a.getAccountNumber().startsWith("ACC") && a.getAccountNumber().length() == 11).verifyComplete();
        StepVerifier.create(second).expectNextMatches(a -> a.getAccountNumber().startsWith("ACC") && a.getAccountNumber().length() == 11).verifyComplete();

        // ensure they are unique
        // Recreate saved accounts to inspect values via repository.save invocations
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(captor.capture());
        Set<String> nums = new HashSet<>();
        captor.getAllValues().forEach(a -> nums.add(a.getAccountNumber()));
        assertThat(nums).hasSize(2);
    }

    @Test
    void createAccount_nullInitialBalance() {
        // Arrange
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> mono = accountService.createAccount(10L, "CASH", null);

        // Assert
        StepVerifier.create(mono)
                .assertNext(account -> assertThat(account.getBalance()).isNull())
                .verifyComplete();

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void findById_found() {
        // Arrange
        Account account = new Account(1L, "ACC12345678", BigDecimal.TEN, "CASH", "ACTIVE");
        when(accountRepository.findById(1L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> mono = accountService.findById(1L);

        // Assert
        StepVerifier.create(mono)
                .expectNext(account)
                .verifyComplete();

        verify(accountRepository).findById(1L);
    }

    @Test
    void findById_notFound() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> mono = accountService.findById(99L);

        // Assert
        StepVerifier.create(mono).verifyComplete();
        verify(accountRepository).findById(99L);
    }

    @Test
    void findByUserId_returnsMultiple() {
        // Arrange
        Account a1 = new Account(5L, "ACC00000001", BigDecimal.ONE, "CASH", "ACTIVE");
        Account a2 = new Account(5L, "ACC00000002", BigDecimal.TEN, "MARGIN", "ACTIVE");
        when(accountRepository.findByUserId(5L)).thenReturn(Flux.just(a1, a2));

        // Act
        Flux<Account> flux = accountService.findByUserId(5L);

        // Assert
        StepVerifier.create(flux)
                .expectNext(a1)
                .expectNext(a2)
                .verifyComplete();

        verify(accountRepository).findByUserId(5L);
    }

    @Test
    void findByUserId_empty() {
        // Arrange
        when(accountRepository.findByUserId(6L)).thenReturn(Flux.empty());

        // Act
        Flux<Account> flux = accountService.findByUserId(6L);

        // Assert
        StepVerifier.create(flux).verifyComplete();
        verify(accountRepository).findByUserId(6L);
    }

    @Test
    void findByAccountNumber_found() {
        // Arrange
        Account account = new Account(7L, "ACCABCDEF12", BigDecimal.TEN, "CASH", "ACTIVE");
        when(accountRepository.findByAccountNumber("ACCABCDEF12")).thenReturn(Mono.just(account));

        // Act
        Mono<Account> mono = accountService.findByAccountNumber("ACCABCDEF12");

        // Assert
        StepVerifier.create(mono)
                .expectNext(account)
                .verifyComplete();

        verify(accountRepository).findByAccountNumber("ACCABCDEF12");
    }

    @Test
    void findByAccountNumber_notFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("NOPE")).thenReturn(Mono.empty());

        // Act
        Mono<Account> mono = accountService.findByAccountNumber("NOPE");

        // Assert
        StepVerifier.create(mono).verifyComplete();
        verify(accountRepository).findByAccountNumber("NOPE");
    }

    @Test
    void deposit_success() {
        // Arrange
        Account account = new Account(1L, "ACC0001", BigDecimal.valueOf(100), "CASH", "ACTIVE");
        when(accountRepository.findById(1L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.deposit(1L, BigDecimal.valueOf(50));

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> assertThat(a.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150)))
                .verifyComplete();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void deposit_inactiveAccount() {
        // Arrange
        Account account = new Account(2L, "ACC0002", BigDecimal.valueOf(100), "CASH", "SUSPENDED");
        when(accountRepository.findById(2L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.deposit(2L, BigDecimal.valueOf(50));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException && throwable.getMessage().equals("Account is not active"))
                .verify();

        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_accountNotFound() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.deposit(999L, BigDecimal.TEN);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_amountNull_triggersNpe() {
        // Arrange
        Account account = new Account(3L, "ACC0003", BigDecimal.TEN, "CASH", "ACTIVE");
        when(accountRepository.findById(3L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.deposit(3L, null);

        // Assert
        StepVerifier.create(result)
                .expectError(NullPointerException.class)
                .verify();

        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_negativeAmount_behavesAsAddNegative() {
        // Arrange
        Account account = new Account(4L, "ACC0004", BigDecimal.valueOf(100), "CASH", "ACTIVE");
        when(accountRepository.findById(4L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.deposit(4L, BigDecimal.valueOf(-50));

        // Assert - current implementation will reduce balance
        StepVerifier.create(result)
                .assertNext(a -> assertThat(a.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(50)))
                .verifyComplete();

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void withdraw_success() {
        // Arrange
        Account account = new Account(10L, "ACC010", BigDecimal.valueOf(100), "CASH", "ACTIVE");
        when(accountRepository.findById(10L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.withdraw(10L, BigDecimal.valueOf(60));

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> assertThat(a.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(40)))
                .verifyComplete();

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void withdraw_insufficientBalance() {
        // Arrange
        Account account = new Account(11L, "ACC011", BigDecimal.valueOf(50), "CASH", "ACTIVE");
        when(accountRepository.findById(11L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.withdraw(11L, BigDecimal.valueOf(60));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Insufficient balance"))
                .verify();

        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_inactiveAccount() {
        // Arrange
        Account account = new Account(12L, "ACC012", BigDecimal.valueOf(100), "CASH", "CLOSED");
        when(accountRepository.findById(12L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.withdraw(12L, BigDecimal.valueOf(10));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException && throwable.getMessage().equals("Account is not active"))
                .verify();

        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_accountNotFound() {
        // Arrange
        when(accountRepository.findById(1000L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.withdraw(1000L, BigDecimal.ONE);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_amountNull_triggersNpe() {
        // Arrange
        Account account = new Account(13L, "ACC013", BigDecimal.valueOf(100), "CASH", "ACTIVE");
        when(accountRepository.findById(13L)).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountService.withdraw(13L, null);

        // Assert
        StepVerifier.create(result).expectError(NullPointerException.class).verify();
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_negativeAmount_behavesAsSubtractNegative() {
        // Arrange
        Account account = new Account(14L, "ACC014", BigDecimal.valueOf(100), "CASH", "ACTIVE");
        when(accountRepository.findById(14L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.withdraw(14L, BigDecimal.valueOf(-50));

        // Assert - current implementation will increase balance (100 - (-50) = 150)
        StepVerifier.create(result)
                .assertNext(a -> assertThat(a.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150)))
                .verifyComplete();

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateBalance_success() {
        // Arrange
        Account account = new Account(20L, "ACC020", BigDecimal.valueOf(10), "CASH", "ACTIVE");
        when(accountRepository.findById(20L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.updateBalance(20L, BigDecimal.valueOf(200));

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> assertThat(a.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(200)))
                .verifyComplete();

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateBalance_accountNotFound() {
        // Arrange
        when(accountRepository.findById(777L)).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountService.updateBalance(777L, BigDecimal.ONE);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateBalance_nullBalance_setsNull() {
        // Arrange
        Account account = new Account(21L, "ACC021", BigDecimal.valueOf(1), "CASH", "ACTIVE");
        when(accountRepository.findById(21L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Account> result = accountService.updateBalance(21L, null);

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> assertThat(a.getBalance()).isNull())
                .verifyComplete();

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void repositorySaveError_propagation_createAccount() {
        // Arrange
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.error(new RuntimeException("DB down")));

        // Act
        Mono<Account> result = accountService.createAccount(1L, "CASH", BigDecimal.ZERO);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("DB down"))
                .verify();

        verify(accountRepository).save(any());
    }

    @Test
    void repositorySaveError_propagation_deposit() {
        // Arrange
        Account account = new Account(30L, "ACC030", BigDecimal.valueOf(10), "CASH", "ACTIVE");
        when(accountRepository.findById(30L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.error(new RuntimeException("DB fail")));

        // Act
        Mono<Account> result = accountService.deposit(30L, BigDecimal.valueOf(5));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("DB fail"))
                .verify();

        verify(accountRepository).save(any());
    }

    @Test
    void repositorySaveError_propagation_withdraw() {
        // Arrange
        Account account = new Account(31L, "ACC031", BigDecimal.valueOf(100), "CASH", "ACTIVE");
        when(accountRepository.findById(31L)).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.error(new RuntimeException("DB err")));

        // Act
        Mono<Account> result = accountService.withdraw(31L, BigDecimal.valueOf(10));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("DB err"))
                .verify();

        verify(accountRepository).save(any());
    }
}
