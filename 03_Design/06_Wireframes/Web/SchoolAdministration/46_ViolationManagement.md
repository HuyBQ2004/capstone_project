# Wireframe — Violation Management (nhóm 4 màn, School Administration)

**Nguồn**: `05_ScreenSpecifications/Web/SchoolAdministration/46_RecordViolationResolution.md`, `47_ViolationRegistry.md`, `48_ApproveRejectViolationResolution.md`, `49_WeeklyOverrideAbuseReport.md`
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), hand-drawn/Balsamiq style, vanilla HTML/JS SPA (không router)
**File/Link**: `06_Wireframes/Web/SchoolAdministration/46_ViolationManagement.html`

> **Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

## Danh sách 4 màn hình trong file

| STT | Màn hình | ID `<div class="screen">` | File Screen Spec nguồn | UC |
| --- | --- | --- | --- | --- |
| 1 | Violation Registry | `screen-registry` | `47_ViolationRegistry.md` | UC-181 |
| 2 | Violation Detail (Record Violation Resolution — bản xem School Admin) | `screen-detail` | `46_RecordViolationResolution.md` | UC-180 (Secondary Actor: School Admin) |
| 3 | Approve/Reject Violation Resolution | `screen-approve` | `48_ApproveRejectViolationResolution.md` | UC-182 |
| 4 | Weekly Override Abuse Report | `screen-override` | `49_WeeklyOverrideAbuseReport.md` | UC-183 |

## Ghi chú quan trọng — sai lệch tên gọi màn 2 so với `04_ScreenFlow`

File nguồn `46_RecordViolationResolution.md` có tên gọi gốc "Record Violation Resolution", nhưng **nội dung thật của Screen Spec (đã tự xác nhận trong chính file, dòng 8) khẳng định School Administration KHÔNG tự lập biên bản** — vai trò thật là **xem lại (read-only) + có nút "Chuyển sang duyệt"**. Hành động "lập biên bản" (Record Resolution) thuộc về Medical Staff (Primary Actor UC-180, xem `Web/MedicalStaff/24_RecordViolationResolution.md`).

Do đó màn số 2 trong wireframe này được dựng **đúng theo nội dung thật** (không theo tên gọi file): hiển thị "Violation Detail (Resolution)" ở chế độ chỉ xem, có banner cảnh báo giải thích rõ vai trò read-only, KHÔNG có bất kỳ form nhập liệu nào. Đây là điểm khớp với mô tả sai lệch actor được nêu trong yêu cầu — cần Nguyễn Phương Thảo xác nhận lại trong bước QA (đổi tên file/screen spec cho đúng bản chất, hoặc giữ nguyên tên nhưng làm rõ ghi chú actor).

## Sơ đồ điều hướng đã hiện thực (JS `showScreen(id)`, không router)

```
Violation Registry (screen-registry)
  --[click 1 dòng có resolution]--> Violation Detail/Resolution (screen-detail)
      --[nút "Chuyển sang duyệt", chỉ hiện khi Pending Approval Medium/High]--> Approve/Reject (screen-approve)
          --[Approve]--> MSG-089 banner --> quay về Violation Registry (screen-registry)
          --[Reject + lý do bắt buộc]--> note text "trả về Medical Staff revision" (không dựng màn Medical Staff thật)
Weekly Override Abuse Report (screen-override)
  --[drill down 1 dòng]--> Violation Registry (screen-registry)
  --[Export]--> mô phỏng Export CSV/PDF (banner tạm thời, không có màn riêng)
```

Sidebar: chỉ nhóm "Vi phạm / Violation" có thể click (`nav-item clickable`); các nhóm màn hình khác (Incident, Compliance, Classroom, v.v.) hiển thị dạng `nav-item static` màu xám, ghi chú "xem file wireframe nhóm khác" — đúng quy tắc chỉ nhóm Violation Management click được trong file này.

## Đối chiếu với Screen Specification

### Màn 1 — Violation Registry (`47_ViolationRegistry.md`)
- [x] Đủ toàn bộ field: Filter Loại (Process/Staff/Partner), Filter Severity, Filter Status, Bảng (Type/Actor-Staff Name/Severity/Status/Reported Date), Badge Severity (danger/warning/info), Badge/icon cảnh báo tái diễn (best-effort, BR-187/MSG-088), Action xem lịch sử xử lý (row click)
- [x] Đúng Design System grayscale/mono — badge phân biệt bằng viền (solid/dashed/thin) + icon, không dùng màu thật
- [x] Thể hiện đủ trạng thái: Success (bảng đầy đủ + badge cảnh báo tái diễn), Empty (ghi chú dòng "Không có vi phạm nào khớp bộ lọc")

### Màn 2 — Violation Detail / Record Violation Resolution bản xem School Admin (`46_RecordViolationResolution.md`)
- [x] Đủ toàn bộ field: Vi phạm gốc liên kết (text + link), Hành động xử lý đề xuất (đọc-only), Người xử lý, Badge mức kỷ luật, Badge trạng thái (Pending Approval/Approved/Closed), Nút "Chuyển sang duyệt" (chỉ hiện khi Pending Approval Medium/High)
- [x] Đúng Design System — KHÔNG thêm form nhập liệu (School Admin chỉ xem), banner cảnh báo làm rõ vai trò read-only
- [x] Thể hiện đủ trạng thái: Success (biên bản đầy đủ); Error (MSG-085 thiếu field) ghi chú trong manifest, không dựng riêng màn Error để tránh trùng lặp — đã note trong Screen Spec nguồn

### Màn 3 — Approve/Reject Violation Resolution (`48_ApproveRejectViolationResolution.md`)
- [x] Đủ toàn bộ field: Chi tiết vi phạm gốc, Đề xuất xử lý + mức kỷ luật, Nút Approve (primary), Nút Reject (destructive) + Textarea lý do bắt buộc
- [x] Đúng Design System — nút Reject dùng style "danger" (viền dày, không dùng màu đỏ thật), Approve dùng `btn-primary`
- [x] Thể hiện đủ trạng thái: Error (AF1 — thiếu lý do khi Reject → validation chặn submit, hiển thị badge lỗi), Success (MSG-089 khi Approve, đóng vi phạm gốc + áp dụng kỷ luật + quay về Violation Registry); Reject thành công → note rõ "trả về Medical Staff revision"

### Màn 4 — Weekly Override Abuse Report (`49_WeeklyOverrideAbuseReport.md`)
- [x] Đủ toàn bộ field: Filter theo tuần (DatePicker chọn tuần), Bảng báo cáo (Staff Name/Override Count/Context/Severity), Nút Export, Action drill down (row click)
- [x] Đúng Design System — badge severity nhấn mạnh mức độ (grayscale)
- [x] Thể hiện đủ trạng thái: Success (bảng đầy đủ + Export mô phỏng), Empty (ghi chú "Không có trường hợp lạm dụng override nào trong tuần này")

## Nguồn dữ liệu
- `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md` (quy tắc bắt buộc hand-drawn/Balsamiq)
- `03_Design/04_ScreenFlow/02_School_Administration_Screen_Flow.md` (sơ đồ mermaid điều hướng UC-181/182/183)
- `03_Design/05_ScreenSpecifications/Web/SchoolAdministration/46_RecordViolationResolution.md`, `47_ViolationRegistry.md`, `48_ApproveRejectViolationResolution.md`, `49_WeeklyOverrideAbuseReport.md`
- `03_Design/00_UI_Design_Documentation_Plan.md` §6.5 (template manifest Wireframe)
