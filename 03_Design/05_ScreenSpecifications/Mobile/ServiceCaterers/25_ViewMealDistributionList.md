# Screen Spec — View Meal Distribution List

**Actor**: Service Caterers | **UC liên quan**: UC-132 - View Meal Distribution List (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-065, BR-120, BR-121, BR-123
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phân phát theo lớp | Card list | Mỗi card: Lớp, Số học sinh, Số suất đặc biệt, Trạng thái phục vụ | — | UC-132 Normal Flow bước 2 |
| 2 | Bộ lọc theo ngày/lớp/loại bữa ăn | Filter chip row | Tuỳ chọn | — | UC-132 Normal Flow bước 3 |
| 3 | Cảnh báo dị ứng nổi bật | Chip màu `danger` | Hiện khi lớp có học sinh dị ứng đã đăng ký | BR-123 | — |
| 4 | Cảnh báo lớp chưa xác nhận phân phát | Highlight card (viền màu warning) | Hiện khi giáo viên chủ nhiệm chưa xác nhận | BR-120 | UC-132 Alternative Flow AF2 |
| 5 | Nút "Xuất PDF/Excel" | Button (AppBar action) | — | — | UC-132 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Chưa có phiên phục vụ nào trong khoảng lọc.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Meal Service Management Dashboard (icon "Xem Danh sách Phân phát"). Thoát: back về Dashboard.
**Bố cục**: AppBar (icon Xuất báo cáo) + filter chip row + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Meal Distribution List, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter chip row + Card list (Lớp, Số học sinh, Số suất đặc biệt, Trạng thái, chip cảnh báo dị ứng).
Thành phần bắt buộc: chip cảnh báo dị ứng màu đỏ, highlight card lớp chưa xác nhận phân phát.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3953–3971 (UC-132 - View Meal Distribution List)
- business_rules.md BR-065, BR-120, BR-121, BR-123
