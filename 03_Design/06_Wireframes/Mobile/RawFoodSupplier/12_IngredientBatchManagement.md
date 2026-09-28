# Wireframe — Ingredient Batch Management (nhóm màn hình Raw Food Supplier, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/RawFoodSupplier/` — 6 file Screen Specification (Draft).
**Công cụ sinh**: Thủ công — skill `wireframer`, vanilla HTML/JS SPA, khung phone-frame nhất quán.
**File/Link**: `03_Design/06_Wireframes/Mobile/RawFoodSupplier/12_IngredientBatchManagement.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (Dashboard → Ingredient Batches List → Batch Detail → Upload Cert / Batch History).

---

## 1. Danh sách 6 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 12 | Ingredient Batches | `12_IngredientBatches.md` | Entry + FAB |
| 13 | Batch List | `13_BatchList.md` | UC-043 |
| 14 | Batch Detail | `14_BatchDetail.md` | Bundled UC-043 |
| 15 | Submit Batch Information | `15_SubmitBatchInformation.md` | UC-081 |
| 16 | Upload Batch Certificates | `16_UploadBatchCertificates.md` | UC-100 |
| 17 | View Batch History | `17_ViewBatchHistory.md` | UC-107 |

---

## 2. Đối chiếu với Screen Specification

Tương ứng 1-1 với bản Web cùng nhóm (`Web/RawFoodSupplier/12_IngredientBatchManagement.md` mục 2) — cùng field, khác bố cục (Card 1 cột thay Table, AppBar+BottomNav thay Sidebar+Topbar, FAB "+" cho hành động Submit Batch Information theo `Layout_Guidelines.md` §2.1).

- [x] 12. Ingredient Batches — đủ field, FAB mở Submit Batch Information.
- [x] 13. Batch List — đủ field (Batch Name & Code, Status Chip, Packaging/Harvest Date).
- [x] 14. Batch Detail — đủ field, FAB "Edit" bổ sung theo Platform Layout Mobile của Screen Spec (mục Bố cục: "FAB góc dưới phải").
- [x] 15. Submit Batch Information — đủ 6 field + Submit/Cancel.
- [x] 16. Upload Batch Certificates — đủ field, Option B placeholder.
- [x] 17. View Batch History — đủ field rút gọn dạng Card (2 dòng mẫu).

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- 6 `<div class="screen">`, FAB xuất hiện ở màn 12 (mở Submit Batch Information) và màn 14 (mở Edit) theo đúng Platform Layout — Mobile của Screen Spec 12/14.
- Grayscale/mono, wired-elements, Patrick Hand, sketchy border.

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

Giống bản Web cùng nhóm (`Web/RawFoodSupplier/12_IngredientBatchManagement.md` mục 5).
