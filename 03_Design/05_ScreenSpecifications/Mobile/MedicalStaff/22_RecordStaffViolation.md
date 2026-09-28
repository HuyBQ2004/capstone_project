# Screen Spec — Record Violation (Dialog hợp nhất — thay thế Record Process Violation UC-178 + Record Staff Violation UC-179)

**Actor**: Medical Staff (dùng chung với Service Caterers theo BR-183/184) | **UC liên quan**: UC-179 - Record Staff Violation (Report 3 §3.2, dòng 4639–4659) — **mã UC sống sót sau khi gộp UI với UC-178, quyết định 11/07/2026**. UC-178 - Record Process Violation (Report 3 §3.2, dòng 3663–3683) — **Deprecated 11/07/2026**, xem `21_RecordProcessViolation.md` | **Status**: Draft
**BR liên quan**: BR-183 (vi phạm quy trình/sản phẩm — kích hoạt khi field "Nhân viên liên quan" để trống), BR-184 (vi phạm nhân viên — kích hoạt khi có điền), BR-185, BR-186, BR-187 (cảnh báo nghi ngờ tái diễn 90 ngày), BR-188 (audit log bất biến), BR-190 (báo cáo lạm dụng manual override), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký — sửa 11/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error), MSG-086 (Success), MSG-088 (Warning)

