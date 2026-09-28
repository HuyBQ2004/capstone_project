# Screen Spec — Upload Supplier Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates, UC-102 - Upload Product Origin Documents (form dùng chung) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-008, MSG-009, MSG-013

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload documents" | — | Report 3 §3.3.2.5.7 |
| 2 | "Back" Button (AppBar) | Button | Về màn danh sách gọi tới (04 hoặc 05) | — | |
| 3 | "Cancel" Button | Button | Huỷ, không lưu | — | |
| 4 | Section Header — "Upload New Profile" | Label | — | — | |
| 5 | Classification of compliance documents (dropdown) | Select | Bắt buộc — Food Safety Certificate hoặc Product Origin Document | — | |
| 6 | Document Classification Text | Text | — | — | |
| 7 | Tên tài liệu / Cơ quan cấp | Input text | Bắt buộc | BR-046 | |
| 8 | File tài liệu | File upload (camera/thư viện) | Bắt buộc, PDF/JPG/PNG, ≤10MB | BR-046 | |
| 9 | Expiration date of the document | Date picker | Bắt buộc nếu Food Safety Certificate | BR-047, BR-048 | Ngày đã qua + loại Food Safety → chặn, MSG-013 |
| 10 | "Add / Plus" Icon | Button | Thêm 1 tài liệu khác | — | |
| 11 | "Submit"/"Save" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048 | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-008, MSG-009, MSG-013.
- Warning: Ngày hết hạn <30 ngày → cảnh báo nhưng vẫn lưu được (BR-047).
- Success: Lưu thành công → quay về màn danh sách gọi tới.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26). Tương ứng node `UploadCert["Supplier Upload Certificates Form"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`, cũng được gọi từ `BatchDetail["Ingredient Batch Detail"]` (upload certificate cho batch cụ thể — xem màn Upload Batch Certificates, 16).
**Bố cục**: Trượt từ dưới lên (`showModalBottomSheet`, `Layout_Guidelines.md` §1.2/`Component_Guidelines.md`) — form ngắn 1 trang, dùng `image_picker` cho file upload.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Supplier Certificates, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet trượt từ dưới lên, form 1 trang.
Component bắt buộc dùng: DropdownMenu (M3), TextField, DatePicker, image_picker upload, FilledButton.
Thành phần bắt buộc: dropdown phân loại tài liệu, tên tài liệu/cơ quan cấp, file upload, ngày hết hạn, nút Submit/Cancel/Add.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-008/009/013), warning, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6293–6309 (§3.3.2.5.7), dòng 2485–2527 (UC-101, UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-008, MSG-009, MSG-013.
