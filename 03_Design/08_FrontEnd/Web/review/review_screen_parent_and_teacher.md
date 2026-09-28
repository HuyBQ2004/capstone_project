# Review Frontend Web — Actor Parent & Teacher

**Phạm vi đối chiếu**: `03_Design/08_FrontEnd/Web/src/app/(app)/{parent,teacher}/` (code thật) vs. `05_ScreenSpecifications/Web/{Parent,Teacher}/*.md` (18 màn Parent + 13 màn Teacher) vs. `04_ScreenFlow/10_Parent_Screen_Flow_Web.md` + `12_Teacher_Screen_Flow_Web.md` vs. `01_Diagrams/01_Class_Diagram/class_diagram_description.md`.
**Phương pháp**: đọc toàn bộ `page.tsx` đã implement (Parent: 7 file, Teacher: 6 file) + `mock-data.ts` + `data-table.tsx`, so khớp field-by-field với bảng "Business Logic" của từng Screen Spec.
**Ngày review**: 03/07/2026.

---

## 1. Kết luận nhanh

Cả 2 actor đều **chưa đủ tính năng** để chạy hết luồng end-to-end theo Screen Spec. Parent implement **6/18 màn** (33%), Teacher implement **5/13 màn** (38%) — tính theo route thực tế tồn tại, chưa kể các màn đã tồn tại nhưng thiếu field so với spec. Có **2 lỗ hổng an toàn nghiêm trọng** (safety-critical) cần vá trước tiên:

1. **Parent không có đường nào để "Report Food Safety Concern" / "Report Urgent Safety Incident"** — toàn bộ UC-073 (BR-138, BR-146…BR-155) không tồn tại trong code. Đây là luồng khẩn cấp báo sự cố an toàn thực phẩm, bypass duyệt.
2. **Teacher không thể xem chi tiết học sinh (Student Detail) hay xác nhận từng suất ăn có cảnh báo dị ứng riêng** — bảng Student List và Distribution List không có `rowHref` nào dẫn tới trang chi tiết, nên `Allergy Warning Banner` theo từng học sinh (BR-123) không bao giờ hiển thị dù dashboard/table đều "nói" là có cảnh báo tự động.

Ngoài ra, một thay đổi field vừa đồng bộ Screen Spec + Wireframe (Quality Concern: Checkbox → Input, ngày 2026-07-01) **chưa được áp dụng vào code** — `parent/feedback/page.tsx` vẫn dùng `QUALITY_TAGS` checkbox cũ.

---

## 2. Bảng độ phủ màn hình

### 2.1 Parent (18 màn spec)

| # | Màn hình Spec | Route code hiện có | Trạng thái |
| --- | --- | --- | --- |
| 01 | Parent Dashboard | `parent/dashboard` | ⚠️ Có, thiếu 4 field quan trọng |
| 02 | Weekly Menu | `parent/weekly-menu` | ⚠️ Có, thiếu 3 field + sai layout |
| 03 | Nutrition Detail | — | ❌ Không có |
| 04 | Nutrition History | — | ❌ Không có |
| 05 | Submit Allergy Information | `parent/allergies` (form) | ⚠️ Có, sai loại field (thiếu Category Select, thiếu Upload Medical Proof) |
| 06 | Update Allergy Information | — | ❌ Không có (không có action "sửa" cho dị ứng đã khai) |
| 07 | Student Health Information | — | ❌ Không có (không có block Height/Weight/BMI/Blood Type) |
| 08 | Submit Health Information | — | ❌ Không có |
| 09 | Update Health Information | — | ❌ Không có |
| 10 | Special Meal Arrangements | — | ❌ Không có |
| 11 | Special Meal Records | — | ❌ Không có |
| 12 | Meal History | `parent/meal-history` | ⚠️ Có, thiếu Date Filter + 2 nút điều hướng |
| 13 | Meal History Detail | — | ❌ Không có (không click-through được) |
| 14 | School Meals Dashboard | — | ❌ Không có |
| 15 | Feedback Center | — (gộp một phần vào `parent/feedback`) | ❌ Không có màn hub 3-tile riêng |
| 16 | Submit Meal Feedback | `parent/feedback` (nửa trên) | ⚠️ Có, sai component Quality Concern (Checkbox thay vì Input đã chốt) + hardcode 1 món |
| 17 | Submit Food Quality Feedback | — | ❌ Không có biến thể theo phiên ăn |
| 18 | Report Food Safety Concern | — | ❌ **Không có — safety-critical** |
| — | *(ngoài spec)* Announcements List/Detail | `parent/announcements`, `parent/announcements/[id]` | ✅ Có, khớp `10_Parent_Screen_Flow_Web.md` node "Notifications", nhưng chưa có file Screen Spec riêng cho Parent |

