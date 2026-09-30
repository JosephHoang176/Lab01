# Order Report Service

## Tổng quan chương trình Lab

Đây là dự án Java 21/Spring Boot dùng miền nghiệp vụ **Order** để minh họa quá
trình tiến hóa từ một chương trình CLI đọc dữ liệu JSON thành một dịch vụ REST
có persistence bằng SQL Server và giao tiếp với pricing service. Dữ liệu mẫu
được đặt trong `data/orders.json`; mô hình cơ sở dữ liệu và ràng buộc nằm trong
`database/schema.sql`.

Các Lab dưới đây được mô tả theo hai góc nhìn:

- **Mục tiêu học tập:** năng lực cần chứng minh ở chặng tương ứng.
- **Phạm vi hiện có:** những thành phần đã được nối trong mã nguồn hiện tại và
  điểm cần hoàn thiện khi dùng làm bài kiểm tra.

### L1 — Order report CLI: đọc, lọc và tổng hợp đơn hàng

**Mục tiêu.** Làm việc với Java cơ bản, record/kiểu dữ liệu, `Optional`, cấu
trúc project Maven/Gradle và xử lý dữ liệu theo quy tắc nghiệp vụ. Chương trình
nhận trạng thái cùng khoảng ngày (hai đầu mút bao gồm), lọc đơn hàng, tính số
lượng đơn và doanh thu, sau đó in báo cáo.

**Đầu vào và đầu ra.** `OrderCli` (profile `cli`) đọc:

- `status`: một giá trị của `OrderStatus`, hoặc để trống để chọn tất cả;
- `from` và `to`: ngày `yyyy-MM-dd`, hoặc để trống;
- dữ liệu đơn hàng có `createdAt`, `status`, `total`, `currency`.

Kết quả gồm bộ lọc đã chọn, order count, revenue và danh sách mã đơn hàng.
Doanh thu được tính bằng tổng `total` của tập đã lọc; dữ liệu tiền tệ phải được
hiểu là số nguyên VND, không phải số thực dùng cho phép tính tài chính.

**Hiện trạng mã nguồn.** Luồng CLI, parse ngày/trạng thái và lọc theo
`DateRange` đã có. Tuy nhiên `OrderService` hiện lấy tập dữ liệu chính từ
`OrderJPARepo`, còn `JsonReader` chỉ là repository JSON được đăng ký riêng;
vì vậy cần xác nhận database đã được nạp dữ liệu trước khi chạy CLI. `Calculate`
và `InMemoryRepository` vẫn chưa là đường chạy hoàn chỉnh của báo cáo.

**Tiêu chí hoàn thành.** Chạy được với profile `cli`, kết quả khớp
`answer-key.json` cho các truy vấn mẫu, và debugger dừng được trong luồng lọc
hoặc tính doanh thu.

### L2 — Repository abstraction và dependency injection

**Mục tiêu.** Tách report logic khỏi nguồn dữ liệu bằng `IOrderRepository`, áp
dụng interface/composition/inheritance và constructor injection. Thay đổi
repository không được buộc sửa service hoặc logic báo cáo.

**Thiết kế hiện có.**

| Thành phần | Trách nhiệm |
|---|---|
| `IOrderRepository` | Hợp đồng `getAllOrders`, tìm theo id, `save`, `update` |
| `JsonReader` | Đọc/ghi `data/orders.json`, báo lỗi rõ khi file không tồn tại hoặc không đọc được |
| `OrderJPARepo` | Đọc/ghi entity qua `EntityManager` |
| `OrderService` | Nhận repository qua constructor và thực hiện nghiệp vụ |
| `InMemoryRepository` | Khung repository để phục vụ test/đổi implementation |

**Hiện trạng mã nguồn.** Constructor injection và hai implementation JSON/JPA đã
được khai báo. `InMemoryRepository` hiện trả về `null` cho toàn bộ thao tác,
nên chưa thể dùng làm implementation thay thế an toàn. Ngoài ra service đang
tiêm đồng thời JSON repository và JPA repository nhưng các thao tác nghiệp vụ
chính đang gọi JPA repository; đây là điểm cần thống nhất nếu mục tiêu của Lab
là chứng minh khả năng hoán đổi repository.

**Tiêu chí hoàn thành.** Đổi implementation trong cấu hình/bean mà không sửa
logic report; test có thể chạy với repository in-memory và không phụ thuộc
SQL Server hoặc file hệ thống.

### L3 — Làm giàu đơn hàng bất đồng bộ

**Mục tiêu.** Dùng Collections/Streams, `CompletableFuture` hoặc virtual
threads, xử lý exception và timeout khi lấy trạng thái giao hàng đồng thời cho
nhiều đơn hàng.

**Luồng xử lý hiện có.** `OrderService.getShipments` tạo một virtual thread cho
mỗi đơn, gọi `ShippingClient`, rồi in một trong các kết quả:

- `<order-code> -> <shipping-status>`;
- `NO_SHIPMENT` khi endpoint trả HTTP 404;
- `TIMEOUT` khi mock shipping endpoint vượt quá timeout.

