# Screen Spec — Active/Deactivate User Dialog

**Actor**: System Admin | **UC liên quan**: UC-003 (Report 3 §3.2 "Active/Deactivate User") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC), BR-008 (bảo vệ tài khoản System Admin mặc định duy nhất)
**MSG liên quan**: MSG-004, MSG-005, MSG-029 (cập nhật thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin tài khoản (tên, email, role) | Text hiển thị (read-only) | — | — | Hiển thị để xác nhận đúng tài khoản trước khi toggle |
| 2 | Trạng thái hiện tại → mới | Badge trước/sau | — | — | VD Active → Inactive |
| 3 | Cảnh báo hệ quả (nếu Deactivate) | Alert (warning) | Hiển thị: hủy toàn bộ session, gửi email thông báo | BR-007 | Theo Normal Flow bước 6 |
| 4 | Nút "Xác nhận" | Button (destructive nếu Deactivate) | Bị chặn (disable + thông báo) nếu target là tài khoản System Admin mặc định duy nhất | BR-008 | |
| 5 | Nút "Huỷ" | Button (ghost) | — | — | |

### Trạng thái màn hình
- Loading: Button "Xác nhận" chuyển loading khi xử lý.
- Empty: Không áp dụng.
- Error: Chặn hành động + validation error nếu target là tài khoản System Admin mặc định (Exceptions UC-003); MSG-004/MSG-005.
- Success: MSG-029, đóng dialog, cập nhật lại badge trạng thái trên `User Account List`; nếu deactivate → hệ thống tự hủy toàn bộ session của user đó (BR-007) và gửi email.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ action toggle trạng thái ở dòng tương ứng trên `User Account List`. Đóng lại trả về `User Account List`.
**Bố cục**: `AlertDialog` (hành động không thể hoàn tác ngay lập tức về mặt session) theo `Component_Guidelines.md` mục 1 — không điều hướng sang trang riêng, giữ ngữ cảnh danh sách phía sau.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Active/Deactivate User Dialog, nền tảng Web, actor System Admin.
Bố cục: AlertDialog giữa màn hình, hiển thị thông tin tài khoản + cảnh báo hệ quả + 2 nút Xác nhận/Huỷ.
Thành phần bắt buộc: tên/email/role tài khoản, badge trạng thái trước/sau, alert cảnh báo (hủy session + gửi email), nút Xác nhận (destructive khi deactivate), nút Huỷ.
Áp dụng Design System: nút destructive dùng token danger, alert dùng token warning.
Trạng thái cần thể hiện: error (chặn deactivate tài khoản System Admin mặc định), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1209–1224 (Table Active/Deactivate User Use Cases Specification)
- traceability_matrix.md §1.1 (UC-003 ↔ BR-007/BR-008)
