# Wireframe — Health & Allergy (nhóm màn hình Parent, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/Parent/` — 6 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/Parent/05-10_HealthAndAllergy.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md` (Sidebar "Sức khỏe & Dị ứng" → Student Health Information → Manage Health Details/Manage Allergy List → Submit/Update dialog; Special Meal Arrangements truy cập độc lập từ Sidebar hoặc Dashboard/Weekly Menu).

---

## 1. Danh sách 6 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 05 | Submit Allergy Information | `05_SubmitAllergyInformation.md` | Modal, chế độ tạo mới — bundled cùng #06 (toggle) |
| 06 | Update Allergy Information | `06_UpdateAllergyInformation.md` | Modal, chế độ chỉnh sửa (pre-filled) — cùng UI với #05 theo đúng ghi chú nguồn |
| 07 | Student Health Information | `07_StudentHealthInformation.md` | Node gốc/hub của nhóm |
| 08 | Submit Health Information | `08_SubmitHealthInformation.md` | Modal, chế độ tạo mới — bundled cùng #09 |
| 09 | Update Health Information | `09_UpdateHealthInformation.md` | Modal, chế độ chỉnh sửa (pre-filled) — cùng UI với #08 |
| 10 | Special Meal Arrangements | `10_SpecialMealArrangements.md` | Màn riêng, truy cập từ Sidebar/Dashboard/Weekly Menu |

Tổng cộng: **2 `<div class="screen">`** (Student Health Information, Special Meal Arrangements) + **2 modal overlay** (Allergy modal bundling #05+#06, Health modal bundling #08+#09 — mỗi modal có nút toggle "Chế độ tạo mới/Chế độ chỉnh sửa" để minh hoạ cả 2 Screen Spec dùng chung 1 UI, đúng ghi chú nguồn "dùng chung UI với..." của cả 4 file).

---

## 2. Đối chiếu với Screen Specification

### 05 + 06. Submit/Update Allergy Information
- [x] Đủ field chung: Allergen Category Select, Specific Allergen Name Input (điều kiện Other), Severity Level Radio Group, Upload Medical Proof Component (bắt buộc nếu Severe), Save Allergy Button — đúng 5 dòng mỗi bản.
- [x] Toggle "Chế độ tạo mới"/"Chế độ chỉnh sửa" minh hoạ khác biệt duy nhất giữa 2 Screen Spec: #06 pre-fill dữ liệu hiện có, #05 để trống — đúng ghi chú nguồn "dùng chung UI".
- [x] Trạng thái: Error (MSG-005, `[Giả định — chưa có MSG riêng cho thiếu tài liệu Severe]` giữ nguyên từ #05) / Success (Toast MSG-036).

### 07. Student Health Information
- [x] Đủ field: Student Summary Card, 4-Metric Card grid (Height/Weight/Blood Type/BMI), Allergy Profile Summary Table, Verification Status Badge, Manage Health Details Button, Manage Allergy List Button — đúng 6 dòng.
- [x] 2 nút hành động mở đúng 2 modal tương ứng.
- [x] Trạng thái: Empty ("Chưa khai báo thông tin sức khỏe") ghi chú text.

### 08 + 09. Submit/Update Health Information
- [x] Đủ field chung: Height, Weight, Blood Type Select, File Upload Field, Save Health Info Button — đúng 5 dòng mỗi bản.
- [x] Toggle tạo mới/chỉnh sửa minh hoạ khác biệt pre-fill, đúng ghi chú nguồn.

### 10. Special Meal Arrangements
- [x] Đủ field: Page Header, Allergen Warning Card (destructive, MSG-051), Special Meal Detail Table (allergen/mức độ/món thay thế/ghi chú), Dual-Confirmation Status (2 badge: Kitchen Released/Teacher Hand-off), Parental Advisory Note (italic), Submit Allergy Information Link — đúng 6 dòng.
- [x] Link "Cập nhật/khai báo dị ứng mới" mở đúng Allergy modal.
- [x] Trạng thái: Empty ("Học sinh chưa có kế hoạch bữa ăn đặc biệt nào") ghi chú text.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA, 2 `<div class="screen">` + 2 modal (mỗi modal bundling 2 Screen Spec tạo/sửa qua nút toggle JS), điều hướng `showScreen(id)`.
- Grayscale/mono, wired-elements CDN (`wired-select`, `wired-radio-group`, `wired-input`), Patrick Hand, sketchy border, dropzone giả lập cho Upload Medical Proof/File Upload.
- Sidebar 5 nhóm Parent; chỉ "Sức khỏe & Dị ứng" active-able trong file này.
- Dữ liệu mẫu nhất quán: Trần Gia Bảo, dị ứng đậu phộng Severe (khớp nhóm 01-04).

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới ngoài `[Giả định — chưa có MSG riêng cho thiếu tài liệu Severe]` đã có sẵn trong Screen Spec #05 (đã liệt kê `00_TODO_GiaDinh_Master.md` Parent #5).
