# Tổng hợp Screen Specifications — MedicalStaff (MedicalStaff)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Medical Staff Dashboard

**Actor**: Medical Staff | **UC liên quan**: — (entry point, không có UC riêng — tổng hợp số liệu từ nhiều module) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-066 (danh mục chế độ ăn đặc biệt), BR-146 (tự động kích hoạt điều tra sự cố), BR-100/101/104 (điểm số vệ sinh — nguồn số liệu Compliance Points)
**MSG liên quan**: không áp dụng (màn hình tổng quan, không có hành động ghi)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | — | — | Report 3 §3.3.2.3.1 |
| 2 | Icon "Add/Plus" | Icon button | — | — | Shortcut tạo nhanh bản ghi mới |
| 3 | Icon "Notification" | Icon button (có badge đỏ) | Badge hiển thị số lượng cảnh báo hiện có | BR-138 | Điều hướng sang Notification Center (xem GuestCommon) |
| 4 | Welcome Banner | Text/Card | — | — | Tên người dùng, phòng làm việc, ngày hiện tại |
| 5 | Card "Allergy pending approval" | Thống kê (số) | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 6 | Card "Meal order needs approval" | Thống kê (số) | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 7 | Card "The issue is being resolved" | Thống kê (số) | — | BR-146 | Điều hướng sang `25_IncidentList.md` |
| 8 | Card "Compliance Points" | Thống kê (%) | — | BR-100, BR-101, BR-104 | Điều hướng sang `23_ComplianceDashboard.md` |
| 9 | Nút "Health Profile" | Button | — | — | Điều hướng sang `03_StudentHealthRecordList.md` |
| 10 | Nút "Student Allergy Review" | Button | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 11 | Nút "Approve Special Meal" | Button | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 12 | Nút "City Safety Incident" | Button | — | — | Điều hướng sang `25_IncidentList.md` |
| 13 | Nút "Food Compliance & Monitoring Board" | Button | — | — | Điều hướng sang `23_ComplianceDashboard.md` |
| 14 | Nút "View food inspection results in 3 steps" | Button | — | BR-079 | Điều hướng sang `11_FoodInspectionRecords.md` |

### Trạng thái màn hình
- Loading: `Shimmer` cho 4 card thống kê khi tải số liệu.
- Empty: nếu 1 thống kê = 0, hiển thị "0" bình thường (không phải trạng thái Empty riêng của toàn màn).
- Error: không tải được số liệu → thẻ card hiện icon lỗi nhỏ + nút "Thử lại" trong card đó (không chặn toàn màn hình).
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào ngay sau Login thành công (role Medical Staff); là màn gốc của Bottom Navigation.
**Bố cục**: Report 3 §3.3.2.3.1 — AppBar (Screen Title + icon Add + icon Notification có badge), Welcome Banner ngay dưới, lưới 4 `Card` thống kê (2×2), danh sách nút hành động dạng `ListTile`/`Card` lớn xếp dọc bên dưới. Bottom Navigation theo `Navigation_Guidelines.md` mục 2 (3–5 mục, icon kèm chữ).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Medical Staff Dashboard, nền tảng Mobile, actor Medical Staff.
Bố cục: AppBar (title + icon Add + icon Notification badge đỏ), Welcome Banner, lưới 2x2 Card thống kê
(Allergy pending approval, Meal order needs approval, Issue being resolved, Compliance Points),
6 nút hành động lớn (Health Profile, Student Allergy Review, Approve Special Meal, City Safety Incident,
Food Compliance & Monitoring Board, View food inspection results in 3 steps).
Áp dụng Design System: seed color #0E7490, badge đỏ #B91C1C cho notification.
Trạng thái cần thể hiện: loading (Shimmer trên card số liệu).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5683–5706 (§3.3.2.3.1, field-by-field gốc).
- business_rules.md (BR-031, BR-066, BR-100, BR-101, BR-104, BR-138, BR-146).
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md (sơ đồ điều hướng "Dash" node).

# Screen Spec — Review Health Information Screen

**Actor**: Medical Staff | **UC liên quan**: UC-077 (Report 3 §3.2, dòng 2043–2061) | **Status**: Draft
**BR liên quan**: BR-031 (thông tin dị ứng/sức khỏe phải được Medical Staff xem xét và xác nhận trước khi có hiệu lực)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách khai báo Pending | List/Card | — | BR-031 | UC-077 bước 2: hệ thống truy vấn danh sách khai báo sức khỏe/dị ứng đang chờ |
| 2 | Student Name & Class (mỗi item) | Text | — | — | |
| 3 | Allergy Type/Severity (mỗi item) | Badge màu | Đỏ cho mức "Severe" (quy ước `Design_System.md` mục 3, xem ghi chú) | BR-031 | |
| 4 | Chọn 1 khai báo → xem chi tiết | Interaction | — | — | Mở panel/trang chi tiết: loại dị ứng, mức độ nghiêm trọng, tài liệu y tế đính kèm |
| 5 | Tài liệu y tế đính kèm | Ảnh/File preview | Bắt buộc khi mức độ "Severe" (BR-031) | BR-031 | |
| 6 | Nút "Approve" | Button (primary) | Chỉ bật khi tài liệu hợp lệ | BR-031 | Chuyển trạng thái "Verified", kích hoạt hồ sơ sức khỏe học sinh |
| 7 | Nút "Reject" | Button (destructive) | Bắt buộc nhập lý do từ chối | BR-031 | |
| 8 | Lý do từ chối | Textarea (hiện khi bấm Reject) | Bắt buộc | BR-031 | |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách khi tải; spinner trên nút Approve/Reject khi xử lý.
- Empty: "Không có khai báo nào đang chờ duyệt" + illustration.
- Error: MSG-004, MSG-005; tài liệu y tế thiếu/không hợp lệ với mức "Severe" → chặn Approve, cảnh báo inline.
- Success: MSG-036 (Toast/SnackBar), danh sách tự cập nhật loại bỏ item vừa xử lý.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Student Allergy Review" trên Dashboard; chọn item → xem chi tiết ngay trong trang (mở rộng Card) hoặc điều hướng `04_StudentHealthRecordDetail.md` để xem đầy đủ hồ sơ.
**Bố cục**: 1 cột, danh sách `Card` cuộn dọc (mỗi khai báo 1 Card), khi chọn mở `showModalBottomSheet` chứa chi tiết + tài liệu đính kèm + 2 nút Approve/Reject cố định dưới cùng sheet.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Student Allergy Review" trên Dashboard (Web); chọn dòng → mở `Sheet` bên phải xem chi tiết.
**Bố cục**: `Table` danh sách khai báo Pending (cột: Student Name & Class, Allergy Type, Severity Badge, ngày nộp), click dòng mở `Sheet` bên phải chứa tài liệu đính kèm (preview ảnh/PDF) + 2 nút Approve/Reject cố định cuối Sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Review Health Information, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: danh sách khai báo pending (Student Name & Class, Allergy Type/Severity Badge),
panel chi tiết (tài liệu y tế đính kèm), nút Approve, nút Reject + textarea lý do.
Áp dụng Design System: danger #B91C1C cho badge Severe.
Trạng thái cần thể hiện: loading, empty, error (thiếu tài liệu), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2043–2061 (UC-077).
- business_rules.md (BR-031); `00_Foundation/Design_System.md` mục 3 (quy ước màu đỏ cho "Severe" — lưu ý: Design_System.md trích dẫn "BR-123" không tồn tại trong `business_rules.md`, cần team xác nhận).
- error_message.md (MSG-004, MSG-005, MSG-036).
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md ("AllergyReview" node).

# Screen Spec — Student Health Record List

**Actor**: Medical Staff | **UC liên quan**: UC-051 (Report 3 §3.2, dòng 2021–2039) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-172 (lịch sử đã xác minh không thể sửa/xóa, chỉ append)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title ("Health Record") | Text | — | — | Report 3 §3.3.2.3.2 |
| 2 | Tab "All students" | Tab | — | — | Danh sách toàn bộ hồ sơ sức khỏe học sinh |
| 3 | Tab "Waiting for confirmation" | Tab (kèm badge số lượng) | — | BR-031 | Lọc hồ sơ đang chờ xác minh |
| 4 | Search Bar | Input (text) | Tìm theo tên/lớp | — | |
| 5 | Student Profile Card (mỗi item) | Card | — | — | |
| 6 | Student Name & Class (trong Card) | Text | — | — | |
| 7 | Status Badge (trong Card) | Badge màu | "Awaiting Approval" (warning) / "Confirmed" (success) | BR-031 | |
| 8 | Allergy Declaration Info (trong Card) | Text | — | BR-031 | Loại dị ứng đã khai báo |
| 9 | Log Footnote (trong Card) | Text nhỏ | — | — | Người nộp/ngày, hoặc người duyệt/ngày tùy trạng thái |
| 10 | Chọn 1 Card → xem chi tiết | Interaction | — | — | Điều hướng `04_StudentHealthRecordDetail.md` |

### Trạng thái màn hình
- Loading: `Shimmer` (Mobile)/`Skeleton` (Web) cho danh sách Card/Table.
- Empty: "Chưa có hồ sơ sức khỏe nào" (tab All) hoặc "Không có hồ sơ chờ xác nhận" (tab Waiting) + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem danh sách).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Health Profile" trên Dashboard; chọn Card → `04_StudentHealthRecordDetail.md`.
**Bố cục**: Report 3 §3.3.2.3.2 — AppBar (Back, Home, Screen Title), 2 Tab ngang (All students / Waiting for confirmation), Search Bar ngay dưới Tab, danh sách `Card` cuộn dọc, pull-to-refresh bắt buộc.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Health Profile" trên Dashboard (Web); chọn dòng → `04_StudentHealthRecordDetail.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Page Header có 2 Tab (All students / Waiting for confirmation) + Search input bên phải, Content Area dùng `Table` (cột: Student Name & Class, Status Badge, Allergy Declaration Info, Log Footnote), phân trang dưới bảng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Student Health Record List, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: 2 Tab (All students/Waiting for confirmation), Search Bar, danh sách hồ sơ
(Student Name & Class, Status Badge, Allergy Declaration Info, Log Footnote).
Áp dụng Design System: warning #B45309 cho "Awaiting Approval", success #15803D cho "Confirmed".
Trạng thái cần thể hiện: loading, empty (mỗi tab), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2021–2039 (UC-051), dòng 5708–5730 (§3.3.2.3.2).
- business_rules.md (BR-031, BR-172).
- error_message.md (MSG-004, MSG-005).

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

# Screen Spec — Update Health Record Screen

**Actor**: Medical Staff | **UC liên quan**: UC-094 (Report 3 §3.2, dòng 2131–2149) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-172 (lịch sử đã xác minh không thể sửa/xóa — chỉ được append)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class (context, read-only) | Text | — | — | Kế thừa từ `04_StudentHealthRecordDetail.md` |
| 2 | Clinical Annotation mới | Textarea | Bắt buộc khi thêm mới; append-only | BR-172 | UC-094 bước 3: "append clinical annotations" |
| 3 | Medications (chỉnh sửa) | Input/Textarea | — | BR-172 | Có thể sửa thông tin thuốc hiện tại (không phải log lịch sử đã xác minh) |
| 4 | Emergency Instructions (chỉnh sửa) | Textarea | — | — | |
| 5 | Lịch sử log đã xác minh (read-only) | List (đọc) | Không được sửa/xóa | BR-172 | Hiển thị mờ/khóa chỉnh sửa, chỉ xem |
| 6 | Nút "Save" | Button (primary) | Disabled khi chưa có thay đổi | BR-031, BR-172 | Submit → MSG-036 |
| 7 | Nút "Cancel" | Button | — | — | Hủy, quay lại `04_StudentHealthRecordDetail.md` |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải dữ liệu hiện có; spinner trên "Save".
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; cố sửa/xóa log đã xác minh → chặn thao tác, cảnh báo "Không thể chỉnh sửa lịch sử đã xác minh" (BR-172).
- Success: MSG-036 (Toast/SnackBar), quay lại `04_StudentHealthRecordDetail.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Update Record" trên `04_StudentHealthRecordDetail.md`; thành công/hủy → quay lại Detail.
**Bố cục**: trang riêng full-screen, form dọc theo `Layout_Guidelines.md` mục 2.2, khối "Lịch sử log đã xác minh" đặt cuối trang dạng read-only card mờ, nút "Cancel"/"Save" cố định trên AppBar hoặc cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Update Record" trên `04_StudentHealthRecordDetail.md` (Web); thành công/hủy → quay lại Detail.
**Bố cục**: `Dialog`/trang con — form 1 cột giữa trang (theo `Layout_Guidelines.md` mục 1.2, form <8 field dùng Dialog), khối lịch sử log đã xác minh hiển thị bên dưới form dạng read-only, nút "Save"/"Cancel" ở cuối Dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Health Record, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Clinical Annotation mới (textarea), Medications, Emergency Instructions,
khối lịch sử log đã xác minh (read-only, khóa sửa), nút Save/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (chặn sửa log đã xác minh), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2131–2149 (UC-094).
- business_rules.md (BR-031, BR-172).
- error_message.md (MSG-004, MSG-005, MSG-036).

# Screen Spec — Health History Screen

**Actor**: Medical Staff | **UC liên quan**: UC-146 (Report 3 §3.2, dòng 2219–2234) | **Status**: Draft
**BR liên quan**: BR-172 (audit trail bất biến — timeline chỉ đọc, không sửa/xóa)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class (context) | Text | — | — | Kế thừa từ `04_StudentHealthRecordDetail.md` |
| 2 | Timeline sự kiện sức khỏe/dị ứng/khai báo | Timeline (đọc) | Chỉ đọc, không sửa/xóa | BR-172 | UC-146 bước 2–3 |
| 3 | Mỗi mốc thời gian (event) | Card/Item | — | — | Loại sự kiện (khai báo/xác nhận/sự cố), ngày giờ, người thực hiện |
| 4 | Nút "Export" | Button | — | — | Xuất timeline dạng báo cáo (PDF/Excel theo BR-178) |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` cho timeline.
- Empty: "Chưa có lịch sử sức khỏe nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem); xuất báo cáo thành công → Toast xác nhận chung (không có MSG code riêng, dùng theo BR-178).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md`; Back → quay lại Detail.
**Bố cục**: trang riêng full-screen, AppBar Back, danh sách Timeline dọc (mốc mới nhất trên đầu), nút "Export" cố định góc trên phải AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md` (Web); Back → quay lại Detail.
**Bố cục**: `Sheet`/trang con — Timeline dọc chiếm toàn bộ Content Area, nút "Export" ở góc trên phải page header.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Health History, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Timeline sự kiện sức khỏe/dị ứng/khai báo (chỉ đọc), nút Export.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2219–2234 (UC-146).
- business_rules.md (BR-172, BR-178 xuất báo cáo).
- error_message.md (MSG-004, MSG-005).

# Screen Spec — Review Special Meal Request Screen

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt), BR-066 (danh mục chế độ ăn đặc biệt: Halal/Vegetarian/Gluten-Free/Nut-Free)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class (context) | Text | — | — | Kế thừa từ `08_SpecialMealRequestList.md` |
| 2 | Dietary type | Text/Select (đọc) | Phải thuộc danh mục chuẩn (BR-066) | BR-066 | UC-078 bước 3–4 |
| 3 | Duration | Text (đọc) | — | — | |
| 4 | Medical justification | Textarea (đọc) | — | — | |
| 5 | Tài liệu minh chứng (nếu có) | Ảnh/File preview | — | BR-031 | |
| 6 | Nút "Approve" | Button (primary) | Chỉ bật khi dietary type khớp danh mục chuẩn | BR-066 | Điều hướng `09_ApproveSpecialMealDialog.md` |
| 7 | Nút "Reject" | Button (destructive) | — | — | Điều hướng `10_RejectSpecialMealDialog.md` |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải chi tiết yêu cầu.
- Empty: không áp dụng (luôn có 1 yêu cầu cụ thể khi vào màn này).
- Error: MSG-004, MSG-005; dietary type không thuộc danh mục chuẩn (BR-066) → cảnh báo, chặn Approve cho tới khi Medical Staff xác nhận phân loại lại.
- Success: chuyển tiếp sang dialog Approve/Reject tương ứng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Special Meal Card trên `08_SpecialMealRequestList.md`; Approve/Reject → dialog tương ứng, sau đó quay lại List.
**Bố cục**: trang riêng full-screen, AppBar Back/Home, block thông tin xếp dọc (Dietary type → Duration → Medical justification → tài liệu), 2 nút Approve/Reject cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `08_SpecialMealRequestList.md` (Web); Approve/Reject → `Dialog` tương ứng.
**Bố cục**: `Sheet` bên phải chứa toàn bộ thông tin yêu cầu + tài liệu minh chứng, 2 nút Approve/Reject cố định cuối Sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Review Special Meal Request, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Dietary type, Duration, Medical justification, tài liệu minh chứng, nút Approve, nút Reject.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (dietary type không hợp lệ).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2065–2083 (UC-078).
- business_rules.md (BR-031, BR-066).
- error_message.md (MSG-004, MSG-005).

# Screen Spec — Special Meal Request List

**Actor**: Medical Staff | **UC liên quan**: UC-036 (Report 3 §3.2, dòng 1977–1995 — xem ghi chú lệch tên cuối file) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt), BR-066 (danh mục chế độ ăn đặc biệt), BR-172 (log không sửa/xóa)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title ("Special Meal") | Text | — | — | Report 3 §3.3.2.3.3 |
| 2 | Tab "Awaiting approval" | Tab | — | BR-031 | Danh sách yêu cầu đang chờ duyệt |
| 3 | Tab "Approved" | Tab | — | — | Lịch sử yêu cầu đã duyệt |
| 4 | Special Meal Card (mỗi item) | Card | — | — | |
| 5 | Student Name & Class (trong Card) | Text | — | — | |
| 6 | Status Badge (trong Card) | Badge màu | "Needs Approval" (warning) | BR-031 | |
| 7 | Meal Type (trong Card) | Text | Phải thuộc danh mục BR-066 | BR-066 | VD "Purely vegetarian menu" |
| 8 | Reason (trong Card) | Text | — | — | VD "Religion / Voluntary" |
| 9 | Chọn 1 Card → xem chi tiết | Interaction | — | — | Điều hướng `07_ReviewSpecialMealRequestScreen.md` |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho danh sách.
- Empty: "Không có yêu cầu bữa ăn đặc biệt nào" (mỗi tab) + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng ở màn List (MSG-036 hiển thị sau khi Approve ở dialog con).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Approve Special Meal" trên Dashboard; chọn Card → `07_ReviewSpecialMealRequestScreen.md`.
**Bố cục**: Report 3 §3.3.2.3.3 — AppBar (Back, Home, Screen Title), 2 Tab ngang (Awaiting approval/Approved), danh sách `Card` cuộn dọc, pull-to-refresh bắt buộc.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Approve Special Meal" trên Dashboard (Web); chọn dòng → `07_ReviewSpecialMealRequestScreen.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Page Header 2 Tab, Content Area dùng `Table` (cột: Student Name & Class, Status Badge, Meal Type, Reason), phân trang dưới bảng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Special Meal Request List, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: 2 Tab (Awaiting approval/Approved), danh sách yêu cầu
(Student Name & Class, Status Badge, Meal Type, Reason).
Áp dụng Design System: warning #B45309 cho "Needs Approval".
Trạng thái cần thể hiện: loading, empty (mỗi tab), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1977–1995 (UC-036), dòng 5731–5757 (§3.3.2.3.3).
- business_rules.md (BR-031, BR-066, BR-172).
- error_message.md (MSG-004, MSG-005, MSG-036).

