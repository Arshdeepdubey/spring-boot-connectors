# rest-to-db-connector

Reads orders from a source REST API, validates and transforms them, and upserts them into a
PostgreSQL `orders` table (schema managed by Flyway).

Full pipeline/config concepts: see [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md). Full
local walkthrough: see [`../docs/RUNBOOK.md`](../docs/RUNBOOK.md).

## Run

```bash
mvn -pl rest-to-db-connector spring-boot:run
```

Default port: `8083`. Requires a reachable Postgres (see `docker-compose.yml` at the repo root).

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/connectors/rest-to-db/execute` | run the pipeline once (upsert by `orderId`) |
| GET | `/api/v1/connectors/rest-to-db/orders` | paginated list of persisted orders |
| GET | `/api/v1/connectors/rest-to-db/orders/{orderId}` | a single order (404 if not found) |
| GET | `/api/v1/connectors/rest-to-db/health` | liveness check |

## Key configuration (env vars)

| Variable | Default | Purpose |
|---|---|---|
| `SOURCE_API_URL` | `http://localhost:8089/api/orders` | source REST endpoint |
| `SOURCE_API_METHOD` | `GET` | one of GET/POST/PUT/PATCH/DELETE |
| `SOURCE_API_AUTH_TYPE` | `NONE` | one of NONE/BASIC/API_KEY/BEARER (plus matching credential vars) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/connectors_db` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `connectors` / `connectors` | DB credentials |

See `src/main/resources/application.yml` for the complete list.