### 2.2 Teacher (13 màn spec)

| # | Màn hình Spec | Route code hiện có | Trạng thái |
| --- | --- | --- | --- |
| 01 | Teacher Home | `teacher/dashboard` | ⚠️ Có, thiếu 2 nút hành động + Notice Board đúng dạng |
| 02 | Student List | `teacher/students` | ⚠️ Có bảng, thiếu Profile Filter Tabs + không click-through |
| 03 | Student Detail | — | ❌ **Không có — không có `rowHref`, bảng học sinh chết (dead-end)** |
| 04 | Allergy Information | — | ❌ Không có màn riêng (chỉ có badge cột trong bảng) |
| 05 | Health Information | — | ❌ Không có |
| 06 | Special Meal Requirements | — | ❌ Không có màn riêng (chỉ có badge cột) |
| 07 | Meal Records | — | ❌ Không có (không ghi nhận Fully/Partially/Not Consumed) |
| 08 | Distribution List | `teacher/distribution` | ⚠️ Có, thiếu Status Filter Tabs + nút "Confirm All" |
| 09 | Distribution Detail | — | ❌ **Không có — bảng phân phát cũng dead-end, không có `rowHref`** |
| 10 | Distribution Confirmation | — (merge vào 1-click) | ⚠️ Xác nhận trực tiếp không qua Dialog xác nhận + timestamp hiển thị |
| 11 | Upload Evidence | — | ❌ Không có (Incident Form không có Photo input) |
| 12 | Incident Form | `teacher/incidents` (nửa trên) | ⚠️ Có, thiếu 4 field bắt buộc (xem §4) |
| 13 | Incident History | `teacher/incidents` (nửa dưới) | ⚠️ Có, thiếu Assessment Filter Tabs |
| — | *(ngoài spec)* Announcements List/Detail | `teacher/announcements`, `.../[id]` | ✅ Có, khớp `12_Teacher_Screen_Flow_Web.md` node "Announcement Detail", chưa có Screen Spec riêng |

---

## 3. Chi tiết gap theo màn hình — Parent

### 01. Parent Dashboard (`parent/dashboard/page.tsx`)
Thiếu so với spec (6 field bắt buộc):
- **Child Selector** (dropdown đổi hồ sơ con) — không tồn tại, kể cả ở Topbar.
- **Notification Bell Icon** — không có.
- **4 Navigation Tiles** (Health Hub / School Meals / Meal Feedback / Meal History) — bị thay bằng 4 `StatCard` chỉ số (Bữa trưa hôm nay, Năng lượng, Dị ứng theo dõi, Đánh giá 30 ngày) — đây là **card thống kê, không phải điều hướng**, nên phụ huynh không có lối vào School Meals Dashboard / Feedback Center / Meal History Detail từ Dashboard.
- **Report Urgent Safety Incident Button** (destructive, full-width, BR-146/BR-138) — **không tồn tại**.
Có nhưng cần xem lại: Allergy Threat Alert Box hiện là `Alert variant="danger"` tĩnh (luôn hiển thị), spec yêu cầu là **có điều kiện** ("Menu Allergy Threat Detected!" chỉ khi thực đơn hôm nay thực sự có allergen trùng) — cần verify logic điều kiện khi nối backend thật, hiện tại là hardcode.