**Ghi chú lệch tên**: `14_Screen_Authorization_Matrix.md` ánh xạ màn "Special Meal Request List" với UC-036, nhưng nội dung UC-036 trong Report 3 mô tả là "View List Allergy & Special Meals" (thiên về xác minh **hồ sơ dị ứng**, không chỉ riêng special meal). Field-by-field thực tế của màn List này lấy từ Report 3 §3.3.2.3.3 "Screens Special Meal For Student" (đáng tin cậy hơn cho đúng màn hình). **[Giả định — cần team xác nhận]** UC-036 có đúng là UC nền cho màn này hay đây là 1 trong các trường hợp lệch tên đã ghi nhận ở `03_Medical_Staff_Screen_Flow.md` (mục ghi chú đầu file).

# Screen Spec — Approve Special Meal Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083, bước 5) | **Status**: Draft
**BR liên quan**: BR-066 (danh mục chế độ ăn đặc biệt), BR-082 (chế biến riêng cho bữa ăn đặc biệt — thông tin routing tới Service Caterers)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt yêu cầu (Student, Meal Type, Reason) | Text (đọc) | — | — | Kế thừa từ `07_ReviewSpecialMealRequestScreen.md` |
| 2 | Ghi chú xác nhận (tùy chọn) | Textarea | Không bắt buộc | — | |
| 3 | Nút "Confirm Approve" | Button (primary) | — | BR-066 | Submit → cập nhật trạng thái, routing yêu cầu tới Service Caterers |
| 4 | Nút "Cancel" | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner trên "Confirm Approve" trong lúc cập nhật trạng thái + gửi thông báo Service Caterers.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005.
- Success: cập nhật trạng thái yêu cầu thành "Approved", thông báo gửi tới Service Caterers (theo UC-078 Postcondition), quay lại `08_SpecialMealRequestList.md` (Toast xác nhận chung, không có MSG code riêng cho hành động approve special meal cụ thể).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Approve" trên `07_ReviewSpecialMealRequestScreen.md`; Confirm/Cancel → quay lại `08_SpecialMealRequestList.md`.
**Bố cục**: `showModalBottomSheet` (theo `Component_Guidelines.md`) — tóm tắt yêu cầu trên cùng, textarea ghi chú, 2 nút Confirm/Cancel cố định cuối sheet.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Approve" trên `07_ReviewSpecialMealRequestScreen.md` (Web); Confirm/Cancel → quay lại List.
**Bố cục**: `AlertDialog` (hành động không thể hoàn tác, theo `Component_Guidelines.md`) — tóm tắt yêu cầu + textarea ghi chú, 2 nút Confirm/Cancel.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Approve Special Meal Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: tóm tắt yêu cầu, ghi chú xác nhận (tùy chọn), nút Confirm Approve, nút Cancel.
Áp dụng Design System: success #15803D cho hành động Approve.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2065–2083 (UC-078, bước 5).
- business_rules.md (BR-066, BR-082).
- error_message.md (MSG-004, MSG-005).

