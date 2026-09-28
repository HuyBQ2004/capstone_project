# Screen Spec — Delivery Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-039 - Manage Deliveries; điều hướng tới UC-092 (Update Delivery Status), UC-109 (View Delivery Batch Records) | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng (Delivery ID) | — | Mở rộng từ UC-039 Normal Flow bước 2 |
| 2 | "Back" Button | Button | Về Delivery List (19) | — | |
| 3 | Vehicle (biển số xe) | Text | — | BR-071 | |
| 4 | Driver (tên nhân viên giao hàng) | Text | — | BR-050 (đăng ký nhân viên giao hàng — dữ liệu tham chiếu, không chỉnh sửa ở màn này) | |
| 5 | Linked Batches | List | Danh sách lô nguyên liệu gắn với chuyến giao này | BR-071 | Điều hướng tới View Delivery Batch Records (24) |
| 6 | Schedule Status (Badge) | Badge | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071, BR-135 | |
| 7 | "Edit" Button | Navigation | Chỉnh sửa driver/vehicle nếu đang Pending — điều hướng Submit Delivery Information (21) chế độ Edit | BR-071 | UC-039 Normal Flow bước 4 |
| 8 | "Cancel Delivery" Button | Button | Chỉ khả dụng nếu trạng thái đang Pending | BR-071 | UC-039 Normal Flow bước 4 |
| 9 | "Update Status" Button | Navigation | Điều hướng tới Update Delivery Status (23) | — | UC-092 |
| 10 | "View Batch Records" Button | Navigation | Điều hướng tới View Delivery Batch Records (24) | — | UC-109 |

### Trạng thái màn hình
- Loading: Skeleton chi tiết chuyến giao hàng.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A (hành động ghi dữ liệu ở màn con 21/23).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Delivery List (19) khi chọn 1 dòng. Điều hướng đi: Submit Delivery Information (21, edit), Update Delivery Status (23), View Delivery Batch Records (24).
**Bố cục**: Admin Shell — Content Area 2 cột: thông tin chuyến giao (Vehicle, Driver, Schedule Status) bên trái (col-span-5), danh sách Linked Batches bên phải (col-span-7); action buttons cố định cuối trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery Detail, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 cột — thông tin chuyến giao bên trái, danh sách lô liên kết bên phải, action buttons cuối trang.
Component bắt buộc dùng: Card, Badge, Table (batch), Button.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status, nút Edit/Cancel/Update Status/View Batch Records.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071, BR-135.
