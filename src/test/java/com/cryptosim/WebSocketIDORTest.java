package com.cryptosim;

import com.cryptosim.security.WebSocketSecurityInterceptor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WebSocketIDORTest {

    @Autowired
    private WebSocketSecurityInterceptor interceptor;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String validToken;

    @BeforeEach
    void setup() {
        // Generate a JWT token for userId = 1
        validToken = "Bearer " + Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }

    @Test
    void subscribe_shouldBeAllowed_whenUserIdMatchesToken() {
        // User 1 subscribing to their own portfolio
        Message<?> message = buildSubscribeMessage(
                "/topic/portfolio/1", validToken
        );

        // Should NOT throw
        assertDoesNotThrow(() -> {
            interceptor.preSend(message, (MessageChannel) null);
        });

        System.out.println("✅ Correct user allowed to subscribe to own portfolio");
    }

    @Test
    void subscribe_shouldThrowAccessDenied_whenUserIdMismatch() {
        // User 1 trying to subscribe to User 2's portfolio — IDOR attempt
        Message<?> message = buildSubscribeMessage(
                "/topic/portfolio/2", validToken
        );

        // Should throw AccessDeniedException
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            interceptor.preSend(message, (MessageChannel) null);
        });

        assertTrue(ex.getMessage().contains("IDOR blocked"));
        System.out.println("✅ IDOR blocked: " + ex.getMessage());
    }

    @Test
    void subscribe_shouldThrowAccessDenied_whenTokenMissing() {
        // No token in header
        Message<?> message = buildSubscribeMessage(
                "/topic/portfolio/1", null
        );

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            interceptor.preSend(message, (MessageChannel) null);
        });

        assertTrue(ex.getMessage().contains("Missing JWT token"));
        System.out.println("✅ Missing token correctly rejected: " + ex.getMessage());
    }

    // Helper — builds a STOMP SUBSCRIBE message
    private Message<?> buildSubscribeMessage(String destination, String token) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        if (token != null) {
            accessor.addNativeHeader("Authorization", token);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}