package com.cryptosim.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class MarketMoodService {

    private static final String[] TRACKED_SYMBOLS =
            {"BTCUSDT", "ETHUSDT", "BNBUSDT"};

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PriceService priceService;

    public Map<String, Object> getMarketMood() {
        int bullishCount = 0;
        int bearishCount = 0;
        Map<String, String> symbolMoods = new HashMap<>();

        for (String symbol : TRACKED_SYMBOLS) {
            try {
                // Current price from Redis
                String currentPriceStr = priceService.getPrice(symbol);
                BigDecimal currentPrice = new BigDecimal(currentPriceStr);

                // 1-min average price from Redis
                String avgPriceStr = redisTemplate.opsForValue()
                        .get("price:avg1m:" + symbol);

                if (avgPriceStr != null) {
                    BigDecimal avgPrice = new BigDecimal(avgPriceStr);

                    // Calculate % change from average
                    BigDecimal change = currentPrice.subtract(avgPrice)
                            .divide(avgPrice, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

                    String mood = change.compareTo(BigDecimal.ZERO) >= 0
                            ? "BULLISH" : "BEARISH";
                    symbolMoods.put(symbol, mood + " (" + change.setScale(2,
                            RoundingMode.HALF_UP) + "%)");

                    if (mood.equals("BULLISH")) bullishCount++;
                    else bearishCount++;

                } else {
                    symbolMoods.put(symbol, "NEUTRAL (no avg data)");
                }

            } catch (Exception e) {
                symbolMoods.put(symbol, "UNAVAILABLE");
            }
        }

        // Overall mood
        String overallMood;
        if (bullishCount > bearishCount) overallMood = "BULLISH";
        else if (bearishCount > bullishCount) overallMood = "BEARISH";
        else overallMood = "NEUTRAL";

        return Map.of(
                "overall", overallMood,
                "bullish", bullishCount,
                "bearish", bearishCount,
                "symbols", symbolMoods
        );
    }
}