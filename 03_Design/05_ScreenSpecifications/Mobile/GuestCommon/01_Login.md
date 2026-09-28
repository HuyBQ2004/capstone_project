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
- Error: MSG-002 (sai email/password) hiển thị dưới field Password; MSG-003 (tài khoản bị vô hiệu hóa) hiển thị dạng banner destructive; MSG-076 (khóa tạm thời sau 5 lần sai, BR-002) hiển thị kèm số phút còn lại; MSG-062 (đăng nhập đáng ngờ từ thiết bị mới) chuyển hướng sang màn hình nhập mã xác minh bổ sung (dùng lại `04_VerifyOTP.md`).
- Success: MSG-026 (SnackBar), redirect theo BR-009 tới màn hình chấp nhận Điều khoản/Chính sách nếu là lần đăng nhập đầu tiên, ngược lại redirect thẳng Actor Dashboard (Mobile) theo role.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Guest Portal (nút "Login") → thành công vào Actor Dashboard (Mobile) theo role (Medical Staff, Service Caterers, Parent, Raw Food Supplier, Teacher).
**Bố cục**: Theo `Report 3.md` §3.3.2.1.5 — 1 cột full-screen, không AppBar (màn hình trước khi đăng nhập). Logo SFMS trên cùng (placeholder image frame theo mô tả gốc), tiêu đề "Đăng nhập", 2 `TextField` (Material 3) xếp dọc (Username/Email, Password masked với icon toggle), `FilledButton` "Login" full-width ngay dưới, `TextButton` "Forgot Password?" căn giữa, `TextButton` "Register Options" ở cuối. Bàn phím ảo không che field đang nhập (`resizeToAvoidBottomInset: true`). Touch target tối thiểu 48×48dp theo `Accessibility.md` mục 2.

---

## AI Generation Prompt ⭐ (dùng cho Stitch/Figma AI/ChatGPT — chạy riêng cho từng nền tảng)
```
Tạo wireframe cho màn hình Login, nền tảng Mobile, actor Guest & Common (dùng chung mọi actor).
Bố cục: 1 cột full-screen, Logo trên cùng, TextField Username/Email, TextField Password (masked),
FilledButton "Login" full-width, TextButton "Forgot Password?", TextButton "Register Options".
Áp dụng Design System: seed color #0E7490 (Material 3 ColorScheme.fromSeed), danger #B91C1C cho lỗi — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (spinner trên nút), error (MSG-002/003/076 dạng banner đỏ), success (MSG-026 SnackBar xanh).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1359–1377 (UC-034 Login, Normal/Alternative Flow), dòng 5368–5382 (field-by-field gốc, §3.3.2.1.5).
- business_rules.md mục 1 (BR-002, BR-004, BR-005, BR-009, BR-010).
- error_message.md (MSG-002, MSG-003, MSG-026, MSG-062, MSG-076).
- 03_Design/04_ScreenFlow/13_Guest_Common_Screen_Flow.md.

**Ghi chú TODO**: giống bản Web — "Register Options" dẫn tới 4 màn Guest Portal/Register School/Supplier/Service Caterers không có dòng riêng trong `14_Screen_Authorization_Matrix.md` khối 1, không tạo file riêng ở đây. [Giả định — cần team xác nhận].
