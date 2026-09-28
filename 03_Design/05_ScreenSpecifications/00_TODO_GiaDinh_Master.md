# TODO Tổng hợp — Các điểm "[Giả định — cần team xác nhận]" toàn dự án

**Ngày lập**: 2026-07-01 | **Người tổng hợp**: Nguyễn Phương Thảo (thực hiện mục 6 — Review & tổng hợp, `00_Team_Work_Assignment_Plan.md`)
**Mục đích**: Gom toàn bộ các điểm còn đánh dấu `[Giả định — cần team xác nhận]` rải rác trong 321 file `05_ScreenSpecifications/` thành 1 bảng duy nhất để cả 5 thành viên rà soát và chốt **trước khi chuyển sang bước `06_Wireframes/`**, theo đúng yêu cầu mục 6.5 của kế hoạch phân công.

**Phạm vi quét**: toàn bộ `Web/` + `Mobile/`, cả 8 actor, loại trừ các file `00_TongHop_*.md` (rollup, không phải nguồn). Tổng cộng phát hiện **218 vị trí đánh dấu Giả định trong 88 file**, gộp theo màn hình (Web+Mobile cùng 1 vấn đề chỉ tính 1 dòng) còn **56 màn hình cần xác nhận**.

> **Cập nhật 2026-07-01 (Claude, sau khi audit lại)**: đã verify độc lập cả 8 vấn đề A1–A8 bằng cách đọc trực tiếp file nguồn (không chỉ tin theo bảng này). **A1, A2, A3, A5, A6, A8 đã xử lý xong** (thêm BR-201/MSG-090, sửa Design_System.md + 29 file cascade + Report 3.md/traceability_matrix.md; gỡ 2 cột `x` sai trong `14_Screen_Authorization_Matrix.md` theo quyết định team; A8: 7/8 màn gộp vào UC có sẵn hoặc UC-171/172 viết mới, 1/8 màn (SecurityPolicies) cần UC hoàn toàn mới — đã thêm UC-184). A4 đã verify, không chặn tiến độ. A7 đã spot-check, chất lượng tốt. Ngoài ra phát hiện + sửa thêm 2 lỗi trích dẫn mới (UC-091 MSG-013→009, UC-152 gỡ BR-050 lạc trong traceability_matrix.md) khi verify chéo báo cáo Mạnh, và phát hiện 1 nghi vấn ghost-citation mới chưa xử lý (UC-095 trích "BR-030" sai nghĩa — xem A8 SchoolConfiguration, cần Huy xác nhận riêng).
>
> **Cập nhật 2 — 2026-07-01 (theo xác nhận trực tiếp của chủ dự án)**: hệ thống **không quản lý campus** — màn `CampusConfig` (dòng 47, System Admin) đã **loại bỏ hoàn toàn**. Field bán kính geofence (BR-028) nay CONFIRMED chính thức thuộc **School Administration** (`37_SchoolConfiguration.md`, dòng 25) — đã cập nhật `13_CampusConfig.md` (đánh dấu loại bỏ), `12_UpdateSchoolScreenDialog.md` (gỡ field bán kính geofence khỏi System Admin), `14_Screen_Authorization_Matrix.md` (System Admin 20→19 màn, tổng 192→191), `01_System_Admin_Screen_Flow.md`, và `00_Team_Work_Assignment_Plan.md` (tổng file 329→328). **Rollup `00_TongHop_SystemAdmin.md`/`00_TongHop_SchoolAdministration.md` cần tái tạo lại** để đồng bộ (xem mục C).

---

## A. Vấn đề xuyên suốt (cross-cutting) — ưu tiên xử lý trước

