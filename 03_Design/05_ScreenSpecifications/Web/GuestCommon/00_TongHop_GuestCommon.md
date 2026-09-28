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

# Screen Spec — Profile Management

**Actor**: System Admin, School Administration, Medical Staff, Service Caterers (cột `x` — Teacher/Raw Food Supplier/Parent KHÔNG có cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Profile Management") | **UC liên quan**: UC-059 (Profile Manage, hub — Report 3 §3.2, dòng 1403–1421), UC-138 (View Profile, dòng 1513–1531), UC-097 (Update Profile, dòng 1425–1443, hiển thị dưới dạng edit mode trong cùng màn — xem ghi chú) | **Status**: Draft
**BR liên quan**: BR-012 (định dạng email/phone hợp lệ), BR-013 (giới hạn upload ảnh 5MB, JPG/JPEG/PNG)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-008 (Error), MSG-009 (Error), MSG-029 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Avatar | Ảnh (hiển thị) | — | BR-013 | Report 3 §3.3.2.1.9 |
| 2 | Name | Text (đọc) | — | — | |
| 3 | Role & School/Company | Text (đọc) | — | — | Lấy từ role/schoolId trong JWT |
| 4 | Nút "Settings" | Button | — | — | Mở khối "Edit Profile" (xem ghi chú) |
| 5 | Menu "Edit Profile" | Menu item | — | UC-097 | Chuyển sang edit mode: Avatar Upload, Full Name, Phone Number (định dạng BR-012), Email (read-only) |
| 6 | Menu "Change Password" | Menu item | — | — | Điều hướng sang `06_ChangePassword.md` |
| 7 | Menu "Notification Settings" | Menu item | — | — | Điều hướng sang `09_NotificationSettings.md` |
| 8 | Nút "Logout" | Button | Hiện dialog xác nhận trước khi đăng xuất | BR-004 | UC-035 Logout (Report 3 dòng 1381–1399) |

### Danh sách Field khi ở Edit mode (UC-097)
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| E1 | Avatar Upload | File input (ảnh) | JPG/JPEG/PNG, tối đa 5MB | BR-013 | MSG-008 nếu vượt dung lượng, MSG-009 nếu sai định dạng |
| E2 | Full Name | Input (text) | Bắt buộc | — | |
| E3 | Phone Number | Input (text) | Định dạng +84 hoặc 0xx hợp lệ | BR-012 | Cảnh báo validation nếu sai |
| E4 | Email Address | Input (text, read-only) | Không sửa được | — | |
| E5 | Nút "Save" | Button (primary) | Disabled khi chưa thay đổi gì | BR-012, BR-013 | Submit → MSG-029 |
| E6 | Nút "Cancel" | Button | — | — | Hủy chỉnh sửa, quay lại view mode |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải dữ liệu profile (UC-138 bước 2); spinner trên nút "Save" khi đang lưu.
- Empty: không áp dụng (mọi user đăng nhập luôn có 1 profile).
- Error: MSG-004 (session hết hạn), MSG-005 (không có quyền truy cập), MSG-008 (ảnh vượt 5MB), MSG-009 (sai định dạng ảnh), số điện thoại sai định dạng → cảnh báo inline (BR-012).
- Success: MSG-029 (Toast) sau khi lưu Edit Profile.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ dropdown Avatar góc phải Topbar (mọi trang trong Dashboard); thoát: đóng dropdown/modal quay lại trang trước.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — không phải trang riêng mà là **dropdown menu** ở Topbar (theo `13_Guest_Common_Screen_Flow.md` mục "Khác biệt bố cục"): dropdown hiển thị Avatar/Name/Role tóm tắt + danh sách menu (Edit Profile, Change Password, Notification Settings, Logout). Khi chọn "Edit Profile", mở `Dialog` (shadcn) chứa form Edit mode (E1–E6) theo `Component_Guidelines.md`.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Bottom Tab Bar mục "Profile" (theo Report 3 §3.3.2.1.9 "Bottom Tab Bar: Home, Meals, Health, Profile" — lưu ý nhãn tab cụ thể theo từng actor lấy từ `Navigation_Guidelines.md`, không cố định 4 tab này cho mọi actor).
**Bố cục**: Report 3 §3.3.2.1.9 — trang riêng full-screen: Avatar lớn trên cùng, Name, Role & School/Company, danh sách Menu dạng `ListTile` (Edit Profile, Change Password, Notification Settings, AI Chatbot), nút "Logout" cuối trang. Khi chọn "Edit Profile", chuyển sang trang riêng (Report 3 §3.3.2.1.10) chứa form Edit mode (E1–E6), có nút "Cancel"/"Save" cố định trên AppBar hoặc cuối trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Profile Management, nền tảng [Web/Mobile], actor dùng chung (SA/ScA/MS/SC).
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Avatar, Name, Role & School/Company, menu Edit Profile/Change Password/Notification Settings, nút Logout;
form Edit mode: Avatar Upload, Full Name, Phone Number, Email (read-only), nút Save/Cancel.
Áp dụng Design System: primary #0E7490 — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (Skeleton), error (MSG-008/009/validation phone), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1403–1421 (UC-059), dòng 1513–1531 (UC-138), dòng 1425–1443 (UC-097), dòng 1381–1399 (UC-035 Logout), dòng 5420–5449 (§3.3.2.1.9–10).
- business_rules.md (BR-012, BR-013).
- error_message.md (MSG-004, MSG-005, MSG-008, MSG-009, MSG-029).

**Ghi chú**: Report 3 mô tả "Edit Profile" (§3.3.2.1.10) như 1 màn hình riêng trên Mobile, nhưng `14_Screen_Authorization_Matrix.md` khối 1 không có dòng riêng cho "Edit Profile" — theo nguyên tắc "không tạo file cho screen không có cột `x` riêng trong Matrix", nội dung Edit Profile được gộp vào file này (dưới dạng edit mode/trang con điều hướng từ Profile Management), không tách file riêng.

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

# Screen Spec — Notification Center/List

**Actor**: School Administration, Teacher, Service Caterers, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Center/List" — System Admin/Medical Staff/Raw Food Supplier KHÔNG có, chỉ có Notification Settings) | **UC liên quan**: — (không có UC riêng; nội dung suy ra từ "Notification History" Report 3 §3.3.2.1.13 + BR-159 cơ chế gửi) | **Status**: Draft
**BR liên quan**: BR-138 (cảnh báo quan trọng bỏ qua phê duyệt, phát ngay), BR-158 (Quiet Hours 22:00–06:00, không áp dụng cho cảnh báo khẩn cấp), BR-159 (retry + fallback email)
**MSG liên quan**: MSG-068 (Warning — hàng đợi push bị tắc nghẽn, hiển thị gián tiếp qua trạng thái "đang gửi")

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Button (icon/link) | — | — | Quay lại trang trước (Dashboard) |
| 2 | Notification List | List/Table (nhiều bản ghi) | Sắp xếp mới nhất trên đầu | BR-158 | Report 3 §3.3.2.1.13 (gốc gọi "Notification History") |
| 3 | Warning Badge (mỗi item) | Badge màu | Đỏ = cảnh báo dị ứng/an toàn thực phẩm, Vàng = tin tức, Xám = hệ thống | — (quy ước màu theo `Design_System.md` mục 3; BR-123 là BR gần nhất về cảnh báo dị ứng nhưng chỉ áp dụng cho ngữ cảnh quét ID học sinh, không phải quy tắc màu badge tổng quát — xem ghi chú) | Không dùng chỉ màu sắc — kèm icon/label theo `Accessibility.md` |
| 4 | Sent Time (mỗi item) | Text (giờ/ngày) | — | — | |
| 5 | Click vào 1 item | Interaction | — | — | Điều hướng sang `08_NotificationDetail.md` (chỉ actor có cột `x` ở Notification Detail: Teacher, Parent — với School Administration/Service Caterers hiện chi tiết dạng mở rộng inline/panel, xem ghi chú Platform Layout) |

### Trạng thái màn hình
- Loading: `Skeleton` dạng list khi tải danh sách thông báo.
- Empty: illustration + câu "Chưa có thông báo nào" kèm gợi ý (theo `Component_Guidelines.md` — không dùng câu chung chung).
- Error: lỗi tải danh sách → `Alert variant="destructive"` kèm nút "Thử lại".
- Success: không có Toast riêng (đây là màn hình xem, không phải hành động ghi).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ icon chuông ở Topbar (dropdown ngắn) + link "Xem tất cả" mở trang đầy đủ; School Administration/Service Caterers không có Notification Detail riêng theo Matrix → khi click item, mở rộng nội dung trực tiếp trong `Sheet`/panel bên phải thay vì điều hướng trang.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — trang đầy đủ dùng `Table`/`DataTable` (cột: Badge mức độ, Nội dung tóm tắt, Sent Time) theo `Component_Guidelines.md` mục "Bảng dữ liệu"; có filter theo mức độ (Đỏ/Vàng/Xám) phía trên bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ icon chuông trên Dashboard hoặc mục điều hướng riêng (tùy actor); click item → `08_NotificationDetail.md` (Teacher/Parent) hoặc mở rộng inline (School Administration/Service Caterers không có Detail riêng).
**Bố cục**: Report 3 §3.3.2.1.13 — trang riêng full-screen, danh sách `ListTile`/Card cuộn dọc, mỗi item có Warning Badge (Chip màu) bên trái + Sent Time bên phải, pull-to-refresh bắt buộc (`Layout_Guidelines.md` mục 2.1).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Center/List, nền tảng [Web/Mobile], actor ScA/Teacher/SC/Parent.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: danh sách thông báo, Warning Badge (đỏ/vàng/xám kèm icon/label), Sent Time.
Áp dụng Design System: danger #B91C1C cho badge đỏ, warning #B45309 cho vàng, secondary #64748B cho xám.
Trạng thái cần thể hiện: loading (Skeleton), empty (illustration + hướng dẫn), error (Alert destructive).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5475–5486 (§3.3.2.1.13, gốc gọi "Notification History").
- business_rules.md (BR-138, BR-158, BR-159); `00_Foundation/Design_System.md` mục 3 (quy ước màu badge — lưu ý: tài liệu này trích dẫn "BR-123" cho quy tắc màu đỏ cảnh báo dị ứng, nhưng BR-123 **không tồn tại** trong `business_rules.md`/`traceability_matrix.md` — có khả năng là lỗi đánh số sót lại, cần team xác nhận lại nguồn đúng, tạm dùng BR-123 làm tham chiếu gần nhất).
- error_message.md (MSG-068).

**Ghi chú**: không có UC-### chính thức nào ánh xạ trực tiếp cho màn hình xem danh sách này trong `traceability_matrix.md` — nội dung suy ra từ field gốc Report 3 §3.3.2.1.13 và các BR liên quan tới cơ chế gửi thông báo. **[Giả định — cần team xác nhận]** hành vi filter/phân trang cụ thể.

# Screen Spec — Notification Detail

**Actor**: Teacher, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Detail" — chỉ 2 actor này có màn chi tiết riêng, các actor khác có Notification Center xem mở rộng inline hoặc không có Center) | **UC liên quan**: — (không có UC riêng trong Report 3/traceability_matrix.md) | **Status**: Draft
**BR liên quan**: BR-123 (cảnh báo dị ứng khi quét ID — nếu thông báo loại này, chi tiết phải dẫn thẳng tới thông tin dị ứng học sinh liên quan), BR-142 (phát hiện từ khóa bệnh lý), BR-158 (Quiet Hours)
**MSG liên quan**: không có MSG code riêng cho màn xem chi tiết (chỉ hiển thị lại nội dung MSG gốc đã gửi)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Button (icon/link) | — | — | Quay lại `07_NotificationCenterList.md` |
| 2 | Warning Badge/Loại thông báo | Badge màu | Đỏ/Vàng/Xám theo mức độ (kế thừa từ List) | — (quy ước màu `Design_System.md` mục 3, xem ghi chú) | [Giả định — cần team xác nhận] |
| 3 | Tiêu đề thông báo | Text | — | — | [Giả định] |
| 4 | Nội dung đầy đủ (nguyên văn MSG đã gửi) | Text (dài) | Hiển thị đúng nội dung MSG gốc, không viết lại | — | Theo `UI_Design_Guidelines.md` mục 4 |
| 5 | Sent Time | Text | — | — | Kế thừa từ List |
| 6 | Nút hành động liên quan (nếu có deep link) | Button | — | — | VD thông báo dị ứng → nút "Xem hồ sơ dị ứng học sinh" điều hướng deep-link (theo `Navigation_Guidelines.md` mục 3) |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải chi tiết thông báo.
- Empty: không áp dụng (luôn có 1 thông báo cụ thể khi vào màn này).
- Error: không tải được chi tiết → `Alert variant="destructive"` kèm nút "Thử lại".
- Success: không áp dụng (màn hình xem thuần túy).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ click 1 item trên `07_NotificationCenterList.md` (chỉ actor Teacher/Parent); Back → về List.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — trang con hoặc `Sheet` trượt từ phải, hiển thị Badge + Tiêu đề trên cùng, nội dung đầy đủ ở giữa, Sent Time nhỏ góc dưới, nút hành động liên quan (nếu có) ở cuối.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ tap 1 item trên `07_NotificationCenterList.md` (Mobile); Back (AppBar) → về List.
**Bố cục**: trang riêng full-screen (theo `Navigation_Guidelines.md` mục 3 "deep link cho Notification"): AppBar Back, Badge + Tiêu đề trên cùng, nội dung đầy đủ cuộn dọc, nút hành động liên quan cố định cuối màn nếu có.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Detail, nền tảng [Web/Mobile], actor Teacher/Parent.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Warning Badge, tiêu đề, nội dung đầy đủ, Sent Time, nút hành động liên quan (nếu có deep link).
Áp dụng Design System: danger #B91C1C cho badge đỏ.
Trạng thái cần thể hiện: loading (Skeleton), error (Alert destructive).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `14_Screen_Authorization_Matrix.md` khối 1 (dòng Notification Detail, không có UC).
- business_rules.md (BR-123, BR-142, BR-158); `00_Foundation/Design_System.md` mục 3 (quy ước màu — lưu ý "BR-123" trích dẫn trong Design_System.md không tồn tại trong `business_rules.md`/`traceability_matrix.md`, cần team xác nhận nguồn đúng).
- `03_Design/00_Foundation/Navigation_Guidelines.md` mục 3 (deep link).

**Ghi chú quan trọng**: đây là màn hình **không có nguồn field-by-field trong Report 3** (Report 3 §3.3.2.1 chỉ có "Notification History" dạng list, không có màn Detail riêng) và **không có UC-### nào** trong `traceability_matrix.md` ánh xạ tới. Toàn bộ field trong bảng trên là **[Giả định — cần team xác nhận]** dựa trên suy luận hợp lý từ pattern List→Detail chung của hệ thống và yêu cầu deep-link ở `Navigation_Guidelines.md`. Đây là khoảng trống dữ liệu cần đội ngũ xác nhận trước khi khóa spec, không phải lỗi bỏ sót khi viết tài liệu này.

# Screen Spec — Notification Settings

**Actor**: Mọi actor (cột `x` ở cả 7 actor, `14_Screen_Authorization_Matrix.md` khối 1) | **UC liên quan**: UC-088 (Report 3 §3.2, dòng 4861–4879) | **Status**: Draft
**BR liên quan**: BR-158 (Quiet Hours mặc định 22:00–06:00, cảnh báo khẩn cấp không bị ảnh hưởng), BR-158b (cấu hình giờ yên tĩnh cá nhân), BR-159 (retry + fallback email), BR-160–164 (bảo mật cấu hình tích hợp — áp dụng gián tiếp cho hạ tầng gửi thông báo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Button (icon/link) | — | — | Quay lại `05_ProfileManagement.md` |
| 2 | Push Notifications | Toggle/Switch | — | — | Report 3 §3.3.2.1.12 |
| 3 | Email Notifications | Toggle/Switch | — | — | |
| 4 | Allergy Warnings | Toggle/Switch | — | BR-123 (loại cảnh báo không tắt được với actor có trách nhiệm an toàn — [Giả định cần xác nhận có cho phép tắt hay khóa cứng với MS/Teacher]) | |
| 5 | Food Safety Alerts | Toggle/Switch | — | BR-138 | |
| 6 | Quiet Hours (khung giờ) | Time range picker | Mặc định 22:00–06:00 | BR-158, BR-158b | Không có field riêng trong Report 3 gốc — bổ sung theo BR-158b "người dùng có thể cấu hình giờ yên tĩnh cá nhân" [Giả định — cần team xác nhận UI cụ thể] |
| 7 | Nút "Save Settings" | Button (primary) | — | — | Submit → lưu cấu hình |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải cấu hình hiện tại; spinner trên nút "Save Settings" khi lưu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005.
- Success: không có MSG code riêng cho hành động lưu cấu hình thành công trong `error_message.md` — hiển thị Toast xác nhận chung "Đã lưu cài đặt thông báo" [Giả định — cần team xác nhận có cần thêm MSG code riêng].

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "Notification Settings" trong dropdown Profile (Topbar) hoặc menu riêng theo actor; thoát: Back → Profile/Dashboard.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — trang riêng (không phải modal, vì có nhiều toggle + time picker), danh sách toggle xếp dọc trong `Card`, Quiet Hours dùng time range picker riêng biệt, nút "Save Settings" cố định góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu "Notification Settings" trên Profile Management (Mobile); thành công → quay lại Profile Management.
**Bố cục**: Report 3 §3.3.2.1.12 — trang riêng full-screen: AppBar Back, danh sách `SwitchListTile` (Push/Email/Allergy Warnings/Food Safety Alerts), Quiet Hours dùng `TimePicker` M3, `FilledButton` "Save Settings" full-width cuối trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Settings, nền tảng [Web/Mobile], actor Guest & Common (dùng chung mọi actor).
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: toggle Push Notifications, Email Notifications, Allergy Warnings, Food Safety Alerts, Quiet Hours time range, nút Save Settings.
Áp dụng Design System: primary #0E7490 cho toggle bật.
Trạng thái cần thể hiện: loading (Skeleton), error (MSG-004/005), success (toast xác nhận).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4861–4879 (UC-088), dòng 5462–5473 (§3.3.2.1.12).
- business_rules.md (BR-158, BR-158b, BR-159).
- error_message.md (MSG-004, MSG-005).
