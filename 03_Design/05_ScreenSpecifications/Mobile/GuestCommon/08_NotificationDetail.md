# Screen Spec — Notification Detail

**Actor**: Teacher, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Detail") | **UC liên quan**: — (không có UC riêng trong Report 3/traceability_matrix.md) | **Status**: Draft
**BR liên quan**: BR-123 (cảnh báo dị ứng khi quét ID — nếu thông báo loại này, chi tiết dẫn thẳng tới thông tin dị ứng học sinh), BR-142 (phát hiện từ khóa bệnh lý), BR-158 (Quiet Hours)
**MSG liên quan**: không có MSG code riêng (chỉ hiển thị lại nội dung MSG gốc đã gửi)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại `07_NotificationCenterList.md` |
| 2 | Warning Badge/Loại thông báo | Chip màu | Đỏ/Vàng/Xám theo mức độ (kế thừa từ List) | — (quy ước màu `Design_System.md` mục 3, xem ghi chú) | [Giả định — cần team xác nhận] |
| 3 | Tiêu đề thông báo | Text | — | — | [Giả định] |
| 4 | Nội dung đầy đủ (nguyên văn MSG đã gửi) | Text (dài, cuộn) | Hiển thị đúng nội dung MSG gốc | — | Theo `UI_Design_Guidelines.md` mục 4 |
| 5 | Sent Time | Text | — | — | |
| 6 | Nút hành động liên quan (nếu có deep link) | FilledButton | — | — | VD "Xem hồ sơ dị ứng học sinh" — deep link theo `Navigation_Guidelines.md` mục 3 |

### Trạng thái màn hình
- Loading: `CircularProgressIndicator` khi tải chi tiết.
- Empty: không áp dụng.
- Error: `SnackBar`/`AlertDialog` màu `danger` kèm nút "Thử lại".
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ tap 1 item trên `07_NotificationCenterList.md` (Mobile); Back (AppBar) → về List. Deep link trực tiếp từ push notification (không qua Dashboard) theo `Navigation_Guidelines.md` mục 3.
**Bố cục**: trang riêng full-screen — AppBar Back, Chip Badge + Tiêu đề trên cùng, nội dung đầy đủ cuộn dọc, nút hành động liên quan (nếu có) cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Detail, nền tảng Mobile, actor Teacher/Parent.
Bố cục: trang riêng full-screen, Chip Warning Badge, tiêu đề, nội dung đầy đủ, Sent Time, nút hành động liên quan (nếu có).
Áp dụng Design System: danger #B91C1C cho badge đỏ.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `14_Screen_Authorization_Matrix.md` khối 1 (dòng Notification Detail, không có UC).
- business_rules.md (BR-123, BR-142, BR-158); `00_Foundation/Design_System.md` mục 3 (quy ước màu — lưu ý "BR-123" trích dẫn trong Design_System.md không tồn tại trong `business_rules.md`/`traceability_matrix.md`, cần team xác nhận nguồn đúng).
- `03_Design/00_Foundation/Navigation_Guidelines.md` mục 3.

**Ghi chú quan trọng**: giống bản Web — không có nguồn field-by-field trong Report 3 và không có UC-### ánh xạ. Toàn bộ field là **[Giả định — cần team xác nhận]**.
