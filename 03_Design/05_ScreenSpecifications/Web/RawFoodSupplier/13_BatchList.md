# Screen Spec — Batch List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-043 - Manage Ingredient Batches (Report 3 §3.2, dòng 3089–3109) | **Status**: Draft
**BR liên quan**: BR-071 (chứng nhận lô hàng bắt buộc: số lô, ngày sản xuất, hạn sử dụng, số lượng, chứng chỉ)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | Report 3 §3.3.2.5.6 |
| 2 | "Back" Button | Button | Về Ingredient Batches (12) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Section Header — "List of outgoing shipments" | Label | — | — | |
| 5 | Material Batch Card (mỗi dòng) | Card/Row | Container tóm tắt 1 lô | BR-071 | |
| 6 | Batch Name & Code | Text | Tên nguyên liệu + mã lô | BR-071 | |
| 7 | Status Badge | Badge | "Quarantine passed" / "VietGAP achieved" / khác | BR-071 | Màu theo `Design_System.md` §3 |
| 8 | Packaging / Harvest Date | Text | Ngày đóng gói/thu hoạch | BR-071 | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã lô | — | |
| 10 | Chọn 1 dòng → xem chi tiết | Navigation | Điều hướng tới Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lô nào đăng ký → CTA "Đăng ký lô đầu tiên" (điều hướng 15).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Ingredient Batches (12). Tương ứng node `BatchList["Ingredient Batches List"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Batch Detail (14), Submit Batch Information (15).
**Bố cục**: Admin Shell — `Table` (cột: Tên/Mã lô, Trạng thái, Ngày đóng gói) kèm filter/search, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch List, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter/search + phân trang.
Component bắt buộc dùng: Table, Badge, Input, Button.
Thành phần bắt buộc: Batch Name & Code, Status Badge, Packaging/Harvest Date.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291 (§3.3.2.5.6), dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`.
