# Screen Spec — Update School Screen/Dialog

**Actor**: System Admin | **UC liên quan**: UC-095 (Report 3 §3.2 "Update Information School") | **Status**: Draft
**BR liên quan**: BR-028 (xác minh & khóa tọa độ GPS), BR-030 (kiểm tra ràng buộc vận hành liên quan liên kết học sinh-phụ huynh, áp dụng gián tiếp nếu sửa tham số ảnh hưởng)
**MSG liên quan**: MSG-001 (lỗi lưu dữ liệu trường), MSG-004, MSG-005, MSG-029 (cập nhật thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên trường | Input (text) | Bắt buộc | — | |
| 2 | Địa chỉ | Textarea | Bắt buộc | — | |
| 3 | Toạ độ GPS (latitude/longitude) | Input (number) x2 | latitude ∈ [-90, 90], longitude ∈ [-180, 180]; **disable nếu `coordinateLocked = true`** | BR-028 | Chỉ System Admin có quyền mở khoá (đặt lại `coordinateLocked = false`) kèm lý do bằng văn bản bắt buộc — xem field 4 |
| 4 | Nút "Mở khoá toạ độ" + lý do (chỉ hiện khi đã khoá) | Button + Textarea (lý do bắt buộc) | Lý do bắt buộc, ghi vào audit log | BR-028 | Ngoại lệ đặc biệt chỉ System Admin |
| 5 | Mã số thuế | Input (text) | Định dạng mã số thuế hợp lệ (MSG-007 nếu sai) | — | |
| 6 | Email chính thức | Input (email) | Định dạng email hợp lệ | — | |
| 7 | Tham số vận hành (giờ hoạt động...) | Input nhóm | — | — | **[Giả định]** — *(sửa 2026-07-01: đã gỡ "bán kính geofence" khỏi field này — BR-028 xác nhận rõ School Administration mới là chủ sở hữu cấu hình bán kính geofence, xem `Web/SchoolAdministration/37_SchoolConfiguration.md`. System Admin chỉ còn quản lý tọa độ GPS (field 3-4), không quản lý bán kính.)* |
| 8 | Nút "Lưu" | Button (primary) | — | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading khi submit.
- Empty: Không áp dụng (form pre-filled từ School Detail).
- Error: MSG-001 (lỗi hệ thống lưu), MSG-007 (sai mã số thuế), MSG-004/MSG-005; validation riêng nếu tọa độ ngoài khoảng hợp lệ hoặc bị khoá.
- Success: MSG-029, quay về `School Detail` với dữ liệu mới; nếu vừa xác nhận tọa độ lần đầu (trường chuyển ACTIVE) → `coordinateLocked` tự động = true (BR-028).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Edit" trên `School Detail`. Sau khi lưu quay về `School Detail`.
**Bố cục**: Form trung bình (~8 field, có nhóm tham số vận hành) — hiện trong `Sheet` trượt từ phải theo `Layout_Guidelines.md` mục 1.2 (không phải Dialog nhỏ do có nhóm tham số geofence + toạ độ). Trường toạ độ hiển thị kèm mini-map preview khi thay đổi giá trị.

*(Cập nhật 2026-07-01: màn `Campus Config` đã bị loại bỏ hoàn toàn — hệ thống không quản lý campus. Bán kính geofence không còn ở form này, đã chuyển hẳn về `Web/SchoolAdministration/37_SchoolConfiguration.md` theo đúng BR-028. Xem `13_CampusConfig.md` để biết lý do loại bỏ.)*

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update School Screen/Dialog, nền tảng Web, actor System Admin.
Bố cục: Sheet trượt từ phải, form dọc nhóm theo section (Thông tin cơ bản / Toạ độ GPS / Tham số vận hành), nút Lưu cố định cuối Sheet.
Thành phần bắt buộc: tên trường, địa chỉ, toạ độ GPS (disable khi khoá) + nút mở khoá kèm lý do, mã số thuế, email, tham số vận hành (giờ hoạt động), nút Lưu.
KHÔNG thêm field bán kính geofence — đã chuyển về School Administration (37_SchoolConfiguration.md).
Áp dụng Design System: field disable dùng màu neutral nhạt, cảnh báo khoá toạ độ dùng token info.
Trạng thái cần thể hiện: error (toạ độ ngoài khoảng, mã số thuế sai), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1781–1797 (Table 3-26 Update Information School Use Cases Specification)
- traceability_matrix.md §1.2 (UC-095 ↔ BR-028)
- business_rules.md BR-028 (quy tắc toạ độ GPS/coordinateLocked — phần bán kính geofence đã chuyển về School Administration, xem `37_SchoolConfiguration.md`)
