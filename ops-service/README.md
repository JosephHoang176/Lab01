# Ops Service

Ops Service is an independent Spring Boot service that reads reporting data from
`OpsServiceDb`. Core API is not a runtime dependency and is not started by the
Docker Compose stack.

## Build and run locally

```powershell
mvn clean verify
$env:DB_HOST="localhost"
$env:DB_PORT="1433"
$env:DB_NAME="OpsServiceDb"
$env:DB_USERNAME="orderapp"
$env:DB_PASSWORD="your-password"
java -jar target\ops-service-1.0.0-SNAPSHOT.jar
```

The SQL Server database must already exist. Flyway creates `dbo.reports`,
`dbo.notifications`, indexes, constraints and seed data.

## Docker Compose

```powershell
mvn clean package
docker compose up --build
```

The existing SQL Server instance is shared by the Core database
`OrderReportDb` and the Ops database `OpsServiceDb`. Compose starts only Ops
Service and connects to the host SQL Server through `host.docker.internal`.
Core API is not started. The application uses `OpsServiceDb` explicitly through
`DB_NAME=OpsServiceDb`.

## API

```text
GET http://localhost:8082/reports/daily
X-Correlation-ID: demo-correlation-id
```

Example response:

```json
[
  {
    "reportId": 35,
    "reportDate": "2026-10-09",
    "currency": "VND",
    "ordersCreatedCount": 10,
    "ordersPaidCount": 8,
    "ordersFulfilledCount": 6,
    "ordersCancelledCount": 1,
    "pendingPaymentCountEod": 2,
    "ordersCreatedAmount": 1500000.00,
    "paymentsReceivedAmount": 1200000.00,
    "generatedAt": "2026-10-09T00:00:00+07:00"
  }
]
```

Every response includes `X-Correlation-ID`. Request and error logs are JSON
and include timestamp, level, service, method, path, status, durationMs and
correlationId.

## Isolation evidence

The SQL Server instance contains two independent databases:

```sql
SELECT name FROM sys.databases
WHERE name IN ('OpsServiceDb', 'OrderReportDb');
```

Ops Service is configured with `DB_NAME=OpsServiceDb`; Core API is not started.
Flyway migrations run only against `OpsServiceDb`, and the service entities use
only `dbo.reports` and `dbo.notifications`. `OrderReportDb` remains the Core
database and contains Core tables such as `Orders`, `Products` and `Users`.

Runtime verification commands:

```powershell
sqlcmd -S localhost,1433 -U orderapp -P hoang123 -C -d OpsServiceDb `
  -Q "SELECT DB_NAME() AS database_name; SELECT USER_NAME() AS database_user; SELECT name FROM sys.tables ORDER BY name"
```

Expected database evidence:

```text
database_name  database_user
OpsServiceDb   orderapp
notifications
reports
sysdiagrams
```

Runtime verification completed with Docker Desktop and SQL Server running:

```powershell
docker compose config
docker compose up --build -d
docker compose ps
curl.exe -i http://localhost:8082/reports/daily
curl.exe -i -H "X-Correlation-ID: acceptance-test-123" http://localhost:8082/reports/daily
```

The container remained `Up`, and both curl calls returned `HTTP/1.1 200`.
The correlation test returned:

```text
X-Correlation-ID: acceptance-test-123
```

The live response contained 35 report rows; the newest seeded row began with:

```json
{"reportId":35,"reportDate":"2026-10-09","currency":"VND",
"ordersCreatedCount":10,"ordersPaidCount":8,"ordersFulfilledCount":6,
"ordersCancelledCount":1,"pendingPaymentCountEod":2,
"ordersCreatedAmount":1500000.00,"paymentsReceivedAmount":1200000.00}
```

The 35 rows are not all produced by `V2__seed_reports_and_notifications.sql`.
That migration inserts exactly one report (`2026-10-09`, `VND`) and one
notification, guarded by `IF NOT EXISTS`. The current shared `OpsServiceDb`
already contained 34 additional historical reports from the existing Ops
database setup. The test suite uses an H2 database (`ops-service-test`) and
does not write these SQL Server rows; the application only reads reports for
this endpoint. The direct SQL verification was:

```sql
SELECT COUNT(*) FROM dbo.reports;
-- 35

SELECT report_date, currency, COUNT(*)
FROM dbo.reports
GROUP BY report_date, currency
ORDER BY report_date, currency;
-- 35 distinct (report_date, currency) pairs, each with count 1
```

`docker compose down` removes only the Ops container and its Docker network;
it does not delete or recreate the host SQL Server database. After
`docker compose down` followed by `docker compose up --build -d`, the
container was `Up`, the endpoint returned HTTP 200, and the SQL count remained
35. The seed report and seed notification each had count 1, so restarting did
not duplicate them. On a newly created empty `OpsServiceDb`, Flyway V1 creates
the schema and V2 contributes one report and one notification; historical rows
are data already present in the shared database, not rows generated at
application startup.

The container emitted valid JSON request logs, for example:

```json
{"timestamp":"2026-10-09T08:57:15.689653283Z","level":"INFO",
"message":"HTTP request completed","correlationId":"acceptance-test-123",
"method":"GET","path":"/reports/daily","status":200,"durationMs":27,
"service":"ops-service"}
```

The runtime SQL check returned `OpsServiceDb` and user `orderapp`; its tables
were `flyway_schema_history`, `notifications`, `reports` and `sysdiagrams`.
`OrderReportDb` remains separate and contains the Core tables. Core API was not
started during the successful container and API checks.
