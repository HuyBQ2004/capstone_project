# Tổng hợp Screen Specifications — Teacher (Teacher)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Teacher Home

**Actor**: Teacher | **UC liên quan**: — (entry point, không gắn UC cụ thể) | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa/roster lớp chủ nhiệm), BR-121 (theo dõi lịch sử bữa ăn), BR-146 (tự động kích hoạt điều tra sự cố khi ≥3 học sinh triệu chứng tương đồng/24h — hiển thị cảnh báo liên quan nếu có)
**MSG liên quan**: MSG-004 (Session expired), MSG-005 (Access denied)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | Hiển thị "Dashboard" + subtext "Class in charge: [Tên lớp] · [Tên giáo viên]" | BR-025 | Nguồn: Report 3 §3.3.2.6 Teacher Dashboard |
| 2 | Greeting Banner | Card/Info box | "Hello, [Tên giáo viên]!" + ngày hệ thống hiện tại | — | |
| 3 | Total Students Card | Metric tile | Số học sinh trong lớp chủ nhiệm | BR-025 (tối đa 50 HS/lớp) | Lấy từ roster lớp (UC-056 bước 2 — class registry assigned to Teacher) |
| 4 | Meals Pending Card | Metric tile | Số suất ăn chưa phân phát trong phiên hiện tại | BR-120, BR-130 | Liên kết Distribution List |
| 5 | Dietary Notes Card | Metric tile | Số học sinh có ghi chú chế độ ăn đặc biệt | BR-065 | Liên kết Special Meal Requirements |
| 6 | Health Incidents Card | Metric tile | Số báo cáo sự cố sức khỏe giáo viên đã gửi (đang mở) | BR-146 | Liên kết Incident History |
| 7 | Distribute Meals Today Button | Button (primary, full-width) | Điều hướng tới Distribution List | BR-120 | |
| 8 | Report New Health Incident Button | Button (secondary, full-width) | Điều hướng tới Incident Form | BR-146 | |
| 9 | Today's Notice Board Card | List/Card | Danh sách bullet cảnh báo khẩn (dị ứng, thực đơn đặc biệt, tình trạng sự cố mới nhất) | BR-123, BR-138 | Cảnh báo `critical`/BR-138 hiển thị không qua duyệt |
| 10 | Bottom Tab Navigation | Nav | 4 tab: Home (active), Students, Meals, Incidents | — | Xem `Navigation_Guidelines.md` mục 2 |

