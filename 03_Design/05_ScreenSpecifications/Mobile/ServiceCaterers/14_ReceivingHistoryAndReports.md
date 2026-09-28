# Screen Spec — Receiving History & Reports

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (Report 3 §3.2, sub-flow "search/filter past receiving logs") | **Status**: Draft
**BR liên quan**: BR-076, BR-175, BR-176, BR-178
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lịch sử nhận hàng | Card list | Mỗi card: Ngày, Nhà cung cấp, Số lô, Trạng thái | BR-076 | UC-044 Normal Flow bước 4 |
| 2 | Bộ lọc theo ngày/nhà cung cấp | Filter chip row | Tuỳ chọn | — | UC-044 Normal Flow bước 4 |
| 3 | Xem chi tiết 1 bản ghi lịch sử | Điều hướng chi tiết (read-only) | — | — | Truy xuất nguồn gốc BR-076 |
| 4 | Xuất báo cáo (PDF/Excel) | Button | Chỉ xem báo cáo liên quan hoạt động của chính Service Caterers | BR-175, BR-176, BR-178 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách lịch sử.
- Empty: Chưa có lịch sử nhận hàng nào trong khoảng lọc.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Receiving Management Dashboard (icon "Lịch sử" trên AppBar). Thoát: back về Dashboard.
**Bố cục**: AppBar (tiêu đề + icon xuất báo cáo) + filter chip row + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving History & Reports, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter chip row + Card list lịch sử nhận hàng.
Thành phần bắt buộc: chip trạng thái Received/Rejected.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044 - Manage Ingredient Receiving)
- business_rules.md BR-076, BR-175, BR-176, BR-178
