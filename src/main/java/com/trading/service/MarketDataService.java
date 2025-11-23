package com.trading.service;

import com.trading.model.MarketData;
import com.trading.model.Stock;
import com.trading.repository.MarketDataRepository;
import com.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final StockRepository stockRepository;
    private final MarketDataRepository marketDataRepository;
    private final Random random = new Random();

    public Flux<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Mono<Stock> getStockById(Long id) {
        return stockRepository.findById(id);
    }

    public Mono<Stock> getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol);
    }

    public Flux<Stock> getStocksBySector(String sector) {
        return stockRepository.findBySector(sector);
    }

    public Flux<MarketData> getMarketDataForStock(Long stockId) {
        return marketDataRepository.findByStockId(stockId);
    }

    public Mono<MarketData> getLatestMarketData(Long stockId) {
        return marketDataRepository.findLatestByStockId(stockId);
    }

    /**
     * Stream real-time price updates for a stock
     * Simulates price changes every 2 seconds
     */
    public Flux<Stock> streamPriceUpdates(Long stockId) {
        return stockRepository.findById(stockId)
                .flatMapMany(stock -> Flux.interval(Duration.ofSeconds(2))
                        .flatMap(tick -> {
                            // Simulate price change (-2% to +2%)
                            double changePercent = (random.nextDouble() * 4) - 2;
                            BigDecimal priceChange = stock.getCurrentPrice()
                                    .multiply(BigDecimal.valueOf(changePercent / 100));
                            BigDecimal newPrice = stock.getCurrentPrice().add(priceChange);

                            stock.setCurrentPrice(newPrice);

                            // Save market data snapshot
                            MarketData marketData = new MarketData(
                                    stockId,
                                    newPrice,
                                    (long) (random.nextInt(10000000) + 1000000));

                            return marketDataRepository.save(marketData)
                                    .then(stockRepository.save(stock));
                        }));
    }

    /**
     * Stream all stock prices
     */
    public Flux<Stock> streamAllPrices() {
        return Flux.interval(Duration.ofSeconds(3))
                .flatMap(tick -> stockRepository.findAll()
                        .flatMap(stock -> {
                            // Simulate price change
                            double changePercent = (random.nextDouble() * 4) - 2;
                            BigDecimal priceChange = stock.getCurrentPrice()
                                    .multiply(BigDecimal.valueOf(changePercent / 100));
                            BigDecimal newPrice = stock.getCurrentPrice().add(priceChange);

                            stock.setCurrentPrice(newPrice);
                            return stockRepository.save(stock);
                        }));
    }
}
