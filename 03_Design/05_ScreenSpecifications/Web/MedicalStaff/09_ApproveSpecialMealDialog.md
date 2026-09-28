# Screen Spec — Approve Special Meal Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083, bước 5) | **Status**: Draft
**BR liên quan**: BR-066 (danh mục chế độ ăn đặc biệt), BR-082 (chế biến riêng cho bữa ăn đặc biệt)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt yêu cầu (Student, Meal Type, Reason) | Text (đọc) | — | — | Kế thừa từ `07_ReviewSpecialMealRequestScreen.md` |
| 2 | Ghi chú xác nhận (tùy chọn) | Textarea | Không bắt buộc | — | |
| 3 | Nút "Confirm Approve" | Button (primary) | — | BR-066 | Submit → cập nhật trạng thái, routing tới Service Caterers |
| 4 | Nút "Cancel" | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner trên "Confirm Approve".
- Empty: không áp dụng.
- Error: MSG-004, MSG-005.
- Success: cập nhật trạng thái "Approved", thông báo Service Caterers, quay lại `08_SpecialMealRequestList.md` (Toast xác nhận chung).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Approve" trên `07_ReviewSpecialMealRequestScreen.md` (Web); Confirm/Cancel → quay lại List.
**Bố cục**: `AlertDialog` — tóm tắt yêu cầu + textarea ghi chú, 2 nút Confirm/Cancel.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Approve" trên `07_ReviewSpecialMealRequestScreen.md` (Mobile); Confirm/Cancel → quay lại List.
**Bố cục**: `showModalBottomSheet` — tóm tắt yêu cầu, textarea ghi chú, 2 nút Confirm/Cancel cố định cuối sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Approve Special Meal Dialog, nền tảng Web, actor Medical Staff.
Bố cục: AlertDialog, tóm tắt yêu cầu, ghi chú xác nhận (tùy chọn), nút Confirm Approve, nút Cancel.
Áp dụng Design System: success #15803D.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2065–2083 (UC-078, bước 5).
- business_rules.md (BR-066, BR-082).
- error_message.md (MSG-004, MSG-005).
