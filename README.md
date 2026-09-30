# Order Report Service

## Local configuration

Copy `.env.example` into your local environment or configure the variables in
the IntelliJ run configuration. Do not commit real credentials.

Required variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
PRICING_SERVICE_URL
```

The service listens on port `8080` by default and calls the pricing service at
`PRICING_SERVICE_URL`.

The interactive `OrderCli` is disabled for the web application by default.
Enable `SPRING_PROFILES_ACTIVE=cli` only when running with an interactive
terminal.

## Verification endpoints

```http
GET http://localhost:8080/health
GET http://localhost:8080/version
```

Every request receives an `X-Correlation-ID` response header. If the caller
does not provide one, the service generates it and includes it in structured
JSON logs. The same ID is forwarded to the pricing service through Feign.

## Build and test

```powershell
mvn test
mvn -DskipTests package
```

## Docker

Build the application before building the image:

```powershell
mvn -DskipTests package
docker build -t order-report-cli:latest .
```

When running the service in Docker, configure `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, and `PRICING_SERVICE_URL` with environment variables. When the
pricing service is another container, use its Compose service name instead of
`localhost`.

The Docker image runs the HTTP API and does not enable the interactive `cli`
profile.