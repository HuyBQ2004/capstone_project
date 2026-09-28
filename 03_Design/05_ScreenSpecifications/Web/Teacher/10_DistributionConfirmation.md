# Screen Spec — Distribution Confirmation

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-120, BR-130
**MSG liên quan**: MSG-021, MSG-045

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Confirmation Dialog Title | Text | "Xác nhận đã phục vụ [Tên học sinh]?" / "Xác nhận cả lớp?" | BR-120 | |
| 2 | Timestamp Display | Text (tự động) | Timestamp hiện tại | BR-120 | |
| 3 | Duplicate Scan Check | Validation ẩn | Chặn xác nhận trùng | BR-130 | MSG-021 |
| 4 | Cancel Button | Button (secondary) | Đóng dialog | — | |
| 5 | Confirm Button | Button (primary) | Lưu, cập nhật Served | BR-120 | MSG-045 |

### Trạng thái màn hình
- Loading: Spinner trong dialog.
- Error: `AlertDialog` + MSG-021.
- Success: `Toast` `success` (MSG-045).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Distribution Detail hoặc action nhanh trên Distribution List. Ra: về Distribution List.
**Bố cục**: `AlertDialog` giữa màn hình — tiêu đề, timestamp tự động, 2 nút Cancel/Confirm.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Confirmation, nền tảng Web, actor Teacher.
Bố cục: AlertDialog giữa màn hình — tiêu đề xác nhận + timestamp + 2 nút Cancel/Confirm.
Thành phần bắt buộc: Confirmation Dialog Title, Timestamp Display, Cancel Button, Confirm Button.
Trạng thái cần thể hiện: loading, error (MSG-021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- Report 3.md, dòng 3799–3819 (UC-016).
- `business_rules.md` BR-120, BR-130; `error_message.md` MSG-021, MSG-045.
