# Screen Spec — Ingredient Batches

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-043 (Manage Ingredient Batches) và các UC con | **Status**: Draft
**BR liên quan**: BR-071 (chứng nhận lô hàng bắt buộc)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | Report 3 §3.3.2.5.6 |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Bộ đếm nhanh theo trạng thái lô (Valid/Expiring/Expired/Quarantine) | Badge/Counter | — | BR-071 | [Giả định nhẹ — mở rộng so với Report 3 gốc để định hướng nhanh, không đổi field cốt lõi] |
| 5 | Điều hướng tới Batch List | Navigation | Điều hướng tới màn 13 | — | |

### Trạng thái màn hình
- Loading: Skeleton bộ đếm.
- Empty: N/A (chuyển cho màn 13).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Ingredient Batches". Điều hướng đi: Batch List (13), Submit Batch Information (15).
**Bố cục**: Admin Shell — Card đếm nhanh theo trạng thái phía trên, dẫn thẳng vào Batch List (13) bên dưới cùng trang hoặc qua điều hướng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Ingredient Batches (entry), nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Card đếm nhanh theo trạng thái lô.
Component bắt buộc dùng: Card, Badge, Button.
Thành phần bắt buộc: bộ đếm theo trạng thái, nút Add/Plus.
Áp dụng Design System: badge theo success/warning/danger/critical.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291 (§3.3.2.5.6 Ingredient Batches Screens).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.