### Trạng thái màn hình
- Loading: Shimmer cho 4 metric card khi tải dashboard.
- Empty: Nếu chưa có lớp chủ nhiệm được gán (TH hiếm, lỗi phân công) → hiển thị thông báo "Chưa được phân công lớp chủ nhiệm — liên hệ Ban giám hiệu" [Giả định — cần team xác nhận, vì Report 3 không mô tả trạng thái này].
- Error: `SnackBar` màu `danger` + MSG-004/MSG-005 khi hết phiên/không có quyền.
- Success: Không áp dụng (màn hình chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Điểm vào ngay sau Common Entry Flow (Login) — xem `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`. Điều hướng đi: Announcement Detail (Notice Board), Meal Distribution (Distribute Meals Today), Health Incident Report Form (Report New Health Incident), Incident History (View History), Notifications (bell icon).
**Bố cục**: AppBar tiêu đề "Dashboard" + icon thông báo; Content 1 cột cuộn dọc gồm Greeting Banner → 4 Metric Card (2x2 grid) → 2 action button full-width → Notice Board Card; `BottomNavigationBar` 4 mục (Home/Students/Meals/Incidents) theo `Layout_Guidelines.md` mục 2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Teacher Home, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Greeting Banner + 4 Metric Card dạng lưới 2x2 + 2 nút hành động full-width + Notice Board Card + BottomNavigationBar 4 mục (Home/Students/Meals/Incidents).
Thành phần bắt buộc: Screen Title Header, Greeting Banner, Total Students Card, Meals Pending Card, Dietary Notes Card, Health Incidents Card, Distribute Meals Today Button, Report New Health Incident Button, Today's Notice Board Card.
Áp dụng Design System: primary #0E7490, Card Material 3, cảnh báo dùng token `danger`/`critical` theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading (shimmer), error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6314–6333 (Teacher Dashboard, field-by-field gốc, Mobile).
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`, `12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-025, BR-120, BR-123, BR-130, BR-138, BR-146.
- `traceability_matrix.md` §1.9 (UC-016, UC-056, UC-080), §1.11 (UC-074).

# Screen Spec — Student List

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History (bước 1–2, class registry), tham chiếu thêm UC-074 (chọn học sinh liên quan khi báo cáo sự cố) | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa lớp, roster), BR-030 (liên kết học sinh–phụ huynh, dữ liệu dị ứng/chế độ ăn)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Home Shortcut Button | Button | Về Teacher Home | — | Nguồn: Report 3 §3.3.2.6 Student List in Class |
| 2 | Screen Title Header | Text | "Class [Tên lớp] Student List" | BR-025 | |
| 3 | Profile Filter Tabs | Tabs | "All" / "Allergy Warning" / "Special Diet" / "Normal" | BR-030, BR-065 | |
| 4 | Student Record Card | Card list (1 bản ghi/card) | Avatar initials, họ tên, badge tình trạng (Allergy Warning đỏ / Special Diet vàng / Normal xám), link "Profile" | BR-030 | Tap → Student Detail |
| 5 | Bottom Tab Navigation | Nav | 4 tab: Home, Students (active), Meals, Incidents | — | |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách card.
- Empty: "Lớp chưa có học sinh nào" + hướng dẫn liên hệ School Administration [Giả định].
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng (màn hình chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home (tab "Students"), từ Meal Distribution ("select class"), từ Incident Form ("select affected student(s)") — xem `07_Teacher_Screen_Flow.md`. Ra: Student Detail (tap Profile).
**Bố cục**: AppBar (Home shortcut + tiêu đề) → Profile Filter Tabs → danh sách `Card` cuộn dọc (1 học sinh/card) → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student List, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Profile Filter Tabs (All/Allergy Warning/Special Diet/Normal) + danh sách Card cuộn dọc + BottomNavigationBar.
Thành phần bắt buộc: Home Shortcut Button, Screen Title Header, Profile Filter Tabs, Student Record Card (avatar, tên, badge trạng thái, link Profile).
Áp dụng Design System: badge màu theo Design_System.md mục 3 (danger=Allergy Warning, warning=Special Diet, secondary=Normal).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6351–6365 (Student List in Class, field-by-field gốc).
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`.
- `business_rules.md` BR-025, BR-030.

# Screen Spec — Student Detail

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info (một phần), UC-056 (bước 4-5, chi tiết học sinh) | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh–phụ huynh), BR-123 (cảnh báo dị ứng)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: Report 3 §3.3.2.6 (Teacher Screens) không có field-by-field riêng cho "Student Detail" — chỉ có link "Profile" từ Student List. Field dưới đây suy ra từ UC-145 (Normal Flow xem thông tin dị ứng/sức khỏe học sinh) + nhu cầu điều hướng tới 3 màn con (Allergy Information, Health Information, Special Meal Requirements, Meal Records) theo `07_Teacher_Screen_Flow.md`. Đánh dấu rõ các field không có nguồn UC/BR tường minh.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Avatar, họ tên, lớp, ngày sinh | BR-030 | [Giả định — cần team xác nhận về field ngày sinh cụ thể] |
| 2 | Allergy Status Badge | Badge | "Có dị ứng" (đỏ)/"Không dị ứng" (xám) | BR-030, BR-123 | Điều hướng Allergy Information |
| 3 | Special Diet Badge | Badge | "Chế độ ăn đặc biệt" (vàng)/ẩn nếu không có | BR-065 | Điều hướng Special Meal Requirements |
| 4 | View Health Information Link | Nút/Link | Điều hướng Health Information | BR-030 | |
| 5 | View Meal Records Link | Nút/Link | Điều hướng Meal Records | BR-121 | |
| 6 | Parent Contact Info | Text | Tên phụ huynh liên kết (chỉ đọc) | BR-030 | [Giả định — cần team xác nhận mức độ chi tiết được phép hiển thị cho Teacher] |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng (luôn có dữ liệu học sinh khi vào từ danh sách).
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student List (tap "Profile"). Ra: Allergy Information, Health Information, Special Meal Requirements, Meal Records.
**Bố cục**: AppBar (back + tên học sinh) → Student Summary Card → các Badge trạng thái → danh sách link điều hướng dạng `Card` action.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Detail, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Summary Card + Badge trạng thái (dị ứng/chế độ ăn đặc biệt) + danh sách link điều hướng (Health Information/Allergy Information/Special Meal Requirements/Meal Records).
Thành phần bắt buộc: Student Summary Card, Allergy Status Badge, Special Diet Badge, View Health Information Link, View Meal Records Link.
Áp dụng Design System: badge danger/warning theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` (suy luận điều hướng — không có wireframe gốc Report 3 cho màn này).
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-056.
- `business_rules.md` BR-030, BR-065, BR-123.

# Screen Spec — Allergy Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info; UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-030 (dữ liệu dị ứng học sinh), BR-123 (cảnh báo dị ứng bắt buộc màu đỏ — BR-123 Design System)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú**: màn hình dùng chung 3 actor (Teacher/Medical Staff/Parent — xem `14_Screen_Authorization_Matrix.md` khối 4). Bản dưới đây là bản **Teacher** — chỉ đọc (read-only), không có action chỉnh sửa (khác Parent có thể Submit/Update, khác Medical Staff có thể xác nhận/review). Field hiển thị dữ liệu cần hợp nhất với bản Medical Staff/Parent ở bước review cuối (mục 5–6 của `00_Team_Work_Assignment_Plan.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Allergy Profile List | List/Card | Mỗi dòng: tên allergen, mức độ nghiêm trọng (Mild/Moderate/Severe) | BR-030 | Severe hiển thị màu `danger` bắt buộc (BR-123, `Design_System.md` mục 3) |
| 3 | Verification Status Badge | Badge | "Đã xác nhận bởi Medical Staff" (`success`) / "Chờ xác nhận" (`warning`) | BR-031 | |
| 4 | Read-only Notice | Text/Banner | "Chỉ Medical Staff và Phụ huynh được chỉnh sửa thông tin này" | — | Teacher không có quyền sửa, chỉ xem — theo phạm vi RBAC actor |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Học sinh chưa khai báo dị ứng" (icon lớn + text).
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Detail (badge dị ứng) hoặc từ Distribution List (allergy badge tap) — xem `07_Teacher_Screen_Flow.md` ("allergy badge tap → View Student Allergy Info").
**Bố cục**: AppBar (back) → Student Identity Header → Allergy Profile List (Card mỗi allergen) → Verification Status Badge → Read-only Notice cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Allergy Information, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Identity Header + danh sách Card allergen (mức độ nghiêm trọng) + Badge xác nhận + banner read-only.
Thành phần bắt buộc: Student Identity Header, Allergy Profile List, Verification Status Badge, Read-only Notice.
Áp dụng Design System: Severe = danger (đỏ, BR-123), Moderate = warning, Mild = info.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm nút chỉnh sửa (Teacher chỉ đọc).
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` ("View Student Allergy Info (read-only)").
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-103.
- `business_rules.md` BR-030, BR-031, BR-123; `Design_System.md` mục 3 (BR-123).

# Screen Spec — Health Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info | **Status**: Draft
**BR liên quan**: BR-030 (dữ liệu sức khỏe học sinh), BR-172 (bảo vệ toàn vẹn dữ liệu — chỉ đọc phía Teacher)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: Report 3 không có wireframe riêng cho Teacher xem "Health Information" (chỉ có bản tương đương phía Parent — "Student Health Hub" — và Medical Staff). Field dưới đây tham chiếu cấu trúc dữ liệu tương tự "Student Health Hub" (Report 3 dòng 5989–6011) nhưng ở chế độ chỉ đọc, thu gọn (Teacher không cần xem cân nặng/chiều cao chi tiết như phụ huynh — chỉ cần thông tin liên quan an toàn thực phẩm/sức khỏe khi có sự cố). [Giả định — cần team xác nhận phạm vi field chính xác Teacher được phép xem].

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Blood Type Card | Metric tile (chỉ đọc) | Nhóm máu | BR-030 | [Giả định — cần xác nhận Teacher có quyền xem] |
| 3 | Chronic Conditions List | List (chỉ đọc) | Bệnh mãn tính/lưu ý y tế đã khai báo | BR-030 | |
| 4 | Emergency Contact Note | Text (chỉ đọc) | Ghi chú liên hệ khẩn (nếu có) | — | [Giả định — cần team xác nhận] |
| 5 | Read-only Notice | Banner | "Chỉ Medical Staff/Phụ huynh chỉnh sửa được" | BR-172 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có thông tin sức khỏe được khai báo".
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Detail ("View Health Information").
**Bố cục**: AppBar (back) → Student Identity Header → Blood Type Card → Chronic Conditions List → Emergency Contact Note → Read-only Notice.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Health Information, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Identity Header + Metric tile nhóm máu + List bệnh mãn tính + note liên hệ khẩn + banner read-only.
Thành phần bắt buộc: Student Identity Header, Blood Type Card, Chronic Conditions List, Emergency Contact Note, Read-only Notice.
Áp dụng Design System: neutral tone (không dùng màu cảnh báo trừ khi có ghi chú nguy hiểm).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm field/nút chỉnh sửa.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011 (Student Health Hub, bản Parent — dùng đối chiếu cấu trúc dữ liệu, không phải wireframe gốc của Teacher).
- `traceability_matrix.md` §1.3 UC-145.
- `business_rules.md` BR-030, BR-172.

# Screen Spec — Special Meal Requirements

**Actor**: Teacher | **UC liên quan**: UC-143 - View Special Meal Requirements | **Status**: Draft
**BR liên quan**: BR-125 (xem yêu cầu bữa ăn đặc biệt khi phục vụ), BR-065 (kế hoạch bữa ăn đặc biệt bắt buộc), BR-066 (danh mục chế độ ăn đặc biệt), BR-082 (chế biến riêng)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: Report 3 không có wireframe riêng cho màn này (không nằm trong 5 Teacher Screens đã đặc tả field-by-field). Field suy ra trực tiếp từ UC-143 Normal Flow + BR-065/BR-066 (danh mục: Halal, Vegetarian, Gluten-Free, Nut-Free...).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Special Meal Requirements — Class [Tên lớp]" | BR-125 | |
| 2 | Special Meal Student List | Card list (1 học sinh/card) | Họ tên, loại chế độ ăn (Halal/Vegetarian/Gluten-Free/Nut-Free/khác), lý do (dị ứng/tôn giáo) | BR-065, BR-066 | Chỉ hiển thị học sinh có `AllergyProfile`/chế độ ăn đã xác minh |
| 3 | Approval Status Badge | Badge | "Đã duyệt kế hoạch" (`success`)/"Chưa có kế hoạch" (`warning`) | BR-065 | |
| 4 | Preparation Note | Text (chỉ đọc) | Ghi chú chế biến riêng (nếu có) | BR-082 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Lớp không có học sinh cần chế độ ăn đặc biệt".
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home (Dietary Notes Card) hoặc Student Detail (Special Diet Badge).
**Bố cục**: AppBar (back) → Screen Title Header → danh sách `Card` học sinh có chế độ ăn đặc biệt, mỗi card kèm badge trạng thái duyệt.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Requirements, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + danh sách Card học sinh (tên, loại chế độ ăn, lý do, badge trạng thái duyệt).
Thành phần bắt buộc: Screen Title Header, Special Meal Student List, Approval Status Badge, Preparation Note.
Áp dụng Design System: success/warning theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm field/nút chỉnh sửa (Teacher chỉ đọc).
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-143.
- `business_rules.md` BR-065, BR-066, BR-082, BR-125.
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` (suy luận điều hướng).

# Screen Spec — Meal Records

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History; UC-080 - Student Meal Consumption | **Status**: Draft
**BR liên quan**: BR-121 (theo dõi lịch sử bữa ăn), BR-125 (yêu cầu bữa ăn đặc biệt khi phục vụ), BR-133 (tóm tắt bữa ăn hàng ngày cho phụ huynh)
**MSG liên quan**: MSG-004, MSG-005, MSG-021

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Meal Records — [Tên học sinh]" hoặc danh sách cả lớp | UC-056 bước 1-3 | Có 2 chế độ: danh sách tóm tắt cả lớp / chi tiết 1 học sinh |
| 2 | Date Range Filter | Filter/DatePicker | Lọc theo khoảng ngày | UC-056 AF1 | |
| 3 | Meal History Summary List | Card list | Ngày, món ăn đã phục vụ, trạng thái tiêu thụ (Fully/Partially/Not Consumed) | BR-121 | |
| 4 | Consumption Record Input (chỉ với học sinh chế độ ăn đặc biệt) | Radio group | "Fully Consumed" / "Partially Consumed" / "Not Consumed" | BR-121, BR-125 | UC-080 bước 3 — chỉ hiển thị cho học sinh có special dietary plan |
| 5 | Save Consumption Button | Button (primary) | Lưu ghi nhận, validate đủ trường bắt buộc | BR-121 | MSG-021 nếu trùng lặp ghi nhận trong cùng phiên |
| 6 | Special Meal Compliance Badge | Badge | Đánh dấu tuân thủ chế độ ăn đặc biệt trong phiên | BR-125 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có dữ liệu bữa ăn trong khoảng thời gian đã chọn" — gợi ý đổi bộ lọc (UC-056 AF2 tương tự).
- Error: `SnackBar` `danger` + MSG-004/MSG-005; MSG-021 khi ghi nhận trùng lặp.
- Success: `SnackBar` `success` khi lưu ghi nhận tiêu thụ thành công.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Detail ("View Meal Records") hoặc Teacher Home. Không có trong sơ đồ mermaid gốc của `07_Teacher_Screen_Flow.md` — bổ sung theo UC-056/UC-080 [Giả định về vị trí điều hướng — cần team xác nhận].
**Bố cục**: AppBar (back) → Date Range Filter → danh sách `Card` lịch sử bữa ăn → (nếu học sinh đặc biệt) khối Consumption Record Input + Save button cố định dưới màn hình.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Records, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Date Range Filter + danh sách Card lịch sử bữa ăn + khối ghi nhận tiêu thụ (radio group) + nút Save cố định dưới.
Thành phần bắt buộc: Date Range Filter, Meal History Summary List, Consumption Record Input, Save Consumption Button, Special Meal Compliance Badge.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-056, UC-080.
- `business_rules.md` BR-121, BR-125, BR-133.
- Report 3.md, dòng 3887–3928 (UC-056 Monitor Student Meal History, UC-080 Student Meal Consumption — Normal Flow đầy đủ).

# Screen Spec — Distribution List

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 1-2) | **Status**: Draft
**BR liên quan**: BR-120 (xác nhận phân phát theo lớp), BR-121 (theo dõi lịch sử bữa ăn), BR-123 (cảnh báo dị ứng khi quét ID — hiển thị nổi bật), BR-130 (ngăn phân phát trùng lặp)
**MSG liên quan**: MSG-004, MSG-005, MSG-021, MSG-045

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Home Shortcut Button | Button | Về Teacher Home | — | Nguồn: Report 3 §3.3.2.6 "Meal Distribution" |
| 2 | Screen Title Header | Text | "Meal Distribution" | — | |
| 3 | Status Filter Tabs | Tabs | "All (N)" / "Pending" / "Served" / "Special Meal" | BR-120 | |
| 4 | Student Meal Status Card | Card list (1 học sinh/card) | Avatar initials, họ tên, tag chế độ ăn ("Vegetarian Meal"/"Normal Meal"), cảnh báo dị ứng chữ đỏ ("Warning: [Allergen] Allergy") nếu có, Status Indicator, link "Details" | BR-123, BR-125 | Tap "Details" → Distribution Detail |
| 5 | Bottom Tab Navigation | Nav | 4 tab: Home, Students, Meals (active), Incidents | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có phiên phân phát bữa ăn hôm nay".
- Error: `SnackBar` `danger` + MSG-004/005; MSG-021 nếu phát hiện quét/xác nhận trùng lặp.
- Success: `SnackBar` `success` (MSG-045) sau khi xác nhận toàn bộ lớp đã phục vụ.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home ("Distribute Meals Today"). Ra: Distribution Detail (tap "Details"), Distribution Confirmation (xác nhận từng học sinh/cả lớp).
**Bố cục**: AppBar (Home shortcut + tiêu đề) → Status Filter Tabs → danh sách `Card` học sinh cuộn dọc → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution List, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Status Filter Tabs (All/Pending/Served/Special Meal) + danh sách Card học sinh (avatar, tên, tag chế độ ăn, cảnh báo dị ứng đỏ, status, link Details) + BottomNavigationBar.
Thành phần bắt buộc: Status Filter Tabs, Student Meal Status Card.
Áp dụng Design System: cảnh báo dị ứng bắt buộc màu danger/đỏ (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349 (Meal Distribution, field-by-field gốc).
- `traceability_matrix.md` §1.9 UC-016.
- `business_rules.md` BR-120, BR-121, BR-123, BR-130.

# Screen Spec — Distribution Detail

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 3), UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-120, BR-123, BR-125
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe riêng trong Report 3 — suy ra từ link "Details" trên Distribution List (Report 3 dòng 6346) + UC-016 bước 3 "Teacher verifies each student received the correct meal".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Avatar, họ tên, lớp | — | |
| 2 | Assigned Meal Card | Card | Tên món ăn được phân công (thường/đặc biệt) | BR-125 | |
| 3 | Allergy Warning Banner | Banner (nếu có) | "Warning: [Allergen] Allergy" chữ/nền đỏ | BR-123 | Bắt buộc màu danger (BR-123) |
| 4 | Distribution Status Badge | Badge | "Pending"/"Served" | BR-120 | |
| 5 | Confirm Served Button | Button (primary) | Điều hướng Distribution Confirmation | BR-120, BR-130 | |
| 6 | Report Distribution Error Link | Link/Button (secondary) | Báo lỗi phân phát sai — theo UC-016 AF1 | BR-065 | Gửi thông báo cho Service Caterers |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng tại màn này (thành công hiển thị ở Distribution Confirmation).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Distribution List (tap "Details"). Ra: Distribution Confirmation.
**Bố cục**: AppBar (back) → Student Identity Header → Assigned Meal Card → Allergy Warning Banner (nếu có) → Distribution Status Badge → Confirm Served Button + Report Error Link.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Detail, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Identity Header + Assigned Meal Card + Allergy Warning Banner (đỏ, nếu có) + Status Badge + nút Confirm Served + link báo lỗi.
Thành phần bắt buộc: Student Identity Header, Assigned Meal Card, Allergy Warning Banner, Distribution Status Badge, Confirm Served Button, Report Distribution Error Link.
Áp dụng Design System: cảnh báo dị ứng màu danger bắt buộc.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349 (Meal Distribution — nguồn gián tiếp qua "Details Link"), dòng 3799–3819 (UC-016 Confirm Meal Distribution, AF1 Distribution Error).
- `business_rules.md` BR-120, BR-123, BR-125, BR-065.

