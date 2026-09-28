# Screen Spec — Purchase Request Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-139 - View Purchase Requests; hành động Accept dẫn sang UC-015 - Confirm Delivery Schedule | **Status**: Draft
**BR liên quan**: BR-077 (bối cảnh thiếu kho), BR-071 (chứng nhận lô hàng — áp dụng khi Accept và điều hướng sang lập lịch giao hàng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã đơn hàng | — | Report 3 §3.3.2.5.4 |
| 2 | "Back" Button (Top Bar) | Button | Về Purchase Request List (10) | — | |
| 3 | "Come back" Button | Button | Huỷ xem chi tiết, về danh sách | — | |
| 4 | Order number | Text | Mã định danh đơn hàng | — | |
| 5 | Recipient | Text | Trường/canteen nhận hàng | — | |
| 6 | Delivery deadline | Text | Ngày giao hạn chót + yêu cầu vận hành quan trọng | — | |
| 7 | List of ingredients to order (Section Header) | Label | — | — | |
| 8 | Ingredients List Container | List | Tên nguyên liệu, khối lượng yêu cầu, mã lô/batch dự kiến | BR-071 | |
| 9 | "Accept" Button | Button | Chấp nhận đơn → điều hướng Confirm Delivery Schedule (22, UC-015) | — | ScreenFlow node `PRResponse["Confirm Quantity & Delivery Date"]` |
| 10 | "Reject" Button | Button | Từ chối đơn, yêu cầu nhập lý do | — | [Giả định — Report 3 không mô tả field lý do từ chối chi tiết cho hành động này, chỉ có trong ScreenFlow "Accept/Reject" — cần team xác nhận có bắt buộc nhập lý do hay không] |

### Trạng thái màn hình
- Loading: Skeleton chi tiết đơn.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: Sau khi Accept → điều hướng Confirm Delivery Schedule (22); sau khi Reject → quay về danh sách (10) với trạng thái cập nhật.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Purchase Request List (10) khi chọn 1 dòng. Tương ứng node `PRDetail["Purchase Request Detail"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Confirm Delivery Schedule (22).
**Bố cục**: Admin Shell — Content Area 2 cột: thông tin đơn (Order number, Recipient, Delivery deadline) bên trái (col-span-4), danh sách nguyên liệu dạng Table bên phải (col-span-8); nút Accept/Reject cố định cuối trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request Detail, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 cột — thông tin đơn bên trái, Table nguyên liệu bên phải, nút Accept/Reject cố định cuối trang.
Component bắt buộc dùng: Table, Button, Card.
Thành phần bắt buộc: Order number, Recipient, Delivery deadline, danh sách nguyên liệu, nút Accept/Reject.
Áp dụng Design System: primary #0E7490, destructive variant cho Reject.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6239–6256 (§3.3.2.5.4 Purchase Request Screen Detail).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).
