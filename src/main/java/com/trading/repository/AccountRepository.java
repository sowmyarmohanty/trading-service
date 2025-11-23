package com.trading.repository;

import com.trading.model.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    
    Flux<Account> findByUserId(Long userId);
    
    Mono<Account> findByAccountNumber(String accountNumber);
    
    Flux<Account> findByStatus(String status);
}
