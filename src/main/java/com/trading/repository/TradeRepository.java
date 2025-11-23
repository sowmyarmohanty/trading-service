package com.trading.repository;

import com.trading.model.Trade;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface TradeRepository extends ReactiveCrudRepository<Trade, Long> {
    
    Flux<Trade> findByStockId(Long stockId);
    
    @Query("SELECT * FROM trades WHERE executed_at BETWEEN :startDate AND :endDate ORDER BY executed_at DESC")
    Flux<Trade> findByExecutedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT * FROM trades ORDER BY executed_at DESC LIMIT :limit")
    Flux<Trade> findRecentTrades(int limit);
}
