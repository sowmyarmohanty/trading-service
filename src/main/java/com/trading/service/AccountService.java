package com.trading.service;

import com.trading.model.Account;
import com.trading.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Mono<Account> createAccount(Long userId, String accountType, BigDecimal initialBalance) {
        log.debug("Creating new account for user: {}", userId);

        String accountNumber = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Account account = new Account(userId, accountNumber, initialBalance, accountType, "ACTIVE");

        return accountRepository.save(account);
    }

    public Mono<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public Flux<Account> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Mono<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public Mono<Account> deposit(Long accountId, BigDecimal amount) {
        log.debug("Depositing {} to account {}", amount, accountId);

        return accountRepository.findById(accountId)
                .flatMap(account -> {
                    if (!"ACTIVE".equals(account.getStatus())) {
                        return Mono.error(new IllegalStateException("Account is not active"));
                    }
                    account.setBalance(account.getBalance().add(amount));
                    return accountRepository.save(account);
                });
    }

    public Mono<Account> withdraw(Long accountId, BigDecimal amount) {
        log.debug("Withdrawing {} from account {}", amount, accountId);

        return accountRepository.findById(accountId)
                .flatMap(account -> {
                    if (!"ACTIVE".equals(account.getStatus())) {
                        return Mono.error(new IllegalStateException("Account is not active"));
                    }
                    if (account.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new IllegalArgumentException("Insufficient balance"));
                    }
                    account.setBalance(account.getBalance().subtract(amount));
                    return accountRepository.save(account);
                });
    }

    public Mono<Account> updateBalance(Long accountId, BigDecimal newBalance) {
        return accountRepository.findById(accountId)
                .flatMap(account -> {
                    account.setBalance(newBalance);
                    return accountRepository.save(account);
                });
    }
}
