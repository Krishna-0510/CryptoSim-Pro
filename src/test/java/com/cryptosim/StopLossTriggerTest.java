package com.cryptosim;

import com.cryptosim.model.StopLossOrder;
import com.cryptosim.repository.StopLossOrderRepository;
import com.cryptosim.scheduler.StopLossScheduler;
import com.cryptosim.service.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StopLossTriggerTest {

    @Autowired
    private StopLossScheduler stopLossScheduler;

    @Autowired
    private StopLossOrderRepository stopLossOrderRepository;

    @MockBean
    private PriceService priceService;

    private StopLossOrder testOrder;

    @BeforeEach
    void setup() {
        // Create a STOP_LOSS order — trigger if price drops below 60000
        StopLossOrder order = new StopLossOrder();
        order.setUserId(1L);
        order.setSymbol("BTCUSDT");
        order.setQuantity(new BigDecimal("0.01"));
        order.setTargetPrice(new BigDecimal("60000.00"));
        order.setType("STOP_LOSS");
        order.setStatus("OPEN");
        testOrder = stopLossOrderRepository.save(order);
    }

    @Test
    void stopLoss_shouldTrigger_whenPriceFallsBelowTarget() {
        // Mock price below target
        when(priceService.getPrice("BTCUSDT"))
                .thenReturn("59000.00");

        // Run scheduler manually
        stopLossScheduler.checkStopLossOrders();

        // Verify order is now TRIGGERED
        StopLossOrder updated = stopLossOrderRepository
                .findById(testOrder.getId()).orElseThrow();
        assertEquals("TRIGGERED", updated.getStatus());
        assertNotNull(updated.getTriggeredAt());
        System.out.println("✅ Stop loss triggered at price 59000");
    }

    @Test
    void stopLoss_shouldNotTrigger_whenPriceAboveTarget() {
        // Mock price above target
        when(priceService.getPrice("BTCUSDT"))
                .thenReturn("65000.00");

        // Run scheduler manually
        stopLossScheduler.checkStopLossOrders();

        // Verify order is still OPEN
        StopLossOrder updated = stopLossOrderRepository
                .findById(testOrder.getId()).orElseThrow();
        assertEquals("OPEN", updated.getStatus());
        System.out.println("✅ Stop loss correctly NOT triggered at price 65000");
    }

    @Test
    void takeProfitOrder_shouldTrigger_whenPriceRisesAboveTarget() {
        // Change order type to TAKE_PROFIT
        testOrder.setType("TAKE_PROFIT");
        testOrder.setTargetPrice(new BigDecimal("70000.00"));
        stopLossOrderRepository.save(testOrder);

        // Mock price above target
        when(priceService.getPrice("BTCUSDT"))
                .thenReturn("71000.00");

        stopLossScheduler.checkStopLossOrders();

        StopLossOrder updated = stopLossOrderRepository
                .findById(testOrder.getId()).orElseThrow();
        assertEquals("TRIGGERED", updated.getStatus());
        System.out.println("✅ Take profit triggered at price 71000");
    }
}