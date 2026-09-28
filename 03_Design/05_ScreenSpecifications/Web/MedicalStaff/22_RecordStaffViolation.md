# Screen Spec — Record Violation (Dialog hợp nhất — thay thế Record Process Violation UC-178 + Record Staff Violation UC-179)

**Actor**: Medical Staff (dùng chung với Service Caterers theo BR-183/184) | **UC liên quan**: UC-179 - Record Staff Violation (Report 3 §3.2, dòng 4639–4659) — **mã UC sống sót sau khi gộp UI với UC-178, quyết định 11/07/2026**. UC-178 - Record Process Violation (Report 3 §3.2, dòng 3663–3683) — **Deprecated 11/07/2026**, xem `21_RecordProcessViolation.md` | **Status**: Draft
**BR liên quan**: BR-183 (vi phạm quy trình/sản phẩm — kích hoạt khi field "Nhân viên liên quan" để trống), BR-184 (vi phạm nhân viên — kích hoạt khi có điền), BR-185, BR-186, BR-187 (cảnh báo nghi ngờ tái diễn 90 ngày), BR-188 (audit log bất biến), BR-190 (báo cáo lạm dụng manual override), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký — sửa 11/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error), MSG-086 (Success), MSG-088 (Warning)

> **Quyết định thiết kế 07/07/2026, chốt cuối 11/07/2026** (`review/medical_staff/review_screen_medical_staff.md` Mục 10): người dùng chỉ ra dialog UC-178 và UC-179 gần như giống hệt nhau, trong khi mẫu biên bản giấy thực tế (`00_Policies/specialreport2173_1.md`) không hề phân biệt 2 loại vi phạm. Quyết định: **giữ tách BR-183/BR-184 ở tầng dữ liệu** (nguồn gốc phát sinh khác nhau — BR-183 từ `NonConformanceRecord` tự động/audit, BR-184 từ nhập tay độc lập; khi `NonConformanceRecord` được "nâng cấp" theo BR-183, hệ thống tạo 1 bản ghi `StaffViolationRecord` tương ứng) nhưng **gộp UI thành 1 dialog duy nhất** — không có toggle chọn "loại vi phạm", BR-183 hay BR-184 được suy ra tự động từ việc có điền field "Nhân viên liên quan" (field 6) hay không. **UC-179/`StaffViolationRecord` là mã "sống sót"**, `21_RecordProcessViolation.md` nay là file tham chiếu.
> **Cập nhật 11/07/2026 — bỏ hẳn field nhân chứng**: hệ thống không track thông tin người chứng kiến dưới bất kỳ hình thức nào (không `witnessId`, không text tự do). PDF xuất ra vẫn giữ nguyên cột chữ ký thứ 3 "Người chứng kiến" (khớp mẫu giấy gốc — trong ví dụ thật cũng để trống) nhưng đây là khoảng trống tĩnh trên PDF, không gắn field dữ liệu nào.
> **Cập nhật 11/07/2026 — đổi tên tiêu đề PDF**: từ "BIÊN BẢN SỰ VIỆC" (nguyên văn mẫu giấy gốc) → **"BIÊN BẢN VI PHẠM"** — hệ thống chỉ dùng mẫu này cho trường hợp vi phạm.
> **Lý do không bắt buộc quy trách nhiệm cá nhân**: mục đích cốt lõi của biên bản là Nhà trường (Medical Staff) ghi nhận lỗi của Service Caterers cấp công ty, 2 bên ký xác nhận cùng chịu trách nhiệm — không bắt buộc quy cho 1 cá nhân. Field "Nhân viên liên quan" (field 6) chỉ dùng khi Medical Staff **chủ động muốn** kích hoạt thêm kỷ luật cá nhân (BR-184/185/187).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Địa điểm xảy ra sự việc (`locationOfIncident`) | Input text | Bắt buộc, không rỗng | BR-211 | Mẫu giấy: "Hôm nay, tại: ___" |
| 2 | Đại diện Y tế trường học (`reportedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản đăng nhập hiện tại | BR-183/184 | Mẫu giấy: "Đại diện P.VSATTP" |
| 3 | Đại diện Nhà thầu (`caterersRepresentativeId`) | Select (tài khoản Service Caterers đang hoạt động) | Bắt buộc | BR-211 | Mẫu giấy: "Đại diện nhà thầu"; liên kết tài khoản có thật, không nhập tay tự do |
| 4 | Nhân viên liên quan (`staffName`) | Input text tự do (không phải khóa ngoại — nhân viên catering không có tài khoản `User`) | Tùy chọn — để trống nếu không quy trách nhiệm cho 1 cá nhân cụ thể | BR-184 | Không có trên mẫu giấy. **Trống = chỉ áp BR-183** (vi phạm quy trình/sản phẩm thuần túy); **có giá trị = kích hoạt thêm BR-184/185/187** (kỷ luật cá nhân) — xem Mục "Cơ chế phân loại" bên dưới |
| 5 | Chức vụ nhân viên (`staffPosition`) | Input text tự do | Tùy chọn; chỉ hiện trong UI khi field 4 đã có ký tự đầu tiên (progressive disclosure, không phải validation bắt buộc) | BR-184 | Không có trên mẫu giấy |
| 5b 🆕 | Sự cố liên quan (`relatedIncidentId`) | Select/link (tùy chọn) | Tùy chọn | BR-185 | **Mới 15/07/2026 (Mục 11.5.1)** — liên kết chéo tới 1 `FoodSafetyIncident` (`INC-xxx`) khi vi phạm phát sinh từ điều tra sự cố (VD điều tra xác định nguyên nhân gốc rễ là nhân viên bếp làm sai). Tái sử dụng đúng pattern context-linking đã có cho `NonConformanceRecord` — Incident là ngữ cảnh mở thứ 3. **Pre-fill sẵn** khi mở dialog từ nút "Lập biên bản vi phạm nhân viên liên quan" ở `17_InvestigationWorkspace.md` (panel Root Cause). **Không** đổi luồng phê duyệt vi phạm: BR-186 (LOW tự đóng, MEDIUM/HIGH → School Admin) vẫn độc lập hoàn toàn với track CAPA (BR-153) — chỉ thêm tham chiếu chéo dữ liệu |
| 6 | Mức độ nghiêm trọng (`severityLevel`) | Select (LOW/MEDIUM/HIGH) | Bắt buộc | BR-183/184, BR-186 | Không có trên mẫu giấy — là điều kiện tự động phân luồng nút "Đóng biên bản"/"Gửi School Administration phê duyệt" ở `24_RecordViolationResolution.md` |
| 7 | Loại vi phạm (`violationType`, gồm `OVERRIDE_ABUSE`) | Select (không phải text tự do — tránh hỏng khả năng lọc) | Tùy chọn | BR-190 | Không có trên mẫu giấy — giữ Select để BR-190 (báo cáo lạm dụng manual override hàng tuần) lọc được chính xác. Danh sách 4 giá trị: "Bỏ qua bước quy trình bắt buộc", "Vi phạm vệ sinh cá nhân", "Lạm dụng quyền ghi đè hệ thống (`OVERRIDE_ABUSE`)", "Hành động không được ủy quyền" — cần đội dự án xác nhận trước khi khóa cứng enum trong code |
| 8 | Thời điểm xảy ra sự việc (`occurredAt`) | DateTimePicker | Bắt buộc | BR-211 | Dùng để hệ thống tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày]..." trong nội dung PDF, khớp mẫu giấy |
| 9 | **Nội dung biên bản** (`violationContent`, thay cho "Mô tả vi phạm" cũ) | Rich text editor (toolbar: đậm/nghiêng/gạch chân, cỡ chữ, màu chữ, bảng, căn lề, chèn ảnh/link) | Bắt buộc, không rỗng | BR-183/184 | Mẫu giấy: phần văn xuôi tự do sau câu "Vào hồi..." — nằm ngay sau câu tự sinh từ field 8, người dùng viết tự do đúng như cách viết biên bản giấy thật; MSG-085 nếu thiếu |
| 10 | Ảnh bằng chứng | Camera/Upload, nhiều ảnh | Bắt buộc ít nhất 1 ảnh | BR-183/184 | MSG-085 nếu thiếu |
| 11 | Nút "Save Violation" | Button (primary) | Disabled khi thiếu field bắt buộc (1, 3, 6, 8, 9, 10) | BR-183/184, BR-188 | Lưu dữ liệu số ngay — **không chờ** có ảnh biên bản đã ký (BR-211) |
| 12 | Nút "Xuất Biên bản (PDF)" | Button (secondary) | Chỉ hiện sau khi đã Save Violation | BR-211 | Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**; sinh câu kết cố định + 3 khoảng chữ ký (Y tế trường học / Nhà thầu / Người chứng kiến — cột thứ 3 luôn để trống tĩnh, không gắn field) — xem Mục "Xuất & Lưu Biên bản đã ký" bên dưới |
| 13 | Ảnh biên bản đã ký (`signedRecordPhotoUrl`) | Upload | Không bắt buộc để Save tại đây; **bắt buộc** trước khi đóng biên bản/gửi School Administration phê duyệt ở `24_RecordViolationResolution.md` — **gate bắt buộc (chặn cứng)**, quyết định người dùng 06/07/2026, không đổi | BR-211 | Tải lên sau khi in — ký tay 2 bên (Y tế trường học/Nhà thầu) — chụp ảnh |

### Cơ chế phân loại BR-183 vs BR-184 (không có toggle thủ công)
- Field 4 (`staffName`) để trống → bản ghi chỉ chịu **BR-183** (vi phạm quy trình/sản phẩm) — không có cá nhân để áp kỷ luật, field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` **không hiện**.
- Field 4 có giá trị → kích hoạt thêm **BR-184/185** (kỷ luật cá nhân — field "Mức độ hình thức kỷ luật" hiện ra ở `24_RecordViolationResolution.md`) và **BR-187** (cảnh báo nghi ngờ tái diễn — so khớp theo đúng chuỗi `staffName` trong 90 ngày gần nhất, best-effort vì `staffName` là text tự do).
- Field 3 (`caterersRepresentativeId`) và field 1/8/9/10 luôn bắt buộc bất kể field 4 có điền hay không — đây là phần "khung biên bản" chung cho mọi trường hợp.
- Khi mở dialog từ 1 `NonConformanceRecord` đã có sẵn (nâng cấp theo BR-183 — ví dụ từ bước kiểm thực 3 bước Không đạt), 1 số field (địa điểm, thời điểm, mô tả sơ bộ) có thể pre-fill từ dữ liệu `NonConformanceRecord` liên kết; khi mở độc lập (Medical Staff tự phát hiện), toàn bộ field trống, nhập từ đầu.
- **Ngữ cảnh thứ 3 — mở từ điều tra sự cố (15/07/2026, Mục 11.5)**: khi mở từ nút "Lập biên bản vi phạm nhân viên liên quan" (`17_InvestigationWorkspace.md`, panel Root Cause), field 5b (`relatedIncidentId`) được pre-fill = `INC-xxx` đang điều tra; các field khác nhập từ đầu (hoặc gợi ý từ nội dung điều tra nếu có).

### Trạng thái màn hình
- **Loading**: spinner trên nút Save Violation khi đang lưu; rà soát trùng tên (best-effort, BR-187) khi field 4 có giá trị.
- **Empty**: không áp dụng (dialog tạo mới).
- **Error**: MSG-004/MSG-005 (lỗi hệ thống chung); MSG-085 khi thiếu field bắt buộc — cảnh báo inline ngay tại field thiếu.
- **Success**: MSG-086 (Toast) khi Save thành công. Nếu field 4 có giá trị và trùng khớp với 1 bản ghi khác cùng mức ≥ MEDIUM trong 90 ngày gần nhất → thêm MSG-088 (banner cảnh báo nghi ngờ tái diễn, best-effort, không chặn lưu — BR-187). Điều hướng tiếp sang `24_RecordViolationResolution.md`. Nếu chưa tải `signedRecordPhotoUrl`, màn 24 sẽ **chặn cứng** nút "Đóng biên bản"/"Gửi phê duyệt" (MSG-095, Error) cho đến khi bổ sung ảnh.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm", hoặc từ 1 `NonConformanceRecord` Không đạt (nâng cấp theo BR-183); thành công → `24_RecordViolationResolution.md`.
**Bố cục**: `Dialog` 1 cột, thứ tự đúng như bảng field ở trên. Field 5 (chức vụ nhân viên) chỉ render khi field 4 có giá trị (client-side, không cần gọi API). Ô "Nội dung biên bản" (field 9) chiếm toàn bộ chiều rộng dialog, chiều cao tối thiểu ~200px, có toolbar định dạng cố định ngay phía trên ô, câu tự sinh từ `occurredAt` hiển thị dạng text tĩnh ngay trên đầu ô, không nằm trong vùng chỉnh sửa được. Nút Save Violation cuối form; nút Xuất Biên bản (PDF) xuất hiện cạnh Save Violation sau khi lưu thành công.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Report Violation" trên Dashboard (Mobile), hoặc từ 1 `NonConformanceRecord` Không đạt; thành công → `24_RecordViolationResolution.md`.
**Bố cục**: trang riêng full-screen, camera (field 10) ưu tiên đặt đầu form theo quy ước cũ của dự án, form dọc, nút Save Violation cố định cuối màn. **Lưu ý cần xác nhận thêm**: toolbar rich text đầy đủ (bảng, căn lề, màu chữ) trên màn hình mobile hẹp có thể không khả thi — đề xuất rút gọn toolbar mobile chỉ còn đậm/nghiêng/gạch chân + danh sách gạch đầu dòng, đội dự án/dev xác nhận trước khi code.

---

## Xuất & Lưu Biên bản đã ký (BR-211, sửa 11/07/2026)

**Bước 1 — Xuất PDF**: sau khi Save Violation thành công, nút "Xuất Biên bản (PDF)" sinh 1 file PDF theo đúng bố cục mẫu `00_Policies/specialreport2173_1.md`, với các điều chỉnh đã chốt:
- Header: tên trường (từ `CURRENT_SCHOOL`) + "Y tế trường học", quốc hiệu tiêu ngữ, tiêu đề **"BIÊN BẢN VI PHẠM"** (đổi từ "BIÊN BẢN SỰ VIỆC" nguyên văn mẫu giấy, 11/07/2026).
- Địa điểm (`locationOfIncident`) + thời điểm lập biên bản (server timestamp lúc bấm "Xuất Biên bản").
- 3 bên trên PDF: "Đại diện Y tế trường học" (`reportedBy`), "Đại diện Nhà thầu" (`caterersRepresentativeId`), "Người chứng kiến" (**cột trống tĩnh, không gắn field dữ liệu nào** — sửa 11/07/2026, trước đây gắn `witnessId`).
- Nội dung sự việc: câu tự sinh "Vào hồi [giờ] ngày [ngày]..." từ `occurredAt` + nội dung biên bản (field 9, rich text).
- Câu kết cố định: "Biên bản được lập thành 01 bản, lưu tại hệ thống SFMS, có giá trị pháp lý như nhau." (điều chỉnh câu chữ chính thức cần đội dự án duyệt).
- 3 khoảng trống chữ ký: "Y TẾ TRƯỜNG HỌC" / "NHÀ THẦU" / "NGƯỜI LÀM CHỨNG" (cột thứ 3 để trống, điền tay ngoài đời nếu cần).

**Bước 2 — In & ký tay**: Medical Staff tải file PDF về, in ra giấy, thu thập chữ ký tay ngoài đời thực (không dùng ký điện tử trên màn hình).

**Bước 3 — Chụp ảnh & tải lên**: quay lại màn hình, dùng field "Ảnh biên bản đã ký" (field 13) để tải ảnh biên bản giấy đã ký. Ảnh này lưu vào `signedRecordPhotoUrl`, gắn với đúng bản ghi vi phạm.

**Trạng thái hiển thị**: badge "Tình trạng biên bản" — *Chưa xuất PDF* → *Đã xuất, chờ ký* → *Đã có ảnh ký xác nhận* (dựa trên việc đã có `signedRecordPhotoUrl` hay chưa).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho dialog "Lập Biên bản Vi phạm", nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên].
Thứ tự field: Địa điểm xảy ra sự việc, Đại diện Y tế trường học (hiển thị tự động, không sửa),
Đại diện Nhà thầu (Select) — KHÔNG có field Người chứng kiến trong form (đã bỏ 11/07/2026),
Nhân viên liên quan (tùy chọn) — khi có giá trị thì hiện thêm Chức vụ nhân viên,
Mức độ nghiêm trọng (Select LOW/MEDIUM/HIGH), Loại vi phạm (Select, tùy chọn),
Thời điểm xảy ra sự việc (DateTimePicker), khối "Nội dung biên bản" là rich text editor có toolbar
định dạng (đậm/nghiêng/gạch chân/cỡ chữ/bảng/căn lề), phía trên ô hiện sẵn dòng tự sinh
"Vào hồi [giờ] ngày [ngày]..." lấy từ Thời điểm xảy ra sự việc, ảnh bằng chứng (bắt buộc ít nhất 1),
nút Save Violation, sau khi lưu hiện thêm nút "Xuất Biên bản (PDF)" (tiêu đề PDF "BIÊN BẢN VI PHẠM").
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM).
Trạng thái cần thể hiện: loading, error (MSG-085 tại field thiếu), success (MSG-086, cảnh báo MSG-088
nếu có Nhân viên liên quan và nghi ngờ tái diễn).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm toggle chọn "loại vi phạm" —
BR-183/BR-184 được suy ra tự động từ việc có điền "Nhân viên liên quan" hay không.
KHÔNG thêm field nhân chứng (witness) trong form nhập liệu.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4639–4659 (UC-179 Record Staff Violation), dòng 3663–3683 (UC-178 Record Process Violation — nay Deprecated, tham khảo lịch sử).
- business_rules.md §18 (BR-183, BR-184, BR-185, BR-186, BR-187, BR-188, BR-190, BR-211).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-086, MSG-088, MSG-095).
- `02_Requirements/Violation_Management_Proposal.md`.
- `00_Policies/specialreport2173_1.md` (mẫu Biên bản Sự việc — căn cứ thiết kế field).
- `review/medical_staff/review_screen_medical_staff.md` Mục 10 (toàn bộ lịch sử quyết định gộp dialog).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.5.1 (field `relatedIncidentId` — liên kết Incident ↔ Violation, đợt 2 15/07/2026).
