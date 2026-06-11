package com.cryptosim.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CoinbaseService {

    private static final String COINBASE_URL =
        "https://api.coinbase.com/v2/prices/{symbol}-USD/spot";

    private final RestTemplate restTemplate = new RestTemplate();

    public String getPrice(String symbol) {
        try {
            String url = COINBASE_URL.replace("{symbol}", symbol.replace("USDT", ""));
            CoinbaseResponse response = restTemplate.getForObject(url, CoinbaseResponse.class);

            if (response != null && response.data != null) {
                System.out.println("✅ Coinbase fallback price for " + symbol +
                        ": " + response.data.amount);
                return response.data.amount;
            }
        } catch (Exception e) {
            System.out.println("❌ Coinbase fallback failed: " + e.getMessage());
        }

        throw new RuntimeException("All price sources failed for: " + symbol);
    }

    // Inner classes to map Coinbase JSON response
    // {"data": {"amount": "65000.00", "currency": "USD"}}
    static class CoinbaseResponse {
        public Data data;
    }

    static class Data {
        public String amount;
        public String currency;
    }
}