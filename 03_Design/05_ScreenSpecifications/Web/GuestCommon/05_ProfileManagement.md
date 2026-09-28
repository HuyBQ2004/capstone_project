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
