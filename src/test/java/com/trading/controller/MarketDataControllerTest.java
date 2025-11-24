package com.trading.controller;

import com.trading.model.MarketData;
import com.trading.model.Stock;
import com.trading.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataControllerTest {

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private MarketDataController controller;

    @Test
    void testGetAllStocks_returnsMultipleStocks() {
        // Arrange
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        Stock s1 = new Stock(1L, "AAA", "Company A", "Tech", new BigDecimal("10.00"), ts);
        Stock s2 = new Stock(2L, "BBB", "Company B", "Finance", new BigDecimal("20.00"), ts);
        when(marketDataService.getAllStocks()).thenReturn(Flux.just(s1, s2));

        // Act
        Flux<Stock> result = controller.getAllStocks();

        // Assert
        StepVerifier.create(result)
                .expectNext(s1)
                .expectNext(s2)
                .verifyComplete();

        verify(marketDataService, times(1)).getAllStocks();
        verifyNoMoreInteractions(marketDataService);
    }

    @Test
    void testGetAllStocks_returnsEmptyFlux() {
        // Arrange
        when(marketDataService.getAllStocks()).thenReturn(Flux.empty());

        // Act
        Flux<Stock> result = controller.getAllStocks();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(marketDataService, times(1)).getAllStocks();
    }

    @Test
    void testGetAllStocks_serviceErrorPropagates() {
        // Arrange
        RuntimeException ex = new RuntimeException("service failure");
        when(marketDataService.getAllStocks()).thenReturn(Flux.error(ex));

        // Act
        Flux<Stock> result = controller.getAllStocks();

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals("service failure"))
                .verify();

        verify(marketDataService, times(1)).getAllStocks();
    }

    @Test
    void testGetStockById_returnsStock_and_callsServiceWithCorrectId() {
        // Arrange
        long id = 42L;
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        Stock stock = new Stock(id, "XYZ", "X Corp", "Energy", new BigDecimal("55.50"), ts);
        when(marketDataService.getStockById(id)).thenReturn(Mono.just(stock));

        // Act
        Mono<Stock> result = controller.getStockById(id);

        // Assert
        StepVerifier.create(result)
                .expectNext(stock)
                .verifyComplete();

        verify(marketDataService, times(1)).getStockById(id);
        verifyNoMoreInteractions(marketDataService);
    }

    @Test
    void testGetStockById_returnsEmptyMono() {
        // Arrange
        long id = 100L;
        when(marketDataService.getStockById(id)).thenReturn(Mono.empty());

        // Act
        Mono<Stock> result = controller.getStockById(id);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(marketDataService, times(1)).getStockById(id);
    }

    @Test
    void testGetStockById_serviceErrorPropagates() {
        // Arrange
        long id = 7L;
        IllegalArgumentException ex = new IllegalArgumentException("invalid id");
        when(marketDataService.getStockById(id)).thenReturn(Mono.error(ex));

        // Act
        Mono<Stock> result = controller.getStockById(id);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().equals("invalid id"))
                .verify();

        verify(marketDataService, times(1)).getStockById(id);
    }

    @Test
    void testGetStockBySymbol_returnsStock_and_callsServiceWithCorrectSymbol() {
        // Arrange
        String symbol = "TEST";
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        Stock stock = new Stock(5L, symbol, "Test Co", "Health", new BigDecimal("99.99"), ts);
        when(marketDataService.getStockBySymbol(symbol)).thenReturn(Mono.just(stock));

        // Act
        Mono<Stock> result = controller.getStockBySymbol(symbol);

        // Assert
        StepVerifier.create(result)
                .expectNext(stock)
                .verifyComplete();

        verify(marketDataService, times(1)).getStockBySymbol(symbol);
    }

    @Test
    void testGetStocksBySector_returnsMultipleStocks_and_callsServiceWithCorrectSector() {
        // Arrange
        String sector = "Retail";
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        Stock s1 = new Stock(11L, "R1", "Retail One", sector, new BigDecimal("10.00"), ts);
        Stock s2 = new Stock(12L, "R2", "Retail Two", sector, new BigDecimal("20.00"), ts);
        when(marketDataService.getStocksBySector(sector)).thenReturn(Flux.just(s1, s2));

        // Act
        Flux<Stock> result = controller.getStocksBySector(sector);

        // Assert
        StepVerifier.create(result)
                .expectNext(s1)
                .expectNext(s2)
                .verifyComplete();

        verify(marketDataService, times(1)).getStocksBySector(sector);
    }

    @Test
    void testGetMarketData_returnsMultipleMarketData_and_callsServiceWithCorrectStockId() {
        // Arrange
        long stockId = 2L;
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        MarketData m1 = new MarketData(101L, stockId, new BigDecimal("12.34"), 1000L, ts);
        MarketData m2 = new MarketData(102L, stockId, new BigDecimal("12.50"), 1500L, ts);
        when(marketDataService.getMarketDataForStock(stockId)).thenReturn(Flux.just(m1, m2));

        // Act
        Flux<MarketData> result = controller.getMarketData(stockId);

        // Assert
        StepVerifier.create(result)
                .expectNext(m1)
                .expectNext(m2)
                .verifyComplete();

        verify(marketDataService, times(1)).getMarketDataForStock(stockId);
    }

    @Test
    void testGetLatestMarketData_returnsMarketData_and_callsServiceWithCorrectStockId() {
        // Arrange
        long stockId = 3L;
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        MarketData latest = new MarketData(201L, stockId, new BigDecimal("200.00"), 5000L, ts);
        when(marketDataService.getLatestMarketData(stockId)).thenReturn(Mono.just(latest));

        // Act
        Mono<MarketData> result = controller.getLatestMarketData(stockId);

        // Assert
        StepVerifier.create(result)
                .expectNext(latest)
                .verifyComplete();

        verify(marketDataService, times(1)).getLatestMarketData(stockId);
    }

    @Test
    void testStreamStockPrices_emitsMultipleItems_unit_and_callsServiceWithCorrectStockId() {
        // Arrange
        long stockId = 9L;
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        Stock a = new Stock(stockId, "S1", "S One", "Misc", new BigDecimal("1.00"), ts);
        Stock b = new Stock(stockId, "S1", "S One", "Misc", new BigDecimal("1.10"), ts);
        when(marketDataService.streamPriceUpdates(stockId)).thenReturn(Flux.just(a, b));

        // Act
        Flux<Stock> result = controller.streamStockPrices(stockId);

        // Assert
        StepVerifier.create(result)
                .expectNext(a)
                .expectNext(b)
                .verifyComplete();

        verify(marketDataService, times(1)).streamPriceUpdates(stockId);
    }

    @Test
    void testStreamAllPrices_emitsMultipleItems_unit_and_delegatesToService() {
        // Arrange
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        Stock a = new Stock(1L, "A", "A Co", "S", new BigDecimal("2.00"), ts);
        Stock b = new Stock(2L, "B", "B Co", "S", new BigDecimal("3.00"), ts);
        when(marketDataService.streamAllPrices()).thenReturn(Flux.just(a, b));

        // Act
        Flux<Stock> result = controller.streamAllPrices();

        // Assert
        StepVerifier.create(result)
                .expectNext(a)
                .expectNext(b)
                .verifyComplete();

        verify(marketDataService, times(1)).streamAllPrices();
    }

    @Test
    void testServiceEmitsNullElements_shouldProduceNullPointerException() {
        // Arrange
        // Reactor forbids emitting nulls; simulate a source that attempts to emit null
        Flux<Stock> nullEmitter = Flux.create(sink -> {
            // This will trigger NullPointerException when subscribing
            sink.next(null);
            sink.complete();
        });
        when(marketDataService.getAllStocks()).thenReturn(nullEmitter);

        // Act
        Flux<Stock> result = controller.getAllStocks();

        // Assert
        StepVerifier.create(result)
                .expectError(NullPointerException.class)
                .verify();

        verify(marketDataService, times(1)).getAllStocks();
    }

    @Test
    void testBackpressureBehavior_onFluxEndpoints_takeOneCompletesAfterOneElement() {
        // Arrange
        LocalDateTime ts = LocalDateTime.of(2020, 1, 1, 0, 0);
        Stock s1 = new Stock(21L, "T1", "T One", "S", new BigDecimal("5.00"), ts);
        Stock s2 = new Stock(22L, "T2", "T Two", "S", new BigDecimal("6.00"), ts);
        when(marketDataService.getAllStocks()).thenReturn(Flux.just(s1, s2));

        // Act
        Flux<Stock> limited = controller.getAllStocks().take(1);

        // Assert
        StepVerifier.create(limited)
                .expectNext(s1)
                .verifyComplete();

        verify(marketDataService, times(1)).getAllStocks();
    }
}
