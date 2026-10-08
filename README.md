# Order Report CLI - Day 12

[![CI](https://github.com/JosephHoang176/Lab01/actions/workflows/ci.yml/badge.svg)](https://github.com/JosephHoang176/Lab01/actions/workflows/ci.yml)

Spring Boot order and product API. Day 12 focuses on Redis cache-aside,
database query performance, N+1 detection, indexing, integration testing and
continuous integration.

## Day 12 status

### Completed

- Added cache-aside for `GET /products` using Redis.
- Added explicit product-cache invalidation after create, update and delete.
- Added a five-minute cache TTL.
- Added a database fallback when Redis is unavailable or a cache value cannot
  be read.
- Found and removed the order-listing N+1 pattern by fetching order lines with
  one `JOIN FETCH` query.
- Moved order status/date filtering into the database query.
- Added Flyway V6 indexes for order listing, order-item lookup and product
  browsing.
- Added six order-flow integration tests.
- Added Checkstyle linting and Maven Failsafe integration-test execution.
- Added GitHub Actions CI with the order:
  `build -> lint -> unit tests -> integration tests`.
- Added this README CI badge and performance documentation in
  [`docs/performance.md`](docs/performance.md).

### Not yet completed or not yet verified

- The real performance target has not been measured yet:
  `GET /products` p95 must be below 200 ms with 10,000 product rows.
- Before/after p50 and p95 values still need to be captured against the real
  SQL Server and Redis instances and filled into
  [`docs/performance.md`](docs/performance.md).
- The actual SQL Server execution plan has not been verified in SSMS with
  `IX_Orders_Status_CreatedAt`.
- The six integration tests currently use `@WebMvcTest` and mocked services;
  they do not yet exercise a real SQL Server and Redis instance.
- A real concurrent HTTP test for two buyers competing for the last stock unit
  has not yet been run.
- GitHub Actions does not yet provision SQL Server and Redis service
  containers.

The Day 12 implementation is complete in source code and CI structure. The
remaining items are environment-backed performance and end-to-end
verification, not missing cache or query-optimization code.

## APIs

All protected endpoints require:

```http
Authorization: Bearer <access-token>
```

### Product APIs

| Method | Endpoint | Access | Day 12 behavior |
|---|---|---|---|
| `GET` | `/products` | Authenticated | Reads `products:all` from Redis; cache miss reads SQL Server and populates Redis |
| `GET` | `/products/{id}` | Authenticated | Reads one product from SQL Server |
| `POST` | `/products` | `ADMIN` | Saves a product and deletes `products:all` |
| `PATCH` | `/products/{id}` | `ADMIN` | Updates a product and deletes `products:all` |
| `DELETE` | `/products/{id}` | `ADMIN` | Deletes a product and deletes `products:all` |

Example product body:

```json
{
  "id": 1,
  "sku": "LAPTOP-001",
  "name": "Laptop Dell",
  "category": "Electronics",
  "unitPrice": 25000000,
  "currency": "VND",
  "stock": 10,
  "active": true
}
```

### Order APIs

| Method | Endpoint | Access | Notes |
|---|---|---|---|
| `GET` | `/orders` | Authenticated | Uses one fetch-join query for orders and order lines |
| `GET` | `/orders/{id}` | Authenticated | Loads the order and lines without lazy N+1 loading |
| `POST` | `/orders` | Authenticated | Creates an order transactionally and reserves stock atomically |
| `POST` | `/orders/create` | Authenticated | Alias of `POST /orders` |
| `PATCH` | `/orders/status?orderId={id}&status={status}` | Authenticated | Changes order status |
| `PATCH` | `/orders/{id}/cancel` | Authenticated | Cancels an order |

`GET /orders` supports:

```text
/orders?status=PAID&from=2026-10-01&to=2026-10-08
```

Requests that create or mutate orders use the database-backed
`Idempotency-Key` mechanism.

### Supporting APIs

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/auth/register` | Register a user |
| `POST` | `/auth/login` | Issue access and refresh JWTs |
| `POST` | `/auth/refresh` | Issue a new token pair |
| `GET` | `/me` | Return the authenticated user |
| `POST` | `/orders/{id}/pay` | Start mock payment; idempotent |
| `POST` | `/webhooks/payment` | Verify and process payment webhook |
| `POST` | `/orders/{id}/invoice` | Upload a PDF/PNG/JPEG invoice after payment |
| `GET` | `/orders/{id}/invoice` | Download the latest invoice |
| `GET` | `/actuator/health` | Public health check |
| `GET` | `/actuator/info` | Public application information |

## Mechanisms implemented on Day 12

### Redis cache-aside

`ProductService.getAllProduct()` follows this flow:

1. Read the serialized product list from Redis key `products:all`.
2. Return the cached list on a valid cache hit.
3. On a miss, read all products from `ProductJPARepository`.
4. Serialize the result and store it in Redis with a five-minute TTL.
5. If Redis is unavailable, return the SQL Server result instead.

Product writes are the explicit invalidation points:

```text
create product -> save database -> delete products:all
update product -> save database -> delete products:all
delete product -> delete database -> delete products:all
```

SQL Server remains the source of truth; Redis is an optimization layer.

### N+1 detection and fix

The old order listing could execute one query for orders followed by one
lazy-loading query for `OrderItems` per order. For `N` orders, that produced
approximately `1 + N` queries.

The JPA repository now uses:

```java
select distinct o
from Order o
left join fetch o.lines
```

The filtered query also applies status and date predicates in SQL Server.
The expected result is one fetch query for the listing instead of a query per
order.

### Indexing and query planning

Flyway migration
[`V6__add_order_listing_indexes.sql`](src/main/resources/db/migration/V6__add_order_listing_indexes.sql)
adds:

```text
IX_Orders_Status_CreatedAt
IX_OrderItems_ProductId
IX_Products_Category_Active
```

`IX_Orders_Status_CreatedAt` supports filtered order listing. The execution
plan must still be checked on the real SQL Server dataset; an index definition
alone does not prove that SQL Server selected an index seek.

### Idempotency and inventory safety

Mutating order requests require an `Idempotency-Key`. The key is scoped by
HTTP method, request path and authenticated owner and is stored in SQL Server
with `PROCESSING`/`COMPLETED` state.

Order creation runs in a transaction and reserves stock with a conditional
update:

```sql
UPDATE Products
SET stock = stock - :quantity
WHERE id = :productId
  AND stock >= :quantity
```

When two transactions compete for the final unit, only the transaction that
updates one row succeeds.

## Tests and CI

Local commands:

```powershell
mvn -B -DskipTests package
mvn -B -DskipTests checkstyle:check
mvn -B test
mvn -B -DskipUnitTests=true verify
```

The six order-flow integration tests cover:

1. Listing orders.
2. Listing with a status filter.
3. Reading an order by ID.
4. Creating an order.
5. Changing order status.
6. Cancelling an order.

The GitHub Actions workflow is
[`ci.yml`](.github/workflows/ci.yml). It runs build, lint, unit tests and
integration tests as separate stages. The current integration tests are
controller-level tests; SQL Server/Redis-backed tests remain a follow-up item.

## Runtime configuration

Redis defaults to:

```text
localhost:6379
```

SQL Server and Flyway configuration is in
[`src/main/resources/application.yml`](src/main/resources/application.yml).
The invoice storage directory defaults to `data\invoices`.

For the performance target, populate 10,000 products, warm Redis with
`GET /products`, run a consistent load profile, and record p50/p95 values in
[`docs/performance.md`](docs/performance.md).

For the broader project architecture and technology list, see
[`PROJECT_OVERVIEW.md`](PROJECT_OVERVIEW.md).