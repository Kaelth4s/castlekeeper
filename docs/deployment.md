# Deployment

```bash
cp .env.example .env && nano .env   # DB_PASSWORD + TELEGRAM_BOT_TOKEN
docker compose up -d --build         # builds + starts all 4 services
```

**Done.** Server on `:${SERV_PORT}`, bot on `:${BOT_PORT}`, PostgreSQL on `:${DB_PORT}`, Redis on `:${REDIS_PORT}`.

---

## Service ports — internal vs external

| Service | Internal (Docker) | External (host, from `.env`) |
|---------|-------------------|------------------------------|
| PostgreSQL | `postgres:5432` | `${DB_PORT}:5432` |
| Redis | `redis:6379` | `${REDIS_PORT}:6379` |
| Server | `server:8080` | `${SERV_PORT}:8080` |
| Bot | `:8081` (health) | `${BOT_PORT}:8081` |

Internal ports are **hardcoded** in `docker-compose.yml`. Change only `.env` variables to adjust external ports.

## Platform-specific prerequisites

**Debian/Ubuntu:** `sudo apt install docker.io docker-compose-v2`  
**NixOS:** `environment.systemPackages = [ docker docker-compose ]; virtualisation.docker.enable = true;`  
**Windows:** [Docker Desktop](https://www.docker.com/products/docker-desktop/)  
**macOS:** [Docker Desktop](https://www.docker.com/products/docker-desktop/)

No JDK/Maven required on host — Docker builds everything inside containers.

## Health checks

```bash
docker compose ps                          # all "Up (healthy)"
curl localhost:${SERV_PORT}/actuator/health  # server
curl localhost:${BOT_PORT}/actuator/health   # bot
curl localhost:${BOT_PORT}/actuator/prometheus  # metrics
```

## Stopping

```bash
docker compose down       # stop, keep data
docker compose down -v    # stop + delete volumes (reset DB)
```
