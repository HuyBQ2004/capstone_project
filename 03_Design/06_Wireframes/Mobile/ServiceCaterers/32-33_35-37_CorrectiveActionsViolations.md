# Wireframe — Corrective Actions & Violations (nhóm màn hình Service Caterers, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/ServiceCaterers/32,33,35,36,37_*.md` — 5 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), khung phone-frame 390px.
**File/Link**: `03_Design/06_Wireframes/Mobile/ServiceCaterers/32-33_35-37_CorrectiveActionsViolations.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` — nhánh `Grid → CorrDash(32) → CorrForm(33)/IncidentInfo(35)`; `InspDash -.->|"InspectionStep.fail()"| ProcessViol(36)`, `ViolDash --> ProcessViol`; Service Caterers cũng có quyền `RecordStaffViolation(37)` theo BR-184.

**Lưu ý phạm vi quan trọng**: Màn **34 (View Inspection Records Log)** thuộc cùng nhóm "Sự cố & Vi phạm" theo mã số nhưng đã được build ở `06_Wireframes/Mobile/ServiceCaterers/15-20_34_ThreeStepInspection.html` (do liên kết chặt với luồng Three-Step Inspection hơn) — **không lặp lại tại đây**, tổng số màn của actor Service Caterers vẫn đủ 37/37 khi tính cả file đó.

---

## 1. Danh sách 5 màn hình chính + 1 màn phụ

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 32 | Corrective Actions Dashboard | `32_CorrectiveActionsDashboard.md` | Screen `#capadash` (entry) |
| 33 | Submit Corrective Action Results Form | `33_SubmitCorrectiveActionResultsForm.md` | Screen `#submitresults` |
| 35 | View Incident Information Detail | `35_ViewIncidentInformationDetail.md` | Screen `#incidentlist` + `#incidentdetail` |
| 36 | Record Process Violation | `36_RecordProcessViolation.md` | Screen `#processviol` |
| 37 | Record Staff Violation | `37_RecordStaffViolation.md` | Screen `#staffviol` |

---

## 2. Đối chiếu với Screen Specification

### 32. Corrective Actions Dashboard
- [x] Đủ field: card list 2 CAPA (1 Late Submission — badge warning đúng trọng tâm MSG-023, 1 In Progress), nút "Cập nhật Tiến độ" — đúng 5 dòng Business Logic. Bổ sung link text (không phải field mới) trỏ tới Incident Detail đúng theo "Điều hướng tiếp" ghi trong Entry/Exit points, và 2 nút Vi phạm (36/37) đặt tại đây do không có Screen Spec "Violation Dashboard" riêng (xem mục 5).

### 33. Submit Corrective Action Results Form
- [x] Đủ field: header CAPA read-only, ghi chú hoàn thành, upload tài liệu bằng chứng, upload Health Department Clearance (điều kiện), cảnh báo MSG-004 chặn nộp (dựng ở trạng thái Failed đúng trọng tâm AI Generation Prompt), nút "Nộp Kết quả" — đúng 5 dòng Business Logic.

### 35. View Incident Information Detail
- [x] Đủ field: card list 2 sự cố (High/Medium — đủ 2 mức nghiêm trọng, badge critical riêng biệt với danger), badge Quarantine (BR-149), icon Xuất Dữ liệu, trang chi tiết khi chạm card (`#incidentdetail`) — đúng 5 dòng Business Logic.

### 36. Record Process Violation
- [x] Đủ field: ngữ cảnh pre-filled (từ InspectionStep.fail() của nhóm Three-Step Inspection đã build), người phát hiện tự động, mô tả vi phạm, ảnh bằng chứng, radio mức độ (Medium được chọn để minh hoạ điều kiện witness), select nhân chứng bắt buộc — đúng 7 dòng Business Logic.

### 37. Record Staff Violation
- [x] Đủ field: tên nhân viên (text tự do, không phải dropdown vì không có tài khoản User), chức vụ tuỳ chọn, loại vi phạm (gồm OVERRIDE_ABUSE), mô tả, ảnh bằng chứng, mức độ, ngày giờ, banner MSG-088 tái diễn (dựng ở trạng thái Warning không chặn — đúng trọng tâm AI Generation Prompt), nút lưu — đúng 9 dòng Business Logic.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, khung `.phone-frame` (390px), 6 `<div class="screen">` (bao gồm `#incidentdetail` phụ).
- Badge `critical` dùng viền `4px double` để phân biệt trực quan với `danger` (viền `3px solid`) đúng yêu cầu Design System "token critical riêng biệt với danger".

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 5 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **32_CorrectiveActionsDashboard.md — không có field/nút chính thức điều hướng sang View Incident Information Detail**: "Entry/Exit points" của cả 2 Screen Spec (32 và 35) đều xác nhận có luồng điều hướng qua lại, nhưng Business Logic của 32 chỉ có 5 field, không field nào là nút "Xem Sự cố". Wireframe tạm dùng 1 link text (không phải field mới, không phải nút chính) để không tự thêm nghiệp vụ — cần Thảo xác nhận có cần bổ sung field chính thức.
2. **36_RecordProcessViolation.md và 37_RecordStaffViolation.md — không có Screen Spec "Violation Dashboard" (ViolDash) dù ScreenFlow có vẽ node này**: `04_Service_Caterers_Screen_Flow.md` có node `Grid --> ViolDash["Violation Management"]` nhưng không có file Screen Spec nào trong 37 màn mô tả riêng cho node này — cần Thảo xác nhận có cần bổ sung Screen Spec riêng (danh sách vi phạm đã lập theo actor) hay 2 form 36/37 là điểm vào trực tiếp duy nhất từ Grid Menu Items như wireframe đã giả định.
3. **35_ViewIncidentInformationDetail.md — field #3 "Chi tiết sự cố" là "trang riêng" nhưng không có Screen Spec con**: cùng loại phát hiện lặp lại đã báo ở các nhóm trước (Food Sample Retention, Meal Service) — đề xuất Thảo áp dụng quy tắc chung đã đề xuất ở `Mobile/ServiceCaterers/24-27_MealServiceSpecialMeals.md` mục 5.
