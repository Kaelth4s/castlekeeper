# Development

## Start

```bash
cp .env.example .env    # fill DB_PASSWORD + TELEGRAM_BOT_TOKEN
docker compose up -d --build
```

API: `http://localhost:8080/swagger-ui.html`. Bot: send `/start` on Telegram.

## Dev mode (faster iteration)

Run server/bot outside Docker, databases in Docker:

```bash
docker compose up -d postgres redis
./mvnw spring-boot:run -pl server    # terminal 1
./mvnw spring-boot:run -pl bot       # terminal 2
```

## Package structure

```
dto/     — 10 Request/Response DTOs + ApiError (shared contract)
server/  — REST API: controller → service → repository → model (JPA)
bot/     — Telegram bot: callback → client → dispatcher → handler → keyboard → dialog
```

### Server layers
| Package | Role |
|---------|------|
| `controller/` | `@RestController` — DTO in/out, 5 entities |
| `service/` | `@Transactional` — DTO ↔ Entity mapping, FK resolution |
| `repository/` | `JpaRepository` interfaces |
| `model/` | `@Entity` — чистый JPA, ноль Jackson |
| `exception/` | `@RestControllerAdvice` → `ApiError` JSON |

### Bot packages
| Package | Role |
|---------|------|
| `callback/` | `CallbackData` record + `Actions` constants |
| `client/` | `RestClient` → server API (25 methods) |
| `dispatcher/` | Central router: text commands + inline callbacks + MDC |
| `handler/` | 6 handlers: Start, Menu, 5× CRUD |
| `dialog/` | `DialogStateMachine` — multi-step wizards |
| `keyboard/` | Reply + Inline keyboard factory with pagination |

## Tests

```bash
./mvnw test                              # 27 tests, all modules
./mvnw test -pl bot -Dtest=CastleHandlerTest  # single class
```

## Adding a new entity

1. **DTO** in `dto/`: `XxxRequest.java` + `XxxResponse.java`
2. **Entity** in `server/model/`, **Repository**, **Service**, **Controller**
3. **Migration** in `resources/db/migration/`: `V6__create_xxx.sql`
4. **Handler** in `bot/handler/`, register in `CommandDispatcher`
5. Add callback prefix to `Actions.java`
