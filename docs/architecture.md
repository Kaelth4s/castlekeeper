# Architecture

## Overview

CastleKeeper is a **client-server monolith** split into two independent runtimes:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Telegram User                            │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTPS (Telegram API)
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Bot (Java 25, Maven)                                            │
│  ┌────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │ Dispatcher  │→│ Dialog State  │→│  REST Client (HttpURL)  │  │
│  │ (commands)  │ │ Machine       │  │  → HTTP to Server       │  │
│  └────────────┘  └──────────────┘  └───────────┬────────────┘  │
└─────────────────────────────────────────────────┼────────────────┘
                                                  │ HTTP (REST/JSON)
                                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│  Server (Spring Boot 3, Java 17)                                 │
│                                                                   │
│  ┌───────────────┐   ┌──────────────┐   ┌─────────────────────┐ │
│  │  Controller    │→  │   Service     │→  │   Repository (JPA)  │ │
│  │  DTO in/out    │   │  DTO ↔ Entity │   │   Entity → DB       │ │
│  │  @Valid        │   │  @Transactional│  │                      │ │
│  └───────────────┘   └──────────────┘   └─────────────────────┘ │
│                                                                   │
│  Data flow:                                                       │
│  JSON → Request DTO → Service(resolves FK, maps) → Entity → DB   │
│  DB → Entity → Service(maps to DTO) → Response DTO → JSON        │
└──────────────────────────────────────────────────────────────────┘
                                                  │
                                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│  PostgreSQL 16 (Docker)                                          │
│  ┌──────────────┐  ┌────────┐  ┌──────────┐  ┌──────────┐     │
│  │ author_type   │  │ author │  │ castle   │  │ material │     │
│  └──────────────┘  └────────┘  └──────────┘  └──────────┘     │
│                    ┌──────────────────┐                          │
│                    │  reconstruction  │                          │
│                    └──────────────────┘                          │
└──────────────────────────────────────────────────────────────────┘
```

## Request Lifecycle

### Example: client sends `POST /api/castles` to create a castle

```
1. Client → Server: POST /api/castles
   Body: {"name":"Neuschwanstein","authorId":3,"builtYear":1869,"materialId":2}

2. Jackson deserializes JSON → CastleRequest DTO
   @Valid triggers Bean Validation (@NotBlank, @NotNull checks)

3. CastleController.create(request)
   → CastleService.create(request)

4. CastleService:
   a) resolveFks(): looks up Author(id=3) and Material(id=2) via repositories
   b) Maps CastleRequest → Castle entity (pure JPA, no Jackson annotations)
   c) repository.save(entity) → Hibernate generates INSERT
   d) Maps saved entity → CastleResponse DTO (with nested AuthorResponse, MaterialResponse)

5. Response DTO → Jackson serialization → JSON
   {"id":5,"name":"Neuschwanstein","author":{"id":3,"name":"Heinrich",...},"material":{...},...}
```

## DTO Pattern — Production Gold Standard

The project uses a strict **DTO (Data Transfer Objects)** pattern:

### Why DTOs over returning Entity directly?

| Without DTO                                                | With DTO                                                    |
| ---------------------------------------------------------- | ----------------------------------------------------------- |
| Entity knows about JSON (`@JsonProperty`, `@Transient`)    | Entity is pure JPA — zero presentation concerns             |
| Swagger shows DB schema details (lazy proxies, FK objects) | Swagger shows clean API contract                            |
| If you add GraphQL or gRPC, Entity must change             | New protocol = new DTO, Entity unchanged                    |
| LAZY fields cause serialization errors outside transaction | Service eagerly maps nested data within `@Transactional`    |
| Validating input vs output on same class is messy          | Request DTO = input validation. Response DTO = output shape |

### DTO layer structure

```
HTTP Request
    │
    ▼ Jackson deserialization
Request DTO       (@Valid, @NotBlank, FK as Long IDs)
    │
    ▼ Service maps
Entity            (Pure JPA: @Entity, @Column, @ManyToOne)
    │
    ▼ Hibernate
PostgreSQL
    │
    ▼ Hibernate returns
Entity            (LAZY fields loaded within @Transactional)
    │
    ▼ Service maps
