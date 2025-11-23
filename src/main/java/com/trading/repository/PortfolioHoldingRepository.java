package com.trading.repository;

import com.trading.model.PortfolioHolding;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PortfolioHoldingRepository extends ReactiveCrudRepository<PortfolioHolding, Long> {
    
    Flux<PortfolioHolding> findByAccountId(Long accountId);
    
    Mono<PortfolioHolding> findByAccountIdAndStockId(Long accountId, Long stockId);
}
