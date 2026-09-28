# Screen Spec — Food Safety Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046, BR-047
**MSG liên quan**: MSG-008, MSG-009, MSG-053

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Food Safety Certificates" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | Danh sách chứng nhận ATTP | Card list | Mỗi Card: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, trạng thái | BR-046 | |
| 5 | Trạng thái (Chip) | Chip | Valid / Expiring Soon (MSG-053) / Expired | BR-047 | |
| 6 | Chọn 1 chứng nhận → xem chi tiết/cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có chứng nhận → CTA "Tải lên chứng nhận đầu tiên".
- Error: MSG-008, MSG-009.
- Warning: MSG-053 badge "Expiring Soon".
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Compliance Documents (26) qua "Food Safety Certificate" Card. Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: 1 cột — AppBar (Back + tiêu đề), danh sách Card cuộn dọc, FAB "+" mở Upload Supplier Certificates (06).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Safety Certificates, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, danh sách Card chứng nhận, FAB "+".
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton.
Thành phần bắt buộc: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, chip trạng thái.
Áp dụng Design System: chip warning cho Expiring Soon.
Trạng thái cần thể hiện: loading, empty, warning.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272, dòng 2485–2505 (UC-101).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 115.
