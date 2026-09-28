# Wireframe — Home & Student Records (nhóm màn hình Teacher, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/Teacher/` — 7 file Screen Specification liệt kê ở bảng mục 1 (Confirmed — đã qua QA mục 6 `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`).
**Công cụ sinh**: Thủ công — theo skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), quy tắc kỹ thuật tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/Teacher/01-07_HomeAndStudentRecords.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md` (nhánh Dashboard → Student List → Student Detail → Health/Allergy tab; Sidebar nhóm "Phân phát Bữa ăn" → Special Meal Requirements, Meal Records). Sidebar tham chiếu `00_Foundation/Navigation_Guidelines.md` mục 1.

---

## 1. Danh sách 7 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | Teacher Home | `01_TeacherHome.md` | Node gốc điều hướng |
| 02 | Student List | `02_StudentList.md` | Filter Tabs All/Allergy/Special Diet/Normal |
| 03 | Student Detail | `03_StudentDetail.md` | Vào từ click hàng bảng Student List |
| 04 | Allergy Information | `04_AllergyInformation.md` | Chỉ đọc — vào từ badge dị ứng ở Student Detail |
| 05 | Health Information | `05_HealthInformation.md` | Chỉ đọc — vào từ tab Health Information |
| 06 | Special Meal Requirements | `06_SpecialMealRequirements.md` | Sidebar hoặc từ Student Detail |
| 07 | Meal Records | `07_MealRecords.md` | Sidebar hoặc từ Student Detail; kèm Dialog ghi nhận tiêu thụ |

Tổng cộng: **7 `<div class="screen">`** + 1 modal overlay (Consumption Record Input trong Meal Records).

Các nút "Distribute Meals Today" (Home) và "Report New Health Incident" (Home) trỏ sang 2 nhóm luồng khác (`08-10_MealDistribution.html`, `11-13_HealthIncidentReporting.html`) — trong file này hiển thị dạng disabled/title ghi chú, không dựng lại màn đích để tránh trùng lặp ngoài phạm vi nhóm.

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 01. Teacher Home
- [x] Đủ field: Page Header, Greeting Banner, 4 Metric Card (Total Students/Meals Pending/Dietary Notes/Health Incidents), 2 button hành động, Today's Notice Board — đúng 9 dòng Business Logic.
- [x] Đúng Design System *(ghi chú: bản hand-drawn/grayscale theo skill wireframer, badge/alert phân biệt bằng kiểu viền + icon doodle thay vì màu — chưa áp Design System thật)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Loading** ghi chú text "Skeleton cho 4 Metric Card"; **Success** là trạng thái chính render đầy đủ; **Empty** (chưa phân công lớp chủ nhiệm) không dựng hình riêng — chỉ ghi chú trong manifest vì ngoài phạm vi luồng chính; **Error**: ghi chú tham chiếu MSG-004/005.

### 02. Student List
- [x] Đủ field: Page Header, Profile Filter Tabs (All/Allergy Warning/Special Diet/Normal), Student Table (avatar, họ tên, badge tình trạng, action xem hồ sơ) — đúng 3 dòng Business Logic.
- [x] Đúng Design System *(badge Severe=viền đậm 3px+icon tròn, Special Diet=viền đứt nét+icon tam giác, Bình thường=viền liền+tick)*.
- [x] Đủ trạng thái có ghi chú: **Success** render 4 dòng mẫu; **Empty** ("Lớp chưa có học sinh nào") chỉ ghi chú text dưới bảng, không dựng hình riêng (lớp mẫu có sẵn 32 học sinh); **Loading/Error**: tham chiếu MSG-004/005.

### 03. Student Detail
- [x] Đủ field: Student Summary Card (avatar, họ tên, lớp, ngày sinh), Allergy Status Badge (link Allergy Info), Special Diet Badge (link Special Meal), tab Health Information/Meal Records, Parent Contact Info — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(badge dùng cùng quy ước viền như Student List)*.
- [x] Đủ trạng thái: **Loading** ghi chú Skeleton; **Error** tham chiếu MSG-004/005; Success/Empty không áp dụng theo đúng Screen Spec.
- Ghi chú: field "Parent Contact Info" giữ nguyên đánh dấu `[Giả định — cần team xác nhận mức độ chi tiết được phép hiển thị]` từ Screen Spec gốc.

### 04. Allergy Information
- [x] Đủ field: Student Identity Header, Allergy Profile Table (allergen + mức độ), Verification Status Badge, Read-only Notice — đúng 4 dòng Business Logic.
- [x] Đúng Design System *(Severe=viền đậm 3px, Moderate=viền đứt nét)*.
- [x] Đủ trạng thái có ghi chú: **Success** render 2 dòng allergen mẫu (đậu phộng Severe, hải sản có vỏ Moderate — khớp dữ liệu Notice Board ở Home); **Empty** ("Học sinh chưa khai báo dị ứng") chỉ ghi chú text; **Loading/Error**: tham chiếu MSG-004/005. KHÔNG có nút chỉnh sửa theo đúng yêu cầu Screen Spec (Teacher chỉ đọc).

### 05. Health Information
- [x] Đủ field: Student Identity Header, Blood Type Card, Chronic Conditions List, Emergency Contact Note, Read-only Notice — đúng 5 dòng Business Logic.
- [x] Đúng Design System *(card grayscale, không dùng màu)*.
- [x] Đủ trạng thái có ghi chú: **Success** render đầy đủ; **Empty** ("Chưa có thông tin sức khỏe") chỉ ghi chú text; **Loading/Error**: tham chiếu MSG-004/005. Giữ nguyên 2 field đánh dấu `[Giả định]` (Blood Type Card, Emergency Contact Note) từ Screen Spec gốc — không tự xác nhận thay team.

### 06. Special Meal Requirements
- [x] Đủ field: Page Header, Special Meal Student Table (họ tên/loại chế độ ăn/lý do/trạng thái duyệt), Approval Status Badge, Preparation Note — đúng 4 dòng Business Logic.
- [x] Đúng Design System *(Approved=viền liền success, Chờ duyệt=viền đứt nét warning)*.
- [x] Đủ trạng thái có ghi chú: **Success** render 3 dòng mẫu (2 Approved, 1 Pending); **Empty** ("Lớp không có học sinh cần chế độ ăn đặc biệt") chỉ ghi chú text; **Loading/Error**: tham chiếu MSG-004/005. KHÔNG có nút chỉnh sửa (Teacher chỉ đọc).

### 07. Meal Records
- [x] Đủ field: Page Header, Date Range Filter, Meal History Table (ngày/món ăn/trạng thái tiêu thụ), Consumption Record Input (Radio: Fully/Partially/Not Consumed, trong Dialog), Save Consumption Button, Special Meal Compliance Badge — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(Fully=viền liền success, Partially=viền đứt nét warning)*.
- [x] Đủ trạng thái có ghi chú: **Success** render 2 dòng lịch sử mẫu + `Toast success` (ghi chú text, không dựng ảnh toast riêng); **Empty** ("Chưa có dữ liệu bữa ăn") chỉ ghi chú text; **Error** (MSG-021 khi trùng lặp) ghi chú text dưới bảng; Dialog ghi nhận tiêu thụ dựng dạng modal overlay đúng yêu cầu Screen Spec (mở qua `Dialog`).

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS, 7 `<div class="screen" id="...">`, điều hướng bằng hàm `showScreen(id)` thuần JS (không router).
- Grayscale/mono tuyệt đối; `wired-elements` nhúng qua CDN; font `Patrick Hand` (Google Fonts); nền graph-paper; sketchy border trick áp dụng cho toàn bộ card/table-wrap/modal/button.
- Nút dùng `<button class="btn-primary/btn-secondary">` (không dùng `<wired-button>`).
- Toàn bộ icon là SVG line-art doodle inline tự vẽ (không emoji, không react-doodle-icons vì vanilla HTML).
- Badge success/warning/danger/info phân biệt bằng kiểu viền (liền/đứt nét/đậm 3px/chấm), không dùng màu.
- Sidebar liệt kê đủ 4 nhóm của Teacher (Bảng tin/Phân phát Bữa ăn/Sự cố Sức khỏe/Cá nhân); chỉ nhóm/screen thuộc file này click được, còn lại `div.side-link.disabled` kèm ghi chú "(xem file wireframe nhóm khác)".
- Dữ liệu mẫu thực tế: tên học sinh/giáo viên Việt Nam, ngày 01/07/2026 (theo ngày hiện tại của phiên làm việc), không dùng lorem ipsum.

---

## 4. Trạng thái nguồn

**Nguồn Confirmed**: Toàn bộ 7 Screen Specification nguồn đã qua bước QA (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`, do chính Nguyễn Phương Thảo thực hiện — tác giả gốc của actor Teacher). Các field còn đánh dấu `[Giả định — cần team xác nhận]` (Parent Contact Info ở #03, Blood Type/Emergency Contact ở #05) đã được liệt kê trong `05_ScreenSpecifications/00_TODO_GiaDinh_Master.md` mục Teacher #3/#1 — wireframe kế thừa nguyên trạng, không tự quyết định thay team.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn (báo lại — tự QA vì Thảo vừa là tác giả Screen Spec vừa là người dựng Wireframe)

Không phát hiện thêm mâu thuẫn mới ngoài các mục đã ghi trong `00_TODO_GiaDinh_Master.md` (Teacher #1, #3). Không cần bổ sung báo cáo mới cho nhóm màn hình này.
