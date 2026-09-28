# Screen Spec — Purchase Requests

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-139 (View Purchase Requests) | **Status**: Draft
**BR liên quan**: Không áp dụng trực tiếp (BR áp dụng ở màn con Purchase Request List/Detail)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | Report 3 §3.3.2.5.3 |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái (Pending/Confirmed/Delivered) | Badge/Counter | Tổng hợp số lượng theo trạng thái | — | Bổ sung nhẹ so với Report 3 gốc để tăng khả năng điều hướng nhanh — [Giả định — cần team xác nhận có cần hay không] |
| 4 | Điều hướng tới Purchase Request List | Navigation | Điều hướng tới màn 10 | — | |

### Trạng thái màn hình
- Loading: Skeleton bộ đếm.
- Empty: N/A (chuyển hẳn empty state cho màn 10).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Purchase Request". Điều hướng đi: Purchase Request List (10).
**Bố cục**: Admin Shell — Content Area hiển thị bộ đếm nhanh dạng Card nhỏ (col-span-3/4 mỗi card), bên dưới là bảng tóm tắt dẫn thẳng vào màn 10 (hoặc redirect thẳng nếu không cần màn trung gian riêng biệt về UI).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Requests (entry), nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, các Card đếm nhanh theo trạng thái đơn mua hàng.
Component bắt buộc dùng: Card, Badge, Button.
Thành phần bắt buộc: bộ đếm theo trạng thái, điều hướng tới Purchase Request List.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237 (§3.3.2.5.3 Purchase Request Screen).
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.
