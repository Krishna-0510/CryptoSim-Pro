package com.cryptosim.scheduler;

import com.cryptosim.model.StopLossOrder;
import com.cryptosim.repository.StopLossOrderRepository;
import com.cryptosim.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class StopLossScheduler {

    @Autowired
    private StopLossOrderRepository stopLossOrderRepository;

    @Autowired
    private PriceService priceService;

    @Scheduled(fixedDelay = 30000) // runs every 30 seconds
    public void checkStopLossOrders() {
        List<StopLossOrder> openOrders = stopLossOrderRepository.findByStatus("OPEN");

        for (StopLossOrder order : openOrders) {
            try {
                String priceStr = priceService.getPrice(order.getSymbol());
                BigDecimal currentPrice = new BigDecimal(priceStr);

                boolean shouldTrigger = false;

                if (order.getType().equals("STOP_LOSS")) {
                    // Trigger if price dropped BELOW target
                    shouldTrigger = currentPrice.compareTo(order.getTargetPrice()) <= 0;
                } else if (order.getType().equals("TAKE_PROFIT")) {
                    // Trigger if price rose ABOVE target
                    shouldTrigger = currentPrice.compareTo(order.getTargetPrice()) >= 0;
                }

                if (shouldTrigger) {
                    order.setStatus("TRIGGERED");
                    order.setTriggeredAt(Instant.now());
                    stopLossOrderRepository.save(order);
                    System.out.println("🔔 Order triggered: " + order.getType() +
                            " for " + order.getSymbol() +
                            " at price " + currentPrice);
                }

            } catch (Exception e) {
                System.out.println("⚠️ Skipping order " + order.getId() +
                        ": " + e.getMessage());
            }
        }
    }
}