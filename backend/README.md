# 🛒 Soopi Shop — Backend API

> **UIT – Học phần Quản lý Dự án | Nhóm 16**
>
> Nền tảng thương mại điện tử kinh doanh **linh kiện máy tính**, được xây dựng trên Spring Boot 3 với PostgreSQL, Redis và xác thực Clerk JWT.

---

## 📋 Tổng quan ứng dụng

Soopi Shop Backend là REST API server cung cấp toàn bộ nghiệp vụ cho cửa hàng linh kiện máy tính trực tuyến.

### Tech Stack

| Thành phần | Công nghệ |
| :--- | :--- |
| **Runtime** | Java 21 (LTS) |
| **Framework** | Spring Boot `3.4.3` |
| **Database** | PostgreSQL `16` (Local Docker / **Neon Serverless Free-tier**) |
| **Cache** | Redis `7` |
| **Migration** | Flyway (V1 + V2) |
| **Authentication** | Clerk OAuth2 / JWT (JWKS) |
| **Authorization** | RBAC nội bộ: `CUSTOMER`, `STAFF`, `ADMIN` |
| **API Docs** | SpringDoc OpenAPI 3 / Swagger UI |
| **Build Tool** | Maven Wrapper (`mvnw`) |
| **Container** | Docker & Docker Compose |

### Các module chính

```
vn.soopishop
├── config/
│   ├── DotenvEnvironmentPostProcessor   # Nạp DATABASE_URL / FLYWAY_DATABASE_URL (Local & Neon SSL)
│   └── SecurityConfig                   # Spring Security + OAuth2 Resource Server
├── seed/
│   ├── CatalogSeedData                  # Dữ liệu mẫu 9 nhóm linh kiện (90 sản phẩm, 124 SKU)
│   └── DatabaseSeeder                   # Seed dữ liệu mẫu (idempotent theo từng SKU/đơn hàng)
└── verify/
    └── DatabaseVerifier                 # Kiểm chứng 6 nhóm quy tắc toàn vẹn & Catalog/Search
```

### Kiến trúc Database (18 bảng / 6 phân hệ)

| Phân hệ | Bảng |
| :--- | :--- |
| 👤 Người dùng & RBAC | `users`, `user_addresses` |
| 🗂️ Danh mục & Sản phẩm | `categories`, `brands`, `products`, `product_variants`, `product_images`, `product_specifications`, `category_spec_attributes` |
| 🛒 Giỏ hàng & Voucher | `carts`, `cart_items`, `vouchers` |
| 📦 Đơn hàng & Thanh toán | `orders`, `order_items`, `order_status_history`, `payments` |
| 📊 Tồn kho | `inventory_transactions` |
| ⭐ Đánh giá | `reviews` |

| Flyway Migration | Nội dung |
| :--- | :--- |
| [`V1__initial_schema.sql`](src/main/resources/db/migration/V1__initial_schema.sql) | Khởi tạo 18 bảng lõi |
| [`V2__integrity_hardening.sql`](src/main/resources/db/migration/V2__integrity_hardening.sql) | Gia cố toàn vẹn, bổ sung partial index & GIN index `pg_trgm` cho tìm kiếm `≤ 500ms` |

---

## ⚙️ Yêu cầu môi trường

| Công cụ | Phiên bản tối thiểu |
| :--- | :--- |
| **JDK** | 21 |
| **Docker** | 24+ |
| **Docker Compose** | v2 (plugin) |
| **Maven** | Tích hợp sẵn qua `mvnw` — không cần cài thêm |

---

## 🚀 Hướng dẫn chạy chương trình

### Bước 1 — Tạo file `.env`

```bash
# macOS / Linux
cp .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env
```

Chỉnh thông số trong `.env` nếu cần. **Không commit file `.env` lên Git.**

<details>
<summary>Xem nội dung mặc định <code>.env.example</code></summary>

```env
# ==== Backend ====
SERVER_PORT=8000

# ==== PostgreSQL ====
POSTGRES_HOST=postgres
POSTGRES_DB=soopi_shop
POSTGRES_PORT=5432
POSTGRES_USER=soopi_user
POSTGRES_PASSWORD=soopi_dev_password_change_me
DATABASE_URL=postgresql://soopi_user:soopi_dev_password_change_me@postgres:5432/soopi_shop

# ==== Redis ====
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# ==== Clerk OAuth2 ====
CLERK_ISSUER_URI=
CLERK_SECRET_KEY=your-clerk-secret-key
```

</details>

---

### Bước 2 — Khởi chạy bằng Docker Compose

#### 🔧 Môi trường Development (khuyên dùng khi phát triển)

Bao gồm **Spring Boot** (port `8000`, debug port `5005`), **PostgreSQL 16** (port `5433`) và **Redis 7** (port `6379`).  
Source code được mount vào container — **Spring Boot DevTools** tự reload khi code thay đổi (~1–2s).

```bash
docker compose -f docker-compose.dev.yml up -d
```

Xem log backend:
```bash
docker compose -f docker-compose.dev.yml logs -f backend
```