### 02. Weekly Menu (`parent/weekly-menu/page.tsx`)
- Dùng card-grid tất cả các ngày cùng lúc thay vì **Weekly Days Tab Bar** — chấp nhận được nếu chủ đích đổi layout, nhưng cần ghi nhận là deviation có chủ đích, không phải thiếu sót ngẫu nhiên.
- **Allergy Alert Meal Card** (viền destructive khi món có allergen trùng hồ sơ con — BR-123, MSG-051): **không có**. Trong khi Dashboard tuyên bố "nhà bếp được cảnh báo tự động khi phục vụ" (BR-123), Weekly Menu — nơi phụ huynh xem trước thực đơn — lại không cảnh báo gì dù `WEEKLY_MENU_DAYS` Thứ 6 có "Tôm rim thịt" trùng đúng allergen "Tôm" của học sinh mẫu. Đây là **gap an toàn dữ liệu thực tế**, không chỉ lý thuyết.
- **View Nutrition Details Button**: không có, món ăn không phải link/button dẫn tới Nutrition Detail.
- **Download PDF Button**: không có.
- Ghi chú: code trích dẫn `MSG-090`, `MSG-040` trong comment — 2 mã này **không xuất hiện** trong Screen Spec gốc (`02_WeeklyMenu.md` dùng MSG-004/005/051). Cần kiểm tra `error_message.md` xem MSG-090/MSG-040 có tồn tại chính thức không trước khi giữ lại citation này.

### 03–04. Nutrition Detail / Nutrition History
Không có route nào. Hệ quả: nút "Give Feedback" (từ Nutrition Detail) và biểu đồ dinh dưỡng (Nutrition Trend Chart, Nutrition Target Comparison) hoàn toàn vắng mặt — phụ huynh không xem được thành phần nguyên liệu/allergen chi tiết của từng món trước khi cho con ăn.

### 05. Submit Allergy Information (`parent/allergies/page.tsx`)
- Có form nhưng field sai kiểu: spec yêu cầu **Allergen Category Select** (nhóm dị ứng chuẩn hoá) + **Specific Allergen Name Input** (chỉ hiện khi chọn "Other") — code chỉ có **1 Input tự do** duy nhất, mất bước phân loại chuẩn hoá.
- **Upload Medical Proof Component** (`Input type="file"`, bắt buộc nếu Severe — BR-031): code thay bằng một `Textarea` ghi chú, **không có upload file thật**. Đây là gap tuân thủ BR-031 (khai báo Severe bắt buộc có giấy tờ y tế xác minh).
- Không có action "sửa" cho dị ứng đã khai báo → **Update Allergy Information (06) không thể thực hiện được** dù dữ liệu mẫu đã có 2 dòng "VERIFIED".

### 07–09. Student Health Information / Submit / Update Health Information
Hoàn toàn không có. Không có nơi nào hiển thị hoặc nhập Height/Weight/Blood Type/BMI — mảng dữ liệu sức khỏe cơ bản của học sinh (khác dị ứng) không tồn tại trong frontend Parent dù `AllergyProfile`/`StudentHealthRecord` là 2 entity tách biệt trong class diagram (dòng 255 `Student "1" --> "0..*" StudentHealthRecord`).

### 10–11. Special Meal Arrangements / Special Meal Records
Không có. Đây là màn xác nhận "suất ăn thay thế" cho học sinh dị ứng (Dual-Confirmation Status: Kitchen Released / Teacher Hand-off) — phụ huynh hiện không có cách nào kiểm chứng con đã thực sự được phục vụ đúng suất ăn thay thế hay chưa, dù Dashboard/Meal History đều nhắc tới cơ chế này (`meal-history` có dòng "Tôm rim thịt → suất riêng: Thịt rim").

### 12–13. Meal History / Meal History Detail
- Bảng có nhưng thiếu **Calendar/Date Filter**, **Weekly Charts Button** (→ Nutrition History), **Special Meals History Button** (→ Special Meal Records).
- Click hàng bảng không dẫn đi đâu → **Meal History Detail (13)** không thể truy cập, dù mỗi dòng đáng lẽ mở chi tiết món ăn/giờ phục vụ.

