# Screen Spec — Batch Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-043 - Manage Ingredient Batches; điều hướng tới UC-100 (Upload Batch Certificates), UC-107 (View Batch History) | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên/Mã lô | — | Mở rộng từ Report 3 §3.3.2.5.6 (Material Batch Card) thành màn chi tiết đầy đủ |
| 2 | "Back" Button | Button | Về Batch List (13) | — | |
| 3 | Product Name | Text | Tên nguyên liệu | BR-071 | |
| 4 | Lot Number | Text | Số lô | BR-071 | |
| 5 | Packaging Date | Text | Ngày đóng gói | BR-071 | |
| 6 | Expiration Date | Text | Hạn sử dụng | BR-071 | |
| 7 | Status Badge | Badge | Quarantine passed / VietGAP achieved / khác | BR-071 | |
| 8 | Chứng chỉ liên kết (danh sách) | List | Liên kết tới các chứng chỉ đã upload cho lô này | BR-071 | Điều hướng tới Upload Batch Certificates (16) |
| 9 | "Upload Batch Certificates" Button | Navigation | Điều hướng tới màn 16 | — | UC-100 |
| 10 | "View Batch History" Button | Navigation | Điều hướng tới màn 17 | — | UC-107 |
| 11 | "Edit" Button | Navigation | Điều hướng tới Submit Batch Information (15) ở chế độ chỉnh sửa | BR-071 | UC-043 Normal Flow bước 3 "edit manufacturing logs" |

### Trạng thái màn hình
- Loading: Skeleton chi tiết lô.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A (màn xem, hành động ghi dữ liệu ở màn con).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Batch List (13) khi chọn 1 dòng. Tương ứng node `BatchDetail["Ingredient Batch Detail"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Upload Batch Certificates (16), View Batch History (17), Submit Batch Information (15, edit mode).
**Bố cục**: Admin Shell — Content Area 2 cột: thông tin lô (Product Name, Lot Number, Packaging/Expiration Date, Status) bên trái (col-span-5), danh sách chứng chỉ liên kết + action buttons bên phải (col-span-7).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch Detail, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 cột — thông tin lô bên trái, chứng chỉ liên kết + action bên phải.
Component bắt buộc dùng: Card, Badge, Table (chứng chỉ), Button.
Thành phần bắt buộc: Product Name, Lot Number, Packaging Date, Expiration Date, Status Badge, chứng chỉ liên kết, nút Upload Batch Certificates/View Batch History/Edit.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291 (§3.3.2.5.6), dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md` (node `BatchDetail`).
