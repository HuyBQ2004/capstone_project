# Screen Spec — Reject Ingredient Batch Form

**Actor**: Service Caterers | **UC liên quan**: UC-072 - Reject Ingredient Batch (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-078
**MSG liên quan**: MSG-015 (Error), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — Report 3 UC-072 không có mã MSG Success riêng cho reject; dùng thông báo xác nhận chung không gắn mã MSG. **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin lô hàng đang kiểm tra | Read-only card | Lấy từ phiên nhận hàng đang hoạt động | — | UC-072 Preconditions |
| 2 | Lý do từ chối (mã lý do bắt buộc) | Checkbox list | Bắt buộc chọn ít nhất 1 lý do | BR-078 | UC-072 Normal Flow bước 3–4 |
| 3 | Ảnh bằng chứng lô hàng bị từ chối | Camera capture | Khuyến nghị bắt buộc | BR-078 | UC-072 Postconditions |
| 4 | Tuỳ chọn "Từ chối một phần" (Partial Rejection) | Switch + TextField số lượng | Khi bật: tạo 2 bản ghi batch riêng | — | UC-072 Alternative Flow AF1 |
| 5 | Nút "Xác nhận Từ chối" | FilledButton màu `danger` + `AlertDialog` xác nhận | Không thể hoàn tác | — | UC-072 Normal Flow bước 4–5 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin lô hàng.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004/MSG-005/MSG-015 nếu thao tác thất bại.
- Success: `SnackBar` xác nhận batch đã chuyển trạng thái "Rejected".

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (bước "Log Rejections"). Thoát: xác nhận xong quay lại Workspace.
**Bố cục**: `showModalBottomSheet` form ngắn — thông tin lô hàng, checklist lý do, camera chụp ảnh, nút xác nhận màu `danger` ở cuối.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Reject Ingredient Batch Form, nền tảng Mobile, actor Service Caterers.
Bố cục: Bottom sheet gồm thông tin lô hàng, checklist lý do từ chối, camera chụp ảnh bằng chứng, tuỳ chọn từ chối một phần, nút Xác nhận Từ chối màu danger kèm AlertDialog xác nhận.
Thành phần bắt buộc: nút hành động màu danger theo SfmsStatusColors.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3243–3257 (UC-072 - Reject Ingredient Batch)
- business_rules.md BR-078
- error_message.md MSG-015
