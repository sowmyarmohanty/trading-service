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
@Table("market_data")
public class MarketData {
    
    @Id
    private Long id;
    
    private Long stockId;
    private BigDecimal price;
    private Long volume;
    private LocalDateTime timestamp;
    
    public MarketData(Long stockId, BigDecimal price, Long volume) {
        this.stockId = stockId;
        this.price = price;
        this.volume = volume;
        this.timestamp = LocalDateTime.now();
    }
}
