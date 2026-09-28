# Screen Spec — Upload Supplier Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates, UC-102 - Upload Product Origin Documents (form dùng chung cho cả 2 UC, phân biệt bằng dropdown phân loại tài liệu) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-008, MSG-009, MSG-013 (chỉ áp dụng khi loại tài liệu = Food Safety Certificate và ngày hết hạn đã qua — theo UC-101 AF1)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload documents" | — | Report 3 §3.3.2.5.7 |
| 2 | "Back" Button | Button | Về màn danh sách gọi tới (04 hoặc 05) | — | |
| 3 | "Cancel" Button | Button | Huỷ, không lưu | — | |
| 4 | Section Header — "Upload New Profile" | Label | — | — | Report 3 §3.3.2.5.7 |
| 5 | Classification of compliance documents (dropdown) | Select | Bắt buộc — chọn "Food Safety Certificate" (UC-101) hoặc "Product Origin Document" (VietGAP/GlobalGAP/Organic...) (UC-102) | — | Quyết định luồng validation phía sau |
| 6 | Document Classification Text (hiển thị loại đã chọn) | Text | — | — | |
| 7 | Tên tài liệu / Cơ quan cấp | Input text | Bắt buộc | BR-046 | |
| 8 | File tài liệu | File upload | Bắt buộc, PDF/JPG/PNG, ≤10MB (MSG-008/009) | BR-046 | |
| 9 | Expiration date of the document | Date picker | Bắt buộc nếu loại = Food Safety Certificate; tuỳ chọn nếu Product Origin Document không có hạn | BR-047, BR-048 | Nếu ngày đã qua và loại = Food Safety Certificate → chặn submit, hiển thị MSG-013 (BR-048) |
| 10 | "Add / Plus" Icon | Button | Thêm 1 tài liệu khác trong cùng phiên upload | — | |
| 11 | "Submit"/"Save" Button | Button | Validate toàn bộ trước khi lưu | BR-046, BR-047, BR-048 | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: MSG-008 (file >10MB), MSG-009 (sai định dạng), MSG-013 (chứng nhận ATTP hết hạn, chặn upload theo BR-048).
- Warning: Nếu ngày hết hạn còn <30 ngày → hiển thị cảnh báo "sẽ sớm hết hạn" (BR-047) nhưng vẫn cho phép lưu.
- Success: Lưu thành công → quay về màn danh sách gọi tới (04/05/26), MSG-029 (cập nhật hồ sơ compliance thành công, dùng theo mẫu chung của UC-091/167).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26) qua "Add / Plus" Icon. Tương ứng node `UploadCert["Upload Certificates Form"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`.
**Bố cục**: Form ngắn hiện trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2, form <8 field) thay vì trang riêng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Supplier Certificates, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn, không phải trang riêng.
Component bắt buộc dùng: Dialog, Select, Input, DatePicker, file upload + preview, Button.
Thành phần bắt buộc: dropdown phân loại tài liệu, tên tài liệu/cơ quan cấp, file upload, ngày hết hạn, nút Submit/Cancel/Add.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-008/009/013), warning (sắp hết hạn), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6293–6309 (§3.3.2.5.7 Supplier Upload Certificates).
- `06_Document/01_Report/Report 3.md` dòng 2485–2527 (UC-101, UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-008, MSG-009, MSG-013.
