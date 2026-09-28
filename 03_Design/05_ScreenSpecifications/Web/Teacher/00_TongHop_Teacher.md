# Tổng hợp Screen Specifications — Teacher (Teacher)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Teacher Home

**Actor**: Teacher | **UC liên quan**: — (entry point, không gắn UC cụ thể) | **Status**: Draft
**BR liên quan**: BR-025, BR-121, BR-146
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | Tiêu đề "Dashboard" + "Class in charge: [Tên lớp] · [Tên giáo viên]" | BR-025 | |
| 2 | Greeting Banner | Alert (info) | Lời chào + ngày hiện tại | — | |
| 3 | Total Students Card | Metric card | Số học sinh lớp chủ nhiệm | BR-025 | |
| 4 | Meals Pending Card | Metric card | Số suất ăn chưa phân phát | BR-120, BR-130 | |
| 5 | Dietary Notes Card | Metric card | Số học sinh có chế độ ăn đặc biệt | BR-065 | |
| 6 | Health Incidents Card | Metric card | Số báo cáo sự cố đang mở | BR-146 | |
| 7 | Distribute Meals Today Button | Button (primary) | Điều hướng Distribution List | BR-120 | Góc trên phải Page Header |
| 8 | Report New Health Incident Button | Button (secondary) | Điều hướng Incident Form | BR-146 | |
| 9 | Today's Notice Board Card | Alert list | Cảnh báo khẩn (dị ứng, thực đơn đặc biệt, sự cố mới nhất) | BR-123, BR-138 | |

### Trạng thái màn hình
- Loading: `Skeleton` cho 4 metric card.
- Empty: "Chưa được phân công lớp chủ nhiệm — liên hệ Ban giám hiệu" [Giả định — cần team xác nhận].
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sau Common Entry Flow (Login Web) — xem `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`. Điều hướng đi: Announcement Detail, Meal Distribution, Health Incident Report Form, Incident History, Notifications.
**Bố cục**: Topbar (breadcrumb, notif, avatar) + Sidebar rút gọn (nhóm: Bảng tin / Phân phát Bữa ăn / Sự cố Sức khỏe / Cá nhân, theo `12_Teacher_Screen_Flow_Web.md`) + Content Area grid 12 cột: Greeting Banner (col-span-12), 4 Metric Card (col-span-3 mỗi card), 2 action button hàng ngang, Notice Board Card (col-span-12) — theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Teacher Home, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar rút gọn 4 nhóm + Content grid 12 cột (Greeting Banner full-width, 4 Metric Card ngang hàng, 2 nút hành động, Notice Board Card).
Thành phần bắt buộc: Page Header, Greeting Banner, Total Students Card, Meals Pending Card, Dietary Notes Card, Health Incidents Card, Distribute Meals Today Button, Report New Health Incident Button, Today's Notice Board Card.
Áp dụng Design System: shadcn/ui "new-york", primary #0E7490, bo góc 8px.
Trạng thái cần thể hiện: loading (Skeleton), error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6314–6333 (Teacher Dashboard, bản Mobile gốc — dùng chung business logic).
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-025, BR-120, BR-123, BR-130, BR-138, BR-146.

# Screen Spec — Student List

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History (bước 1–2, class registry) | **Status**: Draft
**BR liên quan**: BR-025, BR-030
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Class [Tên lớp] Student List" + filter/search phía trên bảng | BR-025 | |
| 2 | Profile Filter Tabs | Tabs/Filter | "All" / "Allergy Warning" / "Special Diet" / "Normal" | BR-030, BR-065 | |
| 3 | Student Table | `Table`/`DataTable` (sort/filter/pagination) | Cột: Avatar, Họ tên, Badge tình trạng, action "Xem hồ sơ" | BR-030 | Web ưu tiên Table thay Card list (`Component_Guidelines.md` mục 2.4) |

