package com.trading.repository;

import com.trading.model.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    
    Flux<Order> findByAccountId(Long accountId);
    
    Flux<Order> findByStatus(String status);
    
    Flux<Order> findByAccountIdAndStatus(Long accountId, String status);
    
    @Query("SELECT * FROM orders WHERE stock_id = :stockId AND status = :status ORDER BY created_at DESC")
    Flux<Order> findByStockIdAndStatus(Long stockId, String status);
}
