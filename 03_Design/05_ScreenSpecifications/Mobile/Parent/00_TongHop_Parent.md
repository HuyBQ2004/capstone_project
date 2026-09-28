# Tổng hợp Screen Specifications — Parent (Parent)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Parent Dashboard

**Actor**: Parent | **UC liên quan**: — (entry point, không gắn UC cụ thể) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-123 (cảnh báo dị ứng), BR-138 (cảnh báo quan trọng bỏ qua duyệt)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú về BR-040**: một số UC liên quan Parent trong Report 3 (UC-050, UC-093, UC-135, UC-141) trích dẫn "BR-040 (giới hạn tối đa 5 học sinh/phụ huynh)" — nhưng theo `business_rules.md` (nguồn BR chính thức đã chốt, xem `00_UI_Design_Documentation_Plan.md` mục 2.4), BR-040 thực tế là "Quy trình đăng ký nhà cung cấp", không liên quan giới hạn số con/phụ huynh. Đây là lệch số ID giữa Report 3 (cũ) và business_rules.md (đã duyệt) — không có BR nào trong business_rules.md hiện hành quy định giới hạn số học sinh/phụ huynh. **[Giả định — cần team xác nhận: giữ giới hạn 5 học sinh/phụ huynh theo UC gốc hay bỏ giới hạn cho tới khi có BR chính thức]**. Toàn bộ Screen Spec Parent trong bộ này không trích BR-040 cho quy tắc giới hạn số con.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Child Selector Dropdown | Dropdown | "Child: [Tên học sinh] (Lớp...)" | BR-031 | Chuyển đổi hồ sơ con — xem [Giả định] về giới hạn số lượng ở trên |
| 2 | Notification Bell Icon | Icon button | Mở Notification Center | — | |
| 3 | Allergy Threat Alert Box | Alert (danger, chỉ hiện khi có xung đột) | "Menu Allergy Threat Detected!" | BR-123 | Tap → Special Meal Arrangements |
| 4 | Today's Lunch Menu Card | Card | Món ăn hôm nay + link "view weekly details" | BR-070 | → Weekly Menu |
| 5 | Health Hub Navigation Tile | Button | → Student Health Information | — | |
| 6 | School Meals Navigation Tile | Button | → School Meals Dashboard | — | |
| 7 | Meal Feedback Navigation Tile | Button | → Feedback Center | — | |
| 8 | Meal History Navigation Tile | Button | → Meal History | — | |
| 9 | Report Urgent Safety Incident Button | Button (danger, dashed border, full-width) | → Report Food Safety Concern | BR-146, BR-138 | Bypass duyệt (BR-138) |
| 10 | Bottom Navigation Bar | Nav | 4 tab: Home (active), Meals, Health, Profile | — | |

### Trạng thái màn hình
- Loading: `Shimmer` toàn bộ card.
- Empty: nếu phụ huynh chưa liên kết học sinh nào → "Chưa liên kết hồ sơ học sinh" + hướng dẫn nhập mã học sinh (theo UC-135 EX2).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Điểm vào sau Common Entry Flow (Login) — xem `05_Parent_Screen_Flow.md`. Điều hướng đi: Weekly Menu, Student Health Hub, School Meals Dashboard, Feedback Center, Meal History, Report Safety Issue, Notifications.
**Bố cục**: AppBar (Child Selector trái, Notification Bell phải) → Allergy Threat Alert Box (nếu có) → Today's Lunch Menu Card → lưới 4 Navigation Tile (2x2) → Report Urgent Safety Incident Button → `BottomNavigationBar` 4 mục.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Parent Dashboard, nền tảng Mobile, actor Parent.
Bố cục: AppBar (Child Selector + Notification Bell) + Allergy Threat Alert (đỏ, có điều kiện) + Today's Lunch Menu Card + lưới 4 Navigation Tile 2x2 + nút Report Urgent Safety Incident (viền đỏ nét đứt) + BottomNavigationBar 4 mục.
Thành phần bắt buộc: Child Selector Dropdown, Notification Bell Icon, Allergy Threat Alert Box, Today's Lunch Menu Card, Health Hub/School Meals/Meal Feedback/Meal History Navigation Tile, Report Urgent Safety Incident Button.
Áp dụng Design System: Allergy Threat = danger (BR-123), nút báo cáo khẩn = danger viền nét đứt.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5952–5971 (Parent Dashboard, field-by-field gốc).
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md`, `10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-031, BR-123, BR-138, BR-146.
- `traceability_matrix.md` §1.6 UC-135
# Screen Spec — Weekly Menu

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information | **Status**: Draft
**BR liên quan**: BR-031, BR-062 (mục tiêu dinh dưỡng theo nhóm tuổi), BR-069 (khai báo đầy đủ nguyên liệu/dị ứng), BR-070 (chỉ menu Published), BR-075 (FEFO — truy xuất nguyên liệu)
**MSG liên quan**: MSG-004, MSG-005, MSG-051 (cảnh báo xung đột dị ứng)

