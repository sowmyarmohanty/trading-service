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
@Table("watchlists")
public class Watchlist {
    
    @Id
    private Long id;
    
    private Long userId;
    private String name;
    private LocalDateTime createdAt;
    
    public Watchlist(Long userId, String name) {
        this.userId = userId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }
}
