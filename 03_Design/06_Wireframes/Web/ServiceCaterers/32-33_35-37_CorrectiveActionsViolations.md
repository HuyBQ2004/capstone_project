# Wireframe — Corrective Actions & Violations (nhóm màn hình Service Caterers, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/ServiceCaterers/32,33,35,36,37_*.md` — 5 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), layout Sidebar + Topbar giống mẫu `Web/SchoolAdministration/`.
**File/Link**: `03_Design/06_Wireframes/Web/ServiceCaterers/32-33_35-37_CorrectiveActionsViolations.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` — nhánh `Dash → CorrDash(32) → CorrForm(33)/IncidentInfo(35)`; `InspDash -.->|"InspectionStep.fail()"| ProcessViol(36)`; `CorrDash --> ProcessViol`.

**Lưu ý phạm vi quan trọng**: Màn **34 (View Inspection Records Log)** đã build ở `06_Wireframes/Web/ServiceCaterers/15-20_34_ThreeStepInspection.html` — **không lặp lại tại đây** (side-link tương ứng đánh dấu `disabled` kèm ghi chú). Tổng số màn actor Service Caterers vẫn đủ 37/37 khi tính cả file đó.

---

## 1. Danh sách 5 Screen Spec → 5 screen + 1 sheet trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 32 | Corrective Actions Dashboard | `32_CorrectiveActionsDashboard.md` | Screen `#capadash` (entry) |
| 33 | Submit Corrective Action Results Form | `33_SubmitCorrectiveActionResultsForm.md` | Screen `#submitresults` |
| 35 | View Incident Information Detail | `35_ViewIncidentInformationDetail.md` | Screen `#incidentlist` + Sheet chi tiết bên phải |
| 36 | Record Process Violation | `36_RecordProcessViolation.md` | Screen `#processviol` |
| 37 | Record Staff Violation | `37_RecordStaffViolation.md` | Screen `#staffviol` |

---

## 2. Đối chiếu với Screen Specification

### 32. Corrective Actions Dashboard
- [x] Đủ field: Table 2 CAPA (Late Submission/In Progress), nút "Cập nhật Tiến độ" — đúng 5 dòng Business Logic. 2 nút Vi phạm (36/37) đặt tại đây do không có Screen Spec "Violation Dashboard" riêng (xem mục 5, kế thừa phát hiện từ bản Mobile).

### 33. Submit Corrective Action Results Form
- [x] Đủ field: header CAPA, textarea ghi chú hoàn thành, upload tài liệu đa file + preview, upload Health Department Clearance (điều kiện), cảnh báo MSG-004 chặn (Failed), nút "Nộp Kết quả" — đúng 5 dòng Business Logic.

### 35. View Incident Information Detail
- [x] Đủ field: Table 2 sự cố (High/Medium), badge critical riêng biệt danger, nút Xuất Dữ liệu, Sheet chi tiết khi click dòng (badge Quarantine BR-149) — đúng 5 dòng Business Logic, bố cục Table + Sheet đúng Platform Layout — Web.

### 36. Record Process Violation
- [x] Đủ field: ngữ cảnh pre-filled, người phát hiện, mô tả, upload ảnh, select mức độ (Medium), select nhân chứng bắt buộc — đúng 7 dòng Business Logic.

### 37. Record Staff Violation
- [x] Đủ field: tên nhân viên (text tự do), chức vụ, loại vi phạm (gồm OVERRIDE_ABUSE), mô tả, upload ảnh, mức độ, ngày giờ, banner MSG-088 (warning, không chặn), nút lưu — đúng 9 dòng Business Logic.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS; Sidebar nhóm "Sự cố & Vi phạm" gồm 4 mục active + 1 mục `disabled` trỏ rõ ràng sang file `15-20_34_ThreeStepInspection.html` cho màn 34.
- Sheet trượt phải cho chi tiết sự cố dùng `.sheet-overlay`/`.sheet-panel` cùng pattern các nhóm trước.
- Badge `critical` viền `4px double`, phân biệt trực quan với `danger` viền `3px solid`.

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 5 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **Kế thừa từ bản Mobile** (`Mobile/ServiceCaterers/32-33_35-37_CorrectiveActionsViolations.md` mục 5): (a) 32 thiếu field chính thức điều hướng sang 35; (b) không có Screen Spec "Violation Dashboard" (ViolDash) dù ScreenFlow có vẽ node — 36/37 được giả định là điểm vào trực tiếp; (c) field #3 của 35 ("Chi tiết sự cố") không có Screen Spec con riêng.
2. **Tổng hợp cuối cùng cho toàn bộ 37 màn Service Caterers**: đây là nhóm cuối trong 7 nhóm wireframe đã build (07-14, 15-20+34, 21-23, 01-06, 24-27, 28-31, 32-33+35-37) — tổng số phát hiện "field mô tả trang/form riêng nhưng thiếu Screen Spec 1-1" đã lặp lại **5 lần** xuyên suốt dự án (23, 24, 26, 32, và ViolDash ở đây) — khuyến nghị mạnh Thảo đưa vào checklist QA chung mục 6 của assignment plan như 1 quy tắc kiểm tra bắt buộc trước khi khoá Screen Specification.
