# Troubleshooting

## Bot not responding

```bash
docker compose logs bot | grep "Bot registered"
# Expected: "Bot registered — polling started"
```

Test token: `curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/getMe"`  
If invalid → re-create token at @BotFather.  
If blocked → restart chat with `/start`.

## Server crashes / restarts

```bash
docker compose logs server | grep ERROR
```

**"Connection refused"** → PostgreSQL not ready. `docker compose restart server`.  
**"Table does not exist"** → Flyway didn't run. Check `server/src/main/resources/db/migration/` files exist.  
**"404 Not Found"** on API → endpoint path wrong. Check Swagger: `http://localhost:${SERV_PORT}/swagger-ui.html`.

## Bot "Сервер замка не отвечает"

Server unreachable from bot. Check: `curl http://localhost:${SERV_PORT}/api/castles`.  
If server is up → check `API_BASE_URL` in bot container (`http://server:8080/api`).

## Port in use

```bash
# Linux: sudo lsof -i :8080
# Windows: netstat -ano | findstr 8080
```

Change `SERV_PORT` or `BOT_PORT` in `.env`, then `docker compose down && docker compose up -d --build`.

## Wizard dialog stuck

Send `/cancel` to bot. If stuck → `docker compose restart bot` (state is in-memory).

## Redis disabled

If `REDIS_HOST` is not set, Redis is unused — the warning in logs is harmless.  
To enable: set `REDIS_HOST=localhost` in `.env`, `docker compose up -d redis`, `docker compose restart bot`.

## Reset everything

```bash
docker compose down -v
docker compose up -d --build
```
