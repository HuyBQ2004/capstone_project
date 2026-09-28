# Wireframe — Compliance &amp; Certificates (nhóm màn hình Raw Food Supplier)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/RawFoodSupplier/` — 5 file Screen Specification (Draft — xem mục 4).
**Công cụ sinh**: Thủ công — theo skill `wireframer`, vanilla HTML/JS single-file SPA, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/RawFoodSupplier/04_ComplianceCertificates.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md` (Dashboard → Food Safety Compliance Record → Food Safety Certificate Detail / Product Origin Certification Detail → Upload Certificates Form).

---

## 1. Danh sách 5 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 26 | Compliance Documents | `26_ComplianceDocuments.md` | Màn hub, đặt làm màn mặc định (active) trong file này |
| 04 | Product Origin Documents | `04_ProductOriginDocuments.md` | UC-102 |
| 05 | Food Safety Certificates | `05_FoodSafetyCertificates.md` | UC-101 |
| 06 | Upload Supplier Certificates | `06_UploadSupplierCertificates.md` | UC-101 + UC-102, form dùng chung |
| 25 | Update Documents | `25_UpdateDocuments.md` | UC-091 |

Tổng cộng: **5 `<div class="screen">`**, không có modal overlay (không có hành động Approve/Reject ở nhóm này — đó là quyền của School Administration, không phải Raw Food Supplier).

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 26. Compliance Documents
- [x] Đủ field: Back, Add/Plus, 2 Card điều hướng (Food Safety Certificate/Product Origin Certification), tóm tắt số lượng theo trạng thái, danh sách filter được — đúng 7 dòng Business Logic.
- [x] Đúng Design System *(counter card không dùng màu, chỉ số + label)*.
- [x] Đủ trạng thái: Success là chính; Warning (MSG-053) thể hiện ở dòng bảng mẫu; Empty không dựng riêng (đã có ở màn 04); Loading/Error tham chiếu MSG-004/005.

### 04. Product Origin Documents
- [x] Đủ field: Back, Add/Plus, danh sách (tên, loại, ngày tải lên, hết hạn, trạng thái) — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(badge Valid=viền liền, Không thời hạn=viền chấm/info)*.
- [x] Đủ trạng thái: Success (2 dòng mẫu) + Empty (dựng đầy đủ card empty-state) đều có trong file này; Loading/Error tham chiếu MSG.

### 05. Food Safety Certificates
- [x] Đủ field: Back, Add/Plus, filter trạng thái, danh sách (tên, cơ quan cấp, ngày cấp, hết hạn, trạng thái) — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(badge Expiring Soon viền đứt nét kèm text MSG-053)*.
- [x] Đủ trạng thái: Warning là trọng tâm minh hoạ; Success/Empty không lặp lại (đã có ở màn 04, tránh trùng lặp không cần thiết).

### 06. Upload Supplier Certificates
- [x] Đủ field: dropdown phân loại, tên tài liệu/cơ quan cấp, file upload, ngày hết hạn, nút Add/Plus, Submit/Cancel — đúng 11 dòng Business Logic.
- [x] Đúng Design System *(Option B `[Image Placeholder]`, `<wired-select>` cho dropdown phân loại, `<wired-calendar>` cho ngày hết hạn)*.
- [x] Đủ trạng thái: Error (MSG-013 khi loại=Food Safety Certificate + hết hạn, MSG-009 sai định dạng) dựng đầy đủ bằng alert cạnh form; Success điều hướng về Compliance Documents.

### 25. Update Documents
- [x] Đủ field: thông tin tài liệu hiện tại (chỉ đọc), file thay thế, ngày hết hạn mới, nút Submit/Cancel — đúng 7 dòng Business Logic.
- [x] Đúng Design System *(badge Expiring Soon giữ nguyên ở khối thông tin cũ để đối chiếu trước/sau)*.
- [x] Đủ trạng thái: Error (MSG-009, kèm ghi chú lệch nguồn MSG-013 vs MSG-009 đã nêu trong Screen Spec) dựng đầy đủ; Success điều hướng về Food Safety Certificates.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS, 5 `<div class="screen">`, điều hướng `showScreen(id)`.
- Grayscale/mono; `wired-elements` qua CDN; font `Patrick Hand`; sketchy border; Option B `[Image Placeholder]` cho mọi file upload (không sinh ảnh thật).
- Badge phân biệt bằng kiểu viền (liền/đứt nét/đậm/chấm) + icon doodle, không dùng màu.
- Sidebar giữ đúng 6 nhóm RFS, chỉ nhóm "Tuân thủ & Chứng nhận" active/click được trong file này.

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

1. **25_UpdateDocuments.md**: Report 3 UC-091 Exceptions ghi MSG-013 nhưng Alternative Flow mô tả hành vi khớp MSG-009 hơn (Invalid Document Format) — Wireframe dùng MSG-009, giữ nguyên ghi chú lệch nguồn hiển thị ngay trong khối alert của màn Update Documents để không giấu vấn đề.
2. **04_ProductOriginDocuments.md**: Report 3 UC-102 Exceptions liệt kê MSG-013 nhưng `traceability_matrix.md` dòng 116 không có — Wireframe không hiển thị MSG-013 ở nhóm Product Origin Documents (chỉ MSG-008/009), theo đúng quyết định đã ghi trong Screen Spec nguồn.
