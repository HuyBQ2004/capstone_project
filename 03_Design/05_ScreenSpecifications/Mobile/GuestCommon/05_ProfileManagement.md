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
