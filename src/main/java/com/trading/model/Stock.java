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
@Table("stocks")
public class Stock {
    
    @Id
    private Long id;
    
    private String symbol;
    private String name;
    private String sector;
    private BigDecimal currentPrice;
    private LocalDateTime lastUpdated;
    
    public Stock(String symbol, String name, String sector, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.sector = sector;
        this.currentPrice = currentPrice;
        this.lastUpdated = LocalDateTime.now();
    }
}
