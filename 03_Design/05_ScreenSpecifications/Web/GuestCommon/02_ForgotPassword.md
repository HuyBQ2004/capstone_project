# Screen Spec — Forgot Password

**Actor**: Mọi actor (cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: UC-032 (Report 3 §3.2, dòng 1337–1355) | **Status**: Draft
**BR liên quan**: BR-001 (mã hóa BCrypt), BR-003 (token hết hạn 1 giờ, giới hạn 1 yêu cầu/15 phút/email), BR-006 (chính sách mật khẩu)
**MSG liên quan**: MSG-003 (Error), MSG-030 (Success, hiển thị ở bước cuối luồng sau khi Reset Password)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Link/Nút "Back" | Button (icon/link) | — | — | Quay lại `01_Login.md` |
| 2 | Email Address | Input (email) | Bắt buộc; đúng định dạng email; phải là email đã đăng ký | BR-012 | Report 3 §3.3.2.1.6 |
| 3 | Nút "Send OTP" | Button (primary) | Disabled khi field trống/sai định dạng | BR-003 | Giới hạn tối đa 1 yêu cầu/15 phút/email — nếu vượt giới hạn hiển thị cảnh báo rate-limit |

### Trạng thái màn hình
- Loading: nút "Send OTP" chuyển spinner trong lúc hệ thống sinh token + gửi email.
- Empty: không áp dụng.
- Error: MSG-003 (tài khoản bị vô hiệu hóa, chặn gửi OTP); email không tồn tại/sai định dạng → cảnh báo validation inline dưới field (không dùng MSG riêng — theo UC-032 Exceptions: "system displays appropriate validation warning"); vượt rate-limit 1 yêu cầu/15 phút → cảnh báo tương tự, không có MSG code riêng [Giả định — cần team xác nhận nội dung chính xác].
- Success: chuyển sang `04_VerifyOTP.md` (không có Toast riêng ở bước này — thông báo thành công cuối cùng là MSG-030 sau khi hoàn tất Reset Password).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ link "Forgot Password?" trên `01_Login.md`; thoát: Back → Login, Send OTP thành công → `04_VerifyOTP.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.3 — card căn giữa `max-width 420px`, không Sidebar/Topbar. Icon Back góc trên trái card, tiêu đề "Quên mật khẩu", 1 field Email, nút "Send OTP" full-width bên dưới.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `TextButton` "Forgot Password?" trên Login (Mobile); Back → Login, Send OTP thành công → `04_VerifyOTP.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.1.6 — AppBar có icon Back, 1 `TextField` Email, `FilledButton` "Send OTP" full-width dưới cùng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Forgot Password, nền tảng [Web/Mobile], actor Guest & Common.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: nút Back, Email Address input, nút "Send OTP".
Áp dụng Design System: primary #0E7490 — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (spinner trên nút), error (MSG-003/validation email), success (chuyển màn Verify OTP).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1337–1355 (UC-032 Forgot Password), dòng 5384–5396 (§3.3.2.1.6).
- business_rules.md (BR-001, BR-003, BR-006).
- error_message.md (MSG-003, MSG-030).
