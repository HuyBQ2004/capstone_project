# Wireframe — Meal Distribution (nhóm màn hình Teacher, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/Teacher/` — 3 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Mobile/Teacher/08-10_MealDistribution.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` (Dash → Distribute Meals Today → Meal Distribution → select class → mark served → Confirm Meal Served Dialog).

---

## 1. Danh sách 3 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 08 | Distribution List | `08_DistributionList.md` | AppBar + Card list cuộn dọc + BottomNav |
| 09 | Distribution Detail | `09_DistributionDetail.md` | Vào từ tap "Details" |
| 10 | Distribution Confirmation | `10_DistributionConfirmation.md` | `showModalBottomSheet` (Material 3) trượt từ dưới lên — đúng Platform Layout Mobile |

Tổng cộng: **2 `<div class="screen">`** + **1 bottom-sheet overlay** (khác dạng modal-center của bản Web — đúng đặc thù Platform Layout Mobile).

---

## 2. Đối chiếu với Screen Specification

### 08. Distribution List
- [x] Đủ field: Status Filter Tabs (Chip), Student Meal Status Card list (thay Table), Confirm All Button — đúng 4 dòng, đúng Platform Layout Mobile ("danh sách Card học sinh cuộn dọc").

### 09. Distribution Detail
- [x] Đủ field: Student Identity Header, Assigned Meal Card, Allergy Warning Banner, Distribution Status Badge, Confirm Served Button, Report Distribution Error Button — đúng 6 dòng.

### 10. Distribution Confirmation
- [x] Đủ field: Dialog Title, Timestamp Display, Duplicate Scan Check (MSG-021), Cancel/Confirm Button — đúng 5 dòng. Dựng dạng bottom sheet Material 3 (khác AlertDialog center của Web) — đúng ghi chú Platform Layout Mobile riêng.

---

## 3. Ghi chú kỹ thuật

- Khung điện thoại 390px, AppBar + BottomNavigationBar (Home/Students/Meals/Incidents), bottom sheet cho Confirmation.
- Grayscale, wired-elements, Patrick Hand, sketchy border, icon SVG doodle.
- Dữ liệu mẫu nhất quán với bản Web và nhóm 01-07 Mobile.

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới.
