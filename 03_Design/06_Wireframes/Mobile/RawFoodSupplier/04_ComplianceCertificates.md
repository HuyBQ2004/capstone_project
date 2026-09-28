# Wireframe — Compliance &amp; Certificates (nhóm màn hình Raw Food Supplier, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/RawFoodSupplier/` — 5 file Screen Specification (Draft).
**Công cụ sinh**: Thủ công — skill `wireframer`, vanilla HTML/JS SPA, khung phone-frame nhất quán với `01_SupplierOnboardingPartnership.html` (Mobile).
**File/Link**: `03_Design/06_Wireframes/Mobile/RawFoodSupplier/04_ComplianceCertificates.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (Dashboard → Food Safety Compliance Record → Food Safety Certificate Detail / Product Origin Certification Detail → Upload New Certificates).

---

## 1. Danh sách 5 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 26 | Compliance Documents | `26_ComplianceDocuments.md` | Màn mặc định, có FAB "+" |
| 04 | Product Origin Documents | `04_ProductOriginDocuments.md` | UC-102 |
| 05 | Food Safety Certificates | `05_FoodSafetyCertificates.md` | UC-101 |
| 06 | Upload Supplier Certificates | `06_UploadSupplierCertificates.md` | UC-101 + UC-102 |
| 25 | Update Documents | `25_UpdateDocuments.md` | UC-091 |

---

## 2. Đối chiếu với Screen Specification

### 26. Compliance Documents
- [x] Đủ field: 2 Card điều hướng, tóm tắt trạng thái, danh sách filter — đúng 7 dòng Business Logic. FAB "+" mở Upload Supplier Certificates theo đúng Platform Layout — Mobile của Screen Spec.
- [x] Đúng Design System *(counter card không màu, chip theo kiểu viền)*.
- [x] Đủ trạng thái: Success/Warning dựng đủ; Empty không lặp (đã ở màn 04).

### 04. Product Origin Documents
- [x] Đủ field: danh sách Card (tên, loại, ngày tải lên, hết hạn, trạng thái) — đúng 6 dòng Business Logic.
- [x] Đúng Design System. [x] Đủ trạng thái: Success + Empty dựng đủ.

### 05. Food Safety Certificates
- [x] Đủ field: danh sách Card + chip Expiring Soon (MSG-053) — đúng 6 dòng Business Logic.
- [x] Đúng Design System. [x] Warning là trọng tâm.

### 06. Upload Supplier Certificates
- [x] Đủ field: dropdown phân loại, tên/cơ quan cấp, file, ngày hết hạn, Add/Submit/Cancel — đúng 11 dòng Business Logic, dựng dạng trang riêng (không modal bottom sheet trong bản demo tĩnh này, ghi chú kỹ thuật mục 3).
- [x] Đúng Design System *(Option B placeholder)*. [x] Error MSG-013/MSG-009 dựng đủ.

### 25. Update Documents
- [x] Đủ field: thông tin cũ (chỉ đọc), file thay thế, ngày hết hạn mới, Submit/Cancel — đúng 7 dòng Business Logic.
- [x] Đúng Design System. [x] Error (MSG-009, ghi chú lệch nguồn) dựng đủ.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Cùng khung `.phone` 400px như nhóm 01; 5 `<div class="screen">`.
- FAB xuất hiện lần đầu ở nhóm này (hành động "+" tạo tài liệu mới), theo đúng `Layout_Guidelines.md` §2.1 (FAB cho hành động chính của màn hình).
- Trong Screen Spec Mobile, Upload Supplier Certificates/Update Documents được thiết kế dạng `showModalBottomSheet` — bản wireframe tĩnh này dựng dạng trang riêng trong cùng SPA để đơn giản hoá điều hướng click-through (không ảnh hưởng nội dung field, chỉ khác cơ chế hiển thị theo phương tiện của bản nháp — sẽ đúng dạng bottom sheet khi lên Prototype Figma thật ở `07_Prototype/`).

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

Giống bản Web cùng nhóm (`Web/RawFoodSupplier/04_ComplianceCertificates.md` mục 5).
