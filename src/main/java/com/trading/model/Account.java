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
@Table("accounts")
public class Account {
    
    @Id
    private Long id;
    
    private Long userId;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType; // CASH, MARGIN
    private String status; // ACTIVE, SUSPENDED, CLOSED
    private LocalDateTime createdAt;
    
    public Account(Long userId, String accountNumber, BigDecimal balance, String accountType, String status) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
