# Screen Spec — Receiving Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-050, BR-078
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lịch giao hàng hôm nay | Card list | Mỗi card: Nhà cung cấp, Giờ dự kiến, Trạng thái | BR-078 | UC-044 Normal Flow bước 2 |
| 2 | Trạng thái giao hàng | Chip | Scheduled / Dispatched / In Progress / Completed | BR-078 | UC-044 Normal Flow bước 2 |
| 3 | Nút "Bắt đầu Nhận hàng" trên card Dispatched | Button | Chỉ hiện khi trạng thái = Dispatched; điều hướng Receive Ingredients Workspace | BR-050 | UC-044 Normal Flow bước 3 |
| 4 | Nút "Xem Lịch sử" | Button (AppBar action) | Điều hướng Receiving History & Reports | — | ScreenFlow: `RecvDash -->|"View History"| RecvHist` |
| 5 | Bộ lọc lịch sử theo ngày/nhà cung cấp | Filter chip row | Tuỳ chọn | — | UC-044 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách lịch giao hàng.
- Empty: Không có lịch giao hàng hôm nay → illustration + gợi ý xem lịch sử.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Nhận hàng" trong Grid Menu Items (`04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Receive Ingredients Workspace, Receiving History & Reports.
**Bố cục**: AppBar (tiêu đề + icon "Lịch sử") + Card list cuộn dọc (pull-to-refresh), mỗi card có nút "Bắt đầu Nhận hàng" khi đủ điều kiện.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar (tiêu đề + icon Lịch sử) + Card list (Nhà cung cấp, Giờ dự kiến, Trạng thái chip, nút Bắt đầu Nhận hàng).
Thành phần bắt buộc: chip trạng thái theo 4 giá trị.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044 - Manage Ingredient Receiving)
- business_rules.md BR-050, BR-078
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md`
