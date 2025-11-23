package com.trading.repository;

import com.trading.model.Stock;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StockRepository extends ReactiveCrudRepository<Stock, Long> {
    
    Mono<Stock> findBySymbol(String symbol);
    
    Flux<Stock> findBySector(String sector);
}
