# Wireframe — Meal Service & Special Meals (nhóm màn hình Service Caterers, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/ServiceCaterers/24,25,26,27_*.md` — 4 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), khung phone-frame 390px.
**File/Link**: `03_Design/06_Wireframes/Mobile/ServiceCaterers/24-27_MealServiceSpecialMeals.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` — nhánh `Grid → MealDash(24) → DistList(25)/AllergyPanel(26)`; `27` vào từ Dashboard hoặc từ Update Menu Screen (`UpdateMenuScr --> SpecialMealCfg`, đã build ở nhóm 01-06, không lặp lại điểm vào đó ở đây).

---

## 1. Danh sách 4 màn hình (6 `<div class="screen">` — có 2 màn phụ suy ra từ field nội bộ)

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 24 | Meal Service Management Dashboard | `24_MealServiceManagementDashboard.md` | Screen `#dashboard` (entry) |
| — | Chi tiết Phiên phục vụ | field #2 của Screen Spec 24 (không có file riêng) | Screen `#detail` |
| 25 | View Meal Distribution List | `25_ViewMealDistributionList.md` | Screen `#distlist` |
| 26 | View Allergy Alerts Panel | `26_ViewAllergyAlertsPanel.md` | Screen `#allergy` |
| — | Chi tiết Dị ứng 1 học sinh | field #3 của Screen Spec 26 (không có file riêng) | Screen `#allergydetail` |
| 27 | Manage Special Meals Configuration | `27_ManageSpecialMealsConfiguration.md` | Screen `#specialmeal` |

---

## 2. Đối chiếu với Screen Specification

### 24. Meal Service Management Dashboard
- [x] Đủ field: card list 2 phiên mẫu (Đang phục vụ/Dự kiến), 2 icon AppBar (Danh sách Phân phát/Cảnh báo Dị ứng), chi tiết phân bổ khi chạm card (`#detail`: banner session Active — chỉ sửa nhân sự, banner lệch số suất BR-121, field giờ/nhân sự/số suất đã khoá) — đúng 6 dòng Business Logic.

### 25. View Meal Distribution List
- [x] Đủ field: filter chip, card list 2 lớp (1 có badge dị ứng danger, 1 highlight warning "GV chưa xác nhận" BR-120), icon xuất báo cáo — đúng 5 dòng Business Logic.

### 26. View Allergy Alerts Panel
- [x] Đủ field: banner xung đột thực đơn (MSG-051), card list 2 học sinh (1 Severe nổi bật + chip xác nhận kép, 1 Moderate), điều hướng chi tiết (`#allergydetail`) — đúng 5 dòng Business Logic.

### 27. Manage Special Meals Configuration
- [x] Đủ field: card học sinh đã xác minh, select loại chế độ ăn, textarea công thức riêng (BR-082), textarea nguyên liệu/dị ứng tiềm ẩn, cảnh báo MSG-051 chặn lưu (dựng ở trạng thái Failed để minh hoạ rule quan trọng nhất — BR-063), nút AI Assist, nút "Lưu Kế hoạch" — đúng 7 dòng Business Logic (đã bỏ field "món thay thế cho 5 dị ứng phổ biến" theo cập nhật 2026-07-01, xem Screen Spec 27).
- Kế thừa nguyên văn cảnh báo mâu thuẫn nguồn của Screen Spec 27 (BR-071 sai, dùng BR-082). BR-081 (món thay thế dị ứng) đã bị xoá khỏi `business_rules.md` — field tương ứng đã gỡ khỏi wireframe.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, khung `.phone-frame` (390px), 6 `<div class="screen">`, không modal.
- 2 màn phụ (`#detail`, `#allergydetail`) suy ra tối thiểu từ đúng câu chữ field #2/#3 của Screen Spec gốc (24, 26) — không tự thêm field ngoài mô tả.

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 4 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **24_MealServiceManagementDashboard.md — field #2 "Chi tiết phân bổ suất ăn theo lớp" không có Screen Spec riêng dù ghi rõ "trang riêng"**: tương tự phát hiện đã báo ở nhóm Food Sample Retention (field dạng form/trang nhưng không tách file) — cần Thảo xác nhận có cần tách Screen Spec riêng cho toàn bộ các trường hợp tương tự trong dự án (đã gặp ít nhất 3 lần: 23_FoodSampleRetentionDashboard field #2/#5, 24_MealServiceManagementDashboard field #2, 26_ViewAllergyAlertsPanel field #3).
2. **26_ViewAllergyAlertsPanel.md — field #3 "Xem chi tiết dị ứng 1 học sinh" cũng không có Screen Spec riêng**: cùng loại phát hiện như trên.
