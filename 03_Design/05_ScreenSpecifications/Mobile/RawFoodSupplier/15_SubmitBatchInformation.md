# Screen Spec — Submit Batch Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-081 - Submit Batch Information | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048, BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-013

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Batch Information" | — | |
| 2 | Product Name | Input text | Bắt buộc | BR-071 | |
| 3 | Lot Number | Input text | Bắt buộc, duy nhất | BR-071 | |
| 4 | Manufacturing Date | Date picker | Bắt buộc | BR-071 | |
| 5 | Packaging Date | Date picker | Bắt buộc | BR-071 | |
| 6 | Expiration Date | Date picker | Bắt buộc, sau Packaging Date | BR-071 | |
| 7 | Liên kết chứng chỉ an toàn | Multi-select | Chọn từ Compliance Documents (26) | BR-046 | MSG-013 nếu chứng chỉ hết hạn |
| 8 | "Submit" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048, BR-071 | |
| 9 | "Cancel" Button | Button | Huỷ, về Batch List (13)/Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-013.
- Success: Đăng ký lô thành công → điều hướng Batch Detail (14).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Ingredient Batches (12)/Batch List (13) qua FAB "+", hoặc Batch Detail (14) qua FAB "Edit".
**Bố cục**: Form 1 trang trong `showModalBottomSheet` (form ngắn <8 field, `Component_Guidelines.md`).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Batch Information, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet, form 1 trang.
Component bắt buộc dùng: TextField, DatePicker (M3), DropdownMenu (multi), FilledButton.
Thành phần bắt buộc: Product Name, Lot Number, Manufacturing/Packaging/Expiration Date, liên kết chứng chỉ, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-013), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3265–3285 (UC-081).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048, BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-013.
