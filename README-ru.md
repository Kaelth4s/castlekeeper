# 🏰 CastleKeeper

**Телеграм-бот — смотритель замков**  
Спроси его о замке — и он ответит, словно настоящий ключник!

---

## ⚔️ О проекте

CastleKeeper — это единая экосистема для управления базой данных средневековых замков через Telegram.  
Ты пишешь боту — и он возвращает список твердынь из настоящей базы PostgreSQL.  
Добавляешь новый замок через REST API — и бот тут же готов о нём рассказать.

Проект родился из желания изучить Spring Boot, Docker и чистую архитектуру, обернув всё в тематику замков — чтобы учиться было интереснее.

---

## 🏗 Архитектура

| Компонент         | Технология                       | Описание                                        |
|-------------------|----------------------------------|-------------------------------------------------|
| **Telegram Bot**  | Java 25 (LTS), Maven             | Обрабатывает команды, ходит в API               |
| **REST API**      | Spring Boot 3, Java 17, Flyway   | CRUD для замков, авторов и материалов           |
| **База данных**   | PostgreSQL 16 (Docker)           | Хранит замки, авторов, материалы и реконструкции |
| **Документация**  | Obsidian (vault в корне)         | Заметки, архитектура, API-контракты, canvas-диаграммы |
| **Инфраструктура**| Docker Compose                   | Поднимает PostgreSQL одной командой              |

### Поток данных

```
Telegram (пользователь) → Bot (Java 25) → HTTP → Server (Spring Boot 3, Java 17) → PostgreSQL
```

> 💡 Почему разные версии Java? Бот использует JDK 25 как LTS, сервер — классический Java 17 для максимальной совместимости со Spring Boot 3.2+.

---

## 📂 Структура проекта

```
castlekeeper/
├── .obsidian/              # Конфигурация Obsidian (vault на корень)
├── bot/                    # Telegram-бот (Java 25 + Maven)
│   ├── pom.xml
│   └── src/main/java/org/kaelth4s/castlekeeper/
├── server/                 # REST API (Spring Boot 3, Java 17, Maven)
│   ├── pom.xml
│   └── src/main/java/org/kaelth4s/castlekeeper/server/
├── docs/                   # Документация
├── notes/                  # Рабочие заметки и планы (Obsidian)
│   ├── welcome.md          # Приветственная страница vault
│   ├── summary.canvas      # Обзорный canvas
│   ├── server.md           # Контракты API
│   ├── bot/
│   │   ├── dialogs.canvas  # Диаграмма диалогов бота
│   │   └── functional.md   # Список функций бота
│   └── database/
│       ├── schema.md       # ERD-диаграмма (Mermaid, сгенерирована)
│       └── schema-draft.yaml  # Описание таблиц (YAML)
├── scripts/                # Вспомогательные скрипты
│   ├── generate_schema.py  # YAML → Mermaid ERD генератор
│   ├── generate_schema.bat # Обёртка для Windows
│   └── generate_schema.sh  # Обёртка для Linux
├── docker-compose.yml      # PostgreSQL 16 (Docker Compose)
├── .env.example            # Шаблон переменных окружения
├── README.md               # Английская версия
└── README-ru.md            # Русская версия (ты здесь)
```

> 🗒️ **Obsidian** открыт прямо на корень проекта, чтобы можно было линковать код из заметок.

---

## 🗄 Схема базы данных

База данных состоит из **5 сущностей** со связями:

```
author_type ──< author ──< castle >── material
                 │            │
                 └──< reconstruction
```

| Таблица | Описание | Ключевые колонки |
|---------|----------|------------------|
| `author_type` | Тип автора (летописец, строитель...) | `id`, `name` (уникальное), `description` |
| `author` | Автор / строитель замка | `id`, `name`, `author_type_id` (FK) |
| `material` | Строительный материал | `id`, `name` (уникальное) |
| `castle` | Сам замок | `id`, `name`, `description`, `author_id` (FK), `built_year`, `destroyed_year`, `height_m`, `material_id` (FK) |
| `reconstruction` | Событие реконструкции | `id`, `castle_id` (FK), `author_id` (FK), `reconstruction_year` |

