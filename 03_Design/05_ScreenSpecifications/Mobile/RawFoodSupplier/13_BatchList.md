# Screen Spec — Batch List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-043 - Manage Ingredient Batches | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Ingredient Batches (12) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Section Header — "List of outgoing shipments" | Label | — | — | |
| 5 | Material Batch Card | Card | Container tóm tắt 1 lô | BR-071 | |
| 6 | Batch Name & Code | Text | Tên nguyên liệu + mã lô | BR-071 | |
| 7 | Status Badge (Chip) | Chip | "Quarantine passed" / "VietGAP achieved" / khác | BR-071 | |
| 8 | Packaging / Harvest Date | Text | Ngày đóng gói/thu hoạch | BR-071 | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã lô | — | |
| 10 | Chọn 1 Card → xem chi tiết | Navigation | Điều hướng tới Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có lô nào → CTA "Đăng ký lô đầu tiên".
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Ingredient Batches (12). Tương ứng node `BatchList["Ingredient Batches List"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Batch Detail (14), Submit Batch Information (15).
**Bố cục**: 1 cột — AppBar, thanh filter/search, danh sách Card cuộn dọc (pull-to-refresh), FAB "+" mở Submit Batch Information (15).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch List, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, thanh filter/search, danh sách Card, FAB "+", pull-to-refresh.
Component bắt buộc dùng: Card (M3), Chip, TextField, FloatingActionButton.
Thành phần bắt buộc: Batch Name & Code, Status Chip, Packaging/Harvest Date.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291, dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
