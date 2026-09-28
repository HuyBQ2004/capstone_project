# Review Frontend Web — Actor School Admin & System Admin

**Phạm vi đối chiếu**: `03_Design/08_FrontEnd/Web/src/app/(app)/{school-admin,system-admin}/` (code thật) vs. `05_ScreenSpecifications/Web/{SchoolAdministration,SystemAdmin}/*.md` (49 màn School Admin + 20 màn System Admin) vs. `04_ScreenFlow/01_System_Admin_Screen_Flow.md` + `02_School_Administration_Screen_Flow.md` vs. `01_Diagrams/01_Class_Diagram/class_diagram_description.md`.
**Phương pháp**: đọc toàn bộ `page.tsx` đã implement (School Admin: 27 file, System Admin: 14 file) + component dùng chung (`incident-detail-view.tsx`, `data-table.tsx`), so khớp field-by-field với bảng "Business Logic" của từng Screen Spec và ràng buộc entity trong Class Diagram.
**Ngày review**: 04/07/2026.

> Ghi chú tên file: yêu cầu gốc là ghi vào `review_screen_parent_and_teacher.md`, nhưng file đó đã chứa review Parent & Teacher hoàn chỉnh. Để tránh mất dữ liệu, review 2 actor này được lưu sang file mới `review_screen_schooladmin_and_systemadmin.md` cùng thư mục `08_FrontEnd/Web/review/`.

---

## 1. Kết luận nhanh

Khác hẳn cặp Parent/Teacher (mới ~33–38% màn), **cả 2 actor quản trị đã implement gần đủ độ phủ màn hình** — đây là 2 actor được làm kỹ nhất trong codebase. Cụ thể:

- **System Admin**: **20/20 màn spec đều có route thật** (~100% độ phủ route). Chất lượng cao, bám sát field-by-field (Create User có Select School điều kiện, Security Policies đủ BR-002/004/006, Update School có khóa/mở khóa tọa độ kèm lý do bắt buộc, AI Settings gate Save theo Test Connection). Gap còn lại là **tinh chỉnh** (filter bar, health-check card, chart recharts) chứ không phải thiếu luồng.
- **School Admin**: **~40/49 màn có route hoặc được gộp hợp lý**. Có **4 lỗ hổng luồng thật sự** (không chỉ thiếu field) cần vá, và một loạt màn "detail/dialog" bị rút gọn làm đứt luồng end-to-end.

**4 gap luồng nghiêm trọng nhất của School Admin (P0/P1):**

1. **Đóng sự cố (Resolution Report — UC-013/UC-121) chưa có màn thật** — `incident-detail-view.tsx` chỉ có 1 nút "Đóng sự cố" (disable tới khi CAPA xong). Thiếu toàn bộ: closing statement (Textarea bắt buộc), checklist hồi phục học sinh (BR-155), tóm tắt root cause, accordion báo cáo đầy đủ (clinical notes/lab results, UC-121). Đây là bước cuối bắt buộc để khép vòng đời sự cố.
2. **Cập nhật hồ sơ học sinh (Student Profile Detail — UC-099) là màn CHỈ ĐỌC** — `students/[id]/page.tsx` không có form sửa (Họ tên/Ngày sinh/Lớp) lẫn nút Lưu. Không có cách nào đổi lớp học sinh (kèm kiểm tra sức chứa MSG-010) hay sửa hồ sơ — UC-099 chưa thực thi được. Kéo theo **Parent Account Detail (25)** và **Linked Students List (27)** không có lối vào (không có link từ student detail).
3. **Academic Year Detail (43) + Activate/Close Dialog (44) chưa đúng** — chỉ có màn List, **không có route `[id]` detail**; Activate/Close là nút bấm trực tiếp trên list, **không qua AlertDialog cảnh báo hệ quả cascade** (Close → toàn bộ Classroom chuyển read-only, BR-193) như spec yêu cầu — rủi ro tương đương Active/Deactivate School nhưng lại thiếu bước xác nhận.
4. **Approve/Reject Violation Resolution (48) thiếu lý do bắt buộc khi Reject** — `violations/page.tsx` Reject bằng 1-click, không có Textarea lý do (spec AF1/BR-186 yêu cầu bắt buộc). Ngoài ra Violation Registry (47) thiếu bộ lọc Type/Severity/Status và không click-through được sang chi tiết biên bản (46).

Ngoài ra có **2 gap an toàn dữ liệu (data-safety) đáng lưu ý** ở Menu Detail và Compliance Dashboard (xem §3), và một **tham chiếu BR sai lan truyền từ tài liệu nền tảng** (`BR-123` — xem §5.4).

