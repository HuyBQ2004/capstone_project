# Screen Spec — Deliveries

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-039 (Manage Deliveries) và các UC con | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Management" | — | Report 3 §3.3.2.5.1 (nút Dashboard) |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái (Scheduled/In Transit/Delivered/Delayed) | Badge/Counter | — | BR-071 | [Giả định nhẹ — mở rộng, không đổi field cốt lõi] |
| 4 | Điều hướng tới Delivery List | Navigation | Điều hướng tới màn 19 | — | |

### Trạng thái màn hình
- Loading: Skeleton bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Delivery Management". Điều hướng đi: Delivery List (19), Submit Delivery Information (21).
**Bố cục**: Admin Shell — Card đếm nhanh theo trạng thái phía trên, dẫn vào Delivery List (19).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Deliveries (entry), nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Card đếm nhanh theo trạng thái giao hàng.
Component bắt buộc dùng: Card, Badge, Button.
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (Supplier Dashboard, nút "Delivery Management").
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.
