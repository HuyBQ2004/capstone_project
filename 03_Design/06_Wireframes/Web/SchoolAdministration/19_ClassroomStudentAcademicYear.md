# Wireframe — Classroom, Student & Academic Year Management (nhóm 14 màn — School Administration)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/SchoolAdministration/19_ClassroomManagement.md` … `45_PromoteStudentsWorkspace.md` (14 file, danh sách đầy đủ ở bảng mục 1)
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router)
**File/Link**: `03_Design/06_Wireframes/Web/SchoolAdministration/19_ClassroomStudentAcademicYear.html`
**Ngày dựng**: 2026-07-01 | **Người phụ trách**: Nguyễn Văn Nam (School Administration + System Admin, theo `00_Team_Work_Assignment_Plan.md` mục 2)

> **Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

---

## 1. Danh sách 14 màn hình trong file HTML

| STT | Màn hình | Screen ID trong HTML (`div.screen`) | File nguồn Screen Spec | Ghi chú |
| --- | --- | --- | --- | --- |
| 19 | Classroom Management | `classroom-management` | `19_ClassroomManagement.md` | UC-037/UC-166 |
| 20 | Classroom Detail | `classroom-detail` (+ modal `assign-teacher-modal`) | `20_ClassroomDetail.md` | UC-165 + action UC-008 (Assign Homeroom Teacher), UC-090 (Update Classroom) — dựng ngay tại trang, không phải màn riêng |
| 21 | Create Classroom | `create-classroom` | `21_CreateClassroom.md` | UC-018; có toggle demo minh hoạ rule chặn MSG-081/BR-192 |
| 22 | Student Profile Management | `student-profile-management` | `22_StudentProfileManagement.md` | UC-147 + action UC-025 |
| — | Create Student Profile | `create-student-profile` | **KHÔNG có file Screen Spec riêng** | UC-025 — xem cảnh báo mục 3 bên dưới |
| 23 | Student Profile Detail | `student-profile-detail` | `23_StudentProfileDetail.md` | UC-099 |
| 24 | Bulk Import Student Profiles | `bulk-import` | `24_BulkImportStudentProfiles.md` | UC-010 |
| 25 | Parent Account Detail | `parent-account-detail` | `25_ParentAccountDetail.md` | bundled trong UC-025 |
| 26 | Link Parent Account | `link-parent-account` | `26_LinkParentAccount.md` | bundled trong UC-025, bước 5 Normal Flow |
| 27 | Linked Students List | `linked-students-list` | `27_LinkedStudentsList.md` | bundled trong UC-025; action "gỡ liên kết" là giả định cần team xác nhận |
| 41 | Academic Year List | `ay-list` | `41_AcademicYearList.md` | UC-176 |
| 42 | Create Academic Year | `ay-create` | `42_CreateAcademicYear.md` | UC-174 |
| 43 | Academic Year Detail | `ay-detail` | `43_AcademicYearDetail.md` | bundled (UC-175 Activate/Close, UC-177 Promote) |
| 44 | Activate/Close Academic Year Dialog | modal `ay-dialog` (dùng chung 2 nhánh) | `44_ActivateCloseAcademicYearDialog.md` | UC-175 — dialog mở từ Academic Year Detail |
| 45 | Promote Students Workspace | `ay-promote` | `45_PromoteStudentsWorkspace.md` | UC-177 |

Tổng cộng: **14/14 Screen Specification** có file nguồn tương ứng, cộng thêm 1 màn phụ trợ "Create Student Profile" không có Screen Spec riêng (dựng từ ghi chú bundled của 25/26/27 theo đúng chỉ dẫn nhiệm vụ).

## 2. Cấu trúc điều hướng đã hiện thực (khớp mermaid `02_School_Administration_Screen_Flow.md`)