---

## 2. Bảng độ phủ màn hình

### 2.1 System Admin (20 màn spec)

| # | Màn hình Spec | Route code hiện có | Trạng thái |
| --- | --- | --- | --- |
| 01 | Audit Logs | `system-admin/audit-logs` | ⚠️ Có, thiếu filter bar (Date range/Action category/IP) — chỉ có ô search |
| 02 | System Administrator Dashboard | `system-admin/dashboard` | ⚠️ Có, thiếu card health-check + chart hoạt động 24h + lưới shortcut; "cảnh báo gần nhất" dùng audit log thay vì Error Logs Critical/High |
| 03 | Manage User Screen/Dialog | `system-admin/users` (dialog) | ✅ Tốt — đủ 6 field, Select School điều kiện, validate email/SĐT |
| 04 | User Account List | `system-admin/users` | ⚠️ Có, thiếu Select filter Role/School/Status (chỉ search) + nút Export |
| 05 | Manage Role Screen | `system-admin/roles` (dialog) | ✅ Tốt — Create/Update/Delete, khóa role default, permission theo module |
| 06 | Role List | `system-admin/roles` | ✅ Tốt |
| 07 | Manage Permission List | `system-admin/permissions` | ✅ Tốt — read-only, filter Module, Export |
| 08 | Active/Deactivate User Dialog | `system-admin/users` (ToggleActive) | ✅ Tốt — 2 bước, cảnh báo hủy session, chặn admin mặc định |
| 09 | Active/Deactivate School Dialog | `system-admin/schools` (SchoolActions) | ✅ Tốt — dialog cảnh báo cascade rõ (BR-021) |
| 10 | School List | `system-admin/schools` | ✅ Có, rowHref → detail; thiếu Select filter Status (chỉ search) |
| 11 | School Detail | `system-admin/schools/[id]` | ✅ Xuất sắc — đủ field + GPS lock badge + mini-map + Export PDF |
| 12 | Update School Screen/Dialog | `.../schools/[id]` (UpdateSchoolDialog) | ✅ Xuất sắc — mở khóa tọa độ kèm lý do bắt buộc, validate MST, auto-lock |
| 13 | Campus Config | — (đã loại bỏ 2026-07-01) | ✅ Đúng — không implement (phù hợp quyết định loại bỏ) |
| 14 | Backup Management | `system-admin/backup` | ✅ Có — lịch/dung lượng/lịch sử; spec coi là panel trong System Status, ở đây tách trang (chấp nhận được) |
| 15 | Security Policies | `system-admin/security-policies` | ✅ Xuất sắc — đủ BR-002/004/006 + validate khoảng giá trị |
| 16 | AI Settings | `system-admin/integrations` | ✅ Xuất sắc — Tabs 4 dịch vụ, gate Save theo Test Connection, ẩn credential |
| 17 | System Activity Monitoring | `system-admin/monitoring` | ⚠️ Có, dùng chart thuần CSS thay vì recharts (spec nêu recharts) — deviation có ghi chú |
| 18 | System Status Screen | `system-admin/system-status` | ⚠️ Có, thiếu card health-check từng service lõi (Database/Email/Firebase/Gemini/GPS + badge), nút Export CSV/PDF, panel Backup nhúng |
| 19 | Error Logs Screen | `system-admin/error-logs` | ✅ Có, panel chi tiết đầy đủ; thiếu filter Date range (chỉ có Severity) |
| 20 | Usage Statistics Screen | `system-admin/usage-statistics` | ✅ Có, chart CSS + top use case; gộp dung lượng/API vào StatCard thay vì 2 chart riêng (chấp nhận được) |

### 2.2 School Admin (49 màn spec)

