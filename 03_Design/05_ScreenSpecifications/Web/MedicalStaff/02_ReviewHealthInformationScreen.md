# Screen Spec — Review Health Information Screen

**Actor**: Medical Staff | **UC liên quan**: UC-077 (Report 3 §3.2, dòng 2043–2061) | **Status**: Draft
**BR liên quan**: BR-031 (thông tin dị ứng/sức khỏe phải được Medical Staff xem xét và xác nhận trước khi có hiệu lực)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách khai báo Pending | Table/List | — | BR-031 | UC-077 bước 2 |
| 2 | Student Name & Class (mỗi item) | Text | — | — | |
| 3 | Allergy Type/Severity (mỗi item) | Badge màu | Đỏ cho mức "Severe" (quy ước `Design_System.md` mục 3, xem ghi chú) | BR-031 | |
| 4 | Chọn 1 khai báo → xem chi tiết | Interaction | — | — | Mở panel chi tiết: loại dị ứng, mức độ nghiêm trọng, tài liệu y tế đính kèm |
| 5 | Tài liệu y tế đính kèm | Ảnh/File preview | Bắt buộc khi mức độ "Severe" (BR-031) | BR-031 | |
| 6 | Nút "Approve" | Button (primary) | Chỉ bật khi tài liệu hợp lệ | BR-031 | Chuyển trạng thái "Verified" |
| 7 | Nút "Reject" | Button (destructive) | Bắt buộc nhập lý do từ chối | BR-031 | |
| 8 | Lý do từ chối | Textarea (hiện khi bấm Reject) | Bắt buộc | BR-031 | |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng khi tải; spinner trên nút Approve/Reject khi xử lý.
- Empty: "Không có khai báo nào đang chờ duyệt" + illustration.
- Error: MSG-004, MSG-005; tài liệu y tế thiếu/không hợp lệ với mức "Severe" → chặn Approve, cảnh báo inline.
- Success: MSG-036 (Toast), danh sách tự cập nhật loại bỏ item vừa xử lý.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Student Allergy Review" trên Dashboard (Web); chọn dòng → mở `Sheet` bên phải xem chi tiết.
**Bố cục**: `Table` danh sách khai báo Pending (cột: Student Name & Class, Allergy Type, Severity Badge, ngày nộp), click dòng mở `Sheet` bên phải chứa tài liệu đính kèm (preview ảnh/PDF) + 2 nút Approve/Reject cố định cuối Sheet.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Student Allergy Review" trên Dashboard (Mobile); chọn item → xem chi tiết.
**Bố cục**: 1 cột, danh sách `Card` cuộn dọc, khi chọn mở `showModalBottomSheet` chứa chi tiết + tài liệu đính kèm + 2 nút Approve/Reject cố định dưới cùng sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Review Health Information, nền tảng Web, actor Medical Staff.
Bố cục: Table danh sách khai báo pending (Student Name & Class, Allergy Type/Severity Badge, ngày nộp),
Sheet chi tiết bên phải (tài liệu y tế đính kèm), nút Approve, nút Reject + textarea lý do.
Áp dụng Design System: danger #B91C1C cho badge Severe.
Trạng thái cần thể hiện: loading, empty, error (thiếu tài liệu), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2043–2061 (UC-077).
- business_rules.md (BR-031); `00_Foundation/Design_System.md` mục 3 (quy ước màu đỏ cho "Severe" — lưu ý: Design_System.md trích dẫn "BR-123" cho quy tắc này nhưng BR-123 không tồn tại trong `business_rules.md`/`traceability_matrix.md`, cần team xác nhận nguồn đúng, gần nhất là BR-123).
- error_message.md (MSG-004, MSG-005, MSG-036).
- 03_Design/04_ScreenFlow/08_Medical_Staff_Screen_Flow_Web.md.
