package com.cryptosim.service;

import com.cryptosim.model.Trade;
import com.cryptosim.repository.TradeRepository;
import com.cryptosim.service.PnLCalculator.PnLResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class RiskMetricsService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PnLCalculator pnlCalculator;

    public Map<String, Object> getRiskMetrics(Long userId) {
        List<Trade> trades = tradeRepository
                .findByUserIdAndSymbolOrderByExecutedAtAsc(userId, "BTCUSDT");

        List<PnLResult> pnlResults = pnlCalculator.calculate(trades);

        if (pnlResults.isEmpty()) {
            return Map.of("message", "No completed trades found");
        }

        BigDecimal totalPnl = BigDecimal.ZERO;
        BigDecimal totalGain = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        int wins = 0;
        int losses = 0;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal runningPnl = BigDecimal.ZERO;

        for (PnLResult result : pnlResults) {
            runningPnl = runningPnl.add(result.realizedPnl);
            totalPnl = totalPnl.add(result.realizedPnl);

            if (result.realizedPnl.compareTo(BigDecimal.ZERO) > 0) {
                wins++;
                totalGain = totalGain.add(result.realizedPnl);
            } else {
                losses++;
                totalLoss = totalLoss.add(result.realizedPnl.abs());
            }

            // Track max drawdown
            if (runningPnl.compareTo(peak) > 0) {
                peak = runningPnl;
            }
            BigDecimal drawdown = peak.subtract(runningPnl);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }

        int total = wins + losses;
        BigDecimal winRate = total == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(wins)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

        BigDecimal avgGain = wins == 0 ? BigDecimal.ZERO :
                totalGain.divide(BigDecimal.valueOf(wins), 8, RoundingMode.HALF_UP);

        BigDecimal avgLoss = losses == 0 ? BigDecimal.ZERO :
                totalLoss.divide(BigDecimal.valueOf(losses), 8, RoundingMode.HALF_UP);

        // Sharpe Ratio = avgGain / avgLoss (simplified)
        BigDecimal sharpeRatio = avgLoss.compareTo(BigDecimal.ZERO) == 0 ?
                BigDecimal.ZERO :
                avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalTrades", total);
        metrics.put("wins", wins);
        metrics.put("losses", losses);
        metrics.put("winRate", winRate + "%");
        metrics.put("totalPnl", totalPnl);
        metrics.put("avgGain", avgGain);
        metrics.put("avgLoss", avgLoss);
        metrics.put("sharpeRatio", sharpeRatio);
        metrics.put("maxDrawdown", maxDrawdown);

        return metrics;
    }
}