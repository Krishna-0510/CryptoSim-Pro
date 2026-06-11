package com.cryptosim.repository;

import com.cryptosim.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Get all active alerts — used by scheduler
    List<Alert> findByStatus(String status);

    // Get all alerts for a specific user
    List<Alert> findByUserId(Long userId);

    // Get all active alerts for a specific user
    List<Alert> findByUserIdAndStatus(Long userId, String status);

    // Get all active alerts for a specific symbol
    List<Alert> findBySymbolAndStatus(String symbol, String status);
}