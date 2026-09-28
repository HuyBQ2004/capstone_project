# Screen Spec — Delivery Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-039 - Manage Deliveries; điều hướng tới UC-092, UC-109 | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng | — | |
| 2 | "Back" Button (AppBar) | Button | Về Delivery List (19) | — | |
| 3 | Vehicle | Text | — | BR-071 | |
| 4 | Driver | Text | — | BR-050 | |
| 5 | Linked Batches | List (Card con) | Danh sách lô nguyên liệu gắn với chuyến | BR-071 | |
| 6 | Schedule Status (Chip) | Chip | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071, BR-135 | |
| 7 | "Edit" Button | Navigation | Điều hướng Submit Delivery Information (21, edit) | BR-071 | Chỉ khi Pending |
| 8 | "Cancel Delivery" Button | Button | Chỉ khả dụng nếu Pending | BR-071 | |
| 9 | "Update Status" Button | Navigation | Điều hướng Update Delivery Status (23) | — | |
| 10 | "View Batch Records" Button | Navigation | Điều hướng View Delivery Batch Records (24) | — | |

### Trạng thái màn hình
- Loading: Shimmer chi tiết.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Delivery List (19) khi chọn 1 Card. Điều hướng đi: Submit Delivery Information (21, edit), Update Delivery Status (23), View Delivery Batch Records (24).
**Bố cục**: 1 cột — AppBar (Back), Card thông tin chuyến giao trên cùng, danh sách Linked Batches Card con bên dưới, các nút hành động xếp dọc cuối màn hình.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery Detail, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, Card thông tin chuyến giao trên cùng, danh sách Card lô liên kết bên dưới, nút hành động xếp dọc cuối màn hình.
Component bắt buộc dùng: Card (M3), Chip, OutlinedButton, FilledButton.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status, nút Edit/Cancel/Update Status/View Batch Records.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071, BR-135.