### 14. School Meals Dashboard
Không có. Đây là màn tổng quan 2 tuần (hiện tại + sắp tới), không có nó thì Dashboard → School Meals (1 trong 4 tile bị mất ở mục 01) không có đích đến hợp lệ.

### 15–18. Feedback Center / Submit Meal Feedback / Submit Food Quality Feedback / Report Food Safety Concern
- **Feedback Center (15)**: không tồn tại như 1 hub riêng với 3 tile lớn — bị rút gọn thành 1 trang `parent/feedback` duy nhất.
- **Submit Meal Feedback (16)**: có, nhưng:
  - Quality Concern field đang là **Checkbox tag group** (`QUALITY_TAGS`) — **ngược lại quyết định đã chốt và đồng bộ ngày 2026-07-01** (đổi sang `Input` tự do, đã cập nhật `16_SubmitMealFeedback.md` + 2 wireframe Web/Mobile). Code frontend **chưa bắt kịp thay đổi này** — cần sửa lại thành `Textarea`/`Input` tự do.
  - Dish Selection bị hardcode 1 món ("Cá basa sốt cà chua") thay vì `Select` liệt kê mọi món ăn trong cửa sổ 24h (BR-139).
- **Submit Food Quality Feedback (17)**: không có biến thể riêng (Meal Session Selector, không gắn món cụ thể).
- **Report Food Safety Concern (18)**: **hoàn toàn không tồn tại**. Đây là màn quan trọng nhất còn thiếu — không Concern Category, không Symptoms Checkbox, không Meal Date/Suspected Dish, không nút "Send Urgent Report" destructive. Phối hợp với gap ở Dashboard (thiếu nút Report Urgent Safety Incident), **Parent hiện tại không có bất kỳ đường nào để báo sự cố an toàn thực phẩm khẩn cấp (UC-073, BR-138)**.

---

## 4. Chi tiết gap theo màn hình — Teacher

### 01. Teacher Home (`teacher/dashboard/page.tsx`)
- Thiếu **Distribute Meals Today Button** dạng nút hành động chính ở Page Header (hiện chỉ có link "Xem tất cả →" phụ, không nổi bật bằng nút primary theo spec).
- Thiếu **Report New Health Incident Button** trên Dashboard (giáo viên phải tự vào Sidebar "Sự cố Sức khỏe" thay vì có shortcut trực tiếp).
- **Today's Notice Board Card** (danh sách cảnh báo khẩn tổng hợp dị ứng/thực đơn đặc biệt/sự cố mới) bị thay bằng 1 `Alert` đơn (chỉ liệt kê tên học sinh dị ứng) — mất phần "sự cố mới nhất"/"thực đơn đặc biệt hôm nay".

### 02–03. Student List / Student Detail
- Bảng `teacher/students` không truyền `rowHref` cho `DataTable` → **click vào hàng không làm gì cả**. Điều này khiến **Student Detail (03) hoàn toàn không thể truy cập**, kéo theo cả chuỗi 4 màn con của nó (Health Information, Allergy Information dạng chi tiết, Special Meal Requirements dạng chi tiết, Meal Records) đều mất lối vào chính thức — dù dữ liệu (`allergies`, `specialDiet`, `healthStatus`) đã có sẵn trong `mock-data.ts`.
- **Profile Filter Tabs** (All/Allergy Warning/Special Diet/Normal): không có — `DataTable` chỉ hỗ trợ ô tìm kiếm text, không có tab lọc theo nhóm.

### 04–05. Allergy Information / Health Information
Không tồn tại như màn riêng. Thông tin dị ứng hiện chỉ là 1 cột Badge trong bảng Student List — thiếu **Verification Status Badge** riêng biệt và **Read-only Notice** ("chỉ Medical Staff/Phụ huynh chỉnh sửa được") mà spec yêu cầu hiển thị rõ để tránh giáo viên hiểu nhầm có quyền sửa.

### 06. Special Meal Requirements
Không tồn tại như màn riêng (chỉ là cột Badge `specialDiet`). Thiếu Preparation Note (ghi chú chế biến riêng — BR-082) và Approval Status Badge tách biệt.

