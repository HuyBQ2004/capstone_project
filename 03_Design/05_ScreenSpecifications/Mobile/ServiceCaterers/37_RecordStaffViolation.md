# Screen Spec — Record Staff Violation

**Actor**: Service Caterers (đồng thời Medical Staff — cả 2 vai trò đều có quyền lập biên bản theo BR-184) | **UC liên quan**: UC-179 - Record Staff Violation (Report 3 §3.2, mới 2026-07-01) | **Status**: Draft
**BR liên quan**: BR-184, BR-187, BR-190
**MSG liên quan**: MSG-085 (Error), MSG-086 (Success), MSG-088 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên nhân viên vi phạm (staffName) | TextField tự do | Bắt buộc | BR-184 | Nhân viên bếp/catering không có tài khoản User — text tự do |
| 2 | Chức vụ nhân viên (tuỳ chọn) | TextField | Tuỳ chọn | BR-184 | UC-179 Normal Flow bước 2 |
| 3 | Loại vi phạm | DropdownMenu | Bắt buộc (gồm `OVERRIDE_ABUSE`) | BR-184, BR-190 | UC-179 Normal Flow bước 2 |
| 4 | Mô tả vi phạm | TextField multiline | Bắt buộc | BR-184 | MSG-085 nếu thiếu |
| 5 | Ảnh bằng chứng | Camera capture | Bắt buộc | BR-184 | MSG-085 nếu thiếu |
| 6 | Mức độ nghiêm trọng | RadioListTile | Bắt buộc | BR-184 | UC-179 Normal Flow bước 2 |
| 7 | Ngày giờ xảy ra vi phạm | DateTimePicker (M3) | Bắt buộc | — | UC-179 Normal Flow bước 2 |
| 8 | Cảnh báo nghi ngờ tái diễn vi phạm | MaterialBanner (best-effort, không chặn) | Hiện khi staffName trùng khớp trong 90 ngày gần nhất | BR-187 | MSG-088 |
| 9 | Nút "Lưu Biên bản Vi phạm Nhân viên" | FilledButton (cố định cuối màn) | Chặn nếu thiếu field bắt buộc; không bị chặn bởi cảnh báo tái diễn | BR-184 | MSG-086 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải form.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-085 nếu thiếu mô tả/ảnh/field bắt buộc.
- Success: `SnackBar` MSG-086; nếu nghi ngờ tái diễn, hiển thị thêm banner MSG-088 (không chặn lưu).

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Grid Menu Items → "Vi phạm" (`04_Service_Caterers_Screen_Flow.md`, Service Caterers có quyền lập biên bản theo BR-184). Thoát: lưu xong quay lại Corrective Actions Dashboard/Violation Management.
**Bố cục**: Trang riêng — thông tin nhân viên + loại vi phạm trên cùng, camera chụp ảnh + mô tả + mức độ + ngày giờ giữa trang, banner cảnh báo tái diễn (nếu có) trước nút lưu cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Staff Violation, nền tảng Mobile, actor Service Caterers.
Bố cục: tên nhân viên (text tự do), chức vụ, loại vi phạm, mô tả, camera chụp ảnh, mức độ nghiêm trọng, ngày giờ xảy ra, banner cảnh báo nghi ngờ tái diễn, nút Lưu Biên bản Vi phạm Nhân viên cố định dưới cùng.
Thành phần bắt buộc: cảnh báo MSG-085 khi thiếu field, banner MSG-088 (warning, không chặn) khi nghi ngờ tái diễn.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-086 + có thể kèm MSG-088).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4639–4659 (UC-179 - Record Staff Violation)
- business_rules.md BR-184, BR-187, BR-190
- error_message.md MSG-085, MSG-086, MSG-088
