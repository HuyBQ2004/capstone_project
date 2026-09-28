# Wireframe — Health Incident Reporting (nhóm màn hình Teacher, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/Teacher/` — 3 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/Teacher/11-13_HealthIncidentReporting.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md` (Health Incident Report Form → submit → Incident Submitted Confirmation → routed to Medical Staff; Dash → View History → Incident History). Attached Photos trong Incident Form mở `11_UploadEvidence.md` dạng Dialog.

---

## 1. Danh sách 3 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 11 | Upload Evidence | `11_UploadEvidence.md` | Dialog nổi trên Incident Form — vào từ khối "Attached Photos" |
| 12 | Incident Form | `12_IncidentForm.md` | Node gốc của nhóm |
| 13 | Incident History | `13_IncidentHistory.md` | Sidebar riêng, xem lại báo cáo đã gửi (read-only) |

Tổng cộng: **2 `<div class="screen">`** (Incident Form, Incident History) + **1 modal overlay** (Upload Evidence, dạng Dialog nổi theo đúng Platform Layout).

---

## 2. Đối chiếu với Screen Specification

### 11. Upload Evidence
- [x] Đủ field: File Upload Input (kéo-thả + chọn file), File Size Validation (10MB, MSG-008), File Type Validation (PDF/JPG/PNG, MSG-009), Photo Preview Thumbnail List (kèm nút xoá), Attach & Continue Button — đúng 5 dòng Business Logic.
- [x] Dựng dạng Dialog/Sheet nổi trên Incident Form, đúng Platform Layout.
- [x] Trạng thái: Empty ("Chưa có ảnh nào được đính kèm"), Error (MSG-008/009), Success (ảnh hiển thị đã tải lên) — đều có ghi chú/minh hoạ trong modal.

### 12. Incident Form
- [x] Đủ field: Page Header, Student Input (multi-select), Class Input (readonly), Number of Affected Students, Suspected Dishes, Ingestion Date, Detected Symptoms Checkboxes, Severity Level Radio, Incident Description, Attached Photos Component, Cancel Button, Submit Button — đúng 12 dòng Business Logic, bao gồm cả 3 field bổ sung theo Normal Flow UC-074 (Number of Affected Students, Suspected Dishes, Ingestion Date) mà Screen Spec đã ghi chú rõ nguồn gốc bổ sung.
- [x] Bố cục form 2 cột đúng Platform Layout (Student/Class/Symptoms trái — Severity/Description/Photos phải).
- [x] Trạng thái: Error (MSG-023, thiếu field bắt buộc) ghi chú text; Success (Toast MSG-048) → điều hướng Incident History.

### 13. Incident History
- [x] Đủ field: Page Header, Assessment Filter Tabs (All/Pending Assessment/Under Assessment), Incident Log Table (học sinh/triệu chứng/mức độ/ngày báo cáo/badge trạng thái) — đúng 3 dòng Business Logic.
- [x] KHÔNG có action chỉnh sửa/đóng sự cố, đúng yêu cầu Screen Spec (Teacher chỉ xem lại báo cáo của chính mình).
- [x] Trạng thái: Empty ("Bạn chưa gửi báo cáo sự cố nào") ghi chú text; Loading/Error tham chiếu MSG-004/005.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA, 2 `<div class="screen">` + 1 modal (Upload Evidence), điều hướng `showScreen(id)`.
- Grayscale/mono, wired-elements CDN (`wired-combo`, `wired-select`, `wired-checkbox`, `wired-radio-group`, `wired-calendar`, `wired-textarea`), Patrick Hand, sketchy border/dropzone.
- Sidebar chỉ nhóm "Sự cố Sức khỏe" active-able; Teacher Home/Phân phát Bữa ăn disabled kèm ghi chú trỏ sang nhóm `01-07` và `08-10`.
- Dữ liệu mẫu (Trần Gia Bảo — dị ứng đậu phộng, #INC-0231) nhất quán với 2 nhóm còn lại.

## 4. Trạng thái nguồn

Nguồn Confirmed. Screen Spec `12_IncidentForm.md` tự ghi chú đã chủ động bổ sung 3 field theo Normal Flow UC-074 (không phải phát hiện mới của Wireframe) — không cần báo cáo thêm.