### 07. Meal Records
Không có. Giáo viên hiện **không có cách ghi nhận Fully/Partially/Not Consumed** cho từng học sinh (đặc biệt học sinh có chế độ ăn đặc biệt — BR-121, BR-125) — đây là nghiệp vụ theo dõi tiêu thụ bữa ăn, khác với "Confirm Meal Distribution" (chỉ xác nhận đã phát, không xác nhận đã ăn hết).

### 08–10. Distribution List / Detail / Confirmation
- `teacher/distribution` không có **Status Filter Tabs** (All/Pending/Served/Special Meal) và không có nút **"Confirm All"** (xác nhận toàn lớp 1 lần) — chỉ có action xác nhận từng dòng.
- Bảng cũng không truyền `rowHref` → **Distribution Detail (09) không thể truy cập** — nghĩa là **Allergy Warning Banner theo từng học sinh** (đúng vị trí spec định nghĩa nó, BR-123) **không bao giờ hiển thị trong thực tế**, dù nút "Quét ID học sinh" ở Page Header có `toast.info` nhắc tới "cảnh báo trực quan + âm thanh nếu có dị ứng" — lời hứa này không có UI nào thực hiện.
- Xác nhận phân phát hiện là **1-click trực tiếp** (`update.mutate` ngay khi bấm nút), không qua `AlertDialog` xác nhận + hiển thị timestamp như **Distribution Confirmation (10)** yêu cầu — mất luôn bước chặn double-submit rõ ràng cho người dùng (dù có `BR-130`/`MSG-021` phía sau, UI không cho thấy).

### 11. Upload Evidence
Không tồn tại. `teacher/incidents/page.tsx` **không có bất kỳ trường đính kèm ảnh nào** (`Input type="file"`) — trái với spec Incident Form yêu cầu "Attached Photos Component".

### 12. Incident Form (`teacher/incidents`, nửa trên)
So với 12 field bắt buộc trong spec, code chỉ có 4: Student (single-select, spec yêu cầu multi-select), Triệu chứng (Input tự do — chấp nhận được), Bữa ăn/món nghi vấn (Select — đạt), Mô tả chi tiết (Textarea — đạt "Incident Description"). **Thiếu**:
- Class Input Field (tự động điền, chỉ đọc).
- **Number of Affected Students Input** (bắt buộc, BR-146/BR-154).
- **Ingestion Date Input** (`DatePicker` riêng, BR-154) — hiện gộp chung vào Select "bữa ăn nghi vấn" nên không tách được ngày ăn.
- **Severity Level Radio Group** (Mild/Medium/Severe, BR-147) — thiếu hoàn toàn, ảnh hưởng tới phân loại/độ ưu tiên điều tra ở phía Medical Staff.
- **Attached Photos Component** (xem mục 11).
- **Cancel Button**.

### 13. Incident History (`teacher/incidents`, nửa dưới)
Có bảng nhưng thiếu **Assessment Filter Tabs** (All/Pending Assessment/Under Assessment) — giáo viên không lọc được báo cáo theo trạng thái xử lý.

---

## 5. Vấn đề xuyên suốt (cross-cutting)

