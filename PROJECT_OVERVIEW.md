# Tổng quan dự án `order-report-cli`

## 1. Mục đích

`order-report-cli` là ứng dụng quản lý đơn hàng được xây dựng theo mô hình Spring Boot. Dự án cung cấp:

- API quản lý đơn hàng và sản phẩm.
- Đăng ký, đăng nhập và xác thực người dùng.
- JWT access token và refresh token.
- Phân quyền theo role, trong đó các thao tác ghi sản phẩm chỉ dành cho `ADMIN`.
- Đọc dữ liệu đơn hàng từ JSON và một số xử lý nghiệp vụ trong bộ nhớ.
- Truy vấn dữ liệu người dùng và sản phẩm qua SQL Server/JPA.
- Migration cơ sở dữ liệu bằng Flyway.
- Xử lý lỗi tập trung, correlation ID và logging request.

## 2. Công nghệ đang sử dụng

| Nhóm | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Web/API | Spring Web MVC, embedded Tomcat |
| Validation | Jakarta Bean Validation, Spring Boot Validation |
| ORM | Spring Data JPA, Hibernate ORM |
| Cơ sở dữ liệu | Microsoft SQL Server |
| JDBC driver | Microsoft SQL Server JDBC |
| Database migration | Flyway Core và Flyway SQL Server |
| Bảo mật | Spring Security |
| Mật khẩu | BCrypt thông qua `PasswordEncoder` |
| Token | JSON Web Token với JJWT 0.12.6 |
| JSON | Jackson, bao gồm hỗ trợ Java Time |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Coverage | JaCoCo |
| Build/dependency | Maven |
| IDE/run | IntelliJ IDEA, Spring Boot executable application |

## 3. Cấu trúc thư mục

```text
order-report-cli/
├── pom.xml
├── README.md
├── PROJECT_OVERVIEW.md
├── data/
│   └── orders.json
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── Application.java
│   │   │   ├── OrderCli.java
│   │   │   ├── Client/
│   │   │   ├── Controller/
│   │   │   ├── DTO/
│   │   │   ├── Entity/
│   │   │   ├── Exceptions/
│   │   │   ├── Mapping/
│   │   │   ├── Middleware/
│   │   │   ├── Repository/
│   │   │   ├── Security/
│   │   │   ├── Service/
│   │   │   ├── enums/
│   │   │   └── interfaces/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── db/migration/
│   │       └── *.json
│   └── test/
│       └── java/org/example/Service/
│           └── UserServiceTest.java
└── target/
    └── build output, compiled classes, test reports and JaCoCo report
```

## 4. Các package chính

### `org.example.Controller`

Lớp REST controller tiếp nhận HTTP request và gọi service:

- `UserController`: đăng ký, đăng nhập, refresh token và lấy thông tin user hiện tại.
- `ProductController`: đọc và thay đổi sản phẩm.
- `OrderController`: lọc, tạo, cập nhật trạng thái và hủy đơn hàng.

### `org.example.Service`

Chứa nghiệp vụ chính:

- `UserService`: validate đăng ký, hash password, xác thực đăng nhập và truy vấn user.
- `ProductService`: kiểm tra dữ liệu và thao tác CRUD sản phẩm.
- `OrderService`: lọc đơn hàng, tính doanh thu, tạo đơn, đổi trạng thái, hủy đơn và truy vấn vận chuyển.

### `org.example.Security`

Thành phần xác thực và phân quyền JWT:

- `JwtService`: tạo, ký, đọc và kiểm tra access/refresh token.
- `JwtAuthenticationFilter`: đọc header `Authorization: Bearer <token>` và đưa authentication vào `SecurityContext`.
- `SecurityConfig`: cấu hình stateless session, endpoint công khai và role `ADMIN`.

### `org.example.Entity`

Các entity JPA tương ứng với dữ liệu nghiệp vụ:

- `User`
- `Product`
- `Order`
- `OrderItem`
- `Customer`
- `Shipment`

### `org.example.Repository`

Tầng truy cập dữ liệu:

- `UserJPARepository` và `ProductJPARepository`: Spring Data JPA repository.
- `JsonReader`: đọc đơn hàng từ file JSON.
- `InMemoryRepository`: repository đơn hàng trong bộ nhớ.
- `Calculate`: triển khai tính toán doanh thu.

### `org.example.DTO`

Các record dùng làm request/response API:

- Request: đăng ký, đăng nhập, refresh token, sản phẩm, đơn hàng, shipment, khoảng ngày.
- Response: API response, user response, token response và error response.

### `org.example.Middleware`