Response DTO      (Flat structure, FK as nested DTOs, no LAZY proxies)
    │
    ▼ Jackson serialization
HTTP Response JSON
```

### Package layout in `server/src/main/java/.../server/`

| Package | Contains | Depends on |
|---------|----------|------------|
| `dto/` | Request/Response DTOs + `ApiError` | Jakarta Validation, Lombok |
| `controller/` | REST endpoints — DTO in, DTO out | `dto/`, `service/` |
| `service/` | Business logic, DTO ↔ Entity mapping, FK resolution, `@Transactional` | `dto/`, `model/`, `repository/` |
| `repository/` | Spring Data JPA interfaces | `model/` |
| `model/` | `@Entity` classes — pure JPA, no Jackson | Jakarta Persistence |
| `exception/` | Custom exceptions + `@RestControllerAdvice` handler | `dto/ApiError` |

## Component Roles

| Component | Role | Technology |
|-----------|------|------------|
| **Bot** | Handles Telegram messages, manages dialog state machine, renders inline keyboards, calls Server REST API | Java 25, Maven |
| **Server** | REST API: CRUD for castles/authors/types/materials. DTO validation, FK resolution, JSON serialization | Spring Boot 3, JPA/Hibernate, Flyway |
| **PostgreSQL** | Persistent storage. Schema managed by Flyway. 5 tables with FK relationships | PostgreSQL 16 (Docker Alpine) |

### Why split Bot and Server?

- **Separation of concerns**: bot deals with Telegram protocol; server deals with data.
- **Independent deployability**: restart server without dropping active dialogs.
- **Testability**: test API with `curl` / Swagger UI without Telegram.
- **Future-proof**: add Web UI, CLI, or another bot platform — all use the same REST API.

### Why monolith, not microservices?

The server is a **single process** with one database. Correct for this scale — splitting would introduce network latency and distributed transactions without benefit.

## Docker Environment

```
castlekeeper-db (postgres:16-alpine)
  ├── Port: ${DB_PORT}:5432
  ├── Volume: pgdata → /var/lib/postgresql/data
  └── Credentials from .env
```

- **Start DB**: `docker compose up -d`
- **Stop DB**: `docker compose down`
- **Reset DB**: `docker compose down -v && docker compose up -d`

The server runs **outside Docker** during development (`./mvnw spring-boot:run`).

## Documentation

| Resource | Description |
|----------|-------------|
| [API specification (OpenAPI JSON)](../server/src/main/resources/api-docs.json) | Machine-readable API contract |
| Swagger UI | `http://localhost:8080/swagger-ui.html` — interactive API explorer |
| [Bot commands](./bot-commands.md) | Full list of commands, arguments, dialog flows |
| [Database schema](./database-schema/README.md) | ERD + per-table documentation (auto-generated by tbls) |
| `.tbls.yml` | tbls config — connects to DB, reads schema, generates docs |
| `.github/workflows/docs.yaml` | CI pipeline: Flyway + tbls → commits updated schema docs |

## Key Design Decisions

- **Flyway over ddl-auto**: schema is versioned SQL files in `db/migration/`. Hibernate validates Entity ↔ Table but never modifies schema.
- **Constructor injection**: all beans receive dependencies via constructor — no `@Autowired` on fields.
- **DTO pattern**: Entity classes are pure JPA. Controller/Service exchange only DTOs. No Jackson annotations on entities.
- **FK resolution in Service**: Request DTOs carry FK IDs (`Long authorId`). Service resolves them to Entity references via repository lookup. Client never sends nested objects for FKs.
- **Global exception handler**: `@RestControllerAdvice` converts typed exceptions into structured `ApiError` JSON.
- **fail-on-unknown-properties**: Jackson rejects unrecognized JSON fields — catches typos at the API boundary.
- **Jakarta Validation**: `@NotBlank`, `@NotNull` on Request DTOs. `@Valid` on controller parameters.
- **LAZY fetching + open-in-view**: `FetchType.LAZY` on all `@ManyToOne` for query performance. `spring.jpa.open-in-view=true` keeps session alive during DTO mapping. Production alternative: `@EntityGraph` or JOIN FETCH queries instead of open-in-view (but this requires more code).