Dừng và xóa container:
```bash
# Chỉ dừng
docker compose -f docker-compose.dev.yml down

# Dừng và xóa toàn bộ volume (reset DB)
docker compose -f docker-compose.dev.yml down -v
```

#### 🏭 Môi trường Production (multi-stage build)

```bash
docker compose up -d --build
```

> Build multi-stage với JRE 21 Alpine runtime — image nhỏ gọn, tối ưu bảo mật. Healthcheck tích hợp sẵn.

#### ☁️ Kết nối Cloud Database Free-tier — Neon Serverless PostgreSQL 16

Dự án chọn **Neon** làm dịch vụ Managed PostgreSQL Free-tier (0 VNĐ, không cần thẻ tín dụng, 0.5 GB storage, 100 CU-hours/tháng, chạy **PostgreSQL 16** thuần, hỗ trợ sẵn extension **`pg_trgm`** cho `V2__integrity_hardening.sql` và **Database Branching**).

1. Tạo project PostgreSQL 16 trên [Neon Console](https://console.neon.tech/) (khuyên chọn region `ap-southeast-1` Singapore cho độ trễ thấp tại Việt Nam).
2. Lấy chuỗi kết nối trong **Connection Details** và dán vào `backend/.env`:
   ```env
   # Pooled endpoint cho ứng dụng runtime (HikariCP)
   DATABASE_URL=postgresql://<user>:<password>@<endpoint>-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require

   # Direct endpoint (không có '-pooler') khuyến nghị cho Flyway Migration để tránh xung đột advisory lock trên PgBouncer
   FLYWAY_DATABASE_URL=postgresql://<user>:<password>@<endpoint>.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require

   # Tối ưu Neon scale-to-zero Free-tier (không giữ kết nối nhàn rỗi)
   DB_POOL_MIN_IDLE=0
   ```
   > [!TIP]
   > `DotenvEnvironmentPostProcessor` tự động giữ tham số SSL và chuẩn hóa `channel_binding=require` (libpq) sang `channelBinding=require` (pgJDBC), nên bạn có thể dán nguyên chuỗi từ Neon Console mà không cần tự chuyển đổi sang `jdbc:postgresql://`.
3. Chạy Flyway Migration + Seed + Kiểm chứng trực tiếp lên Neon từ máy host (không cần container PostgreSQL local):
   ```powershell
   # Windows (PowerShell)
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-application-type=none --seed --verify"
   ```
   ```bash
   # macOS / Linux
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.main.web-application-type=none --seed --verify"
   ```

---

### Bước 3 — Seed dữ liệu mẫu & Kiểm chứng

Chạy sau khi container backend đã khởi động thành công (`docker compose -f docker-compose.dev.yml up -d`) — hoặc chạy trực tiếp bằng `mvnw` khi trỏ sang Neon Cloud DB như hướng dẫn ở trên.

Thực thi lệnh Maven trực tiếp bên trong container `soopi_shop_backend` (không cần cài Java trên máy host):

**Windows (PowerShell):**
```powershell
docker exec soopi_shop_backend mvn spring-boot:run "-Dspring-boot.run.jvmArguments=" "-Dspring-boot.run.arguments=--spring.main.web-application-type=none --seed --verify"
```

**macOS / Linux:**
```bash
docker exec soopi_shop_backend mvn spring-boot:run -Dspring-boot.run.jvmArguments="" -Dspring-boot.run.arguments="--spring.main.web-application-type=none --seed --verify"
```

> [!NOTE]
> Tham số `-Dspring-boot.run.jvmArguments=""` và `--spring.main.web-application-type=none` giúp tiến trình CLI chạy mà không khởi động web server (không mở cổng HTTP), tránh xung đột cổng debug `5005` hay port `8000` với ứng dụng chính đang chạy trong container.

Hoặc sử dụng `docker compose run` với container tạm:

**Windows (PowerShell):**
```powershell
docker compose -f docker-compose.dev.yml run --rm --no-deps --entrypoint mvn backend spring-boot:run "-Dspring-boot.run.jvmArguments=" "-Dspring-boot.run.arguments=--spring.main.web-application-type=none --seed --verify"
```

**macOS / Linux:**
```bash
docker compose -f docker-compose.dev.yml run --rm --no-deps --entrypoint mvn backend spring-boot:run -Dspring-boot.run.jvmArguments="" -Dspring-boot.run.arguments="--spring.main.web-application-type=none --seed --verify"
```


| Tham số | Chức năng |
| :--- | :--- |
| `--seed` | Chèn dữ liệu mẫu **9 danh mục** (CPU, GPU, Mainboard, RAM, SSD/HDD, PSU, Case, Tản nhiệt, Phụ kiện) với **90 sản phẩm, 120+ biến thể SKU, 48 thuộc tính thông số** (idempotent — chạy nhiều lần không bị trùng) |
| `--verify` | Kiểm chứng **6 nhóm quy tắc** của CSDL & dữ liệu Catalog/Search |

<details>
<summary>Kết quả kiểm chứng mong đợi</summary>

| # | Kiểm chứng | Kết quả |
| --- | :--- | :---: |
| 1 | Đếm đủ 18 bảng với dữ liệu mẫu (9 danh mục, 22 thương hiệu, 48 thuộc tính spec, 90 sản phẩm, 120+ biến thể) | `PASS` |
| 2 | **BR-INV-01** — Chống tồn kho âm (`CHECK stock_quantity >= 0`) | `PASS` |
| 3 | **QM-SEC-03** — Không tồn tại `users.password_hash`, 100% user có `clerk_user_id` dạng `user_*` | `PASS` |
| 4 | **BR-INV-02** — `product_variants.stock_quantity == SUM(inventory_transactions.quantity_change)` | `PASS` |
| 5 | **V2 Hardening** — FK `reviews` là `ON DELETE RESTRICT`, đủ 3 index mới (`idx_orders_user_voucher_active`, `pg_trgm`) | `PASS` |
| 6 | **Catalog & Search Readiness** — Đủ 9 nhóm linh kiện, ≥ 10 SP & ≥ 4 spec lọc mỗi nhóm, đủ ảnh đại diện & không lệch danh mục spec | `PASS` |

</details>

---

## 🌐 Các đường dẫn (Endpoints)

Sau khi ứng dụng khởi động, truy cập các URL sau:

| Mục đích | URL |
| :--- | :--- |
| 🏠 **API Base** | `http://localhost:8000` |
| 📄 **Swagger UI** | `http://localhost:8000/swagger-ui/index.html` |
| 📐 **OpenAPI JSON** | `http://localhost:8000/v3/api-docs` |
| ❤️ **Health Check** | `http://localhost:8000/actuator/health` |
| ℹ️ **App Info** | `http://localhost:8000/actuator/info` |

> [!NOTE]
> Thay `8000` bằng giá trị `SERVER_PORT` trong file `.env` nếu bạn đã chỉnh lại cổng.

> [!TIP]
> Khi `app.security.oauth2.enabled=false` (mặc định cho local dev), Swagger UI có thể gọi API trực tiếp **không cần Bearer token**.  
> Để bật xác thực Clerk JWT, đặt `app.security.oauth2.enabled=true` và cấu hình `CLERK_ISSUER_URI`.

---

## 👥 Tài khoản mẫu (Local Dev / UAT)

> [!CAUTION]
> Các `clerk_user_id` dạng `user_seed_*` chỉ dùng cho môi trường **phát triển cục bộ / UAT**.  
> **Tuyệt đối không sử dụng trên Production.** PostgreSQL không lưu mật khẩu — xác thực do Clerk quản lý.

| Clerk User ID | Email | Role | Trạng thái |
| :--- | :--- | :---: | :---: |
| `user_seed_admin_01` | `admin@soopishop.vn` | `ADMIN` | ✅ ACTIVE |
| `user_seed_staff_01` | `staff@soopishop.vn` | `STAFF` | ✅ ACTIVE |
| `user_seed_khach1_01` | `khach1@gmail.com` | `CUSTOMER` | ✅ ACTIVE |
| `user_seed_khach2_01` | `khach2@gmail.com` | `CUSTOMER` | ✅ ACTIVE |
| `user_seed_khach3_01` | `khach3@gmail.com` | `CUSTOMER` | ✅ ACTIVE |
| `user_seed_locked_01` | `locked@gmail.com` | `CUSTOMER` | 🔒 LOCKED |

---

## 🔧 Cấu hình nâng cao

### Hot Reload (Dev)

Spring Boot DevTools tự động reload khi bạn compile trong IDE:
- **IntelliJ IDEA:** `Ctrl + F9` (Build Project)
- **Eclipse:** `Ctrl + B`

### Remote Debug

Port `5005` được expose trong môi trường dev. Kết nối từ IDE:
```
Host: localhost
Port: 5005
Debugger mode: Attach to remote JVM
```

### Biến môi trường quan trọng

| Biến | Mô tả | Mặc định |
| :--- | :--- | :--- |
| `SERVER_PORT` | Cổng HTTP của Spring Boot | `8000` |
| `DATABASE_URL` | Chuỗi kết nối PostgreSQL (Docker local hoặc Neon Cloud SSL) | _(xem `.env.example`)_ |
| `FLYWAY_DATABASE_URL` | Direct endpoint riêng cho Flyway Migration khi dùng Neon Pooled endpoint | _(tùy chọn)_ |
| `DB_POOL_MIN_IDLE` | Số kết nối nhàn rỗi tối thiểu HikariCP (`0` cho Neon scale-to-zero) | `2` |
| `DB_POOL_MAX_SIZE` | Số kết nối tối đa HikariCP | `10` |
| `POSTGRES_HOST` | Host PostgreSQL (Docker local) | `postgres` |
| `POSTGRES_PORT` | Cổng PostgreSQL (Docker local) | `5432` |
| `REDIS_HOST` | Host Redis | `redis` |
| `REDIS_PORT` | Cổng Redis | `6379` |
| `CLERK_ISSUER_URI` | JWKS Issuer URI từ Clerk Dashboard | _(bắt buộc khi bật OAuth2)_ |

---

*Soopi Shop Backend — UIT, Nhóm 16*
