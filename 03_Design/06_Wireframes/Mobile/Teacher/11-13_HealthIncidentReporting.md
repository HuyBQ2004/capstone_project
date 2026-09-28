# Wireframe — Health Incident Reporting (nhóm màn hình Teacher, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/Teacher/` — 3 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Mobile/Teacher/11-13_HealthIncidentReporting.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` (Dash → Report New Health Incident Button → Incident Form → submit → Incident Submitted Confirmation -.-> Medical Staff Investigation Workspace; Dash → View History → Incident History).

---

## 1. Danh sách 3 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 11 | Upload Evidence | `11_UploadEvidence.md` | Full-screen camera/preview Material 3 (khác Dialog nổi của Web) |
| 12 | Incident Form | `12_IncidentForm.md` | Form 1 cột cuộn dọc, 2 nút cố định đáy màn |
| 13 | Incident History | `13_IncidentHistory.md` | AppBar + Card list + BottomNav |

Tổng cộng: **3 `<div class="screen">`** (Incident Form, transient "Incident Submitted" confirmation, Incident History) + **1 modal overlay** (Upload Evidence, dạng full-screen camera theo đúng Platform Layout Mobile).

Lưu ý: "Incident Submitted Confirmation" trong mermaid flow (`07_Teacher_Screen_Flow.md`) không có file Screen Spec số thứ tự riêng (tổng 13 màn Teacher không tính node này riêng) — dựng dưới dạng 1 màn hình chuyển tiếp ngắn (trạng thái Success/Toast MSG-048) trước khi vào Incident History, không phải 1 Screen Spec bổ sung ngoài phạm vi.

---

## 2. Đối chiếu với Screen Specification

### 11. Upload Evidence
- [x] Đủ field: 2 nút lớn Take Photo/Choose from Gallery, danh sách thumbnail cuộn ngang, Attach & Continue Button cố định đáy — đúng Platform Layout Mobile ("Toàn màn hình camera/preview Material 3").
- [x] Trạng thái: MSG-008 (quá dung lượng)/MSG-009 (sai định dạng) ghi chú text.

### 12. Incident Form
- [x] Đủ 12 field Business Logic giống bản Web (Student, Class, Number of Affected Students, Suspected Dishes, Ingestion Date, Symptoms, Severity, Description, Photos, Cancel, Submit) — bố cục 1 cột cuộn dọc, 2 nút cố định đáy màn đúng Platform Layout Mobile.

### 13. Incident History
- [x] Đủ field: Assessment Filter Tabs (Chip), Incident Log Card list (thay Table) — đúng 3 dòng, đúng Platform Layout Mobile ("danh sách Card cuộn dọc").

---

## 3. Ghi chú kỹ thuật

- Khung điện thoại 390px, AppBar/BottomNav theo chuẩn Teacher Mobile.
- Grayscale, wired-elements, Patrick Hand, sketchy border.
- Dữ liệu mẫu nhất quán với bản Web.

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới.
