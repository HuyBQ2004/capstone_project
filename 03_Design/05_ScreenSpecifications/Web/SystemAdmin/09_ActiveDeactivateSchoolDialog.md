# Screen Spec — Active/Deactivate School Dialog

**Actor**: System Admin | **UC liên quan**: UC-001 (Report 3 §3.2 "Active/Deactivate School") | **Status**: Draft
**BR liên quan**: BR-021 (vô hiệu hóa trường dạng cascade)
**MSG liên quan**: MSG-001 (lỗi hệ thống lưu dữ liệu trường), MSG-004, MSG-005, MSG-011 (theo Exceptions UC-001 gốc — xem ghi chú)

> Ghi chú đối chiếu: Exceptions trong Report 3.md (dòng 1614) liệt kê "MSG-004, MSG-005, MSG-011" — nhưng MSG-011 (`error_message.md`) thực chất là "Homeroom teacher is already assigned to another classroom", không liên quan ngữ nghĩa tới Active/Deactivate School. Nhiều khả năng đây là lỗi copy-paste giữa các UC trong Report 3 gốc (Alternative Flow của UC-001 trong cùng bảng cũng mô tả nhầm nội dung "Reassignment... homeroom teacher" — vốn là AF của UC khác). **[Giả định — cần team xác nhận]**: dùng MSG-001 làm exception chính cho lỗi lưu dữ liệu trường, bỏ qua MSG-011 do không khớp nghiệp vụ.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin trường (tên, mã trường) | Text hiển thị (read-only) | — | — | |
| 2 | Trạng thái hiện tại → mới | Badge trước/sau | — | — | |
| 3 | Cảnh báo hệ quả cascade | Alert (danger) | Liệt kê rõ: toàn bộ Classroom/Staff/Student thuộc trường sẽ bị tạm dừng | BR-021 | Rủi ro cao — cảnh báo rõ ràng trước khi xác nhận |
| 4 | Nút "Xác nhận" | Button (destructive) | Kiểm tra không gây xung đột dữ liệu trước khi cho phép | BR-021 | |
| 5 | Nút "Huỷ" | Button (ghost) | — | — | |

### Trạng thái màn hình
- Loading: Button "Xác nhận" loading khi xử lý cascade (có thể mất vài giây do cascade nhiều bảng).
- Empty: Không áp dụng.
- Error: MSG-001 nếu lưu thất bại; MSG-004/MSG-005.
- Success: Toast xác nhận (Report 3 không gán MSG success riêng cho UC-001 — **[Giả định]**: dùng thông báo chung "Cập nhật trạng thái trường thành công"), cascade tạm dừng Classroom/Staff/Student (BR-021), ghi audit log.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ action toggle trạng thái ở dòng trên `School List`. Đóng lại trả về `School List`.
**Bố cục**: `AlertDialog` (hành động rủi ro cao, cascade nhiều bảng — cần xác nhận hai bước theo ghi chú nghiệp vụ ở `01_System_Admin_Screen_Flow.md`), không điều hướng trang riêng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Active/Deactivate School Dialog, nền tảng Web, actor System Admin.
Bố cục: AlertDialog giữa màn hình, hiển thị thông tin trường + cảnh báo cascade rõ ràng + 2 nút Xác nhận/Huỷ.
Thành phần bắt buộc: tên/mã trường, badge trạng thái trước/sau, alert cảnh báo cascade (Classroom/Staff/Student bị tạm dừng), nút Xác nhận (destructive), nút Huỷ.
Áp dụng Design System: alert cảnh báo dùng token danger vì rủi ro cao (cascade).
Trạng thái cần thể hiện: error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1601–1621 (Table 3-20 Active/Deactivate School Use Cases Specification)
- traceability_matrix.md §1.2 (UC-001 ↔ BR-021)
- business_rules.md BR-021
