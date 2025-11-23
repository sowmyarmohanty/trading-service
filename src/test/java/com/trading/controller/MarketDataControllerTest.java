package com.trading.controller;

import com.trading.model.MarketData;
import com.trading.model.Stock;
import com.trading.service.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDataControllerTest {

    private static final Long STOCK_ID = 42L;
    private static final Long OTHER_ID = 99L;
    private static final String SYMBOL = "ABC";
    private static final String SYMBOL_SPECIAL = "@#€-123";
    private static final String SECTOR = "Technology";

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private MarketDataController controller;

    private Stock sampleStock1;
    private Stock sampleStock2;
    private MarketData sampleMarketData1;

    @BeforeEach
    void setUp() {
        sampleStock1 = new Stock(STOCK_ID, SYMBOL, "Company A", SECTOR,
                BigDecimal.valueOf(100), LocalDateTime.now());
        sampleStock2 = new Stock(OTHER_ID, "XYZ", "Company B", "Finance",
                BigDecimal.valueOf(200), LocalDateTime.now());
        sampleMarketData1 = new MarketData(1L, STOCK_ID, BigDecimal.valueOf(101), 1000L, LocalDateTime.now());
    }

    @Test
    void testGetAllStocks_returnsMultipleStocks() {
        // Arrange
        when(marketDataService.getAllStocks()).thenReturn(Flux.just(sampleStock1, sampleStock2));

        // Act
        Flux<Stock> result = controller.getAllStocks();

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .expectNext(sampleStock2)
                .verifyComplete();
        verify(marketDataService, times(1)).getAllStocks();
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
    void testGetAllStocks_serviceErrorIsPropagated() {
        // Arrange
        RuntimeException error = new RuntimeException("service failure");
        when(marketDataService.getAllStocks()).thenReturn(Flux.error(error));

        // Act
        Flux<Stock> result = controller.getAllStocks();

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("service failure"))
                .verify();
        verify(marketDataService, times(1)).getAllStocks();
    }

    @Test
    void testGetAllStocks_serviceReturnsNull_shouldReturnNull() {
        // Arrange
        when(marketDataService.getAllStocks()).thenReturn(null);

        // Act
        Flux<Stock> result = controller.getAllStocks();

        // Assert
        assertNull(result);
        verify(marketDataService, times(1)).getAllStocks();
    }

    @Test
    void testGetStockById_returnsStock() {
        // Arrange
        when(marketDataService.getStockById(eq(STOCK_ID))).thenReturn(Mono.just(sampleStock1));

        // Act
        Mono<Stock> result = controller.getStockById(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .verifyComplete();
        verify(marketDataService, times(1)).getStockById(eq(STOCK_ID));
    }

    @Test
    void testGetStockById_notFound_returnsEmptyMono() {
        // Arrange
        when(marketDataService.getStockById(eq(STOCK_ID))).thenReturn(Mono.empty());

        // Act
        Mono<Stock> result = controller.getStockById(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(marketDataService, times(1)).getStockById(eq(STOCK_ID));
    }

    @Test
    void testGetStockById_serviceErrorIsPropagated() {
        // Arrange
        when(marketDataService.getStockById(eq(STOCK_ID))).thenReturn(Mono.error(new IllegalArgumentException("bad id")));

        // Act
        Mono<Stock> result = controller.getStockById(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException && ex.getMessage().equals("bad id"))
                .verify();
        verify(marketDataService, times(1)).getStockById(eq(STOCK_ID));
    }

    @Test
    void testGetStockById_withNullId_callsServiceWithNull() {
        // Arrange
        when(marketDataService.getStockById(null)).thenReturn(Mono.empty());

        // Act
        Mono<Stock> result = controller.getStockById(null);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(marketDataService, times(1)).getStockById(null);
    }

    @Test
    void testGetStockBySymbol_returnsStock() {
        // Arrange
        when(marketDataService.getStockBySymbol(eq(SYMBOL))).thenReturn(Mono.just(sampleStock1));

        // Act
        Mono<Stock> result = controller.getStockBySymbol(SYMBOL);

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .verifyComplete();
        verify(marketDataService, times(1)).getStockBySymbol(eq(SYMBOL));
    }

    @Test
    void testGetStockBySymbol_notFound_returnsEmptyMono() {
        // Arrange
        when(marketDataService.getStockBySymbol(eq(SYMBOL))).thenReturn(Mono.empty());

        // Act
        Mono<Stock> result = controller.getStockBySymbol(SYMBOL);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(marketDataService, times(1)).getStockBySymbol(eq(SYMBOL));
    }

    @Test
    void testGetStockBySymbol_serviceErrorIsPropagated() {
        // Arrange
        when(marketDataService.getStockBySymbol(eq(SYMBOL))).thenReturn(Mono.error(new RuntimeException("symbol err")));

        // Act
        Mono<Stock> result = controller.getStockBySymbol(SYMBOL);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("symbol err"))
                .verify();
        verify(marketDataService, times(1)).getStockBySymbol(eq(SYMBOL));
    }

    @Test
    void testGetStockBySymbol_withSpecialCharacters_symbolPassedThrough() {
        // Arrange
        when(marketDataService.getStockBySymbol(eq(SYMBOL_SPECIAL))).thenReturn(Mono.just(sampleStock1));

        // Act
        Mono<Stock> result = controller.getStockBySymbol(SYMBOL_SPECIAL);

        // Assert
        StepVerifier.create(result).expectNext(sampleStock1).verifyComplete();
        verify(marketDataService, times(1)).getStockBySymbol(eq(SYMBOL_SPECIAL));
    }

    @Test
    void testGetStocksBySector_returnsMultipleStocks() {
        // Arrange
        when(marketDataService.getStocksBySector(eq(SECTOR))).thenReturn(Flux.just(sampleStock1));

        // Act
        Flux<Stock> result = controller.getStocksBySector(SECTOR);

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .verifyComplete();
        verify(marketDataService, times(1)).getStocksBySector(eq(SECTOR));
    }

    @Test
    void testGetStocksBySector_returnsEmptyFlux() {
        // Arrange
        when(marketDataService.getStocksBySector(eq(SECTOR))).thenReturn(Flux.empty());

        // Act
        Flux<Stock> result = controller.getStocksBySector(SECTOR);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(marketDataService, times(1)).getStocksBySector(eq(SECTOR));
    }

    @Test
    void testGetStocksBySector_serviceErrorIsPropagated() {
        // Arrange
        when(marketDataService.getStocksBySector(eq(SECTOR))).thenReturn(Flux.error(new RuntimeException("sector error")));

        // Act
        Flux<Stock> result = controller.getStocksBySector(SECTOR);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("sector error"))
                .verify();
        verify(marketDataService, times(1)).getStocksBySector(eq(SECTOR));
    }

    @Test
    void testGetMarketData_returnsMultipleMarketDataPoints() {
        // Arrange
        MarketData md1 = new MarketData(10L, STOCK_ID, BigDecimal.valueOf(101), 500L, LocalDateTime.now());
        MarketData md2 = new MarketData(11L, STOCK_ID, BigDecimal.valueOf(102), 600L, LocalDateTime.now());
        when(marketDataService.getMarketDataForStock(eq(STOCK_ID))).thenReturn(Flux.just(md1, md2));

        // Act
        Flux<MarketData> result = controller.getMarketData(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectNext(md1)
                .expectNext(md2)
                .verifyComplete();
        verify(marketDataService, times(1)).getMarketDataForStock(eq(STOCK_ID));
    }

    @Test
    void testGetMarketData_returnsEmptyFlux() {
        // Arrange
        when(marketDataService.getMarketDataForStock(eq(STOCK_ID))).thenReturn(Flux.empty());

        // Act
        Flux<MarketData> result = controller.getMarketData(STOCK_ID);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(marketDataService, times(1)).getMarketDataForStock(eq(STOCK_ID));
    }

    @Test
    void testGetMarketData_serviceErrorIsPropagated() {
        // Arrange
        when(marketDataService.getMarketDataForStock(eq(STOCK_ID))).thenReturn(Flux.error(new RuntimeException("md error")));

        // Act
        Flux<MarketData> result = controller.getMarketData(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("md error"))
                .verify();
        verify(marketDataService, times(1)).getMarketDataForStock(eq(STOCK_ID));
    }

    @Test
    void testGetMarketData_withNullStockId_callsServiceWithNull() {
        // Arrange
        when(marketDataService.getMarketDataForStock(null)).thenReturn(Flux.empty());

        // Act
        Flux<MarketData> result = controller.getMarketData(null);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(marketDataService, times(1)).getMarketDataForStock(null);
    }

    @Test
    void testGetLatestMarketData_returnsMarketData() {
        // Arrange
        MarketData latest = new MarketData(20L, STOCK_ID, BigDecimal.valueOf(150), 2000L, LocalDateTime.now());
        when(marketDataService.getLatestMarketData(eq(STOCK_ID))).thenReturn(Mono.just(latest));

        // Act
        Mono<MarketData> result = controller.getLatestMarketData(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectNext(latest)
                .verifyComplete();
        verify(marketDataService, times(1)).getLatestMarketData(eq(STOCK_ID));
    }

    @Test
    void testGetLatestMarketData_notFound_returnsEmptyMono() {
        // Arrange
        when(marketDataService.getLatestMarketData(eq(STOCK_ID))).thenReturn(Mono.empty());

        // Act
        Mono<MarketData> result = controller.getLatestMarketData(STOCK_ID);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(marketDataService, times(1)).getLatestMarketData(eq(STOCK_ID));
    }

    @Test
    void testGetLatestMarketData_serviceErrorIsPropagated() {
        // Arrange
        when(marketDataService.getLatestMarketData(eq(STOCK_ID))).thenReturn(Mono.error(new RuntimeException("latest error")));

        // Act
        Mono<MarketData> result = controller.getLatestMarketData(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("latest error"))
                .verify();
        verify(marketDataService, times(1)).getLatestMarketData(eq(STOCK_ID));
    }

    @Test
    void testStreamStockPrices_streamEmitsValuesAndCompletes() {
        // Arrange
        when(marketDataService.streamPriceUpdates(eq(STOCK_ID))).thenReturn(Flux.just(sampleStock1, sampleStock2));

        // Act
        Flux<Stock> result = controller.streamStockPrices(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .expectNext(sampleStock2)
                .verifyComplete();
        verify(marketDataService, times(1)).streamPriceUpdates(eq(STOCK_ID));
    }

    @Test
    void testStreamStockPrices_streamIsInfiniteOrLongLived_verifyInitialValuesAndVerifyServiceCalled() {
        // Arrange
        // Simulate a long-lived stream that emits an initial value then continues (we'll cancel after the first value)
        when(marketDataService.streamPriceUpdates(eq(STOCK_ID))).thenReturn(Flux.concat(Flux.just(sampleStock1), Flux.never()));

        // Act
        Flux<Stock> result = controller.streamStockPrices(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .thenCancel()
                .verify();
        verify(marketDataService, times(1)).streamPriceUpdates(eq(STOCK_ID));
    }

    @Test
    void testStreamStockPrices_serviceErrorIsPropagated() {
        // Arrange
        when(marketDataService.streamPriceUpdates(eq(STOCK_ID))).thenReturn(Flux.error(new RuntimeException("stream err")));

        // Act
        Flux<Stock> result = controller.streamStockPrices(STOCK_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("stream err"))
                .verify();
        verify(marketDataService, times(1)).streamPriceUpdates(eq(STOCK_ID));
    }

    @Test
    void testStreamAllPrices_streamEmitsValuesAndCompletes() {
        // Arrange
        when(marketDataService.streamAllPrices()).thenReturn(Flux.just(sampleStock1, sampleStock2));

        // Act
        Flux<Stock> result = controller.streamAllPrices();

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .expectNext(sampleStock2)
                .verifyComplete();
        verify(marketDataService, times(1)).streamAllPrices();
    }

    @Test
    void testStreamAllPrices_streamIsInfiniteOrLongLived_verifyInitialValuesAndVerifyServiceCalled() {
        // Arrange
        when(marketDataService.streamAllPrices()).thenReturn(Flux.concat(Flux.just(sampleStock1), Flux.never()));

        // Act
        Flux<Stock> result = controller.streamAllPrices();

        // Assert
        StepVerifier.create(result)
                .expectNext(sampleStock1)
                .thenCancel()
                .verify();
        verify(marketDataService, times(1)).streamAllPrices();
    }

    @Test
    void testStreamAllPrices_serviceErrorIsPropagated() {
        // Arrange
        when(marketDataService.streamAllPrices()).thenReturn(Flux.error(new RuntimeException("all stream err")));

        // Act
        Flux<Stock> result = controller.streamAllPrices();

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("all stream err"))
                .verify();
        verify(marketDataService, times(1)).streamAllPrices();
    }

    @Test
    void verifyEachControllerMethod_invokesExpectedServiceMethodWithCorrectArgument() {
        // Arrange: stub each service method with benign empty publishers
        when(marketDataService.getAllStocks()).thenReturn(Flux.empty());
        when(marketDataService.getStockById(eq(STOCK_ID))).thenReturn(Mono.empty());
        when(marketDataService.getStockBySymbol(eq(SYMBOL))).thenReturn(Mono.empty());
        when(marketDataService.getStocksBySector(eq(SECTOR))).thenReturn(Flux.empty());
        when(marketDataService.getMarketDataForStock(eq(STOCK_ID))).thenReturn(Flux.empty());
        when(marketDataService.getLatestMarketData(eq(STOCK_ID))).thenReturn(Mono.empty());
        when(marketDataService.streamPriceUpdates(eq(STOCK_ID))).thenReturn(Flux.empty());
        when(marketDataService.streamAllPrices()).thenReturn(Flux.empty());

        // Act: call controller methods (no need to subscribe)
        controller.getAllStocks();
        controller.getStockById(STOCK_ID);
        controller.getStockBySymbol(SYMBOL);
        controller.getStocksBySector(SECTOR);
        controller.getMarketData(STOCK_ID);
        controller.getLatestMarketData(STOCK_ID);
        controller.streamStockPrices(STOCK_ID);
        controller.streamAllPrices();

        // Assert: verify each service method was invoked with expected arguments
        verify(marketDataService, times(1)).getAllStocks();
        verify(marketDataService, times(1)).getStockById(eq(STOCK_ID));
        verify(marketDataService, times(1)).getStockBySymbol(eq(SYMBOL));
        verify(marketDataService, times(1)).getStocksBySector(eq(SECTOR));
        verify(marketDataService, times(1)).getMarketDataForStock(eq(STOCK_ID));
        verify(marketDataService, times(1)).getLatestMarketData(eq(STOCK_ID));
        verify(marketDataService, times(1)).streamPriceUpdates(eq(STOCK_ID));
        verify(marketDataService, times(1)).streamAllPrices();
    }
}