- `GlobalExceptionHandler`: chuyển exception thành error response HTTP.
- `CorrelationIdFilter`: quản lý correlation ID cho request.
- `RequestLogging`: logging request.

### `org.example.Mapping`

Chuyển đổi giữa DTO và entity cho order, product, shipment và user.

## 5. Các API chính

| Method | Endpoint | Mục đích | Quyền |
|---|---|---|---|
| `POST` | `/auth/register` | Đăng ký user | Công khai |
| `POST` | `/auth/login` | Xác thực và nhận token pair | Công khai |
| `POST` | `/auth/refresh` | Cấp access/refresh token mới | Công khai, cần refresh token |
| `GET` | `/me` | Lấy user hiện tại từ access token | Đã đăng nhập |
| `GET` | `/products` | Danh sách sản phẩm | Đã đăng nhập |
| `GET` | `/products/{id}` | Lấy sản phẩm theo ID | Đã đăng nhập |
| `POST` | `/products` | Tạo sản phẩm | `ADMIN` |
| `PATCH` | `/products/{id}` | Cập nhật sản phẩm | `ADMIN` |
| `DELETE` | `/products/{id}` | Xóa sản phẩm | `ADMIN` |
| `GET` | `/orders` | Lọc danh sách đơn hàng | Đã đăng nhập |
| `GET` | `/orders/{orderId}` | Lấy đơn hàng theo ID | Đã đăng nhập |
| `POST` | `/orders/create` | Tạo đơn hàng | Đã đăng nhập |
| `PATCH` | `/orders/status` | Đổi trạng thái đơn hàng | Đã đăng nhập |
| `PATCH` | `/orders/{orderId}/cancel` | Hủy đơn hàng | Đã đăng nhập |

Các request cần xác thực gửi header:

```http
Authorization: Bearer <access-token>
```

## 6. Luồng xác thực

1. Client gửi email, họ tên và password tới `/auth/register`.
2. `UserService` validate dữ liệu và hash password bằng BCrypt.
3. User đăng nhập qua `/auth/login`.
4. `UserService` kiểm tra password hash.
5. `JwtService` tạo access token và refresh token.
6. Client gửi access token trong header Bearer khi gọi API cần đăng nhập.
7. `JwtAuthenticationFilter` xác thực token và tạo `Authentication`.
8. Role trong token được chuyển thành authority dạng `ROLE_ADMIN` hoặc `ROLE_STAFF`.
9. Khi access token hết hạn, client gửi refresh token tới `/auth/refresh`.

## 7. Cấu hình ứng dụng

File cấu hình chính:

```text
src/main/resources/application.yml
```

Cấu hình hiện tại gồm:

- SQL Server tại `localhost:1433`.
- Database `OrderReportDb`.
- Port HTTP `8080`.
- Flyway bật với `baseline-on-migrate`.
- Access token mặc định sống 15 phút.
- Refresh token mặc định sống 7 ngày.
- Actuator expose `health` và `info`.

Không nên commit password database hoặc JWT secret thật vào repository. Khi chạy môi trường thật, nên truyền các giá trị này bằng biến môi trường hoặc secret manager.

## 8. Flyway migration

Migration nằm tại:

```text
src/main/resources/db/migration/
```

Các migration hiện có:

- `V1__initial_schema.sql`: tạo bảng `Users` nếu chưa tồn tại.
- `V2__add_password_hashed_to_users.sql`: thêm cột `password_hashed` nếu chưa tồn tại.

Flyway lưu lịch sử migration trong bảng:

```text
dbo.flyway_schema_history
```

## 9. Testing và build

Chạy test bằng Maven:

```bash
mvn test
```

Build project:

```bash
mvn package
```

Unit test hiện có tập trung vào `UserService`, gồm các trường hợp validate đăng ký, email trùng, hash password, đăng nhập thành công/thất bại và tìm user. JaCoCo tạo báo cáo tại:

```text
target/site/jacoco/index.html
```

## 10. Điểm cần lưu ý

- Dự án đang kết hợp hai nguồn dữ liệu: JPA/SQL Server cho user và product, JSON/in-memory cho một số nghiệp vụ order.
- `baseline-on-migrate` phù hợp khi database đã tồn tại schema trước khi đưa Flyway vào; với database mới cần cân nhắc quy trình migration từ đầu.
- JWT secret trong `application.yml` hiện là secret dùng cho môi trường phát triển, không dùng nguyên giá trị này trong production.
- Việc sinh ID user hiện dựa trên ID lớn nhất cộng một, phù hợp cho bài tập nhưng nên chuyển sang cơ chế identity/sequence hoặc cơ chế sinh ID an toàn khi chạy đồng thời trong production.