`MockShippingClient` gọi `GET /shipments/{orderId}` tại json-server, đặt timeout
HTTP một giây và chuyển timeout thành `ShippingTimeoutException`. Thời gian
toàn bộ enrichment được CLI đo và in ra.

**Tiêu chí hoàn thành.** Phiên bản concurrent nhanh hơn phiên bản tuần tự trên
tập dữ liệu đủ lớn; timeout được báo cáo rõ ràng, không bị nuốt; đơn không có
shipment không làm hỏng toàn bộ báo cáo. Khi chạy thử, có thể tăng
`json-server --delay` để quan sát timeout.

### L4 — Mini Orders REST API và middleware

**Mục tiêu.** Chuyển nghiệp vụ thành REST API Spring Boot, sử dụng routing,
validation, interceptor/filter, correlation ID và global exception handling.

**API hiện có.**

| Method | Endpoint | Ý nghĩa |
|---|---|---|
| `GET` | `/orders?status=&from=&to=` | Liệt kê đơn theo trạng thái/khoảng ngày |
| `GET` | `/orders/{orderId}` | Lấy một đơn hàng |
| `POST` | `/orders/create` | Tạo đơn và khởi động pricing bất đồng bộ |
| `PATCH` | `/orders/status?orderId=&status=` | Chuyển trạng thái theo workflow |
| `PATCH` | `/orders/{orderId}/cancel` | Hủy đơn khi trạng thái cho phép |
| `GET` | `/health`, `/version` | Kiểm tra trạng thái và metadata ứng dụng |

`CorrelationIdFilter` nhận hoặc sinh `X-Correlation-ID`, đặt vào response và
MDC; `RequestLogging` ghi method/path/status/thời gian; `GlobalExceptionHandler`
chuyển lỗi nghiệp vụ thành JSON `400` hoặc `409`, còn lỗi không dự kiến thành
`500` mà không trả stack trace cho client.

**Tiêu chí hoàn thành.** Middleware chạy trên mọi request; chuyển trạng thái
không hợp lệ trả JSON lỗi có correlation ID; request hợp lệ trả đúng DTO và
HTTP status. Các tham số ngày phải tuân thủ định dạng ISO và hai đầu mút của
khoảng ngày phải được tính.

### L5 — Persistence, Flyway và tích hợp Pricing service

**Mục tiêu.** Dùng Spring Data/JPA và SQL Server cho mô hình quan hệ, migration
database, test bằng JUnit/Mockito, đồng thời tách pricing thành một service có
timeout, retry và fallback trạng thái rõ ràng.

**Thiết kế hiện có.**

- `database/schema.sql` tạo các bảng `Users`, `Customers`, `Products`, `Orders`,
  `OrderItems`, `Shipments`, khóa ngoại, unique constraint, check constraint và
  index phục vụ truy vấn.
- `OrderJPARepo` dùng `EntityManager`, fetch các line item cùng order và quản
  lý `save/update` trong transaction.
- `PricingClient` là OpenFeign client; `OrderService` gửi pricing bất đồng bộ
  bằng virtual-thread executor, timeout mỗi lần gọi là hai giây và thử tối đa
  ba lần.
- Khi pricing thành công, tổng tiền và các amount được cập nhật với
  `pricingStatus=COMPLETED`; khi thất bại, order được đánh dấu `FAILED` thay vì
  treo request tạo đơn.
- `docker-compose.yml` mô tả `order-service` và `pricing-service`; cấu hình
  runtime lấy từ biến môi trường, không commit credential.

**Điểm cần lưu ý.** Flyway dependency đã có nhưng `spring.flyway.enabled` đang
đặt là `false`; schema hiện được quản lý bằng script SQL thủ công. Bộ test hiện
có kiểm tra health/version, nhưng chưa bao phủ đầy đủ repository, chuyển trạng
thái, timeout/retry và pricing fallback.

**Tiêu chí hoàn thành.** Có thể thay pricing service bằng mock mà không sửa
report logic; khi pricing service chậm hoặc lỗi, API phản hồi theo chính sách
đã định nghĩa và order giữ trạng thái quan sát được; dữ liệu JPA tuân thủ các
ràng buộc trong schema và các test nghiệp vụ chạy độc lập với môi trường thật.

### Luồng kiến trúc tổng hợp

```text
CLI / REST Controller
          |
          v
      OrderService -----> ShippingClient -----> Mock shipping API
          |                     |
          |                     +--------------> timeout / no shipment
          |
          +-----> IOrderRepository
          |          +--> JsonReader (JSON)
          |          +--> OrderJPARepo (SQL Server)
          |
          +-----> PricingClient (Feign) ---> Pricing service
```

### Dữ liệu chuẩn và quy tắc nghiệp vụ

`src/main/resources/README.md` là tài liệu chuẩn của bộ dữ liệu OrderHub:
200 đơn hàng, model JSON, quy tắc tính `subtotal`, discount, VAT, `total`,
workflow trạng thái và `answer-key.json`. Khi kiểm tra L1-L3, cần dùng đúng
quy tắc này; đặc biệt doanh thu chỉ bao gồm đơn `PAID` hoặc `FULFILLED`, ngày
được lấy theo múi giờ `Asia/Ho_Chi_Minh`, và khoảng ngày là inclusive.

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