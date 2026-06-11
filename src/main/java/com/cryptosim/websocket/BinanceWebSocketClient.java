package com.cryptosim.websocket;

import com.cryptosim.service.PriceService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Component
public class BinanceWebSocketClient implements WebSocketHandler {

    private static final String BINANCE_WS_URL =
        "wss://stream.binance.com:9443/ws/btcusdt@trade";

    @Autowired
    private PriceService priceService;

    @PostConstruct
    public void connect() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.doHandshake(this, BINANCE_WS_URL);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        String payload = message.getPayload().toString();

        // Binance sends JSON like: {"p":"65000.00", ...}
        // Simple parse — no Jackson needed for just price
        if (payload.contains("\"p\":")) {
            String price = payload.split("\"p\":\"")[1].split("\"")[0];
            priceService.updatePrice("BTCUSDT", price);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("✅ Binance WebSocket connected");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        System.out.println("❌ Binance WS error: " + ex.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("🔌 Binance WS closed: " + status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}