### Trạng thái màn hình
- Loading: `Skeleton` bảng.
- Empty: "Lớp chưa có học sinh nào" [Giả định].
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" (theo `12_Teacher_Screen_Flow_Web.md`); ra Student Detail (click hàng bảng).
**Bố cục**: Topbar + Sidebar + Content: Page Header + thanh filter/search + `Table` phân trang, cột action bên phải theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student List, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + filter tabs + Table (sort/filter/pagination).
Thành phần bắt buộc: Profile Filter Tabs, Student Table (avatar, họ tên, badge trạng thái, action xem hồ sơ).
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6351–6365 (bản Mobile gốc — dùng chung business logic).
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-025, BR-030.

# Screen Spec — Student Detail

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info (một phần), UC-056 | **Status**: Draft
**BR liên quan**: BR-030, BR-123
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe gốc Report 3 cho màn này — field suy ra từ UC-145 + điều hướng `12_Teacher_Screen_Flow_Web.md`. Xem ghi chú đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Avatar, họ tên, lớp, ngày sinh | BR-030 | [Giả định — cần team xác nhận] |
| 2 | Allergy Status Badge | Badge | "Có dị ứng"/"Không dị ứng" | BR-030, BR-123 | Link Allergy Information |
| 3 | Special Diet Badge | Badge | "Chế độ ăn đặc biệt"/ẩn | BR-065 | Link Special Meal Requirements |
| 4 | View Health Information Tab/Link | Tab/Link | Điều hướng Health Information | BR-030 | |
| 5 | View Meal Records Tab/Link | Tab/Link | Điều hướng Meal Records | BR-121 | |
| 6 | Parent Contact Info | Text (chỉ đọc) | Tên phụ huynh liên kết | BR-030 | [Giả định — cần team xác nhận] |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success/Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Student List (click hàng bảng). Ra: Health Information, Allergy Information, Special Meal Requirements, Meal Records.
**Bố cục**: Topbar + Sidebar + Content: Page Header (tên học sinh) + Student Summary Card (col-span-4) + Tab điều hướng nội bộ (Health/Allergy/Special Meal/Meal Records) theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Detail, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Student Summary Card + Tab điều hướng (Health/Allergy/Special Meal/Meal Records).
Thành phần bắt buộc: Student Summary Card, Allergy Status Badge, Special Diet Badge, View Health Information Link, View Meal Records Link.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-056.
- `business_rules.md` BR-030, BR-065, BR-123.

# Screen Spec — Allergy Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info; UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-030, BR-123
**MSG liên quan**: MSG-004, MSG-005

> Bản Web — chỉ đọc, dùng chung field/BR/MSG với bản Mobile cùng tên. Xem ghi chú hợp nhất đa-actor ở bản Mobile.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Allergy Profile Table/List | Table hoặc Card list | Allergen, mức độ nghiêm trọng | BR-030 | Severe = `danger` (BR-123) |
| 3 | Verification Status Badge | Badge | "Đã xác nhận"/"Chờ xác nhận" | BR-031 | |
| 4 | Read-only Notice | Alert (info) | "Chỉ Medical Staff/Phụ huynh chỉnh sửa được" | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Học sinh chưa khai báo dị ứng".
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Student Detail (tab Allergy). Không có action ghi (Teacher chỉ đọc).
**Bố cục**: Topbar + Sidebar + Content: Student Identity Header + bảng/list allergen (col-span-8) + badge xác nhận + banner thông báo quyền hạn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Allergy Information, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Student Identity Header + Table/Card list allergen + Badge xác nhận + Alert read-only.
Thành phần bắt buộc: Student Identity Header, Allergy Profile Table, Verification Status Badge, Read-only Notice.
Áp dụng Design System: Severe = danger, Moderate = warning, Mild = info.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm nút chỉnh sửa.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-103.
- `business_rules.md` BR-030, BR-031, BR-123.

# Screen Spec — Health Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info | **Status**: Draft
**BR liên quan**: BR-030, BR-172
**MSG liên quan**: MSG-004, MSG-005

