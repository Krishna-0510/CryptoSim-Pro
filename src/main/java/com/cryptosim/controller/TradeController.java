package com.cryptosim.controller;

import com.cryptosim.model.Trade;
import com.cryptosim.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    // Execute a real trade
    @PostMapping("/execute")
    public ResponseEntity<Trade> executeTrade(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        String symbol = request.get("symbol");
        String type = request.get("type");
        BigDecimal quantity = new BigDecimal(request.get("quantity"));

        Trade trade = tradeService.executeTrade(userId, symbol, type, quantity);
        return ResponseEntity.ok(trade);
    }

    // Simulate a trade — no real execution
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateTrade(
            @RequestBody Map<String, String> request) {

        Long userId = Long.parseLong(request.get("userId"));
        String symbol = request.get("symbol");
        String type = request.get("type");
        BigDecimal quantity = new BigDecimal(request.get("quantity"));

        BigDecimal projectedTotal = tradeService.simulateTrade(userId, symbol, type, quantity);

        return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "type", type,
                "quantity", quantity,
                "projectedTotal", projectedTotal,
                "note", "Simulation only — no trade executed"
        ));
    }
}