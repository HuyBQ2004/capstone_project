# Screen Spec — Create Menu Workspace

**Actor**: Service Caterers | **UC liên quan**: UC-020 - Create Menu (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
**MSG liên quan**: MSG-051 (Warning — xung đột dị ứng), MSG-071 (Success — lưu nháp), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tuần áp dụng / Ngày phục vụ | DatePicker | Bắt buộc; phải cách ngày nộp phê duyệt tối thiểu 3 ngày để kịp BR-061 | BR-061 | UC-020 Normal Flow bước 2 |
| 2 | Bữa ăn trong ngày (Meal Slot) | Select (Sáng/Trưa/Chiều) | Bắt buộc | — | UC-020 Normal Flow bước 2 |
| 3 | Chọn món ăn (Dish Selection) | Multi-select/Table chọn món | Bắt buộc ít nhất 1 món/bữa | BR-068 (giới hạn lặp món) | UC-020 Normal Flow bước 3 |
| 4 | Khai báo nguyên liệu + chất gây dị ứng từng món | Textarea/Tag input theo món | Bắt buộc đầy đủ trước khi lưu | BR-069 | UC-020 Postconditions |
| 5 | Mục tiêu dinh dưỡng (calo, nhóm chất) | Form nhập số + hiển thị % đạt chỉ tiêu | Phải đáp ứng chỉ tiêu theo nhóm tuổi | BR-062 | UC-020 Normal Flow bước 3 |
| 6 | Nút "AI Assist" (Gemini AI) | Button | Điều hướng/mở panel View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-020 Alternative Flow AF1 |
| 7 | Kết quả kiểm tra xung đột dị ứng | Alert banner | Tự động chạy khi lưu/submit; hiển thị MSG-051 nếu có xung đột | BR-063 | UC-020 Normal Flow bước 4 |
| 8 | Nút "Lưu nháp" | Button | Lưu trạng thái Draft, hiển thị MSG-071 | — | UC-020 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Skeleton` cho form khi tải danh sách món ăn/kho.
- Empty: Không áp dụng (form luôn hiển thị đủ field).
- Error: `Alert variant="destructive"` (MSG-004/MSG-005) nếu lưu thất bại.
- Success: `Toast` MSG-071 khi lưu nháp thành công; `Alert variant="warning"` (không tự ẩn) hiển thị MSG-051 nếu phát hiện xung đột dị ứng — không chặn lưu Draft nhưng chặn Submit sang phê duyệt (UC-087).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Menu Management Dashboard → nút "Tạo Thực đơn Mới" (`09_Service_Caterers_Screen_Flow_Web.md`). Thoát: "Lưu nháp" quay lại Menu Management Dashboard, hoặc mở AI Panel (không rời trang, dạng Dialog/Sheet).
**Bố cục**: Form dài chia `Stepper` ngang (Thông tin chung → Chọn món & dinh dưỡng → Xác nhận dị ứng) theo `00_Foundation/Layout_Guidelines.md` mục 1.2; panel AI Recommendations mở dạng `Sheet` trượt từ phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Menu Workspace, nền tảng Web, actor Service Caterers.
Bố cục: Stepper ngang 3 bước (Thông tin chung, Chọn món & dinh dưỡng, Xác nhận dị ứng), nút AI Assist nổi bật, nút Lưu nháp cố định cuối form.
Thành phần bắt buộc: DatePicker, Select bữa ăn, bảng chọn món, tag input nguyên liệu/dị ứng, banner cảnh báo dị ứng MSG-051.
Áp dụng Design System: warning banner dùng token `warning`, alert dị ứng dùng token `danger` (xem Design_System.md mục 3, BR-123 tinh thần màu đỏ dị ứng).
Trạng thái cần thể hiện: loading, error, success (toast lưu nháp), warning (banner xung đột dị ứng).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2823–2841 (UC-020 - Create Menu)
- business_rules.md BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
- error_message.md MSG-051, MSG-071
