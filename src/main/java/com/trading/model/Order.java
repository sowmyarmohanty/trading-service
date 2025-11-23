package com.trading.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class Order {
    
    @Id
    private Long id;
    
    private Long accountId;
    private Long stockId;
    private String orderType; // MARKET, LIMIT, STOP_LOSS
    private String side; // BUY, SELL
    private Integer quantity;
    private BigDecimal price;
    private String status; // PENDING, EXECUTED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Order(Long accountId, Long stockId, String orderType, String side, 
                 Integer quantity, BigDecimal price, String status) {
        this.accountId = accountId;
        this.stockId = stockId;
        this.orderType = orderType;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
