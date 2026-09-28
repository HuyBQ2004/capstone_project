# Wireframe — Dashboard, Menu & Nutrition (nhóm màn hình Parent, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/Parent/` — 4 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Mobile/Parent/01-04_DashboardMenuNutrition.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md`.

---

## 1. Danh sách 4 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | Parent Dashboard | `01_ParentDashboard.md` | AppBar (Child Selector + Bell) + 2x2 Tile grid + BottomNav |
| 02 | Weekly Menu | `02_WeeklyMenu.md` | Card list món ăn theo ngày |
| 03 | Nutrition Detail | `03_NutritionDetail.md` | Vào từ tap món ăn |
| 04 | Nutrition History | `04_NutritionHistory.md` | Chart placeholder |

Tổng cộng: **4 `<div class="screen">`**.

**Ghi chú thiết kế BottomNavigationBar**: Screen Spec chỉ ghi "BottomNavigationBar 4 mục" mà không liệt kê tên cụ thể — nhóm này chọn 4 mục **Home/Menu/Sức khỏe/Phản hồi** (ánh xạ gần nhất với 4 trong 5 nhóm Sidebar bên Web: Tổng quan, Thực đơn & Dinh dưỡng, Sức khỏe & Dị ứng, Phản hồi & Sự cố), còn "Bữa ăn của con" (Meal History/School Meals) truy cập qua tile trên Dashboard thay vì slot riêng trên BottomNav — đây là quyết định bố cục hợp lý, không phải field nghiệp vụ, nên không cần đánh dấu Giả định thêm.

---

## 2. Đối chiếu với Screen Specification

### 01. Parent Dashboard
- [x] Đủ field: Child Selector (AppBar trái), Notification Bell (AppBar phải), Allergy Threat Alert Box, Today's Lunch Menu Card, 4 Navigation Tile (2x2 grid — đúng Platform Layout Mobile), Report Urgent Safety Incident Button — đúng 6 dòng.

### 02. Weekly Menu
- [x] Đủ field: Weekly Days Tab Bar (Chip), Card list món ăn theo ngày (thay Card grid Web), Allergy Alert Meal Card, View Nutrition Details Button cố định đáy — đúng 5 dòng, đúng Platform Layout Mobile.
- Ghi chú: Download PDF Button không xuất hiện trong Platform Layout Mobile của Screen Spec gốc (chỉ có ở Web) — giữ nguyên theo đúng nguồn, không tự thêm.

### 03. Nutrition Detail
- [x] Đủ field: Dish Information Card, Full Ingredient Log Section, Allergen Warnings Section, Give Feedback Button cố định đáy — đúng 4 dòng.

### 04. Nutrition History
- [x] Đủ field: Date Range Filter, Nutrition Trend Chart (placeholder), Nutrition Target Comparison Card — đúng 4 dòng.

---

## 3. Ghi chú kỹ thuật

- Khung điện thoại 390px, AppBar + BottomNavigationBar, grayscale/wired-elements/Patrick Hand.
- Chart dùng Option B placeholder (SKILL.md mục 5).
- Dữ liệu mẫu nhất quán với bản Web (Gia Bảo 2A, đậu phộng, canh bí đỏ).

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới ngoài các mục đã ghi ở `00_TODO_GiaDinh_Master.md`.
