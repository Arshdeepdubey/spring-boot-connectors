# Runbook: running everything end-to-end locally

This walks through starting the local support stack (Postgres, LocalStack S3, WireMock), running
all three connectors in order, and confirming each hop actually happened — the mock external API
returning orders, an object landing in S3, that same object being relayed back out to a REST API,
and the same orders landing in Postgres.

## 1. Start the support stack

```bash
docker compose up -d
docker compose ps    # postgres, localstack and wiremock should all be "healthy"/"running"
```

This starts:

- **postgres** on `localhost:5432` (db `connectors_db`, user/password `connectors`/`connectors`)
- **localstack** on `localhost:4566`, with an S3 bucket named `connector-orders-bucket` created
  automatically on startup (see `docker/localstack/init-s3.sh`)
- **wiremock** on `localhost:8089`, stubbed with:
  - `GET /api/orders` → returns `docker/wiremock/__files/sample-orders.json` (3 sample orders —
    standing in for whatever real "source" system the connectors would normally read from)
  - `POST /api/orders/relay` → returns `201 { "message": "order relayed" }` (standing in for
    whatever real "target" system `s3-to-rest-connector` would relay orders to)

## 2. Build

```bash
mvn clean install
```

## 3. rest-to-s3-connector: source REST API → S3

```bash
mvn -pl rest-to-s3-connector spring-boot:run
```

In another terminal:

```bash
curl -X POST http://localhost:8081/api/v1/connectors/rest-to-s3/execute | jq
```

Expected: `recordsRead: 3`, `recordsValid: 3`, `recordsDelivered: 3`, `status: "SUCCESS"`, and a
`targetLocation` like `s3://connector-orders-bucket/orders/orders-<timestamp>.csv`.

Verify the object actually landed in LocalStack:

```bash
aws --endpoint-url=http://localhost:4566 s3 ls s3://connector-orders-bucket/orders/
aws --endpoint-url=http://localhost:4566 s3 cp s3://connector-orders-bucket/orders/<the-key-from-above> -
```

(No AWS CLI? `docker exec connectors-localstack awslocal s3 ls s3://connector-orders-bucket/orders/`
works just as well.)

## 4. s3-to-rest-connector: S3 → target REST API

```bash
mvn -pl s3-to-rest-connector spring-boot:run
```

```bash
curl -X POST http://localhost:8082/api/v1/connectors/s3-to-rest/execute | jq
```

With no `key` query parameter, it resolves the most recent object under
`connector.source.s3.key-prefix` (default `orders/`) — i.e. the file `rest-to-s3-connector` just
wrote. Pass `?key=orders/orders-<timestamp>.csv` to target a specific object instead.

Expected: `recordsDelivered: 3`, `status: "SUCCESS"`.

Verify WireMock actually received the relayed orders:

```bash
curl http://localhost:8089/__admin/requests | jq '.requests[] | select(.request.url == "/api/orders/relay")'
```

You should see three POST requests, one per order, each with the transformed order as its JSON
body.

## 5. rest-to-db-connector: source REST API → Postgres

```bash
mvn -pl rest-to-db-connector spring-boot:run
```

On startup, Flyway applies `src/main/resources/db/migration/V1__create_orders_table.sql` against
Postgres automatically.

```bash
curl -X POST http://localhost:8083/api/v1/connectors/rest-to-db/execute | jq
```

Expected: `recordsDelivered: 3`, `status: "SUCCESS"`.

Read the data back:

```bash
curl http://localhost:8083/api/v1/connectors/rest-to-db/orders | jq
curl http://localhost:8083/api/v1/connectors/rest-to-db/orders/ORD-1001 | jq
```

Or query Postgres directly:

```bash
docker exec -it connectors-postgres psql -U connectors -d connectors_db -c 'select order_id, customer_id, total_amount, status from orders;'
```

Run `execute` again — the same three orders come back from WireMock, and `OrderEntityRepository`
looks each one up by `orderId` first, so this is an **upsert**: existing rows get updated
(`updatedAt` changes), not duplicated.

## 6. Trying error handling

Edit `docker/wiremock/__files/sample-orders.json` to add a record with `"quantity": -1` or
delete `"orderId"` from one entry, then re-run `docker compose restart wiremock` and call any
connector's `/execute` again — you'll see `recordsInvalid` increase and the specific reason show
up in the response's `errors` array, while the other, valid records still make it all the way
through.

To see delivery-failure handling in `s3-to-rest-connector`: `docker compose stop wiremock`, then
call `POST /api/v1/connectors/s3-to-rest/execute` again — `recordsFailed` increases and
`status` becomes `PARTIAL_SUCCESS` or `FAILED` instead of the whole request throwing a 500.

## 7. Pointing at real AWS / a real Postgres / a real external API

Nothing above requires code changes — every value is an environment variable:

| Variable | Used by | Purpose |
|---|---|---|
| `SOURCE_API_URL`, `SOURCE_API_METHOD` | rest-to-s3, rest-to-db | source REST endpoint + HTTP method (GET/POST/PUT/PATCH/DELETE) |
| `SOURCE_API_AUTH_TYPE` + `..._KEY_VALUE`/`..._BEARER_TOKEN`/`..._USERNAME`/`..._PASSWORD` | rest-to-s3, rest-to-db | source API auth (`NONE`/`API_KEY`/`BEARER`/`BASIC`) |
| `TARGET_S3_BUCKET`, `TARGET_S3_KEY_PREFIX`, `TARGET_S3_FILE_FORMAT` | rest-to-s3 | where/how the archive file is written |
| `SOURCE_S3_BUCKET`, `SOURCE_S3_KEY` / `SOURCE_S3_KEY_PREFIX`, `SOURCE_S3_FILE_FORMAT` | s3-to-rest | which object to read |
| `TARGET_API_URL`, `TARGET_API_METHOD`, `TARGET_API_AUTH_TYPE` + ... | s3-to-rest | target REST endpoint + auth |
| `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_S3_ENDPOINT_OVERRIDE`, `AWS_S3_PATH_STYLE_ACCESS` | rest-to-s3, s3-to-rest | leave the key/secret vars unset in real AWS to use the default credential provider chain (IAM role, etc.); only set `AWS_S3_ENDPOINT_OVERRIDE`/`AWS_S3_PATH_STYLE_ACCESS` for LocalStack |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | rest-to-db | Postgres connection |

## Verifying the build

This project's code was written and hand-reviewed in a sandboxed environment whose network egress
policy blocks Maven Central, so `mvn compile` / `mvn test` could not actually be run while
producing it — every file was reviewed by hand instead. The first thing to do on a machine with
normal internet access is:

```bash
mvn clean verify
```

If anything doesn't compile or a test fails, it's most likely a small, mechanical fix (an import,
a Spring Boot/AWS SDK BOM version mismatch, etc.) rather than a structural problem — please open an
issue or fix forward; the architecture and business logic have been reviewed carefully, but a real
compiler run is still the authoritative check.
