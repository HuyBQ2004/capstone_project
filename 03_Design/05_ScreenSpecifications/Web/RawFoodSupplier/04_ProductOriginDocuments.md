# Screen Spec — Product Origin Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-102 - Upload Product Origin Documents (Report 3 §3.2, dòng 2507–2527) — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046 (yêu cầu tài liệu tuân thủ)
**MSG liên quan**: MSG-008 (vượt 10MB), MSG-009 (sai định dạng file) — theo `traceability_matrix.md` dòng 116

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Product Origin Documents" | — | |
| 2 | "Back" Button | Button | Về Supplier Dashboard / Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06), pre-select loại "Product Origin" | — | |
| 4 | Danh sách tài liệu xuất xứ (VietGAP/GlobalGAP/Organic...) | List/Card | Mỗi mục: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn (nếu có), trạng thái | BR-046 | Report 3 §3.3.2.5.5 "Product Origin Certification" Card, mở rộng thành danh sách đầy đủ |
| 5 | Trạng thái tài liệu (Badge) | Badge | Valid / Expiring Soon / Expired | BR-046 | |
| 6 | Chọn 1 tài liệu → xem chi tiết / cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Skeleton danh sách.
- Empty: Chưa có tài liệu xuất xứ nào → CTA "Tải lên tài liệu đầu tiên".
- Error: MSG-008, MSG-009 (khi upload lỗi từ màn 06).
- Success: N/A (xem/điều hướng).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Compliance Documents (26) qua card "Product Origin Certification". Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: Admin Shell — Content Area dùng Table (cột: Tên tài liệu, Loại, Ngày tải lên, Ngày hết hạn, Trạng thái, Hành động) kèm filter/search phía trên, phân trang phía dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Product Origin Documents, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table danh sách tài liệu xuất xứ với filter/search + phân trang.
Component bắt buộc dùng: Table, Badge, Button.
Thành phần bắt buộc: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn, badge trạng thái, nút Add/Plus.
Áp dụng Design System: badge theo success/warning/danger — Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2258–2272 (Compliance Docs Screens §3.3.2.5.5), dòng 2507–2527 (UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 116.
