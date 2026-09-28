# Screen Spec — Menu Management

**Actor**: School Administration | **UC liên quan**: UC-128 (Report 3 §3.2 "View List Menu") | **Status**: Draft
**BR liên quan**: — (Report 3 gán "Business Rules: None" cho UC-128)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (theo tuần) | Input (search) | — | — | |
| 2 | Filter theo Status (Draft/Pending Approval/Approved/Published/Rejected) | Select | — | — | |
| 3 | Bảng danh sách Menu | Table | Cột: Tuần áp dụng, Trạng thái, Tên Service Caterers, Tóm tắt dinh dưỡng | — | |
| 4 | Badge trạng thái | Badge (info=Draft, warning=Pending, success=Approved/Published, danger=Rejected) | — | — | |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Menu Detail` |
| 6 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có thực đơn nào khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Thực đơn". Điều hướng ra: `Menu Detail`.
**Bố cục**: Dashboard Shell — filter bar (search + status) trên bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (search theo tuần + status), bảng dữ liệu (Tuần/Trạng thái/Service Caterers/Tóm tắt dinh dưỡng).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, action xem chi tiết, nút Export.
Áp dụng Design System: badge info/warning/success/danger theo trạng thái menu.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2959–2975 (Table 3-69 View List Menu Use Cases Specification)
- traceability_matrix.md §1.6 (UC-128)