> Bản Web, chỉ đọc — dùng chung field/BR/MSG với bản Mobile. Xem ghi chú giả định ở bản Mobile.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Blood Type Card | Metric tile (chỉ đọc) | Nhóm máu | BR-030 | [Giả định] |
| 3 | Chronic Conditions List | Table/List (chỉ đọc) | Bệnh mãn tính/lưu ý y tế | BR-030 | |
| 4 | Emergency Contact Note | Text (chỉ đọc) | Ghi chú liên hệ khẩn | — | [Giả định] |
| 5 | Read-only Notice | Alert (info) | Quyền chỉnh sửa thuộc Medical Staff/Phụ huynh | BR-172 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có thông tin sức khỏe được khai báo".
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Student Detail (tab Health Information).
**Bố cục**: Topbar + Sidebar + Content: Student Identity Header + Blood Type Card + Chronic Conditions Table + Emergency Contact Note + Alert read-only.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Health Information, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Student Identity Header + Card nhóm máu + Table bệnh mãn tính + note liên hệ khẩn + Alert.
Thành phần bắt buộc: Student Identity Header, Blood Type Card, Chronic Conditions List, Emergency Contact Note, Read-only Notice.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm field/nút chỉnh sửa.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011 (đối chiếu cấu trúc, bản Parent).
- `traceability_matrix.md` §1.3 UC-145.
- `business_rules.md` BR-030, BR-172.

# Screen Spec — Special Meal Requirements

**Actor**: Teacher | **UC liên quan**: UC-143 - View Special Meal Requirements | **Status**: Draft
**BR liên quan**: BR-125, BR-065, BR-066, BR-082
**MSG liên quan**: MSG-004, MSG-005

> Bản Web, chỉ đọc — dùng chung field/BR/MSG với bản Mobile.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Special Meal Requirements — Class [Tên lớp]" | BR-125 | |
| 2 | Special Meal Student Table | `Table` | Cột: Họ tên, loại chế độ ăn, lý do, trạng thái duyệt | BR-065, BR-066 | |
| 3 | Approval Status Badge | Badge | success/warning | BR-065 | |
| 4 | Preparation Note | Text (chỉ đọc) | Ghi chú chế biến riêng | BR-082 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Lớp không có học sinh cần chế độ ăn đặc biệt".
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" (theo `12_Teacher_Screen_Flow_Web.md`), hoặc từ Student Detail.
**Bố cục**: Topbar + Sidebar + Content: Page Header + `Table` danh sách học sinh chế độ ăn đặc biệt.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Requirements, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Table (tên, loại chế độ ăn, lý do, trạng thái duyệt).
Thành phần bắt buộc: Special Meal Student Table, Approval Status Badge, Preparation Note.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm nút chỉnh sửa.
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-143.
- `business_rules.md` BR-065, BR-066, BR-082, BR-125.
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.

# Screen Spec — Meal Records

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History; UC-080 - Student Meal Consumption | **Status**: Draft
**BR liên quan**: BR-121, BR-125, BR-133
**MSG liên quan**: MSG-004, MSG-005, MSG-021

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Meal Records" + filter chọn học sinh/cả lớp | UC-056 | |
| 2 | Date Range Filter | DatePicker | Lọc khoảng ngày | UC-056 AF1 | |
| 3 | Meal History Table | `Table` | Ngày, món ăn, trạng thái tiêu thụ | BR-121 | |
| 4 | Consumption Record Input | Radio group (trong Dialog/Sheet) | Fully/Partially/Not Consumed | BR-121, BR-125 | Chỉ học sinh chế độ ăn đặc biệt |
| 5 | Save Consumption Button | Button (primary) | Validate, MSG-021 nếu trùng | BR-121 | |
| 6 | Special Meal Compliance Badge | Badge | Tuân thủ chế độ ăn đặc biệt | BR-125 | |

