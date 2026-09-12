# s3-to-rest-connector

Reads an orders file from S3, validates and transforms the records, and relays each order to a
target REST API (one HTTP call per order).

Full pipeline/config concepts: see [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md). Full
local walkthrough: see [`../docs/RUNBOOK.md`](../docs/RUNBOOK.md).

## Run

```bash
mvn -pl s3-to-rest-connector spring-boot:run
```

Default port: `8082`.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/connectors/s3-to-rest/execute?key=<optional-s3-key>` | run the pipeline once; omit `key` to auto-resolve the latest object under the configured prefix |
| GET | `/api/v1/connectors/s3-to-rest/health` | liveness check |

## Key configuration (env vars)

| Variable | Default | Purpose |
|---|---|---|
| `SOURCE_S3_BUCKET` | `connector-orders-bucket` | source bucket |
| `SOURCE_S3_KEY` | *(unset)* | exact object key; if unset, resolves the latest under `SOURCE_S3_KEY_PREFIX` |
| `SOURCE_S3_KEY_PREFIX` | `orders/` | prefix to resolve the latest object from |
| `SOURCE_S3_FILE_FORMAT` | `CSV` | `CSV` or `JSON` — must match how the file was written |
| `TARGET_API_URL` | `http://localhost:8089/api/orders/relay` | target REST endpoint |
| `TARGET_API_METHOD` | `POST` | one of GET/POST/PUT/PATCH/DELETE |
| `TARGET_API_AUTH_TYPE` | `NONE` | one of NONE/BASIC/API_KEY/BEARER (plus matching credential vars) |
| `AWS_REGION` | `us-east-1` | AWS region |
| `AWS_S3_ENDPOINT_OVERRIDE` | *(unset)* | set to `http://localhost:4566` for LocalStack |
| `AWS_S3_PATH_STYLE_ACCESS` | `false` | set `true` for LocalStack |

See `src/main/resources/application.yml` for the complete list.
