package com.cryptosim.controller;

import com.cryptosim.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class PriceHealthController {

    private static final List<String> TRACKED_SYMBOLS =
            List.of("BTCUSDT", "ETHUSDT", "BNBUSDT");

    @Autowired
    private PriceService priceService;

    @GetMapping("/price-health")
    public ResponseEntity<Map<String, Object>> getPriceHealth() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> symbols = new HashMap<>();

        boolean allHealthy = true;

        for (String symbol : TRACKED_SYMBOLS) {
            Map<String, Object> info = new HashMap<>();
            long ageMs = priceService.getPriceAgeMs(symbol);

            if (ageMs == -1) {
                info.put("status", "NO_DATA");
                info.put("ageMs", -1);
                allHealthy = false;
            } else if (ageMs > 10_000) {
                info.put("status", "STALE");
                info.put("ageMs", ageMs);
                allHealthy = false;
            } else {
                info.put("status", "OK");
                info.put("ageMs", ageMs);

                try {
                    info.put("price", priceService.getPrice(symbol));
                } catch (Exception e) {
                    info.put("price", "unavailable");
                }
            }

            symbols.put(symbol, info);
        }

        health.put("overall", allHealthy ? "HEALTHY" : "DEGRADED");
        health.put("symbols", symbols);

        return ResponseEntity.ok(health);
    }
}