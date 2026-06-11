package com.cryptosim.repository;

import com.cryptosim.model.StopLossOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopLossOrderRepository extends JpaRepository<StopLossOrder, Long> {

    // Get all open orders for scheduler to check
    List<StopLossOrder> findByStatus(String status);

    // Get all orders for a specific user
    List<StopLossOrder> findByUserIdAndStatus(Long userId, String status);

    // Get all open orders for a specific symbol
    List<StopLossOrder> findBySymbolAndStatus(String symbol, String status);
}