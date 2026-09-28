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
