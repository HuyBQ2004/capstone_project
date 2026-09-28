# Screen Spec — Compliance Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-167 - Manage Compliance Documents | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-053

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Compliance Record" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | "Food Safety Certificate" Card | Navigation | Điều hướng tới Food Safety Certificates (05) | BR-046, BR-047 | |
| 5 | "Product Origin Certification" Card | Navigation | Điều hướng tới Product Origin Documents (04) | BR-046 | |
| 6 | Tóm tắt số lượng tài liệu theo trạng thái | Chip/Counter | Valid / Expiring Soon / Expired | BR-047 | |
| 7 | Danh sách đầy đủ tài liệu tuân thủ (filter) | Card list | Filter by type/status | BR-046, BR-047, BR-048 | |

### Trạng thái màn hình
- Loading: Shimmer 2 Card + danh sách.
- Empty: Chưa có tài liệu → CTA upload đầu tiên.
- Warning: MSG-053.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Food Safety Compliance Record" Button. Tương ứng node `CompRec["Food Safety Compliance Record"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Food Safety Certificates (05), Product Origin Documents (04), Upload Supplier Certificates (06).
**Bố cục**: 1 cột — AppBar, 2 Card điều hướng full-width trên cùng, danh sách Card tổng hợp filter được cuộn dọc bên dưới, FAB "+" mở Upload Supplier Certificates (06).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Documents, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, 2 Card điều hướng trên cùng, danh sách Card tổng hợp bên dưới, FAB "+".
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton.
Thành phần bắt buộc: 2 Card điều hướng, tóm tắt trạng thái, danh sách filter được.
Áp dụng Design System: chip theo success/warning/danger.
Trạng thái cần thể hiện: loading, empty, warning.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272, dòng 2353–2373 (UC-167).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 113.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
