package com.trading.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Stock ID is required")
    private Long stockId;

    @NotNull(message = "Order type is required")
    private String orderType; // MARKET, LIMIT, STOP_LOSS

    @NotNull(message = "Side is required")
    private String side; // BUY, SELL

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private BigDecimal price; // Required for LIMIT and STOP_LOSS orders
}
