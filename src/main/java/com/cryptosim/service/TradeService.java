package com.cryptosim.service;

import com.cryptosim.exception.InsufficientBalanceException;
import com.cryptosim.model.Trade;
import com.cryptosim.model.User;
import com.cryptosim.repository.TradeRepository;
import com.cryptosim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TradeService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PriceService priceService;

    @Autowired
    private PriceValidator priceValidator;

    // ✅ Pessimistic lock prevents concurrent overdraft
    @Transactional
    public Trade executeTrade(Long userId, String symbol, String type, BigDecimal quantity) {

        // 1. Lock user row — no other transaction can read/write until this completes
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Get and validate price
        String priceStr = priceService.getPrice(symbol);
        BigDecimal price = new BigDecimal(priceStr);
        priceValidator.validate(symbol, price);

        BigDecimal totalCost = price.multiply(quantity);

        if (type.equals("BUY")) {
            // 3. Check balance
            if (user.getBalance().compareTo(totalCost) < 0) {
                throw new InsufficientBalanceException(
                    "Insufficient balance. Required: " + totalCost +
                    ", Available: " + user.getBalance()
                );
            }
            // 4. Deduct balance
            user.setBalance(user.getBalance().subtract(totalCost));

        } else if (type.equals("SELL")) {
            // 4. Add balance
            user.setBalance(user.getBalance().add(totalCost));
        }

        // 5. Save updated balance
        userRepository.save(user);

        // 6. Record the trade
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setSymbol(symbol);
        trade.setType(type);
        trade.setQuantity(quantity);
        trade.setPrice(price);
        trade.setTotal(totalCost);
        trade.setExecutedAt(Instant.now());

        return tradeRepository.save(trade);
    }

    // Simulate trade — no real execution, just projected PnL
    public BigDecimal simulateTrade(Long userId, String symbol,
                                     String type, BigDecimal quantity) {
        String priceStr = priceService.getPrice(symbol);
        BigDecimal price = new BigDecimal(priceStr);
        priceValidator.validate(symbol, price);

        BigDecimal total = price.multiply(quantity);
        System.out.println("📊 Simulated " + type + " " + quantity +
                " " + symbol + " @ " + price + " = " + total);
        return total;
    }
}