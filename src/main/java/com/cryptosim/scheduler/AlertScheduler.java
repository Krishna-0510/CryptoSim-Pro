package com.cryptosim.scheduler;

import com.cryptosim.model.Alert;
import com.cryptosim.repository.AlertRepository;
import com.cryptosim.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class AlertScheduler {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private PriceService priceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 5000) // runs every 5 seconds
    public void checkAlerts() {
        List<Alert> activeAlerts = alertRepository.findByStatus("ACTIVE");

        for (Alert alert : activeAlerts) {
            try {
                String priceStr = priceService.getPrice(alert.getSymbol());
                BigDecimal currentPrice = new BigDecimal(priceStr);

                boolean shouldTrigger = false;

                if (alert.getDirection().equals("ABOVE")) {
                    // Trigger if price went ABOVE target
                    shouldTrigger = currentPrice.compareTo(alert.getTargetPrice()) >= 0;
                } else if (alert.getDirection().equals("BELOW")) {
                    // Trigger if price went BELOW target
                    shouldTrigger = currentPrice.compareTo(alert.getTargetPrice()) <= 0;
                }

                if (shouldTrigger) {
                    // 1. Update alert status
                    alert.setStatus("TRIGGERED");
                    alert.setTriggeredAt(Instant.now());
                    alertRepository.save(alert);

                    // 2. Push via WebSocket to user
                    messagingTemplate.convertAndSend(
                        "/topic/alerts/" + alert.getUserId(),
                        Map.of(
                            "alertId", alert.getId(),
                            "symbol", alert.getSymbol(),
                            "direction", alert.getDirection(),
                            "targetPrice", alert.getTargetPrice(),
                            "currentPrice", currentPrice,
                            "message", alert.getSymbol() + " hit " + currentPrice
                        )
                    );

                    System.out.println("🔔 Alert triggered for user " + alert.getUserId() +
                            ": " + alert.getSymbol() + " " + alert.getDirection() +
                            " " + alert.getTargetPrice());
                }

            } catch (Exception e) {
                System.out.println("⚠️ Skipping alert " + alert.getId() +
                        ": " + e.getMessage());
            }
        }
    }
}