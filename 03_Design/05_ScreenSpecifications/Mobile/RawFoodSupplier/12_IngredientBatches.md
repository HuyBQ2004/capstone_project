# Screen Spec — Ingredient Batches

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-043 và các UC con | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | Report 3 §3.3.2.5.6 |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Bộ đếm nhanh theo trạng thái lô | Chip/Counter | — | BR-071 | [Giả định nhẹ] |
| 5 | Điều hướng tới Batch List | Navigation | Điều hướng tới màn 13 | — | |

### Trạng thái màn hình
- Loading: Shimmer bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Raw Material Shipment"/"Ingredient Batches" Button. Tương ứng node `BatchList["Ingredient Batches List"]` trong `06_Raw_Food_Supplier_Screen_Flow.md` (trên Mobile, màn 12 và 13 gộp thành 1 màn hình liên tục — dải Chip đếm nhanh nằm ở đầu danh sách Batch List). Điều hướng đi: Batch List (13), Submit Batch Information (15).
**Bố cục**: 1 cột — AppBar, dải Chip đếm nhanh trên cùng, FAB "+" mở Submit Batch Information (15).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Ingredient Batches (entry), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, dải Chip đếm nhanh theo trạng thái, FAB "+".
Component bắt buộc dùng: Chip, FloatingActionButton.
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: chip theo success/warning/danger/critical.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291.
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
