package com.cryptosim.config;

import com.cryptosim.security.WebSocketSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketSecurityInterceptor webSocketSecurityInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients subscribe to /topic/...
        registry.enableSimpleBroker("/topic");
        // Client sends messages to /app/...
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket connection endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register our JWT security interceptor
        registration.interceptors(webSocketSecurityInterceptor);
    }
}