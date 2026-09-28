# Wireframe — Ingredient Batch Management (nhóm màn hình Raw Food Supplier)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/RawFoodSupplier/` — 6 file Screen Specification (Draft).
**Công cụ sinh**: Thủ công — skill `wireframer`, vanilla HTML/JS SPA.
**File/Link**: `03_Design/06_Wireframes/Web/RawFoodSupplier/12_IngredientBatchManagement.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md` (Dashboard → Ingredient Batches List → Ingredient Batch Detail → Upload Certificates Form).

---

## 1. Danh sách 6 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 12 | Ingredient Batches | `12_IngredientBatches.md` | Entry |
| 13 | Batch List | `13_BatchList.md` | UC-043 |
| 14 | Batch Detail | `14_BatchDetail.md` | Bundled UC-043 |
| 15 | Submit Batch Information | `15_SubmitBatchInformation.md` | UC-081 |
| 16 | Upload Batch Certificates | `16_UploadBatchCertificates.md` | UC-100 |
| 17 | View Batch History | `17_ViewBatchHistory.md` | UC-107 |

Tổng cộng: **6 `<div class="screen">`**, không có modal (mọi form đều là trang riêng dạng Dialog theo Screen Spec, dựng thành trang SPA để điều hướng liền mạch).

---

## 2. Đối chiếu với Screen Specification

### 12. Ingredient Batches
- [x] Đủ field: bộ đếm nhanh theo trạng thái lô, Add/Plus icon — đúng 5 dòng Business Logic.
- [x] Đúng Design System. [x] Loading tham chiếu.

### 13. Batch List
- [x] Đủ field: Add/Plus, Section Header, Material Batch Card (Batch Name & Code, Status Badge, Packaging/Harvest Date), filter/search — đúng 10 dòng Business Logic.
- [x] Đúng Design System *(badge VietGAP achieved = viền liền, Quarantine passed = viền đứt nét)*.
- [x] Success 2 dòng mẫu; Empty tham chiếu Screen Spec.

### 14. Batch Detail
- [x] Đủ field: Product Name, Lot Number, Packaging/Expiration Date, Status Badge, chứng chỉ liên kết, nút Upload Batch Certificates/View Batch History/Edit — đúng 11 dòng Business Logic.
- [x] Đúng Design System. [x] Success là trọng tâm.

### 15. Submit Batch Information
- [x] Đủ field: Product Name, Lot Number, Manufacturing/Packaging/Expiration Date, liên kết chứng chỉ, Submit/Cancel — đúng 9 dòng Business Logic.
- [x] Đúng Design System *(`<wired-calendar>` cho 3 field ngày)*.
- [x] Error (MSG-013 nếu chứng chỉ hết hạn) ghi chú, không dựng riêng alert trong file này (đã dựng đủ ở nhóm 04-06,25-26) để tránh trùng lặp không cần thiết.

### 16. Upload Batch Certificates
- [x] Đủ field: lô hàng đang chọn (chỉ đọc), file chứng chỉ, ngày hết hạn, Submit/Cancel — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(Option B placeholder)*. [x] Error MSG-009 tham chiếu.

### 17. View Batch History
- [x] Đủ field: danh sách (Lot Number, Packaging Date, Expiration Date, Certificate Links), search theo Lot Number, filter Expiration Status — đúng 5 dòng Business Logic.
- [x] Đúng Design System. [x] Success 2 dòng mẫu; Empty tham chiếu.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- 6 `<div class="screen">`, sidebar chỉ nhóm "Lô Nguyên liệu" active/click được.
- Grayscale/mono, wired-elements CDN, Patrick Hand, sketchy border, badge theo kiểu viền.
- Alert lỗi (MSG-013/MSG-009) không lặp lại ở mọi màn con — chỉ dựng đầy đủ 1 lần ở nhóm Compliance (04-06,25-26) để tránh trùng lặp giữa các file HTML của cùng actor, có ghi chú tham chiếu chéo trong bảng đối chiếu ở trên.

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

1. **17_ViewBatchHistory.md**: Report 3 UC-107 ghi Business Rules = BR-071, nhưng `traceability_matrix.md` dòng 145 ghi BR-076 — Wireframe giữ cả 2 BR trong Screen Spec nguồn, không ảnh hưởng field hiển thị ở Wireframe này.
