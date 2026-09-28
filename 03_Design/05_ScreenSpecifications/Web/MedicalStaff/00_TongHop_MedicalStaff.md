# Tổng hợp Screen Specifications — MedicalStaff (MedicalStaff)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---

# Screen Spec — Medical Staff Dashboard

**Actor**: Medical Staff | **UC liên quan**: — (entry point, không có UC riêng) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-066 (danh mục chế độ ăn đặc biệt), BR-146 (tự động kích hoạt điều tra sự cố), BR-100/101/104 (điểm số vệ sinh)
**MSG liên quan**: không áp dụng

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | — | — | Report 3 §3.3.2.3.1 (suy luận layout Web từ field Mobile) |
| 2 | Welcome Banner | Card | — | — | Tên người dùng, phòng làm việc, ngày hiện tại |
| 3 | Card "Allergy pending approval" | Thống kê (số) | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 4 | Card "Meal order needs approval" | Thống kê (số) | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 5 | Card "The issue is being resolved" | Thống kê (số) | — | BR-146 | Điều hướng sang `25_IncidentList.md` |
| 6 | Card "Compliance Points" | Thống kê (%) | — | BR-100, BR-101, BR-104 | Điều hướng sang `23_ComplianceDashboard.md` |
| 7 | Shortcut "Health Profile" | Card/Button | — | — | Điều hướng sang `03_StudentHealthRecordList.md` |
| 8 | Shortcut "Student Allergy Review" | Card/Button | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 9 | Shortcut "Approve Special Meal" | Card/Button | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 10 | Shortcut "City Safety Incident" | Card/Button | — | — | Điều hướng sang `25_IncidentList.md` |
| 11 | Shortcut "Food Compliance & Monitoring Board" | Card/Button | — | — | Điều hướng sang `23_ComplianceDashboard.md` |
| 12 | Shortcut "View food inspection results in 3 steps" | Card/Button | — | BR-079 | Điều hướng sang `11_FoodInspectionRecords.md` |

### Trạng thái màn hình
- Loading: `Skeleton` cho 4 card thống kê khi tải số liệu.
- Empty: giá trị 0 hiển thị bình thường.
- Error: card không tải được → icon lỗi nhỏ + nút "Thử lại" trong card đó.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào ngay sau Login thành công (role Medical Staff); là trang gốc sau khi chọn menu Sidebar (nếu có) hoặc mặc định khi đăng nhập.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 (Dashboard Shell) — Sidebar 5 nhóm theo `08_Medical_Staff_Screen_Flow_Web.md`, Content Area dùng grid 12 cột: 4 Card thống kê chiếm `col-span-3` mỗi card trên 1 hàng, 6 shortcut card bên dưới dạng grid `col-span-4` (3 cột × 2 hàng).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Medical Staff Dashboard, nền tảng Web, actor Medical Staff.
Bố cục: Sidebar 5 nhóm + Topbar, Content Area grid 12 cột: hàng 1 gồm 4 Card thống kê
(Allergy pending approval, Meal order needs approval, Issue being resolved, Compliance Points),
hàng 2-3 gồm 6 shortcut card (Health Profile, Student Allergy Review, Approve Special Meal,
City Safety Incident, Food Compliance & Monitoring Board, View food inspection results in 3 steps).
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading (Skeleton trên card số liệu).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5683–5706 (§3.3.2.3.1, field gốc Mobile — dùng chung business logic).
- business_rules.md (BR-031, BR-066, BR-100, BR-101, BR-104, BR-138, BR-146).
- 03_Design/04_ScreenFlow/08_Medical_Staff_Screen_Flow_Web.md.

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

# Screen Spec — Student Health Record Detail

**Actor**: Medical Staff | **UC liên quan**: — (không có UC riêng trong Matrix; nội dung suy ra từ UC-051 bước 4 — Report 3 §3.2 dòng 2021–2039) | **Status**: Draft
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
| 9 | Nút "Health History" | Button | — | UC-146 | Điều hướng `06_HealthHistoryScreen.md` |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải chi tiết hồ sơ.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `03_StudentHealthRecordList.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — trang con 2 cột (thông tin cơ bản + tình trạng y tế bên trái `col-span-8`, tài liệu đính kèm bên phải `col-span-4`), 2 nút "Update Record"/"Health History" ở góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Card trên `03_StudentHealthRecordList.md` (Mobile).
**Bố cục**: trang riêng full-screen, AppBar Back/Home, các block thông tin xếp dọc, 2 nút "Update Record"/"Health History" cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Student Health Record Detail, nền tảng Web, actor Medical Staff.
Bố cục: 2 cột — thông tin cơ bản/tình trạng y tế bên trái, tài liệu đính kèm bên phải,
nút Update Record + Health History ở page header.
Áp dụng Design System: danger #B91C1C cho Allergy Severe.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2021–2039 (UC-051, mô tả clinical file trong Normal Flow bước 4).
- business_rules.md (BR-031, BR-172); `00_Foundation/Design_System.md` mục 3 (quy ước màu đỏ cho "Severe" — lưu ý: "BR-123" trích dẫn trong Design_System.md không tồn tại trong `business_rules.md`, cần team xác nhận).
- error_message.md (MSG-004, MSG-005).

**Ghi chú**: giống bản Mobile — không có UC-### hay field-by-field riêng cho màn Detail. **[Giả định — cần team xác nhận]**.

# Screen Spec — Update Health Record Screen

**Actor**: Medical Staff | **UC liên quan**: UC-094 (Report 3 §3.2, dòng 2131–2149) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-172 (lịch sử đã xác minh không thể sửa/xóa — chỉ được append)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class (context, read-only) | Text | — | — | Kế thừa từ `04_StudentHealthRecordDetail.md` |
| 2 | Clinical Annotation mới | Textarea | Bắt buộc khi thêm mới; append-only | BR-172 | UC-094 bước 3 |
| 3 | Medications (chỉnh sửa) | Input/Textarea | — | BR-172 | |
| 4 | Emergency Instructions (chỉnh sửa) | Textarea | — | — | |
| 5 | Lịch sử log đã xác minh (read-only) | List (đọc) | Không được sửa/xóa | BR-172 | |
| 6 | Nút "Save" | Button (primary) | Disabled khi chưa có thay đổi | BR-031, BR-172 | Submit → MSG-036 |
| 7 | Nút "Cancel" | Button | — | — | Hủy, quay lại `04_StudentHealthRecordDetail.md` |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải dữ liệu hiện có; spinner trên "Save".
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; cố sửa/xóa log đã xác minh → chặn, cảnh báo "Không thể chỉnh sửa lịch sử đã xác minh" (BR-172).
- Success: MSG-036 (Toast), quay lại `04_StudentHealthRecordDetail.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Update Record" trên `04_StudentHealthRecordDetail.md` (Web); thành công/hủy → quay lại Detail.
**Bố cục**: `Dialog` (form <8 field, theo `Layout_Guidelines.md` mục 1.2) — form 1 cột giữa trang, khối lịch sử log đã xác minh hiển thị bên dưới form dạng read-only, nút "Save"/"Cancel" cuối Dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Update Record" trên `04_StudentHealthRecordDetail.md` (Mobile); thành công/hủy → quay lại Detail.
**Bố cục**: trang riêng full-screen, form dọc, khối "Lịch sử log đã xác minh" cuối trang dạng read-only card mờ, nút "Cancel"/"Save" cố định trên AppBar hoặc cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Health Record, nền tảng Web, actor Medical Staff.
Bố cục: Dialog form 1 cột, Clinical Annotation mới (textarea), Medications, Emergency Instructions,
khối lịch sử log đã xác minh (read-only), nút Save/Cancel.
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
| 3 | Mỗi mốc thời gian (event) | Card/Item | — | — | Loại sự kiện, ngày giờ, người thực hiện |
| 4 | Nút "Export" | Button | — | — | Xuất timeline dạng báo cáo (PDF/Excel theo BR-178) |

### Trạng thái màn hình
- Loading: `Skeleton` cho timeline.
- Empty: "Chưa có lịch sử sức khỏe nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: xuất báo cáo thành công → Toast xác nhận chung (theo BR-178).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md` (Web); Back → quay lại Detail.
**Bố cục**: `Sheet`/trang con — Timeline dọc chiếm toàn bộ Content Area, nút "Export" ở góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md` (Mobile); Back → quay lại Detail.
**Bố cục**: trang riêng full-screen, AppBar Back, danh sách Timeline dọc, nút "Export" cố định góc trên phải AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Health History, nền tảng Web, actor Medical Staff.
Bố cục: trang con Timeline dọc chiếm Content Area, nút Export ở page header.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2219–2234 (UC-146).
- business_rules.md (BR-172, BR-178).
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
- Loading: `Skeleton` khi tải chi tiết yêu cầu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; dietary type không thuộc danh mục chuẩn (BR-066) → cảnh báo, chặn Approve.
- Success: chuyển tiếp sang dialog Approve/Reject tương ứng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `08_SpecialMealRequestList.md` (Web); Approve/Reject → `Dialog` tương ứng.
**Bố cục**: `Sheet` bên phải chứa toàn bộ thông tin yêu cầu + tài liệu minh chứng, 2 nút Approve/Reject cố định cuối Sheet.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Special Meal Card trên `08_SpecialMealRequestList.md` (Mobile); Approve/Reject → dialog tương ứng.
**Bố cục**: trang riêng full-screen, block thông tin xếp dọc, 2 nút Approve/Reject cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Review Special Meal Request, nền tảng Web, actor Medical Staff.
Bố cục: Sheet bên phải, Dietary type, Duration, Medical justification, tài liệu minh chứng, nút Approve/Reject.
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
| 1 | Page Header ("Special Meal") | Text | — | — | Report 3 §3.3.2.3.3 |
| 2 | Tab "Awaiting approval" | Tab | — | BR-031 | |
| 3 | Tab "Approved" | Tab | — | — | |
| 4 | Bảng danh sách yêu cầu | Table | — | — | Cột: Student Name & Class, Status Badge, Meal Type, Reason |
| 5 | Status Badge (mỗi dòng) | Badge màu | "Needs Approval" (warning) | BR-031 | |
| 6 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Điều hướng `07_ReviewSpecialMealRequestScreen.md` |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng.
- Empty: "Không có yêu cầu bữa ăn đặc biệt nào" (mỗi tab) + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng ở màn List.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Approve Special Meal" trên Dashboard (Web); chọn dòng → `07_ReviewSpecialMealRequestScreen.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Page Header 2 Tab, Content Area dùng `Table`, phân trang dưới bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Approve Special Meal" trên Dashboard (Mobile); chọn Card → `07_ReviewSpecialMealRequestScreen.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.3.3 — AppBar (Back, Home, Screen Title), 2 Tab ngang, danh sách `Card` cuộn dọc, pull-to-refresh bắt buộc.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Special Meal Request List, nền tảng Web, actor Medical Staff.
Bố cục: Page Header 2 Tab (Awaiting approval/Approved), Table (Student Name & Class, Status Badge, Meal Type, Reason).
Áp dụng Design System: warning #B45309 cho "Needs Approval".
Trạng thái cần thể hiện: loading, empty (mỗi tab), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1977–1995 (UC-036), dòng 5731–5757 (§3.3.2.3.3).
- business_rules.md (BR-031, BR-066, BR-172).
- error_message.md (MSG-004, MSG-005, MSG-036).

**Ghi chú lệch tên**: giống bản Mobile — UC-036 trong Report 3 mô tả "View List Allergy & Special Meals", field-by-field thực tế lấy từ §3.3.2.3.3. **[Giả định — cần team xác nhận]**.

# Screen Spec — Approve Special Meal Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083, bước 5) | **Status**: Draft
**BR liên quan**: BR-066 (danh mục chế độ ăn đặc biệt), BR-082 (chế biến riêng cho bữa ăn đặc biệt)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt yêu cầu (Student, Meal Type, Reason) | Text (đọc) | — | — | Kế thừa từ `07_ReviewSpecialMealRequestScreen.md` |
| 2 | Ghi chú xác nhận (tùy chọn) | Textarea | Không bắt buộc | — | |
| 3 | Nút "Confirm Approve" | Button (primary) | — | BR-066 | Submit → cập nhật trạng thái, routing tới Service Caterers |
| 4 | Nút "Cancel" | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner trên "Confirm Approve".
- Empty: không áp dụng.
- Error: MSG-004, MSG-005.
- Success: cập nhật trạng thái "Approved", thông báo Service Caterers, quay lại `08_SpecialMealRequestList.md` (Toast xác nhận chung).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Approve" trên `07_ReviewSpecialMealRequestScreen.md` (Web); Confirm/Cancel → quay lại List.
**Bố cục**: `AlertDialog` — tóm tắt yêu cầu + textarea ghi chú, 2 nút Confirm/Cancel.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Approve" trên `07_ReviewSpecialMealRequestScreen.md` (Mobile); Confirm/Cancel → quay lại List.
**Bố cục**: `showModalBottomSheet` — tóm tắt yêu cầu, textarea ghi chú, 2 nút Confirm/Cancel cố định cuối sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Approve Special Meal Dialog, nền tảng Web, actor Medical Staff.
Bố cục: AlertDialog, tóm tắt yêu cầu, ghi chú xác nhận (tùy chọn), nút Confirm Approve, nút Cancel.
Áp dụng Design System: success #15803D.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2065–2083 (UC-078, bước 5).
- business_rules.md (BR-066, BR-082).
- error_message.md (MSG-004, MSG-005).

# Screen Spec — Reject Special Meal Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083, Exceptions) | **Status**: Draft
**BR liên quan**: BR-066 (danh mục chế độ ăn đặc biệt)
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
- Error: MSG-004, MSG-005; chưa nhập lý do → cảnh báo inline.
- Success: cập nhật trạng thái "Rejected", quay lại `08_SpecialMealRequestList.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Reject" trên `07_ReviewSpecialMealRequestScreen.md` (Web); Confirm/Cancel → quay lại List.
**Bố cục**: `AlertDialog` — tóm tắt yêu cầu + textarea lý do từ chối, 2 nút Confirm/Cancel.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Reject" trên `07_ReviewSpecialMealRequestScreen.md` (Mobile); Confirm/Cancel → quay lại List.
**Bố cục**: `showModalBottomSheet` — tóm tắt yêu cầu, textarea lý do từ chối, 2 nút Confirm/Cancel.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Reject Special Meal Dialog, nền tảng Web, actor Medical Staff.
Bố cục: AlertDialog, tóm tắt yêu cầu, textarea Lý do từ chối (bắt buộc), nút Confirm Reject, nút Cancel.
Áp dụng Design System: danger #B91C1C.
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

> **Lịch sử sửa filter**: (1) **09/07/2026** — đổi filter chính "Bước kiểm tra" → "Kết quả" (`review_medicalstaff_compliance_kitcheninspection.md` Mục 5), vì Medical Staff quan tâm phiên Đạt/Không đạt hơn đang ở bước mấy; (2) **12/07/2026** (rà soát ảnh chụp màn hình thực tế, Hình 5) — bỏ hẳn filter "Bước" (kể cả bản phụ đã thêm ở code) và filter "Món", chỉ giữ Date range + Kết quả để màn lọc gọn, tránh chồng chéo với modal chi tiết (đã hiển thị đầy đủ dữ liệu từng bước — xem Mục 6 review file). Xem `review_screen_medical_staff.md` Mục 15.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo khoảng ngày | Date range picker | — | — | UC-112 bước 3 |
| 2 | Filter theo Kết quả | Select | Đạt / Không đạt / Đang thực hiện — khớp field `overall` của `InspectionSession` | BR-094, BR-098 | **Sửa 09/07/2026** — thay cho filter "Bước kiểm tra" cũ (đã bỏ hẳn, xem field 3) |
| 3 | Bảng danh sách bản ghi kiểm tra | Table | — | — | Cột: ngày, trạng thái, nhân viên phụ trách |
| 4 | Status Badge (mỗi dòng) | Badge màu | Success (Passed)/Danger (Failed) | BR-094, BR-098 | |
| 5 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Ảnh bằng chứng, timestamp, GPS, ghi chú nhân viên; modal hiển thị đầy đủ Mẫu 1/2/3 theo bước (xem `review_medicalstaff_compliance_kitcheninspection.md` Mục 6) |

**Đã bỏ (12/07/2026)**: filter "Bước kiểm tra" (kể cả filter phụ + filter "Trạng thái bước" con đã thêm ở code) và filter "Món/thực đơn" — người dùng đánh giá không cần thiết khi đã có filter Kết quả + modal chi tiết đầy đủ.

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách.
- Empty: AF1 "No Records Found" — "Không tìm thấy bản ghi phù hợp".
- Error: MSG-004, MSG-005, MSG-016, MSG-017.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "View food inspection results in 3 steps" trên Dashboard (Web); chọn dòng → `Sheet` chi tiết bên phải.
**Bố cục**: thanh filter (date range, Kết quả) phía trên `Table`, phân trang dưới bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "View food inspection results in 3 steps" trên Dashboard (Mobile); chọn item → bottom sheet chi tiết.
**Bố cục**: 1 cột, filter `Chip`/`DropdownButton` (date range, Kết quả), danh sách `Card` cuộn dọc.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Inspection Records, nền tảng Web, actor Medical Staff.
Bố cục: thanh filter (date range, Kết quả: Đạt/Không đạt/Đang thực hiện), Table danh sách bản ghi
kiểm tra với Status Badge.
Áp dụng Design System: success #15803D cho Passed, danger #B91C1C cho Failed.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên (KHÔNG có filter Bước, KHÔNG có filter Món).
```

## Nguồn dữ liệu
- Report 3.md, dòng 3553–3571 (UC-112).
- business_rules.md (BR-091–BR-099).
- error_message.md (MSG-004, MSG-005, MSG-016, MSG-017).
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 5 (đổi filter Kết quả), Mục 6 (modal chi tiết đầy đủ).

# Screen Spec — Conduct Food Safety Inspection Screen

