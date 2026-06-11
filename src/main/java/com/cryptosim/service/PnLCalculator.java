package com.cryptosim.service;

import com.cryptosim.model.Trade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Component
public class PnLCalculator {

    // Holds result for each SELL trade
    public static class PnLResult {
        public Long tradeId;
        public String symbol;
        public BigDecimal realizedPnl;
        public BigDecimal sellPrice;
        public BigDecimal quantity;

        public PnLResult(Long tradeId, String symbol,
                         BigDecimal realizedPnl, BigDecimal sellPrice,
                         BigDecimal quantity) {
            this.tradeId = tradeId;
            this.symbol = symbol;
            this.realizedPnl = realizedPnl;
            this.sellPrice = sellPrice;
            this.quantity = quantity;
        }
    }

    // FIFO PnL calculation
    // BUY 1 BTC @60k, BUY 1 BTC @65k, SELL 2 @70k = $15k PnL
    public List<PnLResult> calculate(List<Trade> trades) {
        List<PnLResult> results = new ArrayList<>();

        // Queue holds [quantity, buyPrice] pairs in FIFO order
        Queue<BigDecimal[]> buyQueue = new ArrayDeque<>();

        for (Trade trade : trades) {
            if (trade.getType().equals("BUY")) {
                // Add to queue
                buyQueue.add(new BigDecimal[]{
                    trade.getQuantity(), trade.getPrice()
                });

            } else if (trade.getType().equals("SELL")) {
                BigDecimal remainingQty = trade.getQuantity();
                BigDecimal totalCostBasis = BigDecimal.ZERO;

                // Match SELL against oldest BUYs first (FIFO)
                while (remainingQty.compareTo(BigDecimal.ZERO) > 0
                        && !buyQueue.isEmpty()) {

                    BigDecimal[] oldest = buyQueue.peek();
                    BigDecimal availableQty = oldest[0];
                    BigDecimal buyPrice = oldest[1];

                    if (availableQty.compareTo(remainingQty) <= 0) {
                        // Use entire oldest BUY
                        totalCostBasis = totalCostBasis.add(
                            availableQty.multiply(buyPrice)
                        );
                        remainingQty = remainingQty.subtract(availableQty);
                        buyQueue.poll(); // remove fully consumed BUY
                    } else {
                        // Partially consume oldest BUY
                        totalCostBasis = totalCostBasis.add(
                            remainingQty.multiply(buyPrice)
                        );
                        oldest[0] = availableQty.subtract(remainingQty);
                        remainingQty = BigDecimal.ZERO;
                    }
                }

                // PnL = sell revenue - cost basis
                BigDecimal sellRevenue = trade.getQuantity()
                        .multiply(trade.getPrice());
                BigDecimal realizedPnl = sellRevenue.subtract(totalCostBasis)
                        .setScale(8, RoundingMode.HALF_UP);

                results.add(new PnLResult(
                    trade.getId(),
                    trade.getSymbol(),
                    realizedPnl,
                    trade.getPrice(),
                    trade.getQuantity()
                ));
            }
        }

        return results;
    }
}