# Screen Spec — Update Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-091 - Update Compliance Documents | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-009, MSG-029, MSG-053

> **Ghi chú đối chiếu nguồn**: xem giải thích lệch MSG-013 vs MSG-009 ở bản Web cùng tên (`Web/RawFoodSupplier/25_UpdateDocuments.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên tài liệu đang cập nhật | — | |
| 2 | "Back" Button (AppBar) | Button | Về màn danh sách gọi tới | — | |
| 3 | Thông tin tài liệu hiện tại | Text/Chip | Chỉ đọc | BR-046 | |
| 4 | File thay thế | File upload (camera/thư viện) | Bắt buộc nếu thay tài liệu, ≤10MB | BR-046 | MSG-009 nếu sai định dạng |
| 5 | Ngày hết hạn mới | Date picker | Bắt buộc khi thay file | BR-047, BR-048 | |
| 6 | "Submit"/"Save" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048 | |
| 7 | "Cancel" Button | Button | Huỷ, giữ nguyên tài liệu cũ | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-009.
- Warning: MSG-053.
- Success: MSG-029.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26).
**Bố cục**: Modal bottom sheet — thông tin tài liệu cũ (chỉ đọc) trên cùng, form thay thế bên dưới, nút Submit/Cancel cố định cuối sheet.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Documents, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet, thông tin tài liệu cũ trên cùng, form thay thế bên dưới.
Component bắt buộc dùng: Chip, image_picker upload, DatePicker (M3), FilledButton.
Thành phần bắt buộc: thông tin tài liệu hiện tại, file thay thế, ngày hết hạn mới, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-009), warning (MSG-053), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2463–2483 (UC-091).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 114.
