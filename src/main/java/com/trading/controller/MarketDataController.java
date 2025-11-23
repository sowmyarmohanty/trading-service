package com.trading.controller;

import com.trading.model.MarketData;
import com.trading.model.Stock;
import com.trading.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/stocks")
    public Flux<Stock> getAllStocks() {
        return marketDataService.getAllStocks();
    }

    @GetMapping("/stocks/{id}")
    public Mono<Stock> getStockById(@PathVariable Long id) {
        return marketDataService.getStockById(id);
    }

    @GetMapping("/stocks/symbol/{symbol}")
    public Mono<Stock> getStockBySymbol(@PathVariable String symbol) {
        return marketDataService.getStockBySymbol(symbol);
    }

    @GetMapping("/stocks/sector/{sector}")
    public Flux<Stock> getStocksBySector(@PathVariable String sector) {
        return marketDataService.getStocksBySector(sector);
    }

    @GetMapping("/data/{stockId}")
    public Flux<MarketData> getMarketData(@PathVariable Long stockId) {
        return marketDataService.getMarketDataForStock(stockId);
    }

    @GetMapping("/data/{stockId}/latest")
    public Mono<MarketData> getLatestMarketData(@PathVariable Long stockId) {
        return marketDataService.getLatestMarketData(stockId);
    }

    /**
     * Server-Sent Events endpoint for real-time price updates
     */
    @GetMapping(value = "/stocks/{stockId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Stock> streamStockPrices(@PathVariable Long stockId) {
        return marketDataService.streamPriceUpdates(stockId);
    }

    /**
     * Stream all stock prices
     */
    @GetMapping(value = "/stocks/stream/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Stock> streamAllPrices() {
        return marketDataService.streamAllPrices();
    }
}