1. **Announcements không có Screen Spec chính thức cho Parent/Teacher.** Route đã implement đúng với node "Notifications"/"Announcement Detail" trong `10_Parent_Screen_Flow_Web.md` và `12_Teacher_Screen_Flow_Web.md`, và entity `Announcement` tồn tại hợp lệ trong class diagram (BR-144). Đây không phải lỗi tính năng, nhưng cần bổ sung 2 file Screen Spec (`Announcements_List.md`, `Announcement_Detail.md`) cho Parent + Teacher để hồ sơ thiết kế khớp code, tránh tình trạng "code có, spec không" khi audit sau này.
2. **`rowHref` bị bỏ trống ở mọi `DataTable` của Teacher** (`students`, `distributions`) — đây là nguyên nhân kỹ thuật gốc khiến 3 màn Detail (Student Detail, Distribution Detail) và toàn bộ cảnh báo dị ứng theo từng học sinh không hiển thị được, dù component `DataTable` đã hỗ trợ sẵn prop này (`rowHref?: (row: T) => string`). Đây là **fix rẻ nhất/nhanh nhất** trong toàn bộ review vì hạ tầng đã có sẵn, chỉ thiếu 2 trang đích + 1 dòng prop.
3. **Một số filter dạng Tab (Profile/Status/Assessment Filter Tabs) bị thay bằng ô tìm kiếm text** ở mọi màn Teacher dùng `DataTable` — vì `DataTable` dùng chung hiện chỉ có `searchPlaceholder`, chưa có prop cho tab lọc theo field cố định.
4. **Quality Concern (Submit Meal Feedback) đang lệch khỏi quyết định đã chốt gần nhất** (Input tự do, 2026-07-01) — nguy cơ nếu không sửa: code base tiếp tục lệch so với Screen Spec/wireframe mỗi khi có thay đổi field trong tương lai, vì đội dev có thể không biết Screen Spec đã cập nhật.
5. **Không có file upload thật ở bất kỳ đâu trong Parent/Teacher** (chỉ có `Textarea` ghi chú thay cho `Input type="file"`) — ảnh hưởng: Submit Allergy Information (bắt buộc theo BR-031 khi Severe), Incident Form/Upload Evidence (BR-013 giới hạn dung lượng, MSG-008/009).

---

## 6. Danh sách tính năng cần bổ sung để đủ luồng end-to-end

### Parent (ưu tiên P0 → P2)
- **P0 — an toàn khẩn cấp**: Report Food Safety Concern (18) + nút Report Urgent Safety Incident trên Dashboard (01).
- **P0 — an toàn dữ liệu**: Allergy Alert Meal Card trên Weekly Menu (02); sửa lại Quality Concern về Input tự do (16); thêm Upload Medical Proof file thật (05).
- **P1 — luồng sức khỏe/dị ứng đầy đủ**: Student Health Information (07), Submit/Update Health Information (08–09), Update Allergy Information (06, thêm action "sửa" trên các dòng đã khai), Special Meal Arrangements (10), Special Meal Records (11).
- **P1 — luồng dinh dưỡng**: Nutrition Detail (03), Nutrition History (04), click-through từ Weekly Menu/Meal History Detail.
- **P2 — điều hướng/hub**: Feedback Center (15) dạng hub 3-tile, Submit Food Quality Feedback (17), School Meals Dashboard (14), Meal History Detail (13), Child Selector + Notification Bell ở Topbar (01), Date Filter + 2 nút điều hướng ở Meal History (12), Download PDF + View Nutrition Details ở Weekly Menu (02).
- **P2 — hồ sơ hoá**: viết Screen Spec chính thức cho Announcements List/Detail (Parent).

### Teacher (ưu tiên P0 → P2)
- **P0 — an toàn dị ứng thực tế**: thêm `rowHref` cho bảng Student List → Student Detail (03), và bảng Distribution List → Distribution Detail (09) để **Allergy Warning Banner** thực sự hiển thị được theo từng học sinh khi phân phát.
- **P0 — quy trình sự cố đầy đủ**: bổ sung 4 field còn thiếu ở Incident Form (Number of Affected Students, Ingestion Date, Severity Level, Attached Photos) + màn Upload Evidence (11).
- **P1 — theo dõi tiêu thụ**: Meal Records (07) — ghi nhận Fully/Partially/Not Consumed, đặc biệt cho học sinh chế độ ăn đặc biệt.
- **P1 — xác nhận có kiểm soát**: Distribution Confirmation (10) dạng `AlertDialog` thật (hiện đang 1-click), Confirm All Button + Status Filter Tabs ở Distribution List (08).
- **P2 — chi tiết hồ sơ học sinh**: Allergy Information (04) và Health Information (05) dạng màn riêng, chỉ đọc, với Read-only Notice; Special Meal Requirements (06) dạng màn riêng với Preparation Note.
- **P2 — điều hướng/hub**: Distribute Meals Today Button + Report New Health Incident Button ở Teacher Home (01), Notice Board Card đúng dạng list cảnh báo, Profile Filter Tabs (02), Assessment Filter Tabs (13).
- **P2 — hồ sơ hoá**: viết Screen Spec chính thức cho Announcements List/Detail (Teacher).

