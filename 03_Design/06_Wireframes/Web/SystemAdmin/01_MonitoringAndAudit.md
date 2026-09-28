# Wireframe — Monitoring & Audit (System Admin)

**Nguồn**: `05_ScreenSpecifications/Web/SystemAdmin/01_AuditLogs.md`, `17_SystemActivityMonitoring.md`, `18_SystemStatusScreen.md`, `19_ErrorLogsScreen.md`, `20_UsageStatisticsScreen.md`
**Công cụ sinh**: Thủ công — theo skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), quy tắc kỹ thuật tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/SystemAdmin/01_MonitoringAndAudit.html` (single-file SPA, 5 `<div class="screen">`, điều hướng bằng `showScreen(id)`, không router)

> **Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/01_System_Admin_Screen_Flow.md` (nhánh Dashboard → Audit Logs (UC-106); Dashboard → System Activity Monitoring (UC-057); Dashboard → System Status Screen (UC-149, kèm tab Backup Management); Dashboard → Error Logs Screen (UC-111); Dashboard → Usage Statistics Screen (UC-150)). Sidebar tham chiếu `03_Design/00_Foundation/Navigation_Guidelines.md` mục 1 (cấu trúc menu 2 cấp theo actor).

---

## 1. Danh sách 5 màn hình trong file HTML

| STT | Màn hình | ID screen trong SPA | File Screen Spec nguồn | UC liên quan |
| --- | --- | --- | --- | --- |
| 01 | Audit Logs | `screen-audit-logs` | `01_AuditLogs.md` | UC-106 |
| 17 | System Activity Monitoring | `screen-activity-monitoring` | `17_SystemActivityMonitoring.md` | UC-057 |
| 18 | System Status Screen (+ Backup Management tab) | `screen-system-status` | `18_SystemStatusScreen.md` + `14_BackupManagement.md` | UC-149 (BR-160) |
| 19 | Error Logs Screen (+ Sheet chi tiết lỗi) | `screen-error-logs` | `19_ErrorLogsScreen.md` | UC-111 |
| 20 | Usage Statistics Screen | `screen-usage-statistics` | `20_UsageStatisticsScreen.md` | UC-150 |

**Ghi chú Backup Management**: `14_BackupManagement.md` (Campus Config đã loại bỏ, Backup Management không có UC riêng) — theo đúng ghi chú `01_System_Admin_Screen_Flow.md`: "Backup Management là 1 panel hiển thị trạng thái backup scheduler (BR-160) bên trong UC-149 (View System Status), không phải luồng tạo backup thủ công". Dựng dạng Tab "Backup Management" ngay trong màn System Status Screen (`#tab-panel-backup`), không tách screen riêng.

**Ghi chú điều hướng**: Sidebar liệt kê đủ các nhóm chức năng của toàn bộ actor System Admin (Tổng quan, Quản lý Người dùng & Vai trò, Quản lý Trường học, Cấu hình Hệ thống, Giám sát & Audit, Báo cáo & Thống kê), nhưng **chỉ nhóm "Giám sát & Audit"** (Audit Logs, System Activity Monitoring, System Status Screen, Error Logs Screen, Usage Statistics Screen) là clickable trong file này. Các nhóm khác hiển thị dạng disabled kèm ghi chú "(xem file wireframe nhóm khác)" — đúng ngoài phạm vi màn được giao.

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 01. Audit Logs (`01_AuditLogs.md`, UC-106)
- [x] Đủ 7 field theo bảng Business Logic: (1) Filter Date range (mặc định 7 ngày gần nhất), (2) Filter User ID/Tên (search input), (3) Filter Action category (Select: Create/Update/Delete/Approve/Reject/Login...), (4) Filter IP Address (input), (5) Bảng đọc-only (Timestamp/User/IP Address/Action/Resource/Status — cột, không cho edit/xoá — BR-162, BR-172), (6) Nút "Export CSV/PDF" ([Giả định — cần team xác nhận], đã ghi chú rõ trong wireframe), (7) Phân trang — không thêm field ngoài bảng.
- [x] Đúng Design System *(ghi chú: bản wireframe hand-drawn/grayscale theo skill wireframer, CHƯA áp Design System thật — badge success/danger phân biệt bằng viền + icon doodle thay vì màu; ghi chú "Bảng chỉ đọc — không có hành động Edit/Delete trên từng dòng (BR-162 nhật ký bất biến, BR-172 bảo vệ toàn vẹn)" rõ ràng cuối bảng)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** là trạng thái chính — 7 dòng mẫu dữ liệu thực tế với đủ loại Action/Status; **Empty**: ghi chú text "Không có bản ghi audit nào khớp bộ lọc hiện tại" dưới bảng (không dựng bảng rỗng riêng); **Loading/Error**: ghi chú MSG-004/MSG-005 áp dụng chung theo Screen Spec; bảng được khẳng định read-only (BR-162, ADR-004).
- [x] KHÔNG thêm field/nghiệp vụ ngoài Screen Specification nguồn.

