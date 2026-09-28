# Screen Spec — Reject Ingredient Batch Form

**Actor**: Service Caterers | **UC liên quan**: UC-072 - Reject Ingredient Batch (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-078 (lô nguyên liệu không đạt bị từ chối)
**MSG liên quan**: MSG-015 (Error — GPS, dùng chung ngữ cảnh phiên nhận hàng), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: `Report 3.md` UC-072 chỉ liệt kê `MSG-004, MSG-005, MSG-015` ở mục Exceptions, không có mã Success riêng cho việc từ chối lô hàng — Screen Spec này **không tự đặt tên MSG mới**, dùng thông báo xác nhận chung dạng Toast không gắn mã MSG cụ thể. **[Giả định — cần team xác nhận]** nếu cần bổ sung MSG riêng cho "Reject thành công".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin lô hàng đang kiểm tra | Read-only card | Lấy từ phiên nhận hàng đang hoạt động | — | UC-072 Preconditions |
| 2 | Lý do từ chối (mã lý do bắt buộc) | Select (multi-choice) | Bắt buộc chọn ít nhất 1 lý do | BR-078 | UC-072 Normal Flow bước 3–4 |
| 3 | Ảnh bằng chứng lô hàng bị từ chối | Upload ảnh | Khuyến nghị bắt buộc (đồng bộ tinh thần BR-078 "ảnh bằng chứng") | BR-078 | UC-072 Postconditions |
| 4 | Tuỳ chọn "Từ chối một phần" (Partial Rejection) | Checkbox + input số lượng | Khi bật: tạo 2 bản ghi batch riêng (phần đạt/phần không đạt) | — | UC-072 Alternative Flow AF1 |
| 5 | Nút "Xác nhận Từ chối" | Button (destructive) + `AlertDialog` xác nhận | Không thể hoàn tác | — | UC-072 Normal Flow bước 4–5 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin lô hàng.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005/MSG-015 nếu thao tác thất bại.
- Success: `Toast` xác nhận batch đã chuyển trạng thái "Rejected", loại khỏi kho, thông báo đã gửi nhà cung cấp.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (tab/bước "Log Rejections") khi phát hiện lô hàng không đạt. Thoát: xác nhận xong quay lại Workspace.
**Bố cục**: `Dialog` form ngắn — thông tin lô hàng, checklist lý do từ chối, upload ảnh, nút xác nhận (destructive variant).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Reject Ingredient Batch Form, nền tảng Web, actor Service Caterers.
Bố cục: Dialog gồm thông tin lô hàng, danh sách lý do từ chối (multi-select), upload ảnh bằng chứng, tuỳ chọn từ chối một phần, nút Xác nhận Từ chối (destructive) kèm AlertDialog xác nhận.
Thành phần bắt buộc: nút hành động dùng variant destructive theo Component_Guidelines.md.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3243–3257 (UC-072 - Reject Ingredient Batch)
- business_rules.md BR-078
- error_message.md MSG-015