```
Sidebar --> Classroom Management --(Create)--> Create Classroom --(cần năm học Active, nếu chưa có)--> [alert MSG-081 + link Academic Year List]
Classroom Management --(row click)--> Classroom Detail --(Assign/Reassign)--> [modal Assign Homeroom Teacher]
Sidebar --> Student Profile Management --(Create)--> Create Student Profile --> [bước Link Parent Account]
Student Profile Management --(row click)--> Student Profile Detail --(link phụ huynh)--> Parent Account Detail
Parent Account Detail --(Xem đầy đủ)--> Linked Students List --(row click)--> Student Profile Detail
Parent Account Detail --(Link thêm học sinh)--> Link Parent Account
Student Profile Management --(Bulk Import)--> Bulk Import Student Profiles
Sidebar --> Academic Year List --(Create)--> Create Academic Year --(MSG-082 success)--> Academic Year List
Academic Year List --(row click)--> Academic Year Detail --(Activate/Close)--> [modal Activate/Close Academic Year Dialog]
Academic Year Detail --(Promote Students, chỉ khi Active)--> Promote Students Workspace --(MSG-084 success)--> Academic Year Detail
```

Điều hướng dùng JS thuần `showScreen(id)` ẩn/hiện `div.screen`, sidebar chỉ 2 nhóm "Lớp/Học sinh" và "Năm học" là click được; các mục Dashboard/Announcements/Incident Dashboard hiển thị disabled (ngoài phạm vi file này).

## 3. Rule chặn nghiệp vụ đã minh hoạ

| Rule | Màn hình | Cách minh hoạ trong HTML |
| --- | --- | --- |
| BR-192 — Create Classroom cần năm học Active | `create-classroom` | Checkbox demo "(demo) Minh hoạ trạng thái: trường chưa có năm học Active" bật alert MSG-081 + link điều hướng sang Academic Year List |
| BR-191/MSG-080 — Activate bị chặn nếu đã có năm Active khác | `ay-detail`, modal `ay-dialog` | Nút demo "Xem: Draft (bị chặn Activate — MSG-080)" hiển thị alert danger + nút Activate disabled; xác nhận Activate trong dialog cũng trả về alert MSG-080 |
| BR-193 — Close → Classroom liên quan chuyển read-only | `ay-detail`, modal `ay-dialog` (nhánh Close) | Nút demo "Xem: Closed" hiện ghi chú đỏ BR-193; dialog nhánh Close có alert warning mô tả hệ quả; xác nhận Close trả về MSG-083 và chuyển trạng thái Closed |
| BR-025/MSG-010 — vượt sức chứa lớp (Classroom Detail, Student Profile Detail, Promote Students) | `classroom-detail`, `student-profile-detail`, `ay-promote` | Alert danger tĩnh minh hoạ MSG-010 ở cả 3 màn |
| BR-020/MSG-011 — giáo viên đã gán lớp khác | modal `assign-teacher-modal` | Chọn "Trần Văn Bình" trong combo → hiện alert xác nhận reassign |

## 4. Đối chiếu với Screen Specification (checklist tổng quát 14 màn)

- [x] Đủ toàn bộ field theo bảng "Danh sách Field / Thành phần" của từng file — không bịa thêm field ngoài bảng.
- [x] Grayscale/mono tuyệt đối — không dùng màu Design System thật, badge phân biệt bằng viền/kiểu nét (dashed/dotted/solid) + icon SVG doodle thay vì màu.
- [x] Dùng `wired-elements` qua CDN cho input/select/checkbox/calendar/divider/search-input/combo; nút hành động dùng `.btn-primary/.btn-secondary` (không `<wired-button>`).
- [x] Font 'Patrick Hand' + nền graph-paper + sketchy border đúng theo SKILL.md.
- [x] Không emoji — toàn bộ icon là SVG doodle nhúng inline.
- [x] Ảnh/placeholder: không màn nào trong 14 màn yêu cầu ảnh thật (không cần Option B).
- [x] Trạng thái Success/mặc định là chính; có toggle/nút demo phụ để xem nhanh Error/rule chặn quan trọng mà không cần rời màn.
- [x] Bỏ qua "Initialization Routine" của SKILL.md theo đúng chỉ dẫn nhiệm vụ.

## 5. Checklist riêng từng màn