**Actor**: Medical Staff | **UC liên quan**: UC-014 (Report 3 §3.2, dòng 3443–3461), **UC-186 (Manage Kitchen Inspection Checklist — mới, 06/07/2026)** | **Status**: Draft
**BR liên quan**: BR-101 (kiểm tra hàng tháng), BR-102 (kiểm soát côn trùng), BR-103 (khám sức khỏe NV bếp), BR-104 (điểm vệ sinh), BR-110 (nhiệt độ tủ lưu mẫu — kiểm tra chéo, xem ghi chú), **BR-207 (bằng chứng theo từng mục + chú thích căn cứ pháp lý — đã chốt 05/07/2026, sửa đổi 06/07/2026)**, ~~BR-208 (phân loại Định kỳ/Đột xuất)~~ **Deprecated 09/07/2026**, **BR-210 (Medical Staff tự quản lý CRUD checklist — mới, 06/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG cảnh báo điểm thấp (**chưa xác định mã**, xem ghi chú), MSG-094 (xóa hạng mục đang được tham chiếu)

> **Lịch sử cập nhật**
> - **2026-07-04**: đổi field "Upload ảnh bằng chứng" (1 ảnh chung cho cả checklist) → **đánh giá Đạt/Không đạt + bằng chứng theo từng hạng mục**; đề xuất BR-206/BR-207 để thay ghost-citation `BR-105`/`BR-119` (BR-105 canonical là quy tắc lưu mẫu thực phẩm, chủ đề khác; BR-119 không tồn tại trong `business_rules.md`).
> - **2026-07-05 (đính chính)**: bản 2026-07-04 đã gỡ nhầm `BR-110` với lý do "không liên quan tới checklist bếp". Sai — `Report 3.md` (dòng 3459, đặc tả gốc UC-014) liệt kê rõ BR-110 trong dòng "Business Rules" của chính UC-014. Đã khôi phục. BR-110 (2–8°C) vốn thuộc module Food Sample Retention (Service Caterers, UC-042/069); Medical Staff dùng lại đúng mã này khi kiểm tra chéo tủ lưu mẫu trong lúc audit bếp (UC-014) — không phải quy tắc mới. Đã bổ sung hạng mục D4 tương ứng vào `MedicalStaff_KitchenInspectionChecklist_Proposal.md`.
> - **2026-07-05 (quyết định chính thức của đội dự án, đợt 1)**:
>   1. **BR-207 được tạm chốt** (per-item Pass/Fail + bằng chứng có điều kiện + chú thích căn cứ pháp lý dạng icon "ℹ") — đã merge chính thức vào `business_rules.md` §12. Không còn ở trạng thái "đề xuất".
>   2. **BR-206 bị loại bỏ** — không chính thức hóa ngưỡng "điểm <60%" thành Business Rule mới (không có căn cứ pháp luật/quy định nội bộ).
>   3. **BR-208 mới** — hợp thức hóa field "Loại kiểm tra: Định kỳ / Đột xuất".
> - **2026-07-05 (quyết định chính thức của đội dự án, đợt 2 — descope hậu quả khóa bếp)**: đội dự án quyết định **bỏ hoàn toàn** hậu quả "khóa vận hành bếp, chặn phân phát bữa ăn" mà UC-014 AF1 gốc mô tả khi điểm <60% — đây là **descope có chủ đích** so với `Report 3.md` gốc, không phải sửa lỗi trích dẫn. Lý do: (a) không có căn cứ pháp lý cho việc khóa cả bếp chỉ vì 1 điểm số nội bộ; (b) `MSG-024` mà bản gốc dùng cho cơ chế này thực chất thuộc phạm vi **Incident Management** (theo `error_message.md`, MSG-024 được xếp vào module Incident Management, dùng cho BR-149 — cách ly **lô nguyên liệu** khi có sự cố ATTP) — SRS gốc đã mượn nhầm cơ chế của module khác cho một ngưỡng kiểm tra định kỳ. **Hành vi mới**: điểm <60% → hệ thống **vẫn tự động gửi cảnh báo tới School Administration** (bỏ Service Caterers/School Board — không còn là tình huống khẩn cấp cấp-sự-cố), nhưng **bếp tiếp tục vận hành bình thường, không chặn phân phát bữa ăn**. Kết quả Pass/Failed vẫn ghi nhận đủ theo BR-104. Không tái dùng `MSG-024` (sai module + sai ngữ nghĩa "operation blocked") — **cần đội dự án cấp mã MSG mới** cho cảnh báo không-chặn này. `BR-149` (cách ly lô nguyên liệu khi có sự cố) **giữ nguyên, không liên quan** tới quyết định này. Chi tiết xem `business_rules.md` §12 (ghi chú sau BR-208) và `MedicalStaff_UserStory.md` US-08.
> - **2026-07-06 (checklist chuyển sang dynamic, quyết định người dùng)**: checklist Kitchen Safety and Sanitation Checklist (UC-014) không còn là danh sách hạng mục cố định — Medical Staff nay có toàn quyền CRUD (thêm/sửa/xóa/sắp xếp) cả nhóm và hạng mục, kể cả nội dung chú thích căn cứ pháp lý (icon "ℹ"), **không cần System Admin cấu hình, không cần phê duyệt** (BR-210 mới, thay thế điều khoản "System Admin cấu hình chú thích" trong BR-207 gốc). Nội dung 6 nhóm checklist cụ thể (thay thế Nhóm A–F cũ) chuyển sang dùng làm **dữ liệu khởi tạo mặc định** — xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (bản cập nhật 06/07/2026) và UC-186 (màn hình quản lý checklist mới, mô tả ở mục "Quản lý Checklist" bên dưới).
> Danh sách hạng mục checklist cụ thể (6 nhóm, thay cho Nhóm A–F cũ) và nội dung chú thích pháp lý lấy từ `MedicalStaff_KitchenInspectionChecklist_Proposal.md` vẫn là **bản nháp tham khảo nội dung/dữ liệu khởi tạo** — phần *cơ chế* (BR-207/BR-210) đã chốt, nhưng *nội dung câu chữ từng hạng mục cụ thể* do Medical Staff tự điều chỉnh sau khi hệ thống vận hành (CRUD, BR-210).
> - **2026-07-09 (deprecate BR-208)**: bỏ hẳn field/bước chọn "Loại kiểm tra: Định kỳ / Đột xuất" — không còn phân biệt ở bất kỳ tầng nào (kể cả thống kê ngầm). Lý do: chỉ phục vụ thống kê tần suất, không có căn cứ pháp luật bắt buộc, không ảnh hưởng cơ chế tính điểm. Xem `03_Design/08_FrontEnd/Web/review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 3. Đã xoá field 1b + mọi tham chiếu "bước đầu chọn Loại kiểm tra" trong Platform Layout/AI Prompt bên dưới.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "New Inspection" | Button | — | — | Bắt đầu phiên kiểm tra mới |
| 2 | Kitchen Safety and Sanitation Checklist | Danh sách hạng mục (mỗi dòng = 1 hạng mục, nhóm theo Card) | Bắt buộc đánh giá đủ mọi hạng mục | BR-101, BR-104 | Danh sách hạng mục cụ thể: xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (6 nhóm — Hồ sơ, Người tiếp xúc trực tiếp, Bảo quản, Sơ chế/chế biến, An toàn thực phẩm, Kiểm thực 3 bước/lưu mẫu; dữ liệu khởi tạo, Medical Staff có thể tự CRUD qua UC-186) |
| 2e | Nút "Quản lý Checklist" (mở màn hình/luồng UC-186) | Button (secondary, đặt cạnh "New Inspection") | — | BR-210 | Điều hướng sang luồng CRUD nhóm/hạng mục/chú thích — xem mục "Quản lý Checklist (UC-186)" bên dưới |
| 2a | Mỗi hạng mục — Toggle "Đạt / Không đạt" | Toggle/Radio | Bắt buộc chọn 1 trong 2 cho từng hạng mục | BR-207 | Mỗi hạng mục có kết quả riêng, dùng làm input tính điểm tổng (BR-104) |
| 2b | Mỗi hạng mục — Ảnh minh chứng | Input type="file" + preview | **Đạt** → tùy chọn. **Không đạt** → bắt buộc ít nhất 1 ảnh gắn đúng hạng mục | BR-207 | Thay thế field "Upload ảnh bằng chứng vệ sinh bếp" cũ (1 ảnh chung cho cả phiên) |
| 2c | Mỗi hạng mục — Lý do không đạt (comment) | Textarea | Bắt buộc khi hạng mục = Không đạt; ẩn khi = Đạt | BR-207 | Nội dung có thể tái sử dụng (pre-fill) cho "Mô tả vi phạm" ở `21_RecordProcessViolation.md` nếu phát hiện leo thang thành vi phạm |
| 2d | Mỗi hạng mục — icon "ℹ" Chú thích căn cứ pháp lý | Icon mở Popover/Tooltip (thu gọn mặc định) | Không bắt buộc tương tác | BR-207, BR-210 | Nội dung text đơn giản, không versioning/effective-date, **theo từng trường** (không còn áp dụng chung mọi trường như bản BR-207 gốc). **[SỬA 06/07/2026]** Medical Staff tự sửa trực tiếp qua màn hình "Quản lý Checklist" (UC-186), không còn do System Admin cấu hình. Thay đổi nội dung ghi qua audit log chung (BR-170). Nội dung khởi tạo tham khảo: cột "Chú thích rút gọn" trong `MedicalStaff_KitchenInspectionChecklist_Proposal.md` |
| 3 | Xác minh chứng nhận kiểm soát côn trùng (quý) | Checkbox + ngày hết hạn | Bắt buộc còn hiệu lực | BR-102 | Giữ nguyên so với bản trước |
| 4 | Xác minh khám sức khỏe nhân viên bếp (năm) | Checkbox + ngày hết hạn | Bắt buộc còn hiệu lực | BR-103 | Giữ nguyên so với bản trước |
| 5 | Nút "Submit Checklist" | Button (primary) | Disabled khi: còn hạng mục chưa đánh giá, HOẶC có hạng mục Không đạt mà thiếu ảnh/thiếu lý do | BR-101–104, BR-207 | Hệ thống tự tính điểm từ kết quả Đạt/Không đạt của từng hạng mục — **công thức/trọng số tính điểm chưa chốt** (xem Ghi chú 4, `MedicalStaff_KitchenInspectionChecklist_Proposal.md`) |

### Trạng thái màn hình
- Loading: spinner khi tính điểm checklist sau submit.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; hạng mục Không đạt thiếu ảnh/lý do → chặn submit, cảnh báo inline tại đúng hạng mục (**mã MSG cụ thể chưa chốt** — tạm đề xuất tái dùng MSG-085 "thiếu thông tin bắt buộc" hoặc cấp MSG mới, cần đội dự án xác nhận); điểm < 60% → hệ thống lưu kết quả **"Failed"**, hiển thị Alert (warning, không phải banner khóa cố định) và **tự động gửi cảnh báo tới School Administration** — **không khóa vận hành bếp, không chặn phân phát bữa ăn** (descope 05/07/2026 so với AF1 gốc; không gắn mã BR nào; **mã MSG chưa xác định**, không dùng MSG-024 — xem "Lịch sử cập nhật" ở trên).
- Success: điểm ≥ 60% → lưu "Passed", Toast xác nhận chung.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Conduct Food Safety Inspection" trên Dashboard (Web); thành công/thất bại → quay lại Dashboard.
**Bố cục**: `Stepper` ngang cố định trên form, mỗi bước hiển thị 1 nhóm checklist trong `Card` (đã bỏ bước chọn "Loại kiểm tra" — deprecate BR-208, 09/07/2026). Mỗi hạng mục là 1 dòng trong Card gồm: nhãn hạng mục + icon "ℹ" (mở Popover chú thích căn cứ pháp lý) + Toggle "Đạt/Không đạt" + khu vực ảnh (`Input type="file"` + preview, hiện luôn nhưng chỉ bắt buộc khi Không đạt) + Textarea lý do (chỉ hiện khi Không đạt). Nút "Submit Checklist" cố định cuối form.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Conduct Kitchen Inspection" trên Dashboard (Mobile); thành công/thất bại → quay lại Dashboard, Alert cảnh báo (không phải banner khóa) nếu Failed.
**Bố cục**: `Stepper` Flutter nhiều trang, mỗi bước 1 nhóm checklist (đã bỏ bước chọn "Loại kiểm tra" — deprecate BR-208, 09/07/2026). Mỗi hạng mục là 1 `Card` con gồm: nhãn hạng mục + icon "ℹ" (mở BottomSheet chú thích) + Toggle "Đạt/Không đạt" + nút camera (ưu tiên hiển thị đầu Card, chỉ bắt buộc chụp khi Không đạt) + ô nhập lý do (chỉ hiện khi Không đạt). Nút "Submit Checklist" ở bước cuối.

---

## Quản lý Checklist (UC-186, mới 06/07/2026)

> **BR liên quan**: BR-210. Đây là luồng riêng, tách khỏi luồng "thực hiện kiểm tra" (form chính ở trên) — vào từ nút "Quản lý Checklist" (field 2e).

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| M1 | Danh sách nhóm (group) checklist, dạng cây/accordion | Accordion + drag handle | — | BR-210 | Mỗi nhóm hiển thị tên + danh sách hạng mục con, kéo-thả để sắp xếp lại thứ tự |
| M2 | Nút "Thêm nhóm mới" | Button | Bắt buộc nhập tên nhóm, không để trống | BR-210 | Tạo nhóm rỗng, thêm hạng mục sau |
| M3 | Nút "Sửa tên nhóm" / "Xóa nhóm" (trên mỗi nhóm) | Icon button | Xóa nhóm có hạng mục → cảnh báo xác nhận trước (dialog) | BR-210 | Xóa không ảnh hưởng dữ liệu lịch sử đã lưu (chỉ ẩn khỏi phiên kiểm tra mới) |
| M4 | Nút "Thêm hạng mục" (trong mỗi nhóm) | Button | Bắt buộc nhập tên hạng mục | BR-210 | Hạng mục mới mặc định chưa có chú thích căn cứ pháp lý (có thể để trống) |
| M5 | Mỗi hạng mục — Nút "Sửa" / "Xóa" / kéo-thả sắp xếp | Icon button + drag handle | Xóa hạng mục **đang được tham chiếu bởi phiên kiểm tra đã lưu** → cảnh báo (MSG-094), vẫn cho phép xóa nhưng dữ liệu lịch sử được giữ nguyên, chỉ ẩn khỏi phiên mới | BR-210 | — |
| M6 | Mỗi hạng mục — Textarea "Chú thích căn cứ pháp lý" | Textarea | Không bắt buộc | BR-207, BR-210 | Text đơn giản, không versioning; hiển thị lại đúng nội dung này ở icon "ℹ" trong màn hình thực hiện kiểm tra (field 2d) |
| M7 | Nút "Lưu thay đổi" | Button (primary) | — | BR-170 | Mọi thay đổi (thêm/sửa/xóa/sắp xếp) ghi vào audit log chung khi lưu |

### Trạng thái màn hình
- Empty: chưa có nhóm nào → hiển thị gợi ý "Thêm nhóm đầu tiên", có thể nạp sẵn 6 nhóm mặc định từ `MedicalStaff_KitchenInspectionChecklist_Proposal.md` khi khởi tạo trường mới.
- Error: xóa hạng mục đang dùng → cảnh báo xác nhận (MSG-094), không phải lỗi chặn cứng — Medical Staff vẫn có thể xác nhận xóa (dữ liệu lịch sử được giữ, chỉ ẩn khỏi phiên mới).
- Success: Toast xác nhận đã lưu thay đổi checklist.

### Platform Layout — Web (Quản lý Checklist)
**Entry/Exit points**: vào từ nút "Quản lý Checklist" (field 2e) trên màn hình chính UC-014; thoát quay lại màn hình chính, checklist mới áp dụng ngay cho phiên kiểm tra tiếp theo.
**Bố cục**: 2 cột — cột trái là danh sách nhóm (Accordion, kéo-thả), cột phải là chi tiết hạng mục của nhóm đang chọn (danh sách hạng mục + Textarea chú thích cho hạng mục đang focus). Nút "Lưu thay đổi" cố định cuối trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Conduct Food Safety Inspection, nền tảng Web, actor Medical Staff.
Bố cục: Stepper ngang, mỗi bước 1 Card nhóm checklist (KHÔNG có bước chọn Loại kiểm tra — đã
deprecate BR-208, 09/07/2026). Mỗi hạng mục trong Card gồm: nhãn hạng mục, icon "ℹ" chú thích căn cứ pháp lý (Popover
thu gọn), Toggle Đạt/Không đạt, khu vực upload ảnh (bắt buộc chỉ khi Không đạt), Textarea lý do
(chỉ hiện khi Không đạt). Ngoài checklist còn có xác nhận chứng nhận côn trùng/khám sức khỏe NV bếp
còn hạn, nút Submit Checklist cuối form.
Áp dụng Design System: warning #B45309 cho Alert điểm <60% (không phải banner khóa cố định — bếp vẫn vận hành).
Trạng thái cần thể hiện: loading, error (thiếu ảnh/lý do tại hạng mục Không đạt; điểm <60% → Alert cảnh báo School Administration, không khóa bếp), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3443–3461 (UC-014 — bao gồm dòng "Business Rules": BR-101, BR-102, BR-103, BR-104, BR-105, BR-110; Postconditions/AF1 gốc mô tả khóa bếp — **đã descope 05/07/2026**, xem "Lịch sử cập nhật").
- `business_rules.md` §12 (BR-101, BR-102, BR-103, BR-104, BR-110, BR-207 — đã chính thức; ~~BR-208~~ Deprecated 09/07/2026; BR-210 — mới 06/07/2026; ghi chú cuối §12 về việc không chính thức hóa BR-206 và quyết định descope khóa bếp).
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
| 1 | Filter theo khoảng ngày/bữa/món/trạng thái | Filter bar | — | — | UC-118 bước 3 |
| 2 | Sổ lưu mẫu số hóa (Mẫu số 5) | Table | Đủ 12 cột theo Mẫu số 5 | BR-107, BR-108, BR-111, BR-114 | |
| 3 | Status Badge (mỗi dòng) | Badge màu | Info (Retained)/Warning (Retrieved)/Secondary (Disposed) | — | |
| 4 | Nút "Export" | Button | — | BR-178 | Xuất Excel/PDF. **Cập nhật 11/07/2026**: xuất đúng theo khoảng ngày đang chọn ở field 1 (Filter theo khoảng ngày) — không xuất toàn bộ dữ liệu bất kể filter đang áp dụng |
| 5 | Chọn 1 dòng → xem chi tiết/thao tác | Interaction | — | — | Điều hướng `14_RetrieveFoodSampleRecordsDialog.md` |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải sổ lưu mẫu.
- Empty: "Chưa có mẫu lưu nào phù hợp bộ lọc" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "Food Sample Records — Mẫu số 5" trên Sidebar (Web); chọn dòng → chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: thanh filter phía trên `Table` đầy đủ 12 cột theo Mẫu số 5, nút "Export" góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Sample Register" trên Dashboard (Mobile); chọn item → chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: 1 cột, filter `Chip`/`DropdownButton`, danh sách `Card` tóm tắt, nút "Export" nổi góc trên phải AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Sample Records (Mẫu số 5), nền tảng Web, actor Medical Staff.
Bố cục: thanh filter (ngày/bữa/món/trạng thái), Table 12 cột, Status Badge, nút Export.
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
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.6): bổ sung BIẾN THỂ ngữ cảnh Incident.** Khi mở từ `17_InvestigationWorkspace.md` (card "1. Thu hồi mẫu lưu") của 1 `INC-xxx` cụ thể, dùng **Dialog cỡ lớn / checklist multi-select** thay cho giao diện search/filter rộng — xem mục "Biến thể ngữ cảnh Incident" bên dưới. Bản search/filter hiện tại **giữ nguyên** cho các điểm vào UC-076 không gắn Incident (VD từ `13_FoodSampleRecords.md` độc lập).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tìm mẫu theo ngày/món/batch | Search/Filter | — | — | UC-076 bước 1 |
| 2 | Danh sách mẫu khớp kết quả | List | Hiển thị trạng thái + thời gian lưu còn lại | BR-111 | |
| 3 | Nút "Authorize Retrieval" (mỗi item) | Button | Chỉ khả dụng khi mẫu "Retained" | BR-113 | Nếu "Disposed" → chỉ hiện "Link Disposed Sample to Incident" (AF1) |
| 4 | Incident ID | Input (text)/Select | Bắt buộc; phải active | BR-114 | Option đã đóng bị disable ngay tại dropdown (không chờ Submit mới báo MSG-023) |
| 5 | Tên người ủy quyền (`authorizedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản Medical Staff đăng nhập | BR-114 | **Sửa 12/07/2026**: trước đây Input tự do, nay readonly — nhất quán với `reportedBy` (BR-183), "Đại diện Y tế trường học" (BR-211) |
| 6 | Phòng lab đích | Select | Bắt buộc | BR-114 | Giữ nguyên — danh sách phòng lab cố định |
| 7 | Đại diện phòng lab nhận mẫu | Input (text tự do) | Tùy chọn — có thể chưa xác định tại thời điểm thu hồi | BR-212 | Mới — khớp field "Đại diện là" (Bên nhận mẫu) trong Phụ lục V |
| 8 | Chỉ tiêu yêu cầu kiểm nghiệm | Textarea | Bắt buộc | BR-212 | Mới — khớp cột "Chỉ tiêu yêu cầu kiểm nghiệm" trong Phụ lục V; MSG-023 nếu thiếu |
| 9 | Nút "Submit" | Button (primary) | Disabled khi thiếu field bắt buộc (4, 6, 8) | BR-113, BR-114, BR-150, BR-212 | Khóa mẫu, sinh biên nhận in (nội dung mở rộng theo Phụ lục V) |

### Biến thể ngữ cảnh Incident (mới 15/07/2026, đợt 2 — Mục 11.6)

Khi mở từ `17_InvestigationWorkspace.md` của 1 `INC-xxx`:

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| I1 | Nút "← Quay lại Không gian điều tra (INC-xxx)" | Button (ghost) | — | — | Điều hướng chủ động (Mục 7.6 Loại A), quay đúng Investigation Workspace của `INC-xxx` đang mở |
| I2 | Checklist mẫu (multi-select) | Checklist | Mặc định = `suspectedDishIds[]` đã chọn ở trạng thái TREATMENT | BR-111, BR-150 | Mỗi dòng = 1 mẫu, kèm trạng thái + thời gian lưu còn lại (BR-111); tick nhiều dòng. Mẫu `Disposed` (quá 72h) hiển thị mờ + chỉ cho "Link Disposed Sample to Incident" (AF1) |
| I3 | Incident ID | Text (đọc, khóa sẵn) | Set sẵn theo context, không chọn tay | BR-114 | Khác bản search/filter (Incident ID là Select) — ở đây khóa theo `INC-xxx` đang điều tra |
| I4 | Tên người ủy quyền (`authorizedBy`) | Text (đọc) | Tự động = Medical Staff đăng nhập | BR-114 | Như field 5 bản gốc |
| I5 | Phòng lab đích (dùng chung cả lô gửi) | Select | Bắt buộc | BR-114 | **Quyết định 15/07/2026 (Mục 11.6, Q2)**: **1 phòng lab dùng chung** cho mọi mẫu đã tick trong 1 lần gửi — khớp thực tế Việt Nam: mẫu ngộ độc gửi về **1 phòng lab được chỉ định (ISO 17025/VILAS)** chạy trọn panel vi sinh + hóa (Salmonella, E. coli, S. aureus, B. cereus, Cl. perfringens…) trên nhiều nền mẫu cùng lúc |
| I6 | Chỉ tiêu yêu cầu kiểm nghiệm | Textarea (panel dùng chung, pre-fill mỗi món, cho phép sửa theo từng món) | Bắt buộc | BR-212 | **Quyết định 15/07/2026 (Mục 11.6, Q2)**: mặc định 1 panel chỉ tiêu **dùng chung** áp cho mọi món đã tick, nhưng **cho phép mở rộng chỉnh riêng theo từng món** — clinically món sữa/tráng miệng nghiêng về độc tố tụ cầu (S. aureus enterotoxin), món thịt nghiêng về Salmonella. Cân bằng đơn giản (1 submission) + linh hoạt lâm sàng |
| I7 | Nút "Submit" (gửi 1 lần cho cả lô) | Button (primary) | Disabled khi thiếu I5/I6 hoặc chưa tick mẫu nào | BR-113, BR-114, BR-150, BR-212 | Khóa tất cả mẫu đã tick, ghi `incidentId` cho từng mẫu, sinh biên nhận. Sau khi submit + quay lại, card "1. Thu hồi mẫu lưu" (`17_InvestigationWorkspace.md`) tự query & liệt kê mẫu có `incidentId = INC-xxx` hiện tại (Mục 9.10 Bug 2, 11.8) |

*(Câu hỏi Q2 do người dùng ủy quyền cho tôi nghiên cứu — đã chốt hướng "1 lab dùng chung + chỉ tiêu mặc định chung, cho phép override theo món". Vẫn là **đề xuất**, có thể điều chỉnh nếu đội dự án muốn khóa cứng chỉ tiêu chung tuyệt đối cho mọi món.)*

### Trạng thái màn hình
- Loading: spinner khi xác thực Incident ID + khóa bản ghi.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; MSG-023 (Incident ID không hợp lệ/đã đóng, hoặc thiếu Chỉ tiêu yêu cầu kiểm nghiệm); mẫu Disposed → chỉ liên kết metadata.
- Success: khóa bản ghi, hiển thị biên nhận thu hồi (nội dung mở rộng — xem Phụ lục V), quay lại `13_FoodSampleRecords.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (Web) hoặc từ Investigation Workspace (Web); thành công → quay lại màn nguồn.
**Bố cục**: `Dialog` — kết quả tìm kiếm mẫu trên cùng, form Incident ID/người ủy quyền (readonly)/phòng lab/đại diện phòng lab/chỉ tiêu kiểm nghiệm bên dưới, nút "Submit"/"Cancel" cuối Dialog.
**Biến thể ngữ cảnh Incident (Mục 11.6)**: `Dialog` cỡ lớn — nút "← Quay lại Không gian điều tra" trên cùng, checklist mẫu (mặc định lọc theo `suspectedDishIds[]`) chiếm phần chính, 1 bộ field dùng chung (Incident ID khóa sẵn | phòng lab đích | chỉ tiêu — pre-fill mỗi món, sửa được), nút "Submit" gửi 1 lần cho cả lô cuối Dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (Mobile) hoặc từ Investigation Workspace (Mobile); thành công → quay lại màn nguồn.
**Bố cục**: `showModalBottomSheet` — kết quả tìm kiếm trên cùng, form bên dưới, nút "Submit" cố định cuối sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Retrieve Food Sample Records Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog, tìm mẫu theo ngày/món/batch, danh sách mẫu khớp, nút Authorize Retrieval,
Incident ID, tên người ủy quyền (hiển thị tự động, không sửa được), phòng lab đích (Select),
đại diện phòng lab nhận mẫu (tùy chọn), chỉ tiêu yêu cầu kiểm nghiệm (Textarea, bắt buộc), nút Submit.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (MSG-023, mẫu Disposed), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm bước ký/chụp ảnh biên bản.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3757–3771 (UC-076).
- business_rules.md (BR-111, BR-113, BR-114, BR-150, BR-212).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md` Phụ lục V (Biên bản bàn giao mẫu) — căn cứ BR-212.
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.6 (biến thể ngữ cảnh Incident, batch lab, giới hạn 72h/Disposed — đợt 2 15/07/2026).

# Screen Spec — View Food Safety Reports Screen

**Actor**: Medical Staff | **UC liên quan**: UC-117 (Report 3 §3.2, dòng 3623–3637); gộp chung UC-115 "View Food Safety Compliance Report" (dòng 3601–3617) theo `14_Screen_Authorization_Matrix.md` khối 6 | **Status**: Draft
**BR liên quan**: BR-100–104 (điểm vệ sinh/kiểm tra/pest control/khám sức khỏe), BR-110 (nhiệt độ bảo quản mẫu, 2–8°C)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 12/07/2026**: (1) gỡ mọi tham chiếu BR-089 (Deprecated theo `business_rules.md` Changelog v1.6 — xem BR-110 thay thế, chỉ áp dụng riêng tủ lưu mẫu 2–8°C, không có cảnh báo IoT tự động); (2) đổi nhóm "Pest Control Log" từ badge hiệu lực nhị phân sang **nhật ký sự kiện** (đồng bộ quyết định `review_medicalstaff_compliance_kitcheninspection.md` Mục 1, đã áp code `compliance/page.tsx`). Xem `review_screen_medical_staff.md` Mục 14.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn nhóm báo cáo | Tab | 3 nhóm: Monthly Kitchen Audits, Pest Control Log, Refrigerator Temperature Logs (chỉ tủ lưu mẫu) | BR-101, BR-102, BR-110 | UC-117 bước 2 |
| 2 | Date range filter | Date range picker | — | — | |
| 3 | Refrigerator ID filter | Select | Chỉ áp dụng nhóm Temperature Logs (tủ lưu mẫu, BR-110) | BR-110 | |
| 4 | Kết quả báo cáo/xu hướng (Monthly Kitchen Audits, Temperature Logs) | Chart (recharts)/Table | — | BR-101, BR-110 | |
| 4b | Nhật ký kiểm soát côn trùng (nhóm Pest Control Log) | Table — mỗi dòng = 1 lần kiểm soát/xử lý | Cột: Ngày kiểm soát/xử lý, Đơn vị thực hiện, Loại côn trùng phát hiện (nếu có) + số lượng ước tính, Biện pháp xử lý, Ngày hẹn kiểm tra tiếp theo | BR-102 | Thay cho badge "Còn hiệu lực"/"Đã hết hạn" cũ — không phải chứng chỉ có hạn pháp lý mà là chuỗi sự kiện xử lý liên tục |
| 5 | Compliance summary dashboard (từ UC-115) | Card thống kê | Tổng hợp audit/pest control (bản ghi gần nhất + ngày hẹn tiếp theo)/khám SK NV/điểm vệ sinh/nhiệt độ tủ lưu mẫu | BR-100–104, BR-110 | |
| 6 | Export format (PDF/Excel) | Select | — | BR-178 | |
| 7 | Nút "Export" | Button | — | BR-178 | **Cập nhật 11/07/2026**: xuất đúng theo khoảng ngày đang chọn ở field 2 (Date range filter) — không xuất toàn bộ dữ liệu bất kể filter đang áp dụng (làm rõ theo phản hồi người dùng, tránh nhầm lẫn Export và Filter là 2 khoảng ngày độc lập) |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải báo cáo/biểu đồ.
- Empty: "Không có dữ liệu cho khoảng thời gian đã chọn" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: Tab chọn nhóm báo cáo trên Page Header, Content Area 2 khối (chart theo nhóm + Compliance Summary Dashboard), nút "Export" ở page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard (Mobile).
**Bố cục**: 1 cột, Tab trên cùng, filter dưới Tab, kết quả dạng `fl_chart`/Card cuộn dọc, Compliance Summary là section cuộn tới, nút "Export" nổi AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Food Safety Reports (gộp Compliance Report), nền tảng Web, actor Medical Staff.
Bố cục: Tab chọn nhóm báo cáo (Monthly Kitchen Audits / Pest Control Log / Refrigerator Temperature Logs),
filter ngày/refrigerator ID, chart kết quả (2 nhóm đầu) hoặc Table nhật ký sự kiện (nhóm Pest Control Log:
Ngày, Đơn vị thực hiện, Loại côn trùng + số lượng ước tính, Biện pháp xử lý, Ngày hẹn tiếp theo),
Compliance Summary Dashboard, nút Export.
Áp dụng Design System: warning #B45309 cho đánh giá quá hạn/nhiệt độ vượt ngưỡng.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3623–3637 (UC-117), dòng 3601–3617 (UC-115).
- business_rules.md (BR-100, BR-101, BR-102, BR-103, BR-104, BR-110, BR-178; BR-089 Deprecated — Changelog v1.6).
- error_message.md (MSG-004, MSG-005).
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 1 (nhật ký kiểm soát côn trùng).

# Screen Spec — View School Policy Screen

**Actor**: Medical Staff (dùng chung với Teacher, Service Caterers theo UC-137) | **UC liên quan**: UC-137 (Report 3 §3.2, dòng 1144 — chỉ có mô tả tóm tắt 1 dòng, KHÔNG có bảng đặc tả Normal/Alternative/Exception Flow đầy đủ) | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường học theo vai trò)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách chính sách/quy định của trường | List/Document viewer | Chỉ xem chính sách của trường mình | BR-022 | [Giả định — cần team xác nhận] |
| 2 | Tiêu đề + ngày ban hành (mỗi chính sách) | Text | — | — | [Giả định] |
| 3 | Nội dung chính sách (xem chi tiết) | Text/PDF viewer | Chỉ đọc | — | [Giả định] |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách chính sách.
- Empty: "Trường chưa cập nhật chính sách nào" + illustration.
- Error: MSG-004, MSG-005; truy cập chính sách trường khác → từ chối theo BR-022.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "View School Policy" trên Sidebar (nhóm "Kiểm tra & Giám sát ATTP").
**Bố cục**: danh sách chính sách dạng `Table`/Card grid bên trái, panel xem nội dung bên phải khi chọn 1 mục.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "View School Policy" trên Compliance Dashboard (Mobile).
**Bố cục**: trang riêng full-screen, danh sách `Card`/`ListTile`, chọn 1 mục mở PDF viewer hoặc trang nội dung.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View School Policy, nền tảng Web, actor Medical Staff.
Bố cục: danh sách chính sách bên trái, panel xem nội dung bên phải.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1144 (UC-137).
- business_rules.md (BR-022).
- error_message.md (MSG-004, MSG-005).

