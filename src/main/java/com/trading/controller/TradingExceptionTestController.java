package com.trading.controller;

import com.trading.dto.CreateOrderRequest;
import com.trading.dto.DepositWithdrawRequest;
import com.trading.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for triggering real exception scenarios in the trading
 * application
 * Tests actual business logic validation and error handling
 */
@Slf4j
@RestController
@RequestMapping("/api/test/trading-exceptions")
@RequiredArgsConstructor
public class TradingExceptionTestController {

    private final UserService userService;
    private final AccountService accountService;
    private final OrderService orderService;
    private final TradeService tradeService;
    private final PortfolioService portfolioService;
    private final WatchlistService watchlistService;

    /**
     * Test duplicate user registration (IllegalArgumentException)
     * GET /api/test/trading-exceptions/duplicate-user
     */
    @GetMapping("/duplicate-user")
    public Mono<Map<String, String>> testDuplicateUserRegistration() {
        log.info("Testing duplicate user registration exception");
        // Try to register a user that already exists (john_trader)
        return userService.registerUser("john_trader", "duplicate@test.com", "password123")
                .map(user -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "User was created when it should have failed");
                    return response;
                });
    }

    /**
     * Test insufficient balance for withdrawal (IllegalArgumentException)
     * GET /api/test/trading-exceptions/insufficient-balance
     */
    @GetMapping("/insufficient-balance")
    public Mono<Map<String, String>> testInsufficientBalance() {
        log.info("Testing insufficient balance exception");
        // Try to withdraw more than available balance from account 1
        DepositWithdrawRequest request = new DepositWithdrawRequest(new BigDecimal("999999.00"));
        return accountService.withdraw(1L, request.getAmount())
                .map(account -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Withdrawal succeeded when it should have failed");
                    return response;
                });
    }

    /**
     * Test account not found (NullPointerException or error)
     * GET /api/test/trading-exceptions/account-not-found
     */
    @GetMapping("/account-not-found")
    public Mono<Map<String, String>> testAccountNotFound() {
        log.info("Testing account not found exception");
        // Try to access non-existent account
        return accountService.findById(99999L)
                .map(account -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("accountNumber", account.getAccountNumber());
                    return response;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // This will trigger if account is not found
                    throw new IllegalArgumentException("Account with ID 99999 not found");
                }));
    }

    /**
     * Test inactive account operation (IllegalStateException)
     * GET /api/test/trading-exceptions/inactive-account
     */
    @GetMapping("/inactive-account")
    public Mono<Map<String, String>> testInactiveAccountOperation() {
        log.info("Testing inactive account operation exception");
        // First, let's try to deposit to a non-existent account which should fail
        // In a real scenario, you'd have an inactive account
        return accountService.findById(99999L)
                .flatMap(account -> {
                    DepositWithdrawRequest request = new DepositWithdrawRequest(new BigDecimal("100.00"));
                    return accountService.deposit(account.getId(), request.getAmount());
                })
                .map(account -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    return response;
                })
                .switchIfEmpty(Mono.error(new IllegalStateException("Account is not active or does not exist")));
    }

    /**
     * Test order placement with insufficient balance (IllegalArgumentException)
     * GET /api/test/trading-exceptions/order-insufficient-balance
     */
    @GetMapping("/order-insufficient-balance")
    public Mono<Map<String, String>> testOrderInsufficientBalance() {
        log.info("Testing order placement with insufficient balance");
        // Try to place a large order that exceeds account balance
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(1L);
        request.setStockId(1L);
        request.setOrderType("MARKET");
        request.setSide("BUY");
        request.setQuantity(1000000); // Very large quantity

        return orderService.placeOrder(request)
                .map(order -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Order placed when it should have failed due to insufficient balance");
                    return response;
                });
    }

    /**
     * Test order placement with invalid stock (IllegalArgumentException)
     * GET /api/test/trading-exceptions/order-invalid-stock
     */
    @GetMapping("/order-invalid-stock")
    public Mono<Map<String, String>> testOrderInvalidStock() {
        log.info("Testing order placement with invalid stock");
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(1L);
        request.setStockId(99999L); // Non-existent stock
        request.setOrderType("MARKET");
        request.setSide("BUY");
        request.setQuantity(10);

        return orderService.placeOrder(request)
                .map(order -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Order placed with invalid stock");
                    return response;
                });
    }

    /**
     * Test order placement with invalid account (IllegalArgumentException)
     * GET /api/test/trading-exceptions/order-invalid-account
     */
    @GetMapping("/order-invalid-account")
    public Mono<Map<String, String>> testOrderInvalidAccount() {
        log.info("Testing order placement with invalid account");
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(99999L); // Non-existent account
        request.setStockId(1L);
        request.setOrderType("MARKET");
        request.setSide("BUY");
        request.setQuantity(10);

        return orderService.placeOrder(request)
                .map(order -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Order placed with invalid account");
                    return response;
                });
    }

    /**
     * Test LIMIT order without price (IllegalArgumentException)
     * GET /api/test/trading-exceptions/limit-order-no-price
     */
    @GetMapping("/limit-order-no-price")
    public Mono<Map<String, String>> testLimitOrderWithoutPrice() {
        log.info("Testing LIMIT order without price");
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(1L);
        request.setStockId(1L);
        request.setOrderType("LIMIT");
        request.setSide("BUY");
        request.setQuantity(10);
        request.setPrice(null); // Missing price for LIMIT order

        return orderService.placeOrder(request)
                .map(order -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "LIMIT order placed without price");
                    return response;
                });
    }

    /**
     * Test cancelling already executed order (IllegalStateException)
     * GET /api/test/trading-exceptions/cancel-executed-order
     */
    @GetMapping("/cancel-executed-order")
    public Mono<Map<String, String>> testCancelExecutedOrder() {
        log.info("Testing cancellation of executed order");
        // Try to cancel an already executed order (order ID 11 from mock data)
        return orderService.cancelOrder(11L)
                .map(order -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Executed order was cancelled");
                    return response;
                });
    }

    /**
     * Test cancelling already cancelled order (IllegalStateException)
     * GET /api/test/trading-exceptions/cancel-cancelled-order
     */
    @GetMapping("/cancel-cancelled-order")
    public Mono<Map<String, String>> testCancelCancelledOrder() {
        log.info("Testing cancellation of already cancelled order");
        // Try to cancel an already cancelled order (order ID 21 from mock data)
        return orderService.cancelOrder(21L)
                .map(order -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Cancelled order was cancelled again");
                    return response;
                });
    }

    /**
     * Test selling more shares than owned (IllegalStateException)
     * GET /api/test/trading-exceptions/sell-insufficient-holdings
     */
    @GetMapping("/sell-insufficient-holdings")
    public Mono<Map<String, String>> testSellInsufficientHoldings() {
        log.info("Testing sell with insufficient holdings");
        // Try to sell more shares than owned
        return portfolioService.updateHoldingAfterSell(1L, 1L, 10000, new BigDecimal("100.00"))
                .map(holding -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Sold more shares than owned");
                    return response;
                });
    }

    /**
     * Test adding non-existent stock to watchlist (IllegalArgumentException)
     * GET /api/test/trading-exceptions/watchlist-invalid-stock
     */
    @GetMapping("/watchlist-invalid-stock")
    public Mono<Map<String, String>> testWatchlistInvalidStock() {
        log.info("Testing adding invalid stock to watchlist");
        // Try to add non-existent stock to watchlist
        return watchlistService.addStockToWatchlist(1L, 99999L)
                .map(item -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Invalid stock added to watchlist");
                    return response;
                });
    }

    /**
     * Test adding stock to non-existent watchlist (IllegalArgumentException)
     * GET /api/test/trading-exceptions/invalid-watchlist
     */
    @GetMapping("/invalid-watchlist")
    public Mono<Map<String, String>> testInvalidWatchlist() {
        log.info("Testing adding stock to invalid watchlist");
        // Try to add stock to non-existent watchlist
        return watchlistService.addStockToWatchlist(99999L, 1L)
                .map(item -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Stock added to non-existent watchlist");
                    return response;
                });
    }

    /**
     * Test trade execution with mismatched stocks (IllegalArgumentException)
     * GET /api/test/trading-exceptions/trade-mismatched-stocks
     */
    @GetMapping("/trade-mismatched-stocks")
    public Mono<Map<String, String>> testTradeMismatchedStocks() {
        log.info("Testing trade execution with mismatched stocks");
        // Try to execute trade between orders for different stocks
        // Order 1 is for stock 14, Order 3 is for stock 15
        return tradeService.executeTrade(1L, 3L)
                .map(trade -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "unexpected");
                    response.put("message", "Trade executed with mismatched stocks");
                    return response;
                });
    }

    /**
     * Get list of all trading exception test endpoints
     * GET /api/test/trading-exceptions/list
     */
    @GetMapping("/list")
    public Mono<Map<String, String>> listTradingExceptionEndpoints() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/test/trading-exceptions/duplicate-user", "Test duplicate user registration (400)");
        endpoints.put("GET /api/test/trading-exceptions/insufficient-balance",
                "Test insufficient balance withdrawal (400)");
        endpoints.put("GET /api/test/trading-exceptions/account-not-found", "Test account not found (400)");
        endpoints.put("GET /api/test/trading-exceptions/inactive-account", "Test inactive account operation (409)");
        endpoints.put("GET /api/test/trading-exceptions/order-insufficient-balance",
                "Test order with insufficient balance (400)");
        endpoints.put("GET /api/test/trading-exceptions/order-invalid-stock", "Test order with invalid stock (400)");
        endpoints.put("GET /api/test/trading-exceptions/order-invalid-account",
                "Test order with invalid account (400)");
        endpoints.put("GET /api/test/trading-exceptions/limit-order-no-price", "Test LIMIT order without price (400)");
        endpoints.put("GET /api/test/trading-exceptions/cancel-executed-order", "Test cancel executed order (409)");
        endpoints.put("GET /api/test/trading-exceptions/cancel-cancelled-order", "Test cancel cancelled order (409)");
        endpoints.put("GET /api/test/trading-exceptions/sell-insufficient-holdings",
                "Test sell with insufficient holdings (409)");
        endpoints.put("GET /api/test/trading-exceptions/watchlist-invalid-stock",
                "Test add invalid stock to watchlist (400)");
        endpoints.put("GET /api/test/trading-exceptions/invalid-watchlist", "Test add to invalid watchlist (400)");
        endpoints.put("GET /api/test/trading-exceptions/trade-mismatched-stocks",
                "Test trade with mismatched stocks (400)");

        return Mono.just(endpoints);
    }

    /**
     * Health check endpoint
     * GET /api/test/trading-exceptions/health
     */
    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Trading exception test controller is ready");
        return Mono.just(response);
    }
}
