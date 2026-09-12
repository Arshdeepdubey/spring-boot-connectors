# rest-to-s3-connector

Reads orders from a source REST API, validates and transforms them, converts the batch to a
CSV or JSON file, and uploads it to S3.

Full pipeline/config concepts: see [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md). Full
local walkthrough: see [`../docs/RUNBOOK.md`](../docs/RUNBOOK.md).

## Run

```bash
mvn -pl rest-to-s3-connector spring-boot:run
```

Default port: `8081`.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/connectors/rest-to-s3/execute` | run the pipeline once |
| GET | `/api/v1/connectors/rest-to-s3/health` | liveness check |

## Key configuration (env vars)

| Variable | Default | Purpose |
|---|---|---|
| `SOURCE_API_URL` | `http://localhost:8089/api/orders` | source REST endpoint |
| `SOURCE_API_METHOD` | `GET` | one of GET/POST/PUT/PATCH/DELETE |
| `SOURCE_API_AUTH_TYPE` | `NONE` | one of NONE/BASIC/API_KEY/BEARER (plus matching credential vars) |
| `TARGET_S3_BUCKET` | `connector-orders-bucket` | destination bucket |
| `TARGET_S3_KEY_PREFIX` | `orders/` | destination key prefix |
| `TARGET_S3_FILE_FORMAT` | `CSV` | `CSV` or `JSON` |
| `AWS_REGION` | `us-east-1` | AWS region |
| `AWS_S3_ENDPOINT_OVERRIDE` | *(unset)* | set to `http://localhost:4566` for LocalStack |
| `AWS_S3_PATH_STYLE_ACCESS` | `false` | set `true` for LocalStack |

See `src/main/resources/application.yml` for the complete list.
