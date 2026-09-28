# Screen Spec — View Delivery Batch Records

**Actor**: Raw Food Supplier | **UC liên quan**: UC-109 - View Delivery Batch Records (Report 3 §3.2, dòng 3375–3395) — extends UC-043 (Manage Ingredient Batches) | **Status**: Draft
**BR liên quan**: BR-077 (theo Report 3 UC-109 "Business Rules: BR-077"); `traceability_matrix.md` dòng 146 ghi BR-071 — cả 2 đều xác nhận nội dung tương tự (lô hàng + số lượng), giữ cả 2 để đối chiếu
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Batch Records" | — | UC-109 Normal Flow bước 1 |
| 2 | "Back" Button | Button | Về Delivery Detail (20) | — | |
| 3 | Danh sách chuyến giao hàng lịch sử | Table/List | Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes | BR-071, BR-077 | |
| 4 | Filter theo khoảng ngày dispatch | Date range picker | — | — | |
| 5 | Filter theo trạng thái giao hàng | Select | — | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lịch sử giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A (màn xem, chỉ đọc).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "View Batch Records", hoặc từ Ingredient Batches (12). Điều hướng đi: N/A (màn cuối nhánh, chỉ đọc).
**Bố cục**: Admin Shell — `Table` đầy đủ (Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes) kèm filter theo khoảng ngày + trạng thái, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Delivery Batch Records, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter khoảng ngày + trạng thái, phân trang.
Component bắt buộc dùng: Table, DatePicker (range), Select, Badge.
Thành phần bắt buộc: Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3375–3395 (UC-109).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 146.