### 17. System Activity Monitoring (`17_SystemActivityMonitoring.md`, UC-057)
- [x] Đủ 6 field theo bảng Business Logic: (1) Chart CPU load (line chart SVG inline), (2) Chart RAM load (line chart SVG inline), (3) Chart Disk I/O (line chart SVG inline), (4) Card "Active user sessions" (số liệu + ghi chú cập nhật gần thời gian thực), (5) Card "Database query pool load" (wired-progress gauge), (6) Card/Link "Báo cáo tổng hợp hàng tuần" (BR-165) — không thêm field ngoài bảng.
- [x] Đúng Design System *(ghi chú: chart dùng SVG polyline doodle thay recharts thật — đúng tinh thần wireframe hand-drawn; card số liệu dùng kpi-value + kpi-label pattern theo SKILL.md)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** là trạng thái chính — chart/card đầy đủ số liệu mẫu hợp lý (CPU 47%, RAM 63%, Disk 12 MB/s, 1,284 sessions, 68% DB pool); **Loading/Error**: không dựng hình riêng (Screen Spec ghi "Loading: Skeleton cho từng chart/card" — chỉ ghi chú); **Empty**: không áp dụng (Screen Spec ghi "Empty: Không áp dụng").
- [x] KHÔNG thêm field/nghiệp vụ ngoài Screen Specification nguồn.

### 18. System Status Screen + Backup Management tab (`18_SystemStatusScreen.md` + `14_BackupManagement.md`, UC-149)
- [x] Đủ 3 field System Status Screen: (1) Health-check dashboard các dịch vụ lõi (card grid — Database, Email Service, Firebase, Gemini AI, GPS Service — badge success/danger + thời gian kiểm tra), (2) Panel Backup Management (bundled, dùng wired-tabs — tab "Health Check" + tab "Backup Management"), (3) Nút "Export" (CSV/PDF, AF1) — không thêm field ngoài bảng.
- [x] Panel Backup Management (14_BackupManagement.md) — dựng đủ: lịch backup (Chủ nhật 02:00), trạng thái lần gần nhất (badge success/danger), vị trí lưu trữ (cloud off-site), dung lượng sử dụng (wired-progress 92% — kèm badge cảnh báo), bảng lịch sử backup gần đây — đúng ghi chú "panel không phải luồng tạo backup thủ công".
- [x] Đúng Design System *(ghi chú: card badge phân biệt bằng viền + icon; wired-tabs dùng đúng theo SKILL.md component rule; cảnh báo 92% dung lượng dùng badge warning viền đứt nét)*.
- [x] Đủ trạng thái tối thiểu: **Error** thể hiện qua card Firebase badge danger + thời gian mất kết nối; cảnh báo backup 92% badge warning; **Success** thể hiện qua card Database/Email/Gemini AI/GPS badge success; **Loading**: không dựng hình riêng (ghi chú text); **Empty**: không áp dụng.
- [x] KHÔNG thêm field/nghiệp vụ ngoài Screen Specification nguồn (đặc biệt: không có field bán kính geofence/Campus Config — đã chính thức loại bỏ theo quyết định 2026-07-01).

### 19. Error Logs Screen (`19_ErrorLogsScreen.md`, UC-111)
- [x] Đủ 5 field theo bảng Business Logic: (1) Filter theo Severity (Select: Critical/High/Medium/Low), (2) Filter theo Date range (DatePicker range — input), (3) Bảng danh sách lỗi (Table: Timestamp/Error Message/Severity/Module-Class/Path — row-clickable), (4) Badge Severity (danger=Critical/High, warning=Medium, info=Low), (5) Panel chi tiết lỗi (Sheet trượt từ phải khi click 1 dòng — hiển thị: Thời gian, Mức độ, Module/Class, Path, Stack trace, Request metadata) — không thêm field ngoài bảng.
- [x] Đúng Design System *(ghi chú: Sheet chi tiết lỗi dùng `.modal-overlay` + `.sheet` theo SKILL.md pattern; stack trace dùng monospace font thay vì code block thật; badge phân biệt bằng viền đậm/đứt nét/chấm + icon, không dùng màu)*.
- [x] Đủ trạng thái tối thiểu: **Success** thể hiện qua 4 dòng lỗi mẫu đủ 4 severity + Sheet chi tiết mở sẵn; **Empty**: ghi chú text "Không có lỗi nào trong khoảng thời gian đã chọn" — phân biệt rõ đây là trạng thái tích cực theo Screen Spec; **Loading**: ghi chú text; **Error**: MSG-004/MSG-005 ghi chú text.
- [x] KHÔNG thêm field/nghiệp vụ ngoài Screen Specification nguồn.

