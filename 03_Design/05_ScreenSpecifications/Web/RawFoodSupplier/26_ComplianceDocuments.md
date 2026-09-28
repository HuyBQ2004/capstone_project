# Screen Spec — Compliance Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-167 - Manage Compliance Documents (Report 3 §3.2, dòng 2353–2373) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048 (Report 3); `traceability_matrix.md` dòng 113 xác nhận BR-046, BR-047
**MSG liên quan**: MSG-004, MSG-005, MSG-053 (Warning — expiring soon); MSG-013 áp dụng nếu supplier cố xem/dựa vào 1 chứng nhận Food Safety đã hết hạn (BR-048)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Compliance Record" | — | Report 3 §3.3.2.5.5 |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | "Food Safety Certificate" Card | Navigation | Điều hướng tới Food Safety Certificates (05) | BR-046, BR-047 | |
| 5 | "Product Origin Certification" Card | Navigation | Điều hướng tới Product Origin Documents (04) | BR-046 | |
| 6 | Tóm tắt số lượng tài liệu theo trạng thái | Badge/Counter | Valid / Expiring Soon / Expired | BR-047 | Tổng hợp cho cả 2 loại tài liệu |
| 7 | Danh sách đầy đủ mọi tài liệu tuân thủ (filter theo loại/trạng thái) | List/Table | Filter by type/status | BR-046, BR-047, BR-048 | UC-167 Normal Flow bước 2–4 |

### Trạng thái màn hình
- Loading: Skeleton 2 Card + danh sách.
- Empty: Chưa có tài liệu nào → empty state với CTA upload đầu tiên (UC-167 AF1).
- Warning: MSG-053 cho tài liệu sắp hết hạn (UC-167 AF2, <30 ngày theo BR-047).
- Error: MSG-004, MSG-005.
- Success: N/A (màn hình tổng hợp, không ghi dữ liệu trực tiếp).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Food Safety Compliance Record". Tương ứng node `CompRec["Food Safety Compliance Record"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Food Safety Certificates (05), Product Origin Documents (04), Upload Supplier Certificates (06).
**Bố cục**: Admin Shell — 2 Card lớn (Food Safety Certificate / Product Origin Certification) phía trên dạng col-span-6, danh sách tổng hợp dạng Table filter được phía dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Documents, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 Card điều hướng (Food Safety Certificate / Product Origin Certification) phía trên, Table tổng hợp filter theo loại/trạng thái phía dưới.
Component bắt buộc dùng: Card, Table, Badge, Button.
Thành phần bắt buộc: 2 Card điều hướng, tóm tắt số lượng theo trạng thái, danh sách filter được.
Áp dụng Design System: badge theo success/warning/danger — Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty, warning (expiring soon).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272 (§3.3.2.5.5), dòng 2353–2373 (UC-167).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 113.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md`.
