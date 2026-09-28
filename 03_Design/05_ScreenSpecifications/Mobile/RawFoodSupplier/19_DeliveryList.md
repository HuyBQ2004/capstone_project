# Screen Spec — Delivery List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-039 - Manage Deliveries | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Deliveries" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Deliveries (18) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Delivery Information (21) | — | |
| 4 | Delivery Card | Card | Vehicle, Driver, Linked Batches, Schedule Status | BR-071 | |
| 5 | Filter theo ngày/trạng thái | Dropdown | — | — | |
| 6 | Search theo Delivery ID | Input | — | — | |
| 7 | Chọn 1 Card → xem chi tiết | Navigation | Điều hướng tới Delivery Detail (20) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có lịch giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Deliveries (18). Tương ứng node `DelMgmt["Delivery Management"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Delivery Detail (20), Submit Delivery Information (21).
**Bố cục**: 1 cột — AppBar, filter/search trên cùng, danh sách Card cuộn dọc (pull-to-refresh), FAB "+" mở Submit Delivery Information (21).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery List, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, filter/search trên cùng, danh sách Card, FAB "+", pull-to-refresh.
Component bắt buộc dùng: Card (M3), Chip, DropdownMenu, TextField, FloatingActionButton.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
