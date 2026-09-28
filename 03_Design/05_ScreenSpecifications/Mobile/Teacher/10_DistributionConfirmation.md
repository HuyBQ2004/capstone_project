# Screen Spec — Distribution Confirmation

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-120 (xác nhận phân phát theo lớp), BR-130 (ngăn phân phát trùng lặp)
**MSG liên quan**: MSG-021, MSG-045

> **Ghi chú nguồn**: tương ứng "Confirm Meal Served Dialog" trong `07_Teacher_Screen_Flow.md` — không có wireframe field-by-field riêng trong Report 3, suy ra từ UC-016 bước 4-5 (Normal Flow).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Confirmation Dialog Title | Text | "Xác nhận đã phục vụ [Tên học sinh]?" hoặc "Xác nhận cả lớp đã được phục vụ?" | BR-120 | 2 chế độ: từng học sinh / cả lớp |
| 2 | Timestamp Display | Text (tự động) | Ghi timestamp hiện tại | BR-120 | Tự động, không cho sửa tay |
| 3 | Duplicate Scan Check | Validation ẩn | Chặn nếu học sinh đã được xác nhận trong phiên | BR-130 | MSG-021 nếu vi phạm |
| 4 | Cancel Button | Button (secondary) | Đóng dialog, không lưu | — | |
| 5 | Confirm Button | Button (primary) | Lưu xác nhận, cập nhật trạng thái Served | BR-120 | MSG-045 khi thành công |

### Trạng thái màn hình
- Loading: Spinner nhỏ trong dialog khi đang lưu.
- Empty: không áp dụng.
- Error: `AlertDialog`/toast `danger` + MSG-021 (trùng lặp).
- Success: `SnackBar` `success` (MSG-045), tự đóng dialog và quay về Distribution List.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Distribution Detail hoặc trực tiếp từ Distribution List (xác nhận nhanh). Ra: quay lại Distribution List sau khi xác nhận.
**Bố cục**: `showModalBottomSheet` (Material 3) trượt từ dưới lên — tiêu đề, timestamp tự động, 2 nút Cancel/Confirm cố định dưới cùng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Confirmation, nền tảng Mobile, actor Teacher.
Bố cục: Bottom sheet Material 3 — tiêu đề xác nhận + timestamp tự động + 2 nút Cancel/Confirm.
Thành phần bắt buộc: Confirmation Dialog Title, Timestamp Display, Cancel Button, Confirm Button.
Trạng thái cần thể hiện: loading (spinner nhỏ), error (MSG-021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` ("Confirm Meal Served Dialog").
- Report 3.md, dòng 3799–3819 (UC-016 bước 4-5).
- `business_rules.md` BR-120, BR-130; `error_message.md` MSG-021, MSG-045.
