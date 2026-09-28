# Wireframe — Dashboard, Menu & Nutrition (nhóm màn hình Parent, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/Parent/` — 4 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/Parent/01-04_DashboardMenuNutrition.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md` (Dashboard → sidebar "Thực đơn & Dinh dưỡng" → Weekly Menu → click dish → Nutrition Detail; Weekly Menu/Meal History → Nutrition History).

---

## 1. Danh sách 4 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | Parent Dashboard | `01_ParentDashboard.md` | Node gốc, Child Selector ở Topbar |
| 02 | Weekly Menu | `02_WeeklyMenu.md` | Tab bar Mon-Fri, dish card grid |
| 03 | Nutrition Detail | `03_NutritionDetail.md` | Vào từ click dish card |
| 04 | Nutrition History | `04_NutritionHistory.md` | Chart dinh dưỡng |

Tổng cộng: **4 `<div class="screen">`**.

---

## 2. Đối chiếu với Screen Specification

### 01. Parent Dashboard
- [x] Đủ field: Child Selector (Topbar dropdown), Notification Bell, Allergy Threat Alert Box (có điều kiện), Today's Lunch Menu Card, 4-tile grid (Health Hub/School Meals/Meal Feedback/Meal History), Report Urgent Safety Incident Button (destructive full-width) — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(Allergy Threat = viền đậm 3px, nút Report Urgent = viền đậm 3px full-width)*.
- [x] Trạng thái: Empty ("Chưa liên kết hồ sơ học sinh") ghi chú text; Loading/Error tham chiếu MSG-004/005.
- Ghi chú: giữ nguyên ghi chú nguồn về BR-040 (giới hạn số con/phụ huynh không tồn tại chính thức) — đã liệt kê ở `00_TODO_GiaDinh_Master.md` Parent #3.

### 02. Weekly Menu
- [x] Đủ field: Weekly Days Tab Bar (Mon-Fri), Standard Meal Item Card, Allergy Alert Meal Card (viền destructive + MSG-051), View Nutrition Details Button, Download PDF Button — đúng 5 dòng.
- [x] Dish card "Canh bí đỏ" minh hoạ Allergy Alert khớp dữ liệu dị ứng đậu phộng ở Dashboard.
- Ghi chú: giữ nguyên `[Giả định vị trí UI]` cho Download PDF Button, và ghi chú MSG-076 lệch nghĩa (Empty state dùng câu chữ tự do, không gán MSG sai) — đã liệt kê `00_TODO_GiaDinh_Master.md` Parent #6.

### 03. Nutrition Detail
- [x] Đủ field: Dish Information Card (tên/loại/nhà cung cấp/bếp), Full Ingredient Log Section, Allergen Warnings Section (có điều kiện), Give Feedback Button — đúng 4 dòng.
- [x] Trạng thái: Loading/Error tham chiếu MSG-004/005; Empty/Success không áp dụng theo đúng Screen Spec.

### 04. Nutrition History
- [x] Đủ field: Date Range Filter (Tuần/Tháng), Nutrition Trend Chart (placeholder — Option B theo SKILL.md mục 5 vì không sinh ảnh chart thật), Nutrition Target Comparison Card — đúng 4 dòng (bao gồm Page Header).
- [x] Trạng thái: Empty ("Chưa đủ dữ liệu bữa ăn") ghi chú text.
- Ghi chú: giữ nguyên `[Giả định — cần team xác nhận cấu trúc chi tiết]` từ Screen Spec gốc.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA, 4 `<div class="screen">`, điều hướng `showScreen(id)`.
- Grayscale/mono, wired-elements CDN, Patrick Hand, sketchy border.
- Chart dinh dưỡng dùng Option B (`[Chart Placeholder]` viền đứt nét) vì không có công cụ sinh ảnh/chart thật trong phạm vi vanilla HTML — đúng SKILL.md mục 5 Option B.
- Sidebar 5 nhóm của Parent; chỉ "Tổng quan" + "Thực đơn & Dinh dưỡng" active-able trong file này, còn lại disabled kèm ghi chú trỏ sang nhóm 05-10/11-14/15-18.
- Dữ liệu mẫu: con "Trần Gia Bảo (2A)" dùng chung với actor Teacher (cùng học sinh mẫu) để minh hoạ tính nhất quán liên-actor của hệ thống.

## 4. Trạng thái nguồn

Nguồn Confirmed. Các mục `[Giả định]` đã liệt kê ở `00_TODO_GiaDinh_Master.md` (Parent #3, #6, #8) — không phát hiện thêm mâu thuẫn mới.
