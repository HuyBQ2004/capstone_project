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
