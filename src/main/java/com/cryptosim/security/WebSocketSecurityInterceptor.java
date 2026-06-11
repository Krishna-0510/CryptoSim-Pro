package com.cryptosim.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Only check SUBSCRIBE frames
        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            // Only protect portfolio topics
            if (destination != null && destination.startsWith("/topic/portfolio/")) {

                // Extract userId from destination
                String destUserId = destination.replace("/topic/portfolio/", "");

                // Extract JWT from STOMP header
                String token = accessor.getFirstNativeHeader("Authorization");
                if (token == null || !token.startsWith("Bearer ")) {
                    throw new AccessDeniedException("Missing JWT token");
                }

                token = token.substring(7);

                // Validate JWT and extract userId
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtSecret.getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String tokenUserId = claims.getSubject();

                // Compare token userId vs destination userId
                if (!tokenUserId.equals(destUserId)) {
                    throw new AccessDeniedException(
                        "IDOR blocked: token userId " + tokenUserId +
                        " tried to subscribe to userId " + destUserId
                    );
                }
            }
        }

        return message;
    }
}