### 20. Usage Statistics Screen (`20_UsageStatisticsScreen.md`, UC-150)
- [x] Đủ 6 field theo bảng Business Logic: (1) Filter theo Date range (DatePicker range), (2) Filter theo Role (Select: System Admin/School Administration/Teacher/Parent/Service Caterer), (3) Chart Active users theo ngày/tuần (bar chart SVG inline, 7 cột — đỉnh 2,140 active users), (4) Bảng Top use case được gọi nhiều nhất (Table rút gọn: Use case/Lượt gọi — 5 dòng mẫu), (5) Chart Dung lượng lưu trữ sử dụng (gauge SVG circle — 65% = 325 GB/500 GB), (6) Chart API request count (line chart SVG inline — 128,450 requests/7 ngày) — không thêm field ngoài bảng.
- [x] Đúng Design System *(ghi chú: chart dùng SVG doodle thay recharts thật — phù hợp wireframe hand-drawn; gauge circle vẽ bằng SVG path circle stroke-dasharray; bảng Top use case dùng wf-table; filter bar đặt trên cùng theo Platform Layout Screen Spec)*.
- [x] Đủ trạng thái tối thiểu: **Success** là trạng thái chính — chart/card/bảng đầy đủ số liệu mẫu hợp lý; **Loading/Error**: ghi chú text tham chiếu MSG-004/MSG-005; **Empty**: không áp dụng (Screen Spec ghi "luôn có ít nhất số liệu 0 trong khoảng thời gian chọn").
- [x] KHÔNG thêm field/nghiệp vụ ngoài Screen Specification nguồn.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS, 5 `<div class="screen" id="...">` (screen-audit-logs/screen-activity-monitoring/screen-system-status/screen-error-logs/screen-usage-statistics), điều hướng bằng hàm `showScreen(id)` thuần JS (không router). Các nav-item trong Sidebar gọi `showScreen()` và cập nhật trạng thái `active`.
- Grayscale/mono tuyệt đối; `wired-elements` nhúng qua CDN (`unpkg.com/wired-elements?module`); font `Patrick Hand` (Google Fonts); nền graph-paper (`radial-gradient(#d7d7d7 1px, transparent 1px) 20px 20px`); sketchy border trick (`border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px`) áp dụng cho toàn bộ card/table-wrap/button/modal/sheet.
- Nút dùng `<button class="btn-primary/btn-secondary">` (KHÔNG dùng `<wired-button>`) theo đúng SKILL.md §3 "Buttons" rule.
- Toàn bộ icon là SVG line-art doodle inline tự vẽ (symbol định nghĩa trong `<svg width="0" height="0" style="position:absolute"><defs>...</defs></svg>`, dùng qua `<use href="#ic-..."/>`). Bộ icon bao gồm: ic-dashboard, ic-user, ic-school, ic-gear, ic-eye, ic-chart, ic-report, ic-log, ic-server, ic-error, ic-stats, ic-backup, ic-filter, ic-calendar, ic-ip, ic-download, ic-warning, ic-cloud, ic-history, ic-lock, ic-plug, ic-search, ic-close, ic-pulse, ic-doc, ic-bell — KHÔNG có emoji.
- Badge success/warning/danger/info phân biệt bằng kiểu viền (liền/đứt nét/đậm 2×/chấm) + pseudo-element text icon (✓/!/✗/i), không dùng màu nền.
- Sheet chi tiết lỗi (màn 19) dùng `.modal-overlay` + `.sheet` slide từ phải — đóng bằng `.sheet-close` button, nội dung đổ từ `data-*` attribute dòng bảng qua hàm JS `openErrorDetail(row)`.
- wired-tabs (màn 18) dùng `<wired-tabs>` + `<wired-tab>` với event `onselected` → hàm `handleStatusTab(e)` ẩn/hiện `#tab-panel-health` và `#tab-panel-backup`.
- Đã BỎ QUA "Initialization Routine" của SKILL.md (không tạo/sửa file `agents.md`/`CLAUDE.md` gốc dự án) theo đúng ngoại lệ phổ biến trong các manifest khác của nhóm.