**Ghi chú quan trọng — khoảng trống dữ liệu**: giống bản Mobile, UC-137 không có bảng đặc tả đầy đủ trong Report 3. **[Giả định — cần team xác nhận]** trước khi khóa spec.

# Screen Spec — Investigation Workspace

**Actor**: Medical Staff | **UC liên quan**: UC-041 (Report 3 §3.2, dòng 4445–4460) | **Status**: Draft
**BR liên quan**: BR-146 (ngưỡng tự động kích hoạt điều tra), BR-147 (phân loại severity), BR-148 (cảnh báo tức thời High), BR-149 (cách ly lô hàng), BR-150 (điều tra nguyên nhân gốc rễ), BR-151 (bản đồ truy xuất trong 4 giờ), BR-152 (CAPA), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 15/07/2026 — banner "đang chờ ai"**: bổ sung field 0 (banner đầu trang), áp dụng quyết định đã có sẵn từ 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.6, "Loại B — điểm chờ hành động actor khác") nhưng trước đó chưa được đưa vào Screen Spec. Lý do: Medical Staff chắc chắn sẽ rời màn đi xử lý case khác trong lúc chờ actor khác hành động — cần biết rõ ngay khi quay lại, thay vì nhãn chung chung.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.5/11.8/11.9/11.10)**: (1) đổi bố cục Web từ **grid 2×2** sang **full-width row + collapsible** (component mới `Collapsible Panel`, xem `00_Foundation/Component_Guidelines.md`) — 4 bước có độ dài nội dung rất khác nhau, ép vào grid gây khoảng trắng vô nghĩa (Mục 11.10); (2) khóa danh sách "Type of evidence" (field 5) thành **5 giá trị** (Mục 11.9.3); (3) root cause (field 9-10) chuyển từ 2 Textarea độc lập sang **1 bản ghi trạng thái Sơ bộ → Chính thức**, tự kế thừa sang `20_GenerateIncidentReportDialog.md` (không nhập lại lần 2) — Mục 11.9.1; (4) thêm nút "Lập biên bản vi phạm nhân viên liên quan" ở panel Root Cause, mở `22_RecordStaffViolation.md` với `relatedIncidentId` pre-fill (Mục 11.5.2); (5) card "1. Thu hồi mẫu lưu" thu gọn mặc định + 1 dòng tóm tắt trạng thái (Mục 11.8).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 0 | Banner "Đang chờ ai" 🆕 | Banner/Alert (đọc, điều kiện) | Chỉ hiện khi đang ở đúng điểm chờ | — | Hiện đúng 1 trong các nhãn theo Mục 7.6: "Đang chờ Kitchen Manager xác minh nhà cung cấp" (E11 Trace Supplier Information), "Đang chờ School Administration duyệt CAPA" (E17), "Đang chờ Kitchen Manager/School Administration triển khai & giám sát CAPA" (E18-19), "Đang chờ School Administration xem xét báo cáo sự cố" (E21) — không hiện khi Medical Staff đang là actor cần hành động (VD đang nhập bằng chứng/root cause) |
| 1 | `Collapsible Panel` "1. Extracting Stored Food Samples" | Panel thu gọn/mở (full-width row) | — | BR-150 | Mở `14_RetrieveFoodSampleRecordsDialog.md` (biến thể ngữ cảnh Incident — Mục 11.6). **Thu gọn mặc định (Mục 11.8)**: tiêu đề card luôn hiện 1 dòng tóm tắt trạng thái (VD "✓ 2 mẫu đã thu hồi (Sữa chua bữa phụ, Canh bí đỏ)" hoặc "Chưa thu hồi mẫu nào"); mở ra hiện bảng đầy đủ mẫu của `incidentId` hiện tại (mã mẫu/món/thời điểm/lab/trạng thái) |
| 2 | `Collapsible Panel` "2. Gathering Evidence" | Panel thu gọn/mở (full-width row) | — | BR-150 | Xem khối dưới |
| 3 | `Collapsible Panel` "3. Identify the Root Cause" | Panel thu gọn/mở (full-width row) | — | BR-150, BR-151 | Xem khối dưới; chứa nút "Lập biên bản vi phạm nhân viên liên quan" (field 10b) |
| 4 | `Collapsible Panel` "4. Proposed Remedial Measures" | Panel thu gọn/mở (full-width row) | — | BR-152 | Xem khối dưới |
| **Khối "Gathering Evidence" (§3.3.2.3.10)** | | | | | |
| 5 | Type of evidence | `Select` (5 giá trị cố định) | — | BR-150 | **Khóa danh sách 15/07/2026 (Mục 11.9.3)**: (1) Lời khai học sinh/giáo viên; (2) Ảnh hiện trường/món ăn còn lại; (3) Kết quả kiểm tra bếp/quy trình (liên kết `NonConformanceRecord`); (4) Giấy tờ y tế/bệnh viện; (5) Khác. Không giới hạn ở "ảnh học sinh ngộ độc tại phòng y tế" vì ca nặng thường chuyển thẳng viện |
| 6 | Brief description | Textarea | — | — | |
| 7 | Attached documents (upload) | Input type="file" + preview | — | BR-150 | |
| 8 | Nút "Save Evidence" | Button (primary) | — | — | |
| **Khối "Identify the Root Cause"** | | | | | |
| 9 | Kết quả xét nghiệm lab | Textarea/File | — | BR-150 | [Giả định — cần team xác nhận cấu trúc field, Mục 5.5 vẫn mở]. Trạng thái điển hình "đang chờ nuôi cấy 48h+" trước khi có kết quả |
| 10 | Kết luận nguyên nhân gốc rễ | Textarea, **1 bản ghi trạng thái Sơ bộ → Chính thức** | — | BR-150, BR-151, BR-152 | **Sửa 15/07/2026 (Mục 11.9.1)** — thay 2 Textarea độc lập (trùng với `20_GenerateIncidentReportDialog.md`) bằng **1 bản ghi duy nhất**: "Kết luận **sơ bộ**" nhập tại đây dựa trên kiểm tra quy trình/truy xuất, đủ căn cứ giao CAPA trong 12h (BR-152, SLA); khi có kết quả nuôi cấy lab (48h+), Medical Staff quay lại xác nhận/cập nhật thành "Kết luận **chính thức**", **tự kế thừa** sang `20_GenerateIncidentReportDialog.md` (không nhập lại lần 2). *(Root cause + CAPA cùng 1 bước theo Normal Flow UC-041; lab bổ sung là AF2, không phải điều kiện tiên quyết)* |
| 10b 🆕 | Nút "Lập biên bản vi phạm nhân viên liên quan" | Button (secondary) | — | BR-185 | **Mới 15/07/2026 (Mục 11.5.2)** — mở `22_RecordStaffViolation.md` với `relatedIncidentId` = `INC-xxx` hiện tại được pre-fill. Dùng khi điều tra xác định nguyên nhân gốc rễ là nhân viên bếp làm sai. Không đổi luồng phê duyệt vi phạm (BR-186 độc lập với CAPA) |
| **Khối "Treatment Measures" (§3.3.2.3.11)** | | | | | |
| 11 | Medical treatment/correction measures | Textarea | — | BR-152 | |
| 12 | Unit/Person in charge | Input (text) | — | BR-152 | |
| 13 | Nút "Establish Measures" | Button (primary) | — | BR-152 | Giao CAPA cho Service Caterers, hạn 12 giờ |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải dữ liệu điều tra hiện có.
- Empty: chưa có bằng chứng/kết luận → trạng thái trống cho từng panel.
- Error: MSG-004, MSG-005, MSG-023.
- Success: lưu từng khối → Toast xác nhận; "Establish Measures" → CAPA được giao, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); "Establish Measures" → quay lại Incident Detail.
**Bố cục (sửa 15/07/2026, đợt 2 — Mục 11.10)**: đổi từ grid 2×2 sang **full-width row + collapsible**. Mỗi bước (1–4) là 1 row chiếm trọn chiều ngang (`Collapsible Panel`, `00_Foundation/Component_Guidelines.md`):
- Trạng thái thu gọn: chỉ tiêu đề + 1 dòng tóm tắt trạng thái (VD card 1 — Mục 11.8).
- Trạng thái mở: chiếm trọn chiều rộng, **tự chia 2 cột bên trong panel** cho field của chính nó (VD card 3 mở ra xếp "Kết quả xét nghiệm lab" | "Kết luận nguyên nhân gốc rễ" cạnh nhau) — tận dụng màn rộng ở cấp field thay vì ép cứng ở cấp 4-panel.
- **Không** giới hạn 1 panel mở tại 1 thời điểm (không phải accordion single-open) — 4 bước không khóa tuần tự cứng với nhau, Medical Staff có thể mở đồng thời "Gathering Evidence" và "Root Cause" để đối chiếu.
- Mặc định: row đang làm dở mở sẵn; row đã hoàn tất thu gọn kèm dấu ✓ + tóm tắt; row chưa tới lượt thu gọn để trống.
- Lý do bổ sung: gần với Mobile (4 Card xếp dọc → trang con), giảm điểm lệch nhất quán Web/Mobile so với grid 2×2 (không có tương đương Mobile).

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile, nút "Step 2: Incident Investigation Space"); "Establish Measures" → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.7 — 4 Card xếp dọc dẫn tới 4 trang con tuần tự.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Investigation Workspace, nền tảng Web, actor Medical Staff.
Bố cục: banner "Đang chờ ai" đầu trang (điều kiện), rồi 4 Collapsible Panel full-width xếp dọc
(1. Extract Sample | 2. Gathering Evidence | 3. Root Cause | 4. Treatment Measures) — thu gọn hiện tiêu đề
+ dòng tóm tắt, mở ra chia 2 cột nội bộ; cho phép mở nhiều panel cùng lúc (không accordion single-open).
Panel Gathering Evidence: Type of evidence (Select 5 giá trị), Brief description, Attached documents, Save Evidence.
Panel Root Cause: Kết quả xét nghiệm lab, Kết luận nguyên nhân gốc rễ (trạng thái Sơ bộ→Chính thức),
nút "Lập biên bản vi phạm nhân viên liên quan".
Panel Treatment Measures: nút Establish Measures.
Áp dụng Design System: primary #0E7490, critical #7E22CE cho Quarantine.
Trạng thái cần thể hiện: loading, empty (mỗi panel), error (MSG-023).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4445–4460 (UC-041), dòng 5826–5943 (§3.3.2.3.7, 3.3.2.3.10, 3.3.2.3.11).
- business_rules.md (BR-146–152, BR-154, BR-185–189).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.5.2 (nút biên bản vi phạm), 11.8 (card 1 thu gọn), 11.9.1 (root cause Sơ bộ→Chính thức), 11.9.3 (Type of evidence 5 giá trị), 11.10 (bố cục collapsible).
- `00_Foundation/Component_Guidelines.md` (component `Collapsible Panel`).
- `22_RecordStaffViolation.md` (đích nút "Lập biên bản vi phạm nhân viên liên quan").

