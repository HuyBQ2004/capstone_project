# Screen Spec — Deliveries

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-039 và các UC con | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Management" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái | Chip/Counter | — | BR-071 | [Giả định nhẹ] |
| 4 | Điều hướng tới Delivery List | Navigation | Điều hướng tới màn 19 | — | |

### Trạng thái màn hình
- Loading: Shimmer bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Delivery Management" Button. Tương ứng node `DelMgmt["Delivery Management"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Delivery List (19), Submit Delivery Information (21).
**Bố cục**: 1 cột — AppBar, dải Chip đếm nhanh theo trạng thái trên cùng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Deliveries (entry), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, dải Chip đếm nhanh theo trạng thái.
Component bắt buộc dùng: Chip.
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6182–6199.
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
