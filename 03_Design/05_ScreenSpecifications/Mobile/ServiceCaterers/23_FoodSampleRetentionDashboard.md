# Screen Spec — Food Sample Retention Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-042 - Manage Food Sample Retention (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-109, BR-110
**MSG liên quan**: MSG-020 (Error), MSG-059 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách tủ lưu mẫu đang hoạt động | Card list | Mỗi card: Tủ số, Nhiệt độ hiện tại, Số mẫu đang lưu | BR-109 | UC-042 Normal Flow bước 2 |
| 2 | Nút "Ghi Nhiệt độ" (Log Temperature) | Button | Điều hướng form nhập nhiệt độ 2 lần/ngày | BR-110 | UC-042 Normal Flow bước 3–5 |
| 3 | Cảnh báo nhiệt độ ngoài phạm vi 2–8°C | MaterialBanner | Nếu lệch > 30 phút → cảnh báo báo động | BR-110 | MSG-020/MSG-059 |
| 4 | Danh sách mẫu đang lưu trong từng tủ | Card con (mở rộng) | Điều hướng Food Sample Detail | BR-109 | UC-042 Normal Flow bước 2 |
| 5 | Chuyển tủ cho mẫu | Button + form | Yêu cầu lý do chuyển tủ | — | UC-042 Alternative Flow AF2 |
| 6 | FAB "Đăng ký Mẫu mới" | FloatingActionButton | Điều hướng Record Sample Information Form | — | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách tủ lưu mẫu.
- Empty: Chưa có tủ lưu mẫu nào được cấu hình.
- Error: MSG-020 khi nhiệt độ ngoài phạm vi; `MaterialBanner` MSG-059 nếu kéo dài > 30 phút.
- Success: `SnackBar` xác nhận ghi nhiệt độ/chuyển tủ thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Lưu mẫu Thực phẩm" trong Grid Menu Items (kênh chính tại bếp). Điều hướng tiếp: Record Sample Information Form, Food Sample Detail.
**Bố cục**: AppBar + Card list theo từng tủ lưu mẫu (nhiệt độ hiện tại nổi bật), mở rộng để xem danh sách mẫu con, FAB "Đăng ký Mẫu mới" góc dưới phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Retention Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list theo tủ lưu mẫu (nhiệt độ, số mẫu, nút Ghi Nhiệt độ), FAB Đăng ký Mẫu mới.
Thành phần bắt buộc: banner cảnh báo nhiệt độ ngoài phạm vi (MSG-020/MSG-059).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3709–3727 (UC-042 - Manage Food Sample Retention)
- business_rules.md BR-109, BR-110
- error_message.md MSG-020, MSG-059