| # | Vấn đề | Phạm vi ảnh hưởng | Đề xuất |
| --- | --- | --- | --- |
| A1 | ~~**BR-038 không tồn tại**~~ **ĐÃ XỬ LÝ (2026-07-01, sau khi Claude audit)**: xác nhận đúng như Thảo/Huy/Nam phát hiện — root cause ở `Design_System.md` (không phải Accessibility.md, đã kiểm tra không có). Đây trùng với ghost-code đã có sẵn nghị quyết trong `BR_Citation_Audit_Report.md` (Bucket B, map sang **BR-123**). Đã sửa `Design_System.md` (5 chỗ) + cascade sửa toàn bộ **29 file** trong `05_ScreenSpecifications/` (20 file gốc + 9 file `00_TongHop_*`, nhiều hơn 25 ước lượng ban đầu vì grep ra chính xác toàn repo). Không còn dòng nào trích "BR-038" trong `05_ScreenSpecifications/` ngoài chính bảng ghi chú lịch sử này. | ~~25~~ 29 file, 5 actor (Teacher, ServiceCaterers, SchoolAdministration, MedicalStaff, GuestCommon; Parent chỉ ở bản Mobile) | Không còn hành động cần thiết cho `05_ScreenSpecifications`. **Còn mở**: `02_Requirements/02_UseCases/MF03_Student_Parent_Management.md`, `MF09_Meal_Serving_Distribution.md`, và 3 file `07_implement/MF03_Student_Parent_Management/*` cũng trích "BR-038" — ngoài phạm vi hôm nay, cần 1 vòng audit riêng. |
| A2 | ~~**BR-040 lệch nghĩa**~~ **ĐÃ XỬ LÝ (2026-07-01)**: xác nhận đúng, và khi grep lại toàn Report 3.md phát hiện thêm **UC-025 (Create Student) và UC-053 (Monitor Child Meal History)** cũng bị lỗi này — tổng 6 UC (không phải 4 như ước lượng ban đầu). Theo quyết định của team, đã thêm **BR-201** "Giới hạn 5 hồ sơ học sinh/phụ huynh" vào `business_rules.md` §4, sửa cả 9 vị trí trích dẫn sai trong Report 3.md, cập nhật `traceability_matrix.md` (6 dòng UC + 1 dòng BR mới). | Report 3.md, business_rules.md, traceability_matrix.md (không có file `05_ScreenSpecifications` nào trích sai — Thảo đã đúng khi không hard-code BR-040 ở Parent) | Không còn hành động cần thiết. |
| A3 | ~~**MSG-076 lệch nghĩa**~~ **ĐÃ XỬ LÝ (2026-07-01)**: xác nhận đúng. Đã thêm **MSG-090** "Chưa có thực đơn được công bố cho tuần đã chọn" vào `error_message.md` (§1 Error + bảng tóm tắt module), sửa citation UC-135 EX1 trong Report 3.md từ MSG-076 → MSG-090, cập nhật `traceability_matrix.md`. | `Parent/02_WeeklyMenu.md` (đã dùng câu chữ tự do — không cần sửa file này, giờ đã có mã MSG-090 chính thức để tham chiếu nếu muốn khóa spec chặt hơn) | Không còn hành động cần thiết — có thể cập nhật `02_WeeklyMenu.md` trích thẳng MSG-090 thay vì câu chữ tự do khi khóa spec. |
| A4 | **Quy ước file khác biệt (Huy)** — **đã verify đúng** (Claude đọc trực tiếp `Web/MedicalStaff/16_ViewSchoolPolicyScreen.md` và bản Mobile tương ứng): cả 2 file đều chứa **cả 2 khối** "Platform Layout — Web" **và** "Platform Layout — Mobile" (chỉ khác thứ tự), khác hẳn quy ước 1-file-1-platform của Minh (đã verify `Web/ServiceCaterers/15_ThreeStepInspectionDashboard.md` chỉ có 1 khối Web). Không phải lỗi sai dữ liệu (2 bản field khớp nhau, chỉ là dư thừa thông tin), nhưng sẽ gây nhầm khi sinh Wireframe ở `06_Wireframes/00_Team_Work_Assignment_Plan.md` (giả định mỗi file chỉ có 1 khối Platform Layout tương ứng nền tảng của file đó). | 37 file (MedicalStaff, GuestCommon) | **Khuyến nghị**: không bắt Huy viết lại 37 file — khi làm Wireframe (`06_Wireframes`), người xử lý file của Huy chỉ cần lấy đúng khối Platform Layout khớp với nền tảng đang làm, bỏ qua khối còn lại. Không chặn tiến độ. |
| A5 | ~~**"Menu Detail" — khoảng trống Matrix vs Parent scope**~~ **ĐÃ XỬ LÝ (2026-07-01, theo quyết định team: kiểm tra có page riêng không, không có → gỡ khỏi Matrix)**: kiểm tra 3 nguồn độc lập đều xác nhận không có page riêng cho Parent — (1) Report 3 UC-134 "View Menu Detail" ghi rõ Primary Actor = School Administration, Secondary Actors = **None**; (2) `05_Parent_Screen_Flow.md` và `10_Parent_Screen_Flow_Web.md` chưa từng có node "Menu Detail"; (3) 18 file Screen Spec thật của Parent không có file này. Đã gỡ cột Parent (`x`) khỏi dòng "Menu Detail" trong `14_Screen_Authorization_Matrix.md`, ghi chú rõ lý do + nguồn đối chiếu. | Matrix (đã sửa) | Không còn hành động cần thiết. |
| A6 | ~~**"Incident Detail" — Teacher không có file riêng**~~ **ĐÃ XỬ LÝ (2026-07-01, cùng logic A5)**: kiểm tra xác nhận không có page riêng cho Teacher — (1) Report 3 UC-116 "View Food Safety Incident Detail" ghi Primary Actor = School Administration, Secondary Actors = **None**; (2) ScreenFlow Teacher (Mobile+Web) chưa từng có node "Incident Detail"; (3) `Web/Teacher/13_IncidentHistory.md` chỉ có bảng tổng hợp read-only, không có action/modal xem chi tiết. Đã gỡ cột Teacher (`x`) khỏi dòng "Incident Detail" trong `14_Screen_Authorization_Matrix.md` (giữ nguyên cột School Administration + Medical Staff — cả 2 đều có file riêng hợp lệ). | Matrix (đã sửa) | Không còn hành động cần thiết. |
| A7 | **7 màn dùng chung nhiều actor — đã hợp nhất xong** (xem chi tiết mục B): ComplianceDashboard, IncidentDetail, IncidentList, RecordProcessViolation, RecordStaffViolation, RecordViolationResolution, ViolationRegistry. | 7 cặp màn hình, 3 actor (SchoolAdministration/MedicalStaff/ServiceCaterers) | Không còn hành động cần thiết — đã đối chiếu field dữ liệu khớp `class_diagram_description.md`, giữ nguyên action riêng theo actor. |
| A8 | ~~**8 màn hoàn toàn chưa có UC nguồn**~~ **ĐÃ XỬ LÝ (2026-07-01)**: phân loại từng màn thay vì gộp chung UC-171/172 như suy đoán ban đầu — 3 cách xử lý khác nhau: **(1) Gộp vào UC-171/172 (viết mới Functional Spec, Report 3.md §3.2.13)** — vì 2 UC này trước đó chỉ là stub trong traceability_matrix, không có bảng đặc tả: `13_FoodSampleRetentionReports.md` (UC-171+172), `16_PartnershipReports.md` (UC-171+172), `35_SystemUsageMetrics.md` (UC-171), `36_TraceabilityReports.md` (UC-171+172). **(2) Gộp vào UC đã có sẵn đầy đủ, không cần đụng UC-171/172** — phát hiện qua đọc kỹ Report 3: `14_ReceivingReports.md` → **UC-123** (View Ingredient Receiving Records, đã có sẵn); `17_PartnershipApprovalHistory.md` → **UC-106** (View Audit Logs — Matrix xác nhận School Admin đã có quyền Audit Logs, chỉ là 1 view lọc); `37_SchoolConfiguration.md` → **UC-095** (Update Information School — Description xác nhận rõ School Administration cũng sửa được "operational configuration fields"). **(3) Bắt buộc UC hoàn toàn mới** — chỉ 1/8 màn: `Web/SystemAdmin/15_SecurityPolicies.md` → **UC-184 - Configure Security Policies** (mới, Report 3.md §3.2.12.13), vì đây là màn cấu hình (không phải xem/xuất báo cáo) và không UC nào có sẵn phù hợp. Đã cập nhật cả 8 file Screen Spec, `14_Screen_Authorization_Matrix.md`, và `traceability_matrix.md`. | 8 file, 2 actor (đã sửa) | Không còn hành động cần thiết cho việc gán UC. **Còn mở (không chặn tiến độ)**: (a) team xác nhận phạm vi giá trị cho phép sửa trong UC-184 trước khi code hoá; (b) field "Bán kính geofence" trong SchoolConfiguration cần xác nhận có tồn tại hay chỉ ở Campus Config (System Admin); (c) nghi vấn UC-095 trích "BR-030" sai nghĩa — cần Huy xác nhận riêng, ngoài phạm vi A8. |

