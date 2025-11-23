package com.trading.service;

import com.trading.model.Order;
import com.trading.model.Trade;
import com.trading.repository.OrderRepository;
import com.trading.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PortfolioService portfolioService;
    private final AccountService accountService;

    public Mono<Trade> executeTrade(Long buyOrderId, Long sellOrderId) {
        log.debug("Executing trade between buy order {} and sell order {}", buyOrderId, sellOrderId);

        return Mono.zip(
                orderRepository.findById(buyOrderId),
                orderRepository.findById(sellOrderId)).flatMap(tuple -> {
                    Order buyOrder = tuple.getT1();
                    Order sellOrder = tuple.getT2();

                    // Validate orders
                    if (!buyOrder.getStockId().equals(sellOrder.getStockId())) {
                        return Mono.error(new IllegalArgumentException("Orders are for different stocks"));
                    }
                    if (!"BUY".equals(buyOrder.getSide()) || !"SELL".equals(sellOrder.getSide())) {
                        return Mono.error(new IllegalArgumentException("Invalid order sides"));
                    }
                    if (!"PENDING".equals(buyOrder.getStatus()) || !"PENDING".equals(sellOrder.getStatus())) {
                        return Mono.error(new IllegalStateException("Orders must be in PENDING status"));
                    }

                    // Determine trade quantity and price
                    int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                    BigDecimal tradePrice = sellOrder.getPrice() != null ? sellOrder.getPrice() : buyOrder.getPrice();

                    // Create trade
                    Trade trade = new Trade(buyOrderId, sellOrderId, buyOrder.getStockId(), tradeQuantity, tradePrice);

                    return tradeRepository.save(trade)
                            .flatMap(savedTrade -> {
                                // Update order statuses
                                return Mono.zip(
                                        orderService.updateOrderStatus(buyOrderId, "EXECUTED"),
                                        orderService.updateOrderStatus(sellOrderId, "EXECUTED"))
                                        .then(Mono.just(savedTrade));
                            })
                            .flatMap(savedTrade -> {
                                // Update portfolios
                                return Mono.zip(
                                        portfolioService.updateHoldingAfterBuy(buyOrder.getAccountId(),
                                                buyOrder.getStockId(), tradeQuantity, tradePrice),
                                        portfolioService.updateHoldingAfterSell(sellOrder.getAccountId(),
                                                sellOrder.getStockId(), tradeQuantity, tradePrice))
                                        .then(Mono.just(savedTrade));
                            })
                            .flatMap(savedTrade -> {
                                // Update account balances
                                BigDecimal tradeAmount = tradePrice.multiply(BigDecimal.valueOf(tradeQuantity));

                                return accountService.findById(buyOrder.getAccountId())
                                        .flatMap(buyAccount -> {
                                            BigDecimal newBuyBalance = buyAccount.getBalance().subtract(tradeAmount);
                                            return accountService.updateBalance(buyAccount.getId(), newBuyBalance);
                                        })
                                        .then(accountService.findById(sellOrder.getAccountId()))
                                        .flatMap(sellAccount -> {
                                            BigDecimal newSellBalance = sellAccount.getBalance().add(tradeAmount);
                                            return accountService.updateBalance(sellAccount.getId(), newSellBalance);
                                        })
                                        .then(Mono.just(savedTrade));
                            });
                });
    }

    public Mono<Trade> findById(Long id) {
        return tradeRepository.findById(id);
    }

    public Flux<Trade> findByStockId(Long stockId) {
        return tradeRepository.findByStockId(stockId);
    }

    public Flux<Trade> findRecentTrades(int limit) {
        return tradeRepository.findRecentTrades(limit);
    }

    /**
     * Simple matching engine - matches pending buy and sell orders for the same
     * stock
     */
    public Flux<Trade> matchOrders(Long stockId) {
        log.debug("Running matching engine for stock: {}", stockId);

        return orderRepository.findByStockIdAndStatus(stockId, "PENDING")
                .collectList()
                .flatMapMany(orders -> {
                    // Separate buy and sell orders
                    var buyOrders = orders.stream()
                            .filter(o -> "BUY".equals(o.getSide()))
                            .toList();
                    var sellOrders = orders.stream()
                            .filter(o -> "SELL".equals(o.getSide()))
                            .toList();

                    // Match orders
                    return Flux.fromIterable(buyOrders)
                            .flatMap(buyOrder -> Flux.fromIterable(sellOrders)
                                    .filter(sellOrder -> canMatch(buyOrder, sellOrder))
                                    .next()
                                    .flatMap(sellOrder -> executeTrade(buyOrder.getId(), sellOrder.getId())));
                });
    }

    private boolean canMatch(Order buyOrder, Order sellOrder) {
        // Simple matching logic: prices must be compatible
        if (buyOrder.getPrice() == null || sellOrder.getPrice() == null) {
            return true; // Market orders can match
        }
        return buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0;
    }
}
