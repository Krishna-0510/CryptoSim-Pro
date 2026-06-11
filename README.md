# CryptoSim Pro 🪙
> Production-grade crypto paper trading simulator built with Spring Boot, Redis, WebSocket, and MySQL.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green) ![Redis](https://img.shields.io/badge/Redis-7.x-red) ![MySQL](https://img.shields.io/badge/MySQL-8.x-blue) ![Docker](https://img.shields.io/badge/Docker-Ready-blue)

---

## What is CryptoSim Pro?

CryptoSim Pro is a paper trading simulator where users can buy/sell crypto with virtual money — without any real financial risk. Unlike typical student projects, this app solves **6 real production-grade problems** that most developers miss.

---

## Real Problems Solved

| # | Problem | Solution |
|---|---------|----------|
| 1 | Single point of failure on price feed | Binance primary + Coinbase REST fallback + staleness halt |
| 2 | Race condition causing negative balance | Pessimistic locking (PESSIMISTIC_WRITE) on user balance row |
| 3 | WebSocket IDOR vulnerability | JWT userId validated against topic destination in ChannelInterceptor |
| 4 | Price registry lost on restart | Redis as shared price store — survives restart + scales horizontally |
| 5 | Luck-based leaderboard | Skill score = weighted PnL + winRate + Sharpe ratio + consistency |
| 6 | No circuit breaker on bad prices | Price sanity validator rejects 0, null, or >20% deviation before trade |

---

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.x
- **Database:** MySQL 8.x
- **Cache / Price Registry:** Redis 7.x
- **Real-time:** Spring WebSocket (STOMP)
- **Auth:** JWT with refresh token rotation
- **Containerization:** Docker + Docker Compose

---

## Architecture Overview

```
Client (Mobile/Web)
       │
       ▼
Spring Boot App
  ├── AuthController     → JWT login / register
  ├── TradeController    → BUY / SELL with pessimistic locking
  ├── AlertController    → Price alert CRUD
  ├── AnalyticsController→ PnL, risk metrics, patterns
  └── AdminController    → Platform stats
       │
  ┌────┴────────────────────┐
  │                         │
 MySQL                    Redis
(users, trades,        (price registry,
 stop-loss orders)      rate limiter,
                        alert checks)
       │
  Binance WebSocket (primary price feed)
  Coinbase REST API  (fallback price feed)
```

---

## Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose

### Run with Docker
```bash
git clone https://github.com/Krishna-0510/CryptoSim-Pro.git
cd CryptoSim-Pro
cp .env.example .env
docker-compose up --build
```

App runs at: `http://localhost:8080`
Adminer (DB viewer): `http://localhost:8081`

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login + get JWT |
| POST | /api/trade/buy | Buy crypto |
| POST | /api/trade/sell | Sell crypto |
| POST | /api/trade/simulate | Simulate trade (no execution) |
| POST | /api/orders/stop-loss | Create stop-loss order |
| POST | /api/alerts | Create price alert |
| GET | /api/analytics/risk | Win rate, Sharpe, drawdown |
| GET | /api/analytics/pattern | Best trading time patterns |
| GET | /api/system/price-health | Price feed health status |
| GET | /api/leaderboard | Skill-based leaderboard |

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| main | Stable, deployable code |
| develop | Integration branch |
| phase-1-foundation | Redis price registry + auth |
| phase-2-trade-engine | Pessimistic locking + stop-loss |
| phase-3-websocket-security | JWT WS validation + rate limiter |
| phase-4-analytics | PnL + leaderboard + risk metrics |
| phase-5-polish | Docker + tests + README |

---

## Author

**Krishna** — MCA Final Year  
GitHub: [@Krishna-0510](https://github.com/Krishna-0510)
