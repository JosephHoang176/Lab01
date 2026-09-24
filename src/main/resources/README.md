# OrderHub Training Datasets
### VATEK Internship Program 2026 · Shared by every role

> **Read-only reference data.** Use these files from your own repository — download them into your lab folder, or point `json-server` at this folder. Do not edit them: every intern and mentor must work from identical data, so that lab results can be checked against `answer-key.json`.

All data is **fictional**. Company names are invented, email addresses use the reserved `example.com` domain, and phone numbers are placeholders. The files are generated from a fixed seed, so they are identical for every intake.

---

## Files

| File | Contents | Used by |
|---|---|---|
| [`orders.json`](orders.json) | **200 orders** from 1 July to 29 August 2026, with line items, totals and status timestamps | Every role's Week 1 labs · Backend seed data · AI `lookup_order` |
| [`products.json`](products.json) | **50 products** in 6 categories (3 are discontinued, `isActive: false`) | Order line items · Backend D7 seed |
| [`customers.json`](customers.json) | **20 B2B customers** with a pricing tier and discount | Customer names in orders · Backend L3 grouping |
| [`users.json`](users.json) | **5 staff users** (2 `ADMIN`, 3 `STAFF`) — no passwords | Backend D7 seed |
| [`shipments.json`](shipments.json) | **137 shipments**, one per `PAID` or `FULFILLED` order | Backend L3 mock shipping-status endpoint |
| [`order-events.json`](order-events.json) | **39 order events** (`order.created`, `order.status_changed`), in time order | Frontend D16 realtime mock |
| [`db.json`](db.json) | `orders` + `products` + `customers` + `shipments` in one file | `json-server` mock API — Frontend L2, AI L2, Backend L3 |
| [`answer-key.json`](answer-key.json) | Correct results for the Week 1 report labs | Mentors checking lab output |

---

## Order Model

```jsonc
{
  "id": 142,
  "code": "ORD-2026-0142",
  "customerId": 7,
  "customerName": "Sunrise Stationery Hub",
  "createdBy": 3,                         // users.json id
  "status": "PAID",                       // DRAFT | PENDING_PAYMENT | PAID | FULFILLED | CANCELLED
  "lines": [
    { "lineNo": 1, "productId": 12, "sku": "OFF-004", "productName": "Staples 24/6 (box of 5000)",
      "quantity": 10, "unitPrice": 22000, "lineTotal": 220000 }
  ],
  "subtotal": 220000,
  "discountPercent": 5,                   // from the customer's tier: STANDARD 0 · SILVER 5 · GOLD 10
  "discountAmount": 11000,
  "taxPercent": 10,                       // VAT
  "taxAmount": 20900,
  "total": 229900,
  "currency": "VND",
  "createdAt": "2026-08-14T10:32:05+07:00",
  "updatedAt": "2026-08-14T15:02:05+07:00",
  "paidAt": "2026-08-14T15:02:05+07:00",  // set for PAID and FULFILLED
  "fulfilledAt": null,                    // set for FULFILLED
  "cancelledAt": null                     // set for CANCELLED
}
```

*The example shows the shape; the values in `orders.json` differ.*

### Calculation rules

All amounts are whole **VND** integers — never use floating point for money.

| Field | Rule |
|---|---|
| `lineTotal` | `quantity × unitPrice` |
| `subtotal` | Sum of `lineTotal` |
| `discountAmount` | `round(subtotal × discountPercent / 100)` |
| `taxAmount` | `round((subtotal − discountAmount) × taxPercent / 100)` |
| `total` | `subtotal − discountAmount + taxAmount` |

`round` is **half-up to whole VND**. In integer arithmetic: `(amount × percent + 50) ÷ 100`, discarding the remainder.

### Status workflow

`DRAFT → PENDING_PAYMENT → PAID → FULFILLED`, with `CANCELLED` possible before fulfilment.

| Status | Orders | Timestamps set |
|---|---:|---|
| `DRAFT` | 9 | — |
| `PENDING_PAYMENT` | 32 | — |
| `PAID` | 60 | `paidAt` |
| `FULFILLED` | 77 | `paidAt`, `fulfilledAt` |
| `CANCELLED` | 22 | `cancelledAt` |

---

## Report Definitions (used by `answer-key.json`)

- **Revenue** = sum of `total` over orders whose status is `PAID` or `FULFILLED`.
- **Day** = the calendar date of `createdAt` in Asia/Ho_Chi_Minh (UTC+07:00). Every timestamp already carries the `+07:00` offset.
- **Date ranges** are inclusive at both ends.

`answer-key.json` contains the totals, the order count and summed total per status, revenue per day, revenue per customer, and four sample filter queries with their expected counts and sums.

---

## Serving the Data as a Mock API

`db.json` works with **json-server 0.17.4**, whose pagination, search and delay features the labs rely on:

```bash
npx json-server@0.17.4 db.json --port 3001 --delay 300
```

| Request | Returns |
|---|---|
| `GET /orders` | All orders |
| `GET /orders/142` | One order |
| `GET /orders?status=PAID` | Filter by any field |
| `GET /orders?_page=2&_limit=20` | Pagination — total count in the `X-Total-Count` header |
| `GET /orders?q=ORD-2026-0142` | Full-text search — order code or customer name |
| `GET /orders?_sort=createdAt&_order=desc` | Sorting |
| `GET /shipments/142` | Shipping status for order 142 (Backend L3) |
| `GET /products`, `GET /customers` | Reference data |

`--delay 300` adds 300 ms to every response, which makes loading states and request cancellation visible. For the Backend L3 timeout exercise, raise the delay (for example `--delay 3000`) and set your client timeout lower.

Orders without a shipment (`DRAFT`, `PENDING_PAYMENT`, `CANCELLED`) return **404** from `/shipments/{id}` — handle that case.

---

## What Is Not Here

Some mentor-prepared materials are documents rather than JSON, so they are not in this folder:

- **AI Engineer:** the order emails (L1), the purchase-order PDF/DOCX/HTML files (L3 and the capstone), and the ~200-document order-operations corpus.
- **Frontend:** the Figma file.

The AI track's `lookup_order` data *is* here: seed its database from `orders.json`.

---

*VATEK Internship Program 2026 · OrderHub Training Datasets*
