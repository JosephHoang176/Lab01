# Performance notes

## Baseline (before Day 12)

* `GET /products` always called `Products.findAll`, even when the result had
  not changed.
* Product writes did not invalidate any shared read state.
* Order listing loaded orders first and then lazily loaded `OrderItems` while
  mapping each order. A response containing `N` orders therefore issued one
  order query plus up to `N` item queries (the N+1 pattern).
* Status and date predicates were applied in Java, so the database could not
  use an index to reduce the order-listing result set.

## Day 12 implementation

Product listing now uses a cache-aside flow with the Redis key
`products:all` and a five-minute TTL:

1. Read and deserialize the key.
2. On a miss (or Redis failure), read SQL Server and populate the key.
3. Delete the key after a successful product create, update, or delete.

Redis is deliberately best effort; SQL Server remains the source of truth.

Order listing and order lookup use `left join fetch` for `Order.lines`.
Filtered listings pass status and the half-open created-at range to the
database before the response mapper runs. This keeps the number of SQL
round-trips constant for a page/list request.

## Before/after measurements

The query-count comparison is deterministic from the implementation:

| Scenario | Before Day 12 | After Day 12 |
|---|---:|---:|
| `GET /products` database reads after warm-up | 1 per request | 0 on a Redis hit |
| Order listing with `N` orders and lazy lines | 1 + up to `N` queries | 1 `JOIN FETCH` query |
| Filter evaluation | Java/application process | SQL Server predicates |

The latency acceptance criterion is **p95 < 200 ms with 10,000 products**.
It must be measured against the deployed SQL Server and Redis instances
because this repository does not contain a production-sized dataset or a
benchmark runner. Record the actual values from the same dataset and load
profile here:

| Run | Dataset | Redis state | Requests | p50 | p95 | Result |
|---|---:|---|---:|---:|---:|---|
| Before | 10,000 products | disabled | 1,000 | _ms | _ms | baseline |
| After | 10,000 products | warm | 1,000 | _ms | _ms | target: p95 < 200 ms |

## Indexes and query-plan verification

Flyway migration `V6__add_order_listing_indexes.sql` adds:

* `IX_Orders_Status_CreatedAt` for the filtered listing (with common response
  columns included).
* `IX_OrderItems_ProductId` for product/order-item lookups.
* `IX_Products_Category_Active` for active category browsing.

On a staging database, compare the actual execution plan and IO before and
after applying V6:

```sql
SET STATISTICS IO, TIME ON;
SELECT o.id, o.status, o.created_at
FROM dbo.Orders AS o
WHERE o.status = N'PAID'
  AND o.created_at >= '2026-01-01T00:00:00+00:00'
  AND o.created_at <  '2026-02-01T00:00:00+00:00';
SET STATISTICS IO, TIME OFF;
```

The expected plan uses an index seek (or a range seek) on
`IX_Orders_Status_CreatedAt`, rather than a full table scan. Confirm this with
the actual plan in SQL Server Management Studio and record logical reads and
elapsed time for the production-sized dataset.

## Repeatable checks

```powershell
mvn -B -DskipTests package
mvn -B test
mvn -B -DskipUnitTests=true verify
```

The last command runs the `*IT` order-flow tests through Maven Failsafe.
For a production comparison, capture request latency (p50/p95), SQL query
count, and Redis hit ratio at the same request volume before and after rollout.
