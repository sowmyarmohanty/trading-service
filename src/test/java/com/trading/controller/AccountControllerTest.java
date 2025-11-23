package com.trading.controller;

import com.trading.dto.DepositWithdrawRequest;
import com.trading.model.Account;
import com.trading.service.AccountService;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        // No global setup required; use per-test Arrange for clarity
    }

    @Test
    void createAccount_validRequest_returnsCreatedAccount() {
        // Arrange
        Long userId = 123L;
        String accountType = "CASH";
        BigDecimal initialBalance = new BigDecimal("100.50");

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("accountType", accountType);
        request.put("initialBalance", initialBalance);

        Account returned = new Account(1L, userId, "ACC12345", initialBalance, accountType, "ACTIVE", LocalDateTime.now());

        when(accountService.createAccount(eq(userId), eq(accountType), eq(initialBalance))).thenReturn(Mono.just(returned));

        // Act
        Mono<Account> resultMono = accountController.createAccount(request);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(account -> {
                    assertSame(returned, account, "Controller should return same account object provided by service");
                    assertEquals(userId, account.getUserId());
                    assertEquals(accountType, account.getAccountType());
                    assertEquals(0, initialBalance.compareTo(account.getBalance()));
                })
                .verifyComplete();

        verify(accountService, times(1)).createAccount(eq(userId), eq(accountType), eq(initialBalance));
    }

    @Test
    void createAccount_missingUserId_throwsNullPointerException() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("accountType", "CASH");
        request.put("initialBalance", "10.00");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> accountController.createAccount(request));

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccount_nonNumericUserId_throwsNumberFormatException() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("userId", "notANumber");
        request.put("accountType", "CASH");
        request.put("initialBalance", "10.00");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> accountController.createAccount(request));

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccount_accountTypeWrongType_throwsClassCastException() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 10L);
        request.put("accountType", 123); // not a String
        request.put("initialBalance", "10.00");

        // Act & Assert
        assertThrows(ClassCastException.class, () -> accountController.createAccount(request));

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccount_invalidInitialBalanceFormat_throwsNumberFormatException() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 10L);
        request.put("accountType", "CASH");
        request.put("initialBalance", "invalid-decimal");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> accountController.createAccount(request));

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccount_serviceError_propagatesError() {
        // Arrange
        Long userId = 7L;
        String accountType = "MARGIN";
        BigDecimal initialBalance = new BigDecimal("50.00");

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("accountType", accountType);
        request.put("initialBalance", initialBalance);

        RuntimeException serviceEx = new RuntimeException("database down");
        when(accountService.createAccount(eq(userId), eq(accountType), eq(initialBalance))).thenReturn(Mono.error(serviceEx));

        // Act
        Mono<Account> resultMono = accountController.createAccount(request);

        // Assert
        StepVerifier.create(resultMono)
                .expectErrorMatches(err -> err instanceof RuntimeException && "database down".equals(err.getMessage()))
                .verify();

        verify(accountService, times(1)).createAccount(eq(userId), eq(accountType), eq(initialBalance));
    }

    @Test
    void getAccountById_found_returnsAccount() {
        // Arrange
        Long id = 5L;
        Account account = new Account(id, 11L, "ACC0001", new BigDecimal("200.00"), "CASH", "ACTIVE", LocalDateTime.now());
        when(accountService.findById(eq(id))).thenReturn(Mono.just(account));

        // Act
        Mono<Account> result = accountController.getAccountById(id);

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> assertEquals(id, a.getId()))
                .verifyComplete();

        verify(accountService, times(1)).findById(eq(id));
    }

    @Test
    void getAccountById_notFound_returnsEmptyMono() {
        // Arrange
        Long id = 99L;
        when(accountService.findById(eq(id))).thenReturn(Mono.empty());

        // Act
        Mono<Account> result = accountController.getAccountById(id);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(accountService, times(1)).findById(eq(id));
    }

    @Test
    void getAccountsByUserId_found_returnsMultipleAccounts() {
        // Arrange
        Long userId = 42L;
        Account a1 = new Account(1L, userId, "ACC1", new BigDecimal("10.00"), "CASH", "ACTIVE", LocalDateTime.now());
        Account a2 = new Account(2L, userId, "ACC2", new BigDecimal("20.00"), "MARGIN", "ACTIVE", LocalDateTime.now());
        when(accountService.findByUserId(eq(userId))).thenReturn(Flux.just(a1, a2));

        // Act
        Flux<Account> result = accountController.getAccountsByUserId(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(a1)
                .expectNext(a2)
                .verifyComplete();

        verify(accountService, times(1)).findByUserId(eq(userId));
    }

    @Test
    void getAccountsByUserId_empty_returnsEmptyFlux() {
        // Arrange
        Long userId = 100L;
        when(accountService.findByUserId(eq(userId))).thenReturn(Flux.empty());

        // Act
        Flux<Account> result = accountController.getAccountsByUserId(userId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(accountService, times(1)).findByUserId(eq(userId));
    }

    @Test
    void getAccountByNumber_found_and_notFound() {
        // Arrange - found
        String accNumber = "ACC-XYZ";
        Account account = new Account(7L, 200L, accNumber, new BigDecimal("300.00"), "CASH", "ACTIVE", LocalDateTime.now());
        when(accountService.findByAccountNumber(eq(accNumber))).thenReturn(Mono.just(account));

        // Act & Assert - found
        StepVerifier.create(accountController.getAccountByNumber(accNumber))
                .assertNext(a -> assertEquals(accNumber, a.getAccountNumber()))
                .verifyComplete();

        verify(accountService, times(1)).findByAccountNumber(eq(accNumber));

        // Arrange - not found
        String notFoundNumber = "ACC-NOT";
        when(accountService.findByAccountNumber(eq(notFoundNumber))).thenReturn(Mono.empty());

        // Act & Assert - not found
        StepVerifier.create(accountController.getAccountByNumber(notFoundNumber))
                .verifyComplete();

        verify(accountService, times(1)).findByAccountNumber(eq(notFoundNumber));
    }

    @Test
    void deposit_validAmount_delegatesToService_and_returnsUpdatedAccount() {
        // Arrange
        Long accountId = 55L;
        BigDecimal amount = new BigDecimal("25.75");
        DepositWithdrawRequest request = new DepositWithdrawRequest(amount);

        Account updated = new Account(accountId, 11L, "ACCD", new BigDecimal("125.75"), "CASH", "ACTIVE", LocalDateTime.now());
        when(accountService.deposit(eq(accountId), eq(amount))).thenReturn(Mono.just(updated));

        // Act
        Mono<Account> result = accountController.deposit(accountId, request);

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> assertEquals(updated.getId(), a.getId()))
                .verifyComplete();

        verify(accountService, times(1)).deposit(eq(accountId), eq(amount));
    }

    @Test
    void deposit_nullRequest_throwsNullPointerException() {
        // Arrange
        Long accountId = 10L;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> accountController.deposit(accountId, null));

        verifyNoInteractions(accountService);
    }

    @Test
    void deposit_serviceError_propagatesError() {
        // Arrange
        Long accountId = 8L;
        BigDecimal amount = new BigDecimal("5.00");
        DepositWithdrawRequest request = new DepositWithdrawRequest(amount);

        RuntimeException serviceEx = new RuntimeException("deposit failed");
        when(accountService.deposit(eq(accountId), eq(amount))).thenReturn(Mono.error(serviceEx));

        // Act
        Mono<Account> result = accountController.deposit(accountId, request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("deposit failed"))
                .verify();

        verify(accountService, times(1)).deposit(eq(accountId), eq(amount));
    }

    @Test
    void withdraw_validAmount_delegatesToService_and_returnsUpdatedAccount() {
        // Arrange
        Long accountId = 99L;
        BigDecimal amount = new BigDecimal("15.00");
        DepositWithdrawRequest request = new DepositWithdrawRequest(amount);

        Account updated = new Account(accountId, 33L, "ACCW", new BigDecimal("85.00"), "CASH", "ACTIVE", LocalDateTime.now());
        when(accountService.withdraw(eq(accountId), eq(amount))).thenReturn(Mono.just(updated));

        // Act
        Mono<Account> result = accountController.withdraw(accountId, request);

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> assertEquals(updated.getId(), a.getId()))
                .verifyComplete();

        verify(accountService, times(1)).withdraw(eq(accountId), eq(amount));
    }

    @Test
    void withdraw_insufficientFunds_serviceReturnsError_propagates() {
        // Arrange
        Long accountId = 77L;
        BigDecimal amount = new BigDecimal("1000.00");
        DepositWithdrawRequest request = new DepositWithdrawRequest(amount);

        IllegalArgumentException insufficient = new IllegalArgumentException("Insufficient balance");
        when(accountService.withdraw(eq(accountId), eq(amount))).thenReturn(Mono.error(insufficient));

        // Act
        Mono<Account> result = accountController.withdraw(accountId, request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof IllegalArgumentException && e.getMessage().equals("Insufficient balance"))
                .verify();

        verify(accountService, times(1)).withdraw(eq(accountId), eq(amount));
    }

    @Test
    void deposit_and_withdraw_verify_correct_amount_passed_to_service() {
        // Arrange
        Long accountId = 1234L;
        BigDecimal depositAmount = new BigDecimal("10.500");
        BigDecimal withdrawAmount = new BigDecimal("1.250");

        DepositWithdrawRequest depositRequest = new DepositWithdrawRequest(depositAmount);
        DepositWithdrawRequest withdrawRequest = new DepositWithdrawRequest(withdrawAmount);

        Account depositResult = new Account(accountId, 1L, "ACCDT", new BigDecimal("110.500"), "CASH", "ACTIVE", LocalDateTime.now());
        Account withdrawResult = new Account(accountId, 1L, "ACCDT", new BigDecimal("109.250"), "CASH", "ACTIVE", LocalDateTime.now());

        when(accountService.deposit(eq(accountId), eq(depositAmount))).thenReturn(Mono.just(depositResult));
        when(accountService.withdraw(eq(accountId), eq(withdrawAmount))).thenReturn(Mono.just(withdrawResult));

        // Act
        StepVerifier.create(accountController.deposit(accountId, depositRequest))
                .assertNext(a -> assertEquals(depositResult.getId(), a.getId()))
                .verifyComplete();

        StepVerifier.create(accountController.withdraw(accountId, withdrawRequest))
                .assertNext(a -> assertEquals(withdrawResult.getId(), a.getId()))
                .verifyComplete();

        // Assert - verify exact BigDecimal passed
        ArgumentCaptor<BigDecimal> depositCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> withdrawCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(accountService, times(1)).deposit(eq(accountId), depositCaptor.capture());
        verify(accountService, times(1)).withdraw(eq(accountId), withdrawCaptor.capture());

        assertEquals(0, depositAmount.compareTo(depositCaptor.getValue()));
        assertEquals(0, withdrawAmount.compareTo(withdrawCaptor.getValue()));
    }

    @Test
    void createAccount_with_additional_unexpected_fields_stillParsesRequiredOnes() {
        // Arrange
        Long userId = 555L;
        String accountType = "CASH";
        BigDecimal initialBalance = new BigDecimal("500.00");

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("accountType", accountType);
        request.put("initialBalance", initialBalance);
        request.put("extraField", "extraValue");

        Account returned = new Account(2L, userId, "ACCEXTRA", initialBalance, accountType, "ACTIVE", LocalDateTime.now());
        when(accountService.createAccount(eq(userId), eq(accountType), eq(initialBalance))).thenReturn(Mono.just(returned));

        // Act
        Mono<Account> result = accountController.createAccount(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(a -> {
                    assertEquals(userId, a.getUserId());
                    assertEquals(accountType, a.getAccountType());
                    assertEquals(0, initialBalance.compareTo(a.getBalance()));
                })
                .verifyComplete();

        verify(accountService, times(1)).createAccount(eq(userId), eq(accountType), eq(initialBalance));
    }
}
