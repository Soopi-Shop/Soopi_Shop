# 🤝 Hướng dẫn Đóng góp — Soopi Shop

> Tài liệu này quy định quy ước làm việc với Git cho toàn bộ team Nhóm 16.  
> Mục tiêu: lịch sử commit rõ ràng, dễ review, dễ merge, dễ rollback.

---

## 📌 Mục lục

1. [Quy ước đặt tên nhánh](#1-quy-ước-đặt-tên-nhánh)
2. [Quy ước Conventional Commits](#2-quy-ước-conventional-commits)
3. [Quy trình Pull Request](#3-quy-trình-pull-request)
4. [Quy ước Review Code](#4-quy-ước-review-code)
5. [Nhanh tay (Cheat Sheet)](#5-cheat-sheet)

---

## 1. Quy ước đặt tên nhánh

### Cấu trúc tên nhánh

```
<type>/<scope>-<mô-tả-ngắn>
```

| Phần | Mô tả |
| :--- | :--- |
| `type` | Loại thay đổi (xem bảng bên dưới) |
| `scope` | Phạm vi: `frontend`, `backend`, `db`, `ci`, `docs` |
| `mô-tả-ngắn` | Kebab-case, tiếng Anh, tối đa 5–6 từ |

### Các loại nhánh

| Type | Mục đích | Ví dụ |
| :--- | :--- | :--- |
| `feat` | Tính năng mới | `feat/frontend-product-listing` |
| `fix` | Sửa lỗi | `fix/backend-jwt-expiry` |
| `chore` | Cài đặt, cấu hình, build | `chore/backend-docker-setup` |
| `refactor` | Tái cấu trúc code, không đổi hành vi | `refactor/frontend-axios-client` |
| `docs` | Cập nhật tài liệu | `docs/update-backend-readme` |
| `test` | Thêm hoặc sửa test | `test/backend-order-service` |
| `ci` | CI/CD pipeline | `ci/github-actions-build` |
| `hotfix` | Vá khẩn cấp trên production | `hotfix/payment-null-pointer` |

> [!IMPORTANT]
> - Luôn đặt tên nhánh bằng **chữ thường**, từ cách nhau bởi dấu `-`.
> - **Không dùng** dấu cách, dấu gạch dưới `_`, hay chữ hoa.
> - Nhánh làm việc phải được tạo từ nhánh `main` mới nhất (sau khi `git pull`).

---

## 2. Quy ước Conventional Commits

Dự án tuân theo chuẩn **[Conventional Commits v1.0](https://www.conventionalcommits.org/)**.

### Cấu trúc commit message

```
<type>(<scope>): <mô tả ngắn gọn>

[body — tuỳ chọn]

[footer — tuỳ chọn, ví dụ: Closes #123]
```

### Các loại commit

| Type | Khi nào dùng |
| :--- | :--- |
| `feat` | Thêm tính năng mới (tương ứng MINOR trong SemVer) |
| `fix` | Sửa lỗi (tương ứng PATCH trong SemVer) |
| `chore` | Cài đặt dependency, cấu hình, task build |
| `refactor` | Tái cấu trúc code, không thay đổi hành vi bên ngoài |
| `docs` | Cập nhật tài liệu |
| `style` | Định dạng code (space, semicolon...) không ảnh hưởng logic |
| `test` | Thêm hoặc cập nhật test |
| `perf` | Cải thiện hiệu năng |
| `ci` | Thay đổi CI/CD configuration |
| `revert` | Hoàn tác một commit trước đó |
| `build` | Thay đổi hệ thống build (Docker, Maven, Vite...) |

### Scope gợi ý

| Scope | Dùng khi thay đổi thuộc về |
| :--- | :--- |
| `frontend` | Code trong thư mục `frontend/` |
| `backend` | Code trong thư mục `backend/` |
| `db` | Schema, migration SQL, seed data |
| `auth` | Cơ chế xác thực Clerk |
| `docker` | Dockerfile, docker-compose |
| `ci` | Workflow GitHub Actions |
| `docs` | Tài liệu, README |

### Ví dụ commit hợp lệ

```bash
# Tính năng mới
feat(frontend): add product listing page with filters

# Sửa lỗi
fix(backend): resolve null pointer in JWT filter when header missing

# Cấu hình
chore(docker): add redis healthcheck to dev compose

# Tài liệu
docs(backend): update seed and verify instructions for Docker

# Nhiều thay đổi nhỏ trong cùng file
refactor(frontend): extract axios instance to lib/axios.ts

# Breaking change (thêm ! sau type/scope)
feat(db)!: rename column user_id to clerk_user_id in users table

BREAKING CHANGE: All existing queries referencing user_id must be updated.
```

> [!WARNING]
> - Dòng subject (tiêu đề) **không được vượt quá 72 ký tự**.
> - Dòng subject **không** kết thúc bằng dấu chấm `.`.
> - Dùng **tiếng Anh** cho commit message để đồng nhất lịch sử Git.
> - **Không dùng** message chung chung như `update`, `fix bug`, `change`, `wip`.

---

## 3. Quy trình Pull Request

### Vòng đời một PR

```
main ──────────────────────────────────────────────────► main
  │                                                        ▲
  └─► feat/my-feature ──► commits ──► PR ──► Review ──► Merge
```

### Quy tắc bắt buộc

1. **Tạo nhánh từ `main` mới nhất:**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feat/frontend-cart-page
   ```

2. **Giữ PR nhỏ và tập trung:** Mỗi PR chỉ giải quyết **một vấn đề** hoặc **một tính năng**. Không gộp nhiều tính năng không liên quan vào một PR.

3. **Điền đầy đủ PR Template** (tự động xuất hiện khi tạo PR trên GitHub). Không được xóa các mục trong template.

4. **Tự review trước khi request:** Đọc lại diff của chính mình, xóa code debug/console.log, kiểm tra không commit file `.env`.

5. **Liên kết với Issue:** Nếu PR giải quyết một GitHub Issue, ghi `Closes #<issue_number>` trong phần description.

6. **Cần ít nhất 1 Approve** từ thành viên khác trong team trước khi merge.

7. **Không merge PR của chính mình** khi chưa có review (trừ trường hợp khẩn cấp được team đồng ý).

### Chiến lược Merge

| Trường hợp | Chiến lược | Lý do |
| :--- | :--- | :--- |
| Feature / Fix PR thông thường | **Squash and Merge** | Giữ lịch sử `main` gọn gàng, 1 PR = 1 commit |
| Nhánh long-lived (staging) | **Merge Commit** | Giữ nguyên lịch sử |
| Hotfix | **Merge Commit** | Truy vết nhanh |

> [!NOTE]
> Sau khi merge, **xóa nhánh** feature trên GitHub để tránh ô nhiễm danh sách nhánh. GitHub có nút "Delete branch" tự động sau khi merge.

---

## 4. Quy ước Review Code

### Reviewer nên:
- ✅ Comment cụ thể, trỏ đến dòng code, giải thích lý do
- ✅ Phân biệt rõ: *must fix* (bắt buộc sửa) vs *suggestion* (gợi ý)
- ✅ Approve khi code đạt yêu cầu, kể cả còn typo nhỏ (Author tự sửa)
- ✅ Phản hồi trong vòng **1 ngày làm việc**

### Author nên:
- ✅ Giải thích quyết định thiết kế trong comment nếu cần
- ✅ Trả lời hoặc resolve **tất cả** comment trước khi merge
- ✅ Re-request review sau khi sửa xong

### Không nên:
- ❌ Approve PR mà không đọc code
- ❌ Merge khi còn unresolved comment
- ❌ Dùng ngôn ngữ chỉ trích cá nhân thay vì nhận xét code

---

## 5. Cheat Sheet

```bash
# Tạo nhánh mới từ main
git checkout main && git pull origin main
git checkout -b feat/backend-product-api

# Commit theo chuẩn
git add .
git commit -m "feat(backend): add product search endpoint with pagination"

# Push và tạo PR
git push -u origin feat/backend-product-api
# → Vào GitHub, tạo PR vào main, điền template

# Cập nhật nhánh với main (khi main có thay đổi mới)
git fetch origin
git rebase origin/main

# Xóa nhánh sau khi merge
git branch -d feat/backend-product-api
git push origin --delete feat/backend-product-api
```

---

*Mọi thắc mắc, góp ý về quy trình — tạo Issue với label `process` hoặc thảo luận trong group chat của team.*
