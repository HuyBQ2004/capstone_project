# Screen Spec — View Batch History

**Actor**: Raw Food Supplier | **UC liên quan**: UC-107 - View Batch History (Report 3 §3.2, dòng 3353–3373) — extends UC-043 (Manage Ingredient Batches) | **Status**: Draft
**BR liên quan**: BR-071 (theo Report 3 UC-107 "Business Rules: BR-071"); `traceability_matrix.md` dòng 145 ghi BR-076 (Truy xuất lô nguyên liệu) — cả 2 đều hợp lý ở góc độ khác nhau (BR-071 = nội dung chứng nhận lô, BR-076 = mục đích truy xuất nguồn gốc của màn xem lịch sử này), giữ cả 2
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Batch History" | — | UC-107 Normal Flow bước 1 |
| 2 | "Back" Button | Button | Về Batch Detail (14) | — | |
| 3 | Danh sách toàn bộ lô đã đăng ký | Table/List | Lot Number, Packaging Date, Expiration Date, Certificate Links | BR-071, BR-076 | |
| 4 | Search theo Lot Number | Input | — | — | |
| 5 | Filter theo Expiration Status | Select | Valid / Expiring Soon / Expired | BR-047 | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lô nào trong lịch sử.
- Error: MSG-004, MSG-005.
- Success: N/A (màn xem, chỉ đọc).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Batch Detail (14) qua nút "View Batch History". Điều hướng đi: N/A (màn cuối nhánh, chỉ đọc).
**Bố cục**: Admin Shell — `Table` đầy đủ (Lot Number, Packaging Date, Expiration Date, Certificate Links) kèm search/filter, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Batch History, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table lịch sử lô với search/filter + phân trang.
Component bắt buộc dùng: Table, Input (search), Select (filter), Badge.
Thành phần bắt buộc: Lot Number, Packaging Date, Expiration Date, Certificate Links.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3353–3373 (UC-107).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-076.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 145.
