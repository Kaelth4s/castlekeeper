# 🏰 CastleKeeper

**Telegram bot — castle keeper**  
Ask it about a castle, and it will answer like a real gatekeeper!

---

## ⚔️ About

CastleKeeper is an ecosystem for managing a database of medieval castles through Telegram.  
You message the bot, and it returns a list of strongholds from a real PostgreSQL database.  
Add a new castle via REST API — and the bot is ready to tell its story right away.

The project was born from a desire to learn Spring Boot, Docker, and clean architecture — all wrapped in a castle theme to make learning more engaging.

---

## 🏗 Architecture

| Component          | Technology                       | Description                                   |
|--------------------|----------------------------------|-----------------------------------------------|
| **Telegram Bot**   | Java 25 (LTS), Maven             | Processes commands, calls REST API            |
| **REST API**       | Spring Boot 3, Java 17, Flyway   | CRUD for castles, authors, author types, materials |
| **Database**       | PostgreSQL 16 (Docker)           | Stores castles, authors, materials, reconstructions |
| **Documentation**  | Obsidian (root vault)            | Notes, architecture, API contracts, canvas diagrams |
| **Infrastructure** | Docker Compose                   | One-command PostgreSQL setup                  |

### Data flow

```
Telegram (user) → Bot (Java 25) → HTTP → Server (Spring Boot 3, Java 17) → PostgreSQL
```

> 💡 Why different Java versions? The bot uses JDK 25 as LTS, the server uses classic Java 17 for maximum compatibility with Spring Boot 3.2+.

---

## 📂 Project Structure

```
castlekeeper/
├── .obsidian/              # Obsidian vault config (root-level vault)
├── bot/                    # Telegram bot (Java 25 + Maven)
│   ├── pom.xml
│   └── src/main/java/org/kaelth4s/castlekeeper/
├── server/                 # REST API (Spring Boot 3, Java 17, Maven)
│   ├── pom.xml
│   └── src/main/java/org/kaelth4s/castlekeeper/server/
├── docs/                   # Documentation
├── notes/                  # Working notes & plans (Obsidian)
│   ├── welcome.md          # Vault welcome page
│   ├── summary.canvas      # High-level overview canvas
│   ├── server.md           # API endpoint contracts
│   ├── bot/
│   │   ├── dialogs.canvas  # Bot dialog flow diagram
│   │   └── functional.md   # Bot feature list
│   └── database/
│       ├── schema.md       # ERD diagram (Mermaid, generated)
│       └── schema-draft.yaml  # Table definitions (YAML)
├── scripts/                # Utility scripts
│   ├── generate_schema.py  # YAML → Mermaid ERD generator
│   ├── generate_schema.bat # Windows wrapper
│   └── generate_schema.sh  # Linux wrapper
├── docker-compose.yml      # PostgreSQL 16 (Docker Compose)
├── .env.example            # Environment variables template
├── README.md               # English version
└── README-ru.md            # Russian version
```

---

## 🗄 Database Schema

The database consists of **5 entities** with relationships:

```
author_type ──< author ──< castle >── material
                 │            │
                 └──< reconstruction
```

| Table | Description | Key Columns |
|-------|-------------|-------------|
| `author_type` | Author category (chronicler, builder, etc.) | `id`, `name` (unique), `description` |
| `author` | Castle author / builder | `id`, `name`, `author_type_id` (FK) |
| `material` | Building material | `id`, `name` (unique) |
| `castle` | The castle itself | `id`, `name`, `description`, `author_id` (FK), `built_year`, `destroyed_year`, `height_m`, `material_id` (FK) |
| `reconstruction` | Reconstruction event | `id`, `castle_id` (FK), `author_id` (FK), `reconstruction_year` |
| `author_type` | Author title category | `id`, `name` (unique), `description` |

> 📖 Full ERD diagram: `notes/database/schema.md` — auto-generated from `schema-draft.yaml` via `generate_schema.py`.

---

## 🤖 Bot Commands

The bot responds to Telegram messages with a medieval narrative style.  
Selection-based actions use **inline keyboards** (5 items per page with ◀ ▶ pagination) — no manual ID entry.

