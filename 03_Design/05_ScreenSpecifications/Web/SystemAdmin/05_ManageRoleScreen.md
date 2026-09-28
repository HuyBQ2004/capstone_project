# Screen Spec — Manage Role Screen

**Actor**: System Admin | **UC liên quan**: UC-022 (Create Role), UC-098 (Update Role), UC-029 (Delete Role) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-008 (bảo vệ role hệ thống mặc định — chỉ role custom được sửa/xoá, và chỉ khi 0 user gán)
**MSG liên quan**: MSG-004, MSG-005, MSG-031 (tạo thành công)

> Màn hình dùng chung 3 UC (Create/Update/Delete Role) qua 1 form + 1 dialog xác nhận xoá — theo đúng sơ đồ điều hướng `01_System_Admin_Screen_Flow.md` (`RL --> MRSCreate/MRSUpdate/MRSDelete`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên Role | Input (text) | Bắt buộc, duy nhất trong hệ thống | BR-008 | Disable khi Update role mặc định (không cho sửa tên role default) |
| 2 | Mô tả | Textarea | Tuỳ chọn | — | |
| 3 | Danh sách Permission (checkbox nhóm theo module) | Checkbox group | Ít nhất phải chọn hợp lệ theo module | — | Lấy danh mục cố định từ `Manage Permission List Screen` (UC-129) |
| 4 | Nút "Lưu" (Create/Update) | Button (primary) | Chỉ enable khi form hợp lệ | BR-008 | Với Update: chỉ hiển thị nếu role là custom |
| 5 | Nút "Xoá Role" (chỉ ở Update) | Button (destructive) | Chỉ hiện với role custom | BR-008 | Mở `AlertDialog` xác nhận — UC-029 |
| 6 | Dialog xác nhận xoá | AlertDialog | Chặn nếu còn user (kể cả đã vô hiệu hoá) đang gán role này | BR-008 | AF1 UC-029: hiển thị số lượng user đang gán, yêu cầu reassign trước |

### Trạng thái màn hình
- Loading: submit form / xoá role.
- Empty: Không áp dụng.
- Error: Validation lỗi tên trùng; nếu cố sửa/xoá role mặc định → chặn với thông báo lỗi rõ ràng (theo Exceptions của UC-098/UC-029); nếu xoá role còn user gán → AF1 hiển thị số lượng, chặn thao tác.
- Success: MSG-031 khi tạo; toast xác nhận chung khi update/xoá thành công (Report 3 không gán MSG code riêng cho Update/Delete Role — **[Giả định — cần team xác nhận]**: dùng MSG-029 "Cập nhật thành công" theo pattern chung ở `error_message.md`).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ `Role List` (nút "Create New Role" hoặc chọn role → "Edit"/"Delete"). Sau khi lưu/xoá quay về `Role List`.
**Bố cục**: Form trung bình (permission checkbox có thể dài) — hiện trong `Sheet` trượt từ phải theo `Layout_Guidelines.md` mục 1.2 thay vì Dialog nhỏ (do danh sách permission nhóm theo module có thể dài hơn 8 field). Checkbox group chia theo module (accordion hoặc section có tiêu đề), nút Lưu/Xoá cố định cuối Sheet.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Role Screen (Create/Update/Delete), nền tảng Web, actor System Admin.
Bố cục: Sheet trượt từ phải, form dọc gồm Tên Role, Mô tả, danh sách Permission theo nhóm module (checkbox), nút Lưu + nút Xoá (chỉ khi Update role custom).
Thành phần bắt buộc: Input Tên Role, Textarea Mô tả, Checkbox group Permission theo module, nút Lưu, nút Xoá + AlertDialog xác nhận xoá.
Áp dụng Design System: nút Xoá dùng variant destructive/token danger.
Trạng thái cần thể hiện: error (tên trùng, role mặc định bị khoá sửa), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1249–1269 (Table 3-5 Create Role), 1449–1467 (Table 3-14 Update Role), 1315–1335 (Table 3-8 Delete Role)
- traceability_matrix.md §1.1 (UC-022/UC-029/UC-098 ↔ BR-008)
