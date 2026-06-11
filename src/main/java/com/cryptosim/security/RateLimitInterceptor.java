package com.cryptosim.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_TRADES_PER_MINUTE = 5;
    private static final long WINDOW_SECONDS = 60;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Extract userId from request header
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"error\": \"Missing X-User-Id header\"}");
            return false;
        }

        String key = "ratelimit:" + userId + ":trades";

        // Get current count from Redis
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= MAX_TRADES_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Rate limit exceeded. Max " +
                MAX_TRADES_PER_MINUTE + " trades per minute.\"}"
            );
            return false;
        }

        // Increment count
        if (countStr == null) {
            // First trade — set key with TTL of 60 seconds
            redisTemplate.opsForValue().set(key, "1",
                    Duration.ofSeconds(WINDOW_SECONDS));
        } else {
            redisTemplate.opsForValue().increment(key);
        }

        return true;
    }
}