# Screen Spec — Reject Special Meal Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083, Exceptions: "Medical Staff rejects request and specifies reason") | **Status**: Draft
**BR liên quan**: BR-066 (danh mục chế độ ăn đặc biệt — lý do từ chối thường liên quan không thuộc danh mục chuẩn)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt yêu cầu (Student, Meal Type, Reason) | Text (đọc) | — | — | Kế thừa từ `07_ReviewSpecialMealRequestScreen.md` |
| 2 | Lý do từ chối | Textarea | Bắt buộc | BR-066 | UC-078 Exceptions |
| 3 | Nút "Confirm Reject" | Button (destructive) | Disabled khi chưa nhập lý do | — | Submit → cập nhật trạng thái "Rejected" |
| 4 | Nút "Cancel" | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner trên "Confirm Reject".
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; chưa nhập lý do → cảnh báo inline, chặn submit.
- Success: cập nhật trạng thái "Rejected", thông báo phụ huynh (theo pattern chung của hệ thống), quay lại `08_SpecialMealRequestList.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Reject" trên `07_ReviewSpecialMealRequestScreen.md`; Confirm/Cancel → quay lại `08_SpecialMealRequestList.md`.
**Bố cục**: `showModalBottomSheet` — tóm tắt yêu cầu, textarea lý do từ chối (bắt buộc), 2 nút Confirm/Cancel.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Reject" trên `07_ReviewSpecialMealRequestScreen.md` (Web); Confirm/Cancel → quay lại List.
**Bố cục**: `AlertDialog` (hành động không thể hoàn tác) — tóm tắt yêu cầu + textarea lý do từ chối, 2 nút Confirm/Cancel.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Reject Special Meal Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: tóm tắt yêu cầu, textarea Lý do từ chối (bắt buộc), nút Confirm Reject, nút Cancel.
Áp dụng Design System: danger #B91C1C cho hành động Reject.
Trạng thái cần thể hiện: loading, error (thiếu lý do).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2065–2083 (UC-078, Exceptions).
- business_rules.md (BR-066).
- error_message.md (MSG-004, MSG-005).

# Screen Spec — Food Inspection Records

**Actor**: Medical Staff | **UC liên quan**: UC-112 (Report 3 §3.2, dòng 3553–3571) | **Status**: Draft
**BR liên quan**: BR-091 đến BR-099 (quy trình kiểm tra ba bước, nhiệt độ, tái kiểm tra)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-016 (Error), MSG-017 (Error)

> **Lịch sử sửa filter**: (1) **09/07/2026** — đổi filter chính "Bước kiểm tra" → "Kết quả" (`review_medicalstaff_compliance_kitcheninspection.md` Mục 5); (2) **12/07/2026** — bỏ hẳn filter "Bước" và filter "Món", chỉ giữ Date range + Kết quả. Xem `review_screen_medical_staff.md` Mục 15.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo khoảng ngày | Date range picker | — | — | UC-112 bước 3 |
| 2 | Filter theo Kết quả | Select | Đạt / Không đạt / Đang thực hiện — khớp field `overall` của `InspectionSession` | BR-094, BR-098 | **Sửa 09/07/2026** — thay cho filter "Bước kiểm tra" cũ (đã bỏ hẳn) |
| 3 | Danh sách bản ghi kiểm tra | List/Table | — | — | Cột: ngày, trạng thái (Passed/Failed), nhân viên phụ trách |
| 4 | Status Badge (mỗi bản ghi) | Badge màu | Success (Passed)/Danger (Failed) | BR-094, BR-098 | |
| 5 | Chọn 1 bản ghi → xem chi tiết | Interaction | — | — | Hiện ảnh bằng chứng, timestamp, GPS, ghi chú nhân viên (UC-112 bước 5) |

**Đã bỏ (12/07/2026)**: filter "Bước kiểm tra" và filter "Món/thực đơn".

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải danh sách.
- Empty: AF1 "No Records Found" — hiển thị "Không tìm thấy bản ghi phù hợp" theo đúng tiêu chí filter.
- Error: MSG-004, MSG-005, MSG-016, MSG-017.
- Success: không áp dụng (màn hình xem).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "View food inspection results in 3 steps" trên Dashboard; chọn item → mở chi tiết dạng bottom sheet.
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` (date range, Kết quả) phía trên, danh sách `Card` cuộn dọc, mỗi Card có Status Badge góc phải.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "View food inspection results in 3 steps" trên Dashboard (Web); chọn dòng → mở `Sheet` chi tiết bên phải.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — thanh filter (date range, Kết quả) phía trên `Table`, `Table` hiển thị danh sách kèm phân trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Inspection Records, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: filter (date range, Kết quả: Đạt/Không đạt/Đang thực hiện), danh sách bản ghi
kiểm tra với Status Badge, panel chi tiết (ảnh bằng chứng, timestamp, GPS, ghi chú).
Áp dụng Design System: success #15803D cho Passed, danger #B91C1C cho Failed.
Trạng thái cần thể hiện: loading, empty (không có bản ghi phù hợp), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên (KHÔNG có filter Bước, KHÔNG có filter Món).
```

## Nguồn dữ liệu
- Report 3.md, dòng 3553–3571 (UC-112).
- business_rules.md (BR-091–BR-099).
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 5, Mục 6.
- error_message.md (MSG-004, MSG-005, MSG-016, MSG-017).

# Screen Spec — Conduct Food Safety Inspection Screen

**Actor**: Medical Staff | **UC liên quan**: UC-014 (Report 3 §3.2, dòng 3443–3461), **UC-186 (Manage Kitchen Inspection Checklist — mới, 06/07/2026)** | **Status**: Draft
**BR liên quan**: BR-101 (kiểm tra an toàn/vệ sinh hàng tháng), BR-102 (nhật ký kiểm soát côn trùng), BR-103 (xác nhận sức khỏe nhân viên bếp), BR-104 (điểm số vệ sinh đầu bếp), BR-110 (nhiệt độ tủ lưu mẫu — kiểm tra chéo, xem ghi chú), **BR-207 (bằng chứng theo từng mục + chú thích căn cứ pháp lý — đã chốt 05/07/2026, sửa đổi 06/07/2026)**, ~~BR-208 (phân loại Định kỳ/Đột xuất — đã chốt 05/07/2026)~~ **(Deprecated 09/07/2026)**, **BR-210 (Medical Staff tự quản lý CRUD checklist — mới, 06/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG cảnh báo điểm thấp (**chưa xác định mã**, xem ghi chú), MSG-094 (xóa hạng mục đang được tham chiếu)

> **Lịch sử cập nhật**
> - **2026-07-04**: đổi field "Upload ảnh bằng chứng vệ sinh bếp" (1 ảnh chung cho cả checklist) → **đánh giá Đạt/Không đạt + bằng chứng theo từng hạng mục**; đề xuất BR-206/BR-207 để thay ghost-citation `BR-105`/`BR-119` (BR-105 canonical là quy tắc lưu mẫu thực phẩm, chủ đề khác; BR-119 không tồn tại trong `business_rules.md`).
> - **2026-07-05 (đính chính)**: bản 2026-07-04 đã gỡ nhầm `BR-110` với lý do "thuộc module Food Sample Retention, không liên quan checklist bếp". Sai — `Report 3.md` (dòng 3459, đặc tả gốc UC-014) liệt kê rõ BR-110 trong dòng "Business Rules" của chính UC-014. Đã khôi phục. BR-110 (2–8°C) vốn thuộc module Food Sample Retention (Service Caterers, UC-042/069); Medical Staff dùng lại đúng mã này khi kiểm tra chéo tủ lưu mẫu trong lúc audit bếp (UC-014) — không phải quy tắc mới. Đã bổ sung hạng mục D4 tương ứng vào `MedicalStaff_KitchenInspectionChecklist_Proposal.md`.
> - **2026-07-05 (quyết định chính thức của đội dự án, đợt 1)**:
>   1. **BR-207 được tạm chốt** (per-item Pass/Fail + bằng chứng có điều kiện + chú thích căn cứ pháp lý dạng icon "ℹ") — đã merge chính thức vào `business_rules.md` §12. Không còn ở trạng thái "đề xuất".
>   2. **BR-206 bị loại bỏ** — không chính thức hóa ngưỡng "điểm <60%" thành Business Rule mới (không có căn cứ pháp luật/quy định nội bộ).
>   3. ~~**BR-208 mới** — hợp thức hóa field "Loại kiểm tra: Định kỳ / Đột xuất".~~ **Deprecated 09/07/2026** — bỏ hẳn field/bước chọn Loại kiểm tra, xem `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 3.
> - **2026-07-05 (quyết định chính thức của đội dự án, đợt 2 — descope hậu quả khóa bếp)**: đội dự án quyết định **bỏ hoàn toàn** hậu quả "khóa vận hành bếp, chặn phân phát bữa ăn" mà UC-014 AF1 gốc mô tả khi điểm <60% — đây là **descope có chủ đích** so với `Report 3.md` gốc, không phải sửa lỗi trích dẫn. Lý do: (a) không có căn cứ pháp lý cho việc khóa cả bếp chỉ vì 1 điểm số nội bộ; (b) `MSG-024` mà bản gốc dùng cho cơ chế này thực chất thuộc phạm vi **Incident Management** (theo `error_message.md`, MSG-024 được xếp vào module Incident Management, dùng cho BR-149 — cách ly **lô nguyên liệu** khi có sự cố ATTP) — SRS gốc đã mượn nhầm cơ chế của module khác cho một ngưỡng kiểm tra định kỳ. **Hành vi mới**: điểm <60% → hệ thống **vẫn tự động gửi cảnh báo tới School Administration** (bỏ Service Caterers/School Board — không còn là tình huống khẩn cấp cấp-sự-cố), nhưng **bếp tiếp tục vận hành bình thường, không chặn phân phát bữa ăn**. Kết quả Pass/Failed vẫn ghi nhận đủ theo BR-104. Không tái dùng `MSG-024` (sai module + sai ngữ nghĩa "operation blocked") — **cần đội dự án cấp mã MSG mới** cho cảnh báo không-chặn này. `BR-149` (cách ly lô nguyên liệu khi có sự cố) **giữ nguyên, không liên quan** tới quyết định này. Chi tiết xem `business_rules.md` §12 (ghi chú sau BR-208) và `MedicalStaff_UserStory.md` US-08.
> - **2026-07-06 (checklist chuyển sang dynamic, quyết định người dùng)**: checklist Kitchen Safety and Sanitation Checklist (UC-014) không còn là danh sách hạng mục cố định — Medical Staff nay có toàn quyền CRUD (thêm/sửa/xóa/sắp xếp) cả nhóm và hạng mục, kể cả nội dung chú thích căn cứ pháp lý (icon "ℹ"), **không cần System Admin cấu hình, không cần phê duyệt** (BR-210 mới, thay thế điều khoản "System Admin cấu hình chú thích" trong BR-207 gốc). Nội dung 6 nhóm checklist cụ thể (thay thế Nhóm A–F cũ) chuyển sang dùng làm **dữ liệu khởi tạo mặc định** — xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (bản cập nhật 06/07/2026) và UC-186 (màn hình quản lý checklist mới, mô tả ở mục "Quản lý Checklist" bên dưới).
> **2026-07-09 (deprecate BR-208)**: bỏ hẳn field/bước chọn "Loại kiểm tra: Định kỳ / Đột xuất" khỏi luồng tạo phiên kiểm tra — không còn giá trị nghiệp vụ thực tế cho Medical Staff, chỉ phục vụ thống kê không ai dùng. Giữ nguyên số hiệu BR-208 (đánh dấu Deprecated, không tái sử dụng). Chi tiết: `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 3.
> Danh sách hạng mục checklist cụ thể (6 nhóm, thay cho Nhóm A–F cũ) và nội dung chú thích pháp lý lấy từ `MedicalStaff_KitchenInspectionChecklist_Proposal.md` vẫn là **bản nháp tham khảo nội dung/dữ liệu khởi tạo** — phần *cơ chế* (BR-207/BR-210) đã chốt, nhưng *nội dung câu chữ từng hạng mục cụ thể* do Medical Staff tự điều chỉnh sau khi hệ thống vận hành (CRUD, BR-210).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "New Inspection" | Button | — | — | Bắt đầu phiên kiểm tra mới |
| 2 | Kitchen Safety and Sanitation Checklist | Danh sách hạng mục (mỗi hạng mục = 1 Card con) | Bắt buộc đánh giá đủ mọi hạng mục | BR-101, BR-104 | Danh sách hạng mục cụ thể: xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (6 nhóm — Hồ sơ, Người tiếp xúc trực tiếp, Bảo quản, Sơ chế/chế biến, An toàn thực phẩm, Kiểm thực 3 bước/lưu mẫu; dữ liệu khởi tạo, Medical Staff có thể tự CRUD qua UC-186). UC-014 bước 2–3 gốc: đồng phục (găng/khẩu trang/lưới tóc), vệ sinh nơi làm việc, điều kiện bảo quản |
| 2e | Nút "Quản lý Checklist" (mở màn hình UC-186) | Button (secondary, đặt cạnh "New Inspection") | — | BR-210 | Điều hướng sang luồng CRUD nhóm/hạng mục/chú thích — xem mục "Quản lý Checklist (UC-186)" bên dưới |
| 2a | Mỗi hạng mục — Toggle "Đạt / Không đạt" | Toggle/Radio | Bắt buộc chọn 1 trong 2 cho từng hạng mục | BR-207 | Thay cho cơ chế "1 điểm tổng chung" cũ — mỗi hạng mục có kết quả riêng, dùng làm input tính điểm tổng (BR-104) |
| 2b | Mỗi hạng mục — Ảnh minh chứng (camera trực tiếp) | Camera input + preview | **Đạt** → tùy chọn. **Không đạt** → bắt buộc ít nhất 1 ảnh gắn đúng hạng mục | BR-207 | Thay thế field "Upload ảnh bằng chứng vệ sinh bếp" cũ (1 ảnh chung cho cả phiên); nút camera ưu tiên hiển thị đầu Card theo `Layout_Guidelines.md` mục 2.2 |
| 2c | Mỗi hạng mục — Lý do không đạt (comment) | Text field | Bắt buộc khi hạng mục = Không đạt; ẩn khi = Đạt | BR-207 | Nội dung có thể tái sử dụng (pre-fill) cho "Mô tả vi phạm" ở `22_RecordStaffViolation.md`/`21_RecordProcessViolation.md` nếu phát hiện leo thang thành vi phạm |
| 2d | Mỗi hạng mục — icon "ℹ" Chú thích căn cứ pháp lý | Icon mở BottomSheet (thu gọn mặc định) | Không bắt buộc tương tác | BR-207, BR-210 | Nội dung text đơn giản, không versioning/effective-date, **theo từng trường** (không còn áp dụng chung mọi trường như bản BR-207 gốc). **[SỬA 06/07/2026]** Medical Staff tự sửa trực tiếp qua màn hình "Quản lý Checklist" (UC-186), không còn do System Admin cấu hình. Thay đổi nội dung ghi qua audit log chung (BR-170). Nội dung khởi tạo tham khảo: cột "Chú thích rút gọn" trong `MedicalStaff_KitchenInspectionChecklist_Proposal.md` |
| 3 | Xác minh chứng nhận kiểm soát côn trùng (quý) | Checkbox/Toggle + ngày hết hạn | Bắt buộc xác nhận còn hiệu lực | BR-102 | Giữ nguyên so với bản trước |
| 4 | Xác minh khám sức khỏe nhân viên bếp (năm) | Checkbox/Toggle + ngày hết hạn | Bắt buộc xác nhận còn hiệu lực | BR-103 | Giữ nguyên so với bản trước |
| 5 | Nút "Submit Checklist" | Button (primary) | Disabled khi: còn hạng mục chưa đánh giá, HOẶC có hạng mục Không đạt mà thiếu ảnh/thiếu lý do | BR-101–104, BR-207 | Hệ thống tự tính điểm từ kết quả Đạt/Không đạt của từng hạng mục — **công thức/trọng số tính điểm chưa chốt** (xem Ghi chú 4, `MedicalStaff_KitchenInspectionChecklist_Proposal.md`) |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tính điểm checklist sau submit.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; hạng mục Không đạt thiếu ảnh/lý do → chặn submit, cảnh báo inline tại đúng Card hạng mục (**mã MSG cụ thể chưa chốt** — tạm đề xuất tái dùng MSG-085 "thiếu thông tin bắt buộc" hoặc cấp MSG mới, cần đội dự án xác nhận); điểm < 60% → hệ thống lưu kết quả **"Failed"**, hiển thị Alert (warning, không phải banner khóa cố định) và **tự động gửi cảnh báo tới School Administration** — **không khóa vận hành bếp, không chặn phân phát bữa ăn** (descope 05/07/2026 so với AF1 gốc; không gắn mã BR nào; **mã MSG chưa xác định**, không dùng MSG-024 — xem "Lịch sử cập nhật" ở trên).
- Success: điểm ≥ 60% → lưu báo cáo "Passed", hiển thị Toast xác nhận (không có MSG code Success riêng cho hành động này trong `error_message.md` — dùng thông báo chung "Đã lưu kết quả kiểm tra").

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Conduct Kitchen Inspection" trên Dashboard; thành công/thất bại → quay lại Dashboard (nếu Failed, hiện Alert cảnh báo — không phải banner khóa — theo `UI_Design_Guidelines.md` mục 5.5).
**Bố cục**: form nhiều bước dùng `Stepper` Flutter (theo `Layout_Guidelines.md` mục 2.2). Mỗi bước là 1 nhóm checklist (đã bỏ bước chọn "Loại kiểm tra" — deprecate BR-208, 09/07/2026), mỗi hạng mục là 1 `Card` con gồm: nhãn hạng mục + icon "ℹ" (mở BottomSheet chú thích căn cứ pháp lý) + Toggle "Đạt/Không đạt" + nút camera (đặt ưu tiên đầu Card, chỉ bắt buộc chụp khi Không đạt) + ô nhập lý do (chỉ hiện khi Không đạt). Nút "Submit Checklist" ở bước cuối.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Conduct Food Safety Inspection" trên Dashboard (Web); thành công/thất bại → quay lại Dashboard.
**Bố cục**: `Stepper` ngang cố định trên form (theo `Layout_Guidelines.md` mục 1.2), mỗi bước hiển thị 1 nhóm checklist trong `Card` (đã bỏ bước chọn "Loại kiểm tra" — deprecate BR-208, 09/07/2026). Mỗi hạng mục là 1 dòng gồm: nhãn hạng mục + icon "ℹ" (mở Popover chú thích) + Toggle "Đạt/Không đạt" + khu vực upload ảnh (`Input type="file"` + preview, chỉ bắt buộc khi Không đạt) + Textarea lý do (chỉ hiện khi Không đạt). Nút "Submit Checklist" cố định cuối form.

---

## Quản lý Checklist (UC-186, mới 06/07/2026)

> **BR liên quan**: BR-210. Đây là luồng riêng, tách khỏi luồng "thực hiện kiểm tra" (form chính ở trên) — vào từ nút "Quản lý Checklist" (field 2e).

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| M1 | Danh sách nhóm (group) checklist | Danh sách mở rộng (ExpansionTile) + kéo-thả | — | BR-210 | Mỗi nhóm hiển thị tên + danh sách hạng mục con, kéo-thả để sắp xếp lại thứ tự |
| M2 | Nút "Thêm nhóm mới" (FAB hoặc nút cuối danh sách) | Button | Bắt buộc nhập tên nhóm, không để trống | BR-210 | Tạo nhóm rỗng, thêm hạng mục sau |
| M3 | Nút "Sửa tên nhóm" / "Xóa nhóm" (menu 3 chấm trên mỗi nhóm) | Icon button + BottomSheet menu | Xóa nhóm có hạng mục → cảnh báo xác nhận trước (Dialog) | BR-210 | Xóa không ảnh hưởng dữ liệu lịch sử đã lưu (chỉ ẩn khỏi phiên kiểm tra mới) |
| M4 | Nút "Thêm hạng mục" (trong mỗi nhóm) | Button | Bắt buộc nhập tên hạng mục | BR-210 | Hạng mục mới mặc định chưa có chú thích căn cứ pháp lý (có thể để trống) |
| M5 | Mỗi hạng mục — Nút "Sửa" / "Xóa" / kéo-thả sắp xếp | Icon button + drag handle | Xóa hạng mục **đang được tham chiếu bởi phiên kiểm tra đã lưu** → cảnh báo (MSG-094), vẫn cho phép xóa nhưng dữ liệu lịch sử được giữ nguyên, chỉ ẩn khỏi phiên mới | BR-210 | — |
| M6 | Mỗi hạng mục — Text field "Chú thích căn cứ pháp lý" (mở BottomSheet khi sửa) | Text field | Không bắt buộc | BR-207, BR-210 | Text đơn giản, không versioning; hiển thị lại đúng nội dung này ở icon "ℹ" trong màn hình thực hiện kiểm tra (field 2d) |
| M7 | Nút "Lưu thay đổi" | Button (primary) | — | BR-170 | Mọi thay đổi (thêm/sửa/xóa/sắp xếp) ghi vào audit log chung khi lưu |

### Trạng thái màn hình
- Empty: chưa có nhóm nào → hiển thị gợi ý "Thêm nhóm đầu tiên", có thể nạp sẵn 6 nhóm mặc định từ `MedicalStaff_KitchenInspectionChecklist_Proposal.md` khi khởi tạo trường mới.
- Error: xóa hạng mục đang dùng → cảnh báo xác nhận (MSG-094), không phải lỗi chặn cứng — Medical Staff vẫn có thể xác nhận xóa (dữ liệu lịch sử được giữ, chỉ ẩn khỏi phiên mới).
- Success: Toast/SnackBar xác nhận đã lưu thay đổi checklist.

### Platform Layout — Mobile (Quản lý Checklist)
**Entry/Exit points**: vào từ nút "Quản lý Checklist" (field 2e) trên màn hình chính UC-014; thoát quay lại màn hình chính, checklist mới áp dụng ngay cho phiên kiểm tra tiếp theo.
**Bố cục**: danh sách nhóm dạng ExpansionTile theo chiều dọc (một màn hình, cuộn dọc), mỗi nhóm mở rộng hiển thị danh sách hạng mục con; chạm vào hạng mục mở BottomSheet sửa tên + chú thích. Nút "Thêm nhóm mới" dạng FAB góc dưới phải, nút "Lưu thay đổi" cố định cuối màn hình.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Conduct Food Safety Inspection, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên] (KHÔNG có bước chọn Loại kiểm tra — đã deprecate BR-208, 09/07/2026)
Thành phần bắt buộc: Kitchen Safety and Sanitation
Checklist theo từng bước, mỗi hạng mục gồm: nhãn hạng mục, icon "ℹ" chú thích căn cứ pháp lý (thu gọn),
Toggle Đạt/Không đạt, ảnh minh chứng (bắt buộc chỉ khi Không đạt), ô nhập lý do (chỉ hiện khi Không đạt).
Ngoài checklist còn có xác nhận chứng nhận côn trùng (còn hạn), xác nhận khám sức khỏe nhân viên bếp
(còn hạn), nút Submit Checklist.
Áp dụng Design System: warning #B45309 cho Alert điểm <60% (không phải banner khóa cố định — bếp vẫn vận hành).
Trạng thái cần thể hiện: loading, error (thiếu ảnh/lý do tại hạng mục Không đạt; điểm <60% → Alert
cảnh báo School Administration, không khóa bếp), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3443–3461 (UC-014 — bao gồm dòng "Business Rules": BR-101, BR-102, BR-103, BR-104, BR-105, BR-110; Postconditions/AF1 gốc mô tả khóa bếp — **đã descope 05/07/2026**, xem "Lịch sử cập nhật").
- `business_rules.md` §12 (BR-101, BR-102, BR-103, BR-104, BR-110, BR-207, ~~BR-208~~ Deprecated 09/07/2026 — đã chính thức; BR-210 — mới 06/07/2026; ghi chú cuối §12 về việc không chính thức hóa BR-206 và quyết định descope khóa bếp).
- `MedicalStaff_UserStory.md` (US-05–US-08, US-07b — US-08 đã cập nhật theo quyết định descope; US-22 — mới 06/07/2026, Epic 5, quản lý checklist).
- `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (danh sách hạng mục 6 nhóm — thay Nhóm A–F cũ, 06/07/2026; nội dung chú thích căn cứ pháp lý — **bản nháp tham khảo/dữ liệu khởi tạo**, cơ chế CRUD đã chốt qua BR-210).
- `error_message.md` (MSG-004, MSG-005; MSG-024 không còn áp dụng cho màn này — thuộc Incident Management; MSG-094 — mới 06/07/2026, xóa hạng mục đang dùng).
- `traceability_matrix.md` (UC-186 — mới 06/07/2026).

# Screen Spec — Food Sample Records

