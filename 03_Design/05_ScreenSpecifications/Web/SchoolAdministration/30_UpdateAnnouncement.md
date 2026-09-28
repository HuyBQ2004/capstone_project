# Screen Spec — Update Announcement

**Actor**: School Administration | **UC liên quan**: UC-089 (Report 3 §3.2 "Update Announcement") | **Status**: Draft
**BR liên quan**: BR-144 (lịch sử sửa đổi thông báo)
**MSG liên quan**: MSG-029 (cập nhật thành công — theo Normal Flow bước 6 ghi "MSG-074", nhưng MSG-074 trong `error_message.md` là "Cập nhật thực đơn thành công", không khớp ngữ nghĩa announcement — xem ghi chú), MSG-047 (công bố thông báo)

> **Ghi chú lệch MSG**: tương tự UC-017, Normal Flow UC-089 (Report 3 dòng 4252) ghi "system saves the updated announcement (MSG-074)" — nhưng MSG-074 = "Menu updated successfully" theo `error_message.md`, sai domain. **[Giả định — cần team xác nhận]**: dùng MSG-047 khi Publish, hoặc thông báo chung "Cập nhật thành công" khi chỉ lưu Draft.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tiêu đề | Input (text) | Bắt buộc | — | |
| 2 | Nội dung | Textarea/Rich text | Bắt buộc | — | |
| 3 | Publication Status | RadioGroup (Draft/Published) | — | — | |
| 4 | Lịch sử sửa đổi (revision history) | Table rút gọn | Ghi timestamp + School Admin ID mỗi lần sửa | BR-144 | |
| 5 | Nút "Lưu" | Button (primary) | — | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading.
- Empty: Không áp dụng (form pre-filled).
- Error: EX1 — thông báo không tồn tại (đã bị xoá) → "Announcement Not Found", quay về danh sách; EX2 — thiếu field bắt buộc → validation error.
- Success: Lưu thành công + ghi revision history (BR-144); nếu chuyển sang Published → gửi thông báo cho users liên quan trường.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ `Announcement Detail` (nút Edit). Sau khi lưu quay về `Announcement Detail`.
**Bố cục**: Form hiện trong `Dialog`/`Sheet`, có thêm khối lịch sử sửa đổi rút gọn bên dưới form.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Announcement, nền tảng Web, actor School Administration.
Bố cục: Dialog/Sheet, form 1 cột + bảng lịch sử sửa đổi rút gọn cuối form.
Thành phần bắt buộc: Tiêu đề, Nội dung, Publication Status, bảng lịch sử sửa đổi, nút Lưu.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: error (không tìm thấy, thiếu field), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4245–4261 (Table 3-124 Update Announcement Use Cases Specification)
- traceability_matrix.md §1.10 (UC-089 ↔ BR-144)
