# Screen Spec — Create Menu Workspace

**Actor**: Service Caterers | **UC liên quan**: UC-020 - Create Menu (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
**MSG liên quan**: MSG-051 (Warning — xung đột dị ứng), MSG-071 (Success — lưu nháp), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tuần áp dụng / Ngày phục vụ | DatePicker (M3) | Bắt buộc; tối thiểu 3 ngày trước tuần dùng | BR-061 | UC-020 Normal Flow bước 2 |
| 2 | Bữa ăn trong ngày (Meal Slot) | DropdownMenu | Bắt buộc | — | UC-020 Normal Flow bước 2 |
| 3 | Chọn món ăn (Dish Selection) | Danh sách chọn (Checkbox list) | Bắt buộc ít nhất 1 món/bữa | BR-068 | UC-020 Normal Flow bước 3 |
| 4 | Khai báo nguyên liệu + chất gây dị ứng từng món | TextField theo món (trang riêng trong stepper) | Bắt buộc đầy đủ | BR-069 | UC-020 Postconditions |
| 5 | Mục tiêu dinh dưỡng (calo, nhóm chất) | Form nhập số | Phải đáp ứng chỉ tiêu theo nhóm tuổi | BR-062 | UC-020 Normal Flow bước 3 |
| 6 | Nút "AI Assist" (Gemini AI) | FilledButton | Mở màn View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-020 Alternative Flow AF1 |
| 7 | Kết quả kiểm tra xung đột dị ứng | MaterialBanner | Tự động chạy khi lưu/submit; MSG-051 nếu có xung đột | BR-063 | UC-020 Normal Flow bước 4 |
| 8 | Nút "Lưu nháp" | FilledButton (cố định cuối màn) | Lưu trạng thái Draft, hiển thị MSG-071 | — | UC-020 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Shimmer`/`CircularProgressIndicator` khi tải danh sách món ăn/kho.
- Empty: Không áp dụng.
- Error: `AlertDialog` màu `danger` (MSG-004/MSG-005) nếu lưu thất bại.
- Success: `SnackBar` MSG-071 khi lưu nháp thành công; `MaterialBanner` màu `warning` (không tự ẩn) nếu MSG-051 xuất hiện.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Menu Management Dashboard → FAB "Tạo Thực đơn Mới" (`04_Service_Caterers_Screen_Flow.md`). Thoát: nút "Lưu nháp" quay lại Menu Management Dashboard.
**Bố cục**: Form nhiều trang riêng (không Stepper ngang do màn hình hẹp) — Trang 1: Thông tin chung; Trang 2: Chọn món & dinh dưỡng; Trang 3: Khai báo dị ứng — có nút "Tiếp theo/Quay lại" cố định dưới cùng theo `00_Foundation/Layout_Guidelines.md` mục 2.2; panel AI Recommendations mở dạng `showModalBottomSheet`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Menu Workspace, nền tảng Mobile, actor Service Caterers.
Bố cục: 3 trang tuần tự (Thông tin chung, Chọn món & dinh dưỡng, Khai báo dị ứng) với nút Tiếp theo/Quay lại cố định dưới cùng.
Thành phần bắt buộc: DatePicker, DropdownMenu bữa ăn, checkbox list chọn món, TextField nguyên liệu/dị ứng, banner cảnh báo dị ứng MSG-051.
Áp dụng Design System: ColorScheme.fromSeed #0E7490, SfmsStatusColors cho banner cảnh báo (xem Design_System.md).
Trạng thái cần thể hiện: loading, error, success (snackbar lưu nháp), warning (banner xung đột dị ứng).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2823–2841 (UC-020 - Create Menu)
- business_rules.md BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
- error_message.md MSG-051, MSG-071
