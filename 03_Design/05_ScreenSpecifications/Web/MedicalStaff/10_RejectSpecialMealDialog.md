# Screen Spec — Reject Special Meal Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083, Exceptions) | **Status**: Draft
**BR liên quan**: BR-066 (danh mục chế độ ăn đặc biệt)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt yêu cầu (Student, Meal Type, Reason) | Text (đọc) | — | — | Kế thừa từ `07_ReviewSpecialMealRequestScreen.md` |
| 2 | Lý do từ chối | Textarea | Bắt buộc | BR-066 | UC-078 Exceptions |
| 3 | Nút "Confirm Reject" | Button (destructive) | Disabled khi chưa nhập lý do | — | Submit → cập nhật trạng thái "Rejected" |
| 4 | Nút "Cancel" | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner trên "Confirm Reject".
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; chưa nhập lý do → cảnh báo inline.
- Success: cập nhật trạng thái "Rejected", quay lại `08_SpecialMealRequestList.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Reject" trên `07_ReviewSpecialMealRequestScreen.md` (Web); Confirm/Cancel → quay lại List.
**Bố cục**: `AlertDialog` — tóm tắt yêu cầu + textarea lý do từ chối, 2 nút Confirm/Cancel.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Reject" trên `07_ReviewSpecialMealRequestScreen.md` (Mobile); Confirm/Cancel → quay lại List.
**Bố cục**: `showModalBottomSheet` — tóm tắt yêu cầu, textarea lý do từ chối, 2 nút Confirm/Cancel.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Reject Special Meal Dialog, nền tảng Web, actor Medical Staff.
Bố cục: AlertDialog, tóm tắt yêu cầu, textarea Lý do từ chối (bắt buộc), nút Confirm Reject, nút Cancel.
Áp dụng Design System: danger #B91C1C.
Trạng thái cần thể hiện: loading, error (thiếu lý do).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2065–2083 (UC-078, Exceptions).
- business_rules.md (BR-066).
- error_message.md (MSG-004, MSG-005).