**Ghi chú**: giống bản Mobile — "Collect Evidence"/"Treatment Measures" không có dòng riêng trong Matrix, gộp vào file này.

# Screen Spec — Assign Incident Severity Dialog

> **⚠️ DEPRECATED 15/07/2026** — không còn là dialog/màn hình riêng. UC-009 (Assign Incident Severity) **vẫn giữ nguyên số hiệu và toàn bộ ràng buộc BR-147/BR-148**, nhưng nay được **gộp trình bày** trực tiếp vào `26_IncidentDetail.md` (field 5-6, cùng lúc với "Tường trình ban đầu" + "Chọn món ăn nghi vấn") — bỏ mô hình "hoàn tất Step 1 dialog rồi mới hiện Step 2". Lý do: người dùng phản hồi không muốn tách rời severity thành 1 bước/dialog riêng phải hoàn tất trước khi thấy phần còn lại của màn điều tra. Xem `26_IncidentDetail.md` field 5-6 và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4. Nội dung field bên dưới giữ nguyên để tham chiếu lịch sử — **không xoá file, không dùng để code mới**.

**Actor**: Medical Staff | **UC liên quan**: UC-009 (Report 3 §3.2, dòng 4335–4349) | **Status**: ~~Draft~~ Deprecated
**BR liên quan**: BR-147 (tiêu chí severity: Low/Medium/High), BR-148 (cảnh báo ngay lập tức High), BR-149 (cách ly lô hàng)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Reported symptoms (context, đọc) | Text | — | — | Report 3 §3.3.2.3.6 |
| 2 | Affected student count (context, đọc) | Text | — | — | |
| 3 | Suspected dish information (context, đọc) | Text | — | — | |
| 4 | Severity level | RadioGroup | Bắt buộc chọn: Low/Medium/High | BR-147 | |
| 5 | Clinical Risk Assessment & Recommendations | Textarea | Bắt buộc khi Medium/High | BR-147 | |
| 6 | Nút "Save review & Activate process" | Button (primary) | Disabled khi chưa chọn severity | BR-147, BR-148, BR-149 | |

### Trạng thái màn hình
- Loading: spinner trên nút Save.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: severity High → cảnh báo khẩn cấp ngay (BR-148), Quarantine lô hàng (BR-149), banner `critical`; Low/Medium → lưu bình thường, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); Save → quay lại Incident Detail.
**Bố cục**: `Dialog` — context đọc trên cùng, RadioGroup Severity + Textarea, nút Save/Cancel.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile, "Step 1: Assess the severity"); Save → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.6 — trang riêng full-screen, context trên cùng, RadioGroup + Textarea, nút Save cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Assign Incident Severity Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog, context (symptoms, affected count, suspected dish), RadioGroup Severity level,
Textarea Clinical Risk Assessment, nút Save review & Activate process.
Áp dụng Design System: critical #7E22CE (High), warning #B45309 (Medium), success #15803D (Low).
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
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với `20_GenerateIncidentReportDialog.md`**: từ `26_IncidentDetail.md`, 2 nút tách rời "Validate CAPA đã Approved" (UC-168, màn này) và "Step 3: Generate Incident Report" (UC-033) nay gộp thành **1 điểm vào duy nhất "Hoàn tất sự cố"**. Màn hợp nhất gồm 2 khối tuần tự: **Khối A = nguyên nội dung màn này** (field 1-5 dưới đây, checklist hồi phục học sinh); **Khối B = nguyên nội dung `20_GenerateIncidentReportDialog.md`** (Clinical notes/Lab results/Root cause/Export & Submit) — Khối B chỉ mở khoá sau khi Khối A submit thành công (đúng thứ tự nghiệp vụ: phải xác nhận khắc phục xong mới soạn báo cáo đóng case). **Giữ nguyên UC-168 là 1 use case riêng, không đổi field/BR** — chỉ đổi cách trình bày (không còn điều hướng sang trang/dialog riêng). Xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.7.
> *(Ghi nhận tiện thể, không thuộc phạm vi sửa lần này)*: `20_GenerateIncidentReportDialog.md` hiện trích dẫn MSG-046 làm success message, nhưng nội dung MSG-046 thực tế là "Meal feedback submitted successfully" (thuộc module Feedback, không liên quan Incident) — có khả năng là lỗi trích dẫn từ trước, nên rà soát riêng khi có dịp.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách sự cố có CAPA đã Approved chờ xác nhận y tế | Table | Chỉ hiển thị incident đã qua UC-005 | BR-153 | UC-168 bước 2 |
| 2 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Log sự cố, chi tiết CAPA đã duyệt, student clinical tracking log |
| 3 | Ghi chú kiểm tra bếp | Textarea | Bắt buộc | BR-152 | |
| 4 | Xác nhận học sinh hồi phục 100% | Checkbox theo từng học sinh | Bắt buộc TẤT CẢ = đã hồi phục | BR-155 | EX1: chặn nếu còn triệu chứng |
| 5 | Nút "Submit Validation" | Button (primary) | Disabled khi chưa đủ điều kiện | BR-152, BR-155 | |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách/chi tiết.
- Empty: "Không có sự cố nào đang chờ xác nhận y tế" + illustration.
- Error: MSG-004 (EX1), MSG-005.
- Success: MSG-046 (Toast), trạng thái "Validated - Ready for Closure".

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web, nhánh validate CAPA).
**Bố cục**: `Table` danh sách sự cố chờ xác nhận, chọn dòng mở `Sheet` bên phải (context CAPA + checklist hồi phục + textarea + nút Submit).

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile, nhánh validate CAPA).
**Bố cục**: danh sách `Card` cuộn dọc, chọn item mở trang chi tiết validation.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Corrective Actions Report, nền tảng Web, actor Medical Staff.
Bố cục: Table danh sách sự cố chờ xác nhận, Sheet chi tiết CAPA + checklist hồi phục + nút Submit Validation.
Áp dụng Design System: success #15803D khi đủ điều kiện, danger #B91C1C khi còn triệu chứng.
Trạng thái cần thể hiện: loading, empty, error (MSG-004 EX1), success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4467–4481 (UC-168).
- business_rules.md (BR-152, BR-153, BR-155).
- error_message.md (MSG-004, MSG-005, MSG-046).

# Screen Spec — Generate Incident Report Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-033 (Report 3 §3.2, dòng 4401–4415) | **Status**: Draft
**BR liên quan**: BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với `19_RecordCorrectiveActionsReportScreen.md`**: từ `26_IncidentDetail.md`, nút riêng "Step 3: Submit the incident report" nay gộp vào điểm vào chung **"Hoàn tất sự cố"** cùng với UC-168 (`19_RecordCorrectiveActionsReportScreen.md`). Nội dung màn này trở thành **Khối B** (soạn báo cáo chính thức), chỉ mở khoá sau khi Khối A (checklist hồi phục học sinh, UC-168) submit thành công. Giữ nguyên UC-033 là 1 use case riêng, không đổi field/BR — chỉ đổi cách trình bày. Xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.7.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.4.2/11.9.1)**: (1) bổ sung mục đọc "Biên bản sự cố ban đầu (tham chiếu)" (field 4b) — báo cáo cuối trước đây chỉ có Clinical notes/Lab results/Root cause/Remedial, chưa tham chiếu ngược về biên bản triage ban đầu; (2) field "Root cause determination" (field 8) **không nhập lại** — tự **kế thừa "Kết luận chính thức"** từ `17_InvestigationWorkspace.md` (root cause nay là 1 bản ghi trạng thái Sơ bộ → Chính thức, Mục 11.9.1), chỉ hiển thị đọc/cho phép rà soát, không phải điểm nhập thứ 2.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Incident code | Text (đọc) | — | — | Report 3 §3.3.2.3.8 |
| 2 | Date created | Text (đọc) | — | — | |
| 3 | Severity level | Text/Badge (đọc) | — | BR-147 | |
| 4 | Affected student list/symptoms/ngày khởi phát/suspected dishes/ingredient batches (auto-populate) | Text (đọc) | — | BR-154 | |
| 4b 🆕 | Biên bản sự cố ban đầu (tham chiếu) | Panel/link (đọc) | — | BR-154 | **Mới 15/07/2026 (Mục 11.4.2)** — mục tham chiếu ngược tới biên bản triage đã lập ở `33_CreateIncidentRecordDialog.md` (`IncidentRecordDraft.content`), đưa vào báo cáo cuối để lưu vết đầy đủ chuỗi từ triage → điều tra → báo cáo chính thức. Cùng nguồn với panel "Biên bản gốc" ở `26_IncidentDetail.md` field 4b |
| 5 | Food sample retained | Text (đọc) | — | BR-114 | |
| 6 | Clinical notes | Textarea | Bắt buộc | BR-154 | **Cập nhật 11/07/2026**: pre-fill sẵn từ "Tường trình ban đầu" (`26_IncidentDetail.md` field 5) khi mở màn này lần đầu — Medical Staff vẫn sửa/bổ sung được, không khoá cứng. Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12 |
| 7 | Laboratory sample test results | Textarea/File | Bắt buộc nếu có | BR-154 | |
| 8 | Root cause determination | Text (đọc, **kế thừa**) | — | BR-150, BR-154 | **Sửa 15/07/2026 (Mục 11.9.1)** — **không nhập lại**; tự kế thừa "Kết luận chính thức" từ `17_InvestigationWorkspace.md` field 10 (root cause 1 bản ghi Sơ bộ → Chính thức). Cho phép rà soát/đọc, không phải điểm nhập độc lập thứ 2 (khắc phục trùng lặp Mục 5.3) |
| 9 | Remedial measures (tóm tắt) | Text (đọc) | — | BR-152 | |
| 10 | Nút "Export & Submit Incident Reports" | Button (primary) | Disabled khi thiếu Clinical notes/Root cause | BR-154 | |
| 11 | Nút "Come back" | Button | — | — | Quay lại Investigation Workspace |

### Trạng thái màn hình
- Loading: spinner khi biên soạn PDF + gửi.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: MSG-046 (Toast), trạng thái "Investigation Completed", PDF gửi School Administration.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); thành công → chuyển School Administration Incident Dashboard.
**Bố cục**: `Dialog`/trang con — Incident Report Card tóm tắt, form Clinical notes/Lab results/Root cause, nút "Export & Submit Incident Reports" cuối form.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile); thành công → School Administration Incident Dashboard.
**Bố cục**: Report 3 §3.3.2.3.8 — trang riêng full-screen, Incident Report Card, nút "Export & Submit Incident Reports" cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Generate Incident Report Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog/trang con, Incident Report Card, form Clinical notes/Lab results/Root cause,
nút Export & Submit Incident Reports.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error, success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4401–4415 (UC-033), dòng 5845–5867 (§3.3.2.3.8).
- business_rules.md (BR-114, BR-150, BR-152, BR-154).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-046).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.4.2 (tham chiếu Biên bản sự cố ban đầu), Mục 11.9.1 (root cause kế thừa — đợt 2 15/07/2026).

# Screen Spec — Record Process Violation (UC-178) — `(Deprecated 11/07/2026)`

> **TRẠNG THÁI: DEPRECATED.** UC-178 không còn là màn hình riêng biệt — dialog nhập liệu đã gộp vào `22_RecordStaffViolation.md` (dialog "Lập Biên bản Vi phạm" hợp nhất, mã UC sống sót = **UC-179**). Giữ nguyên số hiệu UC-178 (không tái sử dụng). Chi tiết lý do + cơ chế thay thế: `21_RecordProcessViolation.md`, `review/medical_staff/review_screen_medical_staff.md` Mục 10.

# Screen Spec — Record Violation (Dialog hợp nhất — thay thế Record Process Violation UC-178 + Record Staff Violation UC-179)

**Actor**: Medical Staff (dùng chung với Service Caterers theo BR-183/184) | **UC liên quan**: UC-179 - Record Staff Violation (Report 3 §3.2, dòng 4639–4659) — **mã UC sống sót sau khi gộp UI với UC-178, quyết định 11/07/2026**. UC-178 - Record Process Violation (Report 3 §3.2, dòng 3663–3683) — **Deprecated 11/07/2026**, xem `21_RecordProcessViolation.md` | **Status**: Draft
**BR liên quan**: BR-183 (vi phạm quy trình/sản phẩm — kích hoạt khi field "Nhân viên liên quan" để trống), BR-184 (vi phạm nhân viên — kích hoạt khi có điền), BR-185, BR-186, BR-187 (cảnh báo nghi ngờ tái diễn 90 ngày), BR-188 (audit log bất biến), BR-190 (báo cáo lạm dụng manual override), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký — sửa 11/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error), MSG-086 (Success), MSG-088 (Warning)