### 19 — Classroom Management
- [x] Search tên lớp, filter Grade Level, bảng (Class Name/Grade Level/Student Capacity/Homeroom Teacher), nút Create Classroom, action xem chi tiết, nút Export Excel/CSV.
- [x] Trạng thái Success hiển thị chính; ghi chú Empty state kèm theo dưới bảng.

### 20 — Classroom Detail
- [x] Class Name/Grade Level/Max Capacity (hiển thị + form Edit inline), Homeroom Teacher + nút Assign/Reassign, dialog chọn GV (chặn MSG-011 + xác nhận reassign AF1), bảng học sinh trong lớp, nút Edit (Update Classroom, capacity không được nhỏ hơn số học sinh hiện có → MSG-010).
- [x] Assign Homeroom Teacher (UC-008) và Update Classroom (UC-090) dựng như action/dialog tại trang, đúng ghi chú "không phải màn hình riêng".

### 21 — Create Classroom
- [x] Tên lớp, Cấp lớp (1-12), Sức chứa tối đa (≤50), Năm học áp dụng (select/read-only nếu chỉ 1 năm Active, bắt buộc có Active — MSG-081/BR-192), nút Tạo lớp.
- [x] Không có tuỳ chọn "Activate ngay" (đúng yêu cầu Screen Spec — năm học mới luôn Draft, không liên quan trực tiếp form này nhưng đã tuân thủ không thêm field thừa).

### 22 — Student Profile Management
- [x] Search tên học sinh, filter Class/Grade, bảng (Tên/School Registration Number/Lớp/Liên hệ phụ huynh), nút Create Student Profile, action xem chi tiết, nút Bulk Import, nút Export Excel/CSV.

### Create Student Profile (không có Screen Spec riêng)
- [x] Dựng tối giản chỉ với field xuất hiện trong ghi chú "bundled" của 25/26/27 (bước tìm/link Parent Account) — KHÔNG bịa thêm field hồ sơ học sinh (Họ tên/Ngày sinh/Lớp) vì các field đó không nằm trong bảng Business Logic của 3 file bundled.
- [x] Có cảnh báo rõ ràng ngay trên màn hình về việc thiếu Screen Spec chính thức.

### 23 — Student Profile Detail
- [x] Họ tên, Ngày sinh (DatePicker), Lớp (Select, đổi lớp không vượt sức chứa 50 → MSG-010), School Registration/Admission Code (read-only), thông tin phụ huynh liên kết + link sang Parent Account Detail, nút Lưu.

### 24 — Bulk Import Student Profiles
- [x] Nút Tải template, input upload file + preview tên file, nút Tải lên & Kiểm tra, bảng tóm tắt validation (hợp lệ/không hợp lệ/trùng lặp), danh sách dòng lỗi chi tiết (MSG-012), nút Xác nhận nhập.
- [x] Bố cục Stepper ngang (Tải template → Upload → Validate → Xác nhận) đúng Platform Layout.

### 25 — Parent Account Detail
- [x] Họ tên phụ huynh, Email, Số điện thoại, Badge trạng thái xác minh (warning=Pending/success=Verified — minh hoạ Verified, phân biệt bằng icon+viền), danh sách học sinh liên kết rút gọn (tối đa 5) + link "Xem đầy đủ", nút "Link thêm học sinh" (chặn khi đạt giới hạn — có checkbox demo).

### 26 — Link Parent Account
- [x] Input Email phụ huynh, card "Đã tìm thấy tài khoản" (nhánh tồn tại), form Họ tên + SĐT (nhánh tạo mới, hiển thị disabled vì demo tĩnh dùng nhánh đã tồn tại), Alert cảnh báo giới hạn liên kết (AF1), nút Xác nhận liên kết.

### 27 — Linked Students List
- [x] Bảng đầy đủ (Tên/School Reg No/Lớp/Trạng thái), action xem chi tiết học sinh, action gỡ liên kết kèm `confirm()` giả lập AlertDialog xác nhận — có cảnh báo rõ đây là hành vi giả định cần team xác nhận (đúng ghi chú Screen Spec).