---

## 4. Cảnh báo nguồn Draft

**Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn (báo lại Nguyễn Phương Thảo — QA)

1. **01_AuditLogs.md — Nút "Export CSV/PDF" là [Giả định]**: Field #6 trong bảng Business Logic đã tự đánh dấu "[Giả định — cần team xác nhận]" vì không có trong Normal Flow gốc của UC-106. Wireframe dựng theo đúng cảnh báo này — nút Export có chú thích "(giả định — xem ghi chú Screen Spec)" và ghi rõ trong banner draft trên màn hình. Cần Thảo xác nhận chốt có nên giữ hay bỏ nút này trước khi lên `07_Prototype/`.
2. **01_AuditLogs.md — BR boilerplate không khớp**: Bảng BR gốc trong UC-106 của Report 3 (BR-158–164) là boilerplate lặp lại giống nhiều UC khác, không khớp nội dung nghiệp vụ — Screen Spec đã tự đối chiếu lại từ `traceability_matrix.md` §1.12 và dùng BR-162/170/171/172/174 làm chuẩn. Wireframe kế thừa phán đoán này; cần Thảo xác nhận danh sách BR thật khi QA.
3. **14_BackupManagement.md — "lịch sử backup gần đây" là [Giả định]**: Phần lịch sử backup (bảng 4 dòng gần nhất) trong tab Backup Management là dữ liệu suy luận hợp lý từ ngữ cảnh vận hành, vì Screen Spec không liệt kê cụ thể trường này trong Business Logic — wireframe đánh dấu `<span class="assumption-tag">giả định</span>` ngay tiêu đề bảng. Cần Thảo xác nhận.
4. **17_SystemActivityMonitoring.md và 20_UsageStatisticsScreen.md — chart dùng SVG doodle thay recharts**: Screen Spec AI Generation Prompt đề xuất dùng recharts thật, nhưng môi trường vanilla HTML không hỗ trợ React/recharts. Wireframe thay bằng SVG polyline/circle doodle inline — đúng tinh thần low-fidelity hand-drawn, nhưng không phải prototype-ready component. Cần lưu ý khi chuyển sang `07_Prototype/` sẽ phải thay bằng recharts thật theo Design System.
5. **19_ErrorLogsScreen.md — BR-164 và request metadata nhạy cảm**: Screen Spec ghi "request metadata nhạy cảm — áp dụng BR-164 mã hóa". Wireframe chỉ hiển thị placeholder text "[metadata đã mã hóa — hiển thị sau khi xác thực quyền]" trong Sheet chi tiết lỗi, không tự bịa nội dung metadata cụ thể. Cần Thảo/team xác nhận UX thật khi decryption metadata (có dialog xác nhận thêm quyền không, hay chỉ ẩn field?).

---

## 6. Đối chiếu số lượng (mục 5 bước 2 — `00_Team_Work_Assignment_Plan.md`)

| Actor | Screen Specs có | Wireframe đã tạo | Đủ không |
| --- | --- | --- | --- |
| SystemAdmin | 20 file (01–20, không tính 00_TongHop) | 3 file HTML bao phủ: `02_UserRolePermissionManagement` (màn 02,03,04,05,06,07,08), `09_SchoolManagementAndSystemConfig` (màn 09,10,11,12,15,16; 13 đã loại bỏ; 14 bundled trong 18), `01_MonitoringAndAudit` (màn 01,17,18,19,20) | ✅ Đủ 17/20 màn có nội dung thực; màn 13 (Campus Config) đã chính thức loại bỏ; màn 14 (Backup Management) bundled trong màn 18 — đúng theo ghi chú Screen Flow |

**Giải thích khoảng hở còn lại**:
- `13_CampusConfig.md`: **đã loại bỏ** — xác nhận 2026-07-01, không wireframe.
- `14_BackupManagement.md`: **bundled** trong `System Status Screen` (màn 18, tab "Backup Management") — không tách screen riêng đúng theo `01_System_Admin_Screen_Flow.md`.
- Tổng wireframe thực tế: 17 màn có content + 2 màn không cần (loại bỏ/bundled) = 19/20 — phủ toàn bộ màn hình hoạt động của SystemAdmin.