**Actor**: Medical Staff (dùng chung với School Administration, Service Caterers theo UC-118 — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-118 (Report 3 §3.2, dòng 3779–3793) | **Status**: Draft
**BR liên quan**: BR-107 (chụp ảnh mẫu bắt buộc), BR-108 (GPS/timestamp tự động), BR-110 (nhiệt độ bảo quản 2–8°C), BR-111 (thời gian lưu mẫu tối thiểu), BR-114 (liên kết mẫu với thực đơn/kiểm tra)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo khoảng ngày/bữa/món/trạng thái | Filter bar | — | — | UC-118 bước 3: Retained/Retrieved/Disposed |
| 2 | Sổ lưu mẫu số hóa (Mẫu số 5) | Table/List | Đủ 12 cột theo Mẫu số 5 | BR-107, BR-108, BR-111, BR-114 | Tên mẫu, thời gian thu, chi tiết bảo quản, chữ ký hủy/nhân chứng |
| 3 | Status Badge (mỗi bản ghi) | Badge màu | Info (Retained)/Warning (Retrieved)/Secondary (Disposed) | — | |
| 4 | Nút "Export" | Button | — | BR-178 | Xuất Excel/PDF cho thanh tra. **Cập nhật 11/07/2026**: xuất đúng theo khoảng ngày đang chọn ở field 1 (Filter theo khoảng ngày) — không xuất toàn bộ dữ liệu bất kể filter đang áp dụng |
| 5 | Chọn 1 bản ghi → xem chi tiết/thao tác | Interaction | — | — | Điều hướng `14_RetrieveFoodSampleRecordsDialog.md` khi cần thu hồi mẫu để điều tra |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` khi tải sổ lưu mẫu.
- Empty: "Chưa có mẫu lưu nào phù hợp bộ lọc" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem, trừ hành động Export không có MSG riêng).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Sample Register" trên Dashboard; chọn item → mở chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` phía trên, danh sách `Card` (không dùng bảng dày đặc theo `Component_Guidelines.md` — mỗi bản ghi 1 Card tóm tắt 12 cột quan trọng nhất), nút "Export" nổi góc trên phải AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "Food Sample Records — Mẫu số 5" trên Sidebar (Web); chọn dòng → chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — thanh filter phía trên `Table` đầy đủ 12 cột theo Mẫu số 5, nút "Export" góc trên phải page header.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Sample Records (Mẫu số 5), nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: filter (ngày/bữa/món/trạng thái), sổ lưu mẫu 12 cột, Status Badge, nút Export.
Áp dụng Design System: info #1D4ED8 (Retained), warning #B45309 (Retrieved), secondary #64748B (Disposed).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3779–3793 (UC-118).
- business_rules.md (BR-107, BR-108, BR-110, BR-111, BR-114, BR-178).
- error_message.md (MSG-004, MSG-005).

# Screen Spec — Retrieve Food Sample Records Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-076 (Report 3 §3.2, dòng 3757–3771) | **Status**: Draft
**BR liên quan**: BR-111 (thời gian lưu mẫu tối thiểu), BR-113 (thu hồi mẫu khi có sự cố), BR-114 (liên kết mẫu với thực đơn/kiểm tra, sửa 12/07/2026), BR-150 (điều tra nguyên nhân gốc rễ), **BR-212 (bổ sung thông tin bàn giao mẫu — mới 12/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 12/07/2026 (Nhóm E, căn cứ `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md` Phụ lục V "Biên bản bàn giao mẫu")**: (1) field "Tên người ủy quyền" đổi từ nhập tay sang **tự động = tài khoản Medical Staff đang đăng nhập, readonly** (BR-114); (2) bổ sung field "Đại diện phòng lab nhận mẫu" (tùy chọn) và "Chỉ tiêu yêu cầu kiểm nghiệm" (bắt buộc) (BR-212); (3) biên nhận in ra mở rộng nội dung để khớp cấu trúc Phụ lục V. **Không** thêm bước ký 2 bên/chụp ảnh/gate cứng như BR-211 — xem rationale ở BR-212.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tìm mẫu theo ngày/món/batch | Search/Filter | — | — | UC-076 bước 1 |
| 2 | Danh sách mẫu khớp kết quả | List/Card | Hiển thị trạng thái + thời gian lưu còn lại | BR-111 | |
| 3 | Nút "Authorize Retrieval" (mỗi item) | Button | Chỉ khả dụng khi mẫu còn ở trạng thái "Retained" | BR-113 | Nếu mẫu đã "Disposed" → chỉ hiện nút "Link Disposed Sample to Incident" (AF1) |
| 4 | Incident ID | Input (text)/Select | Bắt buộc; phải là ID đang active | BR-114 | Option đã đóng bị disable ngay tại dropdown |
| 5 | Tên người ủy quyền (`authorizedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản Medical Staff đăng nhập | BR-114 | **Sửa 12/07/2026**: trước đây Input tự do, nay readonly — nhất quán với `reportedBy` (BR-183), "Đại diện Y tế trường học" (BR-211) |
| 6 | Phòng lab đích | Select | Bắt buộc | BR-114 | Giữ nguyên — danh sách phòng lab cố định |
| 7 | Đại diện phòng lab nhận mẫu | TextField tự do | Tùy chọn — có thể chưa xác định tại thời điểm thu hồi | BR-212 | Mới — khớp field "Đại diện là" (Bên nhận mẫu) trong Phụ lục V |
| 8 | Chỉ tiêu yêu cầu kiểm nghiệm | TextField multiline | Bắt buộc | BR-212 | Mới — khớp cột "Chỉ tiêu yêu cầu kiểm nghiệm" trong Phụ lục V; MSG-023 nếu thiếu |
| 9 | Nút "Submit" | Button (primary) | Disabled khi thiếu field bắt buộc (4, 6, 8) | BR-113, BR-114, BR-150, BR-212 | Khóa mẫu (chuyển "Under Investigation"/"Retrieved"), sinh biên nhận in (nội dung mở rộng theo Phụ lục V) |

### Trạng thái màn hình
- Loading: spinner khi xác thực Incident ID + khóa bản ghi mẫu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; MSG-023 (Incident ID không hợp lệ/đã đóng, AF2, hoặc thiếu Chỉ tiêu yêu cầu kiểm nghiệm) → yêu cầu nhập lại/bổ sung; mẫu đã Disposed (AF1) → chỉ cho phép liên kết metadata hủy mẫu vào sự cố, không cho thu hồi vật lý.
- Success: khóa bản ghi thành công, hiển thị biên nhận thu hồi có thể in (UC-076 Postcondition, nội dung mở rộng theo Phụ lục V), quay lại `13_FoodSampleRecords.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (hoặc từ `17_InvestigationWorkspace.md` nhánh "1. Extracting Stored Food Samples"); thành công → quay lại màn nguồn.
**Bố cục**: `showModalBottomSheet` — kết quả tìm kiếm mẫu trên cùng, form Incident ID/người ủy quyền (readonly)/phòng lab/đại diện phòng lab/chỉ tiêu kiểm nghiệm bên dưới, nút "Submit" cố định cuối sheet.

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (Web) hoặc từ Investigation Workspace (Web); thành công → quay lại màn nguồn.
**Bố cục**: `Dialog` — kết quả tìm kiếm mẫu trên cùng (list ngắn), form Incident ID/người ủy quyền (readonly)/phòng lab/đại diện phòng lab/chỉ tiêu kiểm nghiệm bên dưới, nút "Submit"/"Cancel" cuối Dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Retrieve Food Sample Records Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: tìm mẫu theo ngày/món/batch, danh sách mẫu khớp, nút Authorize Retrieval,
Incident ID, tên người ủy quyền (hiển thị tự động, không sửa được), phòng lab đích (Select),
đại diện phòng lab nhận mẫu (tùy chọn), chỉ tiêu yêu cầu kiểm nghiệm (multiline, bắt buộc), nút Submit.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (MSG-023 Incident ID không hợp lệ, mẫu đã Disposed), success (biên nhận in).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm bước ký/chụp ảnh biên bản.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3757–3771 (UC-076).
- business_rules.md (BR-111, BR-113, BR-114, BR-150).
- error_message.md (MSG-004, MSG-005, MSG-023).

# Screen Spec — View Food Safety Reports Screen

**Actor**: Medical Staff | **UC liên quan**: UC-117 (Report 3 §3.2, dòng 3623–3637); gộp chung nội dung UC-115 "View Food Safety Compliance Report" (dòng 3601–3617) theo ghi chú tại `14_Screen_Authorization_Matrix.md` khối 6 ("không có dòng riêng, gộp chung với View Food Safety Reports Screen") | **Status**: Draft
**BR liên quan**: BR-100 (điểm vệ sinh hàng ngày), BR-101 (kiểm tra hàng tháng), BR-102 (kiểm soát côn trùng), BR-103 (khám sức khỏe NV bếp), BR-104 (điểm vệ sinh đầu bếp), BR-110 (nhiệt độ bảo quản mẫu, 2–8°C)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 12/07/2026**: (1) gỡ mọi tham chiếu BR-089 (Deprecated theo `business_rules.md` Changelog v1.6 — xem BR-110 thay thế, chỉ áp dụng riêng tủ lưu mẫu, không có cảnh báo IoT tự động); (2) đổi nhóm "Pest Control Log" từ badge hiệu lực nhị phân sang **nhật ký sự kiện** (đồng bộ quyết định `review_medicalstaff_compliance_kitcheninspection.md` Mục 1, đã áp code `compliance/page.tsx`). Xem `review_screen_medical_staff.md` Mục 14.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn nhóm báo cáo | Tab/Select | 3 nhóm: Monthly Kitchen Audits, Pest Control Log, Refrigerator Temperature Logs (chỉ tủ lưu mẫu) | BR-101, BR-102, BR-110 | UC-117 bước 2 |
| 2 | Date range filter | Date range picker | — | — | UC-117 bước 3 |
| 3 | Refrigerator ID filter (chỉ áp dụng nhóm Temperature Logs, tủ lưu mẫu) | Select | — | BR-110 | |
| 4 | Kết quả báo cáo/xu hướng (Monthly Kitchen Audits, Temperature Logs) | Chart/Table | — | BR-101, BR-110 | UC-117 bước 4 |
| 4b | Nhật ký kiểm soát côn trùng (nhóm Pest Control Log) | Danh sách `Card`/Table — mỗi mục = 1 lần kiểm soát/xử lý | Trường: Ngày kiểm soát/xử lý, Đơn vị thực hiện, Loại côn trùng phát hiện (nếu có) + số lượng ước tính, Biện pháp xử lý, Ngày hẹn kiểm tra tiếp theo | BR-102 | Thay cho badge "Còn hiệu lực"/"Đã hết hạn" cũ |
| 5 | **Khối Compliance Report (từ UC-115)**: Date range selector riêng | Date range picker | — | — | UC-115 bước 2–3 |
| 6 | Export format (PDF/Excel) | Select | — | BR-178 | |
| 7 | Compliance summary dashboard | Dashboard/Card | Tổng hợp audit hàng tháng, pest control (bản ghi gần nhất + ngày hẹn tiếp theo), khám sức khỏe NV, điểm vệ sinh hàng ngày, nhiệt độ tủ lưu mẫu | BR-100–104, BR-110 | UC-115 bước 4–5; nêu rõ đánh giá quá hạn |
| 8 | Nút "Export" | Button | — | BR-178 | Xuất Excel (nhóm báo cáo thường)/PDF theo chuẩn cơ quan y tế (Compliance Report). **Cập nhật 11/07/2026**: mỗi khối xuất đúng theo khoảng ngày của chính khối đó — khối báo cáo theo nhóm dùng field 2 (Date range filter, UC-117), khối Compliance Summary dùng field 5 (Date range selector riêng, UC-115) — không xuất toàn bộ dữ liệu bất kể filter |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải dữ liệu báo cáo/biểu đồ.
- Empty: "Không có dữ liệu cho khoảng thời gian đã chọn" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem/xuất báo cáo, không có MSG riêng cho hành động Export).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard.
**Bố cục**: 1 cột, Tab chọn nhóm báo cáo trên cùng, filter (date range, refrigerator ID) ngay dưới, kết quả hiển thị dạng `fl_chart`/danh sách Card cuộn dọc, nút "Export" nổi góc trên phải AppBar. Khối Compliance Summary (UC-115) hiển thị dưới dạng section riêng có thể cuộn tới, không tách trang khác.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Tab chọn nhóm báo cáo trên Page Header, Content Area chia 2 khối: khối trên (báo cáo theo nhóm, dùng `recharts`/Table), khối dưới (Compliance Summary Dashboard dạng Card thống kê), nút "Export" góc trên phải page header cho từng khối.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Food Safety Reports (gộp Compliance Report), nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Tab chọn nhóm báo cáo (Monthly Kitchen Audits/Pest Control Log/Refrigerator
Temperature Logs), filter ngày/refrigerator ID, chart/table kết quả (nhóm Pest Control Log hiển thị
Table/Card nhật ký sự kiện: Ngày, Đơn vị thực hiện, Loại côn trùng + số lượng ước tính, Biện pháp xử lý,
Ngày hẹn tiếp theo), Compliance Summary Dashboard, nút Export (PDF/Excel).
Áp dụng Design System: warning #B45309 cho đánh giá quá hạn/nhiệt độ vượt ngưỡng.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3623–3637 (UC-117), dòng 3601–3617 (UC-115).
- business_rules.md (BR-100, BR-101, BR-102, BR-103, BR-104, BR-110, BR-178; BR-089 Deprecated — Changelog v1.6).
- error_message.md (MSG-004, MSG-005).
- `14_Screen_Authorization_Matrix.md` khối 6 (ghi chú gộp UC-115 vào màn này).
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 1 (nhật ký kiểm soát côn trùng).

# Screen Spec — View School Policy Screen

**Actor**: Medical Staff (dùng chung với Teacher, Service Caterers theo UC-137) | **UC liên quan**: UC-137 (Report 3 §3.2, dòng 1144 — chỉ có mô tả tóm tắt 1 dòng trong danh sách UC tổng hợp, KHÔNG có bảng đặc tả đầy đủ Normal/Alternative/Exception Flow trong Report 3) | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường học theo vai trò — Teacher/Medical Staff/Service Caterers được xem chính sách trường mình để làm việc hàng ngày)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách chính sách/quy định của trường | List/Document viewer | Chỉ xem chính sách của trường mình (schoolId trong JWT) | BR-022 | [Giả định — cần team xác nhận cấu trúc field cụ thể, do UC-137 không có Normal Flow đầy đủ] |
| 2 | Tiêu đề + ngày ban hành (mỗi chính sách) | Text | — | — | [Giả định] |
| 3 | Nội dung chính sách (xem chi tiết) | Text/PDF viewer | Chỉ đọc | — | [Giả định] |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải danh sách chính sách.
- Empty: "Trường chưa cập nhật chính sách nào" + illustration.
- Error: MSG-004, MSG-005; truy cập chính sách trường khác → từ chối theo BR-022 (cross-school access denied).
- Success: không áp dụng (màn hình xem thuần túy).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "View School Policy" trên Compliance Dashboard (`23_ComplianceDashboard.md`).
**Bố cục**: trang riêng full-screen, danh sách `Card`/`ListTile` các chính sách, chọn 1 mục mở PDF viewer hoặc trang nội dung đầy đủ.

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "View School Policy" trên Sidebar (nhóm "Kiểm tra & Giám sát ATTP", theo `08_Medical_Staff_Screen_Flow_Web.md`).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — danh sách chính sách dạng `Table`/Card grid bên trái, panel xem nội dung bên phải khi chọn 1 mục.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View School Policy, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: danh sách chính sách (tiêu đề + ngày ban hành), panel/trang xem nội dung đầy đủ.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1144 (UC-137, chỉ có mô tả tóm tắt).
- business_rules.md (BR-022).
- error_message.md (MSG-004, MSG-005).

**Ghi chú quan trọng — khoảng trống dữ liệu**: UC-137 chỉ xuất hiện dưới dạng 1 dòng mô tả trong bảng tổng hợp UC (Report 3 dòng 1144: *"Allows Medical Staff to review school food safety policies and operational regulations"*), **không có bảng đặc tả Normal Flow/Alternative Flow/Exception đầy đủ** ở bất kỳ đâu trong Report 3 §3.2 (đã grep toàn file, không tìm thấy). Toàn bộ field trong bảng trên là **[Giả định — cần team xác nhận]** dựa trên mô tả tóm tắt + BR-022 — đây là khoảng trống dữ liệu nguồn cần đội ngũ bổ sung UC đầy đủ trước khi khóa spec, không phải suy diễn tùy tiện.

# Screen Spec — Investigation Workspace

**Actor**: Medical Staff | **UC liên quan**: UC-041 (Report 3 §3.2, dòng 4445–4460) | **Status**: Draft
**BR liên quan**: BR-146 (ngưỡng tự động kích hoạt điều tra), BR-147 (phân loại severity), BR-148 (cảnh báo tức thời High), BR-149 (cách ly lô hàng), BR-150 (điều tra nguyên nhân gốc rễ), BR-151 (bản đồ truy xuất trong 4 giờ), BR-152 (CAPA), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 15/07/2026 — banner "đang chờ ai"**: bổ sung field 0, áp dụng quyết định 09/07/2026 (Mục 7.6 review file) chưa từng đưa vào Screen Spec. Xem `17_InvestigationWorkspace.md` (bản đầy đủ, Web + Mobile) và Mục 10.6.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 0 | Banner "Đang chờ ai" 🆕 | Banner (đọc, điều kiện) | Chỉ hiện đúng điểm chờ | — | "Đang chờ Kitchen Manager xác minh nhà cung cấp" (E11) / "Đang chờ School Administration duyệt CAPA" (E17) / "Đang chờ triển khai & giám sát CAPA" (E18-19) / "Đang chờ School Administration xem xét báo cáo" (E21) |
| 1 | Card "1. Extracting Stored Food Samples" | Card điều hướng | — | BR-150 | Mở `14_RetrieveFoodSampleRecordsDialog.md` |
| 2 | Card "2. Gathering Evidence" | Card điều hướng | — | BR-150 | Mở khối "Collect Evidence" (xem bên dưới) |
| 3 | Card "3. Identify the Root Cause" | Card điều hướng | — | BR-150, BR-151 | Ghi nhận kết quả xét nghiệm lab + kết luận chuyên môn |
| 4 | Card "4. Proposed Remedial Measures" | Card điều hướng | — | BR-152 | Mở khối "Treatment Measures" (xem bên dưới) |
| **Khối "Collect Evidence" (§3.3.2.3.10)** | | | | | |
| 5 | Type of evidence | Select | — | — | VD "Student/Teacher Statement Record" |
| 6 | Brief description | Textarea | — | — | |
| 7 | Attached documents / "Take a photo/Upload a file" | Camera + File input | — | BR-150 | |
| 8 | Nút "Save Evidence" | Button (primary) | — | — | |
| **Khối "Identify the Root Cause"** | | | | | |
| 9 | Kết quả xét nghiệm lab | Textarea/File | — | BR-150 | [Giả định — cần team xác nhận cấu trúc field cụ thể ngoài mô tả tổng quát UC-041 bước 4] |
| 10 | Kết luận nguyên nhân gốc rễ | Textarea | — | BR-150, BR-151 | |
| **Khối "Treatment Measures" (§3.3.2.3.11)** | | | | | |
| 11 | Medical treatment/correction measures | Textarea | — | BR-152 | |
| 12 | Unit/Person in charge (Responsible Party) | Input (text) | — | BR-152 | VD "Kitchen Manager & Milk Supplier" |
| 13 | Nút "Establish Measures" | Button (primary) | — | BR-152 | Giao CAPA cho Service Caterers, hạn 12 giờ |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải dữ liệu điều tra hiện có.
- Empty: chưa có bằng chứng/kết luận nào → hiển thị trạng thái trống cho từng card tương ứng.
- Error: MSG-004, MSG-005, MSG-023 (Incident ID không hợp lệ khi liên kết mẫu).
- Success: lưu từng khối thành công → Toast xác nhận chung; hoàn tất "Establish Measures" → CAPA được giao, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nút "Step 2: Incident Investigation Space"); "Establish Measures" → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.7 — AppBar (Back, Home, "Back to Details"), 4 Card xếp dọc (1 cột) dẫn tới 4 khối con, mỗi khối con mở trang riêng (Collect Evidence, Treatment Measures theo §3.3.2.3.10–11) có nút "Come back" quay lại Investigation Space.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); "Establish Measures" → quay lại Incident Detail.
**Bố cục**: theo `08_Medical_Staff_Screen_Flow_Web.md` — trình bày dạng nhiều panel song song (bằng chứng | root cause | CAPA) tận dụng màn hình rộng thay vì 4 bước tuần tự như Mobile; mỗi panel tương ứng 1 khối (Extract Sample/Evidence/Root Cause/Treatment Measures).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Investigation Workspace, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: 4 khối (Extracting Stored Food Samples, Gathering Evidence, Identify Root Cause,
Proposed Remedial Measures) với field tương ứng (Type of evidence, Brief description, Attached documents,
kết quả xét nghiệm, kết luận nguyên nhân, Medical treatment measures, Responsible Party), nút Save
Evidence/Establish Measures.
Áp dụng Design System: primary #0E7490, critical #7E22CE cho trạng thái Quarantine liên quan.
Trạng thái cần thể hiện: loading, empty (mỗi khối), error (MSG-023).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4445–4460 (UC-041), dòng 5826–5943 (§3.3.2.3.7, 3.3.2.3.10, 3.3.2.3.11).
- business_rules.md (BR-146–152, BR-154).
- error_message.md (MSG-004, MSG-005, MSG-023).
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md ("InvSpace" node).

