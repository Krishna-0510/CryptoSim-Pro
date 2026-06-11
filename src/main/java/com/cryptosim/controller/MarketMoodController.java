package com.cryptosim.controller;

import com.cryptosim.service.MarketMoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/market")
public class MarketMoodController {

    @Autowired
    private MarketMoodService marketMoodService;

    @GetMapping("/mood")
    public ResponseEntity<Map<String, Object>> getMarketMood() {
        return ResponseEntity.ok(marketMoodService.getMarketMood());
    }
}