> **Ghi chú mâu thuẫn nguồn**: UC-135 (Report 3 dòng 3012, Exception EX1 "No Menu Published") trích dẫn "MSG-076" cho trường hợp không có menu công bố — nhưng theo `error_message.md` (nguồn MSG chính thức), MSG-076 thực tế là "Account temporarily locked due to too many failed login attempts" (khóa tài khoản, BR-002), không liên quan tới menu. Đây là lệch mã MSG giữa Report 3 (cũ) và `error_message.md` hiện hành — **không có MSG chính thức nào cho trạng thái "chưa có menu published"** trong bộ MSG hiện tại. Trạng thái Empty dưới đây dùng câu chữ mô tả tự do, không gán MSG code, và đánh dấu **[Giả định — cần team xác nhận/bổ sung MSG mới]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Weekly Days Tab Bar | Tabs | Mon-Fri, mặc định "Mon" | UC-135 bước 3 | |
| 2 | Standard Meal Item Card | Card | Tên món, nguyên liệu, tag "Lunch" | BR-069 | |
| 3 | Allergy Alert Meal Card | Card (viền đỏ, chỉ hiện nếu xung đột) | Tên món (đỏ), "Contains: [Allergen]", tag "Allergy Alert", link xem thay thế | BR-123 | MSG-051; tap → Special Meal Arrangements |
| 4 | View Nutrition Details Button | Button (primary, full-width) | → Nutrition History (biểu đồ dinh dưỡng tuần) | BR-062 | |
| 5 | Download PDF Button | Button (secondary) | Xuất bản in menu | UC-135 bước 6 | [Giả định vị trí UI cụ thể — cần xác nhận] |
| 6 | Child Selector (kế thừa từ Dashboard) | Dropdown | Đổi hồ sơ con → cập nhật cảnh báo dị ứng theo đúng con | BR-031 | UC-135 AF1 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Không có menu được công bố cho tuần này" (block truy cập theo UC-135 EX1) — [Giả định — không có MSG code chính thức, xem ghi chú mâu thuẫn nguồn ở trên].
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Tap to view weekly details"). Ra: Nutrition Detail (tap món ăn), Special Meal Arrangements (tap Allergy Alert Card), Nutrition History.
**Bố cục**: AppBar (back) → Weekly Days Tab Bar → danh sách `Card` món ăn theo ngày → nút "View Nutrition Details" cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Weekly Menu, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Weekly Days Tab Bar (Mon-Fri) + danh sách Card món ăn (đa số Card thường, 1 Card viền đỏ nếu xung đột dị ứng) + nút View Nutrition Details cố định đáy.
Thành phần bắt buộc: Weekly Days Tab Bar, Standard Meal Item Card, Allergy Alert Meal Card, View Nutrition Details Button.
Áp dụng Design System: Allergy Alert = danger/đỏ bắt buộc (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty (MSG-076), error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6051–6068 (Weekly Menu, field-by-field gốc), dòng 2999–3018 (UC-135 Normal/Alternative/Exception Flow).
- `business_rules.md` BR-031, BR-062, BR-069, BR-070, BR-075,
# Screen Spec — Nutrition Detail

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information (bước 4, chi tiết dinh dưỡng) | **Status**: Draft
**BR liên quan**: BR-062 (mục tiêu dinh dưỡng theo nhóm tuổi), BR-069 (khai báo đầy đủ nguyên liệu/dị ứng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Information Card | Card | Tên món, phân loại món, nhà cung cấp catering, bếp chế biến | BR-069 | Nguồn: Report 3 §3.3.2.4 Dish Details |
| 2 | Full Ingredient Log Section | Text block | Danh sách nguyên liệu đầy đủ kèm nguồn gốc | BR-069, BR-076 (truy xuất lô) | |
| 3 | Allergen Warnings Section | Alert (danger, có điều kiện) | "Contains [Allergen]" / "(Shellfish/Peanut free)" | BR-069, BR-123 | |
| 4 | Give Feedback Button | Button (primary, full-width) | → Submit Meal Feedback (cho đúng món này) | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng (luôn có dữ liệu khi vào từ Weekly Menu).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Weekly Menu (tap món ăn). Ra: Submit Meal Feedback.
**Bố cục**: AppBar (back) → Dish Information Card → Full Ingredient Log Section → Allergen Warnings Section (nếu có) → Give Feedback Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition Detail, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Dish Information Card + Full Ingredient Log + Allergen Warnings (đỏ, có điều kiện) + nút Give Feedback cố định đáy.
Thành phần bắt buộc: Dish Information Card, Full Ingredient Log Section, Allergen Warnings Section, Give Feedback Button.
Áp dụng Design System: Allergen Warnings = danger.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6070–6086 (Dish Details, field-by-field gốc).
- `business_rules.md` BR-062, BR-069, BR-076, BR-123.

# Screen Spec — Nutrition History

**Actor**: Parent | **UC liên quan**: — (không có UC riêng trong `traceability_matrix.md`; suy ra từ nút "View Nutrition Details"/"Weekly Charts") | **Status**: Draft
**BR liên quan**: BR-062 (mục tiêu dinh dưỡng theo nhóm tuổi), BR-121 (theo dõi lịch sử bữa ăn), BR-133 (tóm tắt bữa ăn hàng ngày)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe field-by-field riêng trong Report 3 cho "Nutrition History" — theo `05_Parent_Screen_Flow.md`, đây là màn đích của nút "Weekly Charts" trên Meal History Log (ghi chú "≈ Nutrition History") và nút "View Nutrition Details" trên Weekly Menu. Field dưới đây suy ra từ mục đích hiển thị biểu đồ dinh dưỡng tuần/tháng dựa trên BR-062, không có field-by-field gốc. **[Giả định — cần team xác nhận cấu trúc chi tiết]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Nutrition History" | — | |
| 2 | Date Range Filter | Filter (Tuần/Tháng) | Chọn khoảng thời gian xem biểu đồ | — | [Giả định] |
| 3 | Nutrition Trend Chart | Chart (`fl_chart`) | Biểu đồ calo/nhóm chất theo ngày, đối chiếu mục tiêu dinh dưỡng theo tuổi | BR-062 | [Giả định — cần xác nhận loại biểu đồ] |
| 4 | Nutrition Target Comparison Card | Card | So sánh thực tế vs mục tiêu (calo, đạm, chất xơ...) | BR-062 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa đủ dữ liệu bữa ăn để hiển thị biểu đồ".
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Weekly Menu ("View Nutrition Details") hoặc Meal History ("Weekly Charts") — xem `05_Parent_Screen_Flow.md`.
**Bố cục**: AppBar (back) → Date Range Filter → Nutrition Trend Chart → Nutrition Target Comparison Card.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition History, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Date Range Filter + biểu đồ xu hướng dinh dưỡng + Card so sánh mục tiêu.
Thành phần bắt buộc: Date Range Filter, Nutrition Trend Chart, Nutrition Target Comparison Card.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` ("Weekly Charts (≈ Nutrition History)").
- `business_rules.md` BR-062, BR-121, BR-133 (không có UC riêng — xem ghi chú nguồn ở trên).

# Screen Spec — Submit Allergy Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031 (liên kết học sinh–phụ huynh), BR-172 (bảo vệ toàn vẹn nhật ký), BR-066 (danh mục chế độ ăn đặc biệt)
**MSG liên quan**: MSG-004, MSG-005, MSG-036 (cập nhật hồ sơ dị ứng thành công)

> Đây là màn "Add/Edit Allergy" (Report 3 §3.3.2.4) ở **chế độ tạo mới (Submit)** — khác với `06_UpdateAllergyInformation.md` (chế độ chỉnh sửa hồ sơ đã có). Cùng 1 form UI, khác action/endpoint theo đúng UC-084 (tạo mới, trạng thái "Pending Verification") vs UC-093 (cập nhật, giữ nguyên trạng thái "Pending Verification" sau khi sửa).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Dropdown | Dropdown | Bắt buộc chọn danh mục (Peanuts, Seafood...) | BR-066 | |
| 2 | Specific Allergen Name Input | Input (chỉ hiện khi chọn "Other") | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | Radio group | Mild/Moderate/Severe | BR-031 | |
| 4 | Upload Medical Proof Component | File upload (camera/gallery) | **Bắt buộc nếu Severity = Severe** | BR-031 | MSG-036 nếu thiếu khi Severe (UC-084 AF1 — chặn submit) |
| 5 | Save Allergy Button | Button (primary, full-width) | Validate đầy đủ trước khi lưu | BR-031 | Lưu với trạng thái "Pending Verification", thông báo Medical Staff |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng (form tạo mới).
- Error: `SnackBar` `danger` + MSG-004/005; chặn submit nếu Severe thiếu tài liệu (không có MSG code riêng cho AF1 trong `error_message.md` — dùng MSG-005 dạng validation hoặc [Giả định — cần bổ sung MSG riêng]).
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Allergy List" khi chưa có allergen nào). Ra: về Student Health Information sau khi lưu.
**Bố cục**: AppBar (back) → form 1 cột: Allergen Category → Specific Name (nếu Other) → Severity Radio → Upload Medical Proof → Save Allergy Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Allergy Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save Allergy cố định đáy.
Thành phần bắt buộc: Allergen Category Dropdown, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Áp dụng Design System: Severe = danger (đỏ, mặc định được chọn theo wireframe gốc).
Trạng thái cần thể hiện: loading, error (MSG-005/036 thiếu tài liệu), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049 (Add/Edit Allergy, field-by-field gốc), dòng 2087–2107 (UC-084 Normal/Alternative Flow).
- `business_rules.md` BR-031, BR-066, BR-172; `error_message.md` MSG-036.

# Screen Spec — Update Allergy Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031 (liên kết học sinh–phụ huynh)
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Cùng UI với `05_SubmitAllergyInformation.md` (Add/Edit Allergy, Report 3 dòng 6032–6049) nhưng ở **chế độ chỉnh sửa** — form hiện sẵn dữ liệu allergen đã khai báo trước đó, action lưu là "Update" thay vì "Create". Theo UC-093: sau khi sửa, hồ sơ chuyển lại trạng thái "Pending Verification" và Medical Staff được thông báo lại.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Dropdown | Dropdown (pre-filled) | Bắt buộc | — | |
| 2 | Specific Allergen Name Input | Input (pre-filled, điều kiện Other) | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | Radio group (pre-filled) | Mild/Moderate/Severe | BR-031 | Nếu đổi sang Severe → bắt buộc upload lại tài liệu y tế mới (UC-093 bước 3) |
| 4 | Upload Medical Proof Component | File upload | Bắt buộc nếu chuyển sang Severe | BR-031 | |
| 5 | Save Allergy Button | Button (primary, full-width) | Validate, lưu về "Pending Verification" | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: Spinner khi submit; `Shimmer` khi tải dữ liệu cũ.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005; chặn lưu nếu thiếu tài liệu khi chuyển Severe (UC-093 Exceptions).
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Allergy List", khi đã có allergen). Ra: về Student Health Information.
**Bố cục**: Giống `05_SubmitAllergyInformation.md` nhưng field đã điền sẵn dữ liệu hiện có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Allergy Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột đã điền sẵn dữ liệu (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save Allergy cố định đáy.
Thành phần bắt buộc: Allergen Category Dropdown, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049 (wireframe gốc dùng chung), dòng 2109–2129 (UC-093 Normal Flow).
- `business_rules.md` BR-031; `error_message.md` MSG-036.

# Screen Spec — Student Health Information

**Actor**: Parent | **UC liên quan**: UC-050 - View List Student Health Information | **Status**: Draft
**BR liên quan**: BR-031 (liên kết học sinh–phụ huynh, xét duyệt thông tin sức khỏe)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Họ tên, tuổi, lớp | BR-031 | Nguồn: Report 3 §3.3.2.4 Student Health Hub |
| 2 | Height Card | Metric tile | Chiều cao (cm) | — | |
| 3 | Weight Card | Metric tile | Cân nặng (kg) | — | |
| 4 | Blood Type Card | Metric tile | Nhóm máu | — | |
| 5 | BMI Status Card | Metric tile | Chỉ số BMI + phân loại (Normal/...) | — | |
| 6 | Allergy Profile Summary Card | Card (viền đỏ) | Danh sách allergen + mức độ nghiêm trọng | BR-031 | |
| 7 | Verification Status Badge | Badge | "Pending"/"Verified" | BR-031 | |
| 8 | Manage Health Details Button | Button (primary, full-width) | → Submit/Update Health Information | — | |
| 9 | Manage Allergy List Button | Button (primary, full-width) | → Submit/Update Allergy Information | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa khai báo thông tin sức khỏe" + nút "Khai báo ngay" → Submit Health Information.
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (màn hình tổng quan, chỉ đọc + điều hướng).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Health Hub" tile) hoặc bottom-nav "Health". Ra: Submit/Update Health Information, Submit/Update Allergy Information.
**Bố cục**: AppBar (back) → Student Summary Card → lưới 4 Metric Card (2x2: Height/Weight/Blood Type/BMI) → Allergy Profile Summary Card → 2 nút hành động full-width → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Health Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Student Summary Card + lưới 4 Metric Card (Height/Weight/Blood Type/BMI) + Allergy Profile Summary Card (viền đỏ) + 2 nút hành động (Manage Health Details/Manage Allergy List) + BottomNavigationBar.
Thành phần bắt buộc: Student Summary Card, Height/Weight/Blood Type/BMI Status Card, Allergy Profile Summary Card, Verification Status Badge, Manage Health Details Button, Manage Allergy List Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011 (Student Health Hub, field-by-field gốc), dòng 1999–2019 (UC-050 Normal Flow).
- `business_rules.md` BR-031.

# Screen Spec — Submit Health Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031, BR-172
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Đây là màn "Update Health" (Report 3 §3.3.2.4, dòng 6013–6030) ở **chế độ tạo mới (Submit)** — khi phụ huynh khai báo lần đầu (Student Health Information ở trạng thái Empty). Cùng UI với `09_UpdateHealthInformation.md`, khác action lưu (Create vs Update).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | Input số (cm) | Bắt buộc | — | |
| 2 | Weight Input Field | Input số (kg) | Bắt buộc | — | |
| 3 | Blood Type Dropdown | Dropdown | Tuỳ chọn | — | |
| 4 | File Upload Field | File upload (PDF/JPG) | Tuỳ chọn — giấy khám sức khỏe/biểu đồ tăng trưởng | BR-013 | MSG-008/MSG-009 nếu sai định dạng/quá dung lượng |
| 5 | Save Health Info Button | Button (primary, full-width) | Validate Height/Weight bắt buộc | BR-031 | Lưu "Pending Verification", thông báo Medical Staff; MSG-036 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng (form tạo mới).
- Error: `SnackBar` `danger` + MSG-004/005/008/009.
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Health Details", khi chưa có dữ liệu). Ra: về Student Health Information.
**Bố cục**: AppBar (back) → form 1 cột: Height → Weight → Blood Type → File Upload → Save Health Info Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Health Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột (Height, Weight, Blood Type Dropdown, File Upload) + nút Save Health Info cố định đáy.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Dropdown, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/008/009), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030 (Update Health, field-by-field gốc), dòng 2087–2107 (UC-084).
- `business_rules.md` BR-013, BR-031, BR-172; `error_message.md` MSG-008, MSG-009, MSG-036.

# Screen Spec — Update Health Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Cùng UI với `08_SubmitHealthInformation.md` (Update Health, Report 3 dòng 6013–6030) nhưng ở chế độ chỉnh sửa — field điền sẵn dữ liệu hiện có. Sau khi lưu, hồ sơ chuyển lại "Pending Verification" (UC-093 bước 5).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | Input số (pre-filled) | Bắt buộc | — | |
| 2 | Weight Input Field | Input số (pre-filled) | Bắt buộc | — | |
| 3 | Blood Type Dropdown | Dropdown (pre-filled) | Tuỳ chọn | — | |
| 4 | File Upload Field | File upload (hiện file cũ nếu có) | Tuỳ chọn | BR-013 | MSG-008/009 |
| 5 | Save Health Info Button | Button (primary, full-width) | Validate, lưu Pending Verification | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải dữ liệu cũ; Spinner khi submit.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005/008/009.
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Health Details", khi đã có dữ liệu). Ra: về Student Health Information.
**Bố cục**: Giống `08_SubmitHealthInformation.md` nhưng field đã điền sẵn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Health Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột đã điền sẵn (Height, Weight, Blood Type, File Upload) + nút Save Health Info cố định đáy.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Dropdown, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/008/009), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030 (wireframe gốc dùng chung), dòng 2109–2129 (UC-093).
- `business_rules.md` BR-031; `error_message.md` MSG-036.

# Screen Spec — Special Meal Arrangements

**Actor**: Parent | **UC liên quan**: UC-141 - View Special Meal Arrangements | **Status**: Draft
**BR liên quan**: BR-031, BR-065 (kế hoạch bữa ăn đặc biệt bắt buộc), BR-123 (cảnh báo dị ứng — xác nhận kép Kitchen/Teacher)
**MSG liên quan**: MSG-004, MSG-005, MSG-051

> **Ghi chú hợp nhất**: theo `05_Parent_Screen_Flow.md`, màn "Allergy Conflict Alert" (Report 3 dòng 6088–6103, trigger từ Dashboard banner/Weekly Menu card đỏ) là **cùng 1 đích điều hướng** với Special Meal Arrangements (UC-141) — field dưới đây hợp nhất cả 2 nguồn: cấu trúc tổng quan theo UC-141 Normal Flow + nội dung cảnh báo cụ thể theo wireframe "Allergy Conflict Alert" gốc.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Special Meal Arrangements" (hoặc "Allergen Conflict!" chữ đỏ nếu vào từ cảnh báo) | — | Nguồn: Report 3 dòng 6097 |
| 2 | Allergen Warning Card | Alert (danger) | Mô tả xung đột cụ thể giữa hồ sơ dị ứng và món ăn | BR-123 | MSG-051 |
| 3 | Special Meal Detail List | Card list | Loại allergen, mức độ, món thay thế được phân công, ghi chú accommodation | BR-065 | UC-141 bước 3 |
| 4 | Dual-Confirmation Status | Badge x2 | "Kitchen Released" / "Teacher Hand-off" — mỗi trạng thái riêng | BR-123 | UC-141 bước 5 |
| 5 | Parental Advisory Note | Text (italic) | "Phụ huynh nên nhắc con kiểm tra nhãn đỏ trên khay trước khi ăn" | — | Nguồn: Report 3 dòng 6100 |
| 6 | Submit Allergy Information Link | Link (chỉ hiện nếu chưa có hồ sơ dị ứng) | → Submit Allergy Information | — | UC-141 AF1 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Học sinh chưa có kế hoạch bữa ăn đặc biệt nào" + link khai báo dị ứng (UC-141 AF1).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard (Allergy Threat Alert Box) hoặc Weekly Menu (Allergy Alert Meal Card). Ra: Submit Allergy Information (nếu Empty).
**Bố cục**: AppBar (back, tiêu đề đỏ nếu là cảnh báo) → Allergen Warning Card → Special Meal Detail List → Dual-Confirmation Status → Parental Advisory Note.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Arrangements, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Allergen Warning Card (đỏ) + danh sách Card chi tiết bữa ăn đặc biệt + 2 Badge xác nhận kép (Kitchen/Teacher) + ghi chú khuyến cáo phụ huynh.
Thành phần bắt buộc: Allergen Warning Card, Special Meal Detail List, Dual-Confirmation Status, Parental Advisory Note.
Áp dụng Design System: Allergen Warning = danger bắt buộc (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4001–4017 (UC-141 Normal/Alternative Flow), dòng 6088–6103 (Allergy Conflict Alert, field-by-field gốc — hợp nhất theo `05_Parent_Screen_Flow.md`).
- `business_rules.md` BR-031
# Screen Spec — Special Meal Records

**Actor**: Parent | **UC liên quan**: UC-142 - View Special Meal Records | **Status**: Draft
**BR liên quan**: BR-065 (kế hoạch bữa ăn đặc biệt), BR-123 (xác nhận kép), BR-121 (theo dõi tiêu thụ)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: đích điều hướng của nút "Special Meals History" trên Meal History Log (Report 3 dòng 6175) — không có wireframe field-by-field riêng, suy ra từ UC-142 Normal Flow.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Special Meal Records" | — | |
| 2 | Special Meal Record Card | Card list (1 phiên/card) | Ngày, món gốc, món thay thế, allergen tránh, badge xác nhận kép | BR-065, BR-123 | UC-142 bước 3 |
| 3 | Record Detail (khi tap card) | Expand/Sheet | Ghi chú chuẩn bị, timestamp Kitchen release, xác nhận Teacher hand-off, trạng thái tiêu thụ (Fully/Partially/Not Consumed) | BR-121, BR-123 | UC-142 bước 4-5 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có lịch sử bữa ăn đặc biệt" (UC-142 AF1).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Meal History ("Special Meals History" button).
**Bố cục**: AppBar (back) → danh sách `Card` phiên bữa ăn đặc biệt cuộn dọc → tap mở chi tiết dạng bottom sheet.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Records, nền tảng Mobile, actor Parent.
Bố cục: AppBar + danh sách Card phiên bữa ăn đặc biệt (ngày, món gốc/thay thế, badge xác nhận kép) + bottom sheet chi tiết khi tap.
Thành phần bắt buộc: Special Meal Record Card, Record Detail (ghi chú, timestamp, trạng thái tiêu thụ).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4019–4039 (UC-142 Normal/Alternative Flow).
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` ("Special Meals History (≈ Special Meal Records)").
- `business_rules.md` BR-065, BR-121, BR-123.

# Screen Spec — Meal History

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History | **Status**: Draft
**BR liên quan**: BR-031, BR-069, BR-070, BR-133 (tóm tắt bữa ăn hàng ngày)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Meal History Log" | — | Nguồn: Report 3 §3.3.2.4 |
| 2 | Instruction Text | Text | "Select calendar day to see child's historical consumption" | — | |
| 3 | Calendar Strip Component | Interactive calendar (2 tuần) | Ngày disabled/selectable/active theo lịch sử có dữ liệu | BR-133 | |
| 4 | Meal History Summary Card | Card | Ngày, tỷ lệ tiêu thụ (%), loại khẩu phần, trạng thái hài lòng | BR-121, BR-133 | Tap → Meal History Detail |
| 5 | Weekly Charts Button | Button | → Nutrition History | — | |
| 6 | Special Meals History Button | Button | → Special Meal Records | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Không có lịch sử bữa ăn trong khoảng thời gian đã chọn" (UC-133 AF2).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Meal History" tile). Ra: Meal History Detail (chọn ngày), Nutrition History, Special Meal Records.
**Bố cục**: AppBar (back) → Instruction Text → Calendar Strip Component → Meal History Summary Card → 2 nút điều hướng (Weekly Charts/Special Meals History).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Instruction Text + Calendar Strip 2 tuần + Meal History Summary Card + 2 nút điều hướng (Weekly Charts/Special Meals History).
Thành phần bắt buộc: Instruction Text, Calendar Strip Component, Meal History Summary Card, Weekly Charts Button, Special Meals History Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6161–6178 (Meal History Log, field-by-field gốc), dòng 3975–3995 (UC-133 Normal/Alternative Flow).
- `business_rules.md` BR-031, BR-069, BR-070, BR-121, BR-133.

# Screen Spec — Meal History Detail

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-069 (khai báo nguyên liệu/dị ứng), BR-121 (theo dõi lịch sử bữa ăn)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe riêng trong Report 3 — suy ra từ UC-133 bước 4 "Parent selects a specific date entry to view detailed information" và bước 5 "displays the detailed meal record with dish names, ingredients, calorie information, and serving timestamp".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Date Header | Text | Ngày được chọn | — | |
| 2 | Served Dish List | List/Card | Tên món, nguyên liệu, thông tin calo | BR-069 | UC-133 bước 5 |
| 3 | Consumption Status Badge | Badge | Fully/Partially/Not Consumed | BR-121 | |
| 4 | Serving Timestamp | Text | Giờ phục vụ | BR-121 | |
| 5 | Special Meal Applied Note | Text (điều kiện) | Ghi chú nếu có áp dụng kế hoạch bữa ăn đặc biệt | BR-065 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng (luôn có dữ liệu khi chọn từ Calendar Strip).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Meal History (chọn ngày trên Calendar Strip).
**Bố cục**: AppBar (back, hiển thị ngày) → Served Dish List → Consumption Status Badge + Serving Timestamp → Special Meal Applied Note (nếu có).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History Detail, nền tảng Mobile, actor Parent.
Bố cục: AppBar (ngày) + danh sách món ăn (tên, nguyên liệu, calo) + badge trạng thái tiêu thụ + timestamp phục vụ + ghi chú bữa ăn đặc biệt.
Thành phần bắt buộc: Date Header, Served Dish List, Consumption Status Badge, Serving Timestamp, Special Meal Applied Note.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3975–3995 (UC-133 Normal Flow bước 4-5).
- `business_rules.md` BR-065, BR-069, BR-121.

# Screen Spec — School Meals Dashboard

**Actor**: Parent | **UC liên quan**: UC-055 - Monitor School Meals | **Status**: Draft
**BR liên quan**: BR-069 (khai báo nguyên liệu/dị ứng), BR-070 (chỉ menu Published), BR-075 (FEFO/truy xuất)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe field-by-field riêng trong Report 3 cho màn tổng quan này — suy ra từ UC-055 Normal Flow. Đây là màn tổng quan cấp cao hơn Weekly Menu (menu tuần hiện tại + tuần tới, đối chiếu hồ sơ dị ứng của con), khác với Weekly Menu chi tiết theo ngày.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "School Meals" | — | |
| 2 | Current & Upcoming Week Menu Summary | Card list | Tóm tắt menu tuần hiện tại + tuần tới (đã Published) | BR-070 | UC-055 bước 2 |
| 3 | Nutrition & Allergen Highlight | Badge/Alert | Đánh dấu món có allergen liên quan hồ sơ dị ứng của con | BR-069 | UC-055 bước 4 |
| 4 | View Weekly Menu Link | Button | → Weekly Menu (xem chi tiết theo ngày) | — | |
| 5 | View Nutrition Breakdown Link | Button | → Nutrition Detail/History | — | UC-055 bước 5 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có thực đơn được công bố" [Giả định — không có MSG riêng, tương tự Weekly Menu].
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("School Meals" tile). Ra: Weekly Menu, Nutrition Detail/History.
**Bố cục**: AppBar (back) → Current & Upcoming Week Menu Summary (Card list) → Nutrition & Allergen Highlight → 2 nút điều hướng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Meals Dashboard, nền tảng Mobile, actor Parent.
Bố cục: AppBar + danh sách Card tóm tắt menu tuần hiện tại/tới + khối cảnh báo allergen + 2 nút điều hướng (Weekly Menu/Nutrition Breakdown).
Thành phần bắt buộc: Current & Upcoming Week Menu Summary, Nutrition & Allergen Highlight, View Weekly Menu Link, View Nutrition Breakdown Link.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3865–3885 (UC-055 Monitor School Meals — Normal Flow).
- `business_rules.md` BR-069, BR-070, BR-075.

# Screen Spec — Feedback Center

**Actor**: Parent | **UC liên quan**: — (màn hub điều hướng, không gắn UC riêng — dẫn tới UC-085 và UC-073) | **Status**: Draft
**BR liên quan**: BR-139 (cửa sổ 24h nộp phản hồi), BR-146 (tự động kích hoạt điều tra sự cố)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe field-by-field riêng trong Report 3 cho màn hub này — suy ra từ `05_Parent_Screen_Flow.md` (FeedbackCenter → MealFeedback / FoodQualityFeedback / ReportSafety). Đóng vai trò điểm vào trung tâm cho 3 luồng phản hồi khác nhau.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Feedback Center" | — | |
| 2 | Recent Meal Feedback Tile | Button/Card | → Submit Meal Feedback (đánh giá món ăn cụ thể) | BR-139 | |
| 3 | Food Quality Feedback Tile | Button/Card | → Submit Food Quality Feedback (phản hồi chất lượng chung) | BR-139 | |
| 4 | Report Safety Concern Tile | Button/Card (đỏ, nổi bật) | → Report Food Safety Concern | BR-146 | |
| 5 | My Feedback History List | Card list | Danh sách phản hồi đã gửi trước đó + trạng thái phản hồi từ Service Caterers (nếu có) | BR-143 | [Giả định — cần xác nhận có hiển thị lịch sử tại đây hay không] |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có phản hồi nào được gửi".
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (màn hub).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Meal Feedback" tile). Ra: Submit Meal Feedback, Submit Food Quality Feedback, Report Food Safety Concern.
**Bố cục**: AppBar (back) → 3 Card/Tile lớn (Meal Feedback/Food Quality/Report Safety) → My Feedback History List cuộn dọc.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Feedback Center, nền tảng Mobile, actor Parent.
Bố cục: AppBar + 3 Tile lớn (Meal Feedback, Food Quality Feedback, Report Safety Concern — tile cuối màu đỏ nổi bật) + danh sách lịch sử phản hồi.
Thành phần bắt buộc: Recent Meal Feedback Tile, Food Quality Feedback Tile, Report Safety Concern Tile, My Feedback History List.
Áp dụng Design System: Report Safety Concern Tile màu danger.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` (FeedbackCenter → MealFeedback/FoodQualityFeedback/ReportSafety).
- `traceability_matrix.md` §1.10 UC-085, §1.11 UC-073.
- `business_rules.md` BR-139, BR-143, BR-146.

# Screen Spec — Submit Meal Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139 (cửa sổ 24h), BR-140 (mô tả vấn đề bắt buộc khi <3 sao), BR-142 (phát hiện từ khóa bệnh lý), BR-143 (Service Caterers phản hồi lại), BR-145 (ẩn danh dữ liệu tổng hợp)
**MSG liên quan**: MSG-004, MSG-005, MSG-022 (ngoài cửa sổ 24h), MSG-046 (thành công), MSG-058 (phát hiện từ khóa bệnh lý)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Selection Dropdown | Dropdown | Chọn món đã phục vụ trong 24h gần nhất | BR-139 | Nguồn: Report 3 §3.3.2.4 Meal Feedback |
| 2 | Rating Score Component | Star rating (1-5) | Bắt buộc | — | |
| 3 | Quality Concern Description | `Input` (text tự do, chỉ hiện khi <3 sao) | Bắt buộc nhập nếu rating <3 | BR-140 | Đổi từ Checkbox/Tag group sang Input theo yêu cầu team (2026-07-01) — phụ huynh tự mô tả vấn đề (VD "quá mặn", "nguội") thay vì chọn nhãn có sẵn |
| 4 | Review Comments Section | Textarea | Tuỳ chọn | BR-142 | Hệ thống quét từ khóa bệnh lý tự động |
| 5 | Photo Attachment Component | File upload (camera/gallery) | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary, full-width) | Validate, chặn nếu ngoài 24h | BR-139 | MSG-022 nếu hết hạn; MSG-046 khi thành công; MSG-058 nếu phát hiện từ khóa bệnh lý |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005/022.
- Success: `SnackBar` `success` (MSG-046); nếu phát hiện từ khóa bệnh lý → thêm cảnh báo `warning` (MSG-058) và thông báo Medical Staff được kích hoạt.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Feedback Center, Nutrition Detail ("Give Feedback on this Dish"), hoặc Weekly Menu. Ra: về Feedback Center.
**Bố cục**: AppBar (back) → Dish Selection Dropdown → Rating Score Component → Quality Concern Description (input, điều kiện) → Review Comments → Photo Attachment → Send Feedback Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Meal Feedback, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Dish Selection Dropdown + Rating Score (5 sao) + Quality Concern Description input (điều kiện <3 sao) + Review Comments Textarea + Photo Attachment + nút Send Feedback cố định đáy.
Thành phần bắt buộc: Dish Selection Dropdown, Rating Score Component, Quality Concern Description Input, Review Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ li
# Screen Spec — Submit Food Quality Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139, BR-140, BR-142, BR-143
**MSG liên quan**: MSG-004, MSG-005, MSG-022, MSG-046, MSG-058

> **Ghi chú nguồn**: không có wireframe riêng — theo `14_Screen_Authorization_Matrix.md`, đây là 1 màn tách biệt với "Submit Meal Feedback" (cùng UC-085 nhưng phạm vi khác: phản hồi chất lượng chung của bữa ăn/dịch vụ catering, không gắn với 1 món cụ thể như `16_SubmitMealFeedback.md`). Field suy ra từ cùng UC-085 Normal Flow, bỏ field "Dish Selection Dropdown" vì không áp dụng.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Meal Session Selector | Dropdown | Chọn phiên ăn (Sáng/Trưa) trong 24h gần nhất, không chọn món cụ thể | BR-139 | |
| 2 | Rating Score Component | Star rating (1-5) | Bắt buộc | — | |
| 3 | Quality Concern Tags | Checkbox/Tag group (điều kiện <3 sao) | Bắt buộc nếu <3 sao | BR-140 | |
| 4 | General Comments Section | Textarea | Tuỳ chọn — phản hồi chất lượng dịch vụ/vệ sinh chung | BR-142 | |
| 5 | Photo Attachment Component | File upload | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary, full-width) | Validate, chặn ngoài 24h | BR-139 | MSG-022/046/058 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005/022.
- Success: `SnackBar` `success` (MSG-046); `warning` nếu MSG-058.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Feedback Center ("food quality" tile).
**Bố cục**: AppBar (back) → Meal Session Selector → Rating Score → Quality Concern Tags (điều kiện) → General Comments → Photo Attachment → Send Feedback Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Food Quality Feedback, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Meal Session Selector + Rating Score + Quality Concern Tags (điều kiện) + General Comments Textarea + Photo Attachment + nút Send Feedback cố định đáy.
Thành phần bắt buộc: Meal Session Selector, Rating Score Component, Quality Concern Tags, General Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4109–4129 (UC-085 — dùng chung, không có wireframe riêng cho biến thể "Food Quality").
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` (FeedbackCenter -->|"food quality"| FoodQualityFeedback).
- `business_rules.md` BR-139, BR-140, BR-142, BR-143; `error_message.md` MSG-022, MSG-046, MSG-058.

# Screen Spec — Report Food Safety Concern

**Actor**: Parent | **UC liên quan**: UC-073 - Report Food Safety Concern | **Status**: Draft
**BR liên quan**: BR-146 (tự động kích hoạt điều tra ≥3 học sinh/24h), BR-147, BR-148, BR-149 (tự động quarantine), BR-150, BR-151 (bản đồ truy xuất 4h), BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023 (thiếu thông tin bắt buộc), MSG-048 (ghi nhận thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text (đỏ) | "Report Safety Issue" | — | Nguồn: Report 3 §3.3.2.4 |
| 2 | Concern Category Dropdown | Dropdown | Bắt buộc (VD "Spoiled/Sour Food") | BR-146 | |
| 3 | Symptoms Checkbox Group | Checkbox group | Stomach Pain/Vomiting-Nausea/Diarrhea/Fever | BR-146 | |
| 4 | Meal Date Input | DatePicker | Bắt buộc — theo UC-073 bước 3 | BR-146 | Nguồn: Normal Flow UC-073 (không có trong wireframe gốc, bổ sung theo UC) |
| 5 | Suspected Dish Input | Select (từ thực đơn đã phục vụ) | Bắt buộc — theo UC-073 bước 3 | BR-146 | Nguồn: Normal Flow UC-073 |
| 6 | Incident Details Section | Textarea | Bắt buộc | — | MSG-023 nếu thiếu |
| 7 | Photo Attachment Component | File upload (đỏ, bắt buộc) | Bắt buộc — "Attach Food Error / Medical Report Photo *" | — | MSG-023 nếu thiếu |
| 8 | Send Urgent Report Button | Button (danger, full-width) | Validate, gửi ngay không qua duyệt (BR-138) | BR-146, BR-149, BR-151 | MSG-023 nếu thiếu; MSG-048 khi thành công |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng (form tạo mới).
- Error: `SnackBar` `danger` + MSG-023 (thiếu trường bắt buộc, đặc biệt ảnh bằng chứng).
- Success: `SnackBar` `success` (MSG-048) — nếu đạt ngưỡng ≥3 học sinh/24h, tự động tạo sự cố chính thức và quarantine lô nghi vấn (BR-146, BR-149), thông báo cho School Administration.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Report Urgent Safety Incident") hoặc Feedback Center ("safety concern"). Ra: về Parent Dashboard sau khi gửi (không có bước duyệt trung gian — BR-138).
**Bố cục**: AppBar (tiêu đề đỏ) → form 1 cột: Concern Category → Symptoms Checkbox → Meal Date → Suspected Dish → Incident Details → Photo Attachment (viền đỏ) → Send Urgent Report Button (nền đỏ, full-width, cố định đáy).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Report Food Safety Concern, nền tảng Mobile, actor Parent.
Bố cục: AppBar (tiêu đề đỏ) + form 1 cột (Concern Category, Symptoms Checkbox, Meal Date, Suspected Dish, Incident Details, Photo Attachment viền đỏ) + nút Send Urgent Report (nền đỏ) cố định đáy.
Thành phần bắt buộc: Concern Category Dropdown, Symptoms Checkbox Group, Meal Date Input, Suspected Dish Input, Incident Details Section, Photo Attachment Component, Send Urgent Report Button.
Áp dụng Design System: toàn bộ màn hình nhấn mạnh màu danger (khẩn cấp, bypass duyệt BR-138).
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6142–6159 (Report Safety Issue, field-by-field gốc), dòng 4485–4505 (UC-073 Normal/Alternative/Exception Flow).
- `business_rules.md` BR-138, BR-146–151, BR-154, BR-155; `error_message.md` MSG-023, MSG-048.
