# Screen Spec — View Delivery Batch Records

**Actor**: Raw Food Supplier | **UC liên quan**: UC-109 - View Delivery Batch Records — extends UC-043 | **Status**: Draft
**BR liên quan**: BR-071, BR-077 (xem ghi chú đối chiếu nguồn ở bản Web cùng tên)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Batch Records" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Delivery Detail (20) | — | |
| 3 | Danh sách chuyến giao hàng lịch sử | Card list | Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes | BR-071, BR-077 | |
| 4 | Filter theo khoảng ngày dispatch | Date range picker | — | — | |
| 5 | Filter theo trạng thái giao hàng | Dropdown | — | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách.
- Empty: Chưa có lịch sử giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "View Batch Records".
**Bố cục**: 1 cột — AppBar (Back), filter (date range + trạng thái) trên cùng, danh sách Card cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Delivery Batch Records, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, filter date range + trạng thái trên cùng, danh sách Card cuộn dọc, pull-to-refresh.
Component bắt buộc dùng: Card (M3), DatePicker (range), DropdownMenu.
Thành phần bắt buộc: Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes.
Áp dụng Design System: chip/badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3375–3395 (UC-109).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 146.
