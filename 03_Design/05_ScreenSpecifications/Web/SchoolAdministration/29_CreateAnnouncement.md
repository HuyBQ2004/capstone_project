# Screen Spec — Create Announcement

**Actor**: School Administration | **UC liên quan**: UC-017 (Report 3 §3.2 "Create Announcement") | **Status**: Draft
**BR liên quan**: BR-138 (cảnh báo quan trọng bỏ qua phê duyệt), BR-144 (lịch sử sửa đổi thông báo)
**MSG liên quan**: MSG-071 (lưu draft thành công), MSG-072 (publish thành công — lưu ý MSG-072 gốc trong `error_message.md` là "Special meal plan saved successfully", KHÁC với mô tả trong Normal Flow UC-017 "MSG-072" cho publish thông báo — xem ghi chú)

> **Ghi chú lệch MSG**: Normal Flow UC-017 (Report 3 dòng 4076) ghi "If Published is selected... displays MSG-072" — nhưng theo `error_message.md` hiện hành, MSG-072 = "Special meal plan saved successfully" (không liên quan announcement). Khả năng đây là lệch số giữa 2 lần cập nhật tài liệu MSG. **[Giả định — cần team xác nhận]**: dùng MSG-047 ("Announcement published successfully to target audience" — khớp đúng ngữ nghĩa "publish announcement" trong `error_message.md`) thay cho MSG-072 khi hiển thị thông báo publish thành công.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tiêu đề | Input (text) | Bắt buộc | — | |
| 2 | Nội dung | Textarea/Rich text | Bắt buộc | — | |
| 3 | Nhóm đối tượng nhận (Target Group) | Select/Checkbox group | Bắt buộc (Parents/Teachers/Service Caterers...) | — | |
| 4 | Publication Status | RadioGroup (Draft/Published) | Mặc định Draft | BR-138 | Nếu Published → gửi thông báo real-time qua FCM/email ngay |
| 5 | Nút "Lưu" | Button (primary) | Disable tới khi field bắt buộc hợp lệ | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading.
- Empty: Không áp dụng (form tạo mới).
- Error: EX1 — thiếu field bắt buộc khi Publish → validation error, chặn submit.
- Success: MSG-071 nếu Draft; nếu Published — dùng MSG-047 theo ghi chú lệch số ở trên (không phải MSG-072 như Report 3 gốc ghi).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create Announcement" trên `School Announcement Management`. Sau khi lưu quay về danh sách.
**Bố cục**: Form ngắn hiện trong `Dialog`/`Sheet` theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Announcement, nền tảng Web, actor School Administration.
Bố cục: Dialog/Sheet, form 1 cột.
Thành phần bắt buộc: Tiêu đề, Nội dung (rich text), Target Group (checkbox), Publication Status (radio Draft/Published), nút Lưu.
Áp dụng Design System: cảnh báo publish ngay dùng token info.
Trạng thái cần thể hiện: error (thiếu field khi publish), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4069–4085 (Table 3-120 Create Announcement Use Cases Specification)
- traceability_matrix.md §1.10 (UC-017 ↔ BR-138/BR-144)
- error_message.md MSG-047, MSG-071 (đối chiếu lệch số với MSG-072 gốc trong Report 3)
