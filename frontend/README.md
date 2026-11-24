# 🛍️ Soopi Shop - Frontend

Giao diện người dùng (Frontend) cho nền tảng thương mại điện tử **Soopi Shop**, được xây dựng với kiến trúc hiện đại, tối ưu hiệu năng và trải nghiệm người dùng bằng **React 19**, **TypeScript** và **Vite**.

---

## 📖 Tổng quan dự án

Frontend của **Soopi Shop** đảm nhận vai trò hiển thị giao diện mua sắm trực tuyến, quản lý giỏ hàng, đặt hàng, quản lý sản phẩm và tích hợp cơ chế xác thực người dùng.

### Các tính năng chính:
- 🚀 **Tốc độ tải vượt trội:** Sử dụng Vite 8 với quy trình bundling siêu tốc.
- 🔐 **Xác thực bảo mật:** Tích hợp Clerk Authentication quản lý đăng nhập, phân quyền người dùng và bảo vệ route.
- 🎨 **Giao diện hiện đại & linh hoạt:** Thiết kế responsive chuẩn UI/UX với Tailwind CSS v4.
- ⚡ **Quản lý trạng thái thông minh:** Kết hợp Zustand (client state) và TanStack React Query v5 (server state caching & synchronization).
- 🛡️ **Kiểu dữ liệu an toàn:** TypeScript nghiêm ngặt kết hợp với Zod để xác thực dữ liệu API.

---

## 🛠️ Công nghệ sử dụng

| Công nghệ | Phiên bản | Vai trò |
| :--- | :--- | :--- |
| **React** | `^19.3` | Thư viện xây dựng giao diện người dùng |
| **TypeScript** | `~7.0` | Ngôn ngữ lập trình có kiểu tĩnh |
| **Vite** | `^8.3` | Build tool và development server siêu nhanh |
| **Tailwind CSS** | `^4.3` | CSS framework thiết kế giao diện theo utility classes |
| **Zustand** | `^5.1` | Quản lý global state nhẹ và linh hoạt |
| **TanStack React Query** | `^5.108` | Quản lý, cache và đồng bộ dữ liệu server |
| **Axios** | `^1.21` | HTTP client giao tiếp với REST API |
| **Clerk React** | `^6.19` | Dịch vụ xác thực và quản lý tài khoản người dùng |
| **Zod** | `^4.6` | Schema validation cho dữ liệu và form |
| **Nginx** | `1.27-alpine` | Web server phục vụ static assets trong container Production |
| **Docker & Docker Compose** | Latest | Đóng gói và chạy ứng dụng độc lập môi trường |

---

## ⚙️ Biến môi trường

Nội dung cấu hình trong `.env`:

```env
# Clerk Authentication Publishable Key
VITE_CLERK_PUBLISHABLE_KEY=pk_test_xxxxxxxxxxxxxxxxxxxxxxxx

# Địa chỉ Backend API
VITE_API_BASE_URL=http://localhost:8000/api
```

> **Lưu ý:** Các biến môi trường cho Vite bắt buộc phải có tiền tố `VITE_` để có thể truy cập được từ mã nguồn frontend qua `import.meta.env`.

---

## 💻 Hướng dẫn chạy với NPM

### Yêu cầu hệ thống
- **Node.js**: Phiên bản `v20.x` hoặc `v22.x` trở lên
- **NPM**: Phiên bản `10.x` trở lên

### Cài đặt và khởi chạy

1. **Di chuyển vào thư mục frontend:**
   ```bash
   cd frontend
   ```

2. **Cài đặt các thư viện phụ thuộc:**
   ```bash
   npm install
   ```

3. **Thiết lập biến môi trường:**
   Đảm bảo file `.env.local` đã được tạo và điền đúng thông tin (xem phần [Biến môi trường](#-biến-môi-trường)).

4. **Khởi động server phát triển (Development Server):**
   ```bash
   npm run dev
   ```
   Ứng dụng sẽ chạy tại địa chỉ: **`http://localhost:5173`**

### Các câu lệnh có sẵn

| Lệnh | Chức năng |
| :--- | :--- |
| `npm run dev` | Khởi chạy Vite dev server hỗ trợ Hot Module Replacement (HMR) |
| `npm run build` | Kiểm tra kiểu TypeScript và build ứng dụng tối ưu vào thư mục `dist/` |
| `npm run lint` | Chạy bộ linter Oxlint kiểm tra lỗi mã nguồn |
| `npm run preview` | Khởi chạy server local để xem trước bản build từ `dist/` |

---

## 🐳 Hướng dẫn chạy với Docker

Dự án cung cấp sẵn 2 cấu hình Docker riêng biệt phục vụ cho 2 mục đích:
- **Development**: Đồng bộ mã nguồn theo thời gian thực (Hot-reload qua volume mount).
- **Production**: Multi-stage build tối ưu kích thước bundle, phục vụ bằng **Nginx Alpine**.

### 1. Môi trường Development (Hot Reload)

Môi trường này sử dụng `Dockerfile.dev` và `docker-compose.dev.yml`. Thư mục code máy chủ được gắn trực tiếp vào container:

```bash
cd frontend

# Khởi chạy container dev
docker compose -f docker-compose.dev.yml up --build
```

- **Địa chỉ truy cập:** `http://localhost:5173`
- Để chạy nền (detached mode), thêm cờ `-d`:
  ```bash
  docker compose -f docker-compose.dev.yml up -d
  ```

### 2. Môi trường Production (Nginx)

Sử dụng Dockerfile multi-stage để biên dịch TypeScript/Vite thành các file tĩnh, sau đó Nginx phục vụ với cấu hình tối ưu (Gzip, Cache-Control, Security Headers, SPA routing fallback):

```bash
cd frontend

# Khởi chạy container production
docker compose up -d --build
```

- **Địa chỉ truy cập:** `http://localhost:3000`
- Cổng có thể tùy biến thông qua biến môi trường `FRONTEND_PORT` (mặc định là `3000`).

### 3. Dừng và dọn dẹp Containers

- **Dừng container Development:**
  ```bash
  docker compose -f docker-compose.dev.yml down
  ```

- **Dừng container Production:**
  ```bash
  docker compose down
  ```

### 4. Build & Chạy thủ công với Docker CLI

Nếu không sử dụng Docker Compose, có thể dùng lệnh Docker thuần:

#### Chạy Development:
```bash
# Build image dev
docker build -f Dockerfile.dev -t soopi-frontend:dev .

# Run container dev với volume mount
docker run -it --rm \
  -p 5173:5173 \
  -v ${PWD}:/app \
  -v /app/node_modules \
  --name soopi_frontend_dev \
  soopi-frontend:dev
```

#### Chạy Production:
```bash
# Build image production (truyền build-arg nếu cần)
docker build -f Dockerfile \
  --build-arg VITE_API_BASE_URL=http://localhost:8000/api \
  --build-arg VITE_CLERK_PUBLISHABLE_KEY=pk_test_xxx \
  -t soopi-frontend:prod .

# Run container production qua Nginx
docker run -d --rm \
  -p 3000:80 \
  --name soopi_frontend_prod \
  soopi-frontend:prod
```