### Trạng thái màn hình
- Loading: `Skeleton` bảng.
- Empty: "Chưa có dữ liệu bữa ăn trong khoảng thời gian đã chọn".
- Error: `Alert variant="destructive"` + MSG-004/005; MSG-021 khi trùng lặp.
- Success: `Toast` `success`.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" hoặc từ Student Detail.
**Bố cục**: Topbar + Sidebar + Content: Page Header + Date Range Filter + `Table` lịch sử bữa ăn; ghi nhận tiêu thụ mở qua `Dialog` ngắn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Records, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Date Range Filter + Table lịch sử bữa ăn + Dialog ghi nhận tiêu thụ.
Thành phần bắt buộc: Meal History Table, Consumption Record Input, Save Consumption Button, Special Meal Compliance Badge.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-056, UC-080.
- `business_rules.md` BR-121, BR-125, BR-133.
- Report 3.md, dòng 3887–3928.

# Screen Spec — Distribution List

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 1-2) | **Status**: Draft
**BR liên quan**: BR-120, BR-121, BR-123, BR-130
**MSG liên quan**: MSG-004, MSG-005, MSG-021, MSG-045

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Meal Distribution" | — | |
| 2 | Status Filter Tabs | Tabs | All (N)/Pending/Served/Special Meal | BR-120 | |
| 3 | Student Meal Status Table | `Table` | Avatar, họ tên, tag chế độ ăn, cảnh báo dị ứng (đỏ), status, action "Xem chi tiết" | BR-123, BR-125 | |
| 4 | Confirm All Button | Button (primary, góc trên phải) | Xác nhận toàn lớp đã phục vụ | BR-120 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có phiên phân phát bữa ăn hôm nay".
- Error: `Alert variant="destructive"` + MSG-004/005; MSG-021 khi trùng lặp.
- Success: `Toast` `success` (MSG-045).

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" (theo `12_Teacher_Screen_Flow_Web.md`). Ra: Distribution Detail, Distribution Confirmation.
**Bố cục**: Topbar + Sidebar + Content: Page Header (action "Confirm All" góc trên phải) + Status Filter Tabs + `Table` học sinh.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution List, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header (nút Confirm All) + Status Filter Tabs + Table học sinh (avatar, tên, tag chế độ ăn, cảnh báo dị ứng, status, action).
Thành phần bắt buộc: Status Filter Tabs, Student Meal Status Table, Confirm All Button.
Áp dụng Design System: cảnh báo dị ứng màu danger (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349 (bản Mobile gốc).
- `traceability_matrix.md` §1.9 UC-016.
- `business_rules.md` BR-120, BR-121, BR-123, BR-130.

# Screen Spec — Distribution Detail

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 3), UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-120, BR-123, BR-125
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Avatar, họ tên, lớp | — | |
| 2 | Assigned Meal Card | Card | Món ăn được phân công | BR-125 | |
| 3 | Allergy Warning Banner | Alert (destructive, nếu có) | "Warning: [Allergen] Allergy" | BR-123 | |
| 4 | Distribution Status Badge | Badge | Pending/Served | BR-120 | |
| 5 | Confirm Served Button | Button (primary) | Mở Distribution Confirmation (Dialog) | BR-120, BR-130 | |
| 6 | Report Distribution Error Button | Button (secondary/ghost) | Báo lỗi phân phát sai (UC-016 AF1) | BR-065 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Empty/Success: không áp dụng tại màn này.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Distribution List (action "Xem chi tiết"). Ra: Distribution Confirmation (Dialog).
**Bố cục**: Topbar + Sidebar + Content: Student Identity Header + Assigned Meal Card (col-span-6) + Allergy Warning Banner + Distribution Status Badge + 2 nút hành động.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Detail, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Student Identity Header + Assigned Meal Card + Allergy Warning Alert + Status Badge + nút Confirm Served + nút báo lỗi.
Thành phần bắt buộc: Student Identity Header, Assigned Meal Card, Allergy Warning Banner, Distribution Status Badge, Confirm Served Button, Report Distribution Error Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349, dòng 3799–3819 (UC-016).
- `business_rules.md` BR-120, BR-123, BR-125, BR-065.

