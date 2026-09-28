# Screen Spec — Notification Detail

**Actor**: Teacher, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Detail" — chỉ 2 actor này có màn chi tiết riêng, các actor khác có Notification Center xem mở rộng inline hoặc không có Center) | **UC liên quan**: — (không có UC riêng trong Report 3/traceability_matrix.md) | **Status**: Draft
**BR liên quan**: BR-123 (cảnh báo dị ứng khi quét ID — nếu thông báo loại này, chi tiết phải dẫn thẳng tới thông tin dị ứng học sinh liên quan), BR-142 (phát hiện từ khóa bệnh lý), BR-158 (Quiet Hours)
**MSG liên quan**: không có MSG code riêng cho màn xem chi tiết (chỉ hiển thị lại nội dung MSG gốc đã gửi)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Button (icon/link) | — | — | Quay lại `07_NotificationCenterList.md` |
| 2 | Warning Badge/Loại thông báo | Badge màu | Đỏ/Vàng/Xám theo mức độ (kế thừa từ List) | — (quy ước màu `Design_System.md` mục 3, xem ghi chú) | [Giả định — cần team xác nhận] |
| 3 | Tiêu đề thông báo | Text | — | — | [Giả định] |
| 4 | Nội dung đầy đủ (nguyên văn MSG đã gửi) | Text (dài) | Hiển thị đúng nội dung MSG gốc, không viết lại | — | Theo `UI_Design_Guidelines.md` mục 4 |
| 5 | Sent Time | Text | — | — | Kế thừa từ List |
| 6 | Nút hành động liên quan (nếu có deep link) | Button | — | — | VD thông báo dị ứng → nút "Xem hồ sơ dị ứng học sinh" điều hướng deep-link (theo `Navigation_Guidelines.md` mục 3) |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải chi tiết thông báo.
- Empty: không áp dụng (luôn có 1 thông báo cụ thể khi vào màn này).
- Error: không tải được chi tiết → `Alert variant="destructive"` kèm nút "Thử lại".
- Success: không áp dụng (màn hình xem thuần túy).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ click 1 item trên `07_NotificationCenterList.md` (chỉ actor Teacher/Parent); Back → về List.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — trang con hoặc `Sheet` trượt từ phải, hiển thị Badge + Tiêu đề trên cùng, nội dung đầy đủ ở giữa, Sent Time nhỏ góc dưới, nút hành động liên quan (nếu có) ở cuối.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ tap 1 item trên `07_NotificationCenterList.md` (Mobile); Back (AppBar) → về List.
**Bố cục**: trang riêng full-screen (theo `Navigation_Guidelines.md` mục 3 "deep link cho Notification"): AppBar Back, Badge + Tiêu đề trên cùng, nội dung đầy đủ cuộn dọc, nút hành động liên quan cố định cuối màn nếu có.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Detail, nền tảng [Web/Mobile], actor Teacher/Parent.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Warning Badge, tiêu đề, nội dung đầy đủ, Sent Time, nút hành động liên quan (nếu có deep link).
Áp dụng Design System: danger #B91C1C cho badge đỏ.
Trạng thái cần thể hiện: loading (Skeleton), error (Alert destructive).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `14_Screen_Authorization_Matrix.md` khối 1 (dòng Notification Detail, không có UC).
- business_rules.md (BR-123, BR-142, BR-158); `00_Foundation/Design_System.md` mục 3 (quy ước màu — lưu ý "BR-123" trích dẫn trong Design_System.md không tồn tại trong `business_rules.md`/`traceability_matrix.md`, cần team xác nhận nguồn đúng).
- `03_Design/00_Foundation/Navigation_Guidelines.md` mục 3 (deep link).

**Ghi chú quan trọng**: đây là màn hình **không có nguồn field-by-field trong Report 3** (Report 3 §3.3.2.1 chỉ có "Notification History" dạng list, không có màn Detail riêng) và **không có UC-### nào** trong `traceability_matrix.md` ánh xạ tới. Toàn bộ field trong bảng trên là **[Giả định — cần team xác nhận]** dựa trên suy luận hợp lý từ pattern List→Detail chung của hệ thống và yêu cầu deep-link ở `Navigation_Guidelines.md`. Đây là khoảng trống dữ liệu cần đội ngũ xác nhận trước khi khóa spec, không phải lỗi bỏ sót khi viết tài liệu này.
