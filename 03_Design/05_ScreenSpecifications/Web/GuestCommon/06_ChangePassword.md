# Screen Spec — Change Password

**Actor**: System Admin, School Administration, Medical Staff, Service Caterers (cột `x` — Teacher/Raw Food Supplier/Parent KHÔNG có, chỉ đổi mật khẩu qua luồng Forgot Password/OTP/Reset, theo `14_Screen_Authorization_Matrix.md` khối 1 + ghi chú tại `13_Guest_Common_Screen_Flow.md`) | **UC liên quan**: UC-011 (Report 3 §3.2, dòng 1227–1245) | **Status**: Draft
**BR liên quan**: BR-001 (mã hóa BCrypt), BR-006 (chính sách mật khẩu, không trùng 3 mật khẩu gần nhất), BR-007 (RBAC — xác định actor nào có màn này)
**MSG liên quan**: MSG-002 (Error), MSG-004 (Error), MSG-005 (Error), MSG-030 (Success), MSG-063 (Warning)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Button (icon/link) | — | — | Quay lại `05_ProfileManagement.md` |
| 2 | Current Password | Input (password, masked) | Bắt buộc | BR-001 | Report 3 §3.3.2.1.11 |
| 3 | New Password | Input (password, masked) | Bắt buộc; đáp ứng chính sách mật khẩu; không trùng 3 mật khẩu gần nhất | BR-006 | |
| 4 | Confirm New Password | Input (password, masked) | Bắt buộc; phải khớp New Password | BR-006 | |
| 5 | Nút "Save Password"/"Change Password" | Button (primary) | Disabled khi thiếu field | BR-001, BR-006, BR-007 | Submit → xác thực Current Password đúng trước khi đổi |

### Trạng thái màn hình
- Loading: nút "Save Password" chuyển spinner trong lúc xác thực mật khẩu hiện tại + hash mật khẩu mới.
- Empty: không áp dụng.
- Error: MSG-002 (Current Password sai — UC-011 Exceptions dùng lại nội dung "Invalid credentials"), MSG-004 (session hết hạn), MSG-005 (không có quyền), MSG-063 (mật khẩu mới không đạt policy), Confirm không khớp New Password → cảnh báo inline.
- Success: MSG-030 (Toast), hệ thống chấm dứt toàn bộ session khác (theo UC-011 Postcondition), quay lại `05_ProfileManagement.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "Change Password" trong dropdown Profile (Topbar); thoát: Back/Cancel → Profile, thành công → Profile.
**Bố cục**: `Component_Guidelines.md` mục "Form ngắn nổi" — hiển thị dạng `Dialog`/modal nổi trên Dashboard hiện tại (không phải trang riêng), 3 field password xếp dọc, nút "Save Password" full-width trong modal.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu "Change Password" trên trang Profile Management (Mobile); thành công → quay lại Profile Management.
**Bố cục**: Report 3 §3.3.2.1.11 — trang riêng full-screen: AppBar Back, 3 `TextField` password (masked) xếp dọc, `FilledButton` "Save Password" full-width dưới cùng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Change Password, nền tảng [Web/Mobile], actor SA/ScA/MS/SC.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Current Password, New Password, Confirm New Password (đều masked), nút Save Password.
Áp dụng Design System: primary #0E7490, danger cho lỗi mật khẩu sai.
Trạng thái cần thể hiện: loading, error (MSG-002/063), success (MSG-030).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1227–1245 (UC-011), dòng 5450–5461 (§3.3.2.1.11).
- business_rules.md (BR-001, BR-006, BR-007).
- error_message.md (MSG-002, MSG-004, MSG-005, MSG-030, MSG-063).
