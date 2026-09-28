# Screen Spec — Reset Password

**Actor**: Mọi actor (cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: UC-032 (Report 3 §3.2, dòng 1337–1355, bước 5–6 của Normal Flow) | **Status**: Draft
**BR liên quan**: BR-001 (mã hóa BCrypt trước khi lưu), BR-003 (token hết hạn 1 giờ), BR-006 (chính sách mật khẩu + không trùng 3 mật khẩu gần nhất)
**MSG liên quan**: MSG-030 (Success), MSG-063 (Warning — mật khẩu yếu)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | New Password | Input (password, masked) | Bắt buộc; đáp ứng chính sách độ mạnh mật khẩu; không trùng 3 mật khẩu gần nhất | BR-006 | Report 3 §3.3.2.1.8 |
| 2 | Confirm New Password | Input (password, masked) | Bắt buộc; phải khớp với New Password | BR-006 | |
| 3 | Nút "Update Password" | Button (primary) | Disabled khi 2 field trống hoặc không khớp | BR-001, BR-003, BR-006 | Submit → xác thực token còn hiệu lực (AF1 UC-032: token hết hạn) trước khi lưu |

### Trạng thái màn hình
- Loading: nút "Update Password" chuyển spinner trong lúc hệ thống xác thực token + hash mật khẩu mới (BR-001).
- Empty: không áp dụng.
- Error: AF1 "Token Expired" (token hết hạn theo BR-003) → hiển thị Alert destructive với hành động "Yêu cầu gửi lại link mới" (quay lại `02_ForgotPassword.md`); mật khẩu không khớp policy (BR-006, trùng 3 mật khẩu gần nhất) → MSG-063 hoặc cảnh báo validation inline; 2 field không khớp nhau → cảnh báo inline dưới Confirm New Password.
- Success: MSG-030 (Toast), hệ thống vô hiệu hóa toàn bộ session cũ, chuyển hướng về `01_Login.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ link trong email reset password (sau khi qua `04_VerifyOTP.md` theo luồng thực tế — thứ tự hiển thị ở đây theo đúng thứ tự liệt kê trong Screen Authorization Matrix, không phải thứ tự điều hướng); thành công → `01_Login.md`.
**Bố cục**: `Layout_Guidelines.md` mục 1.3 — card căn giữa `max-width 420px`, tiêu đề "Đặt lại mật khẩu", 2 field xếp dọc, nút "Update Password" full-width.

## Platform Layout — Mobile
**Entry/Exit points**: Vào sau `04_VerifyOTP.md` (Mobile) khi OTP xác thực thành công; thành công → `01_Login.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.1.8 — 1 cột, 2 `TextField` password (masked, có toggle hiện/ẩn), `FilledButton` "Update Password" full-width dưới cùng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Reset Password, nền tảng [Web/Mobile], actor Guest & Common.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: New Password input (masked), Confirm New Password input (masked), nút "Update Password".
Áp dụng Design System: primary #0E7490, danger #B91C1C cho lỗi token hết hạn — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (token hết hạn / mật khẩu không đạt policy), success (MSG-030).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1337–1355 (UC-032, AF1 Token Expired), dòng 5410–5419 (§3.3.2.1.8).
- business_rules.md (BR-001, BR-003, BR-006).
- error_message.md (MSG-030, MSG-063).