---

## B. Bảng TODO theo màn hình (56 mục, gộp Web+Mobile trùng lặp)

*(⚠️ = thuộc nhóm A8 — **đã xử lý 2026-07-01**, xem chi tiết UC gán cho từng dòng trong mục A8 ở trên)*

### Teacher

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 1 | HealthInformation | Mobile/Web | Report 3 không có wireframe riêng cho Teacher xem "Health Information" (chỉ có bản Parent — "Student Health Hub" — và Medical Staff). Field tham chiếu cấu trúc "Student Health Hub" (Report 3 dòng 5989–6011) nhưng chỉ đọc, thu gọn. Cần xác nhận phạm vi field chính xác Teacher được phép xem. |
| 2 | MealRecords | Mobile | Không có trong sơ đồ mermaid gốc của `07_Teacher_Screen_Flow.md` — bổ sung theo UC-056/UC-080. Cần xác nhận vị trí điều hướng. |
| 3 | StudentDetail | Mobile/Web | Field "Parent Contact Info" — cần xác nhận mức độ chi tiết được phép hiển thị cho Teacher. |
| 4 | StudentList | Mobile/Web | Nội dung câu Empty state "Lớp chưa có học sinh nào" — chưa có nguồn chính thức, suy luận hợp lý. |
| 5 | TeacherHome | Mobile/Web | Empty state khi chưa được phân công lớp chủ nhiệm — Report 3 không mô tả trạng thái này. |
| 6 | UploadEvidence | Mobile | Elevate từ field "Attached Photos Component" (Report 3 dòng 6381) thành 1 màn hình riêng — cần xác nhận đây có phải màn hình độc lập hay chỉ là component nhúng trong Incident Form. |

