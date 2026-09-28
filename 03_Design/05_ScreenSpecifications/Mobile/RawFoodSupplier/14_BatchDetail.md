# Screen Spec — Batch Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-043 - Manage Ingredient Batches; điều hướng tới UC-100, UC-107 | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên/Mã lô | — | |
| 2 | "Back" Button (AppBar) | Button | Về Batch List (13) | — | |
| 3 | Product Name | Text | Tên nguyên liệu | BR-071 | |
| 4 | Lot Number | Text | Số lô | BR-071 | |
| 5 | Packaging Date | Text | Ngày đóng gói | BR-071 | |
| 6 | Expiration Date | Text | Hạn sử dụng | BR-071 | |
| 7 | Status Badge (Chip) | Chip | Quarantine passed / VietGAP achieved / khác | BR-071 | |
| 8 | Chứng chỉ liên kết (danh sách) | List | Liên kết chứng chỉ đã upload cho lô này | BR-071 | |
| 9 | "Upload Batch Certificates" Button | Navigation | Điều hướng tới màn 16 | — | |
| 10 | "View Batch History" Button | Navigation | Điều hướng tới màn 17 | — | |
| 11 | "Edit" Button (FAB) | Navigation | Điều hướng tới Submit Batch Information (15) chế độ chỉnh sửa | BR-071 | |

### Trạng thái màn hình
- Loading: Shimmer chi tiết lô.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Batch List (13) khi chọn 1 Card. Tương ứng node `BatchDetail["Ingredient Batch Detail"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: `UploadCert` (Upload Batch Certificates, 16), View Batch History (17), Submit Batch Information (15, edit).
**Bố cục**: 1 cột — AppBar (Back), Card thông tin lô trên cùng, danh sách chứng chỉ liên kết bên dưới, FAB "Edit" góc dưới phải, 2 nút hành động (Upload Batch Certificates / View Batch History) cuối màn hình.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch Detail, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, Card thông tin lô trên cùng, danh sách chứng chỉ bên dưới, FAB Edit, 2 nút hành động cuối màn hình.
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton, OutlinedButton.
Thành phần bắt buộc: Product Name, Lot Number, Packaging Date, Expiration Date, Status Chip, chứng chỉ liên kết.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291, dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `BatchDetail`).