### 🏰 Castles
| Command / Action           | Description                                                                                        |
| -------------------------- | -------------------------------------------------------------------------------------------------- |
| `/castles`                 | List all castles (text output)                                                                     |
| `/castlemenu` → 👁️ Select | View castle by picking from inline keyboard                                                        |
| `/random`                  | Random castle (text output)                                                                        |
| `/addcastle`               | 7-step wizard: name → desc → author picker → year → destroyed → height → material picker → confirm |
| `/castlemenu` → ✒️ Edit    | Pick castle → pick field → enter value / pick from list → confirm                                  |
| `/castlemenu` → 💥 Delete  | Pick castle → confirm ✅/❌                                                                          |

### 👥 Authors
| Command / Action           | Description                                                              |
| -------------------------- | ------------------------------------------------------------------------ |
| `/authors`                 | List all authors (text output)                                           |
| `/authormenu` → 👁️ Select | View author by picking from inline keyboard                              |
| `/addauthor`               | Wizard: name → type picker → confirm                                     |
| `/authormenu` → ✒️ Edit    | Pick author → pick field (name/type) → enter value / pick type → confirm |
| `/authormenu` → 💥 Delete  | Pick author → confirm ✅/❌                                                |

### 🏷️ Author Types
| Command / Action         | Description                                    |
| ------------------------ | ---------------------------------------------- |
| `/author_types`          | List all types (text output)                   |
| `/addauthor_type`        | Wizard: name → description → confirm           |
| `/atypemenu` → ✒️ Edit   | Pick type → pick field → enter value → confirm |
| `/atypemenu` → 💥 Delete | Pick type → confirm ✅/❌                        |

### 🧱 Materials
| Command / Action | Description |
|------------------|-------------|
| `/materials` | List all materials (text output) |
| `/materialmenu` → 👁️ Select | View material by picking from inline keyboard |
| `/addmaterial` | Enter name → confirm |
| `/materialmenu` → ✒️ Edit | Pick material → enter new name → confirm |
| `/materialmenu` → 💥 Delete | Pick material → confirm ✅/❌ |

### 🏰 Navigation
| Command | Description |
|---------|-------------|
| `/start` | Return to the castle gates |
| `/menu` | Return to the main hall (3 wings) |
| `/help` | Open the foliant with all commands |

### 💡 UX Design
- **Text lists**: `/castles`, `/authors`, `/materials`, `/random` — simple text output, no buttons
- **Inline keyboard pickers**: all "Select", "Edit", "Delete" actions — 5 items per page with ◀/▶ pagination
- **Wizards**: multi-step forms with confirmation screens
- **FK fields**: always picked from inline keyboard lists, never as raw IDs

> 🗺️ Full dialog flow: `notes/bot/dialogs.canvas` (Obsidian Canvas)

---

## 🔌 API Endpoints

**Base URL:** `http://localhost:8080/api/` (local) or `https://castlekeeper.kaelth4s.ru/api` (production)

### 🏰 Castles
| Method | Endpoint              | Description           |
| ------ | --------------------- | --------------------- |
| GET    | `/api/castles`        | List all castles      |
| GET    | `/api/castles/random` | Get a random castle   |
| GET    | `/api/castles/{id}`   | Get castle by ID      |
| POST   | `/api/castles`        | Create a castle       |
| PUT    | `/api/castles/{id}`   | Update a castle       |
| DELETE | `/api/castles/{id}`   | Delete a castle       |

### 👥 Authors
| Method | Endpoint             | Description           |
| ------ | -------------------- | --------------------- |
| GET    | `/api/authors`       | List all authors      |
| GET    | `/api/authors/{id}`  | Get author by ID      |
| POST   | `/api/authors`       | Create an author      |
| PUT    | `/api/authors/{id}`  | Update an author      |
| DELETE | `/api/authors/{id}`  | Delete an author      |

### 🏷️ Author Types
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/author-types` | List all author types |
| GET | `/api/author-types/{id}` | Get type by ID |
| POST | `/api/author-types` | Create a type |
| PUT | `/api/author-types/{id}` | Update a type |
| DELETE | `/api/author-types/{id}` | Delete a type |

### 🧱 Materials
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/materials` | List all materials |
| GET | `/api/materials/{id}` | Get material by ID |
| POST | `/api/materials` | Create a material |
| PUT | `/api/materials/{id}` | Update a material |
| DELETE | `/api/materials/{id}` | Delete a material |

---

## 📄 License

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This project is distributed under the MIT license.  
Full text: [LICENSE](LICENSE)

---

## 👤 Author

**kaelth4s** (GitHub) — keeper of this digital castle.  
Project started in 2026 and continues to be built, stone by stone.
