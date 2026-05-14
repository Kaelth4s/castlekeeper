# 🏰 Добро пожаловать в CastleKeeper, путник!

Ты стоишь у ворот цифровой крепости. Этот vault — живой архив проекта **CastleKeeper**: телеграм-бота, REST API и базы данных средневековых замков.

---

## 🗺️ Что здесь

- **📦 Документация** — `docs/`: деплой, разработка, архитектура, конфигурация, решение проблем.
- **📝 Заметки** — `notes/`: планы, canvas-диаграммы, контракты API.
- **🤖 Бот** — `bot/`: Spring Boot + TelegramBots, 5 крыльев CRUD, машина состояний.
- **⚙️ Сервер** — `server/`: Spring Boot + JPA + Flyway, REST API для 5 сущностей.
- **🗄️ База данных** — PostgreSQL 16 + Redis 7, поднимаются через Docker Compose.
- **📦 DTO** — `dto/`: общий контракт между сервером и ботом.

---

## 🚀 Быстрый старт

```bash
cp .env.example .env           # укажи DB_PASSWORD + TELEGRAM_BOT_TOKEN
docker compose up -d --build    # сборка + запуск всего
```

Сервер: `http://localhost:8080/swagger-ui.html`. Бот: отправь `/start` в Telegram.

---

## 🤖 Команды бота

Reply Keyboard для навигации, Inline Keyboard для CRUD-пикеров.

### Навигация
| `/start` | `/menu` | `/cancel` |
|----------|---------|-----------|

### CRUD — 5 сущностей
- 🏰 Замки (7-шаговый wizard добавления)
- 👥 Авторы (inline-выбор типа)
- 🏷️ Типы авторов
- 🧱 Материалы
- 🔨 Реконструкции

> 📖 Полный справочник: [[../docs/bot-commands]]

---

## 📚 Полезные ссылки

- [[README]] — главная страница
- [[../docs/architecture]] — архитектура
- [[../docs/development]] — разработка
- [[../docs/deployment]] — деплой
- [[../docs/configuration]] — конфигурация
- [[../docs/troubleshooting]] — решение проблем
- [[bot/functional]] — список функций бота
- [[bot/dialogs]] — диаграмма диалогов
- [[server]] — контракты API
- [[database/schema]] — ERD-схема

---

## ⚔️ Легенда

Пять крыльев, четыре контейнера Docker, три Maven-модуля, два Spring Boot-приложения — и одна база данных, хранящая историю всех твердынь мира. Ты — часть этой легенды.

*Хранитель замков, kaelth4s*
