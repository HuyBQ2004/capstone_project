# Screen Spec — Food Safety Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates (Report 3 §3.2, dòng 2485–2505) — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046, BR-047 (cảnh báo hết hạn 30 ngày)
**MSG liên quan**: MSG-008, MSG-009, MSG-053 (Warning — cảnh báo hết hạn) — theo `traceability_matrix.md` dòng 115

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Food Safety Certificates" | — | |
| 2 | "Back" Button | Button | Về Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06), pre-select loại "Food Safety Certificate" | — | |
| 4 | Danh sách chứng nhận ATTP | List/Card | Mỗi mục: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, trạng thái | BR-046 | Report 3 §3.3.2.5.5 "Food Safety Certificate" Card, mở rộng thành danh sách đầy đủ |
| 5 | Trạng thái (Badge) | Badge | Valid / Expiring Soon (MSG-053, <30 ngày) / Expired | BR-047 | |
| 6 | Chọn 1 chứng nhận → xem chi tiết/cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Skeleton danh sách.
- Empty: Chưa có chứng nhận ATTP nào → CTA "Tải lên chứng nhận đầu tiên" (bắt buộc trước khi được Approved theo BR-040).
- Error: MSG-008, MSG-009 (upload lỗi từ màn 06).
- Warning: MSG-053 hiển thị badge "Expiring Soon" cho chứng nhận còn <30 ngày.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Compliance Documents (26) qua card "Food Safety Certificate". Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: Admin Shell — Table (cột: Tên chứng nhận, Cơ quan cấp, Ngày cấp, Ngày hết hạn, Trạng thái, Hành động), filter theo trạng thái ở đầu bảng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Safety Certificates, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table danh sách chứng nhận ATTP với filter trạng thái.
Component bắt buộc dùng: Table, Badge, Button.
Thành phần bắt buộc: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, badge trạng thái.
Áp dụng Design System: badge warning cho "Expiring Soon" (MSG-053).
Trạng thái cần thể hiện: loading, empty, warning (expiring soon).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272 (§3.3.2.5.5), dòng 2485–2505 (UC-101).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 115.
