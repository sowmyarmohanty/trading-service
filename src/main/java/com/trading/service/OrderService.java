package com.trading.service;

import com.trading.dto.CreateOrderRequest;
import com.trading.model.Order;
import com.trading.repository.AccountRepository;
import com.trading.repository.OrderRepository;
import com.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;

    public Mono<Order> placeOrder(CreateOrderRequest request) {
        log.debug("Placing order: {}", request);

        // Validate account exists and is active
        return accountRepository.findById(request.getAccountId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")))
                .flatMap(account -> {
                    if (!"ACTIVE".equals(account.getStatus())) {
                        return Mono.error(new IllegalStateException("Account is not active"));
                    }

                    // Validate stock exists
                    return stockRepository.findById(request.getStockId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Stock not found")))
                            .flatMap(stock -> {
                                // Validate price for LIMIT and STOP_LOSS orders
                                if (("LIMIT".equals(request.getOrderType())
                                        || "STOP_LOSS".equals(request.getOrderType()))
                                        && request.getPrice() == null) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Price is required for LIMIT and STOP_LOSS orders"));
                                }

                                // For MARKET orders, use current stock price
                                BigDecimal orderPrice = "MARKET".equals(request.getOrderType())
                                        ? stock.getCurrentPrice()
                                        : request.getPrice();

                                // Validate sufficient balance for BUY orders
                                if ("BUY".equals(request.getSide())) {
                                    BigDecimal requiredAmount = orderPrice
                                            .multiply(BigDecimal.valueOf(request.getQuantity()));
                                    if (account.getBalance().compareTo(requiredAmount) < 0) {
                                        return Mono.error(new IllegalArgumentException("Insufficient balance"));
                                    }
                                }

                                Order order = new Order(
                                        request.getAccountId(),
                                        request.getStockId(),
                                        request.getOrderType(),
                                        request.getSide(),
                                        request.getQuantity(),
                                        orderPrice,
                                        "PENDING");

                                return orderRepository.save(order);
                            });
                });
    }

    public Mono<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Flux<Order> findByAccountId(Long accountId) {
        return orderRepository.findByAccountId(accountId);
    }

    public Flux<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Mono<Order> cancelOrder(Long orderId) {
        log.debug("Cancelling order: {}", orderId);

        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    if ("EXECUTED".equals(order.getStatus())) {
                        return Mono.error(new IllegalStateException("Cannot cancel executed order"));
                    }
                    if ("CANCELLED".equals(order.getStatus())) {
                        return Mono.error(new IllegalStateException("Order already cancelled"));
                    }

                    order.setStatus("CANCELLED");
                    order.setUpdatedAt(LocalDateTime.now());
                    return orderRepository.save(order);
                });
    }

    public Mono<Order> updateOrderStatus(Long orderId, String status) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    order.setStatus(status);
                    order.setUpdatedAt(LocalDateTime.now());
                    return orderRepository.save(order);
                });
    }
}
