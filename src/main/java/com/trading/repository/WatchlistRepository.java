package com.trading.repository;

import com.trading.model.Watchlist;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface WatchlistRepository extends ReactiveCrudRepository<Watchlist, Long> {
    
    Flux<Watchlist> findByUserId(Long userId);
}
