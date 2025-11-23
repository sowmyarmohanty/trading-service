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
@Table("portfolio_holdings")
public class PortfolioHolding {
    
    @Id
    private Long id;
    
    private Long accountId;
    private Long stockId;
    private Integer quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentValue;
    private LocalDateTime lastUpdated;
    
    public PortfolioHolding(Long accountId, Long stockId, Integer quantity, BigDecimal averagePrice) {
        this.accountId = accountId;
        this.stockId = stockId;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.lastUpdated = LocalDateTime.now();
    }
}
