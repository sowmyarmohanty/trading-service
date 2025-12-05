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
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataControllerTest {

    private static final Long STOCK_ID_1 = 1L;
    private static final Long STOCK_ID_2 = 2L;
    private static final String SYMBOL_AAPL = "AAPL";
    private static final String SYMBOL_NONE = "NONE";
    private static final String SECTOR_TECH = "Tech";

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private MarketDataController controller;

    private WebTestClient webClient;

    private Stock stock1;
    private Stock stock2;
    private MarketData md1;
    private MarketData md2;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToController(controller).build();

        stock1 = new Stock(STOCK_ID_1, SYMBOL_AAPL, "Apple Inc.", SECTOR_TECH,
                new BigDecimal("150.00"), LocalDateTime.of(2020, 1, 1, 0, 0));
        stock2 = new Stock(STOCK_ID_2, "GOOGL", "Alphabet Inc.", SECTOR_TECH,
                new BigDecimal("2800.00"), LocalDateTime.of(2020, 1, 1, 0, 0));

        md1 = new MarketData(10L, STOCK_ID_1, new BigDecimal("149.50"), 1000L, LocalDateTime.of(2020, 1, 1, 0, 0));
        md2 = new MarketData(11L, STOCK_ID_1, new BigDecimal("150.50"), 1500L, LocalDateTime.of(2020, 1, 1, 0, 1));
    }

    @Test
    void testGetAllStocks_returnsStocks_andDelegatesToService() {
        // Arrange
        when(marketDataService.getAllStocks()).thenReturn(Flux.just(stock1, stock2));

        // Act
        Flux<Stock> resultFlux = controller.getAllStocks();

        // Assert (reactive-level)
        StepVerifier.create(resultFlux)
                .expectNext(stock1)
                .expectNext(stock2)
                .verifyComplete();

        // Also verify HTTP mapping and JSON mapping
        webClient.get()
                .uri("/api/market/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(Stock.class).hasSize(2).contains(stock1, stock2);

        verify(marketDataService, times(2)).getAllStocks(); // once for direct call, once for WebTestClient
    }

    @Test
    void testGetAllStocks_empty() {
        // Arrange
        when(marketDataService.getAllStocks()).thenReturn(Flux.empty());

        // Act & Assert (reactive)
        StepVerifier.create(controller.getAllStocks())
                .verifyComplete();

        // HTTP-level: empty list
        webClient.get()
                .uri("/api/market/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Stock.class).hasSize(0);

        verify(marketDataService, times(2)).getAllStocks();
    }

    @Test
    void testGetAllStocks_serviceError_propagates() {
        // Arrange
        RuntimeException ex = new RuntimeException("boom");
        when(marketDataService.getAllStocks()).thenReturn(Flux.error(ex));

        // Act & Assert (reactive)
        StepVerifier.create(controller.getAllStocks())
                .expectErrorMatches(err -> err instanceof RuntimeException && err.getMessage().equals("boom"))
                .verify();

        // HTTP-level should return 5xx
        webClient.get()
                .uri("/api/market/stocks")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(marketDataService, times(2)).getAllStocks();
    }

    @Test
    void testGetStockById_found_andDelegation() {
        // Arrange
        when(marketDataService.getStockById(eq(STOCK_ID_1))).thenReturn(Mono.just(stock1));

        // Act
        Mono<Stock> mono = controller.getStockById(STOCK_ID_1);

        // Assert (reactive)
        StepVerifier.create(mono)
                .expectNext(stock1)
                .verifyComplete();

        // HTTP-level
        webClient.get()
                .uri("/api/market/stocks/{id}", STOCK_ID_1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Stock.class).isEqualTo(stock1);

        verify(marketDataService, times(2)).getStockById(eq(STOCK_ID_1));
    }

    @Test
    void testGetStockById_notFound_monoEmpty() {
        // Arrange
        when(marketDataService.getStockById(eq(999L))).thenReturn(Mono.empty());

        // Act & Assert (reactive)
        StepVerifier.create(controller.getStockById(999L))
                .verifyComplete();

        // HTTP-level: returns 200 with no body
        webClient.get()
                .uri("/api/market/stocks/{id}", 999L)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(marketDataService, times(2)).getStockById(eq(999L));
    }

    @Test
    void testGetStockById_serviceError_propagates() {
        // Arrange
        when(marketDataService.getStockById(eq(STOCK_ID_1))).thenReturn(Mono.error(new RuntimeException("db")));

        // Act & Assert (reactive)
        StepVerifier.create(controller.getStockById(STOCK_ID_1))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("db"))
                .verify();

        // HTTP-level
        webClient.get()
                .uri("/api/market/stocks/{id}", STOCK_ID_1)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(marketDataService, times(2)).getStockById(eq(STOCK_ID_1));
    }

    @Test
    void testGetStockById_invalidPathVariableType_returnsBadRequest() {
        // Arrange - no stubbing required

        // Act & Assert: invalid (non-numeric) id should yield 400
        webClient.get()
                .uri("/api/market/stocks/invalidString")
                .exchange()
                .expectStatus().isBadRequest();

        // service should not be called
        verifyNoInteractions(marketDataService);
    }

    @Test
    void testGetStockBySymbol_found_and_notFound_and_serviceError() {
        // Arrange
        when(marketDataService.getStockBySymbol(eq(SYMBOL_AAPL))).thenReturn(Mono.just(stock1));
        when(marketDataService.getStockBySymbol(eq(SYMBOL_NONE))).thenReturn(Mono.empty());
        when(marketDataService.getStockBySymbol(eq("ERR"))).thenReturn(Mono.error(new RuntimeException("err")));

        // Found
        StepVerifier.create(controller.getStockBySymbol(SYMBOL_AAPL))
                .expectNext(stock1)
                .verifyComplete();
        webClient.get().uri("/api/market/stocks/symbol/{symbol}", SYMBOL_AAPL)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Stock.class).isEqualTo(stock1);

        // Not found
        StepVerifier.create(controller.getStockBySymbol(SYMBOL_NONE))
                .verifyComplete();
        webClient.get().uri("/api/market/stocks/symbol/{symbol}", SYMBOL_NONE)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        // Service error
        StepVerifier.create(controller.getStockBySymbol("ERR"))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("err"))
                .verify();
        webClient.get().uri("/api/market/stocks/symbol/{symbol}", "ERR")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(marketDataService, times(2)).getStockBySymbol(eq(SYMBOL_AAPL)); // one StepVerifier + one WebTestClient
        verify(marketDataService, times(2)).getStockBySymbol(eq(SYMBOL_NONE));
        verify(marketDataService, times(2)).getStockBySymbol(eq("ERR"));
    }

    @Test
    void testGetStocksBySector_returnsMultiple_andEmpty() {
        // Arrange
        when(marketDataService.getStocksBySector(eq(SECTOR_TECH))).thenReturn(Flux.just(stock1, stock2));
        when(marketDataService.getStocksBySector(eq("Unknown"))).thenReturn(Flux.empty());

        // Multiple
        StepVerifier.create(controller.getStocksBySector(SECTOR_TECH))
                .expectNext(stock1)
                .expectNext(stock2)
                .verifyComplete();

        webClient.get().uri("/api/market/stocks/sector/{sector}", SECTOR_TECH)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Stock.class).hasSize(2).contains(stock1, stock2);

        // Empty
        StepVerifier.create(controller.getStocksBySector("Unknown")).verifyComplete();
        webClient.get().uri("/api/market/stocks/sector/{sector}", "Unknown")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Stock.class).hasSize(0);

        verify(marketDataService, atLeastOnce()).getStocksBySector(eq(SECTOR_TECH));
        verify(marketDataService, atLeastOnce()).getStocksBySector(eq("Unknown"));
    }

    @Test
    void testGetMarketData_andGetLatestMarketData_variants() {
        // Arrange
        when(marketDataService.getMarketDataForStock(eq(STOCK_ID_1))).thenReturn(Flux.just(md1, md2));
        when(marketDataService.getMarketDataForStock(eq(999L))).thenReturn(Flux.empty());
        when(marketDataService.getLatestMarketData(eq(STOCK_ID_1))).thenReturn(Mono.just(md2));
        when(marketDataService.getLatestMarketData(eq(999L))).thenReturn(Mono.empty());

        // Multiple market data
        StepVerifier.create(controller.getMarketData(STOCK_ID_1))
                .expectNext(md1)
                .expectNext(md2)
                .verifyComplete();
        webClient.get().uri("/api/market/data/{stockId}", STOCK_ID_1)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MarketData.class).hasSize(2);

        // Empty market data
        StepVerifier.create(controller.getMarketData(999L)).verifyComplete();
        webClient.get().uri("/api/market/data/{stockId}", 999L)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MarketData.class).hasSize(0);

        // Latest found
        StepVerifier.create(controller.getLatestMarketData(STOCK_ID_1))
                .expectNext(md2)
                .verifyComplete();
        webClient.get().uri("/api/market/data/{stockId}/latest", STOCK_ID_1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MarketData.class).isEqualTo(md2);

        // Latest not found
        StepVerifier.create(controller.getLatestMarketData(999L)).verifyComplete();
        webClient.get().uri("/api/market/data/{stockId}/latest", 999L)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(marketDataService, atLeastOnce()).getMarketDataForStock(eq(STOCK_ID_1));
        verify(marketDataService, atLeastOnce()).getMarketDataForStock(eq(999L));
        verify(marketDataService, atLeastOnce()).getLatestMarketData(eq(STOCK_ID_1));
        verify(marketDataService, atLeastOnce()).getLatestMarketData(eq(999L));
    }

    @Test
    void testStreamStockPrices_streamsValues_and_contentType_and_empty_and_error() {
        // Arrange: happy path
        when(marketDataService.streamPriceUpdates(eq(STOCK_ID_1))).thenReturn(Flux.just(stock1, stock2));

        // Act & Assert: content-type and body
        webClient.get()
                .uri("/api/market/stocks/{stockId}/stream", STOCK_ID_1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(Stock.class).hasSize(2).contains(stock1, stock2);

        verify(marketDataService, times(1)).streamPriceUpdates(eq(STOCK_ID_1));

        // Arrange: empty stream
        when(marketDataService.streamPriceUpdates(eq(999L))).thenReturn(Flux.empty());

        webClient.get()
                .uri("/api/market/stocks/{stockId}/stream", 999L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(Stock.class).hasSize(0);

        // Arrange: service error
        when(marketDataService.streamPriceUpdates(eq(2L))).thenReturn(Flux.error(new RuntimeException("streamErr")));

        webClient.get()
                .uri("/api/market/stocks/{stockId}/stream", 2L)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(marketDataService, times(1)).streamPriceUpdates(eq(2L));
    }

    @Test
    void testStreamAllPrices_streamsValues_and_empty_and_error() {
        // Arrange
        when(marketDataService.streamAllPrices()).thenReturn(Flux.just(stock1));

        // Act & Assert
        webClient.get()
                .uri("/api/market/stocks/stream/all")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(Stock.class).hasSize(1).contains(stock1);

        verify(marketDataService, times(1)).streamAllPrices();

        // Empty
        when(marketDataService.streamAllPrices()).thenReturn(Flux.empty());
        webClient.get().uri("/api/market/stocks/stream/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Stock.class).hasSize(0);

        // Error
        when(marketDataService.streamAllPrices()).thenReturn(Flux.error(new RuntimeException("allErr")));
        webClient.get().uri("/api/market/stocks/stream/all")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(marketDataService, atLeast(3)).streamAllPrices();
    }

    @Test
    void testControllerDelegation_verifyServiceMethodCalled_directInvocation() {
        // Arrange
        when(marketDataService.getStockById(eq(STOCK_ID_1))).thenReturn(Mono.just(stock1));

        // Act
        Mono<Stock> result = controller.getStockById(STOCK_ID_1);

        // Assert
        StepVerifier.create(result)
                .expectNext(stock1)
                .verifyComplete();

        verify(marketDataService, times(1)).getStockById(eq(STOCK_ID_1));
    }

    @Test
    void testServiceReturnsNull_unexpectedBehavior() {
        // Arrange: misbehaving service returns null instead of a Flux/Mono
        when(marketDataService.getAllStocks()).thenReturn(null);

        // Act
        Object returned = controller.getAllStocks();

        // Assert: controller simply returns null reference; document unexpected behavior
        assertNull(returned, "Controller returned null when service returned null - unexpected but documented behavior");

        verify(marketDataService, times(1)).getAllStocks();
    }
}