### Parent

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 7 | FeedbackCenter | Mobile/Web | Field "My Feedback History List" — cần xác nhận có hiển thị lịch sử phản hồi tại đây hay không. |
| 8 | NutritionHistory | Mobile/Web | Không có wireframe field-by-field riêng trong Report 3 — suy ra từ mục đích hiển thị biểu đồ dinh dưỡng (BR-062). Cần xác nhận cấu trúc chi tiết. |
| 9 | ParentDashboard | Mobile | Xem mục A2 (BR-040 lệch nghĩa) — cần team quyết định giữ/bỏ giới hạn 5 con/phụ huynh. |
| 10 | SchoolMealsDashboard | Mobile/Web | Câu chữ Empty state "Chưa có thực đơn được công bố" — không có MSG riêng, tương tự Weekly Menu. |
| 11 | SubmitAllergyInformation | Mobile/Web | Chặn submit khi Severe thiếu tài liệu — không có MSG code riêng cho case này, đang dùng MSG-005 (validation chung). |
| 12 | WeeklyMenu | Mobile/Web | Xem mục A3 (MSG-076 lệch nghĩa) — cần bổ sung mã MSG mới cho "chưa có menu published". |

### MedicalStaff

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 13 | InvestigationWorkspace | Mobile/Web | Field "Kết quả xét nghiệm lab" — cần xác nhận cấu trúc field cụ thể ngoài mô tả tổng quát UC-041 bước 4. |
| 14 | SpecialMealRequestList | Mobile/Web | Matrix ánh xạ UC-036, nhưng nội dung UC-036 trong Report 3 mô tả "View List Allergy & Special Meals" — field thực tế lấy từ §3.3.2.3.3. Cần xác nhận UC-036 có đúng là UC nền cho màn này không. |
| 15 | StudentHealthRecordDetail | Mobile/Web | Không có UC-### hay field-by-field riêng cho màn Detail trong Report 3 — suy luận từ mô tả "clinical file" (UC-051). Cần xác nhận cấu trúc field đầy đủ. |
| 16 | ViewSchoolPolicyScreen | Mobile/Web | UC-137 chỉ có 1 dòng mô tả tóm tắt (Report 3 dòng 1144), không có bảng Normal/Alternative/Exception Flow đầy đủ ở đâu trong Report 3 — khoảng trống dữ liệu nguồn thật sự, cần BA bổ sung UC đầy đủ. |

