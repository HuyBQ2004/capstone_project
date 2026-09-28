# Screen Spec — Verify OTP

**Actor**: Mọi actor (cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: — (không có UC riêng; là 1 bước con của UC-032 Forgot Password, cũng dùng lại khi MSG-062 yêu cầu xác minh thiết bị mới) | **Status**: Draft
**BR liên quan**: BR-003 (token/OTP hết hạn 1 giờ — áp dụng cho token nói chung, số phút cụ thể của khung đếm ngược OTP hiển thị trên màn hình là [Giả định — cần team xác nhận, Report 3 không nêu số cụ thể])
**MSG liên quan**: MSG-062 (Warning — khi dùng lại màn này cho xác minh thiết bị mới)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Button (icon/link) | — | — | Quay lại `02_ForgotPassword.md` |
| 2 | OTP Input Fields | Input (6 ô số, 6-digit) | Bắt buộc đủ 6 số; chỉ nhận ký tự số | BR-003 | Report 3 §3.3.2.1.7 |
| 3 | Nút "Verify Code" | Button (primary) | Disabled khi chưa nhập đủ 6 số | BR-003 | Xác nhận đúng mã → chuyển `03_ResetPassword.md` |
| 4 | Countdown Timer | Text/label (đọc, không tương tác) | — | BR-003 | Hiển thị thời gian còn lại để nhập OTP |
| 5 | Nút "Resend OTP" | Button (text/link) | Disabled trong lúc countdown còn chạy | BR-003 | Gửi lại mã mới, reset countdown |

### Trạng thái màn hình
- Loading: nút "Verify Code" chuyển spinner trong lúc hệ thống xác thực mã.
- Empty: không áp dụng.
- Error: mã OTP sai → cảnh báo validation inline dưới ô nhập (Report 3 không có MSG code riêng cho trường hợp này — [Giả định — cần team xác nhận, có thể dùng lại nội dung tương đương MSG-002]); OTP hết hạn → Countdown về 0, disable "Verify Code", bật "Resend OTP".
- Success: chuyển sang `03_ResetPassword.md` (hoặc nếu dùng cho luồng MSG-062, quay lại tiếp tục đăng nhập).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `02_ForgotPassword.md` sau khi "Send OTP" thành công (hoặc từ `01_Login.md` khi MSG-062 kích hoạt); thành công → `03_ResetPassword.md`.
**Bố cục**: `Layout_Guidelines.md` mục 1.3 — card căn giữa `max-width 420px`, 6 ô input OTP xếp ngang căn giữa, Countdown Timer ngay dưới, nút "Verify Code" full-width, link "Resend OTP" cuối card.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `02_ForgotPassword.md` (Mobile); thành công → `03_ResetPassword.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.1.7 — AppBar Back, 6 ô OTP input lớn dễ bấm (≥48dp mỗi ô theo `Accessibility.md`), Countdown Timer dạng text căn giữa, `FilledButton` "Verify Code" full-width, `TextButton` "Resend OTP" bên dưới.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Verify OTP, nền tảng [Web/Mobile], actor Guest & Common.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: nút Back, 6 ô OTP Input, Countdown Timer, nút "Verify Code", nút "Resend OTP".
Áp dụng Design System: primary #0E7490 — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (mã sai/hết hạn), success (chuyển màn Reset Password).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5398–5408 (§3.3.2.1.7 — không có UC riêng, là bước con của UC-032 dòng 1337–1355).
- business_rules.md (BR-003).
- error_message.md (MSG-062).

**Ghi chú**: đây là 1 trong số ít màn hình không có UC-### riêng theo `14_Screen_Authorization_Matrix.md` khối 1 (cột UC/Ghi chú ghi "—"). Nội dung field lấy trực tiếp từ Report 3 §3.3.2.1.7; hành vi chi tiết (thời lượng countdown cụ thể, số lần Resend tối đa) chưa có nguồn UC/BR rõ ràng — đánh dấu **[Giả định — cần team xác nhận]**.
