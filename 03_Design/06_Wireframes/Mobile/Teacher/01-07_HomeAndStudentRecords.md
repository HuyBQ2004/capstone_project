# Wireframe — Home & Student Records (nhóm màn hình Teacher, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/Teacher/` — 7 file Screen Specification liệt kê ở bảng mục 1 (Confirmed — đã qua QA mục 6 `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`).
**Công cụ sinh**: Thủ công — theo skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), quy tắc kỹ thuật tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Mobile/Teacher/01-07_HomeAndStudentRecords.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` (nhánh Dashboard → Student List → Student Detail → Health/Allergy/Meal Records/Special Meal). Bố cục theo `00_Foundation/Layout_Guidelines.md` mục 2 (AppBar + BottomNavigationBar 4 mục: Home/Students/Meals/Incidents).

---

## 1. Danh sách 7 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | Teacher Home | `01_TeacherHome.md` | Node gốc điều hướng, AppBar + BottomNav |
| 02 | Student List | `02_StudentList.md` | Card list (không Table — đúng Platform Layout Mobile) + Filter Chip |
| 03 | Student Detail | `03_StudentDetail.md` | Card action link tới Health/Meal Records; badge tới Allergy/Special Meal |
| 04 | Allergy Information | `04_AllergyInformation.md` | Chỉ đọc |
| 05 | Health Information | `05_HealthInformation.md` | Chỉ đọc |
| 06 | Special Meal Requirements | `06_SpecialMealRequirements.md` | Card list học sinh chế độ ăn đặc biệt |
| 07 | Meal Records | `07_MealRecords.md` | Kèm modal Consumption Record Input |

Tổng cộng: **7 `<div class="screen">`** + 1 modal overlay. `BottomNavigationBar` cố định 4 mục (Home/Students/Meals/Incidents) theo đúng Platform Layout Mobile của `01_TeacherHome.md`; mục "Incidents" disabled/title trỏ sang nhóm `11-13_HealthIncidentReporting.html`.

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 01. Teacher Home
- [x] Đủ field: 4 Metric Card (grid 2x2 theo đúng Platform Layout Mobile), Greeting Banner, 2 action button full-width, Notice Board Card — đúng 9 dòng Business Logic.
- [x] Đúng Design System *(grayscale hand-drawn, badge/alert phân biệt bằng kiểu viền)*.
- [x] Đủ trạng thái: Success là trạng thái chính; Loading/Empty/Error ghi chú text (giống bản Web, không lặp lại chi tiết).

### 02. Student List
- [x] Đủ field: Profile Filter Tabs (dạng Chip theo Mobile), Student Card list (thay Table) — đúng 3 dòng Business Logic, đúng Platform Layout Mobile ("danh sách Card cuộn dọc").
- [x] Đúng Design System.
- [x] Trạng thái: Success render 3 card mẫu; Empty ghi chú text.

### 03. Student Detail
- [x] Đủ field: Student Summary Card, 2 Badge (Allergy/Special Diet, tap để điều hướng), 2 Card action link (Health Information, Meal Records), Parent Contact Info — đúng 6 dòng Business Logic, đúng Platform Layout Mobile ("danh sách link điều hướng dạng Card action").
- [x] Đúng Design System.
- [x] Trạng thái: Loading/Error ghi chú text.

### 04. Allergy Information
- [x] Đủ field: Student Identity Header, Allergy Profile List (Card mỗi allergen — đúng Platform Layout Mobile), Verification Status Badge, Read-only Notice — đúng 4 dòng.
- [x] Đúng Design System. KHÔNG có nút chỉnh sửa.

### 05. Health Information
- [x] Đủ field: Student Identity Header, Blood Type Card, Chronic Conditions List, Emergency Contact Note, Read-only Notice — đúng 5 dòng. Giữ nguyên 2 field `[Giả định]`.

### 06. Special Meal Requirements
- [x] Đủ field: danh sách Card học sinh chế độ ăn đặc biệt (đúng Platform Layout Mobile), mỗi card kèm Approval Status Badge, Preparation Note — đúng 4 dòng.

### 07. Meal Records
- [x] Đủ field: Date Range Filter (wired-calendar), danh sách Card lịch sử bữa ăn, Consumption Record Input (modal, Radio Fully/Partially/Not Consumed), Save Consumption Button, Special Meal Compliance Badge — đúng 6 dòng, đúng Platform Layout Mobile ("khối Consumption Record Input + Save button cố định").
- Ghi chú: Screen Spec Mobile #07 có đánh dấu `[Giả định về vị trí điều hướng]` (không có trong sơ đồ mermaid gốc `07_Teacher_Screen_Flow.md`) — giữ nguyên, đã liệt kê trong `00_TODO_GiaDinh_Master.md`.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS trong khung điện thoại (`.phone`, 390px), 7 `<div class="screen">`, điều hướng `showScreen(id)`.
- AppBar (back + title) + BottomNavigationBar 4 mục cố định — đúng Platform Layout Mobile chung của Teacher.
- Grayscale/mono, wired-elements CDN, Patrick Hand, sketchy border, icon SVG doodle inline (không emoji).
- Dữ liệu mẫu giống hệt bản Web (cùng học sinh, cùng số liệu) để đảm bảo nhất quán business logic giữa 2 nền tảng.

---

## 4. Trạng thái nguồn

Nguồn Confirmed (đã qua QA). Xem `00_TODO_GiaDinh_Master.md` cho các mục `[Giả định]` còn tồn đọng (Parent Contact Info, Blood Type, Emergency Contact Note, vị trí điều hướng Meal Records).

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

Không phát hiện thêm mâu thuẫn mới ngoài các mục đã ghi trong `00_TODO_GiaDinh_Master.md`.