### SchoolAdministration

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 17 | AllergyReview | Web | Xem mục A1 (BR-038 không tồn tại) — lỗi kế thừa từ Design_System.md, không phải do file này tạo ra. |
| 18 | CreateAnnouncement | Web | UC-017 trích MSG-072 (sai domain — MSG-072 thật là "Special meal plan saved"). Đang dùng MSG-047 thay thế — cần team xác nhận. |
| 19 | ✅ FoodSampleRetentionReports | Web | **Đã xử lý (A8)**: gộp vào UC-171 (View Analytics Dashboard) + UC-172 (Export Report), viết mới Functional Spec tại Report 3.md §3.2.13. |
| 20 | LinkedStudentsList | Web | Action "gỡ liên kết" — Normal Flow gốc UC-025 không mô tả rõ hành vi này, suy luận từ nhu cầu quản lý ngược. |
| 21 | MenuDetail | Web | UC-006 trích BR-070/074/075 nhưng các mã này trong business_rules.md hiện hành mang nội dung khác (lệch số BR có thể do cập nhật khác thời điểm). Đã bổ sung BR-067/068 làm căn cứ chính xác hơn — cần team đối chiếu lại nghi vấn lệch số. |
| 22 | ✅ PartnershipApprovalHistory | Web | **Đã xử lý (A8)**: gộp vào UC-106 (View Audit Logs, đã có sẵn Functional Spec) — Matrix xác nhận School Admin đã có quyền Audit Logs, đây chỉ là 1 view lọc theo Resource = Partnership. Không cần UC mới. |
| 23 | ✅ PartnershipReports | Web | **Đã xử lý (A8)**: gộp vào UC-171 + UC-172, viết mới Functional Spec tại Report 3.md §3.2.13. |
| 24 | ✅ ReceivingReports | Web | **Đã xử lý (A8)**: gộp vào UC-123 (View Ingredient Receiving Records, đã có sẵn Functional Spec đầy đủ). Không cần UC mới hay đụng UC-171/172. |
| 25 | ✅ SchoolConfiguration | Web | **Đã xử lý (A8 + 2026-07-01)**: gộp vào UC-095 (Update Information School) — Description xác nhận School Administration cũng sửa "operational configuration fields". Field "Bán kính geofence" **nay đã CONFIRMED chính thức thuộc School Administration** (không phải Campus Config/System Admin — đã loại bỏ, xem dòng 47 CampusConfig) theo đúng nguyên văn BR-028. **Còn mở**: UC-095 trích "BR-030" sai nghĩa (BR-030 thật = liên kết học sinh-phụ huynh) — cần Huy xác nhận riêng. |
| 26 | ✅ SystemUsageMetrics | Web | **Đã xử lý (A8)**: gộp vào UC-171 (View Analytics Dashboard), phạm vi thu hẹp 1 trường — khác UC-150 (System Admin, toàn hệ thống). |
| 27 | ✅ TraceabilityReports | Web | **Đã xử lý (A8)**: gộp vào UC-171 + UC-172, viết mới Functional Spec tại Report 3.md §3.2.13. |
| 28 | UpdateAnnouncement | Web | UC-089 trích MSG-074 (sai domain — MSG-074 thật là "Menu updated successfully"). Đang dùng MSG-047/thông báo chung thay thế — cần team xác nhận. |
| 29 | UploadStaffList | Web | Matrix ghi "biến thể bulk" của UC-024, nhưng Normal Flow gốc UC-024 chỉ mô tả tạo 1 staff đơn lẻ, không mô tả luồng bulk upload file. Cấu trúc file template bulk — cần xác nhận. |

### ServiceCaterers

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 30 | InspectMealsBeforeServingScreen | Mobile/Web | UC-065 chỉ có 1 dòng khai báo trong bảng tổng hợp UC, không có bảng đặc tả riêng — suy ra từ Step 3 Normal Flow UC-062 (dòng 3542). |
| 31 | InventoryManagementDashboard | Mobile/Web | UC-045 trích BR-081/082/083/089/090 nhưng các mã này thuộc module khác trong business_rules.md hiện hành (Menu&Diet, Food Safety Monitoring...) — dùng BR-075/076/077/089 (ngữ nghĩa gần nhất) thay thế. Cần xác nhận mã BR. **Cập nhật 2026-07-01**: phần trích "BR-081" trong ghi chú này KHÔNG liên quan đến rule "món thay thế dị ứng" đã xoá — đây là 1 trong 2 nhóm từng đụng số với "BR-081" (nhóm kia là UC-061 Record Ingredient Storage, đã repoint sang **BR-202**, xem `traceability_matrix.md` §2 và mục A8). Ghi chú UC-045 ở đây vẫn giữ nguyên [Giả định] cũ (dùng BR-075/076/077/089), **chưa xác nhận riêng** — xem thêm dòng cảnh báo mở rộng ở cuối bảng này (BR-082/BR-083 nghi trùng số tương tự). |
| 32 | RecordInventoryTransactionsLog | Mobile/Web | UC-064 trích BR-082 cho "reason code + sign-off khi điều chỉnh kho", nhưng BR-082 thật là "Chế biến riêng bữa ăn đặc biệt" — business_rules.md hiện không có mã BR nào mô tả đúng yêu cầu này. Giữ yêu cầu nghiệp vụ nhưng không gán BR — cần bổ sung mã BR mới nếu team đồng ý giữ yêu cầu. **Nghi vấn tương tự BR-081**: xem cảnh báo mở rộng cuối bảng. |
| 33 | RecordSampleInformationForm | Mobile/Web | UC-069 AF2 ghi MSG-072 cho lỗi GPS, nhưng MSG-072 thật là thông báo Success khác — đang dùng MSG-020 thay thế, cần xác nhận mã MSG chính xác. |
| 34 | RejectIngredientBatchForm | Mobile/Web | UC-072 không có mã Success riêng cho reject — dùng Toast chung không gắn mã MSG. Cần xác nhận có cần bổ sung MSG riêng hay không. |
| 35 | ServiceCaterersDashboard | Mobile/Web | Widget tóm tắt hoạt động hôm nay — chưa có mô tả field-by-field gốc trong Report 3, chỉ suy từ vai trò tổng quan của Dashboard. |
| 36 | UpdateMenuScreen | Mobile/Web | UC-096 trích BR-072/074 (thuộc module Ingredient Receiving, không liên quan) — không đưa vào spec. Quy tắc "giới hạn lặp món chính 2 lần/tuần" (MSG-075) chưa có mã BR chính thức khớp — giữ MSG-075 nhưng không gán BR cho tới khi team xác nhận. |
| 37 | UploadImageCookedFoodScreen | Mobile/Web | UC-063 chỉ có 1 dòng khai báo, không có bảng đặc tả riêng — suy ra từ Step 2 Normal Flow UC-062 (dòng 3542). |

