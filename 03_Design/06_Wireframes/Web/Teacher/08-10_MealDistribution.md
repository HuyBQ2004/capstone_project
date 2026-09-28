# Wireframe — Meal Distribution (nhóm màn hình Teacher, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/Teacher/` — 3 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/Teacher/08-10_MealDistribution.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md` (nhánh Meal Distribution → select class → Student List → mark served → Confirm Meal Served Dialog). Trong bản Web, Distribution Detail đóng vai trò trung gian giữa List và Confirmation Dialog.

---

## 1. Danh sách 3 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 08 | Distribution List | `08_DistributionList.md` | Node gốc của nhóm; Status Filter Tabs + Table |
| 09 | Distribution Detail | `09_DistributionDetail.md` | Vào từ action "Xem chi tiết" |
| 10 | Distribution Confirmation | `10_DistributionConfirmation.md` | Modal — vào từ Distribution Detail hoặc action nhanh trên List |

Tổng cộng: **2 `<div class="screen">`** (List, Detail) + **1 modal overlay** (Confirmation, đúng Platform Layout "AlertDialog giữa màn hình").

---

## 2. Đối chiếu với Screen Specification

### 08. Distribution List
- [x] Đủ field: Page Header, Status Filter Tabs (All/Pending/Served/Special Meal), Student Meal Status Table (avatar, họ tên, tag chế độ ăn, cảnh báo dị ứng đỏ, status, action), Confirm All Button — đúng 4 dòng Business Logic.
- [x] Đúng Design System *(cảnh báo dị ứng = viền đậm 3px, khớp BR-123)*.
- [x] Đủ trạng thái có ghi chú: Success render 3 dòng mẫu; Empty ("Chưa có phiên phân phát hôm nay") ghi chú text; Error MSG-004/005/021 ghi chú text; Success → Toast MSG-045 ghi chú text.

### 09. Distribution Detail
- [x] Đủ field: Student Identity Header, Assigned Meal Card, Allergy Warning Banner (destructive), Distribution Status Badge, Confirm Served Button, Report Distribution Error Button — đúng 6 dòng.
- [x] Đúng Design System *(Allergy Warning = viền đậm 3px + icon cảnh báo)*.
- [x] Trạng thái: Loading/Error (MSG-004/005) ghi chú text; Empty/Success không áp dụng theo đúng Screen Spec.

### 10. Distribution Confirmation
- [x] Đủ field: Confirmation Dialog Title (đổi theo cá nhân/cả lớp), Timestamp Display (tự động), Duplicate Scan Check (ẩn, MSG-021 khi chặn), Cancel Button, Confirm Button — đúng 5 dòng.
- [x] Dựng dạng `AlertDialog` giữa màn hình, đúng Platform Layout.
- [x] Trạng thái: Loading (spinner trong dialog) ghi chú text; Error (MSG-021) ghi chú text; Success (Toast MSG-045) — bấm Confirm quay về Distribution List.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA, 2 `<div class="screen">` + 1 modal, điều hướng `showScreen(id)`.
- Grayscale/mono, wired-elements CDN, Patrick Hand, sketchy border, icon SVG doodle inline.
- Sidebar chỉ "Meal Distribution" active-able trong file này; Student List/Special Meal/Meal Records/Teacher Home disabled kèm ghi chú trỏ sang nhóm `01-07_HomeAndStudentRecords.html`.
- Dữ liệu mẫu (Trần Gia Bảo, dị ứng đậu phộng) khớp với nhóm 01-07 để nhất quán xuyên suốt actor.

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới.
