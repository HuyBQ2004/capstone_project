# Screen Spec — Login

**Actor**: Mọi actor (SA, ScA, T, RFS, MS, SC, P — cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: UC-034 (Report 3 §3.2, dòng 1359–1377) | **Status**: Draft
**BR liên quan**: BR-002 (khóa tài khoản sau 5 lần sai), BR-004 (timeout session 30 phút), BR-005 (single-device session), BR-009 (chấp nhận điều khoản lần đầu), BR-010 (bắt buộc xác minh email)
**MSG liên quan**: MSG-002 (Error), MSG-003 (Error), MSG-026 (Success), MSG-062 (Warning), MSG-076 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Username/Email | Input (text) | Bắt buộc; không rỗng | BR-012 | Report 3 §3.3.2.1.5 |
| 2 | Password | Input (password, masked) | Bắt buộc; không rỗng | BR-001 | Ẩn ký tự mặc định, có icon toggle hiện/ẩn |
| 3 | Nút "Login" | Button (primary) | Disabled khi 1 trong 2 field trống | BR-002, BR-004, BR-005, BR-009, BR-010 | Submit → xác thực theo Normal Flow UC-034 bước 2–8 |
| 4 | Link "Forgot Password?" | Link | — | BR-003 | Điều hướng sang `02_ForgotPassword.md` |
| 5 | Link "Register Options" | Link | — | — | Điều hướng sang Guest Portal (đăng ký School/Supplier/Service Caterers — ngoài phạm vi 9 màn được giao, xem ghi chú TODO cuối file) |

### Trạng thái màn hình
- Loading: nút "Login" chuyển spinner nội tuyến, disable toàn bộ form trong lúc xác thực (BR-002/004/005/009/010 kiểm tra tuần tự phía server).
- Empty: không áp dụng (form tĩnh, không tải dữ liệu danh sách).
- Error: MSG-002 (sai email/password) hiển thị dưới field Password; MSG-003 (tài khoản bị vô hiệu hóa) hiển thị dạng Alert destructive trên form; MSG-076 (khóa tạm thời sau 5 lần sai, BR-002) hiển thị kèm số phút còn lại; MSG-062 (đăng nhập đáng ngờ từ thiết bị mới) chuyển hướng sang màn hình nhập mã xác minh bổ sung (dùng lại `04_VerifyOTP.md`).
- Success: MSG-026 (Toast), redirect theo BR-009 tới màn hình chấp nhận Điều khoản/Chính sách nếu là lần đăng nhập đầu tiên, ngược lại redirect thẳng dashboard theo role.

---

## Platform Layout — Web
**Entry/Exit points**: Điểm vào đầu tiên của toàn hệ thống Web (không có màn trước đó); thành công → Dashboard theo role (System Admin/School Administration/bản Web của 5 role field); thất bại → ở lại trang.
**Bố cục**: Theo `00_Foundation/Layout_Guidelines.md` mục 1.3 (Guest/Common) — card căn giữa màn hình, `max-width 420px`, không Sidebar/Topbar. Trên card: Logo SFMS ở trên cùng, tiêu đề "Đăng nhập", 2 field (Username/Email, Password) xếp dọc, nút "Login" full-width bên dưới, link "Forgot Password?" căn phải ngay dưới nút, link "Register Options" ở cuối card. Responsive: giữ nguyên layout căn giữa ở mọi breakpoint (`Responsive_Guidelines.md` mục 2).

## Platform Layout — Mobile
**Entry/Exit points**: Từ Guest Portal (nút "Login") → thành công vào Actor Dashboard (Mobile) theo role.
**Bố cục**: Theo `Report 3.md` §3.3.2.1.5 — 1 cột full-screen, không AppBar (màn hình trước khi đăng nhập), Logo trên cùng, 2 `TextField` (Material 3) xếp dọc, `FilledButton` "Login" full-width, `TextButton` "Forgot Password?" và "Register Options" bên dưới. Bàn phím ảo không che field đang nhập (dùng `resizeToAvoidBottomInset: true`).

---

## AI Generation Prompt ⭐ (dùng cho Stitch/Figma AI/ChatGPT — chạy riêng cho từng nền tảng)
```
Tạo wireframe cho màn hình Login, nền tảng [Web/Mobile], actor Guest & Common (dùng chung mọi actor).
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Logo, Username/Email input, Password input (masked), nút Login, link Forgot Password?, link Register Options.
Áp dụng Design System: primary #0E7490, danger #B91C1C cho lỗi — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (spinner trên nút), error (MSG-002/003/076 dạng Alert destructive), success (MSG-026 toast).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1359–1377 (UC-034 Login, Normal/Alternative Flow), dòng 5368–5382 (field-by-field gốc, §3.3.2.1.5).
- business_rules.md mục 1 (BR-002, BR-004, BR-005, BR-009, BR-010).
- error_message.md (MSG-002, MSG-003, MSG-026, MSG-062, MSG-076).
- 03_Design/04_ScreenFlow/13_Guest_Common_Screen_Flow.md.

**Ghi chú TODO**: "Register Options" dẫn tới Guest Portal + 3 màn Register School/Supplier/Service Caterers — các màn này có trong Report 3 §3.3.2.1.1–1.4 nhưng KHÔNG có dòng riêng trong `14_Screen_Authorization_Matrix.md` khối 1 (chỉ 9 màn được giao ở `00_Team_Work_Assignment_Plan.md`), nên không tạo file riêng ở đây — chỉ ghi nhận là điểm điều hướng thoát màn hình. [Giả định — cần team xác nhận có bổ sung 4 màn này vào phạm vi GuestCommon hay không].
