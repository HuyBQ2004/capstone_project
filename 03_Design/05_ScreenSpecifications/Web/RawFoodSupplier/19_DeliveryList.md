# Screen Spec — Delivery List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-039 - Manage Deliveries (Report 3 §3.2, dòng 3067–3087) | **Status**: Draft
**BR liên quan**: BR-071 (Linked Batches)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Deliveries" | — | UC-039 Normal Flow bước 1–2 |
| 2 | "Back" Button | Button | Về Deliveries (18) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Delivery Information (21) | — | |
| 4 | Delivery Card (mỗi dòng) | Card/Row | Vehicle, Driver, Linked Batches, Schedule Status | BR-071 | UC-039 Normal Flow bước 2 |
| 5 | Filter theo ngày/trạng thái | Input/Select | Filter by date, status | — | |
| 6 | Search theo Delivery ID | Input | — | — | |
| 7 | Chọn 1 dòng → xem chi tiết | Navigation | Điều hướng tới Delivery Detail (20) | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lịch giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Deliveries (18). Tương ứng node `DelMgmt["Delivery Management"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Delivery Detail (20), Submit Delivery Information (21).
**Bố cục**: Admin Shell — `Table` (cột: Vehicle, Driver, Linked Batches, Schedule Status) kèm filter theo ngày/trạng thái, search theo Delivery ID, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery List, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter ngày/trạng thái + search + phân trang.
Component bắt buộc dùng: Table, Badge, Input, Select, Button.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`.
