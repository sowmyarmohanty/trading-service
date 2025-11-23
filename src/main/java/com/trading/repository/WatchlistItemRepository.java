package com.trading.repository;

import com.trading.model.WatchlistItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WatchlistItemRepository extends ReactiveCrudRepository<WatchlistItem, Long> {
    
    Flux<WatchlistItem> findByWatchlistId(Long watchlistId);
    
    Mono<Void> deleteByWatchlistIdAndStockId(Long watchlistId, Long stockId);
}
