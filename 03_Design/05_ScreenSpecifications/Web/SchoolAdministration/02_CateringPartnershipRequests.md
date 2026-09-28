# Screen Spec — Catering Partnership Requests

**Actor**: School Administration | **UC liên quan**: UC-127 (Report 3 §3.2 "View List Service Caterers", lọc Pending) | **Status**: Draft
**BR liên quan**: — (Report 3 không gán BR trực tiếp cho UC-127)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên công ty) | Input (search) | — | — | |
| 2 | Filter theo Status | Select | Mặc định lọc "Pending" theo tên màn hình, nhưng cho phép xem cả Approved/Rejected/Suspended | — | |
| 3 | Bảng danh sách Service Caterers | Table | Cột: Tên công ty, Trạng thái hợp đồng, Ngày đăng ký | — | |
| 4 | Badge trạng thái | Badge (warning=Pending, success=Approved, danger=Rejected/Suspended) | — | — | |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Service Caterers Detail` |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: AF1 UC-127 — "Không tìm thấy công ty catering khớp bộ lọc", gợi ý xoá bớt filter.
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Nhà cung cấp". Điều hướng ra: `Service Caterers Detail`.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề), filter bar (search + status) trên bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Catering Partnership Requests, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (search + status), bảng dữ liệu (Tên công ty/Trạng thái/Ngày đăng ký).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, action xem chi tiết.
Áp dụng Design System: badge warning/success/danger theo trạng thái.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2577–2593 (Table 3-59 View List Service Caterers Use Cases Specification)
- traceability_matrix.md §1.4 (UC-127)
