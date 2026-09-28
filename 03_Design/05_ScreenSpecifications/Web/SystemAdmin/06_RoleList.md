# Screen Spec — Role List

**Actor**: System Admin | **UC liên quan**: UC-130 (Report 3 §3.2 "View List Role") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC), BR-008 (bảo vệ role mặc định)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên role) | Input (search) | — | — | |
| 2 | Bảng danh sách Role | Table | Cột: Role Name, Description, User Count, Status (Default/Custom) | BR-007, BR-008 | |
| 3 | Badge loại Role | Badge (info=Default, secondary=Custom) | — | BR-008 | |
| 4 | Nút "Create New Role" | Button (primary, góc trên phải) | — | — | Mở `Manage Role Screen` (Create) |
| 5 | Action: Edit | IconButton | Cho phép với mọi role (Update mô tả/permission chỉ áp dụng role custom — role default có thể xem read-only) | BR-008 | Mở `Manage Role Screen` (Update) |
| 6 | Action: Delete | IconButton | Chỉ hiện/enable với role custom | BR-008 | Mở `Manage Role Screen` (Delete confirm) |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: Không xảy ra thực tế (luôn có ít nhất các role mặc định).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ Role Name/Description/User Count/Status.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Người dùng Hệ thống". Điều hướng ra: `Manage Role Screen` (Create/Update/Delete), `Manage Permission List Screen` (xem danh mục quyền).
**Bố cục**: Dashboard Shell — Page Header (tiêu đề + nút Create New Role), Content: search bar + bảng Table, action cột cuối bảng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Role List, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (nút Create New Role góc phải), bảng dữ liệu (Role Name/Description/User Count/Status).
Thành phần bắt buộc: search, bảng, badge Default/Custom, action Edit/Delete theo dòng.
Áp dụng Design System: badge info cho Default, secondary cho Custom.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1491–1511 (Table 3-16 View List Role Use Cases Specification)
- traceability_matrix.md §1.1 (UC-130 ↔ BR-007/BR-008)
