package com.trading.controller;

import com.trading.dto.DepositWithdrawRequest;
import com.trading.model.Account;
import com.trading.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Account> createAccount(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String accountType = (String) request.get("accountType");
        BigDecimal initialBalance = new BigDecimal(request.get("initialBalance").toString());

        return accountService.createAccount(userId, accountType, initialBalance);
    }

    @GetMapping("/{id}")
    public Mono<Account> getAccountById(@PathVariable Long id) {
        return accountService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public Flux<Account> getAccountsByUserId(@PathVariable Long userId) {
        return accountService.findByUserId(userId);
    }

    @GetMapping("/number/{accountNumber}")
    public Mono<Account> getAccountByNumber(@PathVariable String accountNumber) {
        return accountService.findByAccountNumber(accountNumber);
    }

    @PostMapping("/{id}/deposit")
    public Mono<Account> deposit(@PathVariable Long id, @Valid @RequestBody DepositWithdrawRequest request) {
        return accountService.deposit(id, request.getAmount());
    }

    @PostMapping("/{id}/withdraw")
    public Mono<Account> withdraw(@PathVariable Long id, @Valid @RequestBody DepositWithdrawRequest request) {
        return accountService.withdraw(id, request.getAmount());
    }
}