**Ghi chú**: "Collect Evidence" và "Treatment Measures" không có dòng riêng trong `14_Screen_Authorization_Matrix.md` khối 6 (chỉ "Investigation Workspace" UC-041 có cột `x`) — gộp vào file này làm khối con, không tách file riêng.

# Screen Spec — Assign Incident Severity Dialog

> **⚠️ DEPRECATED 15/07/2026** — không còn là dialog riêng. UC-009 giữ nguyên số hiệu/BR-147/BR-148, nay gộp trình bày vào `26_IncidentDetail.md` (field 5-6). Xem `26_IncidentDetail.md` (bản đầy đủ, Web + Mobile) và Mục 10.4 review file.

**Actor**: Medical Staff | **UC liên quan**: UC-009 (Report 3 §3.2, dòng 4335–4349) | **Status**: ~~Draft~~ Deprecated
**BR liên quan**: BR-147 (tiêu chí severity: Low/Medium/High), BR-148 (cảnh báo ngay lập tức High), BR-149 (cách ly lô hàng)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Reported symptoms (context, đọc) | Text | — | — | Report 3 §3.3.2.3.6, kế thừa từ Incident Detail |
| 2 | Affected student count (context, đọc) | Text | — | — | |
| 3 | Suspected dish information (context, đọc) | Text | — | — | |
| 4 | Severity level | RadioGroup/Select | Bắt buộc chọn: Low/Medium/High (BR-147) | BR-147 | Low = mild không cần khám; Medium = cần khám; High = nhập viện/bùng phát |
| 5 | Clinical Risk Assessment & Recommendations | Textarea | Bắt buộc khi chọn Medium/High | BR-147 | Ghi chú kế hoạch hành động ban đầu |
| 6 | Nút "Save review & Activate process" | Button (primary) | Disabled khi chưa chọn severity | BR-147, BR-148, BR-149 | Submit → lưu severity + kích hoạt quy trình tương ứng |

### Trạng thái màn hình
- Loading: spinner trên nút Save khi lưu + dispatch cảnh báo.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023 (Incident ID không hợp lệ).
- Success: severity = High → **AF1**: cảnh báo khẩn cấp gửi ngay tới School Board, Service Caterers, cơ quan y tế địa phương (BR-148), lô hàng nghi vấn chuyển Quarantine (BR-149) — banner `critical` hiển thị; severity Low/Medium → lưu bình thường, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nút "Step 1: Assess the severity") hoặc từ Investigation Workspace; Save → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.6 — trang riêng full-screen, AppBar (Back/Home/Cancel), context đọc trên cùng, RadioGroup Severity level, Textarea Clinical Risk Assessment, nút "Save review & Activate process" cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); Save → quay lại Incident Detail.
**Bố cục**: `Dialog` (hành động quan trọng, theo `Component_Guidelines.md`) — context đọc trên cùng, RadioGroup Severity + Textarea, nút Save/Cancel cuối Dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Assign Incident Severity Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: context (symptoms, affected count, suspected dish), RadioGroup Severity level
(Low/Medium/High), Textarea Clinical Risk Assessment & Recommendations, nút Save review & Activate process.
Áp dụng Design System: critical #7E22CE cho mức High, warning #B45309 cho Medium, success #15803D cho Low.
Trạng thái cần thể hiện: loading, error, success (banner critical nếu High).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4335–4349 (UC-009), dòng 5808–5825 (§3.3.2.3.6).
- business_rules.md (BR-147, BR-148, BR-149).
- error_message.md (MSG-004, MSG-005, MSG-023).

# Screen Spec — Record Corrective Actions Report Screen

**Actor**: Medical Staff | **UC liên quan**: UC-168 (Report 3 §3.2, dòng 4467–4481) | **Status**: Draft
**BR liên quan**: BR-152 (CAPA), BR-153 (phê duyệt hành động khắc phục — School Administration), BR-155 (đóng sự cố — cần Medical Staff xác nhận hồi phục 100%)
**MSG liên quan**: MSG-004 (Error — dùng lại khi còn học sinh chưa hồi phục theo EX1), MSG-005 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với "Generate Incident Report Dialog"**: từ `26_IncidentDetail.md`, 2 nút tách rời (Validate CAPA/UC-168, Step 3 Generate Report/UC-033) nay gộp thành 1 điểm vào "Hoàn tất sự cố" — màn này là Khối A, Generate Incident Report Dialog là Khối B. Giữ nguyên UC-168/UC-033 riêng biệt. Xem Mục 10.7 review file.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách sự cố có CAPA đã Approved chờ xác nhận y tế | List/Table | Chỉ hiển thị incident đã qua UC-005 (School Admin approve CAPA) | BR-153 | UC-168 bước 2 |
| 2 | Chọn 1 sự cố → xem chi tiết | Interaction | — | — | Hiện log sự cố, chi tiết CAPA đã duyệt, student clinical tracking log |
| 3 | Ghi chú kiểm tra bếp (verification comments) | Textarea | Bắt buộc | BR-152 | UC-168 bước 5 |
| 4 | Xác nhận học sinh hồi phục 100% | Checkbox/Toggle theo từng học sinh | Bắt buộc TẤT CẢ học sinh = đã hồi phục mới được submit | BR-155 | EX1: còn 1 học sinh chưa hồi phục → chặn |
| 5 | Nút "Submit Validation" | Button (primary) | Disabled khi chưa đủ điều kiện (bước 4) | BR-152, BR-155 | Chuyển trạng thái "Validated - Ready for Closure" |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải danh sách/chi tiết.
- Empty: "Không có sự cố nào đang chờ xác nhận y tế" + illustration.
- Error: MSG-004 (EX1 — còn học sinh chưa hồi phục 100%, BR-155), MSG-005.
- Success: MSG-046 (Toast/SnackBar), trạng thái chuyển "Validated - Ready for Closure", quay lại danh sách.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nhánh "validate CAPA đã approved").
**Bố cục**: trang riêng full-screen, danh sách sự cố dạng `Card` cuộn dọc, chọn 1 item mở trang chi tiết validation (context CAPA + checklist hồi phục học sinh + textarea + nút Submit cố định cuối màn).

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web, nhánh validate CAPA).
**Bố cục**: `Table` danh sách sự cố chờ xác nhận, chọn dòng mở `Sheet` bên phải chứa context CAPA + checklist hồi phục + textarea + nút Submit cuối Sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Corrective Actions Report, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: danh sách sự cố chờ xác nhận y tế, chi tiết CAPA đã duyệt, ghi chú kiểm tra bếp,
checklist xác nhận hồi phục 100% từng học sinh, nút Submit Validation.
Áp dụng Design System: success #15803D khi đủ điều kiện, danger #B91C1C khi còn học sinh chưa hồi phục.
Trạng thái cần thể hiện: loading, empty, error (MSG-004 EX1 còn triệu chứng), success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4467–4481 (UC-168).
- business_rules.md (BR-152, BR-153, BR-155).
- error_message.md (MSG-004, MSG-005, MSG-046).

# Screen Spec — Generate Incident Report Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-033 (Report 3 §3.2, dòng 4401–4415) | **Status**: Draft
**BR liên quan**: BR-154 (báo cáo sự cố chính thức, Medical Staff tạo để lưu hồ sơ/xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với "Record Corrective Actions Report Screen"**: nút riêng "Step 3" ở `26_IncidentDetail.md` nay gộp vào "Hoàn tất sự cố" cùng UC-168. Màn này là Khối B. Giữ nguyên UC-033. Xem Mục 10.7 review file.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Incident code | Text (đọc) | — | — | Report 3 §3.3.2.3.8 |
| 2 | Date created | Text (đọc) | — | — | |
| 3 | Severity level | Text/Badge (đọc) | — | BR-147 | Kế thừa từ `18_AssignIncidentSeverityDialog.md` |
| 4 | Affected student list, symptoms, ngày khởi phát, suspected dishes, ingredient batches (tự động điền) | Text (đọc) | Auto-populate | BR-154 | UC-033 bước 3 |
| 5 | Food sample retained (mã + trạng thái) | Text (đọc) | — | BR-114 | |
| 6 | Clinical notes | Textarea | Bắt buộc | BR-154 | UC-033 bước 4. **Cập nhật 11/07/2026**: pre-fill sẵn từ "Tường trình ban đầu" (`26_IncidentDetail.md` field 5) khi mở màn này lần đầu — Medical Staff vẫn sửa/bổ sung được, không khoá cứng |
| 7 | Laboratory sample test results | Textarea/File | Bắt buộc nếu có | BR-154 | |
| 8 | Root cause determination (Conclusion) | Textarea | Bắt buộc | BR-150, BR-154 | |
| 9 | Remedial measures (tóm tắt) | Text (đọc, kế thừa từ Investigation Workspace) | — | BR-152 | |
| 10 | Nút "Export & Submit Incident Reports" | Button (primary) | Disabled khi thiếu Clinical notes/Root cause | BR-154 | Sinh PDF, gửi School Admin |
| 11 | Nút "Come back" | Button | — | — | Quay lại Investigation Workspace |

### Trạng thái màn hình
- Loading: spinner khi hệ thống biên soạn PDF + gửi.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: MSG-046 (Toast), trạng thái sự cố chuyển "Investigation Completed", PDF được gửi tới School Administration (`Incident Dashboard`), quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nút "Step 3: Submit the incident report...") hoặc từ Investigation Workspace; thành công → chuyển tiếp School Administration Incident Dashboard (ngoài phạm vi actor Medical Staff, chỉ ghi nhận điểm điều hướng thoát).
**Bố cục**: Report 3 §3.3.2.3.8 — trang riêng full-screen, Incident Report Card tóm tắt trên cùng, System Info Text giải thích việc biên soạn PDF, nút "Export & Submit Incident Reports" cố định cuối màn, nút "Come back" trên AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); thành công → chuyển School Administration Incident Dashboard.
**Bố cục**: `Dialog`/trang con — Incident Report Card tóm tắt, form Clinical notes/Lab results/Root cause bên dưới, nút "Export & Submit Incident Reports" cuối form.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Generate Incident Report Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Incident code, Date created, Severity level, danh sách học sinh/triệu chứng/món
nghi vấn (auto-populate), Food sample retained, Clinical notes, Laboratory test results, Root cause,
Remedial measures, nút Export & Submit Incident Reports.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error, success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4401–4415 (UC-033), dòng 5845–5867 (§3.3.2.3.8).
- business_rules.md (BR-114, BR-150, BR-152, BR-154).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-046).

# Screen Spec — Record Process Violation (UC-178) — `(Deprecated 11/07/2026)`

> **TRẠNG THÁI: DEPRECATED.** UC-178 không còn là màn hình riêng biệt — dialog nhập liệu đã gộp vào `22_RecordStaffViolation.md` (dialog "Lập Biên bản Vi phạm" hợp nhất, mã UC sống sót = **UC-179**). Giữ nguyên số hiệu UC-178 (không tái sử dụng). Chi tiết: `21_RecordProcessViolation.md`, `review/medical_staff/review_screen_medical_staff.md` Mục 10.

# Screen Spec — Record Violation (Dialog hợp nhất — thay thế Record Process Violation UC-178 + Record Staff Violation UC-179)

**Actor**: Medical Staff (dùng chung với Service Caterers theo BR-183/184) | **UC liên quan**: UC-179 - Record Staff Violation (Report 3 §3.2, dòng 4639–4659) — **mã UC sống sót sau khi gộp UI với UC-178, quyết định 11/07/2026**. UC-178 (Report 3 §3.2, dòng 3663–3683) — **Deprecated 11/07/2026** | **Status**: Draft
**BR liên quan**: BR-183 (vi phạm quy trình/sản phẩm — kích hoạt khi field "Nhân viên liên quan" để trống), BR-184 (vi phạm nhân viên — kích hoạt khi có điền), BR-185, BR-186, BR-187 (cảnh báo nghi ngờ tái diễn 90 ngày), BR-188 (audit log bất biến), BR-190 (báo cáo lạm dụng manual override), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký — sửa 11/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error), MSG-086 (Success), MSG-088 (Warning)

