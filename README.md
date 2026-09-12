# spring-boot-connectors

A small platform of independent Spring Boot integration **connectors**, each implementing the
same end-to-end shape:

```
source (REST API / S3)  ->  validate  ->  transform  ->  convert to file  ->  target (S3 / REST API / Postgres)
```

The business use case running through every connector is intentionally simple: an **order**
(`orderId`, `customerId`, `productName`, `quantity`, `price`, `orderDate`, `status`) originates
from an external REST API, gets archived to S3, relayed on to another REST API, and/or persisted
to Postgres, depending on which connector you run.

> Looking for the previous CRUD demo application that lived in this repository? It has been
> moved to [`legacy/posthere-app`](legacy/posthere-app) and is untouched. It is unrelated to the
> connectors below and is kept only for reference.

## Modules

| Module | Reads from | Writes to | Port |
|---|---|---|---|
| [`common`](common) | — | — | (library, not runnable) |
| [`rest-to-s3-connector`](rest-to-s3-connector) | Source REST API | AWS S3 (CSV/JSON) | 8081 |
| [`s3-to-rest-connector`](s3-to-rest-connector) | AWS S3 (CSV/JSON) | Target REST API | 8082 |
| [`rest-to-db-connector`](rest-to-db-connector) | Source REST API | PostgreSQL | 8083 |

`common` holds everything shared across connectors so each one stays small: a generic REST client
that supports GET/POST/PUT/PATCH/DELETE with pluggable auth (none / basic / API key / bearer), an
AWS S3 client wrapper, the CSV/JSON file converter, the validation/transformation contracts, the
`Order` domain model + its default validator/transformer, a consistent error response shape, and a
`PipelineResult` summary type every connector's `/execute` endpoint returns.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for how the pieces fit together and how to add
a new connector, and [`docs/RUNBOOK.md`](docs/RUNBOOK.md) for a full local end-to-end walkthrough.

## Prerequisites

- Java 17+
- Maven 3.9+ (or use your IDE's embedded Maven)
- Docker + Docker Compose, for the local Postgres / S3 (LocalStack) / mock-API (WireMock) stack

## Quick start

```bash
# 1. Start Postgres, LocalStack (S3) and a WireMock stand-in for the external REST APIs
docker compose up -d

# 2. Build everything
mvn clean install

# 3. Run a connector (each in its own terminal, or one at a time)
mvn -pl rest-to-s3-connector spring-boot:run
mvn -pl s3-to-rest-connector spring-boot:run
mvn -pl rest-to-db-connector spring-boot:run

# 4. Trigger a pipeline run
curl -X POST http://localhost:8081/api/v1/connectors/rest-to-s3/execute
curl -X POST http://localhost:8082/api/v1/connectors/s3-to-rest/execute
curl -X POST http://localhost:8083/api/v1/connectors/rest-to-db/execute
```

Every `/execute` call returns a JSON summary (`recordsRead`, `recordsValid`, `recordsInvalid`,
`recordsDelivered`, `recordsFailed`, `status`, `errors`) so you can see exactly what happened
without digging through logs — though the logs (console + `logs/<connector>.log`) have the same
information at DEBUG level for the `com.example.connectors` package.

The full walkthrough — including verifying the S3 object in LocalStack, and what WireMock
received — is in [`docs/RUNBOOK.md`](docs/RUNBOOK.md).

## Configuration

Every setting is a Spring `@ConfigurationProperties` class backed by `application.yml`, and every
value can be overridden with an environment variable (see each connector's `application.yml` for
the exact names, e.g. `SOURCE_API_URL`, `TARGET_S3_BUCKET`, `AWS_REGION`, `DB_URL`). Nothing
sensitive is committed: passwords, tokens and access keys all default to empty and are meant to be
injected via environment variables or a secrets manager in real deployments.

AWS credentials follow the standard SDK default credential provider chain (environment variables,
shared config/credentials file, container/instance IAM role) unless you explicitly set
`AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY`, which is only intended for local development against
LocalStack.

## Testing

Each module has unit tests (Mockito) for its pipeline logic and controller (`MockMvc`), plus a
couple of integration-style tests using an embedded WireMock server and, for `rest-to-db-connector`,
an in-memory H2 database (`@DataJpaTest`) so `mvn test` needs no external services.

```bash
mvn clean test
```

> **Note on this environment:** the connectors here were authored and hand-reviewed in a sandboxed
> session whose outbound network access does not include Maven Central, so `mvn compile`/`mvn test`
> could not be executed as part of producing this code. Please run `mvn clean verify` yourself
> once the code is on a machine with normal internet access — see
> [`docs/RUNBOOK.md`](docs/RUNBOOK.md#verifying-the-build) for what to expect.
