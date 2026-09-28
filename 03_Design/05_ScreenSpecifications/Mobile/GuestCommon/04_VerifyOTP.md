# Screen Spec — Verify OTP

**Actor**: Mọi actor (cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: — (không có UC riêng; là 1 bước con của UC-032 Forgot Password, cũng dùng lại khi MSG-062 yêu cầu xác minh thiết bị mới) | **Status**: Draft
**BR liên quan**: BR-003 (token/OTP hết hạn 1 giờ — số phút cụ thể của countdown OTP hiển thị trên màn hình [Giả định — cần team xác nhận])
**MSG liên quan**: MSG-062 (Warning — khi dùng lại màn này cho xác minh thiết bị mới)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại `02_ForgotPassword.md` |
| 2 | OTP Input Fields | Input (6 ô số, 6-digit) | Bắt buộc đủ 6 số; chỉ nhận ký tự số | BR-003 | Report 3 §3.3.2.1.7 |
| 3 | Nút "Verify Code" | Button (primary, full-width) | Disabled khi chưa nhập đủ 6 số | BR-003 | Xác nhận đúng mã → chuyển `03_ResetPassword.md` |
| 4 | Countdown Timer | Text (đọc, không tương tác) | — | BR-003 | Hiển thị thời gian còn lại để nhập OTP |
| 5 | Nút "Resend OTP" | TextButton | Disabled trong lúc countdown còn chạy | BR-003 | Gửi lại mã mới, reset countdown |

### Trạng thái màn hình
- Loading: nút "Verify Code" chuyển spinner trong lúc hệ thống xác thực mã.
- Empty: không áp dụng.
- Error: mã OTP sai → cảnh báo validation inline dưới ô nhập [Giả định — cần team xác nhận nội dung MSG cụ thể]; OTP hết hạn → Countdown về 0, disable "Verify Code", bật "Resend OTP".
- Success: chuyển sang `03_ResetPassword.md` (Mobile).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `02_ForgotPassword.md` (Mobile) sau khi "Send OTP" thành công (hoặc từ Login khi MSG-062 kích hoạt); thành công → `03_ResetPassword.md`.
**Bố cục**: Report 3 §3.3.2.1.7 — AppBar Back, 6 ô OTP input lớn (≥48dp mỗi ô theo `Accessibility.md`) căn giữa, Countdown Timer dạng text ngay dưới, `FilledButton` "Verify Code" full-width, `TextButton` "Resend OTP" cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Verify OTP, nền tảng Mobile, actor Guest & Common.
Bố cục: AppBar Back, 6 ô OTP Input lớn, Countdown Timer, FilledButton "Verify Code", TextButton "Resend OTP".
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: loading, error (mã sai/hết hạn), success (chuyển màn Reset Password).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5398–5408 (§3.3.2.1.7 — không có UC riêng, là bước con của UC-032 dòng 1337–1355).
- business_rules.md (BR-003).
- error_message.md (MSG-062).

**Ghi chú**: giống bản Web — chi tiết countdown/số lần Resend tối đa chưa có nguồn UC/BR rõ ràng. **[Giả định — cần team xác nhận]**.