> **Quyết định thiết kế 07/07/2026, chốt cuối 11/07/2026** (`review/medical_staff/review_screen_medical_staff.md` Mục 10): giữ tách BR-183/BR-184 ở tầng dữ liệu (nguồn gốc phát sinh khác nhau) nhưng **gộp UI thành 1 dialog duy nhất** — không có toggle chọn "loại vi phạm", BR-183 hay BR-184 được suy ra tự động từ việc có điền field "Nhân viên liên quan" (field 4) hay không.
> **Cập nhật 11/07/2026 — bỏ hẳn field nhân chứng**: hệ thống không track thông tin người chứng kiến dưới bất kỳ hình thức nào. PDF vẫn giữ cột chữ ký thứ 3 "Người chứng kiến" (khớp mẫu giấy gốc) nhưng là khoảng trống tĩnh, không gắn field dữ liệu.
> **Cập nhật 11/07/2026 — đổi tên tiêu đề PDF**: từ "BIÊN BẢN SỰ VIỆC" → **"BIÊN BẢN VI PHẠM"**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Địa điểm xảy ra sự việc (`locationOfIncident`) | Input text | Bắt buộc, không rỗng | BR-211 | Mẫu giấy: "Hôm nay, tại: ___" |
| 2 | Đại diện Y tế trường học (`reportedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản đăng nhập hiện tại | BR-183/184 | Mẫu giấy: "Đại diện P.VSATTP" |
| 3 | Đại diện Nhà thầu (`caterersRepresentativeId`) | Select (tài khoản Service Caterers đang hoạt động) | Bắt buộc | BR-211 | Mẫu giấy: "Đại diện nhà thầu"; liên kết tài khoản có thật |
| 4 | Nhân viên liên quan (`staffName`) | Input text tự do (không phải khóa ngoại) | Tùy chọn — để trống nếu không quy trách nhiệm cho 1 cá nhân cụ thể | BR-184 | Trống = chỉ áp BR-183; có giá trị = kích hoạt thêm BR-184/185/187 |
| 5 | Chức vụ nhân viên (`staffPosition`) | Input text tự do | Tùy chọn; chỉ hiện khi field 4 đã có ký tự đầu tiên | BR-184 | Không có trên mẫu giấy |
| 6 | Mức độ nghiêm trọng (`severityLevel`) | Select (LOW/MEDIUM/HIGH) | Bắt buộc | BR-183/184, BR-186 | Điều kiện tự động phân luồng nút ở `24_RecordViolationResolution.md` |
| 7 | Loại vi phạm (`violationType`, gồm `OVERRIDE_ABUSE`) | Select | Tùy chọn | BR-190 | 4 giá trị: "Bỏ qua bước quy trình bắt buộc", "Vi phạm vệ sinh cá nhân", "Lạm dụng quyền ghi đè hệ thống", "Hành động không được ủy quyền" |
| 8 | Thời điểm xảy ra sự việc (`occurredAt`) | DateTimePicker | Bắt buộc | BR-211 | Tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày]..." trong PDF |
| 9 | **Nội dung biên bản** (`violationContent`) | Rich text editor (toolbar rút gọn trên Mobile — đậm/nghiêng/gạch chân + gạch đầu dòng) | Bắt buộc, không rỗng | BR-183/184 | MSG-085 nếu thiếu |
| 10 | Ảnh bằng chứng | Camera + File input, nhiều ảnh | Bắt buộc ít nhất 1 ảnh | BR-183/184 | MSG-085 nếu thiếu |
| 11 | Nút "Save Violation" | Button (primary) | Disabled khi thiếu field bắt buộc (1, 3, 6, 8, 9, 10) | BR-183/184, BR-188 | Lưu dữ liệu số ngay — không chờ ảnh biên bản đã ký |
| 12 | Nút "Xuất Biên bản (PDF)" | Button (secondary) | Chỉ hiện sau khi đã Save Violation | BR-211 | Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**; 3 khoảng chữ ký (cột 3 để trống tĩnh) |
| 13 | Ảnh biên bản đã ký (`signedRecordPhotoUrl`) | Camera + File input | Không bắt buộc để Save; **bắt buộc** trước khi đóng/gửi duyệt ở `24_RecordViolationResolution.md` — gate chặn cứng | BR-211 | Tải lên sau khi in — ký tay — chụp ảnh |

### Cơ chế phân loại BR-183 vs BR-184 (không có toggle thủ công)
- Field 4 để trống → chỉ áp **BR-183** — field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` **không hiện**.
- Field 4 có giá trị → kích hoạt thêm **BR-184/185** và **BR-187** (best-effort).
- Field 3, 1/8/9/10 luôn bắt buộc bất kể field 4.

### Trạng thái màn hình
- **Loading**: spinner trên "Save Violation" khi lưu + rà soát trùng tên (khi field 4 có giá trị).
- **Empty**: không áp dụng.
- **Error**: MSG-004, MSG-005, MSG-085 tại field thiếu.
- **Success**: MSG-086 (SnackBar); trùng tên ≥ MEDIUM trong 90 ngày → MSG-088 (banner, không chặn lưu); điều hướng `24_RecordViolationResolution.md`. Nếu chưa tải `signedRecordPhotoUrl`, màn `24` chặn cứng nút đóng/gửi duyệt (MSG-095).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Report Violation" trên Dashboard (`03_Medical_Staff_Screen_Flow.md`), hoặc từ 1 `NonConformanceRecord` Không đạt; thành công → `24_RecordViolationResolution.md`.
**Bố cục**: trang riêng full-screen, camera (field 10) ưu tiên đầu form theo `Layout_Guidelines.md` mục 2.2, field 5 chỉ hiện khi field 4 có giá trị, nút "Save Violation" cố định cuối màn. **Lưu ý cần xác nhận thêm**: toolbar rich text đầy đủ không khả thi trên màn hình hẹp — rút gọn còn đậm/nghiêng/gạch chân + gạch đầu dòng, đội dự án/dev xác nhận trước khi code.

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm" (`08_Medical_Staff_Screen_Flow_Web.md`); thành công → `24_RecordViolationResolution.md`.
**Bố cục**: `Dialog` 1 cột — xem đầy đủ tại `Web/MedicalStaff/22_RecordStaffViolation.md`.

---

## Xuất & Lưu Biên bản đã ký (BR-211, sửa 11/07/2026)

> Mô tả đầy đủ tại `Web/MedicalStaff/22_RecordStaffViolation.md` — áp dụng chung cả 2 nền tảng. Trên Mobile: nút "Xuất Biên bản (PDF)" mở trình xem PDF/trình chia sẻ hệ điều hành; field "Ảnh biên bản đã ký" dùng camera trực tiếp. Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**, cột chữ ký thứ 3 "Người chứng kiến" để trống tĩnh.

## AI Generation Prompt ⭐
```
Tạo wireframe cho dialog "Lập Biên bản Vi phạm", nền tảng Mobile, actor Medical Staff.
Bố cục: trang riêng full-screen, camera ưu tiên đầu form, form dọc.
Thứ tự field: Địa điểm xảy ra sự việc, Đại diện Y tế trường học (hiển thị tự động, không sửa),
Đại diện Nhà thầu (Select) — KHÔNG có field Người chứng kiến trong form,
Nhân viên liên quan (tùy chọn) — khi có giá trị thì hiện thêm Chức vụ nhân viên,
Mức độ nghiêm trọng (Select LOW/MEDIUM/HIGH), Loại vi phạm (Select, tùy chọn),
Thời điểm xảy ra sự việc (DateTimePicker), khối "Nội dung biên bản" là rich text editor rút gọn,
phía trên hiện dòng tự sinh "Vào hồi [giờ] ngày [ngày]...", ảnh bằng chứng (camera, bắt buộc ít nhất 1),
nút Save Violation, sau khi lưu hiện thêm nút "Xuất Biên bản (PDF)" (tiêu đề "BIÊN BẢN VI PHẠM").
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM).
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-086/MSG-088).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm toggle chọn "loại vi phạm".
KHÔNG thêm field nhân chứng (witness) trong form nhập liệu.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4639–4659 (UC-179), dòng 3663–3683 (UC-178 — nay Deprecated).
- business_rules.md §18 (BR-183, BR-184, BR-185, BR-186, BR-187, BR-188, BR-190, BR-211).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-086, MSG-088, MSG-095).
- `02_Requirements/Violation_Management_Proposal.md`.
- `00_Policies/specialreport2173_1.md` (mẫu Biên bản Sự việc).
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md (nhánh "ViolForm").
- `review/medical_staff/review_screen_medical_staff.md` Mục 10.

---


# Screen Spec — Compliance Dashboard

**Actor**: Medical Staff (màn hình dùng chung với School Administration — xem `03_Design/05_ScreenSpecifications/Web|Mobile/SchoolAdministration/` cho bản School Administration; file này viết riêng theo góc nhìn Medical Staff theo `14_Screen_Authorization_Matrix.md` khối 3, dòng "Compliance Dashboard") | **UC liên quan**: UC-027 (Report 3 §3.2, dòng 3469–3483 — primary actor gốc là School Administration, Medical Staff dùng chung theo cột `x`) | **Status**: Draft
**BR liên quan**: BR-091 (nhiệt độ giữ ấm ≥60°C, giám sát trạng thái Step 3), BR-097 (tích hợp quản lý sự cố khi kiểm tra thất bại nghiêm trọng), BR-100 (điểm vệ sinh hàng ngày), BR-104 (điểm vệ sinh đầu bếp)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-093 (Warning — cảnh báo điểm checklist thấp)

> **Cập nhật 05/07/2026**: đội dự án quyết định **bỏ field "Cảnh báo Kitchen Quarantine"** (field #6 cũ). Field đó trích `BR-105` (ghost-citation, canonical là quy tắc lưu mẫu thực phẩm) và `MSG-024` (theo `error_message.md`, thuộc module Incident Management, dùng cho BR-149 — cách ly lô nguyên liệu khi có sự cố, không phải ngưỡng điểm checklist). Điểm checklist <60% (UC-014) **không còn khóa vận hành bếp** — chỉ tự động cảnh báo School Administration (xem `business_rules.md` §12, ghi chú sau BR-208). Đã thay bằng field #4b — Alert cảnh báo điểm thấp (không phải banner cố định).
>
> **Quyết định 12/07/2026 (rà soát ảnh chụp màn hình thực tế, Hình 6)**: bản Web (`compliance/page.tsx`) hiện có thêm 1 card "Nhiệt độ kho & tủ lưu mẫu (BR-089/BR-110)" — **card này KHÔNG có trong đặc tả màn hình gốc**, phát sinh ngoài phạm vi khi triển khai code, đồng thời còn dẫn BR-089 đã Deprecated. Quyết định: **KHÔNG đưa card này vào đặc tả** (áp dụng cho cả Web lẫn Mobile) — dữ liệu nhiệt độ tủ lưu mẫu (BR-110) đã có chỗ đúng phạm vi tại `15_ViewFoodSafetyReportsScreen.md`. Xem `review_screen_medical_staff.md` Mục 16.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trạng thái real-time 3 bước kiểm tra hôm nay (Step 1/2/3) | Widget trạng thái | — | BR-091 | UC-027 bước 2 |
| 2 | Điểm checklist vệ sinh hàng ngày do Service Caterers nộp | Widget số liệu | — | BR-100, BR-104 | UC-027 bước 3 |
| 3 | Ảnh xác minh (verification photos) | Ảnh preview | — | — | UC-027 bước 4 |
| 4 | Cảnh báo bước kiểm tra thất bại/quá hạn | Alert/Banner | Hiện khi có non-conformance (BR-097) | BR-097 | AF1 — điều hướng chi tiết non-conformance |
| 4b | Cảnh báo điểm checklist <60% *(thay thế field Quarantine cũ, 05/07/2026)* | Alert (warning, không phải banner cố định) | Hiện khi điểm gần nhất <60%; đã tự động gửi cảnh báo School Administration | — | Không khóa vận hành bếp, không chặn phân phát bữa ăn |
| 5 | Nút "Xem chi tiết cảnh báo" | Button | — | — | Điều hướng liên kết corrective action request liên quan |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` cho các widget số liệu real-time.
- Empty: nếu chưa có phiên sản xuất nào trong ngày → "Chưa có dữ liệu sản xuất hôm nay".
- Error: MSG-004, MSG-005; điểm <60% → Alert cảnh báo (không phải banner cố định, MSG-093).
- Success: không áp dụng (màn hình xem, hành động "sign off" thuộc về School Administration ở bản của họ).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard; điều hướng tiếp sang `16_ViewSchoolPolicyScreen.md`, `15_ViewFoodSafetyReportsScreen.md`.
**Bố cục**: 1 cột, các widget xếp dọc dạng Card (trạng thái 3 bước, điểm checklist, ảnh xác minh), Alert cảnh báo (không phải banner cố định) nếu có non-conformance/điểm thấp.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Content Area grid 12 cột: widget trạng thái 3 bước + điểm checklist chiếm hàng trên (`col-span-6` mỗi widget), ảnh xác minh + cảnh báo non-conformance/điểm thấp ở hàng dưới.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Dashboard, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: trạng thái 3 bước kiểm tra hôm nay, điểm checklist vệ sinh hàng ngày,
ảnh xác minh, cảnh báo bước kiểm tra thất bại/quá hạn, Alert cảnh báo điểm checklist <60%
(không phải banner khóa).
Áp dụng Design System: warning #B45309 cho các Alert cảnh báo.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3469–3483 (UC-027).
- business_rules.md (BR-091, BR-097, BR-100, BR-104; xem §12 ghi chú sau BR-208 về quyết định descope khóa bếp).
- error_message.md (MSG-004, MSG-005; MSG-093 §3 Warning; MSG-024 không áp dụng cho màn này — thuộc Incident Management).
- `14_Screen_Authorization_Matrix.md` khối 3 (dòng Compliance Dashboard, cột MS có `x`).

**Ghi chú màn hình dùng chung**: theo mục 5 của `00_Team_Work_Assignment_Plan.md`, đây là màn hình dùng chung School Administration + Medical Staff — bản School Administration (do Nam phụ trách) có thể có thêm hành động "sign off" (UC-027 bước 5) mà Medical Staff không có; phần field hiển thị dữ liệu (widget số liệu) sẽ được hợp nhất ở bước review (mục 6 của kế hoạch).

**Cập nhật 05/07/2026 — bổ sung US-09 (BR-209, UC-185)**: xem `28_SendComplianceReportDialog.md` — Medical Staff nay có thể chủ động soạn/gửi báo cáo tuân thủ ATTP (Daily/Weekly) cho School Administration, không cần chỉ dựa vào School Administration tự vào xem màn này.

---


# Screen Spec — Record Violation Resolution

**Actor**: Medical Staff (Primary Actor theo UC-180; màn hình dùng chung với School Administration — School Admin chỉ xem lại/duyệt qua UC-182, xem `Web/SchoolAdministration/46_RecordViolationResolution.md`) | **UC liên quan**: UC-180 - Record Violation Resolution (Report 3 §3.2, dòng 4661–4681 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo với bản School Administration, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-185 (biên bản xử lý sai phạm, `ViolationResolutionRecord`), BR-186 (phân cấp phê duyệt: LOW tự đóng, MEDIUM/HIGH chuyển School Administration), BR-188 (audit log bất biến), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký 3 bên — mới, 06/07/2026, xem ghi chú field 9)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error — thiếu field bắt buộc), MSG-086 (Success — kế thừa), MSG-087 (Success), MSG-089 (Success — chỉ áp dụng khi School Admin duyệt, xem bản của họ), MSG-095 (Error — chặn đóng/gửi duyệt khi chưa có ảnh biên bản đã ký)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Vi phạm gốc liên kết (process hoặc staff conduct) | Text hiển thị + link | — | BR-183, BR-184 | Hợp nhất từ bản School Administration; link sang chi tiết `27_ViolationRegistry.md` |
| 2 | Tóm tắt biên bản vi phạm (context, đọc) | Text | — | — | Kế thừa từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` |
| 3 | Hành động xử lý (actionTaken) | Textarea | Bắt buộc | BR-185 | MSG-085 nếu thiếu |
| 4 | Người xử lý (handledBy) | Text (auto, user hiện tại) | — | BR-185 | |
| 5 | Mức độ hình thức kỷ luật (disciplinaryLevel) | Select | Bắt buộc khi vi phạm nhân viên (UC-179) — Nhắc nhở/Cảnh cáo bằng văn bản/Đình chỉ tạm thời/Đề xuất chấm dứt hợp đồng | BR-185 | Không áp dụng cho vi phạm quy trình thuần túy (UC-178) |
| 6 | Trạng thái (Pending Approval/Approved/Closed) | Badge (warning/success) | — | BR-186 | Hợp nhất từ bản School Administration, khớp field `status` (`ViolationResolutionRecord`) |
| 7 | Nút "Đóng biên bản" (chỉ hiện khi mức LOW) | Button (primary) | Chỉ khả dụng khi severityLevel = LOW; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Medical Staff tự phê duyệt/đóng trực tiếp; tap khi disabled → toast MSG-095 |
| 8 | Nút "Gửi School Administration phê duyệt" (chỉ hiện khi MEDIUM/HIGH) | Button (primary) | Bắt buộc khi severityLevel ≥ MEDIUM; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Chuyển trạng thái "Pending Approval"; tap khi disabled → toast MSG-095 |
| 9 | Badge "Tình trạng biên bản đã ký" (dựa trên `signedRecordPhotoUrl` của biên bản gốc, BR-211) | Badge (danger/success) — "Chưa có ảnh ký — chưa thể đóng/gửi duyệt" / "Đã có ảnh ký xác nhận" | Hiển thị luôn để giải thích lý do nút 7/8 bị disable | BR-211 | Có nút phụ "Quay lại tải ảnh biên bản" điều hướng về `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` |

### Trạng thái màn hình
- Loading: spinner khi lưu biên bản xử lý.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; thiếu actionTaken/disciplinaryLevel bắt buộc → cảnh báo inline; **chưa có `signedRecordPhotoUrl` → nút 7/8 disabled, MSG-095 (Error) khi cố thao tác — chặn cứng theo BR-211 (quyết định người dùng 06/07/2026)**.
- Success: mức LOW → MSG-089 (đóng trực tiếp, áp dụng kỷ luật ngay); mức MEDIUM/HIGH → MSG-087 (nộp chờ phê duyệt), quay lại `27_ViolationRegistry.md`. Điều kiện tiên quyết cho cả 2 nhánh: đã có `signedRecordPhotoUrl`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` sau khi lập biên bản; thành công → `27_ViolationRegistry.md`.
**Bố cục**: trang riêng full-screen, tóm tắt biên bản trên cùng, form Hành động xử lý + Mức kỷ luật, 1 trong 2 nút (Đóng biên bản/Gửi phê duyệt) hiện tùy theo severityLevel, cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (Web) sau khi lập biên bản; thành công → `27_ViolationRegistry.md`.
**Bố cục**: `Dialog`/trang con — tóm tắt biên bản, form Hành động xử lý + Mức kỷ luật, nút hành động tương ứng cuối form.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Violation Resolution, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: link vi phạm gốc, tóm tắt biên bản vi phạm, Hành động xử lý, người xử lý, badge Trạng thái, Select mức kỷ luật
(Nhắc nhở/Cảnh cáo/Đình chỉ/Đề xuất chấm dứt), nút Đóng biên bản (LOW) hoặc Gửi phê duyệt (MEDIUM/HIGH), badge tình trạng biên bản đã ký.
Áp dụng Design System: success #15803D cho đóng trực tiếp, warning #B45309 cho chờ phê duyệt, danger #B91C1C (badge chưa có ảnh ký).
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-087/MSG-089 tùy nhánh), disabled state (MSG-095).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4661–4681 (Table 3-161, UC-180 Record Violation Resolution — Normal Flow đầy đủ).
- business_rules.md §18 (BR-185, BR-186, BR-188, BR-211 — mới 06/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-087, MSG-089, MSG-095 — mới 06/07/2026).
- `02_Requirements/Violation_Management_Proposal.md`.
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md (nhánh "ViolResolution").

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/46_RecordViolationResolution.md` — bổ sung field "Vi phạm gốc liên kết" (link) và "Trạng thái" (badge) còn thiếu so với bản School Administration; giữ nguyên 2 nút hành động riêng của Medical Staff (Đóng biên bản/Gửi phê duyệt) vì School Admin chỉ có nút "Chuyển sang duyệt" (xem qua UC-182).

# Screen Spec — Incident List

**Actor**: Medical Staff (màn hình dùng chung với School Administration — `14_Screen_Authorization_Matrix.md` khối 3, dòng "Incident List"; file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-114 (Report 3 §3.2, dòng 4555–4569 — primary actor gốc School Administration) | **Status**: Draft

> **Viết lại toàn bộ 12/07/2026** — bản trước mô tả model 2-tab cũ, đã lỗi thời so với thiết kế lại 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3) và code thật. Bản này viết lại theo model 3-tab, cộng nút "Xem chi tiết" (12/07/2026).
> **Cập nhật 15/07/2026 — mô hình 4 tab + biên bản sự cố thủ công**: bổ sung tab "Biên bản chờ xử lý", đổi "Xác nhận liên quan ATTP" → "Tạo báo cáo sự cố" (mở dialog soạn biên bản `33_CreateIncidentRecordDialog.md` thay vì tạo `INC-xxx` ngay — đảo ngược quyết định Mục 7.8), siết field "Xem chi tiết" khớp `Teacher/12_IncidentForm.md`. Xem `25_IncidentList.md` (bản đầy đủ, Web + Mobile) và Mục 10 review file.

**BR liên quan**: BR-146 (ngưỡng gộp tự động ≥3 HS/24h), BR-147 (severity), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-100 (Success), MSG-101 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Cấu trúc 4 tab (cập nhật 15/07/2026)

| Tab | Nội dung | Cột/Field | Filter | Hành động |
| --- | --- | --- | --- | --- |
| **Chờ tiếp nhận** | `RPT-xxx`, `status = PENDING_TRIAGE`/`DRAFTING` | Mã, Học sinh, Lớp, Triệu chứng, Bữa ăn/Món nghi vấn, Thời điểm báo cáo, Người báo cáo, chế độ "Chọn", nút "Xem chi tiết", badge "Đang soạn biên bản (nháp)" nếu `DRAFTING` | Theo lớp, loại triệu chứng, ngày báo cáo | Thanh hành động nổi khi ≥1 card chọn: **"Tạo báo cáo sự cố"** (mở `33_CreateIncidentRecordDialog.md`) / "Đóng — Không liên quan ATTP" (đúng 1) |
| **Biên bản chờ xử lý** 🆕 | Biên bản "Hoàn tất" (`RPT-xxx.status = DRAFTED`) | Mã RPT nguồn (chip), Nội dung rút gọn, Người lập, Thời điểm lập | Theo ngày lập | 2 nút/card: "Đóng — Không liên quan ATTP" / "Mở phiên điều tra" |
| **Đang xử lý** | `INC-xxx`, trạng thái 1–5 | Mã case & tiêu đề, Status Badge (5 nhãn), Triệu chứng, Số học sinh ảnh hưởng | Severity Level, Status (BR-147) | Chọn 1 card → `26_IncidentDetail.md` |
| **Đã đóng** | Cả `RPT-xxx` đóng trực tiếp lẫn `INC-xxx` đã đóng | + "Loại hồ sơ" (RPT/INC), "Lý do đóng" | — | Chọn 1 card `INC-xxx` → `26_IncidentDetail.md` (read-only) |
| Nút "Export" | — | — | — | Xuất CSV/PDF danh sách đang hiển thị |

### Cơ chế gộp N-1 (cập nhật 15/07/2026)
1. Bật chế độ "Chọn", tick 1+ card `RPT-xxx` → **"Tạo báo cáo sự cố"** → mở `33_CreateIncidentRecordDialog.md` (soạn biên bản, "Lưu bản nháp"/`DRAFTING` hoặc "Hoàn tất biên bản"/`DRAFTED`).
2. Biên bản "Hoàn tất" → tab "Biên bản chờ xử lý" → "Đóng — Không liên quan ATTP" hoặc "Mở phiên điều tra".
3. "Mở phiên điều tra" → tạo `INC-xxx` mới, `caseCode` gắn chung với biên bản, nội dung pre-fill "Tường trình ban đầu"; `RPT-xxx` → `MERGED`.
4. "Đóng — Không liên quan ATTP" trực tiếp (không qua biên bản, N=1): giữ nguyên như cũ.

### Nút "Xem chi tiết" — field khớp `Teacher/12_IncidentForm.md` (siết 15/07/2026)
Học sinh (multi), Lớp, Số học sinh ảnh hưởng, Món ăn nghi vấn, Ngày ăn, Triệu chứng (checkbox+Other), Mô tả, Ảnh đính kèm, Người báo cáo, Thời điểm báo cáo. "Mức độ nghiêm trọng": hiển thị "Chưa đánh giá" (Mục 7.2) cho tới khi Medical Staff đánh giá; `12_IncidentForm.md` chưa sửa theo quyết định này (ghi nhận riêng).

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho danh sách.
- Empty: "Không có báo cáo/sự cố nào" theo từng tab + illustration.
- Error: MSG-004, MSG-005, MSG-023.
- Success: MSG-100/MSG-101/Toast "Đã tạo phiên điều tra"/"Đã đóng báo cáo".

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Sự cố & Điều tra" trên Dashboard; "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md`; "Mở phiên điều tra"/chọn item (INC) → `26_IncidentDetail.md`.
**Bố cục**: AppBar, 4 Tab, tab "Chờ tiếp nhận" dùng `Card` + chế độ "Chọn"; tab "Biên bản chờ xử lý" dùng `Card` với 2 nút; thanh hành động nổi; pull-to-refresh bắt buộc.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Sự cố & Điều tra" trên Dashboard (Web); "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md` (Web); "Mở phiên điều tra"/chọn dòng (INC) → `26_IncidentDetail.md` (Web).
**Bố cục**: Page Header 4 Tab + filter, tab "Chờ tiếp nhận" dùng `Table` + thanh hành động nổi; tab "Biên bản chờ xử lý" dùng `Table` với 2 nút cuối dòng; 2 tab còn lại Table thông thường. Nút "Export" góc trên phải.

---

## Nguồn dữ liệu
- Report 3.md, dòng 4555–4569 (UC-114), dòng 5758–5782 (§3.3.2.3.4).
- business_rules.md (BR-146, BR-147, BR-154, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-100, MSG-101).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3, §9.13, Mục 10 (15/07/2026) — bản đầy đủ, Web + Mobile ở `25_IncidentList.md`.

**Ghi chú màn hình dùng chung**: xem `Web/SchoolAdministration/09_IncidentList.md` cho góc nhìn School Administration — tab "Chờ tiếp nhận"/"Biên bản chờ xử lý" là đặc thù riêng của Medical Staff.

# Screen Spec — Incident Detail

**Actor**: Medical Staff (màn hình dùng chung với School Administration + Teacher — `14_Screen_Authorization_Matrix.md` khối 3, dòng "Incident Detail"; file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-116, gộp trình bày UC-009 (Report 3 §3.2, dòng 4577–4591 — primary actor gốc School Administration) | **Status**: Draft
**BR liên quan**: BR-146–155 (toàn bộ vòng đời quản lý sự cố)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 15/07/2026 — 3 thay đổi lớn** (Mục 10.4/10.5/10.7 review file): (1) gộp "Đánh giá mức độ nghiêm trọng" (trước là dialog riêng `18_AssignIncidentSeverityDialog.md`, nay Deprecated) vào thẳng màn này; (2) "Chọn món ăn nghi vấn" mặc định lọc 48 giờ (Quyết định 39/2006/QĐ-BYT) + nút "Mở rộng phạm vi ngày"; (3) gộp "Validate CAPA" (UC-168) + "Generate Incident Report" (UC-033) thành 1 điểm vào "Hoàn tất sự cố".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Case code | Text (đọc) | — | — | Report 3 §3.3.2.3.5 |
| 2 | Clinical symptoms | Text (đọc) | — | — | |
| 3 | Affected students (số lượng, lớp, tên) | Text (đọc) | — | BR-146 | |
| 4 | Suspected food item + meal time | Text (đọc) | — | — | |
| 5 | Severity level | `RadioGroup`, bắt buộc | Bắt buộc chọn Low/Medium/High | BR-147 | **Gộp vào màn 15/07/2026** — trước là dialog riêng `18_AssignIncidentSeverityDialog.md` (nay Deprecated) |
| 6 | Đánh giá rủi ro lâm sàng | Textarea | Bắt buộc khi Medium/High | BR-147 | Gộp từ UC-009 |
| 7 | Tường trình ban đầu | Textarea, bắt buộc trước "Lưu đánh giá & Bắt đầu điều tra" | Bắt buộc | — | **Pre-fill từ biên bản sự cố** (`33_CreateIncidentRecordDialog.md`, mới 15/07/2026). Pre-fill sang "Clinical notes" (UC-033) |
| 8 | Chọn món ăn nghi vấn | `Select` multi, bắt buộc | Từ Menu của "Ngày ăn" | BR-149 | **Mặc định lọc 48 giờ** (Quyết định 39/2006/QĐ-BYT) + nút "Mở rộng phạm vi ngày". Chọn xong → Quarantine ngay |
| 9 | Nút "Lưu đánh giá & Bắt đầu điều tra" | Button (primary) | Disabled khi thiếu field 5/7/8 | BR-147, BR-148, BR-149 | **Gộp 15/07/2026**, thay 2 hành động tách rời |
| 10 | Bản đồ truy xuất nguồn gốc (nếu có) | Panel/link | Biên soạn trong 4 giờ | BR-151 | Hợp nhất từ bản School Administration (mục 6, review 2026-07-01) |
| 11 | Danh sách lô hàng bị Quarantine | Danh sách rút gọn | — | BR-149 | Hợp nhất từ bản School Administration |
| 12 | Nút "Step 2: Incident Investigation Space" | Button | Chỉ Medical Staff | BR-150 | Điều hướng `17_InvestigationWorkspace.md` |
| 13 | Nút "Hoàn tất sự cố" | Button | Hiện khi CAPA đã duyệt (E17) | BR-152–155 | **Gộp 15/07/2026** — thay "Validate CAPA" (UC-168) + "Generate Incident Report" (UC-033) |
| 14 | Nút "Export" | Button | — | — | AF1 — xuất CSV/PDF |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải chi tiết sự cố.
- Empty: không áp dụng (luôn có 1 sự cố cụ thể).
- Error: MSG-004, MSG-005, MSG-023.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Card trên `25_IncidentList.md`; nút Step 2/"Hoàn tất sự cố" điều hướng sang màn con.
**Bố cục**: Report 3 §3.3.2.3.5 — AppBar, Section Header, block thông tin xếp dọc (severity + tường trình + chọn món ăn), nút "Lưu đánh giá & Bắt đầu điều tra", Step 2 và "Hoàn tất sự cố" xếp dọc bên dưới, nút "Export" nổi góc trên phải.

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `25_IncidentList.md` (Web).
**Bố cục**: 2 cột — thông tin sự cố + severity + tường trình + chọn món ăn bên trái, action list (Lưu đánh giá & Bắt đầu điều tra, Step 2, Hoàn tất sự cố, Export) bên phải.

---

## Nguồn dữ liệu
- Report 3.md, dòng 4577–4591 (UC-116), dòng 4335–4349 (UC-009).
- business_rules.md (BR-146–155, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`.
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10 (15/07/2026) — bản đầy đủ, Web + Mobile ở `26_IncidentDetail.md`.

