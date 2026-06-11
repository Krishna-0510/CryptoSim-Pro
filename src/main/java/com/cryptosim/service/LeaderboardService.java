package com.cryptosim.service;

import com.cryptosim.model.Trade;
import com.cryptosim.model.User;
import com.cryptosim.repository.TradeRepository;
import com.cryptosim.repository.UserRepository;
import com.cryptosim.service.PnLCalculator.PnLResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class LeaderboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PnLCalculator pnlCalculator;

    public List<Map<String, Object>> getLeaderboard() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (User user : users) {
            List<Trade> trades = tradeRepository
                    .findByUserIdAndSymbolOrderByExecutedAtAsc(user.getId(), "BTCUSDT");

            List<PnLResult> pnlResults = pnlCalculator.calculate(trades);

            if (pnlResults.isEmpty()) continue;

            // Calculate metrics
            BigDecimal totalPnl = BigDecimal.ZERO;
            int wins = 0;
            int total = pnlResults.size();
            List<BigDecimal> pnlList = new ArrayList<>();

            for (PnLResult result : pnlResults) {
                totalPnl = totalPnl.add(result.realizedPnl);
                pnlList.add(result.realizedPnl);
                if (result.realizedPnl.compareTo(BigDecimal.ZERO) > 0) wins++;
            }

            BigDecimal winRate = BigDecimal.valueOf(wins)
                    .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);

            // Sharpe Ratio
            BigDecimal avgPnl = totalPnl
                    .divide(BigDecimal.valueOf(total), 8, RoundingMode.HALF_UP);
            BigDecimal variance = BigDecimal.ZERO;
            for (BigDecimal pnl : pnlList) {
                BigDecimal diff = pnl.subtract(avgPnl);
                variance = variance.add(diff.multiply(diff));
            }
            variance = variance.divide(BigDecimal.valueOf(total), 8, RoundingMode.HALF_UP);
            BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
            BigDecimal sharpeRatio = stdDev.compareTo(BigDecimal.ZERO) == 0 ?
                    BigDecimal.ZERO :
                    avgPnl.divide(stdDev, 4, RoundingMode.HALF_UP);

            // Consistency — % of trades with positive PnL
            BigDecimal consistency = winRate;

            // Skill Score Formula:
            // skillScore = (realizedPnL x 0.4) + (winRate x 0.3)
            //            + (sharpeRatio x 0.2) + (consistency x 0.1)
            BigDecimal skillScore = totalPnl.multiply(new BigDecimal("0.4"))
                    .add(winRate.multiply(new BigDecimal("0.3")))
                    .add(sharpeRatio.multiply(new BigDecimal("0.2")))
                    .add(consistency.multiply(new BigDecimal("0.1")))
                    .setScale(4, RoundingMode.HALF_UP);

            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", user.getId());
            entry.put("username", user.getUsername());
            entry.put("totalPnl", totalPnl);
            entry.put("winRate", winRate.multiply(BigDecimal.valueOf(100)) + "%");
            entry.put("sharpeRatio", sharpeRatio);
            entry.put("skillScore", skillScore);
            entry.put("totalTrades", total);

            leaderboard.add(entry);
        }

        // Sort by skillScore descending
        leaderboard.sort((a, b) -> {
            BigDecimal scoreA = (BigDecimal) a.get("skillScore");
            BigDecimal scoreB = (BigDecimal) b.get("skillScore");
            return scoreB.compareTo(scoreA);
        });

        return leaderboard;
    }
}