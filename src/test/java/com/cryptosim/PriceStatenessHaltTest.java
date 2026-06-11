package com.cryptosim;

import com.cryptosim.exception.StaleDataException;
import com.cryptosim.service.PriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PriceStatenessHaltTest {

    @Autowired
    private PriceService priceService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void getPrice_shouldThrowStaleDataException_whenPriceOlderThan10Seconds() {
        // Manually set a stale price in Redis
        // Timestamp = 15 seconds ago
        long staleTimestamp = Instant.now().toEpochMilli() - 15_000;

        redisTemplate.opsForValue().set("price:BTCUSDT", "65000.00");
        redisTemplate.opsForValue().set("price:BTCUSDT:ts",
                String.valueOf(staleTimestamp));

        // Should throw StaleDataException
        StaleDataException ex = assertThrows(StaleDataException.class, () -> {
            priceService.getPrice("BTCUSDT");
        });

        assertTrue(ex.getMessage().contains("stale"));
        System.out.println("✅ Stale price correctly halted: " + ex.getMessage());
    }

    @Test
    void getPrice_shouldReturnPrice_whenPriceFresh() {
        // Set a fresh price in Redis
        long freshTimestamp = Instant.now().toEpochMilli();

        redisTemplate.opsForValue().set("price:BTCUSDT", "65000.00");
        redisTemplate.opsForValue().set("price:BTCUSDT:ts",
                String.valueOf(freshTimestamp));

        // Should return price without throwing
        String price = assertDoesNotThrow(() -> priceService.getPrice("BTCUSDT"));

        assertEquals("65000.00", price);
        System.out.println("✅ Fresh price returned correctly: " + price);
    }

    @Test
    void getPrice_shouldFallbackToCoinbase_whenRedisMissing() {
        // Delete price from Redis to simulate cache miss
        redisTemplate.delete("price:ETHUSDT");
        redisTemplate.delete("price:ETHUSDT:ts");

        // Should not throw — falls back to Coinbase
        assertDoesNotThrow(() -> {
            String price = priceService.getPrice("ETHUSDT");
            assertNotNull(price);
            System.out.println("✅ Coinbase fallback returned: " + price);
        });
    }
}