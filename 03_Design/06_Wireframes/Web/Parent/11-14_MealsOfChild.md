# Wireframe — Meals of Child (nhóm màn hình Parent, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/Parent/` — 4 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/Parent/11-14_MealsOfChild.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md` (Sidebar "Bữa ăn của con" → School Meals Dashboard / Meal History → click hàng → Meal History Detail; Meal History → Special Meal Records/Weekly Charts).

---

## 1. Danh sách 4 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 11 | Special Meal Records | `11_SpecialMealRecords.md` | Node hiển thị mặc định trong file (`active`) |
| 12 | Meal History | `12_MealHistory.md` | Table + Date Filter |
| 13 | Meal History Detail | `13_MealHistoryDetail.md` | Vào từ click hàng Meal History |
| 14 | School Meals Dashboard | `14_SchoolMealsDashboard.md` | Card grid 2 tuần |

Tổng cộng: **4 `<div class="screen">`** + **1 modal overlay** (Record Detail Dialog trong Special Meal Records).

---

## 2. Đối chiếu với Screen Specification

### 11. Special Meal Records
- [x] Đủ field: Page Header, Special Meal Record Table (ngày/món gốc/món thay thế/allergen/badge xác nhận kép), Record Detail Dialog (ghi chú chuẩn bị/timestamp/trạng thái tiêu thụ) — đúng 3 dòng.
- [x] Trạng thái: Empty ("Chưa có lịch sử bữa ăn đặc biệt") ghi chú text.

### 12. Meal History
- [x] Đủ field: Page Header + Date Range Filter, Calendar/Date Filter, Meal History Table (ngày/tỷ lệ tiêu thụ/loại khẩu phần/hài lòng/action), Weekly Charts Button, Special Meals History Button — đúng 5 dòng.

### 13. Meal History Detail
- [x] Đủ field: Page Header (ngày), Served Dish Table, Consumption Status Badge, Serving Timestamp, Special Meal Applied Note (điều kiện) — đúng 5 dòng.

### 14. School Meals Dashboard
- [x] Đủ field: Page Header, Current & Upcoming Week Menu Summary (2 card), Nutrition & Allergen Highlight, View Weekly Menu Link, View Nutrition Breakdown Link — đúng 5 dòng.
- Ghi chú: giữ nguyên `[Giả định]` cho Empty state ("Chưa có thực đơn được công bố") từ Screen Spec gốc.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA, 4 `<div class="screen">` + 1 modal, điều hướng `showScreen(id)`.
- Grayscale/mono, wired-elements (`wired-calendar`), Patrick Hand, sketchy border.
- Sidebar "Bữa ăn của con" active-able; các nhóm khác disabled trỏ sang nhóm 01-04/05-10/15-18.
- Dữ liệu mẫu nhất quán (Gia Bảo, đậu phộng thay hạt điều, gỏi cuốn tôm thay chay).

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới.
