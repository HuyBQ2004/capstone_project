# Screen Spec — Supplier Partnership Requests

**Actor**: School Administration | **UC liên quan**: UC-131 (Report 3 §3.2 "View List Supplier", lọc Pending) | **Status**: Draft
**BR liên quan**: — (Report 3 không gán BR trực tiếp cho UC-131)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên công ty) | Input (search) | — | — | |
| 2 | Filter theo Status | Select | Mặc định lọc "Pending"; hỗ trợ Approved/Rejected/Flagged | — | |
| 3 | Bảng danh sách Supplier | Table | Cột: Tên công ty, Trạng thái duyệt, Hiệu lực chứng nhận, Chỉ số hiệu suất gần nhất | — | |
| 4 | Badge "Performance Warning" | Badge (warning) | Hiện khi supplier bị flag do hiệu suất | AF2 UC-131 | |
| 5 | Badge trạng thái | Badge (warning=Pending, success=Approved, danger=Rejected/Flagged) | — | — | |
| 6 | Action: xem chi tiết | Row click | — | — | Mở `Supplier Detail` |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: AF1 UC-131 — "Không tìm thấy nhà cung cấp khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, badge Performance Warning hiện khi có supplier bị flag (AF2).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Nhà cung cấp". Điều hướng ra: `Supplier Detail`.
**Bố cục**: Dashboard Shell — filter bar (search + status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Supplier Partnership Requests, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (search + status), bảng dữ liệu (Tên công ty/Trạng thái/Hiệu lực chứng nhận/Chỉ số hiệu suất).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, badge Performance Warning, action xem chi tiết.
Áp dụng Design System: badge warning/success/danger.
Trạng thái cần thể hiện: empty, success (kèm badge Performance Warning).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2595–2615 (Table 3-60 View List Supplier Use Cases Specification)
- traceability_matrix.md §1.4 (UC-131)
