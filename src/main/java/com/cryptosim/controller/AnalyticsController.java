package com.cryptosim.controller;

import com.cryptosim.service.LeaderboardService;
import com.cryptosim.service.PatternAnalyzerService;
import com.cryptosim.service.RiskMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private RiskMetricsService riskMetricsService;

    @Autowired
    private PatternAnalyzerService patternAnalyzerService;

    @Autowired
    private LeaderboardService leaderboardService;

    // GET /api/analytics/risk?userId=1
    @GetMapping("/risk")
    public ResponseEntity<Map<String, Object>> getRiskMetrics(
            @RequestParam Long userId) {
        return ResponseEntity.ok(riskMetricsService.getRiskMetrics(userId));
    }

    // GET /api/analytics/pattern?userId=1
    @GetMapping("/pattern")
    public ResponseEntity<Map<String, Object>> getPattern(
            @RequestParam Long userId) {
        return ResponseEntity.ok(patternAnalyzerService.analyzePatterns(userId));
    }

    // GET /api/analytics/leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }
}