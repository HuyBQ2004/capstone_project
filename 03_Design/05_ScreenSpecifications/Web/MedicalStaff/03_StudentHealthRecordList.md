# Screen Spec — Student Health Record List

**Actor**: Medical Staff | **UC liên quan**: UC-051 (Report 3 §3.2, dòng 2021–2039) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-172 (lịch sử đã xác minh không thể sửa/xóa, chỉ append)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header ("Health Record") | Text | — | — | Report 3 §3.3.2.3.2 |
| 2 | Tab "All students" | Tab | — | — | Danh sách toàn bộ hồ sơ sức khỏe học sinh |
| 3 | Tab "Waiting for confirmation" | Tab (kèm badge số lượng) | — | BR-031 | Lọc hồ sơ đang chờ xác minh |
| 4 | Search input | Input (text) | Tìm theo tên/lớp | — | |
| 5 | Bảng danh sách hồ sơ | Table | — | — | Cột: Student Name & Class, Status Badge, Allergy Declaration Info, Log Footnote |
| 6 | Status Badge (mỗi dòng) | Badge màu | "Awaiting Approval" (warning) / "Confirmed" (success) | BR-031 | |
| 7 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Điều hướng `04_StudentHealthRecordDetail.md` |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng.
- Empty: "Chưa có hồ sơ sức khỏe nào" (tab All) hoặc "Không có hồ sơ chờ xác nhận" (tab Waiting) + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Health Profile" trên Dashboard (Web); chọn dòng → `04_StudentHealthRecordDetail.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Page Header có 2 Tab + Search input bên phải, Content Area dùng `Table`, phân trang dưới bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Health Profile" trên Dashboard (Mobile); chọn Card → `04_StudentHealthRecordDetail.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.3.2 — AppBar (Back, Home, Screen Title), 2 Tab ngang, Search Bar, danh sách `Card` cuộn dọc, pull-to-refresh bắt buộc.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Student Health Record List, nền tảng Web, actor Medical Staff.
Bố cục: Page Header 2 Tab (All students/Waiting for confirmation) + Search input, Table
(Student Name & Class, Status Badge, Allergy Declaration Info, Log Footnote).
Áp dụng Design System: warning #B45309 cho "Awaiting Approval", success #15803D cho "Confirmed".
Trạng thái cần thể hiện: loading, empty (mỗi tab), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2021–2039 (UC-051), dòng 5708–5730 (§3.3.2.3.2).
- business_rules.md (BR-031, BR-172).
- error_message.md (MSG-004, MSG-005).
