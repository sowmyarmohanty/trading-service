package com.trading.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("watchlist_items")
public class WatchlistItem {
    
    @Id
    private Long id;
    
    private Long watchlistId;
    private Long stockId;
    private LocalDateTime addedAt;
    
    public WatchlistItem(Long watchlistId, Long stockId) {
        this.watchlistId = watchlistId;
        this.stockId = stockId;
        this.addedAt = LocalDateTime.now();
    }
}