---

## 7. Kế hoạch chỉnh sửa đề xuất (theo giai đoạn)

**Giai đoạn 1 — Vá an toàn (bắt buộc trước khi demo/nghiệm thu)**
1. Dựng màn `parent/safety-concern` (Report Food Safety Concern, 18) + nút "Report Urgent Safety Incident" trên `parent/dashboard`.
2. Thêm `rowHref` cho `DataTable` ở `teacher/students` (→ `teacher/students/[id]`) và `teacher/distribution` (→ `teacher/distribution/[id]`); dựng 2 trang đích tối thiểu gồm Allergy Warning Banner + Assigned Meal Card (Distribution Detail) và 4 tab (Health/Allergy/Special Meal/Meal Records) cho Student Detail.
3. Sửa `parent/feedback/page.tsx`: đổi `QUALITY_TAGS` checkbox → `Textarea`/`Input` tự do (khớp `16_SubmitMealFeedback.md` đã chốt); đổi Dish Selection từ hardcode sang `Select` liệt kê món trong 24h.
4. Thêm Allergy Alert Meal Card (viền destructive) vào `parent/weekly-menu`, so khớp allergen món ăn với hồ sơ dị ứng đã xác minh của học sinh.
5. Thêm 4 field còn thiếu vào `teacher/incidents` Incident Form (Number of Affected Students, Ingestion Date, Severity Radio, Attached Photos) + dựng `teacher/upload-evidence` hoặc tích hợp trực tiếp file input vào form.

**Giai đoạn 2 — Hoàn thiện luồng chính**
6. Dựng nhóm màn Sức khỏe Parent: `parent/allergies` tách rõ Category Select + Specific Name + Upload Medical Proof thật; thêm action "Sửa" cho từng dòng dị ứng đã khai (Update Allergy Information); dựng khối Height/Weight/Blood Type/BMI (Student Health Information) + form Submit/Update Health Information.
7. Dựng `parent/special-meals` (Special Meal Arrangements + Special Meal Records) với Dual-Confirmation Status Badge.
8. Dựng `teacher/meal-records` (ghi nhận tiêu thụ) + Consumption Record Dialog.
9. Đổi Distribution Confirmation (`teacher/distribution` nút "Xác nhận") sang `AlertDialog` có Timestamp Display + Cancel/Confirm, thêm Confirm All Button + Status Filter Tabs.

**Giai đoạn 3 — Điều hướng, hub, PDF/chart, dọn hồ sơ**
10. Dựng Nutrition Detail/History (Parent), School Meals Dashboard, Feedback Center hub, Submit Food Quality Feedback, Meal History Detail; thêm Child Selector + Notification Bell ở Topbar; Download PDF + View Nutrition Details ở Weekly Menu.
11. Dựng Allergy/Health Information dạng màn riêng cho Teacher (đọc-only) + Special Meal Requirements riêng + Profile/Assessment Filter Tabs; hoàn thiện Teacher Home (2 nút hành động + Notice Board đúng dạng).
12. Viết bổ sung 2 cặp Screen Spec Announcements List/Detail (Parent, Teacher) để khớp ngược với code đã có, tránh audit sau này lại phát hiện "code không có spec".

---

## 8. Ghi chú giới hạn của review này

- Review dựa trên mock data tĩnh (`mock-data.ts`), chưa nối API thật — một số hành vi điều kiện (VD: Allergy Threat Alert Box chỉ hiện khi có allergen trùng thực đơn hôm nay) hiện không thể verify runtime, chỉ đánh giá được qua code tĩnh.
- Chưa audit `src/components/app/announcements/*` chi tiết (announcement-list, announcement-detail) — chỉ xác nhận route tồn tại và khớp ScreenFlow, chưa so field-by-field vì không có Screen Spec nguồn để đối chiếu (xem mục 5.1, 6).
- Chưa audit các actor khác (Medical Staff, Kitchen Staff, Supplier, School Admin, System Admin) — ngoài phạm vi yêu cầu.
