# Screen Spec — Staff Management

**Actor**: School Administration | **UC liên quan**: UC-144 (Report 3 §3.2 "View Staff Account List") | **Status**: Draft
**BR liên quan**: BR-008 (bảo vệ role mặc định — gián tiếp qua danh sách role staff)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên/email) | Input (search) | — | — | |
| 2 | Filter theo Role | Select | Teacher/Medical Staff/Catering staff | BR-013 | |
| 3 | Filter theo Status | Select | Active/Inactive/Pending | — | |
| 4 | Bảng danh sách nhân viên | Table | Cột: Tên, Email, Role, SĐT, Trạng thái | — | |
| 5 | Badge trạng thái | Badge (success/warning/danger) | — | — | |
| 6 | Nút "Upload Staff List" | Button (primary, góc trên phải) | — | — | Mở `Upload Staff List` |
| 7 | Action: toggle trạng thái (Active/Inactive Staff) | IconButton | — | BR-007, BR-008 | UC-002 Active/Deactivate Staff |
| 8 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có nhân viên nào — upload danh sách nhân viên đầu tiên".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Nhân sự". Điều hướng ra: `Upload Staff List`, `Invitation Status`.
**Bố cục**: Dashboard Shell — Page Header (nút Upload Staff List), filter bar (search + role + status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Staff Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Upload Staff List), filter bar (search + role + status), bảng dữ liệu.
Thành phần bắt buộc: search, filter role/status, bảng (Tên/Email/Role/SĐT/Trạng thái), badge trạng thái, action toggle, nút Export.
Áp dụng Design System: badge success/warning/danger.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1535–1555 (Table 3-18 View Staff Account List Use Cases Specification)
- traceability_matrix.md §1.1 (UC-144 ↔ BR-013)
