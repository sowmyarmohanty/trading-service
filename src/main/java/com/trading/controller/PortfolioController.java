package com.trading.controller;

import com.trading.dto.HoldingDetail;
import com.trading.dto.PortfolioSummary;
import com.trading.model.PortfolioHolding;
import com.trading.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/account/{accountId}")
    public Flux<PortfolioHolding> getPortfolioHoldings(@PathVariable Long accountId) {
        return portfolioService.getHoldings(accountId);
    }

    @GetMapping("/account/{accountId}/details")
    public Flux<HoldingDetail> getHoldingDetails(@PathVariable Long accountId) {
        return portfolioService.getHoldingDetails(accountId);
    }

    @GetMapping("/account/{accountId}/summary")
    public Mono<PortfolioSummary> getPortfolioSummary(@PathVariable Long accountId) {
        return portfolioService.getPortfolioSummary(accountId);
    }
}
