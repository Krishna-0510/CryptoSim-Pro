package com.cryptosim.controller;

import com.cryptosim.repository.TradeRepository;
import com.cryptosim.repository.UserRepository;
import com.cryptosim.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private LeaderboardService leaderboardService;

    // GET /api/admin/stats — admin only
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {

        // Total users
        long totalUsers = userRepository.count();

        // Total trades
        long totalTrades = tradeRepository.count();

        // Total platform volume
        BigDecimal totalVolume = tradeRepository.findAll()
                .stream()
                .map(t -> t.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top 3 traders by skill score
        List<Map<String, Object>> leaderboard = leaderboardService.getLeaderboard();
        List<Map<String, Object>> top3 = leaderboard.stream()
                .limit(3)
                .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalTrades", totalTrades);
        stats.put("totalVolume", totalVolume);
        stats.put("top3Traders", top3);

        return ResponseEntity.ok(stats);
    }
}