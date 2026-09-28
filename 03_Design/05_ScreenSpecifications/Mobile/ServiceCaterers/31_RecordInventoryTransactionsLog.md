# Screen Spec — Record Inventory Transactions Log

**Actor**: Service Caterers | **UC liên quan**: UC-064 - Record Inventory Transactions (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-204 (Bắt buộc mã lý do + xác nhận quản lý khi điều chỉnh kho), BR-170 (audit trail)
**MSG liên quan**: MSG-042 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn — ĐÃ XỬ LÝ (2026-07-02)**: giống bản Web — `Report 3.md` UC-064 trích BR-082 cho "mandatory reason code and manager sign-off", nhưng BR-082 thật trong `business_rules.md` là "Chế biến riêng cho bữa ăn đặc biệt" — không liên quan. Đã cấp mã mới **BR-204** tại `business_rules.md` §6. Ghost-citation "BR-082" trong Report 3 UC-064 là lỗi nguồn gốc tài liệu, không thay đổi yêu cầu nghiệp vụ.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn lô nguyên liệu cần điều chỉnh | DropdownMenu | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 2 | Số lượng điều chỉnh | TextField số (+/-) | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 3 | Mã lý do điều chỉnh (spoilage, waste, audit write-off) | DropdownMenu | Bắt buộc | BR-204 | UC-064 Normal Flow bước 2 |
| 4 | Xác nhận/chữ ký quản lý | Signature pad hoặc xác thực quản lý | Bắt buộc | BR-204 | UC-064 Normal Flow bước 3 |
| 5 | Nút "Ghi nhận Giao dịch" | FilledButton | Chặn nếu thiếu mã lý do hoặc sign-off | BR-204 | MSG-042 |
| 6 | Nhật ký giao dịch kho (lịch sử) | Card list | Mỗi card: Ngày, Nguyên liệu, Số lượng, Lý do, Người thực hiện | BR-170 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho form + nhật ký.
- Empty: Chưa có giao dịch điều chỉnh nào.
- Error: `AlertDialog` MSG-004/MSG-005 nếu thiếu mã lý do/sign-off.
- Success: `SnackBar` MSG-042.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Inventory Management Dashboard (icon "Xem Nhật ký Giao dịch Kho" hoặc "Điều chỉnh Kho"). Thoát: back về Dashboard.
**Bố cục**: Trang riêng — form điều chỉnh (bottom sheet) + Card list nhật ký giao dịch bên dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Inventory Transactions Log, nền tảng Mobile, actor Service Caterers.
Bố cục: Bottom sheet form điều chỉnh (chọn lô, số lượng, mã lý do, xác nhận quản lý) + Card list nhật ký giao dịch.
Thành phần bắt buộc: chặn submit nếu thiếu mã lý do/sign-off (BR-204).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3199–3217 (UC-064 - Record Inventory Transactions)
- business_rules.md BR-204 (bắt buộc mã lý do + sign-off khi điều chỉnh kho — bổ sung 2026-07-02)
- business_rules.md BR-170 (audit trail)
- error_message.md MSG-042