### RawFoodSupplier

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 38 | Deliveries | Mobile/Web | Bộ đếm nhanh theo trạng thái — giả định nhẹ, mở rộng so với Report 3 gốc, không đổi field cốt lõi. |
| 39 | IngredientBatches | Mobile/Web | Bộ đếm nhanh theo trạng thái lô — giả định nhẹ, mở rộng để định hướng nhanh. |
| 40 | PurchaseRequestDetail | Mobile/Web | Nút "Reject" yêu cầu nhập lý do — Report 3 không mô tả chi tiết field lý do, chỉ có trong ScreenFlow. Cần xác nhận có bắt buộc nhập lý do hay không. |
| 41 | PurchaseRequests | Mobile/Web | Bộ đếm nhanh theo trạng thái — bổ sung nhẹ để tăng khả năng điều hướng, cần xác nhận có cần hay không. |
| 42 | SelectSchoolWorkspace | Mobile/Web | Không có UC/BR nào mô tả tường minh việc 1 Raw Food Supplier phục vụ nhiều hơn 1 trường cùng lúc — Report 3 §3.3.2.5.1 chỉ mô tả 1 trường đối tác duy nhất. Field là suy luận tối thiểu — cần Product Owner/BA xác nhận mô hình multi-school trước khi khoá spec. |
| 43 | SupplierDashboard | Mobile/Web | Card "School Partnerships" — bổ sung so với Report 3 gốc (chỉ có 4 nút) theo nhánh Screen Flow. Cần xác nhận vị trí chính xác trên Dashboard. |

### SystemAdmin

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 44 | ActiveDeactivateSchoolDialog | Web | Report 3 liệt kê MSG-011 cho Exception, nhưng MSG-011 thật là về homeroom teacher (không liên quan) — nghi ngờ lỗi copy-paste trong Report 3 gốc. Dùng MSG-001 thay thế — cần xác nhận. |
| 45 | AuditLogs | Web | Nút "Export CSV/PDF" — không có trong Normal Flow gốc UC-106, thêm để nhất quán với các màn báo cáo khác (BR-178). |
| 46 | BackupManagement | Web | Bảng lịch sử các lần backup — Normal Flow gốc chỉ nói "display configuration and operating status", không xác nhận có lịch sử dạng bảng hay không. |
| 47 | ❌ CampusConfig | Web | **ĐÃ LOẠI BỎ (2026-07-01)** — chủ dự án xác nhận hệ thống không quản lý campus. BR-028 xác nhận field bán kính geofence chính thức thuộc School Administration (→ dòng 25 SchoolConfiguration), không phải System Admin. Đã gỡ khỏi `14_Screen_Authorization_Matrix.md` (System Admin 20→19 màn) và gỡ field tương ứng khỏi `12_UpdateSchoolScreenDialog.md`. Không cần Wireframe cho màn này. |
| 48 | ManageRoleScreen | Web | Report 3 không gán MSG code riêng cho Update/Delete Role — dùng MSG-029 theo pattern chung, cần xác nhận. |
| 49 | SchoolList | Web | UC-140 gán BR-022/BR-025 nhưng cả 2 đều không mô tả trực tiếp nghiệp vụ "xem danh sách trường" — nghi ngờ boilerplate không khớp hoàn toàn, cần xác nhận mức độ liên quan thực tế. |
| 50 | ✅ SecurityPolicies | Web | **Đã xử lý (A8)**: đây là màn duy nhất trong 8 màn A8 bắt buộc UC mới — đã thêm **UC-184 - Configure Security Policies** tại Report 3.md §3.2.12.13, xác nhận System Admin được sửa (không chỉ xem) các tham số BR-002/004/006. **Còn mở**: phạm vi giá trị cho phép sửa (min-max) cần team xác nhận trước khi code hoá. |

### GuestCommon

