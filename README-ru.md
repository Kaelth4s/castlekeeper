# 🏰 CastleKeeper

**Телеграм-бот — смотритель замков** · Спроси — и ключник ответит.

> 🇬🇧 [English](README.md) · 🇷🇺 Русский

---

## ⚡ Запуск

```bash
cp .env.example .env         # укажи DB_PASSWORD + TELEGRAM_BOT_TOKEN
docker compose up -d --build  # сборка + запуск всех 4 сервисов
```

Сервер → `http://localhost:8080/swagger-ui.html` · Бот → отправь `/start` в Telegram.

## 🏗️ Архитектура

```
docker compose up -d
  ├── postgres:16-alpine  (:5432)
  ├── redis:7-alpine      (:6379)
  ├── server (JVM 21)     (:8080)
  └── bot (JVM 21)        (:8081)

Бот ──HTTP──→ Сервер ──JPA──→ PostgreSQL  ·  Бот ──→ Redis (состояния диалогов)
```

| Компонент | Стек |
|-----------|------|
| Бот | Spring Boot 3 · TelegramBots 6.9 · RestClient · DialogStateMachine |
| Сервер | Spring Boot 3 · JPA/Hibernate · Flyway · Swagger |
| БД | PostgreSQL 16 (5 сущностей) · Redis 7 (опционально) |

## 📂 Структура

```
castlekeeper/
├── dto/              Общие DTO (10 Request/Response + ApiError)
├── server/           REST API (controller → service → repository → model)
├── bot/              Telegram-бот (callback → client → dispatcher → handler → dialog)
├── docs/             Деплой · Разработка · Архитектура · Конфигурация · Проблемы
├── docker-compose.yml   Все 4 сервиса
└── .env.example      Шаблон переменных окружения
```

## 🤖 Бот — 5 крыльев

**Reply Keyboard** для навигации · **Inline Keyboard** для CRUD-пикеров (5/стр ◀▶)

| `/start` | Врата замка |
| `/menu` | Главный зал: 🏰 Замки · 👥 Авторы · 🏷️ Титулы · 🧱 Материалы · 🔨 Реконструкции |
| `/cancel` | Выйти из wizard'а |

В каждом крыле: 📜 Список · 👁 Выбрать · 🏗 Добавить (wizard) · ✒ Изменить · 💥 Удалить

## 🔌 REST API

База: `http://localhost:8080/api/` · Swagger: `/swagger-ui.html`

5 ресурсов: `castles` (+`/random`), `authors`, `author-types`, `materials`, `reconstructions`

| Метод | Статус |
|--------|--------|
| GET коллекция | 200 |
| GET по ID | 200 / 404 |
| POST | 201 / 400 |
| PUT | 200 / 400 / 404 |
| DELETE | 204 / 404 |

> DTO-паттерн: POST/PUT принимают FK как `Long` ID. GET возвращает вложенные объекты.

## 📖 Документация

| Документ | О чём |
|----------|-------|
| [`deployment.md`](docs/deployment.md) | Деплой, reverse proxy, health checks |
| [`development.md`](docs/development.md) | Быстрый старт, структура, тесты, добавление фич |
| [`architecture.md`](docs/architecture.md) | Архитектура, DTO-паттерн, design decisions |
| [`configuration.md`](docs/configuration.md) | Все env vars · properties · переменные Docker |
| [`troubleshooting.md`](docs/troubleshooting.md) | Бот молчит · БД ошибки · порты · логи |
| [`bot-commands.md`](docs/bot-commands.md) | Команды · wizard'ы · машина состояний |

## 🧪 Тесты

```bash
./mvnw test   # 27 тестов: DialogStateMachine(6) CastleHandler(6) MaterialHandler(5) AuthorHandler(4) AuthorTypeHandler(3) ReconstructionHandler(3)
```

## 📄 Лицензия · 👤 Автор

MIT — **kaelth4s**, 2026
