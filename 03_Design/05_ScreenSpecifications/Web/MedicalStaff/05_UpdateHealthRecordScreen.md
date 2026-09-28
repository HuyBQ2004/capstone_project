# Screen Spec — Update Health Record Screen

**Actor**: Medical Staff | **UC liên quan**: UC-094 (Report 3 §3.2, dòng 2131–2149) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-172 (lịch sử đã xác minh không thể sửa/xóa — chỉ được append)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class (context, read-only) | Text | — | — | Kế thừa từ `04_StudentHealthRecordDetail.md` |
| 2 | Clinical Annotation mới | Textarea | Bắt buộc khi thêm mới; append-only | BR-172 | UC-094 bước 3 |
| 3 | Medications (chỉnh sửa) | Input/Textarea | — | BR-172 | |
| 4 | Emergency Instructions (chỉnh sửa) | Textarea | — | — | |
| 5 | Lịch sử log đã xác minh (read-only) | List (đọc) | Không được sửa/xóa | BR-172 | |
| 6 | Nút "Save" | Button (primary) | Disabled khi chưa có thay đổi | BR-031, BR-172 | Submit → MSG-036 |
| 7 | Nút "Cancel" | Button | — | — | Hủy, quay lại `04_StudentHealthRecordDetail.md` |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải dữ liệu hiện có; spinner trên "Save".
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; cố sửa/xóa log đã xác minh → chặn, cảnh báo "Không thể chỉnh sửa lịch sử đã xác minh" (BR-172).
- Success: MSG-036 (Toast), quay lại `04_StudentHealthRecordDetail.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Update Record" trên `04_StudentHealthRecordDetail.md` (Web); thành công/hủy → quay lại Detail.
**Bố cục**: `Dialog` (form <8 field, theo `Layout_Guidelines.md` mục 1.2) — form 1 cột giữa trang, khối lịch sử log đã xác minh hiển thị bên dưới form dạng read-only, nút "Save"/"Cancel" cuối Dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Update Record" trên `04_StudentHealthRecordDetail.md` (Mobile); thành công/hủy → quay lại Detail.
**Bố cục**: trang riêng full-screen, form dọc, khối "Lịch sử log đã xác minh" cuối trang dạng read-only card mờ, nút "Cancel"/"Save" cố định trên AppBar hoặc cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Health Record, nền tảng Web, actor Medical Staff.
Bố cục: Dialog form 1 cột, Clinical Annotation mới (textarea), Medications, Emergency Instructions,
khối lịch sử log đã xác minh (read-only), nút Save/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (chặn sửa log đã xác minh), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2131–2149 (UC-094).
- business_rules.md (BR-031, BR-172).
- error_message.md (MSG-004, MSG-005, MSG-036).
