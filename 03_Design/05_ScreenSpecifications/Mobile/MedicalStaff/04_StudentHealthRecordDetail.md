# Screen Spec — Student Health Record Detail

**Actor**: Medical Staff | **UC liên quan**: — (không có UC riêng trong Matrix; nội dung suy ra từ UC-051 bước 4 "clinical file: medical conditions, medications, emergency contacts, health history" — Report 3 §3.2 dòng 2021–2039) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-172 (lịch sử đã xác minh không thể sửa/xóa, chỉ append)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class | Text | — | — | Kế thừa từ `03_StudentHealthRecordList.md` |
| 2 | Status Badge | Badge màu | "Awaiting Approval"/"Confirmed" | BR-031 | |
| 3 | Medical conditions | Text (đọc) | — | UC-051 | |
| 4 | Medications | Text (đọc) | — | UC-051 | |
| 5 | Emergency contacts | Text (đọc) | — | UC-051 | |
| 6 | Allergy Declaration (loại + mức độ) | Text/Badge | Đỏ cho "Severe" (quy ước `Design_System.md` mục 3, xem ghi chú) | BR-031 | |
| 7 | Tài liệu y tế đính kèm | Ảnh/File preview | — | BR-031 | |
| 8 | Nút "Update Record" | Button | — | UC-094 | Điều hướng `05_UpdateHealthRecordScreen.md` |
| 9 | Nút "Health History" | Button | — | UC-146 | Điều hướng `06_HealthHistoryScreen.md` (xem timeline đầy đủ, chỉ đọc) |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải chi tiết hồ sơ.
- Empty: không áp dụng (luôn có 1 hồ sơ cụ thể).
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Card trên `03_StudentHealthRecordList.md`; có thể vào từ `02_ReviewHealthInformationScreen.md` khi Medical Staff muốn xem đầy đủ hồ sơ trước khi Approve.
**Bố cục**: trang riêng full-screen, AppBar Back/Home, các block thông tin xếp dọc (thông tin cơ bản → tình trạng y tế → dị ứng → tài liệu đính kèm), 2 nút "Update Record"/"Health History" cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `03_StudentHealthRecordList.md` (Web).
**Bố cục**: `Sheet`/trang con 2 cột (thông tin cơ bản bên trái, tài liệu đính kèm bên phải), 2 nút "Update Record"/"Health History" ở góc trên phải page header.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Student Health Record Detail, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Student Name & Class, Status Badge, Medical conditions, Medications,
Emergency contacts, Allergy Declaration, tài liệu đính kèm, nút Update Record, nút Health History.
Áp dụng Design System: danger #B91C1C cho Allergy Severe.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2021–2039 (UC-051, mô tả clinical file trong Normal Flow bước 4).
- business_rules.md (BR-031, BR-172); `00_Foundation/Design_System.md` mục 3 (quy ước màu đỏ cho "Severe" — lưu ý: "BR-123" trích dẫn trong Design_System.md không tồn tại trong `business_rules.md`, cần team xác nhận).
- error_message.md (MSG-004, MSG-005).

**Ghi chú**: không có UC-### hay field-by-field riêng cho màn Detail này trong Report 3 (chỉ có màn List với Card tóm tắt) — nội dung trên suy luận hợp lý từ mô tả "clinical file" trong Normal Flow UC-051. **[Giả định — cần team xác nhận]** cấu trúc field đầy đủ.
