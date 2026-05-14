# 🏰 CastleKeeper

**Telegram bot — castle keeper** · Ask and the gatekeeper shall answer.

> 🇬🇧 English · [🇷🇺 Русский](README-ru.md)

---

## ⚡ Start

```bash
cp .env.example .env         # fill DB_PASSWORD + TELEGRAM_BOT_TOKEN
docker compose up -d --build  # build + start all 4 services
```

Server → `http://localhost:8080/swagger-ui.html` · Bot → send `/start` on Telegram.

## 🏗️ Architecture

```
docker compose up -d
  ├── postgres:16-alpine  (:5432)
  ├── redis:7-alpine      (:6379)
  ├── server (JVM 21)     (:8080)
  └── bot (JVM 21)        (:8081)

Bot ──HTTP──→ Server ──JPA──→ PostgreSQL  ·  Bot ──→ Redis (dialog state)
```

| Component | Stack |
|-----------|-------|
| Bot | Spring Boot 3 · TelegramBots 6.9 · RestClient · DialogStateMachine |
| Server | Spring Boot 3 · JPA/Hibernate · Flyway · Swagger |
| DB | PostgreSQL 16 (5 entities, FK) · Redis 7 (optional) |

## 📂 Structure

```
castlekeeper/
├── dto/              Shared DTOs (10 Request/Response + ApiError)
├── server/           REST API (controller → service → repository → model)
├── bot/              Telegram bot (callback → client → dispatcher → handler → dialog)
├── docs/             Deployment · Development · Architecture · Configuration · Troubleshooting
├── docker-compose.yml   All 4 services
└── .env.example      Environment template
```

## 🤖 Bot — 5 Wings

**Reply Keyboard** navigation · **Inline Keyboard** CRUD pickers (5/page ◀▶)

| `/start` | Castle gates |
| `/menu` | Main hall: 🏰 Castles · 👥 Authors · 🏷️ Types · 🧱 Materials · 🔨 Reconstructions |
| `/cancel` | Exit wizard |

Each wing: 📜 List · 👁 Select · 🏗 Add (wizard) · ✒ Edit · 💥 Delete

## 🔌 REST API

Base: `http://localhost:8080/api/` · Swagger: `/swagger-ui.html`

5 resources: `castles` (+`/random`), `authors`, `author-types`, `materials`, `reconstructions`

| Method | Status |
|--------|--------|
| GET collection | 200 |
| GET by ID | 200 / 404 |
| POST | 201 / 400 |
| PUT | 200 / 400 / 404 |
| DELETE | 204 / 404 |

> DTO pattern: POST/PUT accept FK as `Long` IDs. GET returns nested objects.

## 📖 Docs

| Doc | For |
|-----|-----|
| [`deployment.md`](docs/deployment.md) | Deploy (Linux/NixOS/Windows), reverse proxy, health checks |
| [`development.md`](docs/development.md) | Quickstart, package map, tests, adding features |
| [`architecture.md`](docs/architecture.md) | System overview, DTO pattern, design decisions |
| [`configuration.md`](docs/configuration.md) | All env vars · properties · Docker variables |
| [`troubleshooting.md`](docs/troubleshooting.md) | Bot silent · DB errors · port conflicts · logs |
| [`bot-commands.md`](docs/bot-commands.md) | Command reference · wizard flows · state machine |

## 🧪 Tests

```bash
./mvnw test   # 27 tests: DialogStateMachine(6) CastleHandler(6) MaterialHandler(5) AuthorHandler(4) AuthorTypeHandler(3) ReconstructionHandler(3)
```

## 📄 License · 👤 Author

MIT — **kaelth4s**, 2026