> **Quyết định thiết kế 07/07/2026, chốt cuối 11/07/2026** (`review/medical_staff/review_screen_medical_staff.md` Mục 10): người dùng chỉ ra dialog UC-178 và UC-179 gần như giống hệt nhau, trong khi mẫu biên bản giấy thực tế (`00_Policies/specialreport2173_1.md`) không hề phân biệt 2 loại vi phạm. Quyết định: **giữ tách BR-183/BR-184 ở tầng dữ liệu** (nguồn gốc phát sinh khác nhau — BR-183 từ `NonConformanceRecord` tự động/audit, BR-184 từ nhập tay độc lập; khi `NonConformanceRecord` được "nâng cấp" theo BR-183, hệ thống tạo 1 bản ghi `StaffViolationRecord` tương ứng) nhưng **gộp UI thành 1 dialog duy nhất** — không có toggle chọn "loại vi phạm", BR-183 hay BR-184 được suy ra tự động từ việc có điền field "Nhân viên liên quan" (field 6) hay không. **UC-179/`StaffViolationRecord` là mã "sống sót"**, `21_RecordProcessViolation.md` nay là file tham chiếu.
> **Cập nhật 11/07/2026 — bỏ hẳn field nhân chứng**: hệ thống không track thông tin người chứng kiến dưới bất kỳ hình thức nào (không `witnessId`, không text tự do). PDF xuất ra vẫn giữ nguyên cột chữ ký thứ 3 "Người chứng kiến" (khớp mẫu giấy gốc — trong ví dụ thật cũng để trống) nhưng đây là khoảng trống tĩnh trên PDF, không gắn field dữ liệu nào.
> **Cập nhật 11/07/2026 — đổi tên tiêu đề PDF**: từ "BIÊN BẢN SỰ VIỆC" (nguyên văn mẫu giấy gốc) → **"BIÊN BẢN VI PHẠM"** — hệ thống chỉ dùng mẫu này cho trường hợp vi phạm.
> **Lý do không bắt buộc quy trách nhiệm cá nhân**: mục đích cốt lõi của biên bản là Nhà trường (Medical Staff) ghi nhận lỗi của Service Caterers cấp công ty, 2 bên ký xác nhận cùng chịu trách nhiệm — không bắt buộc quy cho 1 cá nhân. Field "Nhân viên liên quan" (field 6) chỉ dùng khi Medical Staff **chủ động muốn** kích hoạt thêm kỷ luật cá nhân (BR-184/185/187).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Địa điểm xảy ra sự việc (`locationOfIncident`) | Input text | Bắt buộc, không rỗng | BR-211 | Mẫu giấy: "Hôm nay, tại: ___" |
| 2 | Đại diện Y tế trường học (`reportedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản đăng nhập hiện tại | BR-183/184 | Mẫu giấy: "Đại diện P.VSATTP" |
| 3 | Đại diện Nhà thầu (`caterersRepresentativeId`) | Select (tài khoản Service Caterers đang hoạt động) | Bắt buộc | BR-211 | Mẫu giấy: "Đại diện nhà thầu"; liên kết tài khoản có thật, không nhập tay tự do |
| 4 | Nhân viên liên quan (`staffName`) | Input text tự do (không phải khóa ngoại — nhân viên catering không có tài khoản `User`) | Tùy chọn — để trống nếu không quy trách nhiệm cho 1 cá nhân cụ thể | BR-184 | Không có trên mẫu giấy. **Trống = chỉ áp BR-183** (vi phạm quy trình/sản phẩm thuần túy); **có giá trị = kích hoạt thêm BR-184/185/187** (kỷ luật cá nhân) — xem Mục "Cơ chế phân loại" bên dưới |
| 5 | Chức vụ nhân viên (`staffPosition`) | Input text tự do | Tùy chọn; chỉ hiện trong UI khi field 4 đã có ký tự đầu tiên (progressive disclosure, không phải validation bắt buộc) | BR-184 | Không có trên mẫu giấy |
| 5b 🆕 | Sự cố liên quan (`relatedIncidentId`) | Select/link (tùy chọn) | Tùy chọn | BR-185 | **Mới 15/07/2026 (Mục 11.5.1)** — liên kết chéo tới 1 `FoodSafetyIncident` (`INC-xxx`) khi vi phạm phát sinh từ điều tra sự cố (VD điều tra xác định nguyên nhân gốc rễ là nhân viên bếp làm sai). Tái sử dụng đúng pattern context-linking đã có cho `NonConformanceRecord` — Incident là ngữ cảnh mở thứ 3. **Pre-fill sẵn** khi mở dialog từ nút "Lập biên bản vi phạm nhân viên liên quan" ở `17_InvestigationWorkspace.md` (panel Root Cause). **Không** đổi luồng phê duyệt vi phạm: BR-186 (LOW tự đóng, MEDIUM/HIGH → School Admin) vẫn độc lập hoàn toàn với track CAPA (BR-153) — chỉ thêm tham chiếu chéo dữ liệu |
| 6 | Mức độ nghiêm trọng (`severityLevel`) | Select (LOW/MEDIUM/HIGH) | Bắt buộc | BR-183/184, BR-186 | Không có trên mẫu giấy — là điều kiện tự động phân luồng nút "Đóng biên bản"/"Gửi School Administration phê duyệt" ở `24_RecordViolationResolution.md` |
| 7 | Loại vi phạm (`violationType`, gồm `OVERRIDE_ABUSE`) | Select (không phải text tự do — tránh hỏng khả năng lọc) | Tùy chọn | BR-190 | Không có trên mẫu giấy — giữ Select để BR-190 (báo cáo lạm dụng manual override hàng tuần) lọc được chính xác. Danh sách 4 giá trị: "Bỏ qua bước quy trình bắt buộc", "Vi phạm vệ sinh cá nhân", "Lạm dụng quyền ghi đè hệ thống (`OVERRIDE_ABUSE`)", "Hành động không được ủy quyền" — cần đội dự án xác nhận trước khi khóa cứng enum trong code |
| 8 | Thời điểm xảy ra sự việc (`occurredAt`) | DateTimePicker | Bắt buộc | BR-211 | Dùng để hệ thống tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày]..." trong nội dung PDF, khớp mẫu giấy |
| 9 | **Nội dung biên bản** (`violationContent`, thay cho "Mô tả vi phạm" cũ) | Rich text editor (toolbar: đậm/nghiêng/gạch chân, cỡ chữ, màu chữ, bảng, căn lề, chèn ảnh/link) | Bắt buộc, không rỗng | BR-183/184 | Mẫu giấy: phần văn xuôi tự do sau câu "Vào hồi..." — nằm ngay sau câu tự sinh từ field 8, người dùng viết tự do đúng như cách viết biên bản giấy thật; MSG-085 nếu thiếu |
| 10 | Ảnh bằng chứng | Camera/Upload, nhiều ảnh | Bắt buộc ít nhất 1 ảnh | BR-183/184 | MSG-085 nếu thiếu |
| 11 | Nút "Save Violation" | Button (primary) | Disabled khi thiếu field bắt buộc (1, 3, 6, 8, 9, 10) | BR-183/184, BR-188 | Lưu dữ liệu số ngay — **không chờ** có ảnh biên bản đã ký (BR-211) |
| 12 | Nút "Xuất Biên bản (PDF)" | Button (secondary) | Chỉ hiện sau khi đã Save Violation | BR-211 | Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**; sinh câu kết cố định + 3 khoảng chữ ký (Y tế trường học / Nhà thầu / Người chứng kiến — cột thứ 3 luôn để trống tĩnh, không gắn field) — xem Mục "Xuất & Lưu Biên bản đã ký" bên dưới |
| 13 | Ảnh biên bản đã ký (`signedRecordPhotoUrl`) | Upload | Không bắt buộc để Save tại đây; **bắt buộc** trước khi đóng biên bản/gửi School Administration phê duyệt ở `24_RecordViolationResolution.md` — **gate bắt buộc (chặn cứng)**, quyết định người dùng 06/07/2026, không đổi | BR-211 | Tải lên sau khi in — ký tay 2 bên (Y tế trường học/Nhà thầu) — chụp ảnh |

### Cơ chế phân loại BR-183 vs BR-184 (không có toggle thủ công)
- Field 4 (`staffName`) để trống → bản ghi chỉ chịu **BR-183** (vi phạm quy trình/sản phẩm) — không có cá nhân để áp kỷ luật, field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` **không hiện**.
- Field 4 có giá trị → kích hoạt thêm **BR-184/185** (kỷ luật cá nhân — field "Mức độ hình thức kỷ luật" hiện ra ở `24_RecordViolationResolution.md`) và **BR-187** (cảnh báo nghi ngờ tái diễn — so khớp theo đúng chuỗi `staffName` trong 90 ngày gần nhất, best-effort vì `staffName` là text tự do).
- Field 3 (`caterersRepresentativeId`) và field 1/8/9/10 luôn bắt buộc bất kể field 4 có điền hay không — đây là phần "khung biên bản" chung cho mọi trường hợp.
- Khi mở dialog từ 1 `NonConformanceRecord` đã có sẵn (nâng cấp theo BR-183 — ví dụ từ bước kiểm thực 3 bước Không đạt), 1 số field (địa điểm, thời điểm, mô tả sơ bộ) có thể pre-fill từ dữ liệu `NonConformanceRecord` liên kết; khi mở độc lập (Medical Staff tự phát hiện), toàn bộ field trống, nhập từ đầu.
- **Ngữ cảnh thứ 3 — mở từ điều tra sự cố (15/07/2026, Mục 11.5)**: khi mở từ nút "Lập biên bản vi phạm nhân viên liên quan" (`17_InvestigationWorkspace.md`, panel Root Cause), field 5b (`relatedIncidentId`) được pre-fill = `INC-xxx` đang điều tra; các field khác nhập từ đầu (hoặc gợi ý từ nội dung điều tra nếu có).

### Trạng thái màn hình
- **Loading**: spinner trên nút Save Violation khi đang lưu; rà soát trùng tên (best-effort, BR-187) khi field 4 có giá trị.
- **Empty**: không áp dụng (dialog tạo mới).
- **Error**: MSG-004/MSG-005 (lỗi hệ thống chung); MSG-085 khi thiếu field bắt buộc — cảnh báo inline ngay tại field thiếu.
- **Success**: MSG-086 (Toast) khi Save thành công. Nếu field 4 có giá trị và trùng khớp với 1 bản ghi khác cùng mức ≥ MEDIUM trong 90 ngày gần nhất → thêm MSG-088 (banner cảnh báo nghi ngờ tái diễn, best-effort, không chặn lưu — BR-187). Điều hướng tiếp sang `24_RecordViolationResolution.md`. Nếu chưa tải `signedRecordPhotoUrl`, màn 24 sẽ **chặn cứng** nút "Đóng biên bản"/"Gửi phê duyệt" (MSG-095, Error) cho đến khi bổ sung ảnh.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm", hoặc từ 1 `NonConformanceRecord` Không đạt (nâng cấp theo BR-183); thành công → `24_RecordViolationResolution.md`.
**Bố cục**: `Dialog` 1 cột, thứ tự đúng như bảng field ở trên. Field 5 (chức vụ nhân viên) chỉ render khi field 4 có giá trị (client-side, không cần gọi API). Ô "Nội dung biên bản" (field 9) chiếm toàn bộ chiều rộng dialog, chiều cao tối thiểu ~200px, có toolbar định dạng cố định ngay phía trên ô, câu tự sinh từ `occurredAt` hiển thị dạng text tĩnh ngay trên đầu ô, không nằm trong vùng chỉnh sửa được. Nút Save Violation cuối form; nút Xuất Biên bản (PDF) xuất hiện cạnh Save Violation sau khi lưu thành công.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Report Violation" trên Dashboard (Mobile), hoặc từ 1 `NonConformanceRecord` Không đạt; thành công → `24_RecordViolationResolution.md`.
**Bố cục**: trang riêng full-screen, camera (field 10) ưu tiên đặt đầu form theo quy ước cũ của dự án, form dọc, nút Save Violation cố định cuối màn. **Lưu ý cần xác nhận thêm**: toolbar rich text đầy đủ (bảng, căn lề, màu chữ) trên màn hình mobile hẹp có thể không khả thi — đề xuất rút gọn toolbar mobile chỉ còn đậm/nghiêng/gạch chân + danh sách gạch đầu dòng, đội dự án/dev xác nhận trước khi code.

---

## Xuất & Lưu Biên bản đã ký (BR-211, sửa 11/07/2026)

**Bước 1 — Xuất PDF**: sau khi Save Violation thành công, nút "Xuất Biên bản (PDF)" sinh 1 file PDF theo đúng bố cục mẫu `00_Policies/specialreport2173_1.md`, với các điều chỉnh đã chốt:
- Header: tên trường (từ `CURRENT_SCHOOL`) + "Y tế trường học", quốc hiệu tiêu ngữ, tiêu đề **"BIÊN BẢN VI PHẠM"** (đổi từ "BIÊN BẢN SỰ VIỆC" nguyên văn mẫu giấy, 11/07/2026).
- Địa điểm (`locationOfIncident`) + thời điểm lập biên bản (server timestamp lúc bấm "Xuất Biên bản").
- 3 bên trên PDF: "Đại diện Y tế trường học" (`reportedBy`), "Đại diện Nhà thầu" (`caterersRepresentativeId`), "Người chứng kiến" (**cột trống tĩnh, không gắn field dữ liệu nào** — sửa 11/07/2026, trước đây gắn `witnessId`).
- Nội dung sự việc: câu tự sinh "Vào hồi [giờ] ngày [ngày]..." từ `occurredAt` + nội dung biên bản (field 9, rich text).
- Câu kết cố định: "Biên bản được lập thành 01 bản, lưu tại hệ thống SFMS, có giá trị pháp lý như nhau." (điều chỉnh câu chữ chính thức cần đội dự án duyệt).
- 3 khoảng trống chữ ký: "Y TẾ TRƯỜNG HỌC" / "NHÀ THẦU" / "NGƯỜI LÀM CHỨNG" (cột thứ 3 để trống, điền tay ngoài đời nếu cần).

**Bước 2 — In & ký tay**: Medical Staff tải file PDF về, in ra giấy, thu thập chữ ký tay ngoài đời thực (không dùng ký điện tử trên màn hình).

**Bước 3 — Chụp ảnh & tải lên**: quay lại màn hình, dùng field "Ảnh biên bản đã ký" (field 13) để tải ảnh biên bản giấy đã ký. Ảnh này lưu vào `signedRecordPhotoUrl`, gắn với đúng bản ghi vi phạm.

**Trạng thái hiển thị**: badge "Tình trạng biên bản" — *Chưa xuất PDF* → *Đã xuất, chờ ký* → *Đã có ảnh ký xác nhận* (dựa trên việc đã có `signedRecordPhotoUrl` hay chưa).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho dialog "Lập Biên bản Vi phạm", nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên].
Thứ tự field: Địa điểm xảy ra sự việc, Đại diện Y tế trường học (hiển thị tự động, không sửa),
Đại diện Nhà thầu (Select) — KHÔNG có field Người chứng kiến trong form (đã bỏ 11/07/2026),
Nhân viên liên quan (tùy chọn) — khi có giá trị thì hiện thêm Chức vụ nhân viên,
Mức độ nghiêm trọng (Select LOW/MEDIUM/HIGH), Loại vi phạm (Select, tùy chọn),
Thời điểm xảy ra sự việc (DateTimePicker), khối "Nội dung biên bản" là rich text editor có toolbar
định dạng (đậm/nghiêng/gạch chân/cỡ chữ/bảng/căn lề), phía trên ô hiện sẵn dòng tự sinh
"Vào hồi [giờ] ngày [ngày]..." lấy từ Thời điểm xảy ra sự việc, ảnh bằng chứng (bắt buộc ít nhất 1),
nút Save Violation, sau khi lưu hiện thêm nút "Xuất Biên bản (PDF)" (tiêu đề PDF "BIÊN BẢN VI PHẠM").
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM).
Trạng thái cần thể hiện: loading, error (MSG-085 tại field thiếu), success (MSG-086, cảnh báo MSG-088
nếu có Nhân viên liên quan và nghi ngờ tái diễn).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm toggle chọn "loại vi phạm" —
BR-183/BR-184 được suy ra tự động từ việc có điền "Nhân viên liên quan" hay không.
KHÔNG thêm field nhân chứng (witness) trong form nhập liệu.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4639–4659 (UC-179 Record Staff Violation), dòng 3663–3683 (UC-178 Record Process Violation — nay Deprecated, tham khảo lịch sử).
- business_rules.md §18 (BR-183, BR-184, BR-185, BR-186, BR-187, BR-188, BR-190, BR-211).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-086, MSG-088, MSG-095).
- `02_Requirements/Violation_Management_Proposal.md`.
- `00_Policies/specialreport2173_1.md` (mẫu Biên bản Sự việc — căn cứ thiết kế field).
- `review/medical_staff/review_screen_medical_staff.md` Mục 10 (toàn bộ lịch sử quyết định gộp dialog).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.5.1 (field `relatedIncidentId` — liên kết Incident ↔ Violation, đợt 2 15/07/2026).

# Screen Spec — Compliance Dashboard

**Actor**: Medical Staff (màn hình dùng chung với School Administration — file này viết riêng theo góc nhìn Medical Staff) | **UC liên quan**: UC-027 (Report 3 §3.2, dòng 3469–3483) | **Status**: Draft
**BR liên quan**: BR-091, BR-097, BR-100, BR-104
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-093 (Warning — cảnh báo điểm checklist thấp)

> **Cập nhật 05/07/2026**: đội dự án quyết định **bỏ field "Cảnh báo Kitchen Quarantine"** (field #6 cũ) khỏi màn hình này. Field đó vốn trích `BR-105` (ghost-citation — canonical là quy tắc lưu mẫu thực phẩm, không liên quan) và `MSG-024` (theo `error_message.md`, mã này thuộc module Incident Management, dùng cho BR-149 — cách ly lô nguyên liệu khi có sự cố, không phải ngưỡng điểm checklist). Quyết định descope: điểm checklist <60% (UC-014) **không còn khóa vận hành bếp** — chỉ tự động cảnh báo School Administration (xem `business_rules.md` §12, ghi chú sau BR-208). Đã thay field #6 bằng field #4b — Alert cảnh báo điểm thấp (không phải banner khóa cố định). Nếu về sau có một sự cố ATTP thực sự kích hoạt cách ly **lô nguyên liệu** (BR-149), cảnh báo đó thuộc phạm vi UC-041 (Incident Management), không phải màn hình này.
>
> **Quyết định 12/07/2026 (rà soát ảnh chụp màn hình thực tế, Hình 6)**: `compliance/page.tsx` hiện có thêm 1 card "Nhiệt độ kho & tủ lưu mẫu (BR-089/BR-110)" — **card này KHÔNG có trong đặc tả màn hình gốc** (không thuộc danh sách field dưới đây), phát sinh ngoài phạm vi khi triển khai code, đồng thời còn dẫn BR-089 đã Deprecated. Quyết định: **KHÔNG đưa card này vào đặc tả** — dữ liệu nhiệt độ tủ lưu mẫu (BR-110) đã có chỗ đúng phạm vi tại `15_ViewFoodSafetyReportsScreen.md` (nhóm báo cáo "Refrigerator Temperature Logs"), không cần lặp lại ở Compliance Dashboard. Xem `review_screen_medical_staff.md` Mục 16.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trạng thái real-time 3 bước kiểm tra hôm nay | Widget trạng thái | — | BR-091 | UC-027 bước 2 |
| 2 | Điểm checklist vệ sinh hàng ngày | Widget số liệu | — | BR-100, BR-104 | UC-027 bước 3 |
| 3 | Ảnh xác minh (verification photos) | Ảnh preview | — | — | UC-027 bước 4 |
| 4 | Cảnh báo bước kiểm tra thất bại/quá hạn | Alert | Hiện khi có non-conformance | BR-097 | AF1 |
| 4b | Cảnh báo điểm checklist <60% *(thay thế field Quarantine cũ, 05/07/2026)* | Alert (warning, không phải banner cố định) | Hiện khi điểm gần nhất <60%; đã tự động gửi cảnh báo School Administration | — | Không khóa vận hành bếp, không chặn phân phát bữa ăn — descope so với AF1 gốc UC-014 |
| 5 | Nút "Xem chi tiết cảnh báo" | Button | — | — | |

### Trạng thái màn hình
- Loading: `Skeleton` cho các widget số liệu.
- Empty: "Chưa có dữ liệu sản xuất hôm nay".
- Error: MSG-004, MSG-005; điểm <60% → Alert cảnh báo (không phải banner cố định, MSG-093).
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: Content Area grid 12 cột — widget trạng thái 3 bước + điểm checklist hàng trên, ảnh xác minh + cảnh báo (non-conformance, điểm thấp) hàng dưới.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard (Mobile).
**Bố cục**: 1 cột, widget xếp dọc dạng Card, Alert cảnh báo (không phải banner cố định) nếu có non-conformance/điểm thấp.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Dashboard, nền tảng Web, actor Medical Staff.
Bố cục: grid 12 cột, trạng thái 3 bước kiểm tra, điểm checklist, ảnh xác minh,
cảnh báo bước thất bại, Alert cảnh báo điểm checklist <60% (không phải banner khóa).
Áp dụng Design System: warning #B45309 cho Alert điểm thấp/bước thất bại.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3469–3483 (UC-027).
- business_rules.md (BR-091, BR-097, BR-100, BR-104; xem §12 ghi chú sau BR-208 về quyết định descope khóa bếp).
- error_message.md (MSG-004, MSG-005; MSG-093 §3 Warning; MSG-024 không áp dụng cho màn này — thuộc Incident Management).

**Ghi chú màn hình dùng chung**: giống bản Mobile — **đã hợp nhất** với bản `Web/SchoolAdministration/18_ComplianceDashboard.md` ở bước review (mục 6 của `00_Team_Work_Assignment_Plan.md`, 2026-07-01) — field dữ liệu #1-#4 (3-bước realtime, điểm vệ sinh, ảnh xác minh, cảnh báo non-conformance) nay đồng nhất ở cả 2 bản; hành động riêng theo actor (School Administration: "Ký xác nhận"; Medical Staff: "Xem chi tiết cảnh báo") giữ nguyên khác biệt. **Cập nhật 05/07/2026**: banner Kitchen Quarantine (MSG-024/BR-105) đã bị gỡ khỏi cả 2 bản — xem ghi chú đầu file; mã cảnh báo thay thế: **MSG-093**.

**Cập nhật 05/07/2026 — bổ sung US-09 (BR-209, UC-185)**: xem `28_SendComplianceReportDialog.md` — Medical Staff nay có thể chủ động soạn/gửi báo cáo tuân thủ ATTP (Daily/Weekly) cho School Administration từ menu, không cần chỉ dựa vào School Administration tự vào xem màn Compliance Dashboard.

# Screen Spec — Record Violation Resolution

**Actor**: Medical Staff (Primary Actor theo UC-180; màn hình dùng chung với School Administration — School Admin chỉ xem lại/duyệt qua UC-182, xem `Web/SchoolAdministration/46_RecordViolationResolution.md`) | **UC liên quan**: UC-180 - Record Violation Resolution (Report 3 §3.2, dòng 4661–4681 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo với bản School Administration, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-185 (biên bản xử lý sai phạm), BR-186 (phân cấp phê duyệt), BR-188 (audit log bất biến), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký 3 bên — mới, 06/07/2026, xem ghi chú field 9)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error — thiếu field bắt buộc), MSG-087 (Success), MSG-089 (Success — chỉ áp dụng khi School Admin duyệt, xem bản của họ), MSG-095 (Error — chặn đóng/gửi duyệt khi chưa có ảnh biên bản đã ký)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Vi phạm gốc liên kết (process hoặc staff conduct) | Text hiển thị + link | — | BR-183, BR-184 | Hợp nhất từ bản School Administration; link sang chi tiết `27_ViolationRegistry.md` |
| 2 | Tóm tắt biên bản vi phạm (context, đọc) | Text | — | — | Kế thừa từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` |
| 3 | Hành động xử lý (actionTaken) | Textarea | Bắt buộc | BR-185 | MSG-085 nếu thiếu |
| 4 | Người xử lý (handledBy) | Text (auto) | — | BR-185 | |
| 5 | Mức độ hình thức kỷ luật (disciplinaryLevel) | Select | Bắt buộc khi vi phạm nhân viên — Nhắc nhở/Cảnh cáo/Đình chỉ/Đề xuất chấm dứt | BR-185 | |
| 6 | Trạng thái (Pending Approval/Approved/Closed) | Badge (warning/success) | — | BR-186 | Hợp nhất từ bản School Administration, khớp field `status` (`ViolationResolutionRecord`) |
| 7 | Nút "Đóng biên bản" (LOW) | Button (primary) | Chỉ khả dụng khi LOW; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Hover/tap khi disabled → tooltip/toast MSG-095 |
| 8 | Nút "Gửi School Administration phê duyệt" (MEDIUM/HIGH) | Button (primary) | Bắt buộc khi ≥ MEDIUM; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Hover/tap khi disabled → tooltip/toast MSG-095 |
| 9 | Badge "Tình trạng biên bản đã ký" (dựa trên `signedRecordPhotoUrl` của biên bản gốc, BR-211) | Badge (danger/success) — "Chưa có ảnh ký — chưa thể đóng/gửi duyệt" / "Đã có ảnh ký xác nhận" | Hiển thị luôn để giải thích lý do nút 7/8 bị disable | BR-211 | Có link/nút phụ "Quay lại tải ảnh biên bản" điều hướng về `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (field ảnh biên bản đã ký) |

