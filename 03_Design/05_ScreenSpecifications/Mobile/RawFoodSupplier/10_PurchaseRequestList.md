# Screen Spec — Purchase Request List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-139 - View Purchase Requests | **Status**: Draft
**BR liên quan**: BR-077 (xem ghi chú đối chiếu nguồn ở bản Web cùng tên)
**MSG liên quan**: MSG-004, MSG-005; MSG-018 (chưa xác nhận áp dụng — xem ghi chú bản Web)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | Section Header — "Purchase order list" | Label | — | — | |
| 4 | Purchase Order Card (mỗi mục) | Card | Container tóm tắt 1 đơn | — | |
| 5 | Order code | Text | Mã đơn hàng duy nhất | — | |
| 6 | Status Badge (Chip) | Chip | "Awaiting confirmation" / "Delivery completed" / khác | — | |
| 7 | Source of request | Text | Nguồn gốc yêu cầu | — | |
| 8 | Delivery date required | Text | Ngày/giờ giao hàng yêu cầu | — | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã đơn | — | |
| 10 | Chọn 1 Card → xem chi tiết | Navigation | Điều hướng tới Purchase Request Detail (11) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có đơn mua hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Purchase Request" Button. Tương ứng trực tiếp node `PRList["Purchase Request Screen"]` trong `06_Raw_Food_Supplier_Screen_Flow.md` (trên Mobile, màn 09 "Purchase Requests" và màn 10 "Purchase Request List" thực chất là 1 màn hình duy nhất theo Screen Flow gốc — giữ 2 file riêng theo đúng tên trong Authorization Matrix, nhưng bố cục Mobile gộp phần đếm nhanh (09) vào đầu danh sách (10) để tránh 1 màn hình rỗng trung gian). Điều hướng đi: Purchase Request Detail (11).
**Bố cục**: 1 cột — AppBar, thanh filter/search cố định trên cùng, danh sách Card cuộn dọc (pull-to-refresh bắt buộc theo `Layout_Guidelines.md` §2.1).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request List, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, thanh filter/search trên cùng, danh sách Card cuộn dọc, pull-to-refresh.
Component bắt buộc dùng: Card (M3), Chip, TextField (search).
Thành phần bắt buộc: Order code, Status Chip, Source of request, Delivery date required.
Áp dụng Design System: chip theo trạng thái.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237, dòng 3419–3439 (UC-139).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-077.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
