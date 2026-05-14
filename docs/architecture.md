# Architecture

## Overview

CastleKeeper is a **client-server monolith** with 4 Docker services:

```
Telegram → Bot (Spring Boot) ──HTTP──→ Server (Spring Boot) ──JPA──→ PostgreSQL
                │                              │
                └── Redis (dialog state)        └── Flyway (migrations)
```

| Component | Technology | Role |
|-----------|-----------|------|
| **Bot** | Spring Boot 3 + TelegramBots 6.9 | Long-polling, inline keyboards, wizard state machine |
| **Server** | Spring Boot 3 + JPA/Hibernate + Flyway | REST CRUD: 5 entities |
| **PostgreSQL** | 16 Alpine | 5 tables with FK relationships |
| **Redis** | 7 Alpine (optional) | Dialog state persistence for scaling |

## Request lifecycle

```
1. Telegram user sends /start
2. Bot.onUpdateReceived() → CommandDispatcher.dispatch()
3. StartHandler.handle() → SendMessage with ReplyKeyboard
4. User taps "🏰 Замки" → callback "castle:menu" dispatches to CastleHandler
5. CastleHandler.handleCallback() → submenu with ReplyKeyboard

   ... CRUD operations ...

6. CastleHandler → CastleKeeperApiClient → RestClient
7. HTTP PUT /api/castles/{id} → CastleController
8. CastleService.update() → resolve FK → repository.save()
9. Hibernate → UPDATE SQL → PostgreSQL
10. Service maps Entity → CastleResponse DTO (with nested Author, Material)
11. Controller → Jackson → JSON → Bot → SendMessage to user
```

## DTO Pattern

Entities are **pure JPA** — no Jackson annotations. All JSON goes through DTOs:

| Direction | Format | Example |
|-----------|--------|---------|
| Request (POST/PUT) | FK as `Long` ID | `{"authorTypeId": 1}` |
| Response (GET) | FK as nested object | `{"authorType": {"id":1,"name":"Строитель"}}` |

**Why:** Entity doesn't leak to API layer. FK resolution happens in Service. Jackson can't access LAZY proxies. New protocol (gRPC, GraphQL) = new DTO, Entity unchanged.

## Key decisions

- **Flyway over ddl-auto** — versioned SQL migrations, `ddl-auto=validate`
- **Constructor injection** — all beans, no `@Autowired` on fields
- **LAZY + open-in-view** — `FetchType.LAZY` on `@ManyToOne`, session open during DTO mapping
- **Global exception handler** — `@RestControllerAdvice` → structured `ApiError` JSON
- **fail-on-unknown-properties** — catches JSON typos at API boundary
- **DialogStateMachine** — typed `CallbackData` record, retry on invalid input, TTL cleanup, Redis backend
- **Multi-module Maven** — `dto/` shared contract, `server/` + `bot/` independent apps
