# Screen Spec — Update Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-091 - Update Compliance Documents (Report 3 §3.2, dòng 2463–2483) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-009, MSG-029 (Success), MSG-053 (Warning — sắp hết hạn)

> **Ghi chú đối chiếu nguồn**: Report 3 UC-091, dòng Exceptions ghi "MSG-004, MSG-005, MSG-013", nhưng Alternative Flow AF2 mô tả hành vi là **"Invalid Document Format"** — về mặt nghiệp vụ khớp với **MSG-009** ("Unsupported file type") hơn là MSG-013 (dành cho chứng chỉ hết hạn, dùng ở UC-101/152/170). `traceability_matrix.md` dòng 114 cũng không liệt kê MSG-013 cho UC-091 (chỉ MSG-029, MSG-053). Khả năng cao đây là lỗi đánh máy trong bảng Exceptions gốc của Report 3 — dùng MSG-009 ở spec này, **[cần team xác nhận lại]** trước khi khoá.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên tài liệu đang cập nhật | — | UC-091 Normal Flow bước 2 |
| 2 | "Back" Button | Button | Về màn danh sách gọi tới (04/05/26) | — | |
| 3 | Thông tin tài liệu hiện tại (tên, ngày tải lên, ngày hết hạn, trạng thái) | Text/Badge | Chỉ đọc | BR-046 | |
| 4 | File thay thế | File upload | Bắt buộc nếu muốn thay tài liệu, PDF/JPG/PNG ≤10MB | BR-046 | MSG-009 nếu sai định dạng |
| 5 | Ngày hết hạn mới | Date picker | Bắt buộc khi thay file | BR-047, BR-048 | Nếu chứng nhận hết hạn được thay bằng bản hợp lệ → trạng thái tự chuyển "Valid", supplier được nhận lại purchase request (BR-048 AF1) |
| 6 | "Submit"/"Save" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048 | |
| 7 | "Cancel" Button | Button | Huỷ, giữ nguyên tài liệu cũ | — | AF3 |

### Trạng thái màn hình
- Loading: Skeleton form với dữ liệu tài liệu hiện tại.
- Empty: N/A.
- Error: MSG-009 (sai định dạng file) — xem ghi chú đối chiếu nguồn ở trên.
- Warning: MSG-053 nếu ngày hết hạn mới vẫn còn <30 ngày.
- Success: MSG-029 "Profile updated successfully" (dùng chung mẫu cập nhật hồ sơ).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26) khi chọn 1 tài liệu hiện có. Điều hướng đi: về lại màn danh sách gọi tới.
**Bố cục**: `Dialog`/`Sheet` form ngắn (`Layout_Guidelines.md` §1.2), hiển thị thông tin tài liệu cũ phía trên, form thay thế phía dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Documents, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet, thông tin tài liệu cũ phía trên (chỉ đọc), form thay thế phía dưới.
Component bắt buộc dùng: Dialog, Badge, file upload + preview, DatePicker, Button.
Thành phần bắt buộc: thông tin tài liệu hiện tại, file thay thế, ngày hết hạn mới, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-009), warning (MSG-053), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2463–2483 (UC-091).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 114 (đối chiếu MSG — xem ghi chú lệch ở trên).
