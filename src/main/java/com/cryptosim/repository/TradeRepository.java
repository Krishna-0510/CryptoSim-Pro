package com.cryptosim.repository;

import com.cryptosim.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // Get all trades for a user
    List<Trade> findByUserId(Long userId);

    // Get all trades for a user and symbol
    List<Trade> findByUserIdAndSymbol(Long userId, String symbol);

    // Get all trades for a user by type (BUY/SELL)
    List<Trade> findByUserIdAndType(Long userId, String type);

    // Get all trades ordered by time — needed for FIFO PnL (Day 4)
    List<Trade> findByUserIdAndSymbolOrderByExecutedAtAsc(Long userId, String symbol);
}