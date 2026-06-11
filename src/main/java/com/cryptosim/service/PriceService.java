package com.cryptosim.service;

import com.cryptosim.exception.StaleDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class PriceService {

    private static final long STALE_THRESHOLD_MS = 10_000; // 10 seconds

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CoinbaseService coinbaseService;

    // Called by BinanceWebSocketClient on every price update
    public void updatePrice(String symbol, String price) {
        redisTemplate.opsForValue().set("price:" + symbol, price);
        redisTemplate.opsForValue().set("price:" + symbol + ":ts",
                String.valueOf(Instant.now().toEpochMilli()));
    }

    // Called by TradeService before every trade
    public String getPrice(String symbol) {
        String price = redisTemplate.opsForValue().get("price:" + symbol);
        String tsStr = redisTemplate.opsForValue().get("price:" + symbol + ":ts");

        // If Redis has price, check staleness
        if (price != null && tsStr != null) {
            long age = Instant.now().toEpochMilli() - Long.parseLong(tsStr);
            if (age > STALE_THRESHOLD_MS) {
                throw new StaleDataException("Price for " + symbol +
                        " is stale (" + age + "ms old). Trading halted.");
            }
            return price;
        }

        // Redis miss — fall back to Coinbase
        System.out.println("⚠️ Redis miss for " + symbol + " — falling back to Coinbase");
        return coinbaseService.getPrice(symbol);
    }

    // For health dashboard
    public long getPriceAgeMs(String symbol) {
        String tsStr = redisTemplate.opsForValue().get("price:" + symbol + ":ts");
        if (tsStr == null) return -1;
        return Instant.now().toEpochMilli() - Long.parseLong(tsStr);
    }
}