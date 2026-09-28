# Screen Spec — View Allergy Alerts Panel (Special Meal Monitoring)

**Actor**: Service Caterers | **UC liên quan**: UC-103 - View Allergy Alerts (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-065, BR-123
**MSG liên quan**: MSG-051 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có hồ sơ dị ứng đang hoạt động | Card list | Mỗi card: Học sinh, Lớp, Loại dị ứng, Mức độ, Kế hoạch đặc biệt | BR-065 | UC-103 Normal Flow bước 2 |
| 2 | Dị ứng mức "Severe" | Highlight card màu `danger` | Bắt buộc nổi bật | BR-123 | UC-103 Normal Flow bước 3 |
| 3 | Xem chi tiết dị ứng 1 học sinh | Điều hướng chi tiết | Hiển thị dị ứng đã khai báo, ghi chú y tế, lịch sử bữa ăn an toàn | — | UC-103 Normal Flow bước 4 |
| 4 | Trạng thái xác nhận kép (dual-confirmation) | Chip kép | Bếp phát hành + Giáo viên bàn giao | BR-123 | UC-103 Normal Flow bước 5 |
| 5 | Cảnh báo xung đột dị ứng với thực đơn hôm nay | MaterialBanner | Hiện khi thực đơn duyệt có nguyên liệu trùng allergen | — | MSG-051 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Không có học sinh dị ứng nào — thông báo xác nhận (không phải lỗi).
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Meal Service Management Dashboard (icon "Xem Cảnh báo Dị ứng"). Thoát: back về Dashboard.
**Bố cục**: AppBar + banner cảnh báo xung đột thực đơn trên cùng (nếu có) + Card list cuộn dọc, card dị ứng Severe có viền/nền màu `danger` nổi bật.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Allergy Alerts Panel (Special Meal Monitoring), nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + banner cảnh báo xung đột thực đơn (nếu có) + Card list (Học sinh, Lớp, Loại dị ứng, Mức độ, Kế hoạch đặc biệt, chip xác nhận kép).
Thành phần bắt buộc: highlight màu đỏ cho dị ứng Severe, chip xác nhận kép.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3931–3949 (UC-103 - View Allergy Alerts)
- business_rules.md BR-065, BR-123
- error_message.md MSG-051
