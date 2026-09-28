# Wireframe — Health & Allergy (nhóm màn hình Parent, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/Parent/` — 6 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Mobile/Parent/05-10_HealthAndAllergy.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` (Dashboard "Health Hub" tile hoặc bottom-nav "Sức khỏe" → Student Health Information → Manage Health/Allergy → Submit/Update full-screen).

---

## 1. Danh sách 6 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 05 | Submit Allergy Information | `05_SubmitAllergyInformation.md` | Full-screen form — bundled cùng #06 (toggle) |
| 06 | Update Allergy Information | `06_UpdateAllergyInformation.md` | Giống #05 nhưng pre-filled |
| 07 | Student Health Information | `07_StudentHealthInformation.md` | Node gốc, AppBar + BottomNav |
| 08 | Submit Health Information | `08_SubmitHealthInformation.md` | Full-screen form — bundled cùng #09 |
| 09 | Update Health Information | `09_UpdateHealthInformation.md` | Giống #08 nhưng pre-filled |
| 10 | Special Meal Arrangements | `10_SpecialMealArrangements.md` | AppBar đỏ nếu là cảnh báo |

Tổng cộng: **4 `<div class="screen">`** (Student Health Information, Submit/Update Allergy bundled, Submit/Update Health bundled, Special Meal Arrangements) — khác bản Web (dùng modal), ở Mobile các màn Submit/Update là full-screen riêng đúng Platform Layout Mobile ("AppBar (back) → form 1 cột... Save Button cố định đáy").

---

## 2. Đối chiếu với Screen Specification

### 05+06. Submit/Update Allergy Information
- [x] Đủ field: Allergen Category, Specific Allergen Name, Severity Radio, Upload Medical Proof, Save Allergy Button cố định đáy — đúng Platform Layout Mobile.

### 07. Student Health Information
- [x] Đủ field: Student Summary Card, 4 Metric Card (2x2 grid — đúng Platform Layout Mobile), Allergy Profile Summary Card, 2 nút hành động full-width, BottomNavigationBar — đúng 6 dòng.

### 08+09. Submit/Update Health Information
- [x] Đủ field: Height, Weight, Blood Type, File Upload, Save Health Info Button cố định đáy.

### 10. Special Meal Arrangements
- [x] Đủ field: AppBar tiêu đề đỏ nếu cảnh báo (đúng Platform Layout Mobile), Allergen Warning Card, Special Meal Detail List, Dual-Confirmation Status, Parental Advisory Note, link Submit Allergy Information.

---

## 3. Ghi chú kỹ thuật

- Khung điện thoại 390px; Submit/Update dựng full-screen (không modal) đúng đặc thù Mobile.
- Grayscale, wired-elements, Patrick Hand, sketchy border, dropzone giả lập.
- Dữ liệu mẫu nhất quán bản Web.

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới.