# Screen Spec — Create Incident Record Dialog 🆕

**Actor**: Medical Staff | **UC liên quan**: UC-114 (mở rộng, bổ sung 15/07/2026) | **Status**: Draft
**BR liên quan**: BR-146, BR-154 | **MSG liên quan**: MSG-023 (tái sử dụng), MSG-100/MSG-101 (Success)

Dialog soạn "biên bản sự cố" thủ công, theo cấu trúc `00_Policies/specialreport2173_1.md`. Điểm vào: tick chọn `RPT-xxx` ở tab "Chờ tiếp nhận" → "Tạo báo cáo sự cố". Field chính: khối context union (đọc), Textarea "Nội dung biên bản" (bắt buộc, pre-fill gợi ý), 3 nút: "Hủy" / **"Lưu bản nháp"** (`status = DRAFTING`, giữ ở "Chờ tiếp nhận") / **"Hoàn tất biên bản"** (`status = DRAFTED`, chuyển tab "Biên bản chờ xử lý"). Đảo ngược quyết định Mục 7.8.

Nguồn dữ liệu: bản đầy đủ, Web + Mobile ở `33_CreateIncidentRecordDialog.md`.

# Screen Spec — Violation Registry

**Actor**: Medical Staff (màn hình dùng chung với School Administration — Primary Actor theo UC-181 là School Administration, Medical Staff là Secondary Actor; file này viết theo góc nhìn Medical Staff, quyền hạn xem có thể hẹp hơn bản School Administration) | **UC liên quan**: UC-181 - View Violation Registry (Report 3 §3.2, dòng 4683–4703 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-189 (danh sách tổng hợp mọi loại vi phạm — quy trình, nhân viên, đối tác — theo actor/mức độ/trạng thái), BR-187 (cảnh báo nghi ngờ tái diễn), BR-214 (báo cáo tổng hợp vi phạm theo kỳ), BR-178 (định dạng xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 13/07/2026**: bổ sung field "Filter theo khoảng ngày" và "Nút Export" — khoảng trống phát hiện khi thiết kế BR-214 (UC-189). Xem `review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo loại vi phạm (quy trình/nhân viên/đối tác) | Select/Chip filter | — | BR-189 | |
| 2 | Filter theo mức độ (LOW/MEDIUM/HIGH) | Select | — | BR-186 | |
| 3 | Filter theo trạng thái (Pending/Approved/Closed) | Select | — | BR-186 | |
| 4 | Filter theo khoảng ngày | Date range picker | — | — | **Mới 13/07/2026** |
| 5 | Danh sách vi phạm | List/Table | — | BR-189 | Cột: Loại vi phạm, người/đối tượng liên quan, mức độ, trạng thái, ngày lập |
| 6 | Status/Severity Badge (mỗi item) | Badge màu | Danger (HIGH)/Warning (MEDIUM)/Secondary (LOW) | BR-186 | |
| 7 | Cảnh báo nghi ngờ tái diễn (nếu có) | Badge/Icon cảnh báo | — | BR-187 | MSG-088 |
| 8 | Chọn 1 vi phạm → xem chi tiết | Interaction | — | — | Điều hướng lại `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md`/`24_RecordViolationResolution.md` tương ứng (chế độ xem) |
| 9 | Nút "Export" | Button | — | BR-178 | **Mới 13/07/2026** — xuất CSV/PDF đúng theo filter đang áp dụng |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho danh sách.
- Empty: "Chưa có vi phạm nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: Danh sách hiển thị đầy đủ vi phạm trong phạm vi Medical Staff được xem, cảnh báo tái diễn (MSG-088) hiện dưới dạng badge/icon cạnh item liên quan.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Dashboard (nếu có shortcut riêng, hoặc từ menu điều hướng chung theo `03_Medical_Staff_Screen_Flow.md`); chọn item → xem chi tiết vi phạm tương ứng.
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` (loại/mức độ/trạng thái/khoảng ngày) phía trên, danh sách `Card` cuộn dọc, mỗi Card có Severity Badge + cảnh báo tái diễn (nếu có) góc phải, nút "Export" nổi AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm" (`08_Medical_Staff_Screen_Flow_Web.md`); chọn dòng → xem chi tiết.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — thanh filter (loại/mức độ/trạng thái/khoảng ngày) phía trên `Table`, nút "Export" góc trên phải, `Table` hiển thị danh sách kèm phân trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Violation Registry, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: filter (loại vi phạm, mức độ, trạng thái, khoảng ngày), danh sách vi phạm với
Severity Badge, cảnh báo nghi ngờ tái diễn (nếu có), nút Export.
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM), secondary #64748B (LOW).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- business_rules.md §18 (BR-186, BR-187, BR-189), §16 (BR-178, BR-214 — mới 13/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-088).
- `02_Requirements/Violation_Management_Proposal.md`.

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/47_ViolationRegistry.md` — field hiển thị dữ liệu khớp nhau hợp lý, không phát hiện field còn thiếu. **[Giả định — cần team xác nhận]**: BR-189 không nêu rõ ràng buộc phạm vi dữ liệu Medical Staff được xem trong Registry (toàn bộ hay chỉ vi phạm tự lập) — cần team xác nhận trước khi sang bước `06_Wireframes/`.

---

# Screen Spec — ATTP Report Center

**Actor**: Medical Staff | **UC liên quan**: UC-187 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-175 (phân quyền xem báo cáo theo vai trò)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Bổ sung 13/07/2026**: màn hub mới, gom 1 điểm vào chung cho 3 loại báo cáo ATTP Medical Staff có thể chủ động gửi School Administration. Xem `29_ATTPReportCenter.md` để biết đầy đủ bối cảnh, `review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card "Báo cáo Giám sát tuân thủ" | Card hành động | — | BR-209 | Nút "Soạn & Gửi" → `28_SendComplianceReportDialog.md` (UC-185) |
| 2 | Card "Báo cáo Tổng hợp Sự cố" | Card hành động | — | BR-213 | Nút "Soạn & Gửi" → `30_SendIncidentSummaryDialog.md` (UC-188) |
| 3 | Card "Báo cáo Tổng hợp Vi phạm" | Card hành động | — | BR-214 | Nút "Soạn & Gửi" → `31_SendViolationSummaryDialog.md` (UC-189) |
| 4 | Lần gửi gần nhất (mỗi card) | Text nhỏ (đọc) | "Chưa gửi lần nào" nếu chưa có | BR-177 | |
| 5 | Nút "Xem lịch sử báo cáo đã gửi" | Button (link) | — | BR-177 | → `32_SentReportsHistory.md` (UC-190) |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho 3 card.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn điều hướng).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu điều hướng chung (Mobile) "Báo cáo ATTP".
**Bố cục**: 1 cột, 3 Card xếp dọc, nút "Xem lịch sử báo cáo đã gửi" cuối danh sách.

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu Sidebar "Báo cáo ATTP"; mỗi card dẫn tới dialog soạn báo cáo tương ứng.
**Bố cục**: Page Header đơn giản, 3 Card xếp ngang, nút "Xem lịch sử báo cáo đã gửi" góc trên phải.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình ATTP Report Center, nền tảng Mobile, actor Medical Staff.
Bố cục: 1 cột, 3 Card hành động xếp dọc (Giám sát tuân thủ / Tổng hợp Sự cố / Tổng hợp Vi phạm),
mỗi card có mô tả ngắn + lần gửi gần nhất + nút "Soạn & Gửi", nút "Xem lịch sử báo cáo đã gửi".
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-175, BR-213, BR-214), §12 (BR-209).
- `traceability_matrix.md` (UC-187, mới 13/07/2026).
- `29_ATTPReportCenter.md` (bản đầy đủ, Web + Mobile).

