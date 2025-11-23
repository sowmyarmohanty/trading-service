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
@Table("trades")
public class Trade {
    
    @Id
    private Long id;
    
    private Long buyOrderId;
    private Long sellOrderId;
    private Long stockId;
    private Integer quantity;
    private BigDecimal price;
    private LocalDateTime executedAt;
    
    public Trade(Long buyOrderId, Long sellOrderId, Long stockId, Integer quantity, BigDecimal price) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.stockId = stockId;
        this.quantity = quantity;
        this.price = price;
        this.executedAt = LocalDateTime.now();
    }
}