# Screen Spec — Distribution Confirmation

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-120, BR-130
**MSG liên quan**: MSG-021, MSG-045

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Confirmation Dialog Title | Text | "Xác nhận đã phục vụ [Tên học sinh]?" / "Xác nhận cả lớp?" | BR-120 | |
| 2 | Timestamp Display | Text (tự động) | Timestamp hiện tại | BR-120 | |
| 3 | Duplicate Scan Check | Validation ẩn | Chặn xác nhận trùng | BR-130 | MSG-021 |
| 4 | Cancel Button | Button (secondary) | Đóng dialog | — | |
| 5 | Confirm Button | Button (primary) | Lưu, cập nhật Served | BR-120 | MSG-045 |

### Trạng thái màn hình
- Loading: Spinner trong dialog.
- Error: `AlertDialog` + MSG-021.
- Success: `Toast` `success` (MSG-045).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Distribution Detail hoặc action nhanh trên Distribution List. Ra: về Distribution List.
**Bố cục**: `AlertDialog` giữa màn hình — tiêu đề, timestamp tự động, 2 nút Cancel/Confirm.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Confirmation, nền tảng Web, actor Teacher.
Bố cục: AlertDialog giữa màn hình — tiêu đề xác nhận + timestamp + 2 nút Cancel/Confirm.
Thành phần bắt buộc: Confirmation Dialog Title, Timestamp Display, Cancel Button, Confirm Button.
Trạng thái cần thể hiện: loading, error (MSG-021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- Report 3.md, dòng 3799–3819 (UC-016).
- `business_rules.md` BR-120, BR-130; `error_message.md` MSG-021, MSG-045.

# Screen Spec — Upload Evidence

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (thành phần "Attached Photos") | **Status**: Draft
**BR liên quan**: BR-013 (giới hạn dung lượng file)
**MSG liên quan**: MSG-008, MSG-009

> Bản Web — không áp dụng offline (Web luôn yêu cầu kết nối, `Responsive_Guidelines.md`/`UI_Design_Guidelines.md` mục 5). Field/BR dùng chung với bản Mobile trừ phần đồng bộ offline.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | File Upload Input | `Input type="file"` (kéo-thả + chọn file) | Chọn ảnh/tài liệu từ máy tính | BR-013 | |
| 2 | File Size Validation | Validation | Tối đa 10MB/ảnh | BR-013 | MSG-008 |
| 3 | File Type Validation | Validation | Chỉ PDF/JPG/PNG | — | MSG-009 |
| 4 | Photo Preview Thumbnail List | List ảnh + nút xoá | Xem trước, xoá từng ảnh | — | |
| 5 | Attach & Continue Button | Button (primary) | Xác nhận đính kèm, quay lại Incident Form | — | |

### Trạng thái màn hình
- Loading: `Skeleton`/progress bar khi tải lên.
- Empty: "Chưa có ảnh nào được đính kèm".
- Error: `Alert variant="destructive"` + MSG-008/MSG-009.
- Success: ảnh hiển thị trạng thái đã tải lên thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Incident Form (khối "Attached Photos"). Ra: quay lại Incident Form.
**Bố cục**: `Dialog`/`Sheet` ngắn nổi trên Incident Form — vùng kéo-thả file phía trên, danh sách thumbnail lưới bên dưới, nút "Attach & Continue" góc dưới phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Evidence, nền tảng Web, actor Teacher.
Bố cục: Dialog/Sheet với vùng kéo-thả file + lưới thumbnail ảnh đã chọn + nút Attach & Continue.
Thành phần bắt buộc: File Upload Input, Photo Preview Thumbnail List, Attach & Continue Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-008/009), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386.
- `business_rules.md` BR-013; `error_message.md` MSG-008, MSG-009.