---

# Screen Spec — Send Incident Summary Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-188 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-213, BR-147, BR-181, BR-178, BR-158/159, BR-170
**MSG liên quan**: MSG-004, MSG-005, MSG-096 (Error), MSG-098 (Success)

> **Bổ sung 13/07/2026**: hiện thực hóa BR-213 — báo cáo **rollup số liệu** sự cố theo kỳ, KHÔNG lặp lại nội dung UC-033 (báo cáo chi tiết từng sự cố). Xem `30_SendIncidentSummaryDialog.md` để biết đầy đủ bối cảnh.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-213 | Mặc định: Daily |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-213 | |
| 3 | Tổng số sự cố trong kỳ theo trạng thái | Card số liệu (đọc) | Chờ tiếp nhận / Đang xử lý / Đã đóng | BR-213 | |
| 4 | Phân bố theo mức độ nghiêm trọng | Card/biểu đồ nhỏ (đọc) | Chỉ tính sự cố đã qua Step 1 | BR-147 | |
| 5 | Danh sách mã sự cố trong kỳ | Card rút gọn cuộn dọc (đọc) | Mã, Trạng thái, Mức độ, nút "Xem chi tiết" | BR-213 | Không hiện lại clinical notes/root cause/corrective action |
| 6 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-213 | |
| 7 | Nhãn "Confidential" | Badge tĩnh | Luôn hiện | BR-181 | |
| 8 | Nút "Gửi báo cáo" | Button (primary) | Disabled nếu không có sự cố nào trong kỳ (MSG-096) | BR-213 | |
| 9 | Nút "Hủy" | Button | — | — | |

### Trạng thái màn hình
- Loading: spinner khi tổng hợp/gửi.
- Empty: MSG-096, nút Gửi disabled.
- Error: MSG-004, MSG-005, MSG-096.
- Success: MSG-098 (Toast), ghi audit log (BR-170).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Card "Báo cáo Tổng hợp Sự cố" trên `29_ATTPReportCenter.md` (Mobile).
**Bố cục**: Trang riêng full-screen — chọn kỳ, card số liệu xếp dọc, danh sách Card mã sự cố, Textarea ghi chú, nút Gửi cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ Card "Báo cáo Tổng hợp Sự cố" trên `29_ATTPReportCenter.md` (Web).
**Bố cục**: `Dialog` modal — chọn kỳ, card số liệu, Table danh sách mã sự cố, Textarea ghi chú, nhãn Confidential, 2 nút cuối dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Incident Summary Dialog, nền tảng Mobile, actor Medical Staff.
Bố cục: Trang full-screen — chọn kỳ, card tổng số sự cố theo trạng thái, card phân bố mức độ, danh
sách Card mã sự cố (KHÔNG hiện nội dung điều tra), textarea ghi chú, nhãn Confidential, nút Gửi cố
định cuối màn.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty (MSG-096), success (MSG-098).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-213), §11 (BR-147, BR-154).
- `30_SendIncidentSummaryDialog.md` (bản đầy đủ, Web + Mobile).

---

# Screen Spec — Send Violation Summary Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-189 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-214, BR-186, BR-189, BR-211, BR-178, BR-158/159, BR-170
**MSG liên quan**: MSG-004, MSG-005, MSG-097 (Error), MSG-099 (Success)

> **Bổ sung 13/07/2026**: hiện thực hóa BR-214 — báo cáo **rollup số liệu** vi phạm theo kỳ, KHÔNG lặp lại nội dung biên bản đã ký (BR-211). Xem `31_SendViolationSummaryDialog.md` để biết đầy đủ bối cảnh.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-214 | Mặc định: Daily |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-214 | |
| 3 | Tổng số vi phạm trong kỳ theo loại | Card số liệu (đọc) | Quy trình/Nhân viên/Đối tác | BR-214 | |
| 4 | Phân bố theo mức độ | Card/biểu đồ nhỏ (đọc) | LOW/MEDIUM/HIGH | BR-214 | |
| 5 | Danh sách vi phạm trong kỳ | Card rút gọn cuộn dọc (đọc) | Loại, đối tượng, mức độ, trạng thái, nút "Xem biên bản" | BR-214 | Không hiện lại toàn văn biên bản |
| 6 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-214 | |
| 7 | Nút "Gửi báo cáo" | Button (primary) | Disabled nếu không có vi phạm nào trong kỳ (MSG-097) | BR-214 | |
| 8 | Nút "Hủy" | Button | — | — | |

### Trạng thái màn hình
- Loading: spinner khi tổng hợp/gửi.
- Empty: MSG-097, nút Gửi disabled.
- Error: MSG-004, MSG-005, MSG-097.
- Success: MSG-099 (Toast), ghi audit log (BR-170).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Card "Báo cáo Tổng hợp Vi phạm" trên `29_ATTPReportCenter.md` (Mobile).
**Bố cục**: Trang riêng full-screen — chọn kỳ, card số liệu xếp dọc, danh sách Card vi phạm, Textarea ghi chú, nút Gửi cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ Card "Báo cáo Tổng hợp Vi phạm" trên `29_ATTPReportCenter.md` (Web).
**Bố cục**: `Dialog` modal — chọn kỳ, card số liệu, Table danh sách vi phạm, Textarea ghi chú, 2 nút cuối dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Violation Summary Dialog, nền tảng Mobile, actor Medical Staff.
Bố cục: Trang full-screen — chọn kỳ, card tổng số vi phạm theo loại, card phân bố mức độ, danh sách
Card vi phạm (KHÔNG hiện toàn văn biên bản), textarea ghi chú, nút Gửi cố định cuối màn.
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM), secondary #64748B (LOW).
Trạng thái cần thể hiện: loading, empty (MSG-097), success (MSG-099).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-214), §18 (BR-186, BR-189, BR-211).
- `31_SendViolationSummaryDialog.md` (bản đầy đủ, Web + Mobile).

---

# Screen Spec — Sent Reports History

**Actor**: Medical Staff | **UC liên quan**: UC-190 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-177 (lưu trữ lịch sử báo cáo), BR-175, BR-178
**MSG liên quan**: MSG-004, MSG-005

> **Bổ sung 13/07/2026**: hiện thực hóa BR-177 (đã có sẵn) ở phạm vi Medical Staff — xem lại 3 loại báo cáo đã gửi (UC-185/188/189). Thiết kế theo mẫu UC-107 View Batch History, KHÔNG dựa Audit Logs (Medical Staff chưa có quyền). Xem `32_SentReportsHistory.md` để biết đầy đủ bối cảnh.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Lịch sử báo cáo đã gửi" | — | |
| 2 | Filter theo Loại báo cáo | Select/Chip | Giám sát tuân thủ / Tổng hợp Sự cố / Tổng hợp Vi phạm / Tất cả | — | |
| 3 | Filter theo khoảng ngày gửi | Date range picker | — | — | |
| 4 | Danh sách báo cáo đã gửi | Card cuộn dọc (đọc-only) | Loại báo cáo, Kỳ, Khoảng thời gian dữ liệu, Ngày giờ gửi, Người gửi | BR-177, BR-182 | |
| 5 | Nút "Tải lại PDF" (mỗi Card) | Button/Icon | — | BR-178 | |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton`.
- Empty: "Chưa có báo cáo nào được gửi".
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Xem lịch sử báo cáo đã gửi" trên `29_ATTPReportCenter.md` (Mobile).
**Bố cục**: 1 cột, filter Chip/DropdownButton, danh sách Card cuộn dọc.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Xem lịch sử báo cáo đã gửi" trên `29_ATTPReportCenter.md`.
**Bố cục**: filter bar phía trên `Table`, phân trang dưới bảng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Sent Reports History, nền tảng Mobile, actor Medical Staff.
Bố cục: filter Chip/DropdownButton (Loại báo cáo, khoảng ngày gửi) phía trên, danh sách Card cuộn dọc.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-177, BR-175, BR-178, BR-182).
- `32_SentReportsHistory.md` (bản đầy đủ, Web + Mobile).

---

# Screen Spec — Send Compliance Report Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-185 (mới, bổ sung 05/07/2026 — không có trong Report 3.md gốc, chính thức hóa US-09) | **Status**: Draft
**BR liên quan**: BR-209 (gửi báo cáo tuân thủ ATTP định kỳ), BR-104 (điểm vệ sinh đầu bếp), BR-102 (chứng nhận kiểm soát côn trùng), BR-103 (khám sức khỏe nhân viên bếp), BR-178 (xuất báo cáo PDF/Excel), BR-158/BR-159 (thông báo trong hệ thống), BR-170 (audit log)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-092 (Error — không có dữ liệu trong kỳ), MSG-091 (Success — gửi thành công)

> **Bổ sung 05/07/2026**: màn hình này hiện thực hóa **US-09** (`MedicalStaff_UserStory.md`) sau khi đội dự án phát hiện Compliance Dashboard (UC-027) — cơ chế hiện có duy nhất để School Administration nắm tình hình ATTP — vận hành theo mô hình **pull** (School Administration tự vào xem) và **chưa có route triển khai thực tế** phía frontend School Administration (xem `review_screen_medical_staff.md` mục 3.9). Màn hình này bổ sung kênh **push** song song, không thay thế UC-027. Không có trong `Report 3.md` gốc — mọi field đều tái sử dụng dữ liệu/cơ chế đã có (KitchenInspection từ UC-014, export BR-178, thông báo BR-158/159), không mở rộng phạm vi model dữ liệu.
>
> **Sửa 12/07/2026**: field 5 đổi từ badge "Còn hiệu lực/Hết hạn" sang tóm tắt bản ghi nhật ký kiểm soát côn trùng gần nhất — đồng bộ quyết định đổi model "nhật ký sự kiện" cho BR-102 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 1). Xem `review_screen_medical_staff.md` Mục 14.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-209 | Mặc định: Daily |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-209 | Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật) |
| 3 | Điểm vệ sinh trung bình trong kỳ | Card số liệu (đọc) | Tính từ các `KitchenInspection` trong kỳ | BR-104 | |
| 4 | Danh sách phiên kiểm tra Failed trong kỳ (nếu có) | List rút gọn (đọc) | Hiện mã phiên, ngày, lý do | BR-100, BR-104 | Trống nếu không có phiên Failed |
| 5 | Kiểm soát côn trùng — bản ghi gần nhất trong kỳ | Card tóm tắt (đọc) | Ngày xử lý gần nhất, Loại côn trùng phát hiện (nếu có), Ngày hẹn kiểm tra tiếp theo | BR-102 | **Sửa 12/07/2026** — thay cho badge "Còn hiệu lực/Hết hạn" |
| 6 | Tình trạng khám sức khỏe nhân viên bếp | Text/Badge (đọc) | Đạt / Có nhân viên quá hạn | BR-103 | |
| 7 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-209 | Medical Staff tự nhập nhận định/khuyến nghị |
| 8 | Nút "Gửi báo cáo" (Send Report) | Button (primary) | Disabled nếu không có phiên kiểm tra nào trong kỳ (MSG-092) | BR-209 | Xuất PDF/Excel (BR-178) + gửi thông báo (BR-158/159) |
| 9 | Nút "Hủy" (Cancel) | Button | — | — | Đóng màn, không lưu |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tổng hợp dữ liệu kỳ đã chọn và khi đang gửi.
- Empty: không có phiên kiểm tra nào trong kỳ đã chọn → chặn gửi, hiện MSG-092, nút "Gửi báo cáo" disabled.
- Error: MSG-004, MSG-005 (lỗi hệ thống chung), MSG-092.
- Success: MSG-091 (Toast), đóng màn, ghi audit log (BR-170), lưu vào lịch sử báo cáo (BR-177, xem lại tại `32_SentReportsHistory.md`).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Gửi báo cáo tuân thủ" trên `23_ComplianceDashboard.md` (Mobile) **hoặc** từ Card "Báo cáo Giám sát tuân thủ" trên `29_ATTPReportCenter.md` (Mobile, mới 13/07/2026 — UC-187); thành công → đóng màn, quay lại màn nguồn tương ứng.
**Bố cục**: Trang riêng full-screen (bottom sheet hoặc trang con) — chọn kỳ báo cáo trên cùng, các card số liệu/trạng thái xếp dọc, Textarea ghi chú, nút "Gửi báo cáo" cố định cuối màn, nút "Hủy" trên AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Gửi báo cáo tuân thủ" trên `23_ComplianceDashboard.md` (Web) **hoặc** từ Card "Báo cáo Giám sát tuân thủ" trên `29_ATTPReportCenter.md` (Web, mới 13/07/2026 — UC-187); thành công → đóng dialog, quay lại màn nguồn tương ứng.
**Bố cục**: `Dialog` modal — chọn kỳ báo cáo trên cùng, các card số liệu/trạng thái xếp lưới 2 cột giữa dialog, Textarea ghi chú bên dưới, 2 nút "Gửi báo cáo"/"Hủy" cố định cuối dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Compliance Report Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: chọn kỳ Daily/Weekly, card điểm vệ sinh trung bình, danh sách phiên Failed,
card tóm tắt kiểm soát côn trùng (bản ghi gần nhất: ngày xử lý, loại côn trùng nếu có, ngày hẹn tiếp theo),
trạng thái khám sức khỏe nhân viên, textarea ghi chú, nút Gửi báo cáo (primary) + nút Hủy.
Áp dụng Design System: primary #0E7490, disabled state khi không có dữ liệu trong kỳ.
Trạng thái cần thể hiện: loading, empty (MSG-092), success (MSG-091).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §12 (BR-209, ngay sau BR-208).
- `MedicalStaff_UserStory.md` (US-09, [DECIDED — duyệt 05/07/2026]).
- `traceability_matrix.md` (UC-185, hàng BR-209).
- `error_message.md` §1 (MSG-092), §2 (MSG-091).
- `23_ComplianceDashboard.md` (điểm vào từ nút "Gửi báo cáo tuân thủ").
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 1 (nhật ký kiểm soát côn trùng).
                                               