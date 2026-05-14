# Configuration

All configuration via `.env` file + `application.properties`.

## Environment variables (`.env`)

| Variable             | Required | Default | Purpose                            |
| -------------------- | -------- | ------- | ---------------------------------- |
| `DB_NAME`            | ✅        | —       | Database name                      |
| `DB_USER`            | ✅        | —       | Database user                      |
| `DB_PASSWORD`        | ✅        | —       | **SECRET**                         |
| `DB_PORT`            | ❌        | `5432`  | PostgreSQL port on host            |
| `TELEGRAM_BOT_TOKEN` | ✅        | —       | **SECRET** — from @BotFather       |
| `SERV_PORT`          | ❌        | `8080`  | Server port on host                |
| `BOT_PORT`           | ❌        | `8081`  | Bot health-check port on host      |
| `REDIS_HOST`         | ❌        | —       | Set to `localhost` to enable Redis |
| `REDIS_PORT`         | ❌        | `6379`  | Redis port on host                 |

## Docker Compose — container environment

Internal ports are **hardcoded** (always 5432, 6379, 8080, 8081 inside Docker). External ports are from `.env`.

**Server container:** `SERV_PORT=8080`, `DB_HOST=postgres`, `DB_PORT=5432`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`  
**Bot container:** `BOT_PORT=8081`, `TELEGRAM_BOT_TOKEN`, `REDIS_HOST=redis`, `REDIS_PORT=6379`, `API_BASE_URL=http://server:8080/api`

## Application properties

### Server (`server/src/main/resources/application.properties`)

| Key | Value |
|-----|-------|
| `server.port` | `${SERV_PORT}` |
| `spring.datasource.url` | `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` |
| `spring.jpa.hibernate.ddl-auto` | `validate` |
| `spring.flyway.enabled` | `true` |
| `spring.jackson.deserialization.fail-on-unknown-properties` | `true` |
| `spring.jpa.open-in-view` | `true` |

### Bot (`bot/src/main/resources/application.properties`)

| Key | Value |
|-----|-------|
| `server.port` | `${BOT_PORT:8081}` |
| `telegram.bot.token` | `${TELEGRAM_BOT_TOKEN}` |
| `api.base-url` | `${API_BASE_URL:http://localhost:8080/api}` |
| `spring.data.redis.host` | `${REDIS_HOST:}` |
| `spring.data.redis.port` | `${REDIS_PORT:6379}` |

## Logging

```bash
docker compose logs -f bot      # MDC: chatId, command, callback
docker compose logs -f server   # SQL queries (show-sql=true)
docker compose logs postgres    # DB logs
```
