# Soopi Shop — Backend & PostgreSQL Database Setup

Nền tảng thương mại điện tử kinh doanh linh kiện máy tính **Soopi Shop** (UIT – Học phần Quản lý dự án, Nhóm 16).

- **Runtime:** Java 21 (LTS) + Spring Boot `3.4.3`
- **Database:** PostgreSQL `16` (Docker Compose) + Flyway Migrations (`V1` + `V2`)
- **Authentication:** Clerk (`clerk_user_id` mapping — PostgreSQL không lưu mật khẩu) + RBAC nội bộ (`CUSTOMER`, `STAFF`, `ADMIN`)

---

## 1. Kiến trúc Schema hiện tại (18 bảng / 6 phân hệ)

| Flyway Migration | Nội dung chính |
| :--- | :--- |
| [`V1__initial_schema.sql`](src/main/resources/db/migration/V1__initial_schema.sql) | Khởi tạo **18 bảng** lõi trên 6 phân hệ: Người dùng & RBAC (`users` ánh xạ `clerk_user_id`, `user_addresses`), Danh mục & Linh kiện (`categories`, `brands`, `category_spec_attributes`, `products`, `product_variants`, `product_images`, `product_specifications`), Giỏ hàng & Voucher (`carts`, `cart_items`, `vouchers`), Đơn hàng & Thanh toán (`orders`, `order_items`, `order_status_history`, `payments`), Truy vết tồn kho (`inventory_transactions`), Đánh giá (`reviews`). |
| [`V2__integrity_hardening.sql`](src/main/resources/db/migration/V2__integrity_hardening.sql) | Gia cố toàn vẹn & hiệu năng: bổ sung `DEFAULT` cho các cột trạng thái/bảo hành, chuyển 3 khóa ngoại bảng `reviews` sang `ON DELETE RESTRICT`, thêm partial index `idx_orders_user_voucher_active` (tối ưu `per_user_limit`) và 2 GIN index `pg_trgm` (`products.name`, `product_variants.sku`) phục vụ tìm kiếm `<= 500ms`. |

---

## 2. Yêu cầu môi trường

- **Docker & Docker Compose**
- **JDK 21** (hoặc set `JAVA_HOME` trỏ tới JDK 21)
- Maven đã tích hợp sẵn qua Wrapper (`./mvnw` / `.\mvnw.cmd`)

---

## 3. Hướng dẫn cài đặt & chạy nhanh (Local Dev)

### Bước 1: Tạo file `.env`
Sao chép từ `.env.example` và chỉnh thông số nếu cần (**không commit `.env` lên Git**):

```bash
cp .env.example .env
```

Nội dung mặc định trong `.env.example`:
```env
POSTGRES_USER=soopi_user
POSTGRES_PASSWORD=soopi_dev_password_change_me
POSTGRES_DB=soopi_shop
POSTGRES_PORT=5433
REDIS_PORT=6379
APP_PORT=8000
DATABASE_URL=postgresql://soopi_user:soopi_dev_password_change_me@localhost:5433/soopi_shop
```

### Bước 2: Khởi chạy môi trường qua Docker Compose

#### A. Môi trường Development (Hỗ trợ Hot Reloading, Spring Boot port 8000)
Môi trường dev bao gồm: Spring Boot backend (port 8000, debug port 5005), PostgreSQL 16 (port 5433) và Redis 7 (port 6379). Toàn bộ thư mục source code được mount vào container và kích hoạt Spring Boot DevTools để hot reload ngay khi code thay đổi:

```bash
docker compose -f docker-compose.dev.yml up -d
# hoặc gõ ngắn gọn:
docker compose up -d
```

- **Spring Boot Endpoint:** `http://localhost:8000`
- **Swagger UI:** `http://localhost:8000/swagger-ui/index.html`
- **Actuator Health:** `http://localhost:8000/actuator/health`
- **Hot Reloading:** Khi bạn chỉnh sửa mã nguồn hoặc compile trong IDE (IntelliJ Ctrl+F9 / Eclipse), DevTools tự động reload application context trong ~1-2s mà không cần restart container.
- **Remote Debug:** Port `5005` sẵn sàng kết nối JVM debugger từ IDE.

Xem logs backend:
```bash
docker compose logs -f backend
```

#### B. Môi trường Production (Multi-stage build siêu nhẹ, tối ưu bảo mật)
```bash
docker compose up -d --build
```
- Sử dụng multi-stage build (`Dockerfile` / `Dockerfile.dev`) với JRE 21 Alpine runtime.
- Lắng nghe trên port 8000 (`http://localhost:8000`), có healthcheck tích hợp.

Dừng toàn bộ container:
```bash
docker compose -f docker-compose.dev.yml down
# hoặc nếu muốn xóa luôn volume database để reset hoàn toàn:
docker compose -f docker-compose.dev.yml down -v
```

### Bước 3: Chạy Flyway Migration, Seed dữ liệu mẫu & Kiểm chứng tự động

**Windows (PowerShell):**
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--seed --verify"
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--seed --verify"
```

### Kết quả kiểm chứng mong đợi (`--verify`)
1. **Đếm đủ 18 bảng** với dữ liệu mẫu đồng nhất (`--seed` có tính Idempotent, chạy lại nhiều lần không trùng dữ liệu).
2. **Chống âm kho (`BR-INV-01`):** `PASS` — constraint `CHECK (stock_quantity >= 0)` chặn cập nhật tồn kho âm.
3. **Ánh xạ Clerk (`QM-SEC-03`):** `PASS` — không tồn tại cột `users.password_hash`, 100% tài khoản có `clerk_user_id` dạng `user_*`.
4. **Đối soát sổ cái kho (`BR-INV-02`):** `PASS` — `product_variants.stock_quantity == SUM(inventory_transactions.quantity_change)`.
5. **Gia cố V2:** `PASS` — toàn bộ FK bảng `reviews` là `ON DELETE RESTRICT` và đủ 3 index mới (`idx_orders_user_voucher_active`, `idx_products_name_trgm`, `idx_product_variants_sku_trgm`).

---

## 4. Bộ tài khoản mẫu (Chỉ dùng cho Local Dev / UAT)

> **Cảnh báo (`QM-SEC-03`):** Các `clerk_user_id` dạng `user_seed_*` dưới đây chỉ dùng cho môi trường phát triển cục bộ / UAT, **tuyệt đối không sử dụng trên Production**. PostgreSQL không lưu mật khẩu; xác thực do Clerk quản lý.

| Clerk User ID | Email | Role | Status |
| :--- | :--- | :---: | :---: |
| `user_seed_admin_01` | `admin@soopishop.vn` | `ADMIN` | `ACTIVE` |
| `user_seed_staff_01` | `staff@soopishop.vn` | `STAFF` | `ACTIVE` |
| `user_seed_khach1_01` | `khach1@gmail.com` | `CUSTOMER` | `ACTIVE` |
| `user_seed_khach2_01` | `khach2@gmail.com` | `CUSTOMER` | `ACTIVE` |
| `user_seed_khach3_01` | `khach3@gmail.com` | `CUSTOMER` | `ACTIVE` |
| `user_seed_locked_01` | `locked@gmail.com` | `CUSTOMER` | `LOCKED` |
