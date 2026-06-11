package com.cryptosim.controller;

import com.cryptosim.model.StopLossOrder;
import com.cryptosim.repository.StopLossOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private StopLossOrderRepository stopLossOrderRepository;

    // Create a stop-loss or take-profit order
    @PostMapping("/stop-loss")
    public ResponseEntity<StopLossOrder> createOrder(@RequestBody Map<String, String> request) {
        StopLossOrder order = new StopLossOrder();
        order.setUserId(Long.parseLong(request.get("userId")));
        order.setSymbol(request.get("symbol"));
        order.setQuantity(new BigDecimal(request.get("quantity")));
        order.setTargetPrice(new BigDecimal(request.get("targetPrice")));
        order.setType(request.get("type")); // STOP_LOSS or TAKE_PROFIT
        order.setStatus("OPEN");

        StopLossOrder saved = stopLossOrderRepository.save(order);
        return ResponseEntity.ok(saved);
    }

    // Get all open orders for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StopLossOrder>> getUserOrders(@PathVariable Long userId) {
        List<StopLossOrder> orders = stopLossOrderRepository
                .findByUserIdAndStatus(userId, "OPEN");
        return ResponseEntity.ok(orders);
    }

    // Cancel an order
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long orderId) {
        StopLossOrder order = stopLossOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus("CANCELLED");
        stopLossOrderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "message", "Order cancelled successfully",
                "orderId", orderId.toString()
        ));
    }
}