### Trạng thái màn hình
- Loading: spinner khi lưu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; thiếu field bắt buộc → cảnh báo inline; **chưa có `signedRecordPhotoUrl` → nút 7/8 disabled, MSG-095 (Error) khi cố thao tác — chặn cứng theo BR-211 (quyết định người dùng 06/07/2026)**.
- Success: LOW → MSG-089; MEDIUM/HIGH → MSG-087, quay lại `27_ViolationRegistry.md`. Điều kiện tiên quyết cho cả 2 nhánh: đã có `signedRecordPhotoUrl`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (Web); thành công → `27_ViolationRegistry.md`.
**Bố cục**: `Dialog`/trang con — tóm tắt biên bản, form Hành động xử lý + Mức kỷ luật, nút hành động tương ứng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (Mobile); thành công → `27_ViolationRegistry.md`.
**Bố cục**: trang riêng full-screen, tóm tắt trên cùng, form bên dưới, nút cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Violation Resolution, nền tảng Web, actor Medical Staff.
Bố cục: Dialog, link vi phạm gốc, tóm tắt biên bản, Hành động xử lý, badge Trạng thái, Select mức kỷ luật, nút Đóng biên bản/Gửi phê duyệt.
Áp dụng Design System: success #15803D, warning #B45309.
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-087/MSG-089).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4661–4681 (Table 3-161, UC-180 Record Violation Resolution — Normal Flow đầy đủ).
- business_rules.md §18 (BR-185, BR-186, BR-188, BR-211 — mới 06/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-087, MSG-089, MSG-095 — mới 06/07/2026).

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/46_RecordViolationResolution.md` — cả 2 bản thống nhất: Medical Staff là actor **lập** biên bản xử lý (Normal Flow UC-180 bước 1-6), School Administration chỉ **xem lại/duyệt** (qua UC-182, không tự lập). Field dữ liệu 2 bản khớp nhau hợp lý.

# Screen Spec — Incident List

**Actor**: Medical Staff (màn hình dùng chung với School Administration — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-114 (Report 3 §3.2, dòng 4555–4569) | **Status**: Draft

> **Viết lại toàn bộ 12/07/2026** — bản trước mô tả model 2-tab cũ ("Processing"/"Resolved"), đã lỗi thời so với thiết kế lại 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3, đã áp code thử) và code thật (`incidents/page.tsx`). Bản này viết lại theo đúng model **3-tab** hiện hành, đồng thời bổ sung quyết định mới 12/07/2026 (nút "Xem chi tiết" ở tab "Chờ tiếp nhận" — phát hiện qua rà soát ảnh chụp code thật, xem §9.13 review file).
> **Cập nhật 15/07/2026 — mô hình 4 tab + biên bản sự cố thủ công**: bổ sung tab mới "Biên bản chờ xử lý" (giữa "Chờ tiếp nhận" và "Đang xử lý"), đổi hành động "Xác nhận liên quan ATTP" thành mở dialog soạn biên bản (`33_CreateIncidentRecordDialog.md`) thay vì tạo `INC-xxx` ngay lập tức — đảo ngược quyết định 09/07/2026 (Mục 7.8). Đồng thời siết lại nội dung modal "Xem chi tiết" cho khớp đúng field của `Teacher/12_IncidentForm.md`. Chi tiết quyết định: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.2)**: modal "Xem chi tiết" `RPT-xxx` **phóng to theo pattern "Dialog cỡ lớn"** (cùng cỡ đã dùng ở `33_CreateIncidentRecordDialog.md`), bố cục **2 cột**, thay cho modal nhỏ trước đó. Vẫn giữ nguyên là `Dialog` (không tách route/Screen Spec riêng vì `RPT-xxx` là dữ liệu tạm, chỉ tồn tại tới khi merge/close — xem Mục 11.2 review file). Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.2.

**BR liên quan**: BR-146 (ngưỡng gộp tự động ≥3 HS/24h), BR-147 (severity), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-100 (Success), MSG-101 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Cấu trúc 4 tab (cập nhật 15/07/2026)

| Tab | Nội dung | Cột/Field | Filter | Hành động |
| --- | --- | --- | --- | --- |
| **Chờ tiếp nhận** | Danh sách `RPT-xxx` (báo cáo thô của Teacher/Parent), `status = PENDING_TRIAGE` hoặc `DRAFTING` (đang soạn nháp, xem tab kế) | Checkbox chọn, Mã (`RPT-xxx`), Học sinh, Lớp, Triệu chứng, Bữa ăn/Món nghi vấn, Thời điểm báo cáo, Người báo cáo, **nút "Xem chi tiết" mỗi dòng**, badge "Đang soạn biên bản (nháp)" nếu `status = DRAFTING` | Theo lớp, loại triệu chứng, ngày báo cáo (**không** filter severity — chưa có ở giai đoạn này) | Thanh hành động khi ≥1 dòng chọn: **"Tạo báo cáo sự cố"** (≥1 dòng, đổi tên từ "Xác nhận liên quan ATTP" 15/07/2026 — mở `33_CreateIncidentRecordDialog.md` thay vì tạo `INC-xxx` ngay) / **"Đóng — Không liên quan ATTP"** (đúng 1 dòng — tránh đóng nhầm hàng loạt). **"Xem chi tiết"**: mở modal read-only (xem cấu trúc field ở mục riêng bên dưới) |
| **Biên bản chờ xử lý** 🆕 | Danh sách biên bản đã "Hoàn tất" (`RPT-xxx.status = DRAFTED`, từ `33_CreateIncidentRecordDialog.md`) | Mã báo cáo nguồn (chip, có thể nhiều), Nội dung biên bản (rút gọn, "Xem đầy đủ" mở lại read-only), Người lập, Thời điểm lập | Theo ngày lập | 2 nút mỗi dòng: **"Đóng — Không liên quan ATTP"** (`status = CLOSED_NOT_FOOD_SAFETY`, không tạo `INC-xxx`) / **"Mở phiên điều tra"** (tạo `INC-xxx` mới, `caseCode` gắn chung với biên bản, nội dung biên bản pre-fill sang "Tường trình ban đầu" ở `26_IncidentDetail.md`, chuyển `status = MERGED`) |
| **Đang xử lý** | Danh sách `INC-xxx` đã tạo/gộp, trạng thái 1–5 (state machine tiến độ) | Mã case & tiêu đề, Status Badge (1 trong 5 nhãn theo trạng thái thực tế: "Chờ đánh giá mức độ" / "Đang xử lý sơ cứu" / "Đang điều tra" / "Chờ duyệt CAPA" / "Đang triển khai CAPA"), Triệu chứng, Số học sinh ảnh hưởng | Severity Level, Status (BR-147) | Chọn 1 dòng → `26_IncidentDetail.md` |
| **Đã đóng** | Cả `RPT-xxx` đóng trực tiếp (không liên quan ATTP) lẫn `INC-xxx` đã đóng (điều tra hoàn tất) | + cột "Loại hồ sơ" (Báo cáo/`RPT` — Sự cố/`INC`), "Lý do đóng" (Không liên quan ATTP / Đã hoàn tất điều tra) | — | Chọn 1 dòng `INC-xxx` → `26_IncidentDetail.md` (read-only); dòng `RPT-xxx` không điều hướng, chỉ xem qua "Xem chi tiết" nếu cần |
| Nút "Export" | — | — | — | Xuất CSV/PDF danh sách đang hiển thị |

### Trạng thái `RPT-xxx` — enum mở rộng (15/07/2026)

`PENDING_TRIAGE` → (chọn "Tạo báo cáo sự cố") → **`DRAFTING`** (đang soạn nháp, có thể "Lưu bản nháp" nhiều lần) → **`DRAFTED`** (đã "Hoàn tất biên bản", sang tab "Biên bản chờ xử lý") → `MERGED` (mở phiên điều tra) hoặc `CLOSED_NOT_FOOD_SAFETY` (đóng). Riêng nhánh đóng trực tiếp từ tab "Chờ tiếp nhận" (không qua biên bản) vẫn giữ nguyên như cũ: `PENDING_TRIAGE` → `CLOSED_NOT_FOOD_SAFETY`. *(Ghi nhận để người dùng tự áp dụng vào class diagram — theo đúng quy ước đã dùng ở Mục 7.5 review file, không tự sửa class diagram ở đây.)*

### Cơ chế gộp N-1 (tab "Chờ tiếp nhận" → "Biên bản chờ xử lý" → "Đang xử lý", cập nhật 15/07/2026)

1. Tick chọn 1 hoặc nhiều dòng `RPT-xxx` (chỉ dòng `status = PENDING_TRIAGE`).
2. Bấm **"Tạo báo cáo sự cố"** → mở `33_CreateIncidentRecordDialog.md` (dialog soạn biên bản thủ công, không còn là modal xác nhận suông như bản cũ). Nếu N>1, khối context union hiển thị cảnh báo nếu ngày ăn không khớp giữa các RPT — không chặn cứng, Medical Staff tự quyết khi viết biên bản.
3. Trong dialog: Medical Staff tự viết "Nội dung biên bản" (field 5, `33_CreateIncidentRecordDialog.md`), có thể **"Lưu bản nháp"** nhiều lần (`status = DRAFTING`) trước khi **"Hoàn tất biên bản"** (`status = DRAFTED`).
4. Biên bản đã "Hoàn tất" xuất hiện ở tab **"Biên bản chờ xử lý"** — Medical Staff xem lại, quyết định 1 trong 2: "Đóng — Không liên quan ATTP" (`status = CLOSED_NOT_FOOD_SAFETY`, không tạo `INC-xxx`) hoặc **"Mở phiên điều tra"**.
5. "Mở phiên điều tra" → tạo `INC-xxx` mới, `sourceReportIds` = các báo cáo đã gộp, `caseCode` gắn chung với biên bản vừa soạn, nội dung biên bản trở thành pre-fill cho "Tường trình ban đầu" (`26_IncidentDetail.md`); các `RPT-xxx` liên quan chuyển `status = MERGED`. Điều hướng thẳng sang `26_IncidentDetail.md` của `INC-xxx` vừa tạo.
6. "Đóng — Không liên quan ATTP" ngay từ tab "Chờ tiếp nhận" (không qua biên bản, chỉ N=1): giữ nguyên như cũ — `status = CLOSED_NOT_FOOD_SAFETY`, không tạo biên bản, không tạo `INC-xxx`.

### Nút "Xem chi tiết" — field khớp đúng `Teacher/12_IncidentForm.md` (siết lại 15/07/2026; phóng to 2 cột — Mục 11.2)

- Vùng click tách biệt với checkbox chọn dòng (không kích hoạt lẫn nhau).
- **Dạng hiển thị (Mục 11.2)**: `Dialog` cỡ lớn (không tách route riêng — `RPT-xxx` là dữ liệu tạm), bố cục **2 cột**:
  - **Cột trái**: Học sinh (multi), Lớp (đọc), Số học sinh ảnh hưởng, Món ăn nghi vấn (chọn từ thực đơn ngày), Ngày ăn (Ingestion Date), Người báo cáo, Thời điểm báo cáo.
  - **Cột phải**: Triệu chứng phát hiện (checkbox, gồm "Other"), Mô tả sự cố (toàn văn, không cắt bớt), Ảnh đính kèm (nếu có, dạng gallery).
- Nội dung hiển thị **đúng và chỉ** các field đã có ở `Teacher/12_IncidentForm.md`.
- **Riêng field "Mức độ nghiêm trọng"**: theo Mục 7.2 (`review_medicalstaff_incidentmanagement.md`, đã chốt 09/07/2026), Teacher không có quyền tự gán severity — modal hiển thị **"Chưa đánh giá"** cho tới khi Medical Staff chính thức đánh giá ở `26_IncidentDetail.md`. *(Ghi nhận: `12_IncidentForm.md` thực tế chưa được sửa theo quyết định này — khoảng trống riêng, ngoài phạm vi màn này.)*
- Không có hành động sửa/xoá trong modal — chỉ để Medical Staff tham khảo đầy đủ nội dung trước khi quyết định gộp/đóng.
- Lý do bổ sung: bảng chính chỉ hiển thị field rút gọn (phù hợp cho quét nhanh nhiều dòng), không đủ để đọc mô tả dài trước khi ra quyết định.

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/card.
- Empty: "Không có báo cáo/sự cố nào" theo từng tab + illustration.
- Error: MSG-004, MSG-005, MSG-023; race condition khi 1 báo cáo đã bị người khác xử lý trong lúc đang mở modal gộp → "Một số báo cáo đã được xử lý, vui lòng tải lại danh sách".
- Success: Toast "Đã tạo phiên điều tra [mã INC-xxx]" (gộp) / "Đã đóng báo cáo [mã RPT-xxx]" (đóng).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Sự cố & Điều tra" trên Dashboard (Web); "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md` (Web); "Mở phiên điều tra" hoặc chọn dòng ở tab "Đang xử lý"/"Đã đóng" (INC) → `26_IncidentDetail.md` (Web).
**Bố cục**: Page Header 4 Tab + filter, tab "Chờ tiếp nhận" dùng `Table` với cột checkbox đầu tiên + thanh hành động nổi (sticky) ở dưới cùng khi có dòng được chọn; tab "Biên bản chờ xử lý" dùng `Table` với 2 nút hành động cuối mỗi dòng; 2 tab còn lại giữ layout Table thông thường. Nút "Export" góc trên phải.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Sự cố & Điều tra" trên Dashboard (Mobile); "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md` (Mobile); "Mở phiên điều tra" hoặc chọn item (INC) → `26_IncidentDetail.md` (Mobile).
**Bố cục**: AppBar, 4 Tab, tab "Chờ tiếp nhận" dùng danh sách `Card` cuộn dọc + chế độ "Chọn" (long-press/icon multi-select góc trên) thay cho checkbox permanent; tab "Biên bản chờ xử lý" dùng danh sách `Card` với 2 nút cuối mỗi Card; thanh hành động nổi ở đáy màn hình khi có card được chọn; pull-to-refresh.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Incident List, nền tảng Web, actor Medical Staff.
Bố cục: Page Header 4 Tab ("Chờ tiếp nhận" / "Biên bản chờ xử lý" / "Đang xử lý" / "Đã đóng") + filter, nút Export góc trên phải.
Tab "Chờ tiếp nhận": Table với checkbox đầu mỗi dòng, cột Mã/Học sinh/Lớp/Triệu chứng/Bữa ăn-Món nghi vấn/
Thời điểm báo cáo/Người báo cáo, nút "Xem chi tiết" cuối mỗi dòng (vùng click riêng, không lẫn checkbox),
thanh hành động nổi cuối bảng khi có dòng chọn ("Tạo báo cáo sự cố" / "Đóng — Không liên quan ATTP").
Tab "Biên bản chờ xử lý": Table các biên bản đã hoàn tất, cột Mã RPT nguồn/Nội dung rút gọn/Người lập/Thời điểm,
2 nút cuối mỗi dòng ("Đóng — Không liên quan ATTP" / "Mở phiên điều tra").
Tab "Đang xử lý": Table các sự cố INC-xxx, Status Badge theo 5 trạng thái tiến độ.
Tab "Đã đóng": Table gồm cả RPT lẫn INC đã đóng, thêm cột Loại hồ sơ + Lý do đóng.
Áp dụng Design System: warning #B45309.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4555–4569 (UC-114), dòng 5758–5782 (§3.3.2.3.4).
- business_rules.md (BR-146, BR-147, BR-154, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-100, MSG-101).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3 (thiết kế 3-tab, 09/07/2026), §9.13 (nút Xem chi tiết, 12/07/2026), Mục 10 (mô hình 4-tab + biên bản thủ công, 15/07/2026).

**Ghi chú màn hình dùng chung**: xem `Web/SchoolAdministration/09_IncidentList.md` cho góc nhìn School Administration — cùng dữ liệu `INC-xxx`, khác cách trình bày bộ lọc; tab "Chờ tiếp nhận" (RPT-xxx) là đặc thù riêng của Medical Staff (School Administration không xử lý báo cáo thô).

# Screen Spec — Incident Detail

**Actor**: Medical Staff (màn hình dùng chung với School Administration + Teacher — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-116, gộp trình bày UC-009 (Report 3 §3.2, dòng 4577–4591) | **Status**: Draft
**BR liên quan**: BR-146–155
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/10_IncidentDetail.md` — bổ sung field #6 "Bản đồ truy xuất nguồn gốc" (BR-151) và #7 "Danh sách lô hàng bị Quarantine" (BR-149), trước đây thiếu ở bản này. Giữ nguyên toàn bộ hành động riêng của Medical Staff (Step 1/2/3, Validate CAPA). Ghi chú: theo review, Teacher **không có** file Incident Detail riêng — xem giải thích ở bản School Administration.
> **Cập nhật 11/07/2026**: field 5 đổi tên "First aid was administered" → "Tường trình ban đầu", mở rộng thành Textarea nhập bắt buộc ở trạng thái TREATMENT (trước đây Text chỉ đọc) — quyết định sau khi rà soát cho thấy Medical Staff cần ghi lại diễn biến sớm thay vì chỉ viết Clinical notes ở cuối quy trình (UC-033). Không tạo UC/BR/màn hình mới. Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12.
> **Cập nhật 15/07/2026 — 3 thay đổi lớn** (xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4/10.5/10.7): (1) gộp "Đánh giá mức độ nghiêm trọng" (trước đây dialog riêng `18_AssignIncidentSeverityDialog.md`/UC-009) vào thẳng màn này, cùng lúc với "Tường trình ban đầu" + "Chọn món ăn nghi vấn" — bỏ mô hình "hoàn tất Step 1 dialog rồi mới hiện Step 2"; (2) "Chọn món ăn nghi vấn" nay mặc định lọc theo món chế biến trong 48 giờ (căn cứ Quyết định 39/2006/QĐ-BYT), có nút "Mở rộng phạm vi ngày"; (3) gộp "Validate CAPA đã Approved" (UC-168) + "Step 3: Generate Incident Report" (UC-033) thành 1 điểm vào "Hoàn tất sự cố" duy nhất.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.4/11.5/11.6/11.7)**: (1) thêm panel đọc "Biên bản gốc" (field 4b) hiển thị xuyên suốt vòng đời, trỏ `IncidentRecordDraft.content` — vì sau khi biên bản pre-fill sang "Tường trình ban đầu" (sửa được), văn bản gốc mất định danh riêng (Mục 11.4.1); (2) thêm panel đọc "Vi phạm liên quan" (field 11b) từ trạng thái `INVESTIGATING` trở đi, liệt kê `StaffViolationRecord`/`NonConformanceRecord` có `relatedIncidentId` = sự cố hiện tại, link sang `27_ViolationRegistry.md` (Mục 11.5.3); (3) giới hạn "Mở rộng phạm vi ngày" (field 8) ở đúng **72h** — ngưỡng còn khả năng thu hồi mẫu thật (BR-111/BR-112), món rơi vào trạng thái `Disposed` hiển thị mờ + "Link Disposed Sample to Incident" (Mục 11.6); (4) bản đồ truy xuất nguồn gốc (field 10) **thu gọn mặc định** + scope theo lô đã khóa (`quarantineBatchIds[]`), hiển thị **lồng nhau** — mỗi dòng lô bị khóa mở ra đúng chuỗi truy xuất của lô đó (Mục 11.7, đề xuất — cần đội dự án xác nhận khi cập nhật BR-151).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Case code | Text (đọc) | — | — | Report 3 §3.3.2.3.5 |
| 2 | Clinical symptoms | Text (đọc) | — | — | |
| 3 | Affected students | Text (đọc) | — | BR-146 | |
| 4 | Suspected food item + meal time | Text (đọc) | — | — | |
| 4b 🆕 | Biên bản gốc (tham chiếu) | Panel/link (đọc) | — | BR-154 | **Mới 15/07/2026 (Mục 11.4.1)** — hiển thị **xuyên suốt vòng đời** sự cố (không chỉ lúc pre-fill), trỏ tới `IncidentRecordDraft.content` (biên bản đã soạn ở `33_CreateIncidentRecordDialog.md`, mở phiên điều tra thành sự cố này). Mục đích: giữ khả năng mở lại đúng **nguyên văn** biên bản triage ban đầu, vì nội dung đó đã pre-fill sang "Tường trình ban đầu" (field 7, sửa được) nên văn bản gốc mất định danh riêng |
| 5 | Severity level | `RadioGroup`, bắt buộc | Bắt buộc chọn: Low/Medium/High | BR-147 | **Gộp vào màn này 15/07/2026** — trước đây là dialog riêng `18_AssignIncidentSeverityDialog.md` (nay Deprecated, xem file đó). Nguyên field/validation của UC-009, chỉ đổi vị trí trình bày |
| 6 | Đánh giá rủi ro lâm sàng | Textarea | Bắt buộc khi Medium/High | BR-147 | Gộp từ UC-009, xuất hiện cùng lúc với field 5 |
| 7 | Tường trình ban đầu (trước đây "First aid was administered") | Textarea nhập, bắt buộc trước khi "Lưu đánh giá & Bắt đầu điều tra" (đổi từ Text đọc — xem `review_medicalstaff_incidentmanagement.md` Mục 9.12, 11/07/2026) | Bắt buộc | — | Mô tả đầy đủ diễn biến ban đầu: phát hiện lúc nào, triệu chứng, đã sơ cứu gì. **Pre-fill từ nội dung "biên bản sự cố"** đã soạn ở `33_CreateIncidentRecordDialog.md` khi mở lần đầu (Mục 10.3/10.4) — trước đó chỉ pre-fill xuôi sang Clinical notes (UC-033), nay có thêm nguồn pre-fill ngược từ biên bản triage. Medical Staff vẫn sửa được. Field dùng chung với School Administration (`Web/SchoolAdministration/10_IncidentDetail.md` field 4) |
| 8 | Chọn món ăn nghi vấn | `Select` multi, bắt buộc | Bắt buộc trước khi "Lưu đánh giá & Bắt đầu điều tra"; lấy từ đúng Menu/`MealServiceSession` của "Ngày ăn" đã xác nhận | BR-149 | **Mặc định lọc theo món chế biến trong 48 giờ** tính từ thời điểm biên bản/báo cáo được tạo — căn cứ Quyết định 39/2006/QĐ-BYT (`00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`), bổ sung 15/07/2026. Kèm nút **"Mở rộng phạm vi ngày"** để Medical Staff tự nới rộng khi nghi ngờ tác nhân ủ bệnh lâu hơn (không chặn cứng). **Giới hạn 15/07/2026 (đợt 2, Mục 11.6)**: "Mở rộng phạm vi ngày" chỉ nới tới **72h** — ngưỡng lưu mẫu tối thiểu BR-111 (`business_rules.md`), sau đó mẫu có thể đã bị hủy (BR-112). Món có mẫu đã `Disposed` hiển thị **mờ (disabled)** kèm hành động "Link Disposed Sample to Incident" (nhánh AF1 của UC-076) thay vì giả vờ vẫn thu hồi được. Chọn xong → Quarantine kích hoạt ngay đúng lô của (các) món được chọn (BR-149) |
| 9 | Nút "Lưu đánh giá & Bắt đầu điều tra" | Button (primary) | Disabled khi thiếu field 5/7/8 | BR-147, BR-148, BR-149 | **Gộp 15/07/2026** — thay cho 2 hành động tách rời (Step 1 dialog riêng → rồi mới thấy Step 2). Severity High → banner Emergency Response (E7) ngay khi lưu |
| 10 | Bản đồ truy xuất nguồn gốc (nếu có) | Panel `Collapsible` (thu gọn mặc định) | Biên soạn trong 4 giờ | BR-151 | Hợp nhất từ bản School Administration. **Sửa 15/07/2026 (đợt 2, Mục 11.7)**: mặc định **thu gọn** (progressive disclosure — Medical Staff cần chủ yếu để giải trình với School Admin/CDC, không phải theo dõi lâm sàng liên tục); **scope theo lô đã khóa** (`quarantineBatchIds[]` từ `suspectedDishIds[]`) thay vì toàn sự cố; hiển thị **lồng nhau** — gộp chung với field 11, mỗi dòng "Lô bị khóa" mở rộng ra đúng chuỗi truy xuất của lô đó. *(Đề xuất — cần đội dự án xác nhận khi cập nhật BR-151 vì ảnh hưởng cách tự động hoá truy xuất.)* |
| 11 | Danh sách lô hàng bị Quarantine | Table rút gọn (mỗi dòng mở rộng ra chuỗi truy xuất của lô — lồng với field 10) | — | BR-149 | Hợp nhất từ bản School Administration. Từ 15/07/2026 (đợt 2) hiển thị **lồng** với bản đồ truy xuất (Mục 11.7) thay cho 2 panel rời |
| 11b 🆕 | Vi phạm liên quan | Panel/Table đọc (hiển thị từ trạng thái `INVESTIGATING` trở đi) | — | BR-185, BR-189 | **Mới 15/07/2026 (Mục 11.5.3)** — liệt kê các `StaffViolationRecord`/`NonConformanceRecord` có `relatedIncidentId` = sự cố hiện tại (link chéo Epic 3 ↔ Epic 4, xem `22_RecordStaffViolation.md` field mới `relatedIncidentId`), mỗi dòng link sang `27_ViolationRegistry.md`. **Không** đổi luồng phê duyệt — BR-186/BR-153 vẫn 2 track độc lập, chỉ thêm tham chiếu chéo dữ liệu |
| 12 | Nút "Step 2: Incident Investigation Space" | Button (hành động riêng Medical Staff) | Chỉ Medical Staff | BR-150 | → `17_InvestigationWorkspace.md` |
| 13 | Nút "Hoàn tất sự cố" | Button (hành động riêng Medical Staff) | Hiện khi CAPA đã được School Admin duyệt (E17) | BR-152, BR-153, BR-154, BR-155 | **Gộp 15/07/2026** — thay cho 2 nút tách rời "Validate CAPA đã Approved" (`19_RecordCorrectiveActionsReportScreen.md`/UC-168) và "Step 3: Generate Incident Report" (`20_GenerateIncidentReportDialog.md`/UC-033). Mở màn hợp nhất gồm 2 khối tuần tự: (A) xác nhận khắc phục/hồi phục học sinh (UC-168), (B) soạn báo cáo chính thức (UC-033) — khối B chỉ mở khoá sau khi khối A hoàn tất. Xem ghi chú hợp nhất ở 2 file đó |
| 14 | Nút "Export" | Button | — | — | |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải chi tiết.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `25_IncidentList.md` (Web).
**Bố cục**: 2 cột — thông tin sự cố bên trái (`col-span-8`), action list (Step 1/2/3, Validate, Export) bên phải (`col-span-4`).

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Card trên `25_IncidentList.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.3.5 — AppBar, Section Header, block thông tin xếp dọc, 3 nút Step xếp dọc bên dưới.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Incident Detail, nền tảng Web, actor Medical Staff.
Bố cục: 2 cột — thông tin sự cố + severity RadioGroup + đánh giá rủi ro lâm sàng + Tường trình ban đầu +
Chọn món ăn nghi vấn (multi-select, mặc định 48h, nút Mở rộng phạm vi) bên trái, action list
(Lưu đánh giá & Bắt đầu điều tra, Step 2 Investigation, Hoàn tất sự cố, Export) bên phải.
Áp dụng Design System: primary #0E7490, critical #7E22CE (High severity).
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4577–4591 (UC-116), dòng 5783–5807 (§3.3.2.3.5), dòng 4335–4349 (UC-009).
- business_rules.md (BR-146–155, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md` (căn cứ khung 48h).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4/10.5/10.7 (gộp severity, khung 48h, gộp Bước 4 — 15/07/2026); Mục 11.4 (panel Biên bản gốc), Mục 11.5.3 (panel Vi phạm liên quan), Mục 11.6 (giới hạn 72h + Disposed), Mục 11.7 (scope + lồng ghép bản đồ truy xuất — đợt 2 15/07/2026).

