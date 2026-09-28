# Screen Spec — Submit Batch Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-081 - Submit Batch Information (Report 3 §3.2, dòng 3265–3285) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048, BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-013 (chặn nếu chứng nhận supplier đã hết hạn theo BR-048)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Batch Information" | — | UC-081 Normal Flow bước 1 |
| 2 | Product Name | Input text | Bắt buộc | BR-071 | |
| 3 | Lot Number | Input text | Bắt buộc, duy nhất | BR-071 | |
| 4 | Manufacturing Date | Date picker | Bắt buộc | BR-071 | |
| 5 | Packaging Date | Date picker | Bắt buộc | BR-071 | |
| 6 | Expiration Date | Date picker | Bắt buộc, phải sau Packaging Date | BR-071 | |
| 7 | Liên kết chứng chỉ an toàn (safety certificate links) | Select/Multi-select | Chọn từ danh sách chứng chỉ đã có ở Compliance Documents (26) | BR-046 | Nếu supplier có chứng chỉ hết hạn (BR-048) → chặn submit, MSG-013 |
| 8 | "Submit" Button | Button | Validate ngày tháng + chứng chỉ hợp lệ | BR-046, BR-047, BR-048, BR-071 | |
| 9 | "Cancel" Button | Button | Huỷ, về Batch List (13)/Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Skeleton form (khi ở chế độ Edit từ Batch Detail 14).
- Empty: N/A.
- Error: MSG-013 (chứng nhận supplier hết hạn, chặn theo BR-048).
- Success: Đăng ký lô thành công → điều hướng Batch Detail (14) của lô vừa tạo.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Ingredient Batches (12) hoặc Batch List (13) qua "Add / Plus" Icon, hoặc từ Batch Detail (14) qua "Edit". Điều hướng đi: Batch Detail (14).
**Bố cục**: Form ngắn hiện trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2, <8 field).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Batch Information, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn.
Component bắt buộc dùng: Dialog, Input, DatePicker, Select (multi), Button.
Thành phần bắt buộc: Product Name, Lot Number, Manufacturing/Packaging/Expiration Date, liên kết chứng chỉ, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-013), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3265–3285 (UC-081).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048, BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-013.
