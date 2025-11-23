package com.trading.repository;

import com.trading.model.MarketData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MarketDataRepository extends ReactiveCrudRepository<MarketData, Long> {
    
    Flux<MarketData> findByStockId(Long stockId);
    
    @Query("SELECT * FROM market_data WHERE stock_id = :stockId ORDER BY timestamp DESC LIMIT 1")
    Mono<MarketData> findLatestByStockId(Long stockId);
    
    @Query("SELECT * FROM market_data WHERE stock_id = :stockId ORDER BY timestamp DESC LIMIT :limit")
    Flux<MarketData> findRecentByStockId(Long stockId, int limit);
}
