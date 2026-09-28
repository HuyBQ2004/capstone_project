# Screen Spec — Manage Permission List Screen

**Actor**: System Admin | **UC liên quan**: UC-129 (Report 3 §3.2 "View List Permission") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC)
**MSG liên quan**: MSG-004, MSG-005

> Dù tên gọi trong Screen Authorization Matrix có chữ "Manage", Normal Flow thật của UC-129 chỉ mô tả hành vi xem/lọc/export — permission là danh mục cố định trong code (RBAC), không có UC tạo/sửa permission nào tồn tại (xem `01_System_Admin_Screen_Flow.md` ghi chú nghiệp vụ). Màn hình này **chỉ đọc**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (từ khoá) | Input (search) | — | — | |
| 2 | Filter theo Module | Select | — | — | |
| 3 | Bảng danh sách Permission | Table (đọc-only) | Cột: Permission ID, Name, Module/Category, Description | BR-007 | Không có action Create/Edit/Delete nào trên bảng này |
| 4 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: Không xảy ra thực tế (permission là danh mục cố định luôn có dữ liệu).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar (thường là tab con của "Quản lý Người dùng Hệ thống" cạnh Role List) hoặc từ `Manage Role Screen` khi cần tra cứu danh mục quyền trước khi gán. Không có màn hình con.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề, không có nút hành động tạo mới vì đây là màn read-only), Content: search + filter Module, bảng Table.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Permission List Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (không có nút Create), filter bar, bảng dữ liệu read-only.
Thành phần bắt buộc: search, filter Module, bảng (Permission ID/Name/Module/Description), nút Export.
Áp dụng Design System: không dùng badge trạng thái (permission không có trạng thái Active/Inactive).
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG thêm nút Create/Edit/Delete vì đây là màn chỉ đọc.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1469–1489 (Table 3-15 View List Permission Use Cases Specification)
- traceability_matrix.md §1.1 (UC-129 ↔ BR-007)
