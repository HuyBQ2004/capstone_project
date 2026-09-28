# Screen Spec — User Account List

**Actor**: System Admin | **UC liên quan**: UC-153 (Report 3 §3.2 "View User Account List") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên/email) | Input (search) | — | — | |
| 2 | Filter theo Role | Select | — | BR-007 | |
| 3 | Filter theo School/Company | Select | — | — | |
| 4 | Filter theo Status (Active/Inactive/Pending) | Select | — | — | |
| 5 | Bảng danh sách user | Table | Cột: Tên, Email, Role, School/Company, Trạng thái | — | |
| 6 | Badge trạng thái | Badge (success/warning/danger) | Active=success, Pending=warning, Inactive=danger | — | Theo `Design_System.md` mục 3 |
| 7 | Nút "Create User Account" | Button (primary, góc trên phải) | — | — | Mở `Manage User Screen/Dialog` |
| 8 | Action cột cuối: toggle trạng thái | IconButton | — | BR-007, BR-008 | Mở `Active/Deactivate User Dialog` |
| 9 | Nút "Export Excel/CSV" | Button | — | — | |
| 10 | Phân trang | Pagination | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có tài khoản nào khớp bộ lọc" + gợi ý xoá bớt filter.
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, filter/search hoạt động realtime (debounce).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Người dùng Hệ thống". Điều hướng ra: `Manage User Screen/Dialog` (Create), `Active/Deactivate User Dialog` (action theo dòng).
**Bố cục**: Dashboard Shell — Page Header có tiêu đề + nút "Create User Account" góc phải; Content: thanh filter/search ngang trên bảng, bảng `Table` chiếm toàn bộ chiều rộng, phân trang cuối trang (theo `Layout_Guidelines.md` mục 1.1, `Component_Guidelines.md` mục 1 — Table cho Web).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình User Account List, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (tiêu đề + nút Create User Account góc phải), filter bar, bảng dữ liệu, phân trang.
Thành phần bắt buộc: search, filter Role/School/Status, bảng (Tên/Email/Role/School/Trạng thái), badge trạng thái, action toggle, nút Export.
Áp dụng Design System: badge trạng thái theo success/warning/danger.
Trạng thái cần thể hiện: loading, empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1557–1577 (Table 3-19 View User Account List Use Cases Specification)
- traceability_matrix.md §1.1 (UC-153 ↔ BR-007 ↔ MSG-004/005)
