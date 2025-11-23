package com.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummary {

    private Long accountId;
    private String accountNumber;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal totalProfitLoss;
    private BigDecimal profitLossPercentage;
    private Integer totalPositions;
}
