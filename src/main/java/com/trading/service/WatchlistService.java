package com.trading.service;

import com.trading.model.Stock;
import com.trading.model.Watchlist;
import com.trading.model.WatchlistItem;
import com.trading.repository.StockRepository;
import com.trading.repository.WatchlistItemRepository;
import com.trading.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final StockRepository stockRepository;

    public Mono<Watchlist> createWatchlist(Long userId, String name) {
        log.debug("Creating watchlist '{}' for user {}", name, userId);

        Watchlist watchlist = new Watchlist(userId, name);
        return watchlistRepository.save(watchlist);
    }

    public Flux<Watchlist> getUserWatchlists(Long userId) {
        return watchlistRepository.findByUserId(userId);
    }

    public Mono<Watchlist> getWatchlistById(Long id) {
        return watchlistRepository.findById(id);
    }

    public Mono<WatchlistItem> addStockToWatchlist(Long watchlistId, Long stockId) {
        log.debug("Adding stock {} to watchlist {}", stockId, watchlistId);

        return watchlistRepository.findById(watchlistId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Watchlist not found")))
                .then(stockRepository.findById(stockId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Stock not found")))
                .then(Mono.defer(() -> {
                    WatchlistItem item = new WatchlistItem(watchlistId, stockId);
                    return watchlistItemRepository.save(item);
                }));
    }

    public Mono<Void> removeStockFromWatchlist(Long watchlistId, Long stockId) {
        log.debug("Removing stock {} from watchlist {}", stockId, watchlistId);

        return watchlistItemRepository.deleteByWatchlistIdAndStockId(watchlistId, stockId);
    }

    public Flux<Stock> getWatchlistStocks(Long watchlistId) {
        return watchlistItemRepository.findByWatchlistId(watchlistId)
                .flatMap(item -> stockRepository.findById(item.getStockId()));
    }

    public Mono<Void> deleteWatchlist(Long watchlistId) {
        return watchlistItemRepository.findByWatchlistId(watchlistId)
                .flatMap(item -> watchlistItemRepository.delete(item))
                .then(watchlistRepository.deleteById(watchlistId));
    }
}
