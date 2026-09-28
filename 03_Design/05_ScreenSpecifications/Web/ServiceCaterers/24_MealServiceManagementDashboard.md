# Screen Spec — Meal Service Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-047 - Manage Meal Service (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-100, BR-121, BR-125
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên phục vụ bữa ăn đang hoạt động/dự kiến | Table (Web) / Card list (Mobile) | Cột: Lớp, Giờ phục vụ, Số suất, Trạng thái | — | UC-047 Normal Flow bước 2 |
| 2 | Chi tiết phân bổ suất ăn theo lớp | Read-only detail | Điều chỉnh timing/nhân sự/số suất | BR-121 | UC-047 Normal Flow bước 4–5 |
| 3 | Cảnh báo session đang phục vụ | Alert banner | Nếu session đang Active, chỉ cho sửa field không quan trọng (nhân sự), chặn giảm số suất | — | UC-047 Alternative Flow AF1 |
| 4 | Cảnh báo lệch số suất so với danh sách phân phát | Alert banner | Yêu cầu xác nhận trước khi lưu | BR-121 | UC-047 Alternative Flow AF2 |
| 5 | Nút "Xem Danh sách Phân phát" | Button | Điều hướng View Meal Distribution List | — | ScreenFlow |
| 6 | Nút "Xem Cảnh báo Dị ứng" | Button | Điều hướng View Allergy Alerts Panel | BR-125 | ScreenFlow |

### Trạng thái màn hình
- Loading: `Skeleton` cho danh sách phiên phục vụ.
- Empty: Chưa có phiên phục vụ nào được lên lịch hôm nay.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `Toast` xác nhận cập nhật cấu hình phiên phục vụ thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Phục vụ Bữa ăn" trong Sidebar. Điều hướng tiếp: View Meal Distribution List, View Allergy Alerts Panel, Manage Special Meals Configuration.
**Bố cục**: Page Header + `Table` danh sách phiên phục vụ + panel chi tiết mở bên phải (Sheet) khi chọn 1 dòng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Service Management Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table danh sách phiên phục vụ (Lớp, Giờ, Số suất, Trạng thái), Sheet chi tiết bên phải khi chọn 1 dòng, link Xem Danh sách Phân phát/Xem Cảnh báo Dị ứng.
Thành phần bắt buộc: banner cảnh báo lệch số suất, banner session đang active hạn chế sửa.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3821–3839 (UC-047 - Manage Meal Service)
- business_rules.md BR-100, BR-121, BR-125
