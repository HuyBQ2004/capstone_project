# Screen Spec — Record Inventory Transactions Log

**Actor**: Service Caterers | **UC liên quan**: UC-064 - Record Inventory Transactions (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-204 (Bắt buộc mã lý do + xác nhận quản lý khi điều chỉnh kho), BR-170 (audit trail)
**MSG liên quan**: MSG-042 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn — ĐÃ XỬ LÝ (2026-07-02)**: `Report 3.md` UC-064 trích **BR-082** cho yêu cầu "mandatory reason code and manager sign-off" khi điều chỉnh kho thủ công, nhưng BR-082 trong `business_rules.md` canonical = "Chế biến riêng cho bữa ăn đặc biệt" (Menu & Diet) — hoàn toàn khác chủ đề. Đã cấp mã mới **BR-204** cho yêu cầu này tại `business_rules.md` §6 và cập nhật `traceability_matrix.md`. Ghost-citation "BR-082" trong Report 3 UC-064 là lỗi nguồn gốc tài liệu, không thay đổi yêu cầu nghiệp vụ thực tế.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn lô nguyên liệu cần điều chỉnh | Select | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 2 | Số lượng điều chỉnh | Input số (+/-) | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 3 | Mã lý do điều chỉnh (spoilage, waste, audit write-off) | Select | Bắt buộc | BR-204 | UC-064 Normal Flow bước 2 |
| 4 | Xác nhận/chữ ký quản lý | Upload chữ ký hoặc xác thực quản lý | Bắt buộc | BR-204 | UC-064 Normal Flow bước 3 |
| 5 | Nút "Ghi nhận Giao dịch" | Button | Chặn nếu thiếu mã lý do hoặc sign-off | BR-204 | UC-064 Exceptions, MSG-042 |
| 6 | Nhật ký giao dịch kho (lịch sử) | Table (Web) / Card list (Mobile) | Cột: Ngày, Nguyên liệu, Số lượng, Lý do, Người thực hiện | BR-170 (audit trail chung) | — |

### Trạng thái màn hình
- Loading: `Skeleton` cho form + nhật ký.
- Empty: Chưa có giao dịch điều chỉnh nào.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu thiếu mã lý do/sign-off.
- Success: `Toast` MSG-042.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Inventory Management Dashboard (nút "Xem Nhật ký Giao dịch Kho" hoặc "Điều chỉnh Kho"). Thoát: back về Dashboard.
**Bố cục**: Page Header + form điều chỉnh dạng `Dialog` ngắn + `Table` nhật ký giao dịch bên dưới (phân trang).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Inventory Transactions Log, nền tảng Web, actor Service Caterers.
Bố cục: Dialog form điều chỉnh (chọn lô, số lượng, mã lý do, xác nhận quản lý) + Table nhật ký giao dịch bên dưới.
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