> 📖 Полная ERD-диаграмма: `notes/database/schema.md` — генерируется из `schema-draft.yaml` скриптом `generate_schema.py`.

---

## 🤖 Команды бота

Бот отвечает в средневековом стиле, голосом ключника. Управляет тремя сущностями:

### 🏰 Замки
| Команда | Описание |
|---------|----------|
| `/castles` | Список всех замков |
| `/castle <ID>` | Замок по номеру |
| `/search <имя>` | Поиск замка по названию |
| `/random` | Случайный замок |
| `/addcastle` | Добавить замок |
| `/editcastle <ID>` | Изменить замок |
| `/deletecastle <ID>` | Удалить замок |

### 👥 Авторы
| Команда | Описание |
|---------|----------|
| `/authors` | Список всех авторов |
| `/author <ID>` | Автор по номеру |
| `/searchauthor <имя>` | Поиск автора по имени |
| `/addauthor` | Добавить автора |
| `/editauthor <ID>` | Изменить автора |
| `/deleteauthor <ID>` | Удалить автора |

### 🧱 Материалы
| Команда | Описание |
|---------|----------|
| `/materials` | Список всех материалов |
| `/material <ID>` | Материал по номеру |
| `/addmaterial` | Добавить материал |
| `/editmaterial <ID>` | Изменить материал |
| `/deletematerial <ID>` | Удалить материал |

### 🏰 Навигация
| Команда | Описание |
|---------|----------|
| `/start` | Вернуться к вратам замка |
| `/menu` | Вернуться в главный зал |
| `/help` | Фолиант со всеми командами |

> 🗺️ Полная диаграмма диалогов: `notes/bot/dialogs.canvas` (Obsidian Canvas)

---

## 🔌 API Эндпоинты

**Базовый URL:** `http://localhost:8080/api/` (локально) или `https://castlekeeper.kaelth4s.ru/api` (продакшн)

### 🏰 Замки
| Метод | Эндпоинт | Описание |
|--------|----------|----------|
| GET | `/api/castles` | Список всех замков |
| GET | `/api/castles/random` | Случайный замок |
| GET | `/api/castles/{id}` | Замок по ID |
| GET | `/api/castles?name=` | Поиск замка по имени |
| POST | `/api/castles` | Создать замок |
| PUT | `/api/castles/{id}` | Обновить замок |
| DELETE | `/api/castles/{id}` | Удалить замок |

### 👥 Авторы
| Метод | Эндпоинт | Описание |
|--------|----------|----------|
| GET | `/api/authors` | Список всех авторов |
| GET | `/api/authors/{id}` | Автор по ID |
| GET | `/api/authors?name=` | Поиск автора по имени |
| POST | `/api/authors` | Создать автора |
| PUT | `/api/authors/{id}` | Обновить автора |
| DELETE | `/api/authors/{id}` | Удалить автора |

### 🧱 Материалы
| Метод | Эндпоинт | Описание |
|--------|----------|----------|
| GET | `/api/materials` | Список всех материалов |
| GET | `/api/materials/{id}` | Материал по ID |
| POST | `/api/materials` | Создать материал |
| PUT | `/api/materials/{id}` | Обновить материал |
| DELETE | `/api/materials/{id}` | Удалить материал |

---

## 💎 Зачем это?

Этот проект был создан, чтобы:

- Освоить связку **Telegram Bot + Spring Boot + PostgreSQL**
- Попрактиковаться в **чистой модульной архитектуре**
- Вести документацию в **Obsidian** прямо внутри репозитория

А ещё — потому что замки это круто 🏰

---

## 👤 Автор

**kaelth4s** (GitHub) — ключник этого цифрового замка.  
Проект родился в 2026 году и продолжает строиться, камень за камнем.

---

## 📄 Лицензия

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Этот проект распространяется под лицензией MIT.  
Полный текст лицензии доступен в файле [LICENSE](LICENSE).
