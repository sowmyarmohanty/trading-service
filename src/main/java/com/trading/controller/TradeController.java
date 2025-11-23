package com.trading.controller;

import com.trading.model.Trade;
import com.trading.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/{id}")
    public Mono<Trade> getTradeById(@PathVariable Long id) {
        return tradeService.findById(id);
    }

    @GetMapping("/stock/{stockId}")
    public Flux<Trade> getTradesByStockId(@PathVariable Long stockId) {
        return tradeService.findByStockId(stockId);
    }

    @GetMapping("/recent")
    public Flux<Trade> getRecentTrades(@RequestParam(defaultValue = "10") int limit) {
        return tradeService.findRecentTrades(limit);
    }

    @PostMapping("/execute")
    public Mono<Trade> executeTrade(@RequestBody Map<String, Long> request) {
        return tradeService.executeTrade(
                request.get("buyOrderId"),
                request.get("sellOrderId"));
    }

    @PostMapping("/match/{stockId}")
    public Flux<Trade> matchOrders(@PathVariable Long stockId) {
        return tradeService.matchOrders(stockId);
    }
}
