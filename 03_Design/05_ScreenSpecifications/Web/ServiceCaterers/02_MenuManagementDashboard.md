# Screen Spec — Menu Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-048 - Manage Menus (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-067
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách thực đơn theo tuần | Table (Web) / Card list (Mobile) | Mỗi dòng: Tuần áp dụng, Trạng thái, Ngày cập nhật cuối | — | UC-048 Normal Flow bước 2 |
| 2 | Trạng thái thực đơn | Badge | Draft / Pending Approval / Approved / Published / Rejected | BR-067, BR-068 | Màu theo `Design_System.md` mục 3 (`info`=Draft, `warning`=Pending, `success`=Approved/Published, `danger`=Rejected) |
| 3 | Bộ lọc theo khoảng ngày | DateRangePicker | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 4 | Bộ lọc theo trạng thái | Select | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 5 | Nút "Tạo Thực đơn Mới" | Button | Điều hướng tới Create Menu Workspace (UC-020) | — | — |
| 6 | Hành động "Edit" trên dòng Draft/Rejected | Button | Chỉ hiện khi status = Draft hoặc Rejected (BR-067); điều hướng Update Menu Screen (UC-096) | BR-067 | UC-048 Normal Flow bước 4 |
| 7 | Hành động "Submit" trên dòng Draft | Button | Chỉ hiện khi status = Draft; điều hướng Submit Menu For Approval Trigger (UC-087) | BR-067 | UC-048 Normal Flow bước 4 |
| 8 | Hành động "Delete" trên dòng Draft/Rejected | Button (destructive) + `AlertDialog` xác nhận | Chỉ hiện khi status = Draft hoặc Rejected; chặn nếu Approved/Published | BR-067 | UC-048 Exceptions: chặn thao tác + hiển thị cảnh báo nếu cố xoá/sửa thực đơn đã duyệt |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách khi tải.
- Empty: Chưa có thực đơn nào → hiển thị illustration + nút "Tạo Thực đơn Mới".
- Error: `Alert variant="destructive"` khi tải danh sách thất bại (MSG-004/MSG-005 nếu do session/quyền), kèm nút "Thử lại".
- Success: Toast xác nhận sau khi Delete thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Thực đơn" trong Sidebar (`09_Service_Caterers_Screen_Flow_Web.md`). Điều hướng tiếp: Create Menu Workspace, Update Menu, Submit Menu For Approval.
**Bố cục**: Page Header (tiêu đề "Quản lý Thực đơn" + nút "Tạo Thực đơn Mới" góc phải) + thanh filter (ngày, trạng thái) phía trên `Table` + `Table` dữ liệu (cột: Tuần, Trạng thái, Ngày cập nhật, Hành động) kèm phân trang dưới cùng, theo `00_Foundation/Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Management Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header (tiêu đề + nút Tạo Thực đơn Mới) + thanh filter (ngày, trạng thái) + Table dữ liệu (Tuần, Trạng thái, Ngày cập nhật, Hành động Edit/Submit/Delete) + phân trang.
Thành phần bắt buộc: badge trạng thái theo màu ngữ nghĩa, action buttons theo điều kiện trạng thái (BR-067).
Áp dụng Design System: xem 00_Foundation/Design_System.md, Component_Guidelines.md (Table, Badge, AlertDialog).
Trạng thái cần thể hiện: loading, empty, error, success (toast xoá thành công).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2867–2886 (UC-048 - Manage Menus)
- business_rules.md BR-067, BR-068
- traceability_matrix.md
