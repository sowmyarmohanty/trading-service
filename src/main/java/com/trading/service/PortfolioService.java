package com.trading.service;

import com.trading.dto.HoldingDetail;
import com.trading.dto.PortfolioSummary;
import com.trading.model.PortfolioHolding;
import com.trading.repository.AccountRepository;
import com.trading.repository.PortfolioHoldingRepository;
import com.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;

    public Flux<PortfolioHolding> getHoldings(Long accountId) {
        return portfolioHoldingRepository.findByAccountId(accountId);
    }

    public Flux<HoldingDetail> getHoldingDetails(Long accountId) {
        return portfolioHoldingRepository.findByAccountId(accountId)
                .flatMap(holding -> stockRepository.findById(holding.getStockId())
                        .map(stock -> {
                            BigDecimal currentValue = stock.getCurrentPrice()
                                    .multiply(BigDecimal.valueOf(holding.getQuantity()));
                            BigDecimal cost = holding.getAveragePrice()
                                    .multiply(BigDecimal.valueOf(holding.getQuantity()));
                            BigDecimal profitLoss = currentValue.subtract(cost);
                            BigDecimal profitLossPercentage = cost.compareTo(BigDecimal.ZERO) > 0
                                    ? profitLoss.divide(cost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                    : BigDecimal.ZERO;

                            return new HoldingDetail(
                                    stock.getId(),
                                    stock.getSymbol(),
                                    stock.getName(),
                                    holding.getQuantity(),
                                    holding.getAveragePrice(),
                                    stock.getCurrentPrice(),
                                    currentValue,
                                    profitLoss,
                                    profitLossPercentage);
                        }));
    }

    public Mono<PortfolioSummary> getPortfolioSummary(Long accountId) {
        return accountRepository.findById(accountId)
                .flatMap(account -> getHoldingDetails(accountId)
                        .collectList()
                        .map(holdings -> {
                            BigDecimal totalValue = holdings.stream()
                                    .map(HoldingDetail::getCurrentValue)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal totalCost = holdings.stream()
                                    .map(h -> h.getAveragePrice().multiply(BigDecimal.valueOf(h.getQuantity())))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal totalProfitLoss = totalValue.subtract(totalCost);

                            BigDecimal profitLossPercentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                                    ? totalProfitLoss.divide(totalCost, 4, RoundingMode.HALF_UP)
                                            .multiply(BigDecimal.valueOf(100))
                                    : BigDecimal.ZERO;

                            return new PortfolioSummary(
                                    account.getId(),
                                    account.getAccountNumber(),
                                    totalValue,
                                    totalCost,
                                    totalProfitLoss,
                                    profitLossPercentage,
                                    holdings.size());
                        }));
    }

    public Mono<PortfolioHolding> updateHoldingAfterBuy(Long accountId, Long stockId, Integer quantity,
            BigDecimal price) {
        log.debug("Updating portfolio after BUY: account={}, stock={}, qty={}, price={}", accountId, stockId, quantity,
                price);

        return portfolioHoldingRepository.findByAccountIdAndStockId(accountId, stockId)
                .flatMap(existingHolding -> {
                    // Update existing holding
                    int newQuantity = existingHolding.getQuantity() + quantity;
                    BigDecimal totalCost = existingHolding.getAveragePrice()
                            .multiply(BigDecimal.valueOf(existingHolding.getQuantity()))
                            .add(price.multiply(BigDecimal.valueOf(quantity)));
                    BigDecimal newAveragePrice = totalCost.divide(BigDecimal.valueOf(newQuantity), 2,
                            RoundingMode.HALF_UP);

                    existingHolding.setQuantity(newQuantity);
                    existingHolding.setAveragePrice(newAveragePrice);

                    return portfolioHoldingRepository.save(existingHolding);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Create new holding
                    PortfolioHolding newHolding = new PortfolioHolding(accountId, stockId, quantity, price);
                    return portfolioHoldingRepository.save(newHolding);
                }));
    }

    public Mono<PortfolioHolding> updateHoldingAfterSell(Long accountId, Long stockId, Integer quantity,
            BigDecimal price) {
        log.debug("Updating portfolio after SELL: account={}, stock={}, qty={}, price={}", accountId, stockId, quantity,
                price);

        return portfolioHoldingRepository.findByAccountIdAndStockId(accountId, stockId)
                .flatMap(existingHolding -> {
                    int newQuantity = existingHolding.getQuantity() - quantity;

                    if (newQuantity < 0) {
                        return Mono.error(new IllegalStateException("Insufficient holdings to sell"));
                    }

                    if (newQuantity == 0) {
                        // Delete holding if quantity becomes zero
                        return portfolioHoldingRepository.delete(existingHolding)
                                .then(Mono.just(existingHolding));
                    } else {
                        // Update quantity
                        existingHolding.setQuantity(newQuantity);
                        return portfolioHoldingRepository.save(existingHolding);
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalStateException("No holdings found to sell")));
    }
}
