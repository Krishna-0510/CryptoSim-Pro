package com.cryptosim.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal targetPrice;

    // ABOVE or BELOW
    @Column(nullable = false)
    private String direction;

    // ACTIVE or TRIGGERED or CANCELLED
    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant triggeredAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }
}