# Screen Spec — Distribution Confirmation

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-120 (xác nhận phân phát theo lớp), BR-130 (ngăn phân phát trùng lặp)
**MSG liên quan**: MSG-021, MSG-045

> **Ghi chú nguồn**: tương ứng "Confirm Meal Served Dialog" trong `07_Teacher_Screen_Flow.md` — không có wireframe field-by-field riêng trong Report 3, suy ra từ UC-016 bước 4-5 (Normal Flow).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Confirmation Dialog Title | Text | "Xác nhận đã phục vụ [Tên học sinh]?" hoặc "Xác nhận cả lớp đã được phục vụ?" | BR-120 | 2 chế độ: từng học sinh / cả lớp |
| 2 | Timestamp Display | Text (tự động) | Ghi timestamp hiện tại | BR-120 | Tự động, không cho sửa tay |
| 3 | Duplicate Scan Check | Validation ẩn | Chặn nếu học sinh đã được xác nhận trong phiên | BR-130 | MSG-021 nếu vi phạm |
| 4 | Cancel Button | Button (secondary) | Đóng dialog, không lưu | — | |
| 5 | Confirm Button | Button (primary) | Lưu xác nhận, cập nhật trạng thái Served | BR-120 | MSG-045 khi thành công |

### Trạng thái màn hình
- Loading: Spinner nhỏ trong dialog khi đang lưu.
- Empty: không áp dụng.
- Error: `AlertDialog`/toast `danger` + MSG-021 (trùng lặp).
- Success: `SnackBar` `success` (MSG-045), tự đóng dialog và quay về Distribution List.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Distribution Detail hoặc trực tiếp từ Distribution List (xác nhận nhanh). Ra: quay lại Distribution List sau khi xác nhận.
**Bố cục**: `showModalBottomSheet` (Material 3) trượt từ dưới lên — tiêu đề, timestamp tự động, 2 nút Cancel/Confirm cố định dưới cùng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Confirmation, nền tảng Mobile, actor Teacher.
Bố cục: Bottom sheet Material 3 — tiêu đề xác nhận + timestamp tự động + 2 nút Cancel/Confirm.
Thành phần bắt buộc: Confirmation Dialog Title, Timestamp Display, Cancel Button, Confirm Button.
Trạng thái cần thể hiện: loading (spinner nhỏ), error (MSG-021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` ("Confirm Meal Served Dialog").
- Report 3.md, dòng 3799–3819 (UC-016 bước 4-5).
- `business_rules.md` BR-120, BR-130; `error_message.md` MSG-021, MSG-045.

# Screen Spec — Upload Evidence

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (thành phần "Attached Photos") | **Status**: Draft
**BR liên quan**: BR-196 (cho phép ghi nhận bằng chứng offline), BR-200 (giới hạn hiệu lực cache offline)
**MSG liên quan**: MSG-008 (file vượt 10MB), MSG-009 (định dạng không hỗ trợ), MSG-078 (đã lưu offline, chờ đồng bộ)

> **Ghi chú nguồn**: elevate từ field "Attached Photos Component" trong Health Incident Report (Report 3 dòng 6381) thành 1 màn hình riêng để mô tả đầy đủ luồng chụp/chọn ảnh + hàng đợi đồng bộ offline (theo `UI_Design_Guidelines.md` mục 3, mục 5 — Teacher là 1 trong 5 role field được áp dụng offline evidence capture BR-196). [Giả định — cần team xác nhận đây có phải màn hình độc lập hay chỉ là 1 component nhúng trong Incident Form].

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Take Photo Button | Button (camera) | Mở camera thiết bị | ADR-002 (ngữ cảnh camera), BR-196 | Xin quyền camera đúng lúc (`UI_Design_Guidelines.md` mục 3) |
| 2 | Choose From Gallery Button | Button | Chọn ảnh có sẵn | — | |
| 3 | File Size Validation | Validation | Tối đa 10MB/ảnh | BR-013 | MSG-008 nếu vượt |
| 4 | File Type Validation | Validation | Chỉ nhận PDF/JPG/PNG | — | MSG-009 nếu sai định dạng |
| 5 | Photo Preview Thumbnail List | List ảnh | Xem trước ảnh đã chọn, cho phép xoá từng ảnh | — | |
| 6 | Offline Sync Badge | Badge | "Chờ đồng bộ" (vàng/cam) khi lưu lúc mất kết nối | BR-196 | MSG-078 |
| 7 | Attach & Continue Button | Button (primary) | Xác nhận đính kèm, quay lại Incident Form | — | |

### Trạng thái màn hình
- Loading: Spinner khi đang tải/nén ảnh.
- Empty: "Chưa có ảnh nào được đính kèm".
- Error: `SnackBar` `danger` + MSG-008/MSG-009.
- Success: Nếu offline → MSG-078 + badge "Chờ đồng bộ"; nếu online → ảnh hiển thị trạng thái đã tải lên (✓ xanh).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Incident Form (nút "Take"/"Choose" trong Attached Photos Component). Ra: quay lại Incident Form với ảnh đã đính kèm.
**Bố cục**: Toàn màn hình camera/preview theo Material 3 — 2 nút lớn (Take Photo/Choose from Gallery) phía trên, danh sách thumbnail cuộn ngang phía dưới, nút "Attach & Continue" cố định đáy màn hình.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Evidence, nền tảng Mobile, actor Teacher.
Bố cục: 2 nút lớn Take Photo/Choose from Gallery + danh sách thumbnail ảnh đã chọn (cuộn ngang) + nút Attach & Continue cố định đáy.
Thành phần bắt buộc: Take Photo Button, Choose From Gallery Button, Photo Preview Thumbnail List, Offline Sync Badge, Attach & Continue Button.
Áp dụng Design System: badge "Chờ đồng bộ" màu warning theo UI_Design_Guidelines.md mục 5.
Trạng thái cần thể hiện: loading, empty, error (MSG-008/009), success (MSG-078 khi offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386 (Health Incident Report, field "Attached Photos Component").
- `business_rules.md` BR-013, BR-196, BR-200; `error_message.md` MSG-008, MSG-009, MSG-078.
- `03_Design/00_Foundation/UI_Design_Guidelines.md` mục 3, mục 5.

# Screen Spec — Incident Form

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 (theo Business Rules field của UC-074, Report 3 dòng 4523 — đầy đủ hơn bảng BR tóm tắt ở `traceability_matrix.md` §1.11 chỉ ghi BR-146)
**MSG liên quan**: MSG-023 (báo cáo sự cố thiếu thông tin), MSG-048 (ghi nhận báo cáo sự cố thành công)

> **Ghi chú mâu thuẫn nguồn**: wireframe field-by-field gốc (Report 3 dòng 6367–6386) không liệt kê field "Suspected Dishes"/"Ingestion Date"/"Number of Affected Students" mà Normal Flow bước 3 của UC-074 (Report 3 dòng 4518) yêu cầu bắt buộc ("Teacher enters: observed symptoms, number of affected students, suspected dishes, and ingestion date"). Theo nguyên tắc mục 3 của Assignment Plan (không suy diễn nhưng cũng không được bỏ sót field UC yêu cầu), 3 field dưới đây được bổ sung thêm vào wireframe gốc — đánh dấu rõ nguồn UC, không phải nguồn wireframe.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Health Incident Report" + subtext "Record student condition" | — | Nguồn: Report 3 §3.3.2.6 |
| 2 | Student Input Field | Input (autocomplete từ Student List) | Bắt buộc — chọn học sinh (hỗ trợ chọn nhiều học sinh nếu ≥1 em bị ảnh hưởng) | BR-146 | |
| 3 | Class Input Field | Input (chỉ đọc) | Pre-filled tên lớp giáo viên | — | |
| 4 | Number of Affected Students Input | Input (số) | Bắt buộc — theo UC-074 bước 3 | BR-146, BR-154 | Nguồn: Normal Flow UC-074, không có trong wireframe gốc |
| 5 | Suspected Dishes Input | Input/Select (từ thực đơn ngày) | Bắt buộc — theo UC-074 bước 3 | BR-151, BR-154 | Nguồn: Normal Flow UC-074 |
| 6 | Ingestion Date Input | DatePicker | Bắt buộc — theo UC-074 bước 3 | BR-154 | Nguồn: Normal Flow UC-074 |
| 7 | Detected Symptoms Checkboxes | Checkbox group | Stomachache/Vomiting/Allergy/Diarrhea/Fever + "Other" (text tự do) | BR-146 | Bắt buộc chọn ≥1 |
| 8 | Severity Level Radio Group | Radio group | Mild (mặc định)/Medium/Severe | BR-147 | |
| 9 | Incident Description Input | Textarea | Bắt buộc, mô tả chi tiết | — | MSG-023 nếu thiếu |
| 10 | Attached Photos Component | File upload (Take/Choose) | Tuỳ chọn — xem `11_UploadEvidence.md` | BR-196 | Cho phép offline (evidence capture) |
| 11 | Cancel Button | Button (destructive-outline) | Huỷ, quay về Teacher Home | — | |
| 12 | Submit Incident Report Button | Button (primary, full-width) | Validate đầy đủ trường bắt buộc trước khi gửi | BR-146, BR-149, BR-151 | MSG-023 nếu thiếu; MSG-048 khi thành công (tự động quarantine lô nghi vấn BR-149, sinh bản đồ truy xuất trong 4h BR-151) |

### Trạng thái màn hình
- Loading: Spinner khi đang submit.
- Empty: không áp dụng (form nhập mới).
- Error: `SnackBar` `danger` + MSG-023 (thiếu trường bắt buộc: triệu chứng, ngày ăn, món ăn theo mô tả UC).
- Success: `SnackBar` `success` + MSG-048, điều hướng tới Incident Submitted Confirmation rồi Incident History.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home ("Report New Health Incident"). Ra: chọn học sinh → Student List; submit → Incident Submitted Confirmation → (routed) Medical Staff Investigation Workspace.
**Bố cục**: AppBar → form 1 cột cuộn dọc theo thứ tự field trên → 2 nút Cancel/Submit cố định đáy màn hình (`Layout_Guidelines.md` mục 2.2).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident Form, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + form 1 cột (Student/Class input, Symptoms checkbox, Severity radio, Description textarea, Attached Photos, 2 nút Cancel/Submit cố định đáy).
Thành phần bắt buộc: Student Input Field, Class Input Field, Number of Affected Students Input, Suspected Dishes Input, Ingestion Date Input, Detected Symptoms Checkboxes, Severity Level Radio Group, Incident Description Input, Attached Photos Component, Cancel Button, Submit Incident Report Button.
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386 (Health Incident Report, field-by-field gốc), dòng 4507–4529 (UC-074 Report Food Safety Incident — Normal/Alternative/Exception Flow).
- `business_rules.md` BR-146, BR-196; `error_message.md` MSG-023, MSG-048.

# Screen Spec — Incident History

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (xem lại báo cáo đã gửi) | **Status**: Draft
**BR liên quan**: BR-146, BR-154 (báo cáo sự cố chính thức), BR-155 (đóng sự cố — chỉ School Administration, Teacher chỉ xem)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có UC "View Incident History" riêng cho Teacher trong `traceability_matrix.md` — màn hình chỉ hiển thị lại dữ liệu do UC-074 tạo ra, ở chế độ chỉ đọc, giới hạn phạm vi báo cáo của chính giáo viên đó (theo `07_Teacher_Screen_Flow.md`: "chỉ hiển thị các báo cáo do giáo viên đó tạo, không có quyền xem toàn bộ danh sách incident").

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Home Shortcut Button | Button | Về Teacher Home | — | Nguồn: Report 3 §3.3.2.6 |
| 2 | Screen Title Header | Text | "Incident History" | — | |
| 3 | Assessment Filter Tabs | Tabs | "All" / "Pending Assessment" / "Under Assessment" | BR-147 | |
| 4 | Incident Log Card | Card list (1 báo cáo/card) | Học sinh liên quan, triệu chứng, badge mức độ (Mild/Medium), ngày báo cáo, badge trạng thái (Pending Assessment vàng/Concluded xanh) | BR-147, BR-155 | Chỉ đọc — trạng thái do Medical Staff/School Administration cập nhật |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Bạn chưa gửi báo cáo sự cố nào".
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home ("View History") hoặc tab "Incidents". Không có màn con (chỉ đọc danh sách).
**Bố cục**: AppBar (Home shortcut + tiêu đề) → Assessment Filter Tabs → danh sách `Card` cuộn dọc → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident History, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Assessment Filter Tabs (All/Pending Assessment/Under Assessment) + danh sách Card báo cáo sự cố + BottomNavigationBar.
Thành phần bắt buộc: Assessment Filter Tabs, Incident Log Card (học sinh, triệu chứng, badge mức độ, ngày, badge trạng thái).
Áp dụng Design System: Pending Assessment = warning, Concluded = success.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm action chỉnh sửa/đóng sự cố (ngoài quyền Teacher).
```

## Nguồn dữ liệu
- Report 3.md, dòng 6388–6402 (Incident History, field-by-field gốc).
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`.
- `business_rules.md` BR-146, BR-147, BR-154, BR-155.