### 41 — Academic Year List
- [x] Filter Status (Draft/Active/Closed/Archived), bảng (Year Label/Start-End Date/Status/Số lớp), Badge trạng thái phân biệt bằng kiểu viền (dashed=Draft, solid dày=Active, dotted=Closed, nền xám=Archived), nút Create Academic Year, action xem chi tiết — có dòng Archived vẫn hiển thị (BR-195).

### 42 — Create Academic Year
- [x] Year Label, Start Date, End Date (2x wired-calendar), Checkbox "Sao chép cấu trúc lớp từ năm học trước" (AF1/BR-194, chỉ tên/cấp/sức chứa, không chuyển học sinh), nút Tạo năm học → MSG-082, không có tuỳ chọn Activate ngay.

### 43 — Academic Year Detail
- [x] Year Label/Start-End Date, Badge trạng thái, bảng lớp thuộc năm học (read-only note khi Closed/Archived — BR-193/195), nút Activate (chỉ Draft, disable + MSG-080 nếu có năm Active khác), nút Close (chỉ Active, destructive), nút Promote Students (chỉ khi Active) — có 4 nút demo chuyển trạng thái để xem toàn bộ điều kiện hiện/ẩn nút.

### 44 — Activate/Close Academic Year Dialog
- [x] Thông tin năm học (Year Label + badge trước/sau), Alert info cho nhánh Activate (cảnh báo chặn nếu còn năm Active khác), Alert warning cho nhánh Close (Classroom read-only + pending operations), nút Xác nhận (primary/destructive tuỳ nhánh) + nút Huỷ. Dùng 1 modal chung, đổi nội dung theo `ayDialogMode`.

### 45 — Promote Students Workspace
- [x] Select khối/lớp nguồn (năm học hiện tại), Select lớp đích (năm học mới, không vượt sức chứa — MSG-010), bảng học sinh bị ảnh hưởng + checkbox từng dòng (mặc định chọn hết), checkbox bỏ chọn = loại trừ học sinh lưu ban (dòng chuyển mờ/gạch ngang minh hoạ), nút Xác nhận lên lớp → MSG-084.
- [x] Bố cục Stepper ngang (Chọn khối nguồn-đích → Xem & loại trừ → Xác nhận) đúng Platform Layout.

## 6. Vấn đề/mâu thuẫn phát hiện — báo lại Nguyễn Phương Thảo (QA)

1. **"Create Student Profile" (UC-025) không có Screen Specification riêng** trong bộ 14 file được giao — đây là lỗ hổng rõ nhất. Field hồ sơ học sinh (Họ tên, Ngày sinh, Lớp...) cho bước 1 của UC-025 hiện KHÔNG có nguồn chính thức nào; wireframe chỉ dựng được bước 2 (tìm/link Parent Account) vì field đó có trong ghi chú "bundled" của 25/26/27. **Cần bổ sung Screen Spec chính thức cho Create Student Profile trước khi coi wireframe khối này là đầy đủ.**
2. `23_StudentProfileDetail.md` tự ghi nhận lệch số BR: Report 3 gán BR-041 (vốn là "Trạng thái nhà cung cấp", không liên quan) cho UC-099; file đã tự sửa dùng BR-025 làm căn cứ — cần Thảo xác nhận lại khi QA để thống nhất BR chính thức.
3. `27_LinkedStudentsList.md` tự đánh dấu action "gỡ liên kết" là **[Giả định — cần team xác nhận]** vì Normal Flow gốc UC-025 không mô tả hành vi này — đã giữ nguyên cảnh báo này trong wireframe, cần chốt trước khi sang Prototype chính thức.
4. `41_AcademicYearList.md` ghi chú màn hình "cũng dùng chung với System Admin" nhưng phạm vi School Admin giới hạn theo trường — wireframe này chỉ dựng theo góc nhìn School Admin (đúng phạm vi nhiệm vụ), cần đối chiếu thêm khi làm Wireframe cho System Admin để tránh trùng lặp/lệch field.
5. Không phát hiện thiếu field nào khác trong 13 file Screen Spec còn lại — 13/13 file có đủ bảng Business Logic rõ ràng để dựng 1-1.