> **Quyết định thiết kế 07/07/2026, chốt cuối 11/07/2026** (`review/medical_staff/review_screen_medical_staff.md` Mục 10): giữ tách BR-183/BR-184 ở tầng dữ liệu (nguồn gốc phát sinh khác nhau) nhưng **gộp UI thành 1 dialog duy nhất** — không có toggle chọn "loại vi phạm", BR-183 hay BR-184 được suy ra tự động từ việc có điền field "Nhân viên liên quan" (field 4) hay không. **UC-179/`StaffViolationRecord` là mã "sống sót"**, `21_RecordProcessViolation.md` nay là file tham chiếu. Nội dung đầy đủ giống hệt bản Web (`Web/MedicalStaff/22_RecordStaffViolation.md`) — file này chỉ khác phần Platform Layout Mobile.
> **Cập nhật 11/07/2026 — bỏ hẳn field nhân chứng**: hệ thống không track thông tin người chứng kiến dưới bất kỳ hình thức nào. PDF xuất ra vẫn giữ nguyên cột chữ ký thứ 3 "Người chứng kiến" (khớp mẫu giấy gốc) nhưng là khoảng trống tĩnh, không gắn field dữ liệu.
> **Cập nhật 11/07/2026 — đổi tên tiêu đề PDF**: từ "BIÊN BẢN SỰ VIỆC" → **"BIÊN BẢN VI PHẠM"**.
> **Lý do không bắt buộc quy trách nhiệm cá nhân**: mục đích cốt lõi của biên bản là Nhà trường (Medical Staff) ghi nhận lỗi của Service Caterers cấp công ty, 2 bên ký xác nhận cùng chịu trách nhiệm — không bắt buộc quy cho 1 cá nhân. Field "Nhân viên liên quan" (field 4) chỉ dùng khi Medical Staff **chủ động muốn** kích hoạt thêm kỷ luật cá nhân (BR-184/185/187).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Địa điểm xảy ra sự việc (`locationOfIncident`) | Input text | Bắt buộc, không rỗng | BR-211 | Mẫu giấy: "Hôm nay, tại: ___" |
| 2 | Đại diện Y tế trường học (`reportedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản đăng nhập hiện tại | BR-183/184 | Mẫu giấy: "Đại diện P.VSATTP" |
| 3 | Đại diện Nhà thầu (`caterersRepresentativeId`) | Select (tài khoản Service Caterers đang hoạt động) | Bắt buộc | BR-211 | Mẫu giấy: "Đại diện nhà thầu"; liên kết tài khoản có thật |
| 4 | Nhân viên liên quan (`staffName`) | Input text tự do (không phải khóa ngoại) | Tùy chọn — để trống nếu không quy trách nhiệm cho 1 cá nhân cụ thể | BR-184 | Trống = chỉ áp BR-183; có giá trị = kích hoạt thêm BR-184/185/187 |
| 5 | Chức vụ nhân viên (`staffPosition`) | Input text tự do | Tùy chọn; chỉ hiện khi field 4 đã có ký tự đầu tiên | BR-184 | Không có trên mẫu giấy |
| 6 | Mức độ nghiêm trọng (`severityLevel`) | Select (LOW/MEDIUM/HIGH) | Bắt buộc | BR-183/184, BR-186 | Điều kiện tự động phân luồng nút "Đóng biên bản"/"Gửi School Administration phê duyệt" ở `24_RecordViolationResolution.md` |
| 7 | Loại vi phạm (`violationType`, gồm `OVERRIDE_ABUSE`) | Select | Tùy chọn | BR-190 | Danh sách 4 giá trị: "Bỏ qua bước quy trình bắt buộc", "Vi phạm vệ sinh cá nhân", "Lạm dụng quyền ghi đè hệ thống", "Hành động không được ủy quyền" — cần đội dự án xác nhận trước khi khóa cứng enum |
| 8 | Thời điểm xảy ra sự việc (`occurredAt`) | DateTimePicker | Bắt buộc | BR-211 | Dùng để tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày]..." trong PDF |
| 9 | **Nội dung biên bản** (`violationContent`) | Rich text editor (toolbar rút gọn trên Mobile — đậm/nghiêng/gạch chân + gạch đầu dòng, xem Platform Layout) | Bắt buộc, không rỗng | BR-183/184 | MSG-085 nếu thiếu |
| 10 | Ảnh bằng chứng | Camera + File input, nhiều ảnh | Bắt buộc ít nhất 1 ảnh | BR-183/184 | MSG-085 nếu thiếu |
| 11 | Nút "Save Violation" | Button (primary) | Disabled khi thiếu field bắt buộc (1, 3, 6, 8, 9, 10) | BR-183/184, BR-188 | Lưu dữ liệu số ngay — không chờ ảnh biên bản đã ký |
| 12 | Nút "Xuất Biên bản (PDF)" | Button (secondary) | Chỉ hiện sau khi đã Save Violation | BR-211 | Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**; mở trình xem PDF/chia sẻ hệ điều hành để in |
| 13 | Ảnh biên bản đã ký (`signedRecordPhotoUrl`) | Camera + File input | Không bắt buộc để Save; **bắt buộc** trước khi đóng/gửi duyệt ở `24_RecordViolationResolution.md` — gate chặn cứng | BR-211 | Chụp trực tiếp bằng camera theo `Layout_Guidelines.md` mục 2.2 |

### Cơ chế phân loại BR-183 vs BR-184 (không có toggle thủ công)
- Field 4 (`staffName`) để trống → bản ghi chỉ chịu **BR-183** — field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` **không hiện**.
- Field 4 có giá trị → kích hoạt thêm **BR-184/185** (kỷ luật cá nhân) và **BR-187** (cảnh báo nghi ngờ tái diễn, best-effort theo `staffName` trong 90 ngày).
- Field 3, 1/8/9/10 luôn bắt buộc bất kể field 4 có điền hay không.
- Khi mở dialog từ 1 `NonConformanceRecord` đã có sẵn (nâng cấp BR-183), 1 số field có thể pre-fill; khi mở độc lập, toàn bộ trống.

### Trạng thái màn hình
- **Loading**: spinner trên "Save Violation" khi lưu + rà soát trùng tên (best-effort, khi field 4 có giá trị).
- **Empty**: không áp dụng.
- **Error**: MSG-004, MSG-005, MSG-085 tại field thiếu.
- **Success**: MSG-086 (SnackBar). Nếu field 4 có giá trị và trùng khớp ≥ MEDIUM trong 90 ngày → MSG-088 (banner, không chặn lưu). Điều hướng `24_RecordViolationResolution.md`. Nếu chưa có `signedRecordPhotoUrl`, màn 24 chặn cứng nút đóng/gửi duyệt (MSG-095).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Report Violation" trên Dashboard (`03_Medical_Staff_Screen_Flow.md`), hoặc từ 1 `NonConformanceRecord` Không đạt; thành công → `24_RecordViolationResolution.md`.
**Bố cục**: trang riêng full-screen, camera (field 10) ưu tiên đặt đầu form theo `Layout_Guidelines.md` mục 2.2, form dọc, field 5 chỉ hiện khi field 4 có giá trị, nút "Save Violation" cố định cuối màn. **Lưu ý cần xác nhận thêm**: toolbar rich text đầy đủ (bảng, căn lề, màu chữ) không khả thi trên màn hình hẹp — rút gọn còn đậm/nghiêng/gạch chân + gạch đầu dòng, đội dự án/dev xác nhận trước khi code.

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm" (`08_Medical_Staff_Screen_Flow_Web.md`); thành công → `24_RecordViolationResolution.md`.
**Bố cục**: `Dialog` 1 cột — xem đầy đủ tại `Web/MedicalStaff/22_RecordStaffViolation.md`.

---

## Xuất & Lưu Biên bản đã ký (BR-211, sửa 11/07/2026)

> Mô tả đầy đủ tại `Web/MedicalStaff/22_RecordStaffViolation.md` — áp dụng chung cả 2 nền tảng. Trên Mobile: nút "Xuất Biên bản (PDF)" mở trình xem PDF/trình chia sẻ hệ điều hành để in qua máy in kết nối hoặc lưu file; field "Ảnh biên bản đã ký" dùng camera trực tiếp theo `Layout_Guidelines.md` mục 2.2. Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**, cột chữ ký thứ 3 "Người chứng kiến" để trống tĩnh (không gắn field).

## AI Generation Prompt ⭐
```
Tạo wireframe cho dialog "Lập Biên bản Vi phạm", nền tảng Mobile, actor Medical Staff.
Bố cục: trang riêng full-screen, camera ưu tiên đầu form, form dọc.
Thứ tự field: Địa điểm xảy ra sự việc, Đại diện Y tế trường học (hiển thị tự động, không sửa),
Đại diện Nhà thầu (Select) — KHÔNG có field Người chứng kiến trong form,
Nhân viên liên quan (tùy chọn) — khi có giá trị thì hiện thêm Chức vụ nhân viên,
Mức độ nghiêm trọng (Select LOW/MEDIUM/HIGH), Loại vi phạm (Select, tùy chọn),
Thời điểm xảy ra sự việc (DateTimePicker), khối "Nội dung biên bản" là rich text editor rút gọn
(đậm/nghiêng/gạch chân/gạch đầu dòng), phía trên hiện dòng tự sinh "Vào hồi [giờ] ngày [ngày]...",
ảnh bằng chứng (camera, bắt buộc ít nhất 1), nút Save Violation, sau khi lưu hiện thêm nút
"Xuất Biên bản (PDF)" (tiêu đề PDF "BIÊN BẢN VI PHẠM").
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM).
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-086/MSG-088).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm toggle chọn "loại vi phạm".
KHÔNG thêm field nhân chứng (witness) trong form nhập liệu.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4639–4659 (UC-179), dòng 3663–3683 (UC-178 — nay Deprecated).
- business_rules.md §18 (BR-183, BR-184, BR-185, BR-186, BR-187, BR-188, BR-190, BR-211).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-086, MSG-088, MSG-095).
- `02_Requirements/Violation_Management_Proposal.md`.
- `00_Policies/specialreport2173_1.md` (mẫu Biên bản Sự việc).
- `03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md` (nhánh "ViolForm").
- `review/medical_staff/review_screen_medical_staff.md` Mục 10 (lịch sử quyết định gộp dialog).
