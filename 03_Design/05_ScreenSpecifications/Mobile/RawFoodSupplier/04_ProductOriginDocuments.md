# Screen Spec — Product Origin Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-102 - Upload Product Origin Documents — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046
**MSG liên quan**: MSG-008, MSG-009

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Product Origin Documents" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | Danh sách tài liệu xuất xứ | Card list | Mỗi Card: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn, trạng thái | BR-046 | |
| 5 | Trạng thái tài liệu (Chip) | Chip | Valid / Expiring Soon / Expired | BR-046 | |
| 6 | Chọn 1 tài liệu → xem chi tiết/cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có tài liệu → CTA "Tải lên tài liệu đầu tiên".
- Error: MSG-008, MSG-009.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Compliance Documents (26) qua "Product Origin Certification" Card. Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: 1 cột — AppBar (Back + tiêu đề), danh sách Card cuộn dọc (1 tài liệu = 1 Card), FAB "+" góc dưới phải mở Upload Supplier Certificates (06).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Product Origin Documents, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, danh sách Card tài liệu, FAB "+" góc dưới phải.
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton.
Thành phần bắt buộc: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn, chip trạng thái.
Áp dụng Design System: chip theo success/warning/danger.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272 (§3.3.2.5.5), dòng 2507–2527 (UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 116.
