# Screen Spec — View Batch History

**Actor**: Raw Food Supplier | **UC liên quan**: UC-107 - View Batch History — extends UC-043 | **Status**: Draft
**BR liên quan**: BR-071, BR-076 (xem ghi chú đối chiếu nguồn ở bản Web cùng tên)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Batch History" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Batch Detail (14) | — | |
| 3 | Danh sách toàn bộ lô đã đăng ký | Card list | Lot Number, Packaging Date, Expiration Date, Certificate Links | BR-071, BR-076 | |
| 4 | Search theo Lot Number | Input | — | — | |
| 5 | Filter theo Expiration Status | Dropdown | Valid / Expiring Soon / Expired | BR-047 | |

### Trạng thái màn hình
- Loading: Shimmer danh sách.
- Empty: Chưa có lô nào trong lịch sử.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Batch Detail (14) qua nút "View Batch History".
**Bố cục**: 1 cột — AppBar (Back), thanh search/filter trên cùng, danh sách Card cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Batch History, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, thanh search/filter, danh sách Card cuộn dọc, pull-to-refresh.
Component bắt buộc dùng: Card (M3), TextField (search), DropdownMenu (filter).
Thành phần bắt buộc: Lot Number, Packaging Date, Expiration Date, Certificate Links.
Áp dụng Design System: chip/badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3353–3373 (UC-107).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-076.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 145.