| # | Màn hình | Nền tảng | Nội dung giả định / vấn đề cần xác nhận |
| --- | --- | --- | --- |
| 51 | ForgotPassword | Mobile/Web | Nội dung cảnh báo rate-limit/email không tồn tại — không có MSG code riêng, cần xác nhận nội dung chính xác. |
| 52 | Login | Mobile/Web | "Register Options" dẫn tới 4 màn Guest Portal/Register (School/Supplier/Service Caterers) — không có dòng riêng trong Matrix khối 1, không tạo file riêng. |
| 53 | NotificationCenterList | Mobile/Web | Không có UC-### chính thức ánh xạ trong traceability_matrix.md — nội dung suy ra từ Report 3 §3.3.2.1.13. Cần xác nhận hành vi filter/phân trang cụ thể. |
| 54 | NotificationDetail | Mobile/Web | Không có nguồn field-by-field trong Report 3 (chỉ có "Notification History" dạng list) và không có UC-### nào ánh xạ — toàn bộ field là suy luận hợp lý từ pattern List→Detail chung của hệ thống. |
| 55 | NotificationSettings | Mobile/Web | Toast xác nhận lưu cấu hình — không có MSG code riêng trong error_message.md, cần xác nhận có cần thêm mã MSG riêng. |
| 56 | VerifyOTP | Mobile/Web | Không có UC-### riêng theo Matrix khối 1 (cột ghi "—"). Thời lượng countdown cụ thể, số lần Resend tối đa — chưa có nguồn UC/BR rõ ràng. |

---

## B2. ⚠️ Phát hiện hệ thống: trùng số Business Rule (BR number collision)

**Bối cảnh (2026-07-01)**: Khi xoá BR-081 ("Món thay thế cho 5 loại dị ứng phổ biến" — team quyết định loại bỏ vì không thực tế, xem `business_rules.md` §7), rà soát toàn dự án phát hiện số "BR-081" đã bị **2 nhóm hoàn toàn không liên quan cùng dùng**: (1) Menu & Diet Management — rule "món thay thế dị ứng" (đã xoá), và (2) Ingredient & Inventory Management — rule thật "khớp khu vực lưu trữ theo yêu cầu nhiệt độ" (UC-061 Record Ingredient Storage, các file `11_StoreIngredientsForm.md`, wireframe `07-14_ReceivingAndEvidence`). Nhóm (2) đã được cấp mã mới **BR-202** và cập nhật toàn bộ (ScreenSpec Web/Mobile + rollup + wireframe .md/.html + `traceability_matrix.md`).

**Nghi vấn còn mở — cần audit riêng, KHÔNG xử lý trong đợt này**: cùng cơ chế lỗi (Report 3.md dùng số BR khác với `business_rules.md` chuẩn) có khả năng lặp lại ở:
- **BR-082**: `traceability_matrix.md` dòng UC-064 (Record Inventory Transactions) trích BR-082 với nghĩa "reason code + sign-off khi điều chỉnh kho tồn" (xem dòng 32 bảng B ở trên) — nhưng BR-082 thật trong `business_rules.md` §7 là "Chế biến riêng cho bữa ăn đặc biệt" (Menu & Diet), hoàn toàn không liên quan.
- **BR-083**: `traceability_matrix.md` dòng UC-045 (Manage Inventory) trích BR-083 cùng nhóm BR-081/082/089/090 (xem dòng 31 bảng B) — nhưng BR-083 thật trong `business_rules.md` §8 là "Ghi nhận bằng chứng mỗi bước" (Three-Step Inspection), cũng không liên quan tới "quản lý tồn kho".

Cả 2 trường hợp đều đã tự gắn `[Giả định — cần team xác nhận]` từ trước và dùng BR-075/076/077/089 thay thế trong Screen Spec — **chưa gây lỗi hiển thị** như trường hợp BR-081, nhưng nên được đội ngũ xác nhận theo đúng quy trình đã dùng cho BR-081 (nếu đúng là trùng số ý nghĩa thật ở module khác → cấp mã BR mới, không tái sử dụng số cũ — *lưu ý: BR-203 đã được dùng cho rule mới "khối lượng mẫu lưu tối thiểu", nên trường hợp này nếu cần cấp số mới thì bắt đầu từ BR-204*).

---

## B3. ⚠️ Phát hiện mới (2026-07-02, đối chiếu `business_rules.md` với `00_Policies/`)

Khi đối chiếu Module 6/8/9/12 với 4 văn bản pháp luật trong `00_Policies/` (QĐ 1246/QĐ-BYT, Luật ATTP 55/2010/QH12, NĐ 15/2018/NĐ-CP, TTLT 13/2016/TTLT-BYT-BGDĐT), đã sửa trực tiếp trong `business_rules.md`: BR-111 (cụ thể hoá 72 giờ), BR-203 mới (khối lượng mẫu lưu tối thiểu — đã repoint citation sai "BR-107" trong `21_RecordSampleInformationForm.md`, `error_message.md` MSG-019, `traceability_matrix.md`), BR-105/106/112 (bổ sung điều kiện/ngoại lệ theo luật), BR-047 (nâng ngưỡng cảnh báo hết hạn tài liệu NCC lên 2 mức 180/30 ngày thay vì chỉ 30 ngày), chú thích nguồn cho BR-090/091/103, làm rõ lại BR-079.

