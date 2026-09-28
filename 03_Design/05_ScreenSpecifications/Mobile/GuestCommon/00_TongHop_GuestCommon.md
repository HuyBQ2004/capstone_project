# Tổng hợp Screen Specifications — GuestCommon (GuestCommon)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


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

# Screen Spec — Forgot Password

**Actor**: Mọi actor (cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: UC-032 (Report 3 §3.2, dòng 1337–1355) | **Status**: Draft
**BR liên quan**: BR-001 (mã hóa BCrypt), BR-003 (token hết hạn 1 giờ, giới hạn 1 yêu cầu/15 phút/email), BR-006 (chính sách mật khẩu)
**MSG liên quan**: MSG-003 (Error), MSG-030 (Success, hiển thị ở bước cuối luồng sau khi Reset Password)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại `01_Login.md` |
| 2 | Email Address | Input (email) | Bắt buộc; đúng định dạng email; phải là email đã đăng ký | BR-012 | Report 3 §3.3.2.1.6 |
| 3 | Nút "Send OTP" | Button (primary, full-width) | Disabled khi field trống/sai định dạng | BR-003 | Giới hạn tối đa 1 yêu cầu/15 phút/email |

### Trạng thái màn hình
- Loading: nút "Send OTP" chuyển spinner trong lúc hệ thống sinh token + gửi email.
- Empty: không áp dụng.
- Error: MSG-003 (tài khoản bị vô hiệu hóa, chặn gửi OTP); email không tồn tại/sai định dạng → cảnh báo validation inline dưới field; vượt rate-limit 1 yêu cầu/15 phút → cảnh báo tương tự [Giả định — cần team xác nhận nội dung chính xác].
- Success: chuyển sang `04_VerifyOTP.md` (Mobile).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `TextButton` "Forgot Password?" trên Login (Mobile); Back → Login, Send OTP thành công → `04_VerifyOTP.md`.
**Bố cục**: Report 3 §3.3.2.1.6 — AppBar có icon Back + tiêu đề "Quên mật khẩu", 1 `TextField` Email, `FilledButton` "Send OTP" full-width dưới cùng, đặt cao trong màn hình để dễ với tay (theo `Layout_Guidelines.md` mục 2.2).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Forgot Password, nền tảng Mobile, actor Guest & Common.
Bố cục: AppBar (Back + tiêu đề), TextField Email Address, FilledButton "Send OTP" full-width.
Áp dụng Design System: seed color #0E7490 — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (spinner trên nút), error (MSG-003/validation email), success (chuyển màn Verify OTP).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1337–1355 (UC-032 Forgot Password), dòng 5384–5396 (§3.3.2.1.6).
- business_rules.md (BR-001, BR-003, BR-006).
- error_message.md (MSG-003, MSG-030).

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
| 3 | Nút "Update Password" | Button (primary, full-width) | Disabled khi 2 field trống hoặc không khớp | BR-001, BR-003, BR-006 | Submit → xác thực token còn hiệu lực trước khi lưu |

### Trạng thái màn hình
- Loading: nút "Update Password" chuyển spinner trong lúc hệ thống xác thực token + hash mật khẩu mới (BR-001).
- Empty: không áp dụng.
- Error: AF1 "Token Expired" (BR-003) → banner đỏ với hành động "Yêu cầu gửi lại link mới" (quay lại `02_ForgotPassword.md`); mật khẩu không khớp policy (BR-006) → MSG-063; 2 field không khớp → cảnh báo inline.
- Success: MSG-030 (SnackBar), vô hiệu hóa toàn bộ session cũ, chuyển hướng về `01_Login.md` (Mobile).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào sau `04_VerifyOTP.md` (Mobile) khi OTP xác thực thành công; thành công → `01_Login.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.1.8 — 1 cột full-screen, tiêu đề "Đặt lại mật khẩu", 2 `TextField` password (masked, toggle hiện/ẩn), `FilledButton` "Update Password" full-width dưới cùng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Reset Password, nền tảng Mobile, actor Guest & Common.
Bố cục: 1 cột full-screen, TextField New Password (masked), TextField Confirm New Password (masked), FilledButton "Update Password" full-width.
Áp dụng Design System: seed color #0E7490, danger #B91C1C cho lỗi token hết hạn.
Trạng thái cần thể hiện: loading, error (token hết hạn/mật khẩu không đạt policy), success (MSG-030).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1337–1355 (UC-032, AF1 Token Expired), dòng 5410–5419 (§3.3.2.1.8).
- business_rules.md (BR-001, BR-003, BR-006).
- error_message.md (MSG-030, MSG-063).

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

# Screen Spec — Profile Management

**Actor**: System Admin, School Administration, Medical Staff, Service Caterers (cột `x` — Teacher/Raw Food Supplier/Parent KHÔNG có cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Profile Management") | **UC liên quan**: UC-059 (Profile Manage, hub — Report 3 §3.2, dòng 1403–1421), UC-138 (View Profile, dòng 1513–1531), UC-097 (Update Profile, dòng 1425–1443) | **Status**: Draft
**BR liên quan**: BR-012 (định dạng email/phone hợp lệ), BR-013 (giới hạn upload ảnh 5MB, JPG/JPEG/PNG)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-008 (Error), MSG-009 (Error), MSG-029 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Avatar | Ảnh (hiển thị) | — | BR-013 | Report 3 §3.3.2.1.9 |
| 2 | Name | Text (đọc) | — | — | |
| 3 | Role & School/Company | Text (đọc) | — | — | |
| 4 | Menu "Edit Profile" | ListTile | — | UC-097 | Chuyển sang trang Edit Profile riêng (§3.3.2.1.10) |
| 5 | Menu "Change Password" | ListTile | — | — | Điều hướng sang `06_ChangePassword.md` |
| 6 | Menu "Notification Settings" | ListTile | — | — | Điều hướng sang `09_NotificationSettings.md` |
| 7 | Menu "AI Chatbot" | ListTile | — | — | Ngoài phạm vi 9 màn được giao (không có dòng riêng trong Matrix khối 1) — chỉ ghi nhận điểm điều hướng, không tách file |
| 8 | Nút "Logout" | Button | Hiện dialog xác nhận trước khi đăng xuất | BR-004 | UC-035 Logout |

### Danh sách Field trang Edit Profile (UC-097, §3.3.2.1.10)
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| E1 | Avatar Upload | image_picker | JPG/JPEG/PNG, tối đa 5MB | BR-013 | MSG-008/MSG-009 |
| E2 | Full Name | TextField | Bắt buộc | — | |
| E3 | Phone Number | TextField | Định dạng +84 hoặc 0xx hợp lệ | BR-012 | |
| E4 | Email Address | TextField (read-only) | Không sửa được | — | |
| E5 | Nút "Save" | FilledButton | Disabled khi chưa thay đổi gì | BR-012, BR-013 | Submit → MSG-029 |
| E6 | Nút "Cancel" | TextButton | — | — | Hủy chỉnh sửa, quay lại Profile Management |

### Trạng thái màn hình
- Loading: `Shimmer`/`CircularProgressIndicator` khi tải dữ liệu profile; spinner trên "Save" khi đang lưu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-008, MSG-009; số điện thoại sai định dạng → cảnh báo inline (BR-012).
- Success: MSG-029 (SnackBar) sau khi lưu Edit Profile.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Bottom Tab Bar mục "Profile" (theo cấu trúc tab riêng của từng actor — xem `Navigation_Guidelines.md`).
**Bố cục**: Report 3 §3.3.2.1.9 — trang riêng full-screen: Avatar lớn trên cùng, Name, Role & School/Company, danh sách Menu dạng `ListTile`, nút "Logout" cuối trang. Chọn "Edit Profile" → điều hướng sang trang riêng (§3.3.2.1.10) với form E1–E6, nút "Cancel"/"Save" cố định trên AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Profile Management, nền tảng Mobile, actor dùng chung (SA/ScA/MS/SC).
Bố cục: trang riêng full-screen, Avatar, Name, Role & School/Company, ListTile menu (Edit Profile/Change Password/Notification Settings/AI Chatbot), nút Logout;
trang Edit Profile riêng: Avatar Upload, Full Name, Phone Number, Email (read-only), nút Save/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: loading, error (MSG-008/009/validation phone), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1403–1421 (UC-059), dòng 1513–1531 (UC-138), dòng 1425–1443 (UC-097), dòng 1381–1399 (UC-035), dòng 5420–5449 (§3.3.2.1.9–10).
- business_rules.md (BR-012, BR-013).
- error_message.md (MSG-004, MSG-005, MSG-008, MSG-009, MSG-029).

**Ghi chú**: giống bản Web — "Edit Profile" không có dòng riêng trong Matrix khối 1, gộp vào file này như trang con. "AI Chatbot"/"AI Chat History" cũng ngoài phạm vi 9 màn (Matrix không gán cột `x` rõ ràng cho actor cụ thể) — chỉ ghi nhận là điểm điều hướng, không tạo file riêng.

# Screen Spec — Change Password

**Actor**: System Admin, School Administration, Medical Staff, Service Caterers (cột `x` — Teacher/Raw Food Supplier/Parent KHÔNG có, theo `14_Screen_Authorization_Matrix.md` khối 1 + `13_Guest_Common_Screen_Flow.md`) | **UC liên quan**: UC-011 (Report 3 §3.2, dòng 1227–1245) | **Status**: Draft
**BR liên quan**: BR-001 (mã hóa BCrypt), BR-006 (chính sách mật khẩu, không trùng 3 mật khẩu gần nhất), BR-007 (RBAC)
**MSG liên quan**: MSG-002 (Error), MSG-004 (Error), MSG-005 (Error), MSG-030 (Success), MSG-063 (Warning)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại `05_ProfileManagement.md` |
| 2 | Current Password | TextField (masked) | Bắt buộc | BR-001 | Report 3 §3.3.2.1.11 |
| 3 | New Password | TextField (masked) | Bắt buộc; đáp ứng chính sách mật khẩu; không trùng 3 mật khẩu gần nhất | BR-006 | |
| 4 | Confirm New Password | TextField (masked) | Bắt buộc; phải khớp New Password | BR-006 | |
| 5 | Nút "Save Password" | FilledButton (full-width) | Disabled khi thiếu field | BR-001, BR-006, BR-007 | Submit → xác thực Current Password đúng trước khi đổi |

### Trạng thái màn hình
- Loading: nút "Save Password" chuyển spinner trong lúc xác thực + hash mật khẩu mới.
- Empty: không áp dụng.
- Error: MSG-002 (Current Password sai), MSG-004, MSG-005, MSG-063 (mật khẩu mới không đạt policy), Confirm không khớp → cảnh báo inline.
- Success: MSG-030 (SnackBar), hệ thống chấm dứt toàn bộ session khác, quay lại `05_ProfileManagement.md` (Mobile).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu "Change Password" trên trang Profile Management (Mobile); thành công → quay lại Profile Management.
**Bố cục**: Report 3 §3.3.2.1.11 — trang riêng full-screen: AppBar Back, 3 `TextField` password (masked) xếp dọc, `FilledButton` "Save Password" full-width dưới cùng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Change Password, nền tảng Mobile, actor SA/ScA/MS/SC.
Bố cục: trang riêng full-screen, AppBar Back, TextField Current/New/Confirm Password (masked), FilledButton "Save Password".
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: loading, error (MSG-002/063), success (MSG-030).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1227–1245 (UC-011), dòng 5450–5461 (§3.3.2.1.11).
- business_rules.md (BR-001, BR-006, BR-007).
- error_message.md (MSG-002, MSG-004, MSG-005, MSG-030, MSG-063).

# Screen Spec — Notification Center/List

**Actor**: School Administration, Teacher, Service Caterers, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Center/List") | **UC liên quan**: — (không có UC riêng; nội dung suy ra từ "Notification History" Report 3 §3.3.2.1.13 + BR-159) | **Status**: Draft
**BR liên quan**: BR-138 (cảnh báo quan trọng phát ngay), BR-158 (Quiet Hours, không áp dụng cho cảnh báo khẩn cấp), BR-159 (retry + fallback email)
**MSG liên quan**: MSG-068 (Warning)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại Dashboard |
| 2 | Notification List | ListView/Card | Sắp xếp mới nhất trên đầu | BR-158 | Report 3 §3.3.2.1.13 |
| 3 | Warning Badge (mỗi item) | Chip màu (M3) | Đỏ = cảnh báo dị ứng/an toàn thực phẩm, Vàng = tin tức, Xám = hệ thống | — (quy ước màu `Design_System.md` mục 3, xem ghi chú) | Kèm icon/label, không chỉ dùng màu |
| 4 | Sent Time (mỗi item) | Text | — | — | |
| 5 | Tap vào 1 item | Interaction | — | — | Điều hướng sang `08_NotificationDetail.md` (Teacher/Parent) |

### Trạng thái màn hình
- Loading: `Shimmer` dạng list khi tải danh sách.
- Empty: illustration + "Chưa có thông báo nào" kèm gợi ý.
- Error: `SnackBar`/`AlertDialog` màu `danger` kèm nút "Thử lại".
- Success: không có SnackBar riêng (màn hình xem thuần túy).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ icon chuông trên Dashboard hoặc mục điều hướng riêng theo actor; tap item → `08_NotificationDetail.md` (Teacher/Parent).
**Bố cục**: Report 3 §3.3.2.1.13 — trang riêng full-screen, danh sách `ListTile`/Card cuộn dọc, mỗi item có Chip màu bên trái + Sent Time bên phải, pull-to-refresh bắt buộc.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Center/List, nền tảng Mobile, actor ScA/Teacher/SC/Parent.
Bố cục: trang riêng full-screen, ListTile/Card danh sách thông báo, Chip Warning Badge (đỏ/vàng/xám), Sent Time, pull-to-refresh.
Áp dụng Design System: danger #B91C1C, warning #B45309, secondary #64748B.
Trạng thái cần thể hiện: loading (Shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5475–5486 (§3.3.2.1.13).
- business_rules.md (BR-138, BR-158, BR-159); `00_Foundation/Design_System.md` mục 3 (quy ước màu badge — lưu ý: tài liệu này trích dẫn "BR-123" nhưng BR-123 **không tồn tại** trong `business_rules.md`/`traceability_matrix.md`, có khả năng là lỗi đánh số sót lại, cần team xác nhận; BR-123 là BR gần nhất về cảnh báo dị ứng).
- error_message.md (MSG-068).

**Ghi chú**: giống bản Web — không có UC-### chính thức. **[Giả định — cần team xác nhận]** hành vi filter/phân trang cụ thể.

# Screen Spec — Notification Detail

**Actor**: Teacher, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Detail") | **UC liên quan**: — (không có UC riêng trong Report 3/traceability_matrix.md) | **Status**: Draft
**BR liên quan**: BR-123 (cảnh báo dị ứng khi quét ID — nếu thông báo loại này, chi tiết dẫn thẳng tới thông tin dị ứng học sinh), BR-142 (phát hiện từ khóa bệnh lý), BR-158 (Quiet Hours)
**MSG liên quan**: không có MSG code riêng (chỉ hiển thị lại nội dung MSG gốc đã gửi)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại `07_NotificationCenterList.md` |
| 2 | Warning Badge/Loại thông báo | Chip màu | Đỏ/Vàng/Xám theo mức độ (kế thừa từ List) | — (quy ước màu `Design_System.md` mục 3, xem ghi chú) | [Giả định — cần team xác nhận] |
| 3 | Tiêu đề thông báo | Text | — | — | [Giả định] |
| 4 | Nội dung đầy đủ (nguyên văn MSG đã gửi) | Text (dài, cuộn) | Hiển thị đúng nội dung MSG gốc | — | Theo `UI_Design_Guidelines.md` mục 4 |
| 5 | Sent Time | Text | — | — | |
| 6 | Nút hành động liên quan (nếu có deep link) | FilledButton | — | — | VD "Xem hồ sơ dị ứng học sinh" — deep link theo `Navigation_Guidelines.md` mục 3 |

### Trạng thái màn hình
- Loading: `CircularProgressIndicator` khi tải chi tiết.
- Empty: không áp dụng.
- Error: `SnackBar`/`AlertDialog` màu `danger` kèm nút "Thử lại".
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ tap 1 item trên `07_NotificationCenterList.md` (Mobile); Back (AppBar) → về List. Deep link trực tiếp từ push notification (không qua Dashboard) theo `Navigation_Guidelines.md` mục 3.
**Bố cục**: trang riêng full-screen — AppBar Back, Chip Badge + Tiêu đề trên cùng, nội dung đầy đủ cuộn dọc, nút hành động liên quan (nếu có) cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Detail, nền tảng Mobile, actor Teacher/Parent.
Bố cục: trang riêng full-screen, Chip Warning Badge, tiêu đề, nội dung đầy đủ, Sent Time, nút hành động liên quan (nếu có).
Áp dụng Design System: danger #B91C1C cho badge đỏ.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `14_Screen_Authorization_Matrix.md` khối 1 (dòng Notification Detail, không có UC).
- business_rules.md (BR-123, BR-142, BR-158); `00_Foundation/Design_System.md` mục 3 (quy ước màu — lưu ý "BR-123" trích dẫn trong Design_System.md không tồn tại trong `business_rules.md`/`traceability_matrix.md`, cần team xác nhận nguồn đúng).
- `03_Design/00_Foundation/Navigation_Guidelines.md` mục 3.

**Ghi chú quan trọng**: giống bản Web — không có nguồn field-by-field trong Report 3 và không có UC-### ánh xạ. Toàn bộ field là **[Giả định — cần team xác nhận]**.

# Screen Spec — Notification Settings

**Actor**: Mọi actor (cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: UC-088 (Report 3 §3.2, dòng 4861–4879) | **Status**: Draft
**BR liên quan**: BR-158 (Quiet Hours mặc định 22:00–06:00), BR-158b (cấu hình giờ yên tĩnh cá nhân), BR-159 (retry + fallback email)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại `05_ProfileManagement.md` |
| 2 | Push Notifications | Switch (M3) | — | — | Report 3 §3.3.2.1.12 |
| 3 | Email Notifications | Switch (M3) | — | — | |
| 4 | Allergy Warnings | Switch (M3) | — | BR-123 [Giả định — có khóa cứng bật với MS/Teacher hay không cần xác nhận] | |
| 5 | Food Safety Alerts | Switch (M3) | — | BR-138 | |
| 6 | Quiet Hours (khung giờ) | TimePicker (M3) | Mặc định 22:00–06:00 | BR-158, BR-158b | [Giả định — cần team xác nhận UI cụ thể, không có trong field gốc Report 3] |
| 7 | Nút "Save Settings" | FilledButton (full-width) | — | — | Submit → lưu cấu hình |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải cấu hình hiện tại; spinner trên "Save Settings" khi lưu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005.
- Success: SnackBar xác nhận chung "Đã lưu cài đặt thông báo" [Giả định — chưa có MSG code riêng].

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu "Notification Settings" trên Profile Management (Mobile); thành công → quay lại Profile Management.
**Bố cục**: Report 3 §3.3.2.1.12 — trang riêng full-screen: AppBar Back, danh sách `SwitchListTile` (Push/Email/Allergy Warnings/Food Safety Alerts), Quiet Hours dùng `TimePicker` M3, `FilledButton` "Save Settings" full-width cuối trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Settings, nền tảng Mobile, actor Guest & Common (dùng chung mọi actor).
Bố cục: trang riêng full-screen, AppBar Back, SwitchListTile Push/Email/Allergy Warnings/Food Safety Alerts, TimePicker Quiet Hours, FilledButton "Save Settings".
Áp dụng Design System: seed color #0E7490 cho switch bật.
Trạng thái cần thể hiện: loading, error (MSG-004/005), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4861–4879 (UC-088), dòng 5462–5473 (§3.3.2.1.12).
- business_rules.md (BR-158, BR-158b, BR-159).
- error_message.md (MSG-004, MSG-005).