| # | Màn hình Spec | Route code hiện có | Trạng thái |
| --- | --- | --- | --- |
| 01 | School Administration Dashboard | `school-admin/dashboard` | ⚠️ Có, thiếu lưới shortcut theo module + alert vi phạm/override gần nhất (BR-189/190); card compliance là số liệu tĩnh, không link |
| 02 | Catering Partnership Requests | `school-admin/partnerships` (gộp) | ⚠️ Gộp chung với Supplier; thiếu Select filter Status |
| 03 | Supplier Partnership Requests | `school-admin/partnerships` (gộp) | ⚠️ Gộp chung; thiếu badge "Performance Warning" cho supplier bị flag |
| 04 | Service Caterers Detail | `school-admin/partnerships/[id]` (gộp) | ✅ Tốt — approve/reject + lý do, chặn nếu cert hết hạn, bảng giấy tờ, điểm site inspection |
| 05 | Supplier Detail | `school-admin/partnerships/[id]` (gộp) | ⚠️ Tốt; thiếu bảng lịch sử giao hàng rút gọn (spec field #4) |
| 06 | Menu Management | `school-admin/menu-approval` | ✅ Có (list) |
| 07 | Menu Detail | `school-admin/menu-approval/[id]` | ⚠️ Có approve/reject + cảnh báo dị ứng + trần chi phí; **thiếu**: danh sách nguyên liệu + trace allergen từng món (BR-069), tóm tắt dinh dưỡng theo nhóm tuổi (BR-062), bảng lịch sử duyệt + chữ ký điện tử (BR-066) |
| 08 | Incident Dashboard | — (dùng chung `incidents`) | ⚠️ Không có màn dashboard riêng — thiếu card severity + card CAPA chờ duyệt (entry point tổng hợp) |
| 09 | Incident List | `school-admin/incidents` | ✅ Có, kèm Export dialog (UC-031); thiếu Select filter Severity/Status (chỉ search) |
| 10 | Incident Detail | `school-admin/incidents/[id]` (shared view) | ⚠️ Có timeline+quarantine+CAPA; **thiếu**: Suspected food item + meal time, First aid administered, nút Export, panel bản đồ truy xuất (BR-151 chỉ hiện dạng text timeline) |
| 11 | Corrective Action Review | `school-admin/corrective-actions` | ✅ Xuất sắc — approve/reject + ghi chú/lý do bắt buộc + gallery bằng chứng |
| 12 | Resolution Report / Close Incident | — (chỉ 1 nút trong Incident Detail) | ❌ **Thiếu màn** — không có closing statement, checklist hồi phục HS (BR-155), root cause, accordion báo cáo đầy đủ (UC-121) |
| 13 | Food Sample Retention Reports | `school-admin/reports/[slug]` | ✅ Có (hub báo cáo) |
| 14 | Receiving Reports | `school-admin/reports/[slug]` | ✅ Có |
| 15 | Inspection Reports | `school-admin/reports/[slug]` | ✅ Có |
| 16 | Partnership Reports | `school-admin/reports/[slug]` | ✅ Có |
| 17 | Partnership Approval History | `school-admin/partnerships` (khối audit log inline) | ✅ Có — trích audit log lọc Resource=Supplier |
| 18 | Compliance Dashboard | — (gộp một phần vào `reports`) | ⚠️ **Thiếu bản chất UC-027**: 3-bước kiểm tra realtime theo lô, điểm vệ sinh ngày, Banner Kitchen Quarantine (BR-105/149), nút "Ký xác nhận trạng thái an toàn hôm nay" |
| 19 | Classroom Management | `school-admin/classrooms` | ✅ Có, rowHref + create dialog; thiếu Select filter Grade |
| 20 | Classroom Detail | `school-admin/classrooms/[id]` | ⚠️ Có; **không chặn** khi GVCN đã gán lớp khác (MSG-011 chỉ nêu trong mô tả, không validate); Update Classroom **thiếu field Sức chứa** (spec field #5 + MSG-010) |
| 21 | Create Classroom | `school-admin/classrooms` (dialog) | ⚠️ **Thiếu field "Năm học áp dụng"** + chặn MSG-081 khi chưa có năm Active (đang hardcode `academicYearId: "AY-2025"`) |
| 22 | Student Profile Management | `school-admin/students` | ✅ Có — create dialog, link bulk import, rowHref |
| 23 | Student Profile Detail | `school-admin/students/[id]` | ❌ **Chỉ đọc** — không có form sửa (Họ tên/Ngày sinh/Lớp) + nút Lưu ⇒ UC-099 Update Student chưa thực thi; không link Parent Account |
| 24 | Bulk Import Student Profiles | `school-admin/students/import` | ✅ Xuất sắc — stepper, bảng validate, dòng lỗi có mã |
| 25 | Parent Account Detail | — | ❌ Không có route (student detail không link sang) |
| 26 | Link Parent Account | (gộp trong Create Student — field email) | ⚠️ Có email PH; thiếu UI nhánh "tìm thấy/tạo mới" + chặn giới hạn 5 HS (AF1) |
| 27 | Linked Students List | — | ❌ Không có route |
| 28 | School Announcement Management | `school-admin/announcements` | ✅ Có |
| 29 | Create Announcement | `school-admin/announcements/create` | ⚠️ Có; Target Group là **Select đơn** thay vì Checkbox group multi-select (spec field #3) |
| 30 | Update Announcement | `school-admin/announcements/[id]/edit` | ✅ Có (có khối lịch sử sửa đổi) |
| 31 | Announcement Detail | `school-admin/announcements/[id]` | ✅ Có (file mỏng — cần verify hiển thị đủ Author/Publication Dates + lịch sử sửa đổi) |
| 32 | Staff Management | `school-admin/staff` | ✅ Có — create + toggle + cột "Xác minh email"; thiếu Select filter Role/Status |
| 33 | Upload Staff List | `school-admin/staff/upload` | ✅ Có |
| 34 | Invitation Status | (gộp vào cột "Xác minh email" của Staff) | ⚠️ Không có màn riêng — thiếu nút "Gửi lại email xác minh" + theo dõi hạn token 48h (BR-011) |
| 35 | System Usage Metrics | `school-admin/reports/[slug]` | ✅ Có |
| 36 | Traceability Reports | `school-admin/reports/[slug]` | ✅ Có |
| 37 | School Configuration | `school-admin/config` | ✅ Xuất sắc — geofence 50–500m, trần chi phí, email báo cáo |
| 38 | Verification Documents | (gộp trong `partnerships/[id]`) | ✅ Đúng — nhúng bảng giấy tờ |
| 39 | Allergy Review | `school-admin/allergy-review` | ✅ Có (read-only, không có Approve/Reject — đúng); thiếu Select filter Class/loại dị ứng |
| 40 | Export Incident Reports | `school-admin/incidents` (dialog) | ✅ Có — filter + Generate Preview + PDF/Excel |
| 41 | Academic Year List | `school-admin/academic-years` | ✅ Có; thiếu Select filter Status |
| 42 | Create Academic Year | `school-admin/academic-years` (dialog) | ⚠️ **Thiếu checkbox "Sao chép cấu trúc lớp"** (BR-194) + validate Start<End |
| 43 | Academic Year Detail | — | ❌ Không có route `[id]` (chỉ có list) |
| 44 | Activate/Close Academic Year Dialog | `school-admin/academic-years` (nút inline) | ⚠️ Bấm trực tiếp, **không qua AlertDialog cảnh báo hệ quả** (Close → Classroom read-only, BR-193) |
| 45 | Promote Students Workspace | `school-admin/academic-years/promote` | ✅ Xuất sắc — stepper, loại trừ cá nhân, chặn vượt sức chứa |
| 46 | Record Violation Resolution (bản School Admin) | — | ❌ Không có màn xem chi tiết biên bản (read-only) |
| 47 | Violation Registry | `school-admin/violations` | ⚠️ Có bảng; thiếu Select filter Type/Severity/Status + badge cảnh báo tái diễn (BR-187/MSG-088) + rowHref sang chi tiết |
| 48 | Approve/Reject Violation Resolution | `school-admin/violations` (nút inline) | ⚠️ **Reject thiếu lý do bắt buộc** (Textarea, AF1/BR-186) |
| 49 | Weekly Override Abuse Report | `school-admin/violations/override-report` | ✅ Có |

---

## 3. Chi tiết gap theo màn hình — School Admin

### 07. Menu Detail (`menu-approval/[id]/page.tsx`)
Hiển thị thực đơn theo card từng ngày (món + kcal), badge trần chi phí, cảnh báo xung đột dị ứng (chặn Approve — BR-063, MSG-051) — phần này tốt. **Thiếu 3 khối bắt buộc theo spec:**
- **Danh sách nguyên liệu đầy đủ + trace allergen từng món** (BR-069) — hiện chỉ liệt kê tên món, không có breakdown nguyên liệu để BGH kiểm tra allergen thủ công.
- **Tóm tắt dinh dưỡng theo nhóm tuổi** (BR-062) — không có card số liệu dinh dưỡng.
- **Bảng lịch sử duyệt/từ chối + chữ ký điện tử** (BR-066) — không có; đây là yêu cầu tuân thủ (electronic signature khi duyệt menu).

### 08 + 12. Incident Dashboard & Resolution Report
- **Incident Dashboard (08)** không tồn tại như entry point riêng — vào thẳng `incidents` list. Thiếu card tổng số sự cố theo severity + card số CAPA chờ duyệt (spec fields #1–2).
- **Resolution Report / Close Incident (12)** là gap luồng lớn nhất: `incident-detail-view.tsx` chỉ render nút "Đóng sự cố (BR-155)" disable tới khi `capaDone`. **Không có**: Textarea closing statement (bắt buộc), checklist trạng thái hồi phục từng học sinh bị ảnh hưởng (toàn bộ phải "Fully Recovered" — BR-155), tóm tắt root cause (BR-150), accordion báo cáo đầy đủ clinical notes/lab results (UC-121, BR-154). Hệ quả: **không thể hoàn tất đóng sự cố đúng quy trình** — vòng đời Workflow 11 bị đứt ở bước cuối.

### 10. Incident Detail (`incident-detail-view.tsx` dùng chung)
Có timeline điều tra (extra tốt), bảng lô Quarantine (BR-149), bảng CAPA liên kết (BR-152). **Thiếu so với spec đã hợp nhất với bản Medical Staff:**
- **Suspected food item + meal time** (field #3, `suspectedDishIds` trong class diagram) — không hiển thị.
- **First aid administered** (field #4) — không hiển thị.
- **Nút Export** (AF1 UC-116) — không có.
- **Bản đồ truy xuất nguồn gốc** (BR-151) hiện chỉ là 1 dòng text trong timeline, không phải panel/link truy xuất thật.

### 18. Compliance Dashboard (bị gộp vào `reports`)
`reports/page.tsx` là hub tổng hợp tỷ lệ tuân thủ theo module (kiểm thực/lưu mẫu/nhận hàng) — hữu ích nhưng **không thay thế được UC-027**. Thiếu bản chất của Compliance Dashboard:
- **Trạng thái 3-bước kiểm tra hôm nay theo thời gian thực** theo từng lô sản xuất (BR-091).
- **Điểm vệ sinh hàng ngày** của bếp (BR-100/104).
- **Banner Kitchen Quarantine** cố định khi bếp đang bị cách ly (BR-105/149, MSG-024).
- **Nút "Ký xác nhận trạng thái an toàn hôm nay"** (hành động ký review riêng của School Admin — Normal Flow bước 5).

### 20 + 21. Classroom Detail & Create Classroom
- **Create Classroom (21)**: thiếu field **"Năm học áp dụng"** (Select/read-only) và **chặn MSG-081** khi trường chưa có năm học Active (BR-192). Code hiện hardcode `academicYearId: "AY-2025"` — vi phạm ràng buộc "Classroom phải thuộc năm học Active".
- **Classroom Detail (20)**: dialog Update chỉ sửa **GVCN + Khối**, **thiếu field Sức chứa** (spec field #5: sửa Capacity, chặn nếu nhỏ hơn số HS hiện có — MSG-010). Đồng thời **không enforce MSG-011** khi GVCN đã chủ nhiệm lớp khác — mô tả có nhắc "Nếu giáo viên đã chủ nhiệm lớp khác → MSG-011" nhưng không có logic kiểm tra/chặn thực tế (BR-020).

### 23 + 25 + 27. Nhóm hồ sơ học sinh & phụ huynh
- **Student Profile Detail (23)** là màn **chỉ đọc** — chỉ hiển thị card + hồ sơ sức khỏe. Spec UC-099 (Update Student) yêu cầu form sửa Họ tên/Ngày sinh/Lớp (Select, validate sức chứa lớp mới MSG-010) + School Reg No read-only + link Parent Account + nút Lưu. **UC-099 chưa thực thi được**.
- **Parent Account Detail (25)** và **Linked Students List (27)**: không có route, và Student Detail không có link "Phụ huynh liên kết" → không có lối vào. Toàn bộ nhánh xem/quản lý liên kết PH-HS ngược (BR-030, tối đa 5 HS/PH) không truy cập được từ UI.
- **Link Parent Account (26)**: khi tạo học sinh có nhập email PH, nhưng thiếu UI nhánh "tìm thấy tài khoản → chỉ xác nhận link" vs "chưa có → tạo mới (Họ tên+SĐT)" và **cảnh báo chặn khi PH đã đạt 5 HS** (AF1, BR-030).

### 41–44. Academic Year Management
- **Academic Year Detail (43)** không có route `[id]` — click 1 năm học trên list không đi đâu. Thiếu: bảng lớp thuộc năm học, nút Activate/Close/Promote theo điều kiện trạng thái.
- **Activate/Close Dialog (44)**: hiện là nút bấm trực tiếp trên list gọi `useAction`. Spec yêu cầu **AlertDialog** hiển thị hệ quả: nhánh Close cảnh báo toàn bộ Classroom chuyển read-only (BR-193) — hành động rủi ro cascade tương tự Active/Deactivate School nhưng **không có bước xác nhận + cảnh báo**. MSG-080 chỉ xử lý qua `onError` toast, không có pre-check dialog.
- **Create Academic Year (42)**: thiếu **checkbox "Sao chép cấu trúc lớp từ năm học trước"** (BR-194, chỉ sao chép cấu trúc, không chuyển HS) và validate Start < End.

### 46–48. Violation Management
- **Violation Registry (47)**: bảng đủ cột nhưng **thiếu bộ lọc** Type/Severity/Status (spec fields #1–3, chỉ có search), **thiếu badge/icon cảnh báo tái diễn** (BR-187/MSG-088), và **không rowHref** sang chi tiết biên bản.
- **Record Violation Resolution — bản School Admin (46)**: không có màn xem read-only biên bản xử lý (vi phạm gốc, actionTaken, handledBy, mức kỷ luật) — click dòng không mở được chi tiết.
- **Approve/Reject Violation Resolution (48)**: Reject bằng 1-click `update.mutate({resolution:"REJECTED"})` — **không có Textarea lý do bắt buộc** (spec AF1 + BR-186: Reject phải ghi lý do để trả về Medical Staff sửa lại). Đây là gap tuân thủ quy trình phân cấp phê duyệt.

### 01. Dashboard
Có 4 StatCard (menu/đối tác/sự cố/tỷ lệ kiểm thực) + 2 Alert (sự cố High, cert sắp hết hạn) + 2 bảng. **Thiếu**: lưới shortcut điều hướng theo module (spec field #5), và alert **cảnh báo vi phạm/override abuse gần nhất** (spec field #6, BR-189/190) — hiện alert đang là cert/incident chứ không phải từ Violation Registry.

---

## 4. Chi tiết gap theo màn hình — System Admin

System Admin gần như đủ; các gap đều ở mức tinh chỉnh, không đứt luồng:

### 02. System Administrator Dashboard (`dashboard/page.tsx`)
So spec (6 thành phần): có card trường + card user (dạng số) + 2 bảng (trường, audit log). **Thiếu**:
- **Card trạng thái hệ thống health-check** (badge success/danger) — spec field #3.
- **Biểu đồ hoạt động hệ thống 24h** (recharts, CPU/RAM/session) — spec field #4; hiện không có.
- **Danh sách shortcut điều hướng** tới 18 màn con (spec field #5).
- **Cảnh báo lỗi gần nhất** đang lấy từ audit log recent; spec field #6 yêu cầu lấy từ Error Logs mức Critical/High (BR-164).

### 18. System Status Screen (`system-status/page.tsx`)
Hiện có 4 StatCard (uptime/CPU/disk/requests) + list tích hợp ngoài. **Thiếu so với UC-149:**
- **Card health-check từng service lõi** riêng biệt (Database, Email Service, Firebase, Gemini AI, GPS Service) với badge success/danger — spec field #1 (hiện gộp mơ hồ vào "Tích hợp dịch vụ ngoài").
- **Nút Export CSV/PDF** (AF1) — không có.
- **Panel Backup Management nhúng** — spec định nghĩa Backup là panel con của System Status (bundled UC-149 bước 4); code tách hẳn sang `/backup` (chấp nhận được nhưng nên link chéo).

### Filter bar còn thiếu trên các màn danh sách
- **Audit Logs (01)**: thiếu filter Date range / Action category / IP Address (chỉ có search) — spec fields #1–4.
- **User Account List (04)**: thiếu Select filter Role/School/Status + nút Export (spec fields #2–4, #9).
- **School List (10)**: thiếu Select filter Status.
- **Error Logs (19)**: thiếu filter Date range (chỉ có Severity).

### Chart recharts vs CSS (17, 20)
`System Activity Monitoring` và `Usage Statistics` dùng chart thuần CSS (đã ghi chú "dự án chưa cài recharts"). Spec chỉ định recharts (`Design_System.md`). Không phải lỗi chức năng nhưng cần thống nhất khi nối dữ liệu thật (tooltip/trục/legend recharts sẽ khác layout hiện tại).

---

## 5. Vấn đề xuyên suốt (cross-cutting)

1. **Màn "Detail" và "Dialog xác nhận" hay bị rút gọn thành nút 1-click.** Mẫu lặp lại: Close Incident (12), Activate/Close Academic Year (44), Approve/Reject Violation Resolution (48) đều bỏ qua bước màn/dialog có cảnh báo hệ quả + nhập liệu bắt buộc mà spec yêu cầu. Đây là rủi ro tuân thủ (thiếu closing statement, thiếu lý do reject, thiếu cảnh báo cascade) — nên chuẩn hóa 1 pattern `AlertDialog` + form bắt buộc dùng lại cho cả 3.

2. **Thiếu `rowHref`/route detail ở vài bảng School Admin** khiến đứt luồng: Academic Year List → Detail (43), Violation Registry → Resolution Detail (46), Student Detail → Parent Account (25) → Linked Students (27). Hạ tầng `DataTable` đã hỗ trợ `rowHref` (đã dùng ở classrooms/students/incidents/partnerships/schools) — fix rẻ, chỉ thiếu trang đích.

3. **Bộ lọc dạng Select bị thay bằng ô search** ở hầu hết bảng của cả 2 actor (User List, School List, Audit Logs, Error Logs, Classroom, Staff, Allergy Review, Violation Registry, Academic Year). `DataTable` hiện chỉ expose `searchPlaceholder` + `filters` (prop cố định), chưa có UI Select filter cho người dùng chọn — nên bổ sung prop filter tabs/select dùng chung.

4. **Tham chiếu `BR-123` sai lan truyền từ tài liệu nền tảng.** Code (`students/[id]`, `classrooms`) và cả Screen Spec (`39_AllergyReview.md`) trích `BR-123` cho quy tắc "cảnh báo dị ứng màu đỏ khi phục vụ". Chính Screen Spec đã ghi chú **BR-123 không tồn tại** trong `business_rules.md` (đã grep). Quy tắc đúng theo class diagram là **BR-033 → BR-043** (AllergyProfile). Cần sửa citation ở cả code lẫn `Design_System.md`/`Accessibility.md` để tránh audit fail.

5. **Lệch số BR/MSG trong comment code cần rà.** Nhiều nơi code trích MSG theo Report 3 gốc (VD announcement dùng MSG-047 — đúng; nhưng một số spec ghi chú Report 3 gốc ghi MSG-072/074 sai domain). Code hiện đang dùng đúng MSG-047 — tốt; chỉ cần đảm bảo không quay lại dùng số cũ khi nối backend.

6. **Điểm mạnh cần giữ**: validate phía client rất sát BR (Create User SĐT/email, Security Policies khoảng giá trị, Update School tọa độ, Create Classroom sức chứa, Promote Students vượt sức chứa), dùng `AlertDialog` cascade đúng cho Active/Deactivate School và Deactivate User — nên nhân rộng pattern này sang các màn còn thiếu ở §5.1.

---

## 6. Danh sách tính năng cần bổ sung để đủ luồng end-to-end

### School Admin (ưu tiên P0 → P2)
- **P0 — khép vòng đời sự cố**: dựng màn **Resolution Report / Close Incident (12)** với closing statement + checklist hồi phục HS (BR-155) + root cause + accordion báo cáo đầy đủ (UC-121); gắn vào nút "Đóng sự cố".
- **P0 — cập nhật hồ sơ học sinh**: biến **Student Profile Detail (23)** thành form sửa được (UC-099) + validate đổi lớp (MSG-010); thêm link **Parent Account Detail (25)** + **Linked Students List (27)**.
- **P0 — phê duyệt vi phạm có kiểm soát**: thêm **Textarea lý do bắt buộc khi Reject** ở Approve/Reject Violation Resolution (48, BR-186); dựng màn xem chi tiết biên bản (46).
- **P1 — vòng đời năm học an toàn**: dựng **Academic Year Detail (43)** + chuyển Activate/Close sang **AlertDialog cảnh báo cascade** (44, BR-193); thêm checkbox "Sao chép cấu trúc lớp" ở Create Academic Year (42, BR-194).
- **P1 — ràng buộc lớp học**: thêm field "Năm học áp dụng" + chặn MSG-081 ở Create Classroom (21); thêm field Sức chứa + enforce MSG-011/MSG-010 ở Classroom Detail (20).
- **P1 — Menu Detail đầy đủ**: bổ sung danh sách nguyên liệu + trace allergen (BR-069), tóm tắt dinh dưỡng theo nhóm tuổi (BR-062), bảng lịch sử duyệt + chữ ký điện tử (BR-066).
- **P1 — Compliance Dashboard thật (18)**: dựng màn 3-bước realtime + điểm vệ sinh + Banner Kitchen Quarantine + nút "Ký xác nhận" (UC-027), tách khỏi hub reports.
- **P2 — hoàn thiện Incident Detail (10)**: thêm Suspected food item + meal time, First aid administered, nút Export, panel bản đồ truy xuất (BR-151).
- **P2 — bộ lọc & điều hướng**: Select filter cho Violation Registry (47)/Classroom (19)/Staff (32)/Allergy Review (39)/Academic Year (41)/Incident (09); rowHref sang detail ở Violation Registry; Invitation Status (34) với nút gửi lại email + hạn token; Target Group multi-select ở Create Announcement (29); tách/đánh dấu Catering vs Supplier + Status filter + Performance Warning badge ở Partnership list (02/03).
- **P2 — dashboard**: lưới shortcut theo module + alert vi phạm/override gần nhất (01).

### System Admin (ưu tiên P1 → P2)
- **P1 — hoàn thiện Dashboard (02)**: card health-check + biểu đồ hoạt động 24h + lưới shortcut + đổi nguồn "cảnh báo gần nhất" sang Error Logs Critical/High.
- **P1 — System Status (18)**: card health-check từng service lõi + nút Export + nhúng/link panel Backup.
- **P2 — bộ lọc danh sách**: filter Date/Action/IP ở Audit Logs (01); filter Role/School/Status + Export ở User List (04); filter Status ở School List (10); filter Date range ở Error Logs (19).
- **P2 — chart**: chuyển chart CSS sang recharts ở Monitoring (17) + Usage Statistics (20) khi nối dữ liệu thật, tách chart dung lượng/API request riêng theo spec.

---

## 7. Kế hoạch chỉnh sửa đề xuất (theo giai đoạn)

**Giai đoạn 1 — Vá các lỗ hổng luồng end-to-end (bắt buộc trước nghiệm thu School Admin)**
1. Dựng màn **Resolution Report / Close Incident** (`school-admin/incidents/[id]/close` hoặc block trong incident detail): closing statement bắt buộc + checklist hồi phục HS (BR-155) + accordion báo cáo đầy đủ.
2. Chuyển **Student Profile Detail** sang form UC-099 (sửa Họ tên/Ngày sinh/Lớp + Save + validate MSG-010); dựng **Parent Account Detail** + **Linked Students List** và link từ student detail.
3. Thêm **Textarea lý do reject** cho Approve/Reject Violation Resolution (48); dựng route chi tiết biên bản (46) + rowHref từ Violation Registry.
4. Dựng **Academic Year Detail** (`academic-years/[id]`) + chuyển Activate/Close sang **AlertDialog** cảnh báo cascade (BR-193); thêm checkbox sao chép cấu trúc lớp ở Create Year.
5. Sửa **Create/Update Classroom**: thêm "Năm học áp dụng" + chặn MSG-081; thêm field Sức chứa + enforce MSG-011/MSG-010.

**Giai đoạn 2 — Hoàn thiện field & tuân thủ theo spec**
6. Bổ sung Menu Detail: nguyên liệu + trace allergen, dinh dưỡng theo nhóm tuổi, lịch sử duyệt + chữ ký điện tử.
7. Dựng Compliance Dashboard thật (UC-027) tách khỏi reports hub.
8. Hoàn thiện Incident Detail (suspected dish/meal time, first aid, Export, panel truy xuất) + dựng Incident Dashboard entry (card severity + CAPA).
9. Bổ sung bộ lọc Select dùng chung cho `DataTable` (System Admin: Audit/User/School/Error; School Admin: Violation/Classroom/Staff/Allergy/Academic Year/Incident) — nâng cấp component 1 lần, áp cho cả 2 actor.

**Giai đoạn 3 — Tinh chỉnh dashboard, chart, dọn tham chiếu**
10. Hoàn thiện System Admin Dashboard (health card + chart 24h + shortcut) và System Status (health-check cards + Export + Backup panel).
11. Chuyển chart CSS → recharts ở Monitoring + Usage Statistics; tách chart dung lượng/API request.
12. Sửa tham chiếu **BR-123 → BR-033/BR-043** ở code (`students/[id]`, `classrooms`) và tài liệu nền tảng; rà lại các citation MSG lệch số; hoàn thiện Invitation Status (resend + token 48h), Target Group multi-select, Partnership list tách Catering/Supplier.

---

## 8. Ghi chú giới hạn của review này
- Review dựa trên mock data tĩnh (`mock-data.ts` + `useAction`/`usePagedList`), chưa nối API thật — một số hành vi điều kiện (VD MSG-080 chặn Activate năm học, MSG-011 chặn GVCN trùng) được đánh giá qua code tĩnh; hành vi backend thực tế cần verify khi tích hợp.
- Một số file mỏng chưa đọc chi tiết field-by-field (`menu-approval` list, `announcements` list/detail/edit, `staff/upload`, `reports/[slug]`) — đã xác nhận route tồn tại và khớp entry point ScreenFlow; đánh giá dựa trên spec + các file cùng nhóm.
- Chỉ review 2 actor School Admin & System Admin theo yêu cầu; các actor khác (Parent, Teacher, Medical Staff, Service Caterers, Raw Food Supplier) ngoài phạm vi — Parent/Teacher đã có review riêng tại `review_screen_parent_and_teacher.md`.
