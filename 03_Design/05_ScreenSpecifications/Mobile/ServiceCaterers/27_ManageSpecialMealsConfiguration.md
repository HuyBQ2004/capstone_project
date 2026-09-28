# Screen Spec — Manage Special Meals Configuration

**Actor**: Service Caterers | **UC liên quan**: UC-023 - Create Special Meal Plan (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-063, BR-065, BR-066, BR-069, BR-082, BR-156, BR-157
**MSG liên quan**: MSG-072 (Success), MSG-051 (Error), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — Report 3 UC-023 trích BR-071 cho "chế biến riêng", nhưng nội dung đúng trong `business_rules.md` là **BR-082**. Dùng BR-082.
>
> **✅ Đã xử lý (2026-07-01)**: field "Món thay thế cho 5 loại dị ứng phổ biến nhất" (trước đây trích BR-081) **đã bị gỡ bỏ** — team quyết định xoá hẳn BR-081 khỏi `business_rules.md` vì không thực tế (danh sách "5 loại phổ biến" không định nghĩa rõ, không có data model hỗ trợ, trùng chức năng với chính màn hình Special Meal Plan này).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có dị ứng/chế độ ăn đặc biệt đã xác minh | Card list | Chỉ hiển thị hồ sơ đã Medical Staff xác nhận | BR-065 | UC-023 Normal Flow bước 2 |
| 2 | Loại chế độ ăn đặc biệt | Chip/Dropdown | Halal, Vegetarian, Gluten-Free, Nut-Free, khác | BR-066 | UC-023 Normal Flow bước 4 |
| 3 | Công thức + hướng dẫn nấu riêng biệt | TextField multiline | Bắt buộc mô tả dụng cụ nấu riêng | BR-082 | UC-023 Normal Flow bước 5 |
| 4 | Khai báo nguyên liệu + dấu vết dị ứng tiềm ẩn | Tag input | Bắt buộc đầy đủ | BR-069 | UC-023 Normal Flow bước 6 |
| 5 | Kiểm tra không trùng allergen với hồ sơ học sinh | Validation tự động | Chặn lưu nếu phát hiện trùng | BR-063 | MSG-051 |
| 6 | Nút "AI Assist" gợi ý công thức an toàn | Button | Mở panel gợi ý (Gemini AI) | BR-156, BR-157 | UC-023 Alternative Flow AF1 |
| 7 | Nút "Lưu Kế hoạch Bữa ăn Đặc biệt" | FilledButton | Lưu, hiển thị MSG-072 | — | UC-023 Normal Flow bước 8 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải danh sách học sinh.
- Empty: Chưa có học sinh nào cần kế hoạch bữa ăn đặc biệt.
- Error: `AlertDialog` MSG-051 nếu phát hiện trùng allergen (chặn lưu).
- Success: `SnackBar` MSG-072.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Update Menu Screen (`04_Service_Caterers_Screen_Flow.md`: `UpdateMenuScr --> SpecialMealCfg`) hoặc Meal Service Management Dashboard. Thoát: lưu xong quay lại màn gọi.
**Bố cục**: Trang riêng — danh sách học sinh trên cùng (chọn 1 để cấu hình), form cấu hình bên dưới theo từng bước, nút "Lưu Kế hoạch" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Special Meals Configuration, nền tảng Mobile, actor Service Caterers.
Bố cục: danh sách học sinh dị ứng/đặc biệt, form cấu hình (loại chế độ ăn, công thức riêng, khai báo nguyên liệu), nút AI Assist, nút Lưu Kế hoạch cố định dưới cùng.
Thành phần bắt buộc: cảnh báo trùng allergen (MSG-051).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-072).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2845–2863 (UC-023 - Create Special Meal Plan)
- business_rules.md BR-063, BR-065, BR-066, BR-069, BR-082, BR-156, BR-157
- error_message.md MSG-072, MSG-051
