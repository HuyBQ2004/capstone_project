# Wireframe — Meals of Child (nhóm màn hình Parent, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/Parent/` — 4 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Mobile/Parent/11-14_MealsOfChild.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` (Dashboard tile "School Meals"/"Meal History" → Meal History Calendar Strip → chọn ngày → Meal History Detail; Meal History → Special Meal Records/Weekly Charts).

---

## 1. Danh sách 4 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 11 | Special Meal Records | `11_SpecialMealRecords.md` | Card list + tap mở bottom sheet |
| 12 | Meal History | `12_MealHistory.md` | Calendar Strip Component (thay DatePicker Web) |
| 13 | Meal History Detail | `13_MealHistoryDetail.md` | Vào từ chọn ngày trên Calendar Strip |
| 14 | School Meals Dashboard | `14_SchoolMealsDashboard.md` | Card list 2 tuần |

Tổng cộng: **4 `<div class="screen">`** + **1 bottom-sheet overlay** (Record Detail — khác Dialog center của Web, đúng Platform Layout Mobile).

---

## 2. Đối chiếu với Screen Specification

### 11. Special Meal Records
- [x] Đủ field: Card list phiên bữa ăn đặc biệt cuộn dọc, tap mở chi tiết dạng bottom sheet — đúng Platform Layout Mobile.

### 12. Meal History
- [x] Đủ field: Instruction Text, Calendar Strip Component, Meal History Summary Card, 2 nút điều hướng (Weekly Charts/Special Meals History) — đúng Platform Layout Mobile (khác Table+DatePicker của Web).

### 13. Meal History Detail
- [x] Đủ field: AppBar hiển thị ngày, Served Dish List, Consumption Status Badge + Serving Timestamp, Special Meal Applied Note — đúng Platform Layout Mobile.

### 14. School Meals Dashboard
- [x] Đủ field: Current & Upcoming Week Menu Summary (Card list — đúng Platform Layout Mobile), Nutrition & Allergen Highlight, 2 nút điều hướng.

---

## 3. Ghi chú kỹ thuật

- Khung điện thoại 390px; Calendar Strip dựng bằng dãy `.cal-day` thay vì `wired-calendar` để khớp đúng mô tả "Calendar Strip Component" của Screen Spec Mobile (khác Web dùng `wired-calendar` đầy đủ).
- Grayscale, wired-elements, Patrick Hand, sketchy border, bottom sheet cho Record Detail.
- Dữ liệu mẫu nhất quán bản Web.

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới.
