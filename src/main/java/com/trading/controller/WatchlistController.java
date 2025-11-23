package com.trading.controller;

import com.trading.model.Stock;
import com.trading.model.Watchlist;
import com.trading.model.WatchlistItem;
import com.trading.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Watchlist> createWatchlist(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String name = (String) request.get("name");

        return watchlistService.createWatchlist(userId, name);
    }

    @GetMapping("/{id}")
    public Mono<Watchlist> getWatchlistById(@PathVariable Long id) {
        return watchlistService.getWatchlistById(id);
    }

    @GetMapping("/user/{userId}")
    public Flux<Watchlist> getUserWatchlists(@PathVariable Long userId) {
        return watchlistService.getUserWatchlists(userId);
    }

    @GetMapping("/{id}/stocks")
    public Flux<Stock> getWatchlistStocks(@PathVariable Long id) {
        return watchlistService.getWatchlistStocks(id);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<WatchlistItem> addStockToWatchlist(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        return watchlistService.addStockToWatchlist(id, request.get("stockId"));
    }

    @DeleteMapping("/{watchlistId}/stocks/{stockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeStockFromWatchlist(
            @PathVariable Long watchlistId,
            @PathVariable Long stockId) {
        return watchlistService.removeStockFromWatchlist(watchlistId, stockId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteWatchlist(@PathVariable Long id) {
        return watchlistService.deleteWatchlist(id);
    }
}
