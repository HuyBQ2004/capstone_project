# Screen Spec — Menu Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-048 - Manage Menus (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-067
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách thực đơn theo tuần | Card list (1 bản ghi = 1 Card) | Mỗi card: Tuần áp dụng, Trạng thái, Ngày cập nhật cuối | — | UC-048 Normal Flow bước 2 |
| 2 | Trạng thái thực đơn | Chip (Material 3) | Draft / Pending Approval / Approved / Published / Rejected | BR-067, BR-068 | Màu theo `Design_System.md` mục 3 |
| 3 | Bộ lọc theo khoảng ngày | DatePicker (M3) | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 4 | Bộ lọc theo trạng thái | DropdownMenu | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 5 | FAB "Tạo Thực đơn Mới" | FloatingActionButton | Điều hướng tới Create Menu Workspace (UC-020) | — | Góc dưới phải theo `Layout_Guidelines.md` mục 2.1 |
| 6 | Hành động "Edit" trên card Draft/Rejected | Button | Chỉ hiện khi status = Draft hoặc Rejected; điều hướng Update Menu Screen (UC-096) | BR-067 | — |
| 7 | Hành động "Submit" trên card Draft | Button | Chỉ hiện khi status = Draft; điều hướng Submit Menu For Approval Trigger (UC-087) | BR-067 | — |
| 8 | Hành động "Delete" trên card Draft/Rejected | Icon Button (danger) + `AlertDialog` xác nhận | Chỉ hiện khi status = Draft hoặc Rejected | BR-067 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho card list.
- Empty: Chưa có thực đơn nào → illustration + FAB nổi bật.
- Error: `SnackBar`/`AlertDialog` màu `danger` khi tải thất bại (MSG-004/MSG-005).
- Success: `SnackBar` màu `success` sau khi Delete thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Thực đơn" trong Grid Menu Items (`04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Create Menu Workspace, Update Menu Screen.
**Bố cục**: AppBar (tiêu đề "Quản lý Thực đơn") + Content 1 cột: thanh filter thu gọn trên cùng + Card list cuộn dọc (pull-to-refresh bắt buộc) + FAB "Tạo Thực đơn Mới" góc dưới phải, theo `00_Foundation/Layout_Guidelines.md` mục 2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter thu gọn + Card list (1 bản ghi/card: Tuần, Trạng thái, Ngày cập nhật, action Edit/Submit/Delete) + FAB Tạo Thực đơn Mới.
Thành phần bắt buộc: Chip trạng thái theo màu ngữ nghĩa, pull-to-refresh.
Áp dụng Design System: xem 00_Foundation/Design_System.md, Component_Guidelines.md (Card, Chip, FAB).
Trạng thái cần thể hiện: loading (shimmer), empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2867–2886 (UC-048 - Manage Menus)
- business_rules.md BR-067, BR-068
