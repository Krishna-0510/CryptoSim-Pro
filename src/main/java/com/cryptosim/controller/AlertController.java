package com.cryptosim.controller;

import com.cryptosim.model.Alert;
import com.cryptosim.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    // Create a new alert
    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Map<String, String> request) {
        Alert alert = new Alert();
        alert.setUserId(Long.parseLong(request.get("userId")));
        alert.setSymbol(request.get("symbol"));
        alert.setTargetPrice(new BigDecimal(request.get("targetPrice")));
        alert.setDirection(request.get("direction")); // ABOVE or BELOW
        alert.setStatus("ACTIVE");

        Alert saved = alertRepository.save(alert);
        return ResponseEntity.ok(saved);
    }

    // Get all active alerts for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Alert>> getUserAlerts(@PathVariable Long userId) {
        List<Alert> alerts = alertRepository.findByUserIdAndStatus(userId, "ACTIVE");
        return ResponseEntity.ok(alerts);
    }

    // Delete/cancel an alert
    @DeleteMapping("/{alertId}")
    public ResponseEntity<Map<String, String>> deleteAlert(@PathVariable Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.setStatus("CANCELLED");
        alertRepository.save(alert);

        return ResponseEntity.ok(Map.of(
                "message", "Alert cancelled successfully",
                "alertId", alertId.toString()
        ));
    }
}