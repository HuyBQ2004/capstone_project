# Screen Spec — Purchase Requests

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-139 (View Purchase Requests) | **Status**: Draft
**BR liên quan**: Không áp dụng trực tiếp
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | Report 3 §3.3.2.5.3 |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái (Pending/Confirmed/Delivered) | Chip/Counter | Tổng hợp số lượng theo trạng thái | — | [Giả định — cần team xác nhận có cần hay không] |
| 4 | Điều hướng tới Purchase Request List | Navigation | Điều hướng tới màn 10 | — | |

### Trạng thái màn hình
- Loading: Shimmer bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Purchase Request" Button. Tương ứng node `PRList["Purchase Request Screen"]` trong `06_Raw_Food_Supplier_Screen_Flow.md` (dùng luôn làm màn danh sách chính — xem ghi chú ở màn 10 về khả năng gộp 2 màn 09/10 trên Mobile).
**Bố cục**: 1 cột — AppBar, dải Chip đếm nhanh theo trạng thái trên cùng, sau đó chuyển thẳng vào danh sách (xem màn 10).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Requests (entry), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, dải Chip đếm nhanh theo trạng thái.
Component bắt buộc dùng: Chip, Card (M3).
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237 (§3.3.2.5.3).
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.