# Screen Spec — Incident Form

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-023, MSG-048

> **Ghi chú mâu thuẫn nguồn**: xem giải thích đầy đủ ở bản Mobile cùng tên — wireframe gốc (Report 3 dòng 6367–6386) thiếu 3 field mà Normal Flow UC-074 yêu cầu (Number of Affected Students, Suspected Dishes, Ingestion Date), đã bổ sung theo UC.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Health Incident Report" | — | |
| 2 | Student Input Field | `Select` (multi, autocomplete) | Bắt buộc | BR-146 | |
| 3 | Class Input Field | Input (chỉ đọc) | Pre-filled | — | |
| 4 | Number of Affected Students Input | Input (số) | Bắt buộc | BR-146, BR-154 | Nguồn: Normal Flow UC-074 |
| 5 | Suspected Dishes Input | `Select` (từ thực đơn ngày) | Bắt buộc | BR-151, BR-154 | Nguồn: Normal Flow UC-074 |
| 6 | Ingestion Date Input | `DatePicker` | Bắt buộc | BR-154 | Nguồn: Normal Flow UC-074 |
| 7 | Detected Symptoms Checkboxes | Checkbox group | ≥1 lựa chọn + "Other" | BR-146 | |
| 8 | Severity Level Radio Group | `RadioGroup` | Mild/Medium/Severe | BR-147 | |
| 9 | Incident Description Input | `Textarea` | Bắt buộc | — | MSG-023 |
| 10 | Attached Photos Component | `Input type="file"` | Tuỳ chọn — xem `11_UploadEvidence.md` | — | |
| 11 | Cancel Button | Button (ghost) | Huỷ | — | |
| 12 | Submit Incident Report Button | Button (primary) | Validate đầy đủ | BR-146, BR-149, BR-151 | MSG-023/MSG-048 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Error: `Alert variant="destructive"` + MSG-023.
- Success: `Toast` `success` + MSG-048.
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sự cố Sức khỏe" (theo `12_Teacher_Screen_Flow_Web.md`). Ra: Incident History sau khi submit.
**Bố cục**: Topbar + Sidebar + Content: form dài chia 2 cột (Student/Class/Symptoms bên trái, Severity/Description/Photos bên phải) — không dùng Stepper vì form không quá dài; nút Cancel/Submit cố định cuối form.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident Form, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + form 2 cột (Student/Class/Symptoms | Severity/Description/Photos) + nút Cancel/Submit.
Thành phần bắt buộc: Student Input Field, Class Input Field, Number of Affected Students Input, Suspected Dishes Input, Ingestion Date Input, Detected Symptoms Checkboxes, Severity Level Radio Group, Incident Description Input, Attached Photos Component, Cancel Button, Submit Incident Report Button.
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386 (wireframe gốc), dòng 4507–4529 (UC-074 Normal/Alternative/Exception Flow).
- `business_rules.md` BR-146–151, BR-154, BR-155; `error_message.md` MSG-023, MSG-048.

# Screen Spec — Incident History

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (xem lại báo cáo đã gửi) | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Incident History" | — | |
| 2 | Assessment Filter Tabs | Tabs | All/Pending Assessment/Under Assessment | BR-147 | |
| 3 | Incident Log Table | `Table` | Học sinh, triệu chứng, mức độ, ngày báo cáo, badge trạng thái | BR-147, BR-155 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Bạn chưa gửi báo cáo sự cố nào".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sự cố Sức khỏe" (theo `12_Teacher_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Page Header + Assessment Filter Tabs + `Table` phân trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident History, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Filter Tabs + Table báo cáo sự cố.
Thành phần bắt buộc: Assessment Filter Tabs, Incident Log Table.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG thêm action chỉnh sửa/đóng sự cố.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6388–6402 (bản Mobile gốc).
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-146, BR-147, BR-154, BR-155.
