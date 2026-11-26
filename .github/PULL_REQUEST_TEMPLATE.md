## 📝 Mô tả

<!-- Giải thích ngắn gọn PR này làm gì và tại sao cần thay đổi này -->



## 🔗 Liên kết Issue

<!-- Nếu PR giải quyết một GitHub Issue, ghi: Closes #<số_issue> -->
<!-- Nếu không có issue: ghi N/A -->

Closes #

---

## 🧪 Loại thay đổi

<!-- Đánh dấu [x] vào ô phù hợp -->

- [ ] ✨ `feat` — Tính năng mới
- [ ] 🐛 `fix` — Sửa lỗi
- [ ] ♻️ `refactor` — Tái cấu trúc code (không thay đổi hành vi)
- [ ] 📚 `docs` — Cập nhật tài liệu
- [ ] 🔧 `chore` — Cài đặt, cấu hình, build
- [ ] 🚨 `test` — Thêm hoặc sửa test
- [ ] ⚡ `perf` — Cải thiện hiệu năng
- [ ] 💥 Breaking change (thay đổi làm vỡ tính tương thích ngược)

---

## ✅ Checklist trước khi tạo PR

<!-- Tích [x] vào từng mục sau khi hoàn thành -->

**Chung:**
- [ ] Code đã được tự review (đọc lại diff của mình)
- [ ] Không có file `.env`, secret hay credential nào bị commit nhầm
- [ ] Đã xóa toàn bộ `console.log`, `System.out.println` debug thừa
- [ ] Tên nhánh đúng theo quy ước `<type>/<scope>-<mô-tả>` trong [CONTRIBUTING.md](../CONTRIBUTING.md)
- [ ] Commit message đúng Conventional Commits format

**Backend (nếu có thay đổi):**
- [ ] Đã kiểm tra API hoạt động đúng qua Swagger UI (`http://localhost:8000/swagger-ui`)
- [ ] Nếu thay đổi DB schema → đã tạo migration file Flyway mới (không sửa file cũ)
- [ ] Các biến môi trường mới đã được bổ sung vào `backend/.env.example`

**Frontend (nếu có thay đổi):**
- [ ] Giao diện đã được kiểm tra trực quan trên trình duyệt
- [ ] Build production thành công: `npm run build`
- [ ] Lint không có lỗi: `npm run lint`
- [ ] Biến môi trường mới đã được bổ sung vào `frontend/.env.example`

---

## 📌 Ghi chú thêm cho Reviewer

<!-- Những điểm cần reviewer chú ý đặc biệt, quyết định thiết kế, trade-off,
     hoặc những gì chủ động không xử lý trong PR này (sẽ làm ở PR sau) -->


