# Screen Spec — Purchase Request List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-139 - View Purchase Requests (Report 3 §3.2, dòng 3419–3439) | **Status**: Draft
**BR liên quan**: BR-077 (cảnh báo thiếu kho — theo Report 3 UC-139 "Business Rules: BR-077"; `traceability_matrix.md` dòng 148 để trống BR — giữ lại BR-077 vì logic hợp lý: đơn mua hàng phát sinh từ cảnh báo thiếu kho phía Service Caterers, hiển thị ngữ cảnh cho supplier)
**MSG liên quan**: MSG-004, MSG-005; MSG-018 (Report 3 Exceptions liệt kê nhưng khó khớp ngữ cảnh xem-danh-sách của Supplier — [cần team xác nhận], không hiển thị mặc định)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | Report 3 §3.3.2.5.3 |
| 2 | "Back" Button | Button | Về Purchase Requests (09) | — | |
| 3 | Section Header — "Purchase order list" | Label | — | — | |
| 4 | Purchase Order Card (mỗi dòng) | Card/Row | Container tóm tắt 1 đơn | — | |
| 5 | Order code | Text | Mã đơn hàng duy nhất | — | |
| 6 | Status Badge | Badge | "Awaiting confirmation" / "Delivery completed" / khác theo vòng đời đơn | — | Màu theo `Design_System.md` §3 |
| 7 | Source of request | Text | Vai trò/nguồn gốc yêu cầu (VD "Springfield Head Chef") | — | |
| 8 | Delivery date required | Text | Ngày/giờ giao hàng yêu cầu | — | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã đơn | — | Chuẩn hoá theo `Component_Guidelines.md` DataTable pattern |
| 10 | Chọn 1 dòng → xem chi tiết | Navigation | Điều hướng tới Purchase Request Detail (11) | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có đơn mua hàng nào → empty state "Chưa có đơn đặt hàng nào từ trường đối tác".
- Error: MSG-004, MSG-005.
- Success: N/A (màn hình chỉ xem).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Purchase Requests (09) hoặc trực tiếp từ Supplier Dashboard (01). Tương ứng node `PRList["Purchase Request Screen"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Purchase Request Detail (11).
**Bố cục**: Admin Shell — `Table`/`DataTable` (cột: Mã đơn, Nguồn yêu cầu, Ngày giao yêu cầu, Trạng thái) kèm filter/search phía trên, phân trang phía dưới (theo `Component_Guidelines.md` §1 "Bảng dữ liệu").

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request List, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter/search phía trên, phân trang phía dưới.
Component bắt buộc dùng: Table, Badge, Input (search), Select (filter).
Thành phần bắt buộc: Order code, Status Badge, Source of request, Delivery date required.
Áp dụng Design System: badge theo trạng thái (Design_System.md mục 3).
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237 (§3.3.2.5.3), dòng 3419–3439 (UC-139).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-077.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 148.
