# CryptoSim Pro 🚀
> Intelligent Paper Trading Platform — Production-Grade Spring Boot Backend

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Redis](https://img.shields.io/badge/Redis-7.2-red)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

---

## What Makes This Different

Most student projects are CRUD apps. CryptoSim Pro solves **6 real production problems**:

| # | Problem | Solution |
|---|---------|----------|
| 1 | Single point of failure on price feed | Binance primary + Coinbase fallback + staleness halt |
| 2 | Race condition causing negative balance | Pessimistic locking (PESSIMISTIC_WRITE) on user row |
| 3 | WebSocket IDOR vulnerability | JWT userId validated in ChannelInterceptor |
| 4 | Price registry dies on restart | Redis as shared price store |
| 5 | Luck-based leaderboard | Skill score = PnL + winRate + Sharpe + consistency |
| 6 | No circuit breaker on bad prices | Price sanity validator before every trade |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    CryptoSim Pro                     │
├──────────────┬──────────────────┬───────────────────┤
│  Price Layer │   Trade Engine   │   Analytics       │
│              │                  │                   │
│  Binance WS  │  Pessimistic     │  FIFO PnL         │
│  Coinbase    │  Locking         │  Skill Score      │
│  Redis Cache │  Stop-Loss       │  Risk Metrics     │
│  Staleness   │  Price Validator │  Pattern Detect   │
│  Check       │  Rate Limiter    │  Leaderboard      │
├──────────────┴──────────────────┴───────────────────┤
│              WebSocket Layer (STOMP)                 │
│         JWT Security + IDOR Protection               │
├─────────────────────────────────────────────────────┤
│         MySQL (trades, users, alerts)                │
│         Redis (prices, rate limits, alerts)          │
└─────────────────────────────────────────────────────┘
```

---

## Quick Start

### Prerequisites
- Docker + Docker Compose
- Java 21 (for local dev)

### Run with Docker
```bash
# Clone the repo
git clone https://github.com/<your-username>/CryptoSim-Pro.git
cd CryptoSim-Pro

# Copy env file and fill in values
cp .env.example .env

# Launch everything
docker-compose up --build
```

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Adminer (DB) | http://localhost:8081 |
| Redis | localhost:6379 |

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Login + get JWT |

### Trading
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/trade/execute` | Execute a trade |
| POST | `/api/trade/simulate` | Simulate trade (no execution) |
| POST | `/api/orders/stop-loss` | Create stop-loss order |
| GET | `/api/orders/user/{userId}` | Get open orders |
| DELETE | `/api/orders/{orderId}` | Cancel order |

### Alerts
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/alerts` | Create price alert |
| GET | `/api/alerts/user/{userId}` | Get active alerts |
| DELETE | `/api/alerts/{alertId}` | Cancel alert |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/risk?userId=1` | Risk metrics |
| GET | `/api/analytics/pattern?userId=1` | Trading patterns |
| GET | `/api/analytics/leaderboard` | Skill leaderboard |

### System
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/price-health` | Price feed status |
| GET | `/api/market/mood` | Market mood indicator |
| GET | `/api/admin/stats` | Platform stats (admin only) |

---

## Key Technical Decisions

**Why Pessimistic Locking?**
Optimistic locking retries on conflict — in high-frequency trading that means retrying a BUY that should have failed. Pessimistic locking blocks concurrent reads until the transaction completes, guaranteeing no overdraft.

**Why Redis for Price Registry?**
ConcurrentHashMap lives in JVM heap — server restart = all prices lost, trading halted until Binance reconnects. Redis survives restarts and enables horizontal scaling.

**Why FIFO for PnL?**
FIFO matches real tax accounting. Average cost basis hides the true profit on individual lots — FIFO shows exactly which BUY each SELL consumed.

---

## Running Tests
```bash
mvn test
```

Tests cover:
- Concurrent BUY orders do not overdraft balance
- Stop-loss triggers correctly at threshold price
- Stale price halts trading after 10 seconds
- WebSocket IDOR blocked for mismatched userId

---

## Built By
**Kisuuu** — MCA Final Year  
Gujarat, India