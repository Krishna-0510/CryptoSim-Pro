package com.cryptosim.service;

import com.cryptosim.model.Trade;
import com.cryptosim.repository.TradeRepository;
import com.cryptosim.service.PnLCalculator.PnLResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class PatternAnalyzerService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PnLCalculator pnlCalculator;

    public Map<String, Object> analyzePatterns(Long userId) {
        List<Trade> trades = tradeRepository
                .findByUserIdAndSymbolOrderByExecutedAtAsc(userId, "BTCUSDT");

        List<PnLResult> pnlResults = pnlCalculator.calculate(trades);

        if (pnlResults.isEmpty()) {
            return Map.of("message", "No completed trades to analyze");
        }

        // Group PnL by hour of day (0-23)
        Map<Integer, BigDecimal> pnlByHour = new HashMap<>();
        Map<Integer, Integer> countByHour = new HashMap<>();

        // Group PnL by day of week (1=Mon, 7=Sun)
        Map<String, BigDecimal> pnlByDay = new HashMap<>();
        Map<String, Integer> countByDay = new HashMap<>();

        // Match sell trades with PnL results by index
        List<Trade> sellTrades = new ArrayList<>();
        for (Trade t : trades) {
            if (t.getType().equals("SELL")) sellTrades.add(t);
        }

        for (int i = 0; i < Math.min(sellTrades.size(), pnlResults.size()); i++) {
            Trade trade = sellTrades.get(i);
            BigDecimal pnl = pnlResults.get(i).realizedPnl;

            ZonedDateTime zdt = trade.getExecutedAt()
                    .atZone(ZoneId.of("Asia/Kolkata")); // IST

            int hour = zdt.getHour();
            String day = zdt.getDayOfWeek().toString();

            // Aggregate by hour
            pnlByHour.merge(hour, pnl, BigDecimal::add);
            countByHour.merge(hour, 1, Integer::sum);

            // Aggregate by day
            pnlByDay.merge(day, pnl, BigDecimal::add);
            countByDay.merge(day, 1, Integer::sum);
        }

        // Find best hour
        int bestHour = -1;
        BigDecimal bestHourPnl = null;
        for (Map.Entry<Integer, BigDecimal> entry : pnlByHour.entrySet()) {
            if (bestHourPnl == null ||
                    entry.getValue().compareTo(bestHourPnl) > 0) {
                bestHour = entry.getKey();
                bestHourPnl = entry.getValue();
            }
        }

        // Find best day
        String bestDay = null;
        BigDecimal bestDayPnl = null;
        for (Map.Entry<String, BigDecimal> entry : pnlByDay.entrySet()) {
            if (bestDayPnl == null ||
                    entry.getValue().compareTo(bestDayPnl) > 0) {
                bestDay = entry.getKey();
                bestDayPnl = entry.getValue();
            }
        }

        // Build avg PnL per hour map
        Map<String, Object> hourlyAvg = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) {
            if (pnlByHour.containsKey(h)) {
                BigDecimal avg = pnlByHour.get(h)
                        .divide(BigDecimal.valueOf(countByHour.get(h)),
                                4, RoundingMode.HALF_UP);
                hourlyAvg.put(h + ":00", avg);
            }
        }

        String insight = "You trade best on " + bestDay +
                " around " + bestHour + ":00 IST" +
                " with avg PnL of " + bestHourPnl;

        Map<String, Object> result = new HashMap<>();
        result.put("insight", insight);
        result.put("bestDay", bestDay);
        result.put("bestHour", bestHour + ":00 IST");
        result.put("pnlByDay", pnlByDay);
        result.put("avgPnlByHour", hourlyAvg);

        return result;
    }
}