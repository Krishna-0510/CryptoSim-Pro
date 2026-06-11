package com.cryptosim;

import com.cryptosim.model.User;
import com.cryptosim.repository.UserRepository;
import com.cryptosim.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConcurrentBuyOrderTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setup() {
        // Create test user with balance of 100 USDT
        User user = new User();
        user.setUsername("testuser_concurrent");
        user.setBalance(new BigDecimal("100.00"));
        user = userRepository.save(user);
        testUserId = user.getId();
    }

    @Test
    void concurrentBuyOrders_shouldNotOverdraftBalance() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Each thread tries to BUY worth 50 USDT
        // Only 2 should succeed (balance = 100), rest should fail
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // quantity = 0.001 BTC @ ~50000 = 50 USDT
                    tradeService.executeTrade(
                        testUserId, "BTCUSDT", "BUY",
                        new BigDecimal("0.001")
                    );
                } catch (Exception e) {
                    // Expected — insufficient balance
                    System.out.println("✅ Correctly rejected: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Balance should never go below 0
        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertTrue(
            updatedUser.getBalance().compareTo(BigDecimal.ZERO) >= 0,
            "Balance went negative! Race condition detected!"
        );

        System.out.println("✅ Final balance: " + updatedUser.getBalance());
    }
}