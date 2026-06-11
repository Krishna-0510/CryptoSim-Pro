package com.cryptosim.service;

import com.cryptosim.exception.InvalidPriceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PriceValidator {

    private static final BigDecimal MAX_DEVIATION = new BigDecimal("0.20"); // 20%

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void validate(String symbol, BigDecimal currentPrice) {

        // Check null or zero
        if (currentPrice == null) {
            throw new InvalidPriceException("Price is null for: " + symbol);
        }
        if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException("Price is zero or negative for: " + symbol);
        }

        // Check 20% deviation from 1-min average stored in Redis
        String avgStr = redisTemplate.opsForValue().get("price:avg1m:" + symbol);
        if (avgStr != null) {
            BigDecimal avg = new BigDecimal(avgStr);
            BigDecimal deviation = currentPrice.subtract(avg)
                    .abs()
                    .divide(avg, 4, RoundingMode.HALF_UP);

            if (deviation.compareTo(MAX_DEVIATION) > 0) {
                throw new InvalidPriceException(
                    "Price deviation too high for " + symbol +
                    ": " + deviation.multiply(BigDecimal.valueOf(100)) + "% from average"
                );
            }
        }
    }
}