**Còn mở, cần xử lý tiếp**:
1. **Screen Spec chưa đồng bộ ngưỡng BR-047 mới**: ~13 file ScreenSpec (`RawFoodSupplier/05_FoodSafetyCertificates.md`, `06_UploadSupplierCertificates.md`, `25_UpdateDocuments.md`, `26_ComplianceDocuments.md`, `SchoolAdministration/05_SupplierDetail.md`, `38_VerificationDocuments.md`, và các rollup `00_TongHop_*` liên quan) vẫn mô tả badge "Expiring Soon" chỉ ở ngưỡng **<30 ngày** — cần team quyết định có bổ sung badge/tier cảnh báo sớm (180 ngày) vào UI hay giữ nguyên 1 badge khẩn cấp duy nhất (chấp nhận BR-047 chỉ là ngưỡng nội bộ hệ thống, còn việc gia hạn hồ sơ là trách nhiệm ngoài hệ thống của nhà cung cấp).
2. **Nghi vấn trích dẫn sai mới phát hiện — MSG-052**: `error_message.md` MSG-052 ("Ingredient batch is close to expiration") đang ghi chú "(BR-047/BR-083)" nhưng BR-047 là cảnh báo hết hạn **tài liệu nhà cung cấp** (không phải hạn dùng nguyên liệu) và BR-083 là "Ghi nhận bằng chứng mỗi bước" — cả 2 đều không đúng ngữ nghĩa "hạn dùng nguyên liệu". Mã đúng nhiều khả năng là **BR-092** ("Kiểm tra hạn sử dụng nguyên liệu"). Ảnh hưởng: `ServiceCaterers/30_MonitorExpirationDatesPanel.md` (Web/Mobile) đang trích BR-047 cho ngữ cảnh nguyên liệu. **Chưa sửa — cần team xác nhận trước khi repoint**, cùng nhóm với nghi vấn BR-082/BR-083 ở mục B2.
3. **Report 3.md UC-030 (Dispose Food Sample) AF1** trích "BR-120" cho việc chặn hủy mẫu khi có sự cố nghi ngờ ngộ độc, nhưng BR-120 thật trong `business_rules.md` là "Xác nhận phân phát bữa ăn theo lớp" (Meal Serving), không liên quan. Đây là citation lỗi thời trong Report 3.md gốc (không sửa theo quy ước dự án) — `business_rules.md` BR-112 (đã cập nhật 2026-07-02) mô tả đúng ngoại lệ này và trỏ đúng về BR-113, không bị ảnh hưởng bởi lỗi trích dẫn của Report 3.

---

## C. Quy trình duyệt đề xuất

1. Team họp rà soát bảng B theo thứ tự ưu tiên: **các màn có mâu thuẫn mã BR/MSG (lệch số) → các field mở rộng nhẹ (rủi ro thấp nhất)** — nhóm A8 (⚠️→✅) đã xử lý xong 2026-07-01, không còn ưu tiên hàng đầu.
2. Với mỗi dòng, quyết định 1 trong 3 hướng: (a) giữ nguyên như đề xuất trong Screen Spec, (b) sửa lại theo hướng khác — cập nhật trực tiếp vào file gốc, (c) cần bổ sung tài liệu nguồn (UC/BR/MSG mới) trước khi khoá spec.
3. Sau khi quyết định xong, gỡ nhãn `[Giả định — cần team xác nhận]` khỏi các file đã chốt, thay bằng ghi chú "Đã xác nhận bởi team ngày ..." để giữ traceability.
4. Chỉ chuyển sang `06_Wireframes/` sau khi bảng này được duyệt (tối thiểu nhóm A8 và mục A1-A4 phải có quyết định rõ ràng — **A8 nay đã xong**).
5. **⚠️ Cần tái tạo file tổng hợp**: `00_TongHop_SchoolAdministration.md` và `00_TongHop_SystemAdmin.md` (Web) vẫn còn nội dung cũ của 8 màn A8 (chưa đồng bộ với 8 file gốc vừa sửa) — đây là file rollup/derived, cần regenerate lại từ các file gốc theo mục 6.6 `00_Team_Work_Assignment_Plan.md` trước khi dùng làm nguồn tham chiếu.

