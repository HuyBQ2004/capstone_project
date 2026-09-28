# Screen Spec — Meal Service Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-047 - Manage Meal Service (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-100, BR-121, BR-125
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên phục vụ bữa ăn đang hoạt động/dự kiến | Card list | Mỗi card: Lớp, Giờ phục vụ, Số suất, Trạng thái | — | UC-047 Normal Flow bước 2 |
| 2 | Chi tiết phân bổ suất ăn theo lớp | Read-only detail (trang riêng) | Điều chỉnh timing/nhân sự/số suất | BR-121 | UC-047 Normal Flow bước 4–5 |
| 3 | Cảnh báo session đang phục vụ | MaterialBanner | Chỉ cho sửa field không quan trọng, chặn giảm số suất | — | UC-047 Alternative Flow AF1 |
| 4 | Cảnh báo lệch số suất so với danh sách phân phát | MaterialBanner | Yêu cầu xác nhận trước khi lưu | BR-121 | UC-047 Alternative Flow AF2 |
| 5 | Nút "Xem Danh sách Phân phát" | Button (AppBar action) | Điều hướng View Meal Distribution List | — | ScreenFlow |
| 6 | Nút "Xem Cảnh báo Dị ứng" | Button (AppBar action) | Điều hướng View Allergy Alerts Panel | BR-125 | ScreenFlow |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách phiên phục vụ.
- Empty: Chưa có phiên phục vụ nào được lên lịch hôm nay.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `SnackBar` xác nhận cập nhật cấu hình phiên phục vụ thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Phục vụ Bữa ăn" trong Grid Menu Items. Điều hướng tiếp: View Meal Distribution List, View Allergy Alerts Panel.
**Bố cục**: AppBar (icon Danh sách Phân phát + Cảnh báo Dị ứng) + Card list cuộn dọc (pull-to-refresh), chạm vào card mở trang chi tiết phiên phục vụ.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Service Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar (icon Danh sách Phân phát/Cảnh báo Dị ứng) + Card list phiên phục vụ (Lớp, Giờ, Số suất, Trạng thái).
Thành phần bắt buộc: banner cảnh báo lệch số suất, banner session đang active hạn chế sửa.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3821–3839 (UC-047 - Manage Meal Service)
- business_rules.md BR-100, BR-121, BR-125