**Ghi chú màn hình dùng chung**: giống bản Mobile — hợp nhất field dữ liệu ở bước review, giữ riêng hành động theo actor.

# Screen Spec — Create Incident Record Dialog 🆕

**Actor**: Medical Staff | **UC liên quan**: UC-114 (mở rộng, xem `Report 3.md` dòng 4555–4569 — không có trong bản gốc, bổ sung 15/07/2026) | **Status**: Draft
**BR liên quan**: BR-146 (ngưỡng gộp), BR-154 (báo cáo sự cố chính thức — áp dụng sớm hơn ở giai đoạn triage)
**MSG liên quan**: MSG-023 (Error, tái sử dụng), MSG-100 (Success — lưu nháp), MSG-101 (Success — hoàn tất)

> **Mới 15/07/2026** — bổ sung theo phản hồi người dùng (3 ảnh chụp code thật, "Sự cố & Điều tra"). Đảo ngược quyết định 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.8, Câu hỏi 1 — khi đó chốt "gộp làm 1 bước, không tách 2 bước" vì cho rằng tách ra là dư thừa). Bước giữa mới này **không phải** xác nhận suông như bản gốc mà là **soạn thảo 1 văn bản chính thức** (biên bản), nên không còn là bước dư thừa. Xem Mục 10 cùng file review.
> Cấu trúc field rút ra từ mẫu thực tế `00_Policies/specialreport2173_1.md` ("Biên bản Sự việc") — cùng phương pháp đã dùng cho BR-211 (biên bản vi phạm).
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.3)**: (a) bổ sung field đọc "Ảnh minh chứng (kế thừa từ báo cáo nguồn)" — union `evidencePhotoUrls[]` từ các `RPT-xxx` đã chọn, chỉ để tham khảo khi viết (không upload thêm ở đây); (b) nâng field "Nội dung biên bản" từ `Textarea` trơn lên **rich text editor** (cùng toolbar `22_RecordStaffViolation.md` field 9) để nhất quán thuật ngữ "biên bản". **Không** áp cơ chế gate ký-ảnh BR-211 (in — ký tay — chụp ảnh) cho biên bản sự cố này: đây là tài liệu triage nội bộ, không phải văn bản pháp lý cuối cùng; báo cáo chính thức cuối cùng là UC-033 (BR-154) xuất PDF ở bước "Hoàn tất sự cố". Xem Mục 11.3 file review.

## Business Logic (dùng chung mọi nền tảng)

### Điểm vào
Từ tab "Chờ tiếp nhận" của `25_IncidentList.md`: tick chọn 1 hoặc nhiều dòng `RPT-xxx` (`status = PENDING_TRIAGE`) → bấm nút **"Tạo báo cáo sự cố"** (đổi tên từ "Xác nhận liên quan ATTP" — hành động cũ giờ tách thành 2 bước, xem ghi chú đảo ngược ở trên).

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tiêu đề biên bản + thời gian/địa điểm lập | Text (đọc, tự động) | — | — | "Biên bản sự cố an toàn thực phẩm" + timestamp server tại thời điểm mở dialog + tên trường (từ hồ sơ trường) — cùng cấu trúc phần đầu `specialreport2173_1.md`, không nhập tay |
| 2 | Người lập biên bản | Text (đọc, tự động) | — | — | = tài khoản Medical Staff đăng nhập (nhất quán `reportedBy`/BR-183, "Đại diện Y tế trường học"/BR-211) |
| 3 | Các báo cáo nguồn (`RPT-xxx`) | Danh sách chip/link (đọc) | ≥1 | — | Mã các báo cáo thô đã tick chọn, mỗi chip mở lại modal "Xem chi tiết" (`25_IncidentList.md`) khi bấm |
| 4 | Khối context tự động (union) | Card (đọc) | — | BR-146 | Học sinh liên quan (union), Triệu chứng (union), Món ăn/bữa ăn nghi vấn (union, tham khảo — không phải field chính thức dùng để Quarantine, xem Mục 7.2/7.7 review file) — giữ để Medical Staff đối chiếu khi viết |
| 4b 🆕 | Ảnh minh chứng (kế thừa từ báo cáo nguồn) | Gallery (đọc) | — | BR-146 | Union `evidencePhotoUrls[]` từ các `RPT-xxx` đã chọn (field 10 `Teacher/12_IncidentForm.md`), chỉ để **tham khảo** khi viết biên bản. **Không** phải chỗ upload ảnh mới — nơi Medical Staff tự thêm bằng chứng đúng vị trí là panel "Gathering Evidence" (`17_InvestigationWorkspace.md`, BR-150), tránh 2 nguồn bằng chứng rời rạc (Mục 11.3.1) |
| 5 | Nội dung biên bản | **Rich text editor** (cùng toolbar `22_RecordStaffViolation.md` field 9: đậm/nghiêng/gạch chân, cỡ chữ, màu chữ, bảng, căn lề, chèn ảnh/link), bắt buộc | Không được để trống | BR-154 | Medical Staff **tự viết** phần tường trình tổng hợp; pre-fill gợi ý ban đầu = ghép nối mô tả của các RPT đã chọn (cùng cơ chế pre-fill đã dùng ở "Tường trình ban đầu" → Clinical notes), toàn quyền sửa/viết lại. Nâng từ `Textarea` trơn lên rich text để nhất quán thuật ngữ "biên bản" — cùng nguồn cấu trúc `specialreport2173_1.md` với biên bản vi phạm (Mục 11.3.2). **Không** áp gate ký-ảnh BR-211 (Mục 11.3.3) |
| 6 | Nút "Lưu bản nháp" | Button (secondary) | Không validate nội dung rỗng | — | Lưu đúng nội dung đang có, đóng dialog, **không chuyển tab**; các `RPT-xxx` liên quan chuyển `status = DRAFTING`, hiện badge "Đang soạn biên bản (nháp)" ở tab "Chờ tiếp nhận"; mở lại nút "Tạo báo cáo sự cố"/"Tiếp tục soạn" trên đúng các RPT đó sẽ load lại dialog với nội dung đã lưu |
| 7 | Nút "Hoàn tất biên bản" | Button (primary) | Disabled khi field 5 rỗng (MSG-023) | BR-154 | Validate nội dung không rỗng → lưu, các `RPT-xxx` liên quan chuyển `status = DRAFTED`, biên bản xuất hiện ở tab mới "Biên bản chờ xử lý" (`25_IncidentList.md`) |
| 8 | Nút "Hủy" | Button (ghost) | — | — | Đóng dialog, không lưu gì (khác "Lưu bản nháp") |

### Trạng thái màn hình
- Loading: spinner trên nút đang bấm (Lưu bản nháp/Hoàn tất biên bản).
- Empty: không áp dụng.
- Error: MSG-023 (nội dung biên bản rỗng khi bấm "Hoàn tất biên bản").
- Success: MSG-100 (Toast, "Lưu bản nháp") / MSG-101 (Toast, "Hoàn tất biên bản") — đóng dialog, quay lại `25_IncidentList.md`, tab tương ứng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ tab "Chờ tiếp nhận" (`25_IncidentList.md`, Web); Lưu bản nháp/Hoàn tất/Hủy → quay lại `25_IncidentList.md`.
**Bố cục**: `Dialog` cỡ lớn — khối context đọc (field 1-4b, gồm gallery ảnh union) phía trên, rich text editor nội dung biên bản (field 5) với thanh toolbar định dạng chiếm phần lớn chiều cao, 3 nút (Hủy | Lưu bản nháp | Hoàn tất biên bản) cố định cuối dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ tab "Chờ tiếp nhận" (`25_IncidentList.md`, Mobile); Lưu bản nháp/Hoàn tất/Hủy → quay lại danh sách.
**Bố cục**: trang riêng full-screen (không phải Dialog nổi, do nội dung dài) — context đọc xếp dọc trên cùng, Textarea nội dung biên bản, 3 nút xếp dọc cố định cuối màn (Hoàn tất biên bản trên cùng làm primary, Lưu bản nháp giữa, Hủy dưới cùng dạng text link).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Create Incident Record Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog lớn, khối context đọc (thời gian/địa điểm, người lập, các RPT nguồn, union học sinh/triệu chứng/món ăn,
gallery ảnh minh chứng kế thừa từ báo cáo nguồn — chỉ đọc), rich text editor "Nội dung biên bản" bắt buộc (toolbar định dạng
đậm/nghiêng/gạch chân, cỡ chữ, màu, bảng, căn lề, chèn ảnh/link), 3 nút cuối dialog: Hủy (ghost) | Lưu bản nháp (secondary) | Hoàn tất biên bản (primary).
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (viền đỏ Textarea khi rỗng), success (toast).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `00_Policies/specialreport2173_1.md` (cấu trúc biên bản tham khảo).
- `business_rules.md` BR-146, BR-154, Changelog v1.8.
- `error_message.md` MSG-023, MSG-100, MSG-101.
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.8 (quyết định gốc bị đảo ngược), Mục 10.3 (quyết định mới), Mục 11.3 (ảnh union + rich text editor).
- `22_RecordStaffViolation.md` field 9 (rich text editor tham chiếu toolbar).

**Ghi chú màn hình dùng chung**: giống bản Mobile — cùng field/logic, chỉ khác bố cục Dialog vs trang riêng.

# Screen Spec — Violation Registry

**Actor**: Medical Staff (màn hình dùng chung với School Administration — Primary Actor theo UC-181 là School Administration, Medical Staff là Secondary Actor; file này viết theo góc nhìn Medical Staff, phạm vi xem có thể hẹp hơn bản School Administration) | **UC liên quan**: UC-181 - View Violation Registry (Report 3 §3.2, dòng 4683–4703 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-189 (danh sách tổng hợp mọi loại vi phạm), BR-187 (cảnh báo nghi ngờ tái diễn), BR-214 (báo cáo tổng hợp vi phạm theo kỳ), BR-178 (định dạng xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 13/07/2026**: bổ sung field 4 "Filter theo khoảng ngày" và field 9 "Nút Export" — khoảng trống phát hiện khi thiết kế BR-214 (Send Violation Summary Report, UC-189): màn này trước đây chưa có Export, khác với các màn danh sách khác đã có sẵn (`25_IncidentList.md`, `15_ViewFoodSafetyReportsScreen.md`). Export tôn trọng đúng filter đang áp dụng (đồng bộ nguyên tắc Nhóm D, xem `review_screen_medical_staff.md` Mục 11). Xem Mục 17.
> **Sửa 15/07/2026 (đợt 2 — Mục 11.5.4)**: bổ sung cột **"Sự cố liên quan"** (hiển thị `INC-xxx` nếu vi phạm có `relatedIncidentId`) và **filter theo Sự cố liên quan** — hệ quả của quyết định liên kết chéo Incident ↔ Violation (Mục 11.5). Cho phép Medical Staff/School Admin lọc ra các vi phạm phát sinh từ 1 sự cố cụ thể.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo loại vi phạm (quy trình/nhân viên/đối tác) | Select | — | BR-189 | |
| 2 | Filter theo mức độ (LOW/MEDIUM/HIGH) | Select | — | BR-186 | |
| 3 | Filter theo trạng thái (Pending/Approved/Closed) | Select | — | BR-186 | |
| 4 | Filter theo khoảng ngày | Date range picker | — | — | **Mới 13/07/2026** |
| 4b 🆕 | Filter theo Sự cố liên quan | Select/Search (`INC-xxx`) | — | BR-185 | **Mới 15/07/2026 (Mục 11.5.4)** — lọc các vi phạm có `relatedIncidentId` khớp 1 sự cố cụ thể |
| 5 | Bảng danh sách vi phạm | Table | — | BR-189 | Cột: Loại vi phạm, người/đối tượng liên quan, mức độ, trạng thái, ngày lập, **Sự cố liên quan** (`INC-xxx` nếu có `relatedIncidentId`, link sang `26_IncidentDetail.md` — mới 15/07/2026, Mục 11.5.4) |
| 6 | Severity Badge (mỗi dòng) | Badge màu | Danger (HIGH)/Warning (MEDIUM)/Secondary (LOW) | BR-186 | |
| 7 | Cảnh báo nghi ngờ tái diễn (nếu có) | Badge/Icon | — | BR-187 | MSG-088 |
| 8 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Điều hướng chế độ xem tương ứng |
| 9 | Nút "Export" | Button | — | BR-178 | **Mới 13/07/2026** — xuất CSV/PDF đúng theo filter đang áp dụng (loại/mức độ/trạng thái/khoảng ngày), không xuất toàn bộ dữ liệu |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng.
- Empty: "Chưa có vi phạm nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: Bảng/danh sách hiển thị đầy đủ vi phạm trong phạm vi Medical Staff được xem, cảnh báo tái diễn (MSG-088) hiện dưới dạng badge/icon cạnh dòng liên quan.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm"; chọn dòng → xem chi tiết.
**Bố cục**: thanh filter (loại/mức độ/trạng thái/khoảng ngày) phía trên `Table`, nút "Export" góc trên phải, phân trang dưới bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu điều hướng chung (Mobile); chọn item → xem chi tiết.
**Bố cục**: 1 cột, filter `Chip`/`DropdownButton`, danh sách `Card` cuộn dọc, nút "Export" nổi AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Violation Registry, nền tảng Web, actor Medical Staff.
Bố cục: thanh filter (loại/mức độ/trạng thái/khoảng ngày/sự cố liên quan), Table danh sách vi phạm với Severity Badge
+ cột "Sự cố liên quan" (INC-xxx), nút Export góc trên phải.
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM), secondary #64748B (LOW).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4683–4703 (Table 3-162, UC-181 View Violation Registry — Normal Flow đầy đủ, Primary Actor School Administration, Secondary Medical Staff).
- business_rules.md §18 (BR-186, BR-187, BR-189), §16 (BR-178, BR-214 — mới 13/07/2026).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.5.4 (cột + filter Sự cố liên quan — đợt 2 15/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-088).

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/47_ViolationRegistry.md` — field hiển thị dữ liệu khớp nhau hợp lý (filter Type/Severity/Status, bảng danh sách, badge severity, cảnh báo tái diễn). Không phát hiện field còn thiếu. **[Giả định — cần team xác nhận]**: BR-189 không nêu rõ ràng buộc phạm vi dữ liệu Medical Staff được xem trong Registry (toàn bộ hay chỉ vi phạm tự lập) — cần team xác nhận trước khi sang bước `06_Wireframes/`.

# Screen Spec — ATTP Report Center

**Actor**: Medical Staff | **UC liên quan**: UC-187 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-175 (phân quyền xem báo cáo theo vai trò)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Bổ sung 13/07/2026**: màn hub mới, gom 1 điểm vào chung cho 3 loại báo cáo ATTP mà Medical Staff có thể chủ động soạn và gửi cho School Administration — trước đây chỉ có 1 loại (UC-185, vào từ `23_ComplianceDashboard.md`). Phát sinh từ phản hồi người dùng (rà soát ảnh chụp màn hình, "Hình 7") khi nhận thấy: (1) dữ liệu sự cố và vi phạm cũng là mảng Medical Staff quản lý nhưng chưa có kênh báo cáo chủ động theo kỳ; (2) chưa có nơi xem lại các báo cáo đã gửi. Không thay thế UC-185 — chỉ là điểm vào bổ sung, giữ nguyên UC-185 vẫn có thể vào trực tiếp từ Compliance Dashboard như cũ. Xem `business_rules.md` BR-213/BR-214, Changelog v1.7; `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card "Báo cáo Giám sát tuân thủ" | Card hành động | — | BR-209 | Mô tả ngắn: điểm kiểm thực 3 bước, checklist vệ sinh, côn trùng, sức khỏe NV. Nút "Soạn & Gửi" → `28_SendComplianceReportDialog.md` (UC-185) |
| 2 | Card "Báo cáo Tổng hợp Sự cố" | Card hành động | — | BR-213 | Mô tả ngắn: số sự cố theo trạng thái/mức độ trong kỳ. Nút "Soạn & Gửi" → `30_SendIncidentSummaryDialog.md` (UC-188) |
| 3 | Card "Báo cáo Tổng hợp Vi phạm" | Card hành động | — | BR-214 | Mô tả ngắn: số vi phạm theo loại/mức độ trong kỳ. Nút "Soạn & Gửi" → `31_SendViolationSummaryDialog.md` (UC-189) |
| 4 | Lần gửi gần nhất (mỗi card) | Text nhỏ (đọc) | "Chưa gửi lần nào" nếu chưa có | BR-177 | VD: "Gửi gần nhất: 12/07/2026 (Weekly)" |
| 5 | Nút "Xem lịch sử báo cáo đã gửi" | Button (link) | — | BR-177 | → `32_SentReportsHistory.md` (UC-190) |

### Trạng thái màn hình
- Loading: `Skeleton` cho 3 card.
- Empty: không áp dụng (3 card luôn hiển thị, chỉ khác trạng thái "Chưa gửi lần nào").
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn điều hướng).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu Sidebar "Báo cáo ATTP" (mới); mỗi card dẫn tới dialog soạn báo cáo tương ứng; nút "Xem lịch sử" dẫn tới `32_SentReportsHistory.md`.
**Bố cục**: Page Header đơn giản, Content Area 3 Card xếp ngang (`col-span-4` mỗi card) hàng trên, nút "Xem lịch sử báo cáo đã gửi" ở góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu điều hướng chung (Mobile) "Báo cáo ATTP"; chọn Card → mở dialog soạn báo cáo tương ứng.
**Bố cục**: 1 cột, 3 Card xếp dọc, nút "Xem lịch sử báo cáo đã gửi" cuối danh sách card (dạng link/button phụ).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình ATTP Report Center, nền tảng Web, actor Medical Staff.
Bố cục: Page Header, 3 Card hành động xếp ngang (Giám sát tuân thủ / Tổng hợp Sự cố / Tổng hợp Vi phạm),
mỗi card có mô tả ngắn + lần gửi gần nhất + nút "Soạn & Gửi", nút "Xem lịch sử báo cáo đã gửi" góc trên phải.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-175, BR-213, BR-214), §12 (BR-209).
- `error_message.md` (MSG-004, MSG-005).
- `traceability_matrix.md` (UC-187, mới 13/07/2026).
- `review/medical_staff/review_screen_medical_staff.md` Mục 17.

