package com.trading.controller;

import com.trading.dto.CreateOrderRequest;
import com.trading.model.Order;
import com.trading.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/{id}")
    public Mono<Order> getOrderById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/account/{accountId}")
    public Flux<Order> getOrdersByAccountId(@PathVariable Long accountId) {
        return orderService.findByAccountId(accountId);
    }

    @GetMapping("/status/{status}")
    public Flux<Order> getOrdersByStatus(@PathVariable String status) {
        return orderService.findByStatus(status);
    }

    @PutMapping("/{id}/cancel")
    public Mono<Order> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
