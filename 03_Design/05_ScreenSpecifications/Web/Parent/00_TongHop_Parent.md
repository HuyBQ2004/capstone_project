# Tổng hợp Screen Specifications — Parent (Parent)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Parent Dashboard

**Actor**: Parent | **UC liên quan**: — (entry point) | **Status**: Draft
**BR liên quan**: BR-031, BR-123, BR-138
**MSG liên quan**: MSG-004, MSG-005

> Xem ghi chú về BR-040 (giới hạn số con/phụ huynh — không tồn tại trong `business_rules.md` chính thức) ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Child Selector | Dropdown (Topbar) | Chuyển đổi hồ sơ con | BR-031 | Không phải Dashboard riêng — dropdown ở Topbar (`10_Parent_Screen_Flow_Web.md`) |
| 2 | Notification Bell Icon | Icon button | Mở Notification Center | — | |
| 3 | Allergy Threat Alert Box | Alert (destructive, có điều kiện) | "Menu Allergy Threat Detected!" | BR-123 | |
| 4 | Today's Lunch Menu Card | Card | Món ăn hôm nay + link chi tiết tuần | BR-070 | |
| 5 | Health Hub / School Meals / Meal Feedback / Meal History Tiles | Card grid (4 ô) | Điều hướng 4 khu vực chính | — | |
| 6 | Report Urgent Safety Incident Button | Button (destructive, full-width) | → Report Food Safety Concern | BR-146, BR-138 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa liên kết hồ sơ học sinh" + hướng dẫn nhập mã học sinh.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sau Common Entry Flow (Login Web) — `10_Parent_Screen_Flow_Web.md`.
**Bố cục**: Topbar (Child Selector + Notif + Avatar) + Sidebar (5 nhóm: Thực đơn & Dinh dưỡng / Sức khỏe & Dị ứng / Bữa ăn của con / Phản hồi & Sự cố / Cá nhân) + Content: Allergy Alert (nếu có) + Today's Lunch Menu Card + Card grid 4 ô + nút báo cáo khẩn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Parent Dashboard, nền tảng Web, actor Parent.
Bố cục: Topbar (Child Selector + Notif) + Sidebar 5 nhóm + Content (Allergy Alert, Today's Lunch Menu Card, 4-card grid, nút báo cáo khẩn destructive).
Thành phần bắt buộc: Allergy Threat Alert Box, Today's Lunch Menu Card, 4 Navigation Tile, Report Urgent Safety Incident Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5952–5971 (bản Mobile gốc).
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-031, BR-123, BR-138, BR-146.

# Screen Spec — Weekly Menu

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information | **Status**: Draft
**BR liên quan**: BR-031, BR-062, BR-069, BR-070, BR-075, BR-123
**MSG liên quan**: MSG-004, MSG-005, MSG-051

> Xem ghi chú mâu thuẫn MSG-076 ở bản Mobile cùng tên (không có MSG chính thức cho "chưa có menu published").

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Weekly Days Tab Bar | Tabs | Mon-Fri | UC-135 bước 3 | |
| 2 | Standard Meal Item Card | Card | Tên món, nguyên liệu, tag "Lunch" | BR-069 | |
| 3 | Allergy Alert Meal Card | Card (viền destructive, có điều kiện) | Tên món, allergen, tag cảnh báo | BR-123 | MSG-051 |
| 4 | View Nutrition Details Button | Button (primary) | → Nutrition History | BR-062 | |
| 5 | Download PDF Button | Button (secondary) | Xuất PDF menu | UC-135 bước 6 | [Giả định vị trí UI] |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Không có menu được công bố cho tuần này" [Giả định — không có MSG code chính thức].
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Thực đơn & Dinh dưỡng" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Weekly Days Tab Bar + Card grid món ăn (col-span-4/dish) + nút Download PDF góc trên phải + nút View Nutrition Details.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Weekly Menu, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Weekly Days Tab Bar + Card grid món ăn + nút Download PDF + nút View Nutrition Details.
Thành phần bắt buộc: Weekly Days Tab Bar, Standard Meal Item Card, Allergy Alert Meal Card, View Nutrition Details Button, Download PDF Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6051–6068, dòng 2999–3018 (UC-135).
- `business_rules.md` BR-031, BR-062, BR-069, BR-070, BR-075, BR-123; `error_message.md` MSG-051.

# Screen Spec — Nutrition Detail

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information (bước 4) | **Status**: Draft
**BR liên quan**: BR-062, BR-069
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Information Card | Card | Tên món, phân loại, nhà cung cấp, bếp | BR-069 | |
| 2 | Full Ingredient Log Section | Text block | Danh sách nguyên liệu | BR-069, BR-076 | |
| 3 | Allergen Warnings Section | Alert (destructive, có điều kiện) | Cảnh báo allergen | BR-069, BR-123 | |
| 4 | Give Feedback Button | Button (primary) | → Submit Meal Feedback | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Empty/Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Weekly Menu (click dish card).
**Bố cục**: Topbar + Sidebar + Content: Dish Information Card (col-span-8) + Full Ingredient Log + Allergen Warnings Alert + nút Give Feedback góc dưới phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition Detail, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Dish Information Card + Ingredient Log + Allergen Warnings Alert + nút Give Feedback.
Thành phần bắt buộc: Dish Information Card, Full Ingredient Log Section, Allergen Warnings Section, Give Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6070–6086.
- `business_rules.md` BR-062, BR-069, BR-076, BR-123.

# Screen Spec — Nutrition History

**Actor**: Parent | **UC liên quan**: — (suy ra, xem ghi chú bản Mobile) | **Status**: Draft
**BR liên quan**: BR-062, BR-121, BR-133
**MSG liên quan**: MSG-004, MSG-005

> [Giả định — cần team xác nhận cấu trúc chi tiết]. Xem ghi chú nguồn đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Nutrition History" | — | |
| 2 | Date Range Filter | Filter (Tuần/Tháng) | Chọn khoảng thời gian | — | [Giả định] |
| 3 | Nutrition Trend Chart | Chart (`recharts`) | Biểu đồ calo/nhóm chất theo ngày | BR-062 | |
| 4 | Nutrition Target Comparison Card | Card | So sánh thực tế vs mục tiêu | BR-062 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa đủ dữ liệu bữa ăn để hiển thị biểu đồ".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Thực đơn & Dinh dưỡng" hoặc từ Weekly Menu/Meal History.
**Bố cục**: Topbar + Sidebar + Content: Date Range Filter + Chart (col-span-8) + Nutrition Target Comparison Card (col-span-4).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition History, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Date Range Filter + Chart dinh dưỡng + Card so sánh mục tiêu.
Thành phần bắt buộc: Date Range Filter, Nutrition Trend Chart, Nutrition Target Comparison Card.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-062, BR-121, BR-133.

# Screen Spec — Submit Allergy Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031, BR-066, BR-172
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ tạo mới — xem ghi chú phân biệt Submit/Update ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Select | `Select` | Bắt buộc | BR-066 | |
| 2 | Specific Allergen Name Input | `Input` (điều kiện "Other") | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | `RadioGroup` | Mild/Moderate/Severe | BR-031 | |
| 4 | Upload Medical Proof Component | `Input type="file"` | Bắt buộc nếu Severe | BR-031 | MSG-036 nếu thiếu |
| 5 | Save Allergy Button | Button (primary) | Validate trước khi lưu | BR-031 | |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-005; [Giả định — chưa có MSG riêng cho thiếu tài liệu Severe].
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng" (`10_Parent_Screen_Flow_Web.md`), từ Student Health Information.
**Bố cục**: Form ngắn (<8 field) hiện trong `Dialog`/`Sheet` theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Allergy Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet ngắn với form (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save Allergy.
Thành phần bắt buộc: Allergen Category Select, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Trạng thái cần thể hiện: loading, error (MSG-005/036), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049, dòng 2087–2107 (UC-084).
- `business_rules.md` BR-031, BR-066, BR-172; `error_message.md` MSG-036.

# Screen Spec — Update Allergy Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ chỉnh sửa — dùng chung UI với `05_SubmitAllergyInformation.md`. Xem ghi chú đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Select | `Select` (pre-filled) | Bắt buộc | — | |
| 2 | Specific Allergen Name Input | `Input` (pre-filled, điều kiện) | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | `RadioGroup` (pre-filled) | Mild/Moderate/Severe | BR-031 | |
| 4 | Upload Medical Proof Component | `Input type="file"` | Bắt buộc nếu chuyển Severe | BR-031 | |
| 5 | Save Allergy Button | Button (primary) | Validate, lưu Pending Verification | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: Spinner submit; `Skeleton` khi tải dữ liệu cũ.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng", từ Student Health Information.
**Bố cục**: `Dialog`/`Sheet` ngắn, field điền sẵn dữ liệu hiện có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Allergy Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với form đã điền sẵn dữ liệu (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save.
Thành phần bắt buộc: Allergen Category Select, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049, dòng 2109–2129 (UC-093).
- `business_rules.md` BR-031; `error_message.md` MSG-036.

# Screen Spec — Student Health Information

**Actor**: Parent | **UC liên quan**: UC-050 - View List Student Health Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Họ tên, tuổi, lớp | BR-031 | |
| 2 | Height/Weight/Blood Type/BMI Card | Metric card grid (4 ô) | Chỉ số sức khỏe | — | |
| 3 | Allergy Profile Summary Table | `Table` hoặc Card | Allergen + mức độ | BR-031 | |
| 4 | Verification Status Badge | Badge | Pending/Verified | BR-031 | |
| 5 | Manage Health Details Button | Button (primary) | → Submit/Update Health Information | — | |
| 6 | Manage Allergy List Button | Button (primary) | → Submit/Update Allergy Information | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa khai báo thông tin sức khỏe" + nút "Khai báo ngay".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Student Summary Card + 4-card metric grid + Allergy Profile Table + 2 nút hành động góc trên phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Health Information, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Student Summary Card + 4-card metric grid + Allergy Profile Table + 2 nút hành động.
Thành phần bắt buộc: Student Summary Card, 4 Metric Card, Allergy Profile Summary Table, Verification Status Badge, Manage Health Details Button, Manage Allergy List Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011, dòng 1999–2019 (UC-050).
- `business_rules.md` BR-031.

# Screen Spec — Submit Health Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031, BR-172
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ tạo mới — dùng chung UI với `09_UpdateHealthInformation.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | `Input` số | Bắt buộc | — | |
| 2 | Weight Input Field | `Input` số | Bắt buộc | — | |
| 3 | Blood Type Select | `Select` | Tuỳ chọn | — | |
| 4 | File Upload Field | `Input type="file"` | Tuỳ chọn | BR-013 | MSG-008/009 |
| 5 | Save Health Info Button | Button (primary) | Validate | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-004/005/008/009.
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng", từ Student Health Information.
**Bố cục**: `Dialog`/`Sheet` ngắn (<8 field) theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Health Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với form (Height, Weight, Blood Type Select, File Upload) + nút Save Health Info.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Select, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/008/009), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030, dòng 2087–2107 (UC-084).
- `business_rules.md` BR-013, BR-031, BR-172; `error_message.md` MSG-008, MSG-009, MSG-036.

# Screen Spec — Update Health Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ chỉnh sửa — dùng chung UI với `08_SubmitHealthInformation.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | `Input` (pre-filled) | Bắt buộc | — | |
| 2 | Weight Input Field | `Input` (pre-filled) | Bắt buộc | — | |
| 3 | Blood Type Select | `Select` (pre-filled) | Tuỳ chọn | — | |
| 4 | File Upload Field | `Input type="file"` (hiện file cũ) | Tuỳ chọn | BR-013 | |
| 5 | Save Health Info Button | Button (primary) | Validate, lưu Pending Verification | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải dữ liệu cũ; Spinner khi submit.
- Error: `Alert variant="destructive"` + MSG-004/005/008/009.
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng", từ Student Health Information.
**Bố cục**: `Dialog`/`Sheet` ngắn, field điền sẵn dữ liệu hiện có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Health Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với form đã điền sẵn (Height, Weight, Blood Type, File Upload) + nút Save.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Select, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030, dòng 2109–2129 (UC-093).
- `business_rules.md` BR-031; `error_message.md` MSG-036.

# Screen Spec — Special Meal Arrangements

**Actor**: Parent | **UC liên quan**: UC-141 - View Special Meal Arrangements | **Status**: Draft
**BR liên quan**: BR-031, BR-065, BR-123
**MSG liên quan**: MSG-004, MSG-005, MSG-051

> Hợp nhất với "Allergy Conflict Alert" — xem ghi chú đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Special Meal Arrangements" | — | |
| 2 | Allergen Warning Card | Alert (destructive) | Mô tả xung đột | BR-123 | MSG-051 |
| 3 | Special Meal Detail Table | `Table`/Card | Allergen, mức độ, món thay thế, ghi chú | BR-065 | |
| 4 | Dual-Confirmation Status | Badge x2 | Kitchen Released / Teacher Hand-off | BR-123 | |
| 5 | Parental Advisory Note | Text (italic) | Ghi chú khuyến cáo | — | |
| 6 | Submit Allergy Information Link | Link (điều kiện) | → Submit Allergy Information | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Học sinh chưa có kế hoạch bữa ăn đặc biệt nào" + link khai báo.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng" (`10_Parent_Screen_Flow_Web.md`), từ Dashboard/Weekly Menu.
**Bố cục**: Topbar + Sidebar + Content: Allergen Warning Alert (full-width) + Table chi tiết bữa ăn đặc biệt + 2 Badge xác nhận kép.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Arrangements, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Allergen Warning Alert + Table chi tiết + 2 Badge xác nhận kép + ghi chú khuyến cáo.
Thành phần bắt buộc: Allergen Warning Card, Special Meal Detail Table, Dual-Confirmation Status, Parental Advisory Note.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4001–4017 (UC-141), dòng 6088–6103.
- `business_rules.md` BR-031, BR-065, BR-123; `error_message.md` MSG-051.

# Screen Spec — Special Meal Records

**Actor**: Parent | **UC liên quan**: UC-142 - View Special Meal Records | **Status**: Draft
**BR liên quan**: BR-065, BR-121, BR-123
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Special Meal Records" | — | |
| 2 | Special Meal Record Table | `Table` | Ngày, món gốc, món thay thế, allergen, badge xác nhận kép | BR-065, BR-123 | |
| 3 | Record Detail Dialog | `Dialog` (khi click hàng) | Ghi chú chuẩn bị, timestamp, trạng thái tiêu thụ | BR-121, BR-123 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có lịch sử bữa ăn đặc biệt".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Bữa ăn của con" (`10_Parent_Screen_Flow_Web.md`), từ Meal History.
**Bố cục**: Topbar + Sidebar + Content: Page Header + `Table` phân trang, click hàng mở `Dialog` chi tiết.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Records, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Table phiên bữa ăn đặc biệt + Dialog chi tiết khi click hàng.
Thành phần bắt buộc: Special Meal Record Table, Record Detail Dialog.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4019–4039 (UC-142).
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-065, BR-121, BR-123.

# Screen Spec — Meal History

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History | **Status**: Draft
**BR liên quan**: BR-031, BR-069, BR-070, BR-121, BR-133
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Meal History" + Date Range Filter | — | |
| 2 | Calendar/Date Filter | `DatePicker` | Lọc theo ngày/khoảng ngày | BR-133 | |
| 3 | Meal History Table | `Table` | Ngày, tỷ lệ tiêu thụ, loại khẩu phần, trạng thái hài lòng, action "Xem chi tiết" | BR-121, BR-133 | |
| 4 | Weekly Charts Button | Button | → Nutrition History | — | |
| 5 | Special Meals History Button | Button | → Special Meal Records | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Không có lịch sử bữa ăn trong khoảng thời gian đã chọn".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Bữa ăn của con" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Page Header + Date Filter + `Table` lịch sử, 2 nút điều hướng góc trên phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Page Header + Date Filter + Table lịch sử bữa ăn + 2 nút điều hướng.
Thành phần bắt buộc: Calendar/Date Filter, Meal History Table, Weekly Charts Button, Special Meals History Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6161–6178, dòng 3975–3995 (UC-133).
- `business_rules.md` BR-031, BR-069, BR-070, BR-121, BR-133.

# Screen Spec — Meal History Detail

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-065, BR-069, BR-121
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | Ngày được chọn | — | |
| 2 | Served Dish Table | `Table`/List | Tên món, nguyên liệu, calo | BR-069 | |
| 3 | Consumption Status Badge | Badge | Fully/Partially/Not Consumed | BR-121 | |
| 4 | Serving Timestamp | Text | Giờ phục vụ | BR-121 | |
| 5 | Special Meal Applied Note | Text (điều kiện) | Ghi chú bữa ăn đặc biệt | BR-065 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Empty/Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Meal History (click hàng bảng/chọn ngày).
**Bố cục**: Topbar + Sidebar + Content: Page Header (ngày) + Table món ăn + Badge trạng thái + timestamp.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History Detail, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Page Header + Table món ăn + Badge trạng thái tiêu thụ + timestamp.
Thành phần bắt buộc: Served Dish Table, Consumption Status Badge, Serving Timestamp, Special Meal Applied Note.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3975–3995 (UC-133).
- `business_rules.md` BR-065, BR-069, BR-121.

# Screen Spec — School Meals Dashboard

**Actor**: Parent | **UC liên quan**: UC-055 - Monitor School Meals | **Status**: Draft
**BR liên quan**: BR-069, BR-070, BR-075
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "School Meals" | — | |
| 2 | Current & Upcoming Week Menu Summary | Card grid | Tóm tắt menu 2 tuần | BR-070 | |
| 3 | Nutrition & Allergen Highlight | Alert/Badge | Cảnh báo allergen liên quan | BR-069 | |
| 4 | View Weekly Menu Link | Button | → Weekly Menu | — | |
| 5 | View Nutrition Breakdown Link | Button | → Nutrition Detail/History | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có thực đơn được công bố" [Giả định].
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Bữa ăn của con" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Card grid tóm tắt menu 2 tuần + Alert allergen + 2 nút điều hướng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Meals Dashboard, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Card grid menu 2 tuần + Alert allergen + 2 nút điều hướng.
Thành phần bắt buộc: Current & Upcoming Week Menu Summary, Nutrition & Allergen Highlight, View Weekly Menu Link, View Nutrition Breakdown Link.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3865–3885 (UC-055).
- `business_rules.md` BR-069, BR-070, BR-075.

# Screen Spec — Feedback Center

**Actor**: Parent | **UC liên quan**: — (màn hub, dẫn tới UC-085, UC-073) | **Status**: Draft
**BR liên quan**: BR-139, BR-146
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Feedback Center" | — | |
| 2 | Recent Meal Feedback Tile | Card/Button | → Submit Meal Feedback | BR-139 | |
| 3 | Food Quality Feedback Tile | Card/Button | → Submit Food Quality Feedback | BR-139 | |
| 4 | Report Safety Concern Tile | Card/Button (destructive) | → Report Food Safety Concern | BR-146 | |
| 5 | My Feedback History Table | `Table` | Lịch sử phản hồi đã gửi + trạng thái | BR-143 | [Giả định] |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có phản hồi nào được gửi".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: 3-card grid (Meal Feedback/Food Quality/Report Safety) + Table lịch sử phản hồi.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Feedback Center, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + 3-card grid (tile Report Safety màu destructive) + Table lịch sử phản hồi.
Thành phần bắt buộc: Recent Meal Feedback Tile, Food Quality Feedback Tile, Report Safety Concern Tile, My Feedback History Table.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `traceability_matrix.md` §1.10 UC-085, §1.11 UC-073.
- `business_rules.md` BR-139, BR-143, BR-146.

# Screen Spec — Submit Meal Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139, BR-140, BR-142, BR-143, BR-145
**MSG liên quan**: MSG-004, MSG-005, MSG-022, MSG-046, MSG-058

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Selection Select | `Select` | Bắt buộc, chỉ món trong 24h | BR-139 | |
| 2 | Rating Score Component | Star rating (1-5) | Bắt buộc | — | |
| 3 | Quality Concern Description | `Input` (text tự do, điều kiện <3 sao) | Bắt buộc nếu <3 sao | BR-140 | Đổi từ Checkbox group sang Input theo yêu cầu team (2026-07-01) — phụ huynh tự mô tả vấn đề (VD "quá mặn", "nguội") thay vì chọn nhãn có sẵn |
| 4 | Review Comments Section | `Textarea` | Tuỳ chọn | BR-142 | |
| 5 | Photo Attachment Component | `Input type="file"` | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary) | Validate, chặn ngoài 24h | BR-139 | MSG-022/046/058 |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-004/005/022.
- Success: `Toast` `success` (MSG-046); `Toast` `warning` nếu MSG-058.
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố", từ Feedback Center/Nutrition Detail.
**Bố cục**: `Dialog`/`Sheet` ngắn — Dish Select, Rating, Tags, Comments, Photo, nút Send Feedback.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Meal Feedback, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với Dish Selection, Rating Score, Quality Concern Description (input, điều kiện), Review Comments, Photo Attachment, nút Send Feedback.
Thành phần bắt buộc: Dish Selection Select, Rating Score Component, Quality Concern Description Input, Review Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài 
# Screen Spec — Submit Food Quality Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139, BR-140, BR-142, BR-143
**MSG liên quan**: MSG-004, MSG-005, MSG-022, MSG-046, MSG-058

> Biến thể không gắn 1 món cụ thể — xem ghi chú nguồn ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Meal Session Selector | `Select` | Chọn phiên ăn trong 24h | BR-139 | |
| 2 | Rating Score Component | Star rating | Bắt buộc | — | |
| 3 | Quality Concern Tags | Checkbox group (điều kiện) | Bắt buộc nếu <3 sao | BR-140 | |
| 4 | General Comments Section | `Textarea` | Tuỳ chọn | BR-142 | |
| 5 | Photo Attachment Component | `Input type="file"` | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary) | Validate | BR-139 | MSG-022/046/058 |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-004/005/022.
- Success: `Toast` `success` (MSG-046); `warning` nếu MSG-058.
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố", từ Feedback Center.
**Bố cục**: `Dialog`/`Sheet` ngắn — Meal Session Selector, Rating, Tags, Comments, Photo, nút Send Feedback.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Food Quality Feedback, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với Meal Session Selector, Rating Score, Quality Concern Tags, General Comments, Photo Attachment, nút Send Feedback.
Thành phần bắt buộc: Meal Session Selector, Rating Score Component, Quality Concern Tags, General Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4109–4129 (UC-085).
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-139, BR-140, BR-142, BR-143; `error_message.md` MSG-022, MSG-046, MSG-058.

# Screen Spec — Report Food Safety Concern

**Actor**: Parent | **UC liên quan**: UC-073 - Report Food Safety Concern | **Status**: Draft
**BR liên quan**: BR-138, BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023, MSG-048

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text (destructive) | "Report Safety Issue" | — | |
| 2 | Concern Category Select | `Select` | Bắt buộc | BR-146 | |
| 3 | Symptoms Checkbox Group | Checkbox group | Stomach Pain/Vomiting/Diarrhea/Fever | BR-146 | |
| 4 | Meal Date Input | `DatePicker` | Bắt buộc | BR-146 | Nguồn: Normal Flow UC-073 |
| 5 | Suspected Dish Input | `Select` | Bắt buộc | BR-146 | Nguồn: Normal Flow UC-073 |
| 6 | Incident Details Section | `Textarea` | Bắt buộc | — | MSG-023 |
| 7 | Photo Attachment Component | `Input type="file"` (bắt buộc) | Bắt buộc | — | MSG-023 |
| 8 | Send Urgent Report Button | Button (destructive) | Validate, gửi ngay (BR-138) | BR-146, BR-149, BR-151 | MSG-023/048 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Error: `Alert variant="destructive"` + MSG-023.
- Success: `Toast` `success` (MSG-048).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố" (đặt cố định, dễ thấy — không chỉ ẩn trong menu, theo `10_Parent_Screen_Flow_Web.md` "Khác biệt so với bản Mobile").
**Bố cục**: Topbar + Sidebar + Content: form đơn (không Stepper — <8 field) với toàn bộ nhấn mạnh màu destructive, nút Send Urgent Report nổi bật.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Report Food Safety Concern, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + form đơn (Concern Category, Symptoms, Meal Date, Suspected Dish, Incident Details, Photo Attachment) + nút Send Urgent Report màu destructive.
Thành phần bắt buộc: Concern Category Select, Symptoms Checkbox Group, Meal Date Input, Suspected Dish Input, Incident Details Section, Photo Attachment Component, Send Urgent Report Button.
Áp dụng Design System: nhấn mạnh màu danger toàn màn hình.
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6142–6159, dòng 4485–4505 (UC-073).
- `business_rules.md` BR-138, BR-146–151, BR-154, BR-155; `error_message.md` MSG-023, MSG-048.