**Ghi chú màn hình dùng chung**: không dùng chung với School Administration — đây là màn hình soạn/gửi (viết), phía School Administration chỉ nhận (đọc) qua thông báo + Compliance Dashboard/Incident List/Violation Registry hiện có, không có "Report Center" tương ứng.

# Screen Spec — Send Incident Summary Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-188 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-213 (báo cáo tổng hợp sự cố theo kỳ), BR-147 (phân loại mức độ), BR-181 (ẩn danh dữ liệu cá nhân), BR-178 (xuất báo cáo PDF/Excel), BR-158/BR-159 (thông báo trong hệ thống), BR-170 (audit log)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-096 (Error — không có sự cố trong kỳ), MSG-098 (Success — gửi thành công)

> **Bổ sung 13/07/2026**: hiện thực hóa **BR-213** — mở rộng cơ chế "gửi báo cáo chủ động" của UC-185 sang mảng dữ liệu sự cố ATTP. **Nguyên tắc cốt lõi**: đây là báo cáo **rollup số liệu**, KHÔNG lặp lại nội dung chi tiết đã có trong báo cáo chính thức từng sự cố (clinical notes, kết quả xét nghiệm mẫu, root cause, corrective action — đã tự động gửi School Administration ngay khi đóng sự cố, theo BR-154/UC-033 `20_GenerateIncidentReportDialog.md`). Mỗi mã sự cố trong bảng chỉ hiện trạng thái/mức độ + liên kết điều hướng, không hiện lại nội dung điều tra. Vào từ `29_ATTPReportCenter.md` (UC-187). Xem `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-213 | Mặc định: Daily; cùng cơ chế với UC-185 |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-213 | Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật) |
| 3 | Tổng số sự cố trong kỳ theo trạng thái | Card số liệu (đọc) | Đếm theo 3 nhóm: Chờ tiếp nhận / Đang xử lý / Đã đóng (khớp `25_IncidentList.md`) | BR-213 | |
| 4 | Phân bố theo mức độ nghiêm trọng | Card/biểu đồ nhỏ (đọc) | Chỉ tính sự cố đã qua Step 1 (Assign Severity) | BR-147 | Sự cố chưa đánh giá mức độ không tính vào biểu đồ này |
| 5 | Danh sách mã sự cố trong kỳ | Table rút gọn (đọc) | Cột: Mã sự cố, Trạng thái, Mức độ, nút "Xem chi tiết" | BR-213 | **Không hiện lại** clinical notes/root cause/corrective action; "Xem chi tiết" điều hướng `26_IncidentDetail.md` (mở tab/route mới, không rời dialog) |
| 6 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-213 | Medical Staff tự nhập nhận định/khuyến nghị tổng quan |
| 7 | Nhãn "Confidential" | Badge tĩnh | Luôn hiện | BR-181 | Nhắc báo cáo chứa dữ liệu sự cố y tế, không phát tán ngoài phạm vi School Administration |
| 8 | Nút "Gửi báo cáo" (Send Report) | Button (primary) | Disabled nếu không có sự cố nào trong kỳ (MSG-096) | BR-213 | Xuất PDF/Excel (BR-178) + gửi thông báo (BR-158/159) |
| 9 | Nút "Hủy" (Cancel) | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tổng hợp dữ liệu kỳ đã chọn và khi đang gửi.
- Empty: không có sự cố nào trong kỳ đã chọn → chặn gửi, hiện MSG-096, nút "Gửi báo cáo" disabled.
- Error: MSG-004, MSG-005 (lỗi hệ thống chung), MSG-096.
- Success: MSG-098 (Toast), đóng dialog, ghi audit log (BR-170), cập nhật "Lần gửi gần nhất" trên `29_ATTPReportCenter.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Card "Báo cáo Tổng hợp Sự cố"); thành công → đóng dialog, quay lại Report Center.
**Bố cục**: `Dialog` modal — chọn kỳ báo cáo trên cùng, card số liệu (tổng số theo trạng thái + phân bố mức độ) xếp lưới 2 cột, Table danh sách mã sự cố giữa dialog, Textarea ghi chú bên dưới, nhãn "Confidential" góc trên phải, 2 nút "Gửi báo cáo"/"Hủy" cố định cuối dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Mobile); thành công → đóng màn, quay lại Report Center.
**Bố cục**: Trang riêng full-screen — chọn kỳ báo cáo trên cùng, card số liệu xếp dọc, danh sách mã sự cố dạng `Card` cuộn dọc rút gọn, Textarea ghi chú, nút "Gửi báo cáo" cố định cuối màn, nút "Hủy" trên AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Incident Summary Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog modal — chọn kỳ Daily/Weekly, card tổng số sự cố theo trạng thái (Chờ tiếp nhận/Đang xử
lý/Đã đóng), card phân bố mức độ nghiêm trọng, Table danh sách mã sự cố (Mã/Trạng thái/Mức độ/nút Xem
chi tiết — KHÔNG hiện nội dung điều tra chi tiết), textarea ghi chú, nhãn "Confidential", nút Gửi báo
cáo (primary) + nút Hủy.
Áp dụng Design System: primary #0E7490, danger #B91C1C/warning #B45309 cho mức độ HIGH/MEDIUM.
Trạng thái cần thể hiện: loading, empty (MSG-096), success (MSG-098).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-213, mới 13/07/2026), §11 (BR-147, BR-154).
- `traceability_matrix.md` (UC-188, mới 13/07/2026).
- `error_message.md` (MSG-096, MSG-098).
- `25_IncidentList.md` (nguồn số liệu rollup), `26_IncidentDetail.md` (điều hướng chi tiết), `20_GenerateIncidentReportDialog.md` (UC-033, báo cáo chi tiết từng sự cố — không lặp lại ở đây).
- `29_ATTPReportCenter.md` (điểm vào từ nút "Soạn & Gửi").

# Screen Spec — Send Violation Summary Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-189 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-214 (báo cáo tổng hợp vi phạm theo kỳ), BR-186 (phân cấp mức độ), BR-189 (danh sách tổng hợp vi phạm), BR-211 (biên bản vi phạm PDF đã ký), BR-178 (xuất báo cáo PDF/Excel), BR-158/BR-159 (thông báo trong hệ thống), BR-170 (audit log)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-097 (Error — không có vi phạm trong kỳ), MSG-099 (Success — gửi thành công)

> **Bổ sung 13/07/2026**: hiện thực hóa **BR-214** — mở rộng cơ chế "gửi báo cáo chủ động" của UC-185 sang mảng dữ liệu vi phạm. **Nguyên tắc cốt lõi**: đây là báo cáo **rollup số liệu**, KHÔNG lặp lại nội dung biên bản vi phạm đã ký 3 bên (đã có sẵn theo BR-211, xuất PDF ngay khi lập từng vi phạm). Mỗi vi phạm trong bảng chỉ hiện loại/đối tượng/mức độ/trạng thái + liên kết tới biên bản PDF đã có (nếu đã ký), không hiện lại toàn văn biên bản. Vào từ `29_ATTPReportCenter.md` (UC-187). Xem `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-214 | Mặc định: Daily; cùng cơ chế với UC-185 |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-214 | Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật) |
| 3 | Tổng số vi phạm trong kỳ theo loại | Card số liệu (đọc) | Đếm theo 3 loại: quy trình/nhân viên/đối tác (BR-189) | BR-214 | |
| 4 | Phân bố theo mức độ | Card/biểu đồ nhỏ (đọc) | LOW/MEDIUM/HIGH (BR-186) | BR-214 | |
| 5 | Danh sách vi phạm trong kỳ | Table rút gọn (đọc) | Cột: Loại, đối tượng liên quan, mức độ, trạng thái, nút "Xem biên bản" (nếu đã có PDF ký, BR-211) | BR-214 | **Không hiện lại** toàn văn biên bản đã ký; "Xem biên bản" mở PDF đã lưu theo BR-211 |
| 6 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-214 | Medical Staff tự nhập nhận định/khuyến nghị tổng quan |
| 7 | Nút "Gửi báo cáo" (Send Report) | Button (primary) | Disabled nếu không có vi phạm nào trong kỳ (MSG-097) | BR-214 | Xuất PDF/Excel (BR-178) + gửi thông báo (BR-158/159) |
| 8 | Nút "Hủy" (Cancel) | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tổng hợp dữ liệu kỳ đã chọn và khi đang gửi.
- Empty: không có vi phạm nào trong kỳ đã chọn → chặn gửi, hiện MSG-097, nút "Gửi báo cáo" disabled.
- Error: MSG-004, MSG-005 (lỗi hệ thống chung), MSG-097.
- Success: MSG-099 (Toast), đóng dialog, ghi audit log (BR-170), cập nhật "Lần gửi gần nhất" trên `29_ATTPReportCenter.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Card "Báo cáo Tổng hợp Vi phạm"); thành công → đóng dialog, quay lại Report Center.
**Bố cục**: `Dialog` modal — chọn kỳ báo cáo trên cùng, card số liệu (tổng số theo loại + phân bố mức độ) xếp lưới 2 cột, Table danh sách vi phạm giữa dialog, Textarea ghi chú bên dưới, 2 nút "Gửi báo cáo"/"Hủy" cố định cuối dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Mobile); thành công → đóng màn, quay lại Report Center.
**Bố cục**: Trang riêng full-screen — chọn kỳ báo cáo trên cùng, card số liệu xếp dọc, danh sách vi phạm dạng `Card` cuộn dọc rút gọn, Textarea ghi chú, nút "Gửi báo cáo" cố định cuối màn, nút "Hủy" trên AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Violation Summary Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog modal — chọn kỳ Daily/Weekly, card tổng số vi phạm theo loại (quy trình/nhân viên/đối
tác), card phân bố mức độ (LOW/MEDIUM/HIGH), Table danh sách vi phạm (Loại/Đối tượng/Mức độ/Trạng
thái/nút Xem biên bản — KHÔNG hiện toàn văn biên bản), textarea ghi chú, nút Gửi báo cáo (primary) +
nút Hủy.
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM), secondary #64748B (LOW).
Trạng thái cần thể hiện: loading, empty (MSG-097), success (MSG-099).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-214, mới 13/07/2026), §18 (BR-186, BR-189, BR-211).
- `traceability_matrix.md` (UC-189, mới 13/07/2026).
- `error_message.md` (MSG-097, MSG-099).
- `27_ViolationRegistry.md` (nguồn số liệu rollup), `24_RecordViolationResolution.md` (biên bản đã ký, BR-211 — không lặp lại ở đây).
- `29_ATTPReportCenter.md` (điểm vào từ nút "Soạn & Gửi").

# Screen Spec — Sent Reports History

**Actor**: Medical Staff | **UC liên quan**: UC-190 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-177 (lưu trữ lịch sử báo cáo tối thiểu 12 tháng), BR-175 (phân quyền xem báo cáo theo vai trò), BR-178 (định dạng xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Bổ sung 13/07/2026**: màn hình xem lại lịch sử các báo cáo Medical Staff đã gửi cho School Administration (cả 3 loại: BR-209/UC-185, BR-213/UC-188, BR-214/UC-189). Phát sinh từ phản hồi người dùng — trước đây sau khi gửi thành công 1 báo cáo, không còn nơi nào xem lại (chỉ có Toast lúc gửi). **Không cấp BR mới** — đây là hiện thực hóa **BR-177** (đã có sẵn trong `business_rules.md` §16, quy định lưu trữ báo cáo tối thiểu 12 tháng + audit trail mỗi lần xuất) ở phạm vi riêng của Medical Staff. **Thiết kế theo mẫu UC-107 "View Batch History"** (Raw Food Supplier) — màn lịch sử riêng, đọc dữ liệu nghiệp vụ trực tiếp, KHÔNG dựa vào màn "Audit Logs" (UC-106) dùng chung của System Admin/School Administration, vì Medical Staff hiện chưa có quyền truy cập Audit Logs (`14_Screen_Authorization_Matrix.md`, cột MS trống ở dòng "Audit Logs") và Audit Logs hiển thị Timestamp/User/Action/Resource — không tối ưu để đọc lại nội dung 1 báo cáo. Xem `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Lịch sử báo cáo đã gửi" | — | |
| 2 | Filter theo Loại báo cáo | Select | Giám sát tuân thủ / Tổng hợp Sự cố / Tổng hợp Vi phạm / Tất cả | — | Mặc định: Tất cả |
| 3 | Filter theo khoảng ngày gửi | Date range picker | — | — | |
| 4 | Bảng lịch sử báo cáo đã gửi | Table (đọc-only) | Cột: Loại báo cáo, Kỳ (Daily/Weekly), Khoảng thời gian dữ liệu, Ngày giờ gửi, Người gửi | BR-177, BR-182 | Sắp xếp mặc định: mới nhất trước |
| 5 | Nút "Tải lại PDF" (mỗi dòng) | Button/Icon | — | BR-178 | Tải lại đúng file PDF đã sinh tại thời điểm gửi (không tạo lại/tính lại số liệu) |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng.
- Empty: "Chưa có báo cáo nào được gửi" + illustration (hoặc "Không có báo cáo nào khớp bộ lọc" khi đang lọc).
- Error: MSG-004, MSG-005.
- Success: bảng hiển thị đầy đủ, không có MSG riêng (màn chỉ đọc).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Xem lịch sử báo cáo đã gửi" trên `29_ATTPReportCenter.md`. Điều hướng đi: không có (màn cuối nhánh, chỉ đọc).
**Bố cục**: filter bar (Loại báo cáo, khoảng ngày) phía trên `Table` đầy đủ, phân trang dưới bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Xem lịch sử báo cáo đã gửi" trên `29_ATTPReportCenter.md` (Mobile).
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` phía trên, danh sách `Card` cuộn dọc (mỗi Card: Loại báo cáo + badge, Kỳ, Khoảng thời gian dữ liệu, Ngày gửi, nút "Tải lại PDF").

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Sent Reports History, nền tảng Web, actor Medical Staff.
Bố cục: filter bar (Loại báo cáo, khoảng ngày gửi) phía trên Table lịch sử (Loại báo cáo, Kỳ, Khoảng
thời gian dữ liệu, Ngày giờ gửi, Người gửi, nút Tải lại PDF mỗi dòng), phân trang dưới bảng.
Component bắt buộc dùng: Table, Select (filter), DateRangePicker, Badge (loại báo cáo), Button (tải PDF).
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-177, BR-175, BR-178, BR-182).
- `error_message.md` (MSG-004, MSG-005).
- `traceability_matrix.md` (UC-190, mới 13/07/2026).
- `17_ViewBatchHistory.md` (Raw Food Supplier, UC-107) — mẫu tham chiếu cho cấu trúc màn lịch sử riêng theo actor.
- `28_SendComplianceReportDialog.md` (UC-185), `30_SendIncidentSummaryDialog.md` (UC-188), `31_SendViolationSummaryDialog.md` (UC-189) — 3 nguồn dữ liệu ghi vào lịch sử này mỗi khi gửi thành công.
- `review/medical_staff/review_screen_medical_staff.md` Mục 17.

# Screen Spec — Send Compliance Report Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-185 (mới, bổ sung 05/07/2026 — không có trong Report 3.md gốc, chính thức hóa US-09) | **Status**: Draft
**BR liên quan**: BR-209 (gửi báo cáo tuân thủ ATTP định kỳ), BR-104 (điểm vệ sinh đầu bếp), BR-102 (chứng nhận kiểm soát côn trùng), BR-103 (khám sức khỏe nhân viên bếp), BR-178 (xuất báo cáo PDF/Excel), BR-158/BR-159 (thông báo trong hệ thống), BR-170 (audit log)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-092 (Error — không có dữ liệu trong kỳ), MSG-091 (Success — gửi thành công)

> **Bổ sung 05/07/2026**: màn hình này hiện thực hóa **US-09** (`MedicalStaff_UserStory.md`) sau khi đội dự án phát hiện Compliance Dashboard (UC-027) — cơ chế hiện có duy nhất để School Administration nắm tình hình ATTP — vận hành theo mô hình **pull** (School Administration tự vào xem) và **chưa có route triển khai thực tế** phía frontend School Administration (xem `review_screen_medical_staff.md` mục 3.9). Màn hình này bổ sung kênh **push** song song, không thay thế UC-027. Không có trong `Report 3.md` gốc — mọi field đều tái sử dụng dữ liệu/cơ chế đã có (KitchenInspection từ UC-014, export BR-178, thông báo BR-158/159), không mở rộng phạm vi model dữ liệu.
>
> **Sửa 12/07/2026**: field 5 đổi từ badge "Còn hiệu lực/Hết hạn" sang tóm tắt bản ghi nhật ký kiểm soát côn trùng gần nhất — đồng bộ quyết định đổi model "nhật ký sự kiện" cho BR-102 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 1, đã áp code `compliance/page.tsx`). Xem `review_screen_medical_staff.md` Mục 14.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-209 | Mặc định: Daily |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-209 | Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật) |
| 3 | Điểm vệ sinh trung bình trong kỳ | Card số liệu (đọc) | Tính từ các `KitchenInspection` trong kỳ | BR-104 | |
| 4 | Danh sách phiên kiểm tra Failed trong kỳ (nếu có) | List/Table rút gọn (đọc) | Hiện mã phiên, ngày, lý do | BR-100, BR-104 | Trống nếu không có phiên Failed |
| 5 | Kiểm soát côn trùng — bản ghi gần nhất trong kỳ | Card tóm tắt (đọc) | Ngày xử lý gần nhất, Loại côn trùng phát hiện (nếu có), Ngày hẹn kiểm tra tiếp theo | BR-102 | **Sửa 12/07/2026** — thay cho badge "Còn hiệu lực/Hết hạn"; nếu không có bản ghi nào trong kỳ, hiện "Không có hoạt động kiểm soát côn trùng trong kỳ" |
| 6 | Tình trạng khám sức khỏe nhân viên bếp | Text/Badge (đọc) | Đạt / Có nhân viên quá hạn | BR-103 | |
| 7 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-209 | Medical Staff tự nhập nhận định/khuyến nghị |
| 8 | Nút "Gửi báo cáo" (Send Report) | Button (primary) | Disabled nếu không có phiên kiểm tra nào trong kỳ (MSG-092) | BR-209 | Xuất PDF/Excel (BR-178) + gửi thông báo (BR-158/159) |
| 9 | Nút "Hủy" (Cancel) | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tổng hợp dữ liệu kỳ đã chọn và khi đang gửi.
- Empty: không có phiên kiểm tra nào trong kỳ đã chọn → chặn gửi, hiện MSG-092, nút "Gửi báo cáo" disabled.
- Error: MSG-004, MSG-005 (lỗi hệ thống chung), MSG-092.
- Success: MSG-091 (Toast), đóng dialog, ghi audit log (BR-170), lưu vào lịch sử báo cáo (BR-177, xem lại tại `32_SentReportsHistory.md`).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Gửi báo cáo tuân thủ" trên `23_ComplianceDashboard.md` (Web) **hoặc** từ Card "Báo cáo Giám sát tuân thủ" trên `29_ATTPReportCenter.md` (Web, mới 13/07/2026 — UC-187); thành công → đóng dialog, quay lại màn nguồn tương ứng.
**Bố cục**: `Dialog` modal — chọn kỳ báo cáo trên cùng, các card số liệu/trạng thái xếp lưới 2 cột giữa dialog, Textarea ghi chú bên dưới, 2 nút "Gửi báo cáo"/"Hủy" cố định cuối dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Gửi báo cáo tuân thủ" trên `23_ComplianceDashboard.md` (Mobile) **hoặc** từ Card "Báo cáo Giám sát tuân thủ" trên `29_ATTPReportCenter.md` (Mobile, mới 13/07/2026 — UC-187); thành công → đóng màn, quay lại màn nguồn tương ứng.
**Bố cục**: Trang riêng full-screen (bottom sheet hoặc trang con) — chọn kỳ báo cáo trên cùng, các card số liệu/trạng thái xếp dọc, Textarea ghi chú, nút "Gửi báo cáo" cố định cuối màn, nút "Hủy" trên AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Compliance Report Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog modal — chọn kỳ Daily/Weekly, card điểm vệ sinh trung bình, danh sách phiên Failed,
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
- `29_ATTPReportCenter.md` (UC-187, điểm vào bổ sung 13/07/2026), `32_SentReportsHistory.md` (UC-190, xem lại sau khi gửi).
