# Screen Spec — View Allergy Alerts Panel (Special Meal Monitoring)

**Actor**: Service Caterers | **UC liên quan**: UC-103 - View Allergy Alerts (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-065, BR-123
**MSG liên quan**: MSG-051 (Warning — xung đột dị ứng với thực đơn), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có hồ sơ dị ứng đang hoạt động | Table (Web) / Card list (Mobile) | Cột: Học sinh, Lớp, Loại dị ứng, Mức độ, Kế hoạch bữa đặc biệt | BR-065 | UC-103 Normal Flow bước 2 |
| 2 | Dị ứng mức "Severe" | Highlight màu `danger` | Bắt buộc nổi bật | BR-123 | UC-103 Normal Flow bước 3 |
| 3 | Xem chi tiết dị ứng 1 học sinh | Điều hướng chi tiết | Hiển thị dị ứng đã khai báo, ghi chú y tế, lịch sử bữa ăn an toàn | — | UC-103 Normal Flow bước 4 |
| 4 | Trạng thái xác nhận kép (dual-confirmation) | Badge | Bếp phát hành + Giáo viên bàn giao | BR-123 | UC-103 Normal Flow bước 5 |
| 5 | Cảnh báo xung đột dị ứng với thực đơn hôm nay | Alert banner | Hiện khi thực đơn duyệt có nguyên liệu trùng allergen | — | UC-103 Alternative Flow AF1, MSG-051 |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách.
- Empty: Không có học sinh dị ứng nào trong phạm vi actor — thông báo xác nhận (không phải lỗi).
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Meal Service Management Dashboard (nút "Xem Cảnh báo Dị ứng"). Thoát: back về Dashboard.
**Bố cục**: Page Header + `Table` (cột: Học sinh, Lớp, Loại dị ứng, Mức độ badge màu đỏ nếu Severe, Kế hoạch đặc biệt) + banner cảnh báo xung đột thực đơn trên cùng nếu có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Allergy Alerts Panel (Special Meal Monitoring), nền tảng Web, actor Service Caterers.
Bố cục: Page Header + banner cảnh báo xung đột thực đơn (nếu có) + Table (Học sinh, Lớp, Loại dị ứng, Mức độ, Kế hoạch đặc biệt, trạng thái xác nhận kép).
Thành phần bắt buộc: highlight màu đỏ cho dị ứng Severe (BR-123 tinh thần), badge xác nhận kép.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty (thông tin, không phải lỗi), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3931–3949 (UC-103 - View Allergy Alerts)
- business_rules.md BR-065, BR-123
- error_message.md MSG-051
