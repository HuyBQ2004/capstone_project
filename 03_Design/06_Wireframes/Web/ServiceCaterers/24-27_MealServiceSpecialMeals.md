# Wireframe — Meal Service & Special Meals (nhóm màn hình Service Caterers, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/ServiceCaterers/24,25,26,27_*.md` — 4 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), layout Sidebar + Topbar giống mẫu `Web/SchoolAdministration/`.
**File/Link**: `03_Design/06_Wireframes/Web/ServiceCaterers/24-27_MealServiceSpecialMeals.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` — nhánh `Dash → MealDash(24) → DistList(25)/AllergyPanel(26)/SpecialCfg(27)`.

---

## 1. Danh sách 4 Screen Spec → 4 screen + 2 sheet trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 24 | Meal Service Management Dashboard | `24_MealServiceManagementDashboard.md` | Screen `#dashboard`, Table + Sheet chi tiết bên phải khi chọn 1 dòng (đúng Platform Layout — Web) |
| 25 | View Meal Distribution List | `25_ViewMealDistributionList.md` | Screen `#distlist` |
| 26 | View Allergy Alerts Panel | `26_ViewAllergyAlertsPanel.md` | Screen `#allergy` + Sheet chi tiết 1 học sinh |
| 27 | Manage Special Meals Configuration | `27_ManageSpecialMealsConfiguration.md` | Screen `#specialmeal` (bố cục 2 cột: Table học sinh + form) |

---

## 2. Đối chiếu với Screen Specification

### 24. Meal Service Management Dashboard
- [x] Đủ field: Table 2 dòng (Đang phục vụ/Dự kiến), nút Xem Danh sách Phân phát/Xem Cảnh báo Dị ứng, Sheet chi tiết (banner session Active, banner lệch số suất BR-121, field giờ/nhân sự/số suất đã khoá) đúng Platform Layout — Web ("panel chi tiết mở bên phải khi chọn 1 dòng") — đúng 6 dòng Business Logic.

### 25. View Meal Distribution List
- [x] Đủ field: filter bar, Table 2 lớp (badge dị ứng danger + highlight hàng "GV chưa xác nhận"), nút Xuất PDF/Excel — đúng 5 dòng Business Logic.

### 26. View Allergy Alerts Panel
- [x] Đủ field: banner xung đột thực đơn, Table 2 học sinh (Severe/Moderate, badge xác nhận kép), Sheet chi tiết khi click dòng — đúng 5 dòng Business Logic.

### 27. Manage Special Meals Configuration
- [x] Đủ field: Table học sinh xác minh (cột trái), form cấu hình đầy đủ (loại chế độ ăn, công thức riêng, nguyên liệu/dị ứng tiềm ẩn, cảnh báo MSG-051 chặn — Failed, nút AI Assist, nút Lưu Kế hoạch) — đúng 7 dòng Business Logic (đã bỏ field "món thay thế cho 5 dị ứng phổ biến" theo cập nhật 2026-07-01, xem Screen Spec 27), bố cục 2 cột đúng Platform Layout — Web.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS; Sidebar chỉ nhóm "Phục vụ Bữa ăn" active/click được (bao gồm cả 27 vì đây là điểm vào phụ hợp lệ theo Screen Spec 27: "hoặc từ Meal Service Management Dashboard").
- 2 Sheet trượt phải (`#detail-sheet`, `#allergy-detail-sheet`) dùng `.sheet-overlay`/`.sheet-panel` giống pattern đã dùng ở nhóm Menu Management (AI Panel).

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 4 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **Kế thừa từ bản Mobile** (`Mobile/ServiceCaterers/24-27_MealServiceSpecialMeals.md` mục 5): field #2 của 24 và field #3 của 26 mô tả là "trang riêng"/"điều hướng chi tiết" nhưng không có Screen Spec riêng — đã là phát hiện lặp lại lần thứ 3 trong dự án (cùng loại với 23_FoodSampleRetentionDashboard), đề xuất Thảo tổng hợp thành 1 quy tắc chung: "field mô tả mở trang/form riêng phải có Screen Spec 1-1" để áp dụng nhất quán từ nay.
