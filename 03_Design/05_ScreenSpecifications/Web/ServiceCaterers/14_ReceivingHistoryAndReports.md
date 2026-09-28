# Screen Spec — Receiving History & Reports

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (Report 3 §3.2, sub-flow "search/filter past receiving logs") | **Status**: Draft
**BR liên quan**: BR-076, BR-175, BR-176, BR-178
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lịch sử nhận hàng | Table (Web) / Card list (Mobile) | Cột: Ngày, Nhà cung cấp, Số lô, Trạng thái (Received/Rejected) | BR-076 | UC-044 Normal Flow bước 4 |
| 2 | Bộ lọc theo ngày/nhà cung cấp | Filter bar | Tuỳ chọn | — | UC-044 Normal Flow bước 4 |
| 3 | Xem chi tiết 1 bản ghi lịch sử | Điều hướng chi tiết (mở lại Record Ingredient Receiving dạng read-only) | — | — | Truy xuất nguồn gốc BR-076 |
| 4 | Xuất báo cáo (PDF/Excel) | Button | Chỉ xem báo cáo liên quan hoạt động của chính Service Caterers | BR-175, BR-176, BR-178 | Phạm vi báo cáo giới hạn theo vai trò/trường |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách lịch sử.
- Empty: Chưa có lịch sử nhận hàng nào trong khoảng lọc.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem/xuất báo cáo).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Receiving Management Dashboard (nút "Xem Lịch sử"). Thoát: back về Dashboard.
**Bố cục**: Page Header + thanh filter + `Table` dữ liệu (kèm phân trang) + nút "Xuất Báo cáo" góc trên phải, theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving History & Reports, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + thanh filter (ngày, nhà cung cấp) + Table lịch sử nhận hàng + nút Xuất Báo cáo.
Thành phần bắt buộc: badge trạng thái Received/Rejected.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044 - Manage Ingredient Receiving)
- business_rules.md BR-076, BR-175, BR-176, BR-178
