# Screen Spec — Purchase Request Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-139 - View Purchase Requests; hành động Accept dẫn sang UC-015 - Confirm Delivery Schedule | **Status**: Draft
**BR liên quan**: BR-077, BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã đơn hàng | — | |
| 2 | "Back" Button (AppBar) | Button | Về Purchase Request List (10) | — | |
| 3 | "Come back" Button | Button | Huỷ xem chi tiết | — | |
| 4 | Order number | Text | Mã định danh đơn hàng | — | |
| 5 | Recipient | Text | Trường/canteen nhận hàng | — | |
| 6 | Delivery deadline | Text | Ngày giao hạn chót | — | |
| 7 | List of ingredients to order (Section Header) | Label | — | — | |
| 8 | Ingredients List Container | List (Card con) | Tên nguyên liệu, khối lượng, mã lô dự kiến | BR-071 | |
| 9 | "Accept" Button | Button | Chấp nhận → điều hướng Confirm Delivery Schedule (22) | — | |
| 10 | "Reject" Button | Button | Từ chối, yêu cầu nhập lý do | — | [Giả định — cần team xác nhận có bắt buộc lý do hay không] |

### Trạng thái màn hình
- Loading: Shimmer chi tiết đơn.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: Accept → Confirm Delivery Schedule (22); Reject → về danh sách (10).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Purchase Request List (10) khi chọn 1 Card. Tương ứng node `PRDetail["Purchase Request Detail"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Confirm Delivery Schedule (22, node `PRResponse`).
**Bố cục**: 1 cột — AppBar (Back), thông tin đơn (Order number/Recipient/Delivery deadline) trên cùng dạng Card, danh sách nguyên liệu Card con cuộn dọc bên dưới, 2 nút Accept/Reject cố định cuối màn hình (vùng ngón cái).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request Detail, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, Card thông tin đơn trên cùng, danh sách Card nguyên liệu cuộn dọc, 2 nút Accept/Reject cố định cuối màn hình.
Component bắt buộc dùng: Card (M3), FilledButton (Accept), OutlinedButton (Reject).
Thành phần bắt buộc: Order number, Recipient, Delivery deadline, danh sách nguyên liệu, nút Accept/Reject.
Áp dụng Design System: seed color #0E7490, màu danger cho Reject.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6239–6256 (§3.3.2.5.4).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).
