# Review — Frontend Web (Medical Staff) đối chiếu ScreenFlow / Screen Spec / Class Diagram

**Phạm vi**: `03_Design/08_FrontEnd/Web/src/app/(app)/medical-staff/**` (16 route, Next.js 15 App Router).
**Đối chiếu với**: `03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md` (Mobile), `08_Medical_Staff_Screen_Flow_Web.md` (Web), `03_Design/05_ScreenSpecifications/Web/MedicalStaff/*` (28 file, sau khi bổ sung `28_SendComplianceReportDialog.md` 05/07/2026), `03_Design/01_Diagrams/01_Class_Diagram/class_diagram_description.md`.
**Ngày review**: 05/07/2026. **Cập nhật 05/07/2026 (sau review)**: đội dự án đã tạm chốt BR-207 (chính thức, merge vào `business_rules.md`), loại bỏ đề xuất BR-206 (không đủ căn cứ pháp lý), và tạm chốt BR-208 mới (phân loại kiểu kiểm tra Định kỳ/Đột xuất) — các mục 2, 3.1, 3.4, 3.5 và bảng kế hoạch (Mục 4) bên dưới đã được cập nhật theo quyết định này; đoạn nào chưa cập nhật (do thuộc phạm vi code, chưa triển khai) được giữ nguyên và đánh dấu rõ.

> **Lưu ý về phạm vi actor**: yêu cầu gốc có nhắc "2 actor" nhưng toàn bộ đường dẫn, tên file đích (`review_screen_medical_staff.md`) và thư mục lưu (`.../review/`) chỉ trỏ tới Medical Staff. File `review_screen_parent_and_teacher.md` đã có sẵn cho cặp Parent+Teacher — có thể đó là nguồn gây nhầm số "2". Report này xử lý **một mình Medical Staff**; nếu ý định thực sự là "Medical Staff + actor khác", cần nói rõ actor thứ hai để tôi làm bổ sung.

> **Cập nhật 05/07/2026 (đợt 3) — descope cơ chế "khóa bếp"**: đội dự án quyết định **bỏ hoàn toàn** hậu quả "khóa vận hành bếp, chặn phân phát bữa ăn" mà UC-014 AF1 gốc mô tả khi điểm checklist <60%. Đây là descope có chủ đích so với `Report 3.md` (không phải sửa lỗi trích dẫn) — lý do và chi tiết xem Mục 3.1 (đã cập nhật) và Mục 3.8 (mới). Đã sửa đồng bộ: `business_rules.md` §12, `MedicalStaff_UserStory.md` US-08, Screen Spec 12 (Web+Mobile), `23_ComplianceDashboard.md` (Web+Mobile MedicalStaff), `Web/SchoolAdministration/18_ComplianceDashboard.md`. **Chưa sửa code** (`conduct-inspection/page.tsx`, `compliance/page.tsx`) — đưa vào kế hoạch Mục 4 (#16).
> **Cập nhật 05/07/2026 (đợt 4) — US-09 "Send Compliance Report" đã duyệt**: người dùng đã duyệt hướng (b) — bổ sung UC-185/BR-209, xem Mục 3.9 (cập nhật) cho chi tiết đầy đủ. US-09 trong `MedicalStaff_UserStory.md` nay là `[DECIDED — duyệt 05/07/2026]`. Đã tạo Screen Spec `28_SendComplianceReportDialog.md` (Web+Mobile), cập nhật `business_rules.md`, `error_message.md`, `traceability_matrix.md`, 2 ScreenFlow Medical Staff. Chưa sửa code — mục #19 trong kế hoạch.
> **Cập nhật 06/07/2026 (đợt 5) — Audit UX qua trải nghiệm mô phỏng thực tế**: theo yêu cầu người dùng, đã đóng vai Medical Staff dùng phần mềm theo đúng `MedicalStaff_UserStory.md` (đọc toàn bộ 18 file `page.tsx`, không chạy được dev server thật do sandbox chặn mạng npm) — xem Mục 7 (mới). Phát hiện 3 lỗi logic/dữ liệu nghiêm trọng (Investigation Workspace không lưu dữ liệu thật, cổng kiểm soát CAPA approval bị vô hiệu do thiếu trạng thái trong data model, root cause nhập trùng lặp), 3 thiếu sót tính năng/phản hồi, và các điểm UI/mật độ hiển thị. Người dùng xác nhận toàn bộ phát hiện; **quyết định đợt này chỉ dừng ở tài liệu hóa** (Mục 4, #21–#28), chưa sửa code.
> **Cập nhật 06/07/2026 (đợt 6) — 2 thay đổi nghiệp vụ mới**: (1) Checklist kiểm tra bếp (UC-014) chuyển sang quản lý động — Medical Staff tự CRUD nhóm/hạng mục/chú thích pháp lý, không qua System Admin (**BR-210 mới, UC-186 mới**, sửa đổi BR-207); (2) Biên bản Vi phạm (UC-178/179) thiết kế lại theo mẫu giấy thực tế, xuất PDF + gate bắt buộc (chặn cứng) yêu cầu ảnh biên bản đã ký 3 bên trước khi đóng/gửi duyệt (**BR-211 mới**). Chi tiết đầy đủ ở Mục 8 (mới); đồng bộ file TongHop ở Mục 9 (mới). **Chưa sửa code** — Mục 4 (#29, #30).
> **Cập nhật 09/07/2026 (đợt 7) — ĐẢO NGƯỢC quyết định BR-208**: mọi ghi chú "BR-208 đã chốt/hợp thức hóa" bên dưới (Mục 2, 3.1, 3.4, 3.5, và các dòng #2/#7/#9 trong bảng kế hoạch Mục 4) phản ánh quyết định **05/07/2026 — nay đã bị đảo ngược**. Đội dự án xác nhận field/bước chọn "Loại kiểm tra: Định kỳ/Đột xuất" không có giá trị nghiệp vụ thực tế và quyết định **bỏ hẳn**. BR-208 được đánh dấu `(Deprecated 09/07/2026)` trong `business_rules.md` §12 (giữ nguyên số hiệu, không tái sử dụng). Đã lan truyền việc gỡ bỏ sang toàn bộ 13 file spec/docs liên quan (Screen Spec 12 Web+Mobile, 2 file TongHop MedicalStaff, `MedicalStaff_UserStory.md`); riêng `23_ComplianceDashboard.md` (Web+Mobile), `28_SendComplianceReportDialog.md` (Web+Mobile), `SchoolAdministration/18_ComplianceDashboard.md` và `00_TongHop_SchoolAdministration.md` được xác nhận **không có field/cột/filter nào gắn BR-208** — chỉ có tham chiếu định vị "§12 sau BR-208" (giữ nguyên vì BR-208 vẫn tồn tại, chỉ đổi trạng thái). Chi tiết đầy đủ: `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 3. **Chưa sửa code** (`conduct-inspection/page.tsx`, `compliance/page.tsx`, `mock-data.ts` — loại trừ theo yêu cầu người dùng, sẽ xử lý đợt sau).

---

## 1. Bảng tổng hợp mức độ hoàn thiện theo màn hình

| Route (code) | Nhóm ScreenFlow | UC | Screen Spec tương ứng | Trạng thái |
| --- | --- | --- | --- | --- |
| `dashboard` | — | — | `01_MedicalStaffDashboard.md` | ✅ Đầy đủ |
| `health-reviews` | Hồ sơ SK & Dị ứng | UC-077 | (gộp trong 02) | ✅ Đầy đủ |
| `health-records`, `[id]`, `[id]/history` | Hồ sơ SK & Dị ứng | UC-051, UC-094, UC-146 | `02_ReviewHealthInformationScreen.md` + tương đương | ✅ Đầy đủ |
| `special-meals` | Hồ sơ SK & Dị ứng | UC-107/108/109/110 (List/Review/Approve/Reject) | 4 spec gộp 1 trang | ✅ Đầy đủ |
| `conduct-inspection` | Kiểm tra & Giám sát ATTP | UC-014, **UC-186 (mới 06/07/2026)** | `12_ConductFoodSafetyInspectionScreen.md` | ⚠️ **Chưa khớp bản Spec mới nhất** (§3.1) + **chưa lập trình checklist động BR-210** (§8.1, #29) |
| `compliance` | Kiểm tra & Giám sát ATTP | UC-027 (spec) vs **UC-054 (ScreenFlow)** | `23_ComplianceDashboard.md` | ⚠️ **Lệch UC + thiếu widget** (§3.2) |
| `inspections` | Kiểm tra & Giám sát ATTP | UC liên quan `11_FoodInspectionRecords` | `11_FoodInspectionRecords.md` | ✅ Đầy đủ |
| `reports` | Kiểm tra & Giám sát ATTP | UC-117 | (View Food Safety Reports) | ✅ Đầy đủ |
| `policies` | Kiểm tra & Giám sát ATTP | UC-137 | `16_ViewSchoolPolicyScreen.md` | ✅ Đầy đủ |
| `samples` | Lưu mẫu Thực phẩm | UC-118, UC-076 | `14_RetrieveFoodSampleRecordsDialog.md` + tương đương | ✅ Đầy đủ, chi tiết tốt |
| `incidents`, `[id]`, `investigation` | Sự cố ATTP | UC-009, UC-041, UC-033, UC-168 | `17_InvestigationWorkspace.md`, `19/20` | ✅ Đầy đủ |
| `violations` | Quản lý Vi phạm | UC-178, UC-179, UC-180 | `21_RecordProcessViolation.md`, `24_RecordViolationResolution.md` | ✅ Field đúng, witnessId/repeatSuspect đúng; ⚠️ **chưa lập trình xuất PDF + gate ảnh ký 3 bên BR-211** (§8.2, #30) |
| `announcements`, `[id]` | *(không có trong ScreenFlow)* | UC-125 | *(không có Screen Spec riêng cho Medical Staff)* | ⚠️ **Docs thiếu, code có** (§3.3) |

Kết luận nhanh: 13/16 route khớp tốt với Screen Spec + ScreenFlow. 3 route có vấn đề cần xử lý, chi tiết ở Mục 3.

---

## 2. Đánh giá field/input — đúng bối cảnh bài toán

Đối chiếu `mock-data.ts` (type definitions) với Class Diagram + Screen Spec, các field chính đều đúng:

- `Student`: `allergies`, `specialDiet`, `allergySeverity`, `medicalDocuments` — khớp `AllergyProfile`/BR-031 (bắt buộc tài liệu y tế khi severity = Severe, một ràng buộc đúng nghiệp vụ dù không có trong Class Diagram — xem Mục 3.4).
- `SpecialMealRequest.dietaryType`: validate theo danh mục chuẩn BR-066 — đúng.
- `KitchenInspection`: đã có `score`, `quarantineTriggered`, `failedItems`, `type` — đúng hướng BR-104 và (sau quyết định 05/07/2026) BR-208 cho field `type`; nhưng field bằng chứng vẫn ở mức tổng (`photosCount`), chưa per-item theo BR-207 đã chốt — xem Mục 3.1. Riêng ngưỡng cách ly <60% không còn gắn mã BR nào (BR-206 đã bị loại bỏ) — xem Mục 3.1.
- `ViolationRecord.witness`: đúng, bắt buộc khi severity ≥ MEDIUM (BR-183) — đã xác nhận qua code (`violations/page.tsx`).
- `Incident`: thiếu field `status` chi tiết theo lifecycle đầy đủ (REPORTED → ... → CLOSED) trong type — hiện dùng `status: keyof typeof STATUS_BADGE` chung, không tường minh enum theo Class Diagram `FoodSafetyIncident`. Không sai nhưng kém tường minh — đề xuất khai báo enum riêng nếu cần truy vết trạng thái chặt hơn (mức độ: nhẹ, không chặn release).

Không phát hiện field bịa thêm ngoài bối cảnh nghiệp vụ, trừ 1 điểm ở Mục 3.5 (loại kiểm tra Đột xuất/Định kỳ).

---

## 3. Phát hiện chi tiết (không tự sửa — chờ xác nhận)

### 3.1. `conduct-inspection/page.tsx` chưa cập nhật theo BR-207/BR-208 (đã chốt chính thức 05/07/2026, chưa triển khai vào code)

Code hiện tại đã có Toggle Đạt/Không đạt **theo từng hạng mục** và tính điểm trung bình cộng — đúng hướng đã chốt. Nhưng còn thiếu so với Screen Spec `12_ConductFoodSafetyInspectionScreen.md` bản 2026-07-05 (chính thức):
- Ảnh minh chứng vẫn là **1 counter chung** (`photosCount`) — chưa gắn ảnh theo từng hạng mục (field 2b, **BR-207**, đã chốt).
- Chưa có ô nhập lý do (comment) bắt buộc riêng cho từng hạng mục Không đạt (field 2c, BR-207).
- Chưa có icon "ℹ" chú thích căn cứ pháp lý (field 2d, BR-207).
- Chưa có bước chọn "Loại kiểm tra: Định kỳ / Đột xuất" khi tạo phiên (field 1b, **BR-208**, đã chốt) — hiện `KitchenInspection.type` đã có trong mock-data nhưng UI tạo phiên mới chưa cho chọn tường minh theo đúng field 1b của Screen Spec.
- Code vẫn hard-code trích dẫn `BR-105`/`MSG-024` cho cơ chế "khóa vận hành bếp" (dòng comment đầu file + mô tả header + toast). **Quyết định 05/07/2026 (đợt 3, xem Mục 3.8)**: cơ chế "khóa vận hành bếp, chặn phân phát bữa ăn" bị **loại bỏ hoàn toàn** khỏi hệ thống (không chỉ đổi mã BR) — thay bằng: điểm <60% vẫn ghi "Failed" và **tự động cảnh báo School Administration**, nhưng bếp **tiếp tục vận hành bình thường**. Code cần: (1) bỏ toàn bộ logic khóa/chặn phân phát, (2) đổi toast/Alert từ "Kitchen quarantine is active: Operation blocked" sang cảnh báo dạng warning không chặn, (3) bỏ trích dẫn `BR-105`/`MSG-024` (cả hai đều sai module — `MSG-024` thuộc Incident Management theo `error_message.md`).

**Việc còn thiếu ở trên không còn là "chờ duyệt"** — BR-207/BR-208 đã được đội dự án tạm chốt và merge vào `business_rules.md` §12 (05/07/2026), và quyết định descope khóa bếp cũng đã chốt (đợt 3). Đây giờ là việc lập trình cụ thể cần làm, xem Mục 4 (mục #1, #2, #3, #16).

### 3.2. Compliance Dashboard: lệch UC + thiếu widget so với ScreenFlow

Cả hai file ScreenFlow (`03_Medical_Staff_Screen_Flow.md` dòng 12, `08_Medical_Staff_Screen_Flow_Web.md` dòng 12) mô tả một màn hình **"Monitor Food Safety Dashboard (UC-054)"** với phạm vi rộng: trạng thái 3-bước (BR-091), **nhiệt độ kho/tủ lưu mẫu (BR-089/110)**, điểm checklist (BR-100/101/110), **corrective action đang mở (BR-097/098)**, cảnh báo tủ lạnh.

Nhưng Screen Spec thực tế (`23_ComplianceDashboard.md`) và code (`compliance/page.tsx`) đều tự nhận là **UC-027** (phạm vi hẹp hơn: 3-bước, điểm vệ sinh, cảnh báo non-conformance, banner Quarantine) — khớp với `traceability_matrix.md` dòng 172 (UC-027 = "Daily Food Safety Monitoring", actor chính School Administration, dùng chung Medical Staff).

Theo `traceability_matrix.md` dòng 173, **UC-054** ("Monitor Food Safety", actor Medical Staff, BR-089/100/101/102/103/104, MSG-055/059) là **một UC khác**, không phải bí danh của UC-027. Hệ quả:
- Widget **nhiệt độ tủ lạnh/kho** (BR-089, MSG-055 "Cảnh báo nhiệt độ cao trong kho") — **không có** trong `compliance/page.tsx`.
- Widget xác nhận **kiểm soát côn trùng (BR-102)** và **khám sức khỏe NV bếp (BR-103)** ở dạng dashboard-level — hiện chỉ tồn tại dưới dạng checkbox trong `conduct-inspection`, không có ở Compliance Dashboard.
- MSG-059 ("Nhiệt độ tủ lưu mẫu dao động") chưa xuất hiện ở bất kỳ đâu trong Medical Staff frontend.

**Cần đội dự án xác nhận**: UC-054 có phải một màn hình riêng cần bổ sung, hay là lỗi đặt tên trong ScreenFlow (nên sửa "UC-054" thành "UC-027" cho khớp Screen Spec + code)? Đề xuất cá nhân (kiến trúc): giữ UC-027 làm màn hình chính (đã tốt), bổ sung 1-2 widget thiếu (nhiệt độ kho, MSG-055/059) vào chính `compliance/page.tsx` thay vì tạo màn hình UC-054 riêng biệt — tránh trùng lặp UI, nhưng đây là đề xuất, chưa phải quyết định.

### 3.3. Announcements (UC-125) — có trong code/nav nhưng không có trong ScreenFlow lẫn Screen Spec Medical Staff

`announcements/page.tsx` + `roles.ts` (dòng 222) đã triển khai đầy đủ, trích đúng UC-125/BR-022/BR-138. Nhưng cả hai file ScreenFlow (Mobile lẫn Web) và toàn bộ `03_Design/05_ScreenSpecifications/Web/MedicalStaff/` **không có mục nào** nhắc tới màn hình này cho actor Medical Staff. Đây là **code đi trước docs** — không phải lỗi tính năng (tính năng đúng, đã có ở actor khác), chỉ là ScreenFlow/Screen Spec chưa được cập nhật để phản ánh route đã tồn tại.

### 3.4. `BR-110` — ĐÃ GIẢI QUYẾT (05/07/2026): khôi phục, xác nhận là kiểm tra chéo hợp lệ

Ở lần chỉnh sửa trước (04/07/2026), tôi đã gỡ `BR-110` (nhiệt độ tủ lưu mẫu 2–8°C) khỏi `12_ConductFoodSafetyInspectionScreen.md` với lý do "không liên quan tới checklist vệ sinh bếp" — **kết luận đó sai**. Đối chiếu lại `Report 3.md` (dòng 3459, đặc tả gốc UC-014), dòng "Business Rules" liệt kê rõ BR-110 là một trong các BR của chính UC-014. Đội dự án đã xác nhận hướng (a) trong 2 lựa chọn đã nêu trước đó: **giữ nguyên mã BR-110**, khôi phục vào Screen Spec 12 (Web+Mobile) kèm ghi chú rõ đây là **kiểm tra chéo** (Medical Staff xác minh lại nhiệt độ tủ lưu mẫu trong lúc audit bếp UC-014), không phải quyền sở hữu chính của module này (vẫn thuộc Food Sample Retention, Service Caterers, UC-042/069). Đã bổ sung mục D4 vào `MedicalStaff_KitchenInspectionChecklist_Proposal.md`. Việc còn lại: cập nhật code `conduct-inspection/page.tsx` — comment đầu file hiện ghi đúng BR-110 cho mục này rồi, không cần sửa.

### 3.5. `KitchenInspection.type: "Định kỳ tháng" | "Đột xuất"` — ĐÃ GIẢI QUYẾT (05/07/2026): chính thức hóa thành BR-208

Code đã triển khai phân loại kiểu kiểm tra "Đột xuất" (surprise) và "Định kỳ tháng" (monthly scheduled) trước khi có căn cứ BR/UC. Đội dự án đã xác nhận và tạm chốt: cấp mã mới **BR-208** (thay vì gán vào BR-101 sẵn có, tránh lẫn với "kiểm tra hàng tháng" cụ thể mà BR-101 đang mô tả) — đã merge vào `business_rules.md` §12 và Screen Spec `12_ConductFoodSafetyInspectionScreen.md` (Web+Mobile, field 1b). Lưu ý: **chưa tìm được căn cứ pháp luật** cụ thể cho việc phân loại này trong `00_Policies/` — đây là quyết định vận hành nội bộ dự án/nhà trường, đã ghi rõ trong `business_rules.md`. Việc còn lại: cập nhật UI tạo phiên mới trong `conduct-inspection/page.tsx` để có bước chọn tường minh "Định kỳ / Đột xuất" đúng field 1b (hiện `type` đã có trong data model nhưng chưa chắc đã có UI chọn khi tạo phiên — cần kiểm tra lại khi lập trình).

### 3.6. Class Diagram — trích dẫn BR lỗi thời tại §2.3 (Student & Health), ảnh hưởng trực tiếp AllergyProfile/StudentHealthRecord mà Medical Staff thao tác

Đối chiếu `class_diagram_description.md` dòng 1186 ("BR-033 → BR-043") và dòng 1191 ("BR-039") với `business_rules.md`: **BR-033 đến BR-039 và BR-042/BR-043 không tồn tại** trong `business_rules.md` (numbering nhảy thẳng BR-032 → BR-040, đã xác nhận bằng grep trực tiếp). Đây là trích dẫn hoàn toàn không có căn cứ (ghost-citation toàn dải, không phải lỗi đánh máy 1 số).

Đối chiếu với code thực tế: `health-reviews/page.tsx` dùng đúng **BR-031** (xét duyệt) + MSG-036; `StudentDistribution.allergyWarning` dùng đúng **BR-123** (cảnh báo trùng thực đơn); `health-records/[id]/page.tsx` (Clinical Annotation, append-only) dùng đúng **BR-172** — không phải BR-039 như Class Diagram ghi. Nghĩa là **code và business_rules.md nhất quán với nhau**, chỉ riêng Class Diagram §2.3 bị lệch — khả năng cao đoạn này chưa được cập nhật sau một đợt đánh số lại BR trước đây.

**Đề xuất**: cập nhật `class_diagram_description.md` §2.3 — dòng 1186 sửa "BR-033 → BR-043" thành **BR-031, BR-123**; dòng 1191 sửa "BR-039" thành **BR-172**. Đây là sửa cho khớp thực tế đã triển khai, không phải thêm nghiệp vụ mới — nhưng vẫn cần đội dự án xác nhận trước khi sửa file diagram chính thức (theo đúng nguyên tắc "flag, không tự sửa").

*Ghi chú phạm vi*: bảng BR-to-Class mapping (mục 4 của Class Diagram) còn có `InventoryItem → BR-081/082/083` — đã được `business_rules.md` renumber thành BR-202/204/205 (ghi rõ trong changelog của chính file BR). Mục này thuộc phạm vi Inventory/Service Caterers (ngoài Medical Staff), nêu ở đây để đội dự án tiện xử lý cùng đợt rà soát Class Diagram, không thuộc phạm vi sửa của review này.

### 3.7. Bổ sung 05/07/2026 — 4 gap do người dùng phát hiện qua walkthrough trực tiếp, đã đối chiếu BR/Screen Spec, **xác nhận hợp lệ, đúng scope**

Người dùng (chủ trì Medical Staff module) đã tự rà lại 5 màn hình và đặt câu hỏi trực tiếp; đối chiếu từng điểm với `business_rules.md` và Screen Spec tương ứng, cả 4 điểm dưới đây đều **có căn cứ rõ ràng, không phát sinh nghiệp vụ ngoài phạm vi UC/BR hiện có** — được xác nhận đưa vào kế hoạch triển khai (Mục 4, mục #11–#15).

**a) Duyệt khai báo sức khỏe (`health-reviews/page.tsx`, UC-077) — thiếu Textarea bắt buộc khi "Cần bổ sung" + panel xem tài liệu trước khi Xác minh.**
Căn cứ `02_ReviewHealthInformationScreen.md`: field #4 ("Chọn 1 khai báo → xem chi tiết" — mở panel chứa loại dị ứng, mức độ, tài liệu y tế đính kèm), field #5 (tài liệu y tế bắt buộc khi mức độ Severe), field #6 (nút Approve **chỉ bật khi tài liệu hợp lệ**), field #7–8 (nút Reject bắt buộc kèm Textarea lý do). Toàn bộ đều dẫn chiếu BR-031. Code hiện tại (`ReviewActions`) không có panel chi tiết, không có Textarea, nút Xác minh không kiểm tra tài liệu — đúng là thiếu so với spec, không phải suy diễn thêm.

**b) Hồ sơ kiểm thực (`inspections/page.tsx`, UC-112) — thiếu filter (ngày/bước/món) + click dòng xem chi tiết.**
Căn cứ `11_FoodInspectionRecords.md`: field #1 (filter khoảng ngày), #2 (filter bước 1/2/3, BR-079), #3 (filter thực đơn/món), #6 (chọn 1 dòng → xem ảnh bằng chứng, timestamp, GPS, ghi chú nhân viên). Code hiện tại chỉ có ô search chung, không có filter cấu trúc, không có `rowHref`/tương tác chi tiết — đúng là thiếu.

**c) Sổ lưu mẫu (`samples/page.tsx`, UC-118) — thiếu filter theo ngày/bữa/món/trạng thái, giữ nguyên bảng phẳng.**
Căn cứ `13_FoodSampleRecords.md`: field #1 yêu cầu rõ "Filter theo khoảng ngày/bữa/món/trạng thái"; field #2 yêu cầu bảng giữ **đủ 12 cột theo Mẫu số 5** (biểu mẫu lưu mẫu chính thức theo QĐ 1246/QĐ-BYT) — nghĩa là spec gốc **đã định sẵn bảng phẳng**, không phân cấp theo ngày. Quyết định giữ bảng phẳng + thêm filter (thay vì phân cấp điều hướng theo ngày như đề xuất ban đầu của người dùng) khớp đúng tinh thần Mẫu số 5, đã được người dùng xác nhận. BR-111 (72 giờ lưu mẫu tự động, không hủy sớm) không bị ảnh hưởng bởi thay đổi filter này.

**d) Sổ vi phạm & xử lý (`violations/page.tsx`, UC-181) — thiếu nút xem chi tiết biên bản.**
Căn cứ `27_ViolationRegistry.md`: field #7 "Chọn 1 dòng → xem chi tiết". Code hiện tại không có `rowHref`/dialog xem lại toàn bộ nội dung biên bản đã lập (mô tả, nhân chứng, ảnh, `staffPosition`...) sau khi đã submit — chỉ có nút "Lập xử lý" khi còn OPEN. Đây là thiếu sót thật, cần bổ sung.
**Phát hiện thêm cùng lúc (chưa nằm trong câu hỏi gốc của người dùng nhưng cùng file spec)**: `27_ViolationRegistry.md` field #1–3 còn yêu cầu filter theo loại vi phạm (quy trình/nhân viên/đối tác, BR-189), mức độ (LOW/MEDIUM/HIGH, BR-186), và trạng thái (Pending/Approved/Closed, BR-186) — code hiện tại cũng chỉ có ô search chung, thiếu 3 filter này. Đề xuất gộp chung vào cùng đợt lập trình với mục (d).
**Không đổi**: Update/Delete cho biên bản đã lập **cố ý không có** — BR-188 yêu cầu biên bản vi phạm/xử lý phải ghi vào audit log bất biến, không được sửa/xóa sau khi tạo. Đây là thiết kế đúng, không phải thiếu sót.
**Xác nhận thêm qua rà `00_Policies/`**: không có mẫu biên bản chính thức nào của Nhà nước áp dụng cho vi phạm nội bộ nhân viên bếp/catering (đã kiểm tra toàn bộ 14 Mẫu số của NĐ 15/2018/NĐ-CP và 6 Mẫu số của QĐ 1246/QĐ-BYT — không mẫu nào liên quan). Các trường bắt buộc của biên bản này hoàn toàn do BR-183/184/185 (nội bộ dự án) quy định, không dẫn chiếu văn bản pháp luật nào.

### 3.8. Descope 05/07/2026 — bỏ cơ chế "điểm <60% → khóa vận hành bếp"

Đội dự án quyết định loại bỏ hoàn toàn hậu quả "khóa vận hành bếp, chặn phân phát bữa ăn" mà `Report 3.md` UC-014 AF1 gốc mô tả (dòng 3455), sau khi phát hiện 2 vấn đề: (1) không có căn cứ pháp lý/quy định nội bộ nào cho ngưỡng 60% (đã xác nhận qua rà `00_Policies/`, xem Mục 3.1/US-08 cũ); (2) `MSG-024`/`BR-105` mà UC-014 gốc mượn cho cơ chế này thực chất thuộc phạm vi **Incident Management** — theo bảng phân loại module của chính `error_message.md` ("Incident Management | MSG-023, MSG-024, MSG-048, MSG-049"), MSG-024 đúng ra dùng cho `BR-149` (tự động cách ly **lô nguyên liệu cụ thể** khi có sự cố ATTP thực sự xảy ra, đã cài đúng trong `incidents/[id]/page.tsx`), không phải cho một ngưỡng điểm checklist định kỳ. SRS gốc đã mượn nhầm cơ chế của module khác.

**Quyết định (người dùng, 05/07/2026)**: đây là **descope có chủ đích**, không phải sửa lỗi trích dẫn đơn thuần. Hành vi mới: điểm <60% → hệ thống **tự động gửi cảnh báo tới School Administration** (bỏ Service Caterers/School Board so với AF1 gốc — không còn là tình huống khẩn cấp cấp-sự-cố), nhưng **bếp tiếp tục vận hành bình thường, không chặn phân phát bữa ăn**. Kết quả Pass/Failed vẫn ghi nhận đủ (BR-104). BR-149 (cách ly lô nguyên liệu khi có sự cố) **giữ nguyên, không thay đổi**.

Đã cập nhật đồng bộ: `business_rules.md` §12 (ghi chú sau BR-208), `MedicalStaff_UserStory.md` (US-08, đổi nhãn thành `[DECIDED — descope so với SRS gốc]`), Screen Spec 12 (Web+Mobile), `23_ComplianceDashboard.md` (Web+Mobile MedicalStaff, field #6 cũ → field #4b mới), `Web/SchoolAdministration/18_ComplianceDashboard.md` (field #5 cũ → field #4b mới). **Mã MSG chính thức đã cấp 05/07/2026: `MSG-093`** (Warning, không tái dùng MSG-024) — xem `error_message.md` §3. **Chưa sửa code** — xem Mục 4 (#16).

### 3.9. School Admin chưa có route Compliance Dashboard thật — US-09 đã duyệt (UC-185/BR-209)

Khi kiểm tra khả năng "School Admin tự xem báo cáo" (mô hình pull mà US-09 trong `MedicalStaff_UserStory.md` giả định đang tồn tại), phát hiện: `Web/SchoolAdministration/18_ComplianceDashboard.md` có đặc tả đầy đủ (bao gồm nút "Ký xác nhận trạng thái an toàn hôm nay", trạng thái 3-bước realtime, banner cảnh báo) nhưng **không có route nào tên `compliance` trong `school-admin/` ở frontend**. Route gần nhất, `school-admin/reports/page.tsx`, là một màn **khác về bản chất** — báo cáo tổng hợp theo kỳ (refetch 5 phút, BR-180), không có trạng thái realtime hôm nay, không có nút Ký xác nhận. `school-admin/dashboard/page.tsx` cũng không có gì liên quan.

**Quyết định (người dùng, 05/07/2026)**: đã duyệt hướng (b) — bổ sung tính năng **"Send Compliance Report" (UC-185, BR-209)** để Medical Staff chủ động soạn/gửi báo cáo Daily/Weekly cho School Administration, song song (không thay thế) mô hình pull hiện có (UC-027/UC-054). US-09 đổi nhãn thành `[DECIDED — duyệt 05/07/2026]`.

**Đã triển khai tài liệu đầy đủ**: `business_rules.md` §12 (BR-209, sau ghi chú descope), `traceability_matrix.md` (hàng UC-185 ở §1, hàng BR-209 ở §2, gộp UC-185/BR-209/MSG-091-093 vào module "Food Safety Inspection & Monitoring" ở §4 — không tạo module mới, theo đúng nguyên tắc đã dùng cho Violation Management 2026-07-01), `error_message.md` (MSG-091 Success §2, MSG-092 Error §1, MSG-093 Warning §3, cập nhật bảng module §4), `MedicalStaff_UserStory.md` (US-09 → DECIDED), Screen Spec mới `28_SendComplianceReportDialog.md` (Web+Mobile, cả 2 file lẻ và 2 file `00_TongHop_MedicalStaff.md`), 2 ScreenFlow Medical Staff (Web+Mobile — thêm nhánh từ Compliance Dashboard), `23_ComplianceDashboard.md` (Web+Mobile) và `Web/SchoolAdministration/18_ComplianceDashboard.md` (ghi chú trỏ sang tính năng mới). **Chưa sửa code** — xem Mục 4 (#19).

---

## 4. Kế hoạch chỉnh sửa (đề xuất thứ tự ưu tiên)

| # | Việc cần làm | Vị trí | Điều kiện tiên quyết | Ưu tiên | Trạng thái |
| --- | --- | --- | --- | --- | --- |
| 1 | ~~Duyệt chính thức BR-206/BR-207~~ → **BR-207 đã chốt & merge; BR-206 đã bị loại bỏ** | `business_rules.md` §12 | — | — | ✅ Xong 05/07/2026 |
| 2 | Cập nhật `conduct-inspection/page.tsx`: ảnh + lý do theo từng hạng mục, icon "ℹ" chú thích, bỏ trích dẫn `BR-105` (không thay bằng mã BR nào — trích thẳng UC-014 AF1), ~~thêm bước chọn "Loại kiểm tra" (BR-208)~~ | Frontend code | Không còn — BR-207 đã chốt | Cao | ✅ Xong 05/07/2026 (stepper + per-item ảnh/lý do/ℹ; trích dẫn UC-014 AF1 sau đó thay bằng MSG-093 theo descope #16). **09/07/2026**: BR-208 deprecated — bước chọn "Loại kiểm tra" nếu đã code thì cần **gỡ bỏ** khỏi `conduct-inspection/page.tsx` (chưa xử lý, loại trừ theo yêu cầu người dùng lần này) |
| 3 | Xác nhận mã MSG cho lỗi "thiếu ảnh/lý do tại hạng mục" (tái dùng MSG-085 hay cấp mới) | `error_message.md` | Không có | Cao | ✅ Quyết định 05/07/2026: cảnh báo inline không gắn mã MSG (không tái dùng MSG-085, không tự đặt mã mới) — code đã làm theo; nếu sau này đội dự án cấp mã chính thức thì bổ sung vào `error_message.md` và code |
| 4 | Làm rõ UC-027 vs UC-054: sửa ScreenFlow hoặc bổ sung widget nhiệt độ kho/pest cert vào `compliance/page.tsx` | ScreenFlow + Screen Spec 23 + code | Quyết định của đội dự án (§3.2) | Trung bình | ✅ Code xong 05/07/2026 (quyết định người dùng: gộp widget UC-054 — nhiệt độ BR-089/110 + MSG-055/059, chứng nhận BR-102/103 — vào `compliance/page.tsx`, giữ UC-027; xác nhận lại "giữ widget" 06/07/2026) — phần docs còn lại theo dõi ở mục #20(b) |
| 5 | Bổ sung mục Announcements (UC-125) vào cả 2 ScreenFlow Medical Staff | `03_Medical_Staff_Screen_Flow.md`, `08_..._Web.md` | Không có | Trung bình | ⏳ Chưa xử lý |
| 6 | ~~Quyết định hướng xử lý BR-110 dùng chéo module~~ → đã khôi phục BR-110 + ghi chú kiểm tra chéo | Screen Spec 12 (Web+Mobile) | — | — | ✅ Xong 05/07/2026 |
| 7 | ~~Bổ sung căn cứ nghiệp vụ cho "Loại kiểm tra"~~ → **BR-208 đã chốt & merge (05/07/2026)** → **đã Deprecated (09/07/2026)**, xem dòng #9 | `business_rules.md` §12, Screen Spec 12 | — | — | ✅ Xong 05/07/2026; đảo ngược 09/07/2026 |
| 8 | Sửa trích dẫn BR lỗi thời tại Class Diagram §2.3 (BR-033→043, BR-039) | `class_diagram_description.md` | Xác nhận với đội dự án | Trung bình | ⏳ Chưa xử lý |
| 9 | ~~Lan truyền BR-207/BR-208 (và bỏ trích dẫn BR-105/BR-206) sang `23_ComplianceDashboard.md` (Web+Mobile), `Web/SchoolAdministration/18_ComplianceDashboard.md`, `conduct-inspection/page.tsx`, `compliance/page.tsx`, và 2 file `00_TongHop_MedicalStaff.md`~~ → **09/07/2026: BR-208 deprecated, không còn cần lan truyền field "Loại kiểm tra"**; đã xác nhận 4 file spec liệt kê không có field liên quan BR-208. Còn lại: gỡ trích dẫn BR-105/BR-206 khỏi code (`conduct-inspection/page.tsx`, `compliance/page.tsx`) | Screen Spec + TongHop + code | Không có | Trung bình | ✅ Spec xong 09/07/2026; code ⏳ chưa xử lý (loại trừ theo yêu cầu người dùng) |
| 10 | Sửa `MedicalStaff_UserStory.md`: mục "Phụ lục A" được nhắc tới ở US-06/07/08 nhưng **không tồn tại trong file** (file bị cắt cụt ở dòng 140, giữa câu) — cần bổ sung để tài liệu tự nhất quán | `02_Requirements/MedicalStaff_UserStory.md` | Xác nhận với đội dự án (phát hiện mới, ngoài yêu cầu ban đầu) | Cao (lỗi toàn vẹn dữ liệu) | ⏳ Chưa xử lý |
| 11 | ~~Đưa "Bảng giám sát tuân thủ" lên đầu nhóm "Kiểm tra & Giám sát ATTP"~~ | `roles.ts`, cả 2 ScreenFlow | — | — | ✅ Xong 05/07/2026 (quyết định người dùng) |
| 12 | `health-reviews/page.tsx`: thêm Textarea bắt buộc khi bấm "Cần bổ sung" (Reject, field #7–8, BR-031) + panel/Sheet xem tài liệu y tế đính kèm trước khi cho phép "Xác minh" (field #4–6, BR-031) | Frontend code (§3.7a) | Không có | Cao | ✅ Xong 06/07/2026 (dialog chi tiết: dị ứng/mức độ/tài liệu; Approve khóa khi Severe thiếu tài liệu; Reject bắt buộc Textarea lý do — gộp xử lý luôn mục #24) |
| 13 | `inspections/page.tsx`: thêm filter ngày/bước/món (field #1–3, BR-079) + click dòng xem chi tiết ảnh/GPS/timestamp/ghi chú (field #6) | Frontend code (§3.7b) | Không có | Trung bình | ✅ Xong 06/07/2026 (filter khoảng ngày + bước/trạng thái bước + món; dialog chi tiết 3 bước/ảnh/GPS/ghi chú; mock-server hỗ trợ dateFrom/dateTo/step/stepStatus; `InspectionSession` thêm dish/evidencePhotos/gps/staffNotes) |
| 14 | `samples/page.tsx`: thêm filter ngày/bữa/món/trạng thái (field #1, BR-107/108/110/111/114), giữ nguyên bảng phẳng 12 cột Mẫu số 5 | Frontend code (§3.7c) | Không có | Trung bình | ✅ Xong 06/07/2026 (filter khoảng ngày theo collectedAt + bữa Trưa/Phụ + món + trạng thái; bảng phẳng giữ nguyên) |
| 15 | `violations/page.tsx`: thêm nút/đường dẫn xem chi tiết biên bản (field #7, `27_ViolationRegistry.md`) + filter loại/mức độ/trạng thái (field #1–3, BR-186/189) | Frontend code (§3.7d) | Không có | Trung bình | ✅ Xong 06/07/2026 (nút "Chi tiết" mở dialog read-only toàn bộ biên bản — BR-188 bất biến, kèm badge BR-211 + xuất PDF; 3 filter loại/mức độ/trạng thái xử lý) |
| 16 | Sửa `conduct-inspection/page.tsx` + `compliance/page.tsx`: bỏ toàn bộ logic khóa vận hành bếp/chặn phân phát bữa ăn, bỏ trích dẫn `BR-105`/`MSG-024`; đổi Alert/toast "Kitchen quarantine is active: Operation blocked" thành cảnh báo warning không chặn, tự động gửi cảnh báo School Administration | Frontend code (§3.8) | Không có — đã chốt 05/07/2026 | Cao | ✅ Xong 05/07/2026 (code: 2 page + `mock-data.ts` bỏ `quarantineTriggered` + `mock-server.ts`; dùng MSG-093) |
| 17 | ~~Cấp mã MSG mới cho "cảnh báo điểm checklist <60%, không khóa bếp"~~ → **MSG-093 đã cấp & merge** | `error_message.md` §3 | — | — | ✅ Xong 05/07/2026 |
| 18 | ~~Quyết định US-09~~ → **Đã duyệt hướng (b)**: bổ sung UC-185/BR-209 "Send Compliance Report", song song với UC-027/UC-054 | `MedicalStaff_UserStory.md` US-09 | — | — | ✅ Xong 05/07/2026 |
| 19 | Lập trình `28_SendComplianceReportDialog.md` (UC-185): thêm route/dialog gửi báo cáo Daily/Weekly, gọi API tổng hợp `KitchenInspection` trong kỳ, export BR-178, gửi thông báo BR-158/159, chặn khi rỗng (MSG-092), toast thành công (MSG-091) | Frontend code (`compliance/page.tsx` hoặc route mới), Screen Spec 28 (§3.9) | Không có — đã chốt 05/07/2026 | Trung bình | ✅ Xong 05/07/2026 (component `send-compliance-report-dialog.tsx`, nút mở từ `compliance/page.tsx`, resource mock `compliance-reports`) |
| 20 | Cập nhật docs theo 2 quyết định code 05/07/2026: ~~(a) Screen Spec 12 (Web+Mobile) còn ghi "mã MSG chưa xác định" — điền `MSG-093`~~ **→ Đã xong (audit toàn diện §6, 05/07/2026)**; (b) Screen Spec 23 (Web+Mobile) chưa phản ánh 4 widget UC-054 (nhiệt độ kho/tủ lưu mẫu BR-089/110 + chứng nhận BR-102/103) mà người dùng quyết định GIỮ trong `compliance/page.tsx` — bổ sung field hoặc ghi chú gộp phạm vi UC-054, tránh trạng thái "code đi trước docs" | Screen Spec 23 (Web+Mobile) | Quyết định giữ widget 05/07/2026 | Trung bình | ⏳ Chưa xử lý — còn lại (b) |
| 21 | **[Chỉ tài liệu — người dùng xác nhận 06/07/2026]** `incidents/[id]/investigation/page.tsx` không gọi `useCreate`/`useUpdate` nào — 4 panel (Extract Sample/Gathering Evidence/Root Cause/Treatment Measures) chỉ dùng `useState` nội bộ + toast; rời trang/F5 mất toàn bộ dữ liệu; "Establish Measures" không tạo `CorrectiveAction` thật (thẻ "CAPA liên quan" ở Incident Detail không đổi) | Frontend code (§7.1a) | Không có | Cao | ✅ Xong 06/07/2026 (4 panel persist qua `useUpdate("incidents")` — field mới `investigationEvidence/LabResult/RootCause`; Establish Measures tạo CAPA thật qua `useCreate("corrective-actions")`, source gắn incident.id, hạn +12h BR-152) |
| 22 | **[Chỉ tài liệu — người dùng xác nhận 06/07/2026]** Nút "Validate CAPA đã duyệt (UC-168)" bật theo `relatedCapa.length > 0`, không kiểm tra trạng thái phê duyệt; `CorrectiveAction.status` (`mock-data.ts`) không có giá trị nào đại diện "đã được School Admin duyệt" (chỉ ASSIGNED/SUBMITTED/IN_PROGRESS/COMPLETED/OVERDUE) — thiếu field trong data model, không chỉ thiếu điều kiện `if`. Phạm vi: **chỉ Medical Staff** — chưa kiểm tra màn Approve/Reject Corrective Action phía School Administration (theo quyết định người dùng 06/07/2026) | Frontend code + `mock-data.ts` (§7.1b) | ~~Cần xác nhận giá trị trạng thái~~ → **người dùng chốt `APPROVED` 06/07/2026** | Cao | ✅ Xong 06/07/2026 (giá trị `APPROVED` ghi vào chú thích `CorrectiveAction.status`; CAPA-2026-032 mock → APPROVED; nút Validate chỉ bật khi có CAPA APPROVED, dialog chỉ liệt kê CAPA đã duyệt) |
| 23 | **[Chỉ tài liệu — người dùng xác nhận 06/07/2026]** Root cause bị nhập 2 lần độc lập cho cùng 1 sự cố: Investigation Workspace (panel 3) và dialog "Lập báo cáo sự cố" (Step 3, UC-033) — không pre-fill giữa 2 nơi, rủi ro 2 kết luận khác nhau | Frontend code (§7.1c) — phụ thuộc mục #21 (cần lưu dữ liệu thật trước khi pre-fill được) | Không có | Trung bình | ✅ Xong 06/07/2026 (dialog UC-033 Step 3 pre-fill root cause + lab result từ `investigationRootCause/LabResult` đã lưu ở UC-041; kèm dòng trạng thái nguồn pre-fill) |
| 24 | **[Chỉ tài liệu — người dùng xác nhận 06/07/2026]** `health-reviews/page.tsx`: nút "Cần bổ sung" không có field lý do — chỉ đổi trạng thái + toast, phụ huynh không biết cần bổ sung gì | Frontend code (§7.2d) | Không có | Cao | ✅ Xong 06/07/2026 — gộp vào mục #12 (Reject bắt buộc Textarea lý do, lưu `reviewNote`) |
| 25 | **[Chỉ tài liệu — người dùng xác nhận 06/07/2026]** `samples/page.tsx` dialog Retrieve: dropdown Incident ID cho chọn cả sự cố đã đóng, chỉ chặn sau khi Submit — nên lọc/disable option đã đóng ngay tại dropdown | Frontend code (§7.2e) | Không có | Trung bình | ✅ Xong 06/07/2026 (option sự cố CLOSED bị disable ngay tại dropdown kèm nhãn "ĐÃ ĐÓNG — không thể liên kết"; guard MSG-023 khi Submit giữ nguyên làm lớp bảo vệ thứ hai) |
| 26 | **[Quyết định 06/07/2026]** `health-records/[id]/page.tsx` dialog "Cập nhật hồ sơ": đổi field dị ứng từ Input text tự do (phân tách dấu phẩy) sang chọn từ danh mục dị ứng chuẩn (tag/multi-select), theo đúng pattern `specialDiet`/BR-066 | Frontend code (§7.2f) | **Chặn**: `business_rules.md` hiện chưa định nghĩa danh mục dị ứng chuẩn nào (BR-031 không liệt kê danh mục) — cần đội dự án/cố vấn y tế xác nhận danh mục trước khi lập trình tag-picker | Cao (an toàn dữ liệu dị ứng) | ⏳ Chờ đội dự án cấp danh mục dị ứng chuẩn |
| 27 | Ghi nhận 6 điểm UI/kiến trúc thông tin (không chặn vận hành, ưu tiên thấp hơn): (g) sidebar 4 nhóm chỉ 1 mục nên gộp; (h) dữ liệu KitchenInspection lặp ở 3 màn (compliance/conduct-inspection/reports); (i) tên "Hồ sơ kiểm thực" (UC-079) dễ nhầm với "Kiểm tra an toàn bếp" (UC-014); (j) dashboard shortcut thiếu "Kiểm tra an toàn bếp" (hành động chính) nhưng có "Kết quả kiểm thực 3 bước" (chỉ xem); (k) ảnh minh chứng chỉ là nút đếm, không preview/xóa; (l) nhãn nút trộn Anh/Việt không nhất quán | `roles.ts` (sidebar), `dashboard/page.tsx`, `conduct-inspection/page.tsx`, `violations/page.tsx`, nhiều dialog (§7.3) | Không có | Thấp–Trung bình | ⏳ Chờ đội dự án ưu tiên |
| 28 | **[Quyết định 06/07/2026 — chỉ phạm vi Medical Staff]** Tăng chiều cao Input/Select (hiện `h-9`) lên `h-11`, Textarea (hiện `min-h-[80px]`) lên `min-h-[110px]`; tăng khoảng cách Label→Input (hiện `space-y-1.5`) lên `space-y-2`, khoảng cách giữa field (hiện `space-y-4`) lên `space-y-5`/`space-y-6` — áp dụng qua `className` override riêng từng file `medical-staff/**/*.tsx`, **không sửa** `components/ui/input.tsx` dùng chung (tránh lệch style role khác) | Tất cả 18 file `medical-staff/**/*.tsx` có dùng Input/Select/Textarea (§7.4) | Đội dự án/thiết kế xác nhận giá trị cụ thể trước khi áp dụng đại trà | Trung bình | ⏳ Chờ lập trình |
| 29 | **[Quyết định người dùng 06/07/2026]** Lập trình màn "Quản lý Checklist" (UC-186, BR-210): Medical Staff tự CRUD nhóm/hạng mục checklist bếp + chú thích căn cứ pháp lý (icon "ℹ"), không qua System Admin, không cần phê duyệt; xóa hạng mục đang được tham chiếu → cảnh báo xác nhận (MSG-094) nhưng vẫn cho xóa, giữ nguyên dữ liệu lịch sử. Cần nạp 6 nhóm mặc định từ `MedicalStaff_KitchenInspectionChecklist_Proposal.md` làm dữ liệu khởi tạo | `conduct-inspection/page.tsx` (route/dialog mới), Screen Spec `12_ConductFoodSafetyInspectionScreen.md` (đã cập nhật, §8.1) | Không có — BR-207/BR-210 đã chốt 06/07/2026 | Cao | ✅ Xong 06/07/2026 (route mới `conduct-inspection/checklist/page.tsx` — CRUD 2 cột + MSG-094; seed 6 nhóm/31 mục trong `mock-data.ts` giữ nguyên đánh dấu `[cần xác nhận thêm]`; `conduct-inspection/page.tsx` chuyển sang checklist động, nút field 2e; sắp xếp bằng nút lên/xuống — bản thật thay bằng kéo-thả theo spec) |
| 30 | **[Quyết định người dùng 06/07/2026]** Lập trình xuất Biên bản Vi phạm chuẩn (PDF, theo mẫu `00_Policies/specialreport2173_1.md`) tại `violations/page.tsx` (UC-178/179) + gate bắt buộc tải ảnh biên bản đã ký 3 bên (`signedRecordPhotoUrl`) trước khi cho phép "Đóng biên bản"/"Gửi School Administration phê duyệt" (BR-211, MSG-095 Error). Cần thư viện sinh PDF phía client hoặc server (chưa chọn — cân nhắc nhẹ cho hệ thống theo đúng yêu cầu người dùng) | `violations/page.tsx`, dialog Record Violation Resolution, Screen Spec 21/22/24 (Web+Mobile, đã cập nhật, §8.2) | Không có — BR-211 đã chốt 06/07/2026; tên chính thức văn bản ("BIÊN BẢN SỰ VIỆC" hay "BIÊN BẢN VI PHẠM") cần đội dự án xác nhận trước khi in mẫu chính thức | Cao (yêu cầu người dùng, ảnh hưởng pháp lý bằng chứng vi phạm) | ✅ Xong 06/07/2026 (mock: cửa sổ in trình duyệt theo mẫu `specialreport2173_1.md` — Print → Save as PDF, nhẹ, không thêm thư viện; bản thật sinh PDF server-side theo BR-178. Field 2a/2b/2c + witness UC-179 vào 2 dialog; gate chặn cứng MSG-095 tại nút Đóng/Gửi duyệt; badge 3 trạng thái biên bản ở bảng + dialog. Tiêu đề PDF tạm dùng "BIÊN BẢN SỰ VIỆC" theo mẫu gốc, có ghi chú chờ duyệt trong code) |

---

## 5. Kết luận

Frontend Medical Staff đã triển khai **13/16 màn hình khớp tốt** với Screen Spec và ScreenFlow, field/input đúng bối cảnh nghiệp vụ, không phát hiện tính năng bịa đặt nghiêm trọng.

**Cập nhật 05/07/2026 — các quyết định đã chốt**: đội dự án đã (1) tạm chốt **BR-207** (bằng chứng theo từng hạng mục + chú thích căn cứ pháp lý) và merge chính thức vào `business_rules.md` §12; (2) **loại bỏ đề xuất BR-206** (ngưỡng <60% cách ly bếp) do không tìm được căn cứ pháp lý/quy định nội bộ nào ngoài chính `Report 3.md` UC-014 — ngưỡng này vẫn giữ nguyên về chức năng nhưng không gắn mã BR; (3) tạm chốt **BR-208** mới cho field "Loại kiểm tra: Định kỳ/Đột xuất"; (4) khôi phục **BR-110** vào Screen Spec 12 với ghi chú rõ đây là kiểm tra chéo. Cả 3 Screen Spec/BR liên quan (`business_rules.md`, Screen Spec 12 Web+Mobile) đã được cập nhật đồng bộ.

**Còn tồn đọng, ưu tiên xử lý tiếp** (xem Mục 4): (a) lập trình lại `conduct-inspection/page.tsx` theo BR-207/BR-208 đã chốt — hiện code vẫn ở phiên bản cũ (mục #2, #9); (b) lệch phạm vi UC-027/UC-054 ở Compliance Dashboard (mục #4); (c) trích dẫn BR lỗi thời trong Class Diagram §2.3 (mục #8); (d) **phát hiện mới**: `MedicalStaff_UserStory.md` bị cắt cụt và thiếu hẳn mục "Phụ lục A" mà chính nó tự trích dẫn nhiều lần (US-06/07/08) — cần đội dự án xác nhận trước khi bổ sung (mục #10).

**Cập nhật 05/07/2026 (bổ sung sau walkthrough trực tiếp với người dùng)**: đã xác nhận và chốt thứ tự menu "Bảng giám sát tuân thủ" lên đầu nhóm Kiểm tra & Giám sát ATTP (mục #11, đã sửa xong). Ngoài ra, 4 gap mới được người dùng phát hiện qua rà soát thủ công — Duyệt khai báo sức khỏe (thiếu Textarea lý do + panel tài liệu), Hồ sơ kiểm thực (thiếu filter + xem chi tiết), Sổ lưu mẫu (thiếu filter, giữ bảng phẳng Mẫu số 5), Sổ vi phạm & xử lý (thiếu xem chi tiết + filter) — đã được đối chiếu kỹ với `business_rules.md` và Screen Spec tương ứng (xem Mục 3.7), **xác nhận cả 4 đều đúng scope, có căn cứ BR/UC rõ ràng, không phát sinh nghiệp vụ ngoài phạm vi hiện có**. Đã đưa vào kế hoạch triển khai (mục #12–#15), sẵn sàng để lập trình.

**Cập nhật 05/07/2026 (đợt 3) — báo cáo tổng hợp gửi School Admin**: rà soát toàn bộ 3 loại báo cáo Medical Staff có thể gửi School Administration: (1) Báo cáo sự cố (UC-033) — đã hoàn chỉnh, cơ chế push thật; (2) Báo cáo vi phạm — chỉ push từng vụ riêng lẻ khi MEDIUM/HIGH (UC-182), không có báo cáo tổng hợp định kỳ (không có trong SRS, không phải thiếu sót); (3) Báo cáo kiểm tra bếp — thuần túy pull (UC-027), và phát hiện thêm là **phía School Admin còn chưa có route Compliance Dashboard thật** để thực hiện việc pull này (Mục 3.9) — nghĩa là mô hình pull hiện tại chưa đủ điều kiện hoạt động. Đã khuyến nghị bổ sung tính năng gửi chủ động (US-09, hướng b) nhưng **chờ người dùng xác nhận** (mục #18). Đồng thời, theo yêu cầu người dùng, đã **descope hoàn toàn cơ chế "điểm <60% → khóa vận hành bếp"** khỏi UC-014 (Mục 3.8) — giữ lại cảnh báo tự động tới School Administration nhưng bếp không còn bị khóa; đã cập nhật đồng bộ 7 file tài liệu (`business_rules.md`, `MedicalStaff_UserStory.md`, Screen Spec 12 Web+Mobile, `23_ComplianceDashboard.md` Web+Mobile, `18_ComplianceDashboard.md` School Admin), code chưa sửa (mục #16–17).

Không có thay đổi nào được tự động áp dụng vào code — chỉ cập nhật tài liệu (BR/UC/Screen Spec) và điều hướng menu (mục #11) theo đúng phạm vi được yêu cầu và xác nhận bởi người dùng.

**Cập nhật 05/07/2026 (đợt 4) — US-09 duyệt & triển khai tài liệu đầy đủ**: người dùng đã duyệt US-09 hướng (b). Đã cấp mã chính thức **UC-185** (Send Compliance Report), **BR-209**, **MSG-091/092/093** và cập nhật đồng bộ toàn bộ 10 tài liệu liên quan (xem chi tiết Mục 3.9) — bao gồm cả việc gộp MSG-093 vào module "Food Safety Inspection" thay vì tạo module mới trong `error_message.md`, để nhất quán với nguyên tắc "không tạo module mới cho tính năng mở rộng" đã áp dụng cho Violation Management trước đó (đã phát hiện và sửa 1 điểm không nhất quán giữa `error_message.md` và `traceability_matrix.md` trong lúc thực hiện — ban đầu `error_message.md` tạo module riêng "Compliance Reporting", đã gộp lại cho khớp). Còn lại duy nhất việc lập trình frontend (mục #19), chưa đụng tới code theo đúng yêu cầu người dùng.

---

## 6. Audit vận hành toàn diện (05/07/2026, đợt 5) — toàn bộ Screen Spec Web+Mobile MedicalStaff + `business_rules.md`

**Phạm vi**: đọc lại toàn bộ 28 file Screen Spec Web/MedicalStaff, đối chiếu chéo với Mobile/MedicalStaff (spot-check các file có khả năng lệch cao: 08, 17, 19, 26 — đọc đầy đủ; các file còn lại đối chiếu cấu trúc, không phát hiện lệch giữa 2 nền tảng ngoài khác biệt layout đã ghi nhận theo đúng quy ước dự án), cùng `business_rules.md` §11–§12 (Incident Management, Food Safety Monitoring). Mục tiêu: (a) vận hành thực tế có trơn tru không, (b) còn nghịch lý/lệch trích dẫn nào không, (c) đúng phạm vi/bối cảnh bài toán chưa.

### 6.1. Lỗi phát hiện và đã sửa trong đợt audit này

1. **BR-208 tự mâu thuẫn với chính quyết định descope**: câu mô tả gốc "không làm thay đổi... ngưỡng **cách ly bếp**" vẫn dùng thuật ngữ "cách ly bếp" dù cơ chế này đã bị loại bỏ hoàn toàn trong ghi chú ngay bên dưới. Đã sửa thành "ngưỡng cảnh báo" + chú thích trỏ đúng ghi chú descope. Đây là kiểu lỗi nguy hiểm nhất về vận hành: thuật ngữ cũ còn sót lại trong chính văn bản BR có thể khiến lập trình viên đọc nhầm là cơ chế khóa bếp vẫn tồn tại.
2. **2 file `00_TongHop_MedicalStaff.md` (Web + Mobile) hoàn toàn lỗi thời** ở 2 màn hình cốt lõi nhất của toàn bộ chuỗi quyết định trong phiên làm việc này — `12_ConductFoodSafetyInspectionScreen` và `23_ComplianceDashboard` — vẫn còn nguyên bản **trước** khi có BR-207/BR-208/BR-110/descope/US-09 (tức là bản gốc với `BR-105`, `MSG-024`, "khóa vận hành bếp", banner Quarantine cố định). Đây là 2 file "bản gộp để đọc nhanh/xuất báo cáo" — nếu ai đó dùng file này thay vì file lẻ để tham khảo hoặc bàn giao, họ sẽ nhận thông tin sai hoàn toàn về cơ chế đã bị loại bỏ 05/07/2026. Đã đồng bộ lại cả 2 file theo đúng nội dung file lẻ hiện hành, đồng thời phát hiện và vá luôn **2 chỗ bị cắt cụt giữa câu** (Web: giữa mục Violation Registry/Compliance Dashboard; Mobile: giữa mục Record Staff Violation/Compliance Dashboard) — dấu hiệu của lỗi ghi file trong một lần tái tạo trước đó, cùng loại lỗi đã từng xảy ra với bảng "Tóm tắt độ phủ theo Module" ở `traceability_matrix.md` (xem ghi chú 2026-07-01 trong chính file đó).
3. **`Web/SchoolAdministration/00_TongHop_SchoolAdministration.md` cũng lỗi thời tương tự** ở mục Compliance Dashboard (bản School Administration) — vẫn giữ field "Banner Kitchen Quarantine" với `BR-105`/`MSG-024`/"khóa vận hành bếp" y hệt bản trước descope, dù file lẻ `Web/SchoolAdministration/18_ComplianceDashboard.md` đã được cập nhật đúng từ trước. Đây là phát hiện **ngoài phạm vi thư mục MedicalStaff** nhưng trực tiếp liên quan tới quyết định descope của Medical Staff (2 màn dùng chung dữ liệu) nên đã sửa luôn để tránh cùng loại thông tin sai lệch lan sang phía School Administration. **Lưu ý**: file này có 1 lỗi cắt cụt khác, có sẵn từ trước (giữa mục Compliance Dashboard và Classroom Management, không liên quan tới đợt sửa đổi 05/07/2026) — đã vá phần nội dung Compliance Dashboard bị cắt, nhưng **chưa rà soát toàn bộ phần còn lại của file này** (ngoài phạm vi Medical Staff, cần audit riêng nếu đội dự án muốn).

### 6.2. Vấn đề còn tồn đọng, chưa sửa (cần đội dự án xác nhận trước khi xử lý)

4. **UC lệch số cho cùng 1 luồng Special Meal**: `08_SpecialMealRequestList.md` (cả Web và Mobile) trích dẫn **UC-036** ("View List Allergy & Special Meals" theo Report 3, thiên về hồ sơ dị ứng), trong khi 3 màn hình ngay sau trong cùng luồng — `07_ReviewSpecialMealRequestScreen.md`, `09_ApproveSpecialMealDialog.md`, `10_RejectSpecialMealDialog.md` — đều trích **UC-078**. Cả 2 bản Web/Mobile của file 08 đã tự flag đây là "**[Giả định — cần team xác nhận]**" từ trước, nhưng đây là lần đầu tiên được xác nhận là ảnh hưởng tới tính vận hành trơn tru: nếu backend implement theo đúng 2 UC riêng biệt (thay vì xem là 1 luồng), có nguy cơ tách nhầm thành 2 tính năng độc lập không khớp dữ liệu. **Khuyến nghị**: coi cả luồng List → Review → Approve/Reject là **1 UC duy nhất** (UC-078, vì 3/4 màn đã dùng và có Normal Flow đầy đủ hơn), sửa lại trích dẫn UC-036 ở file 08 thành UC-078, ghi chú rõ UC-036 (Report 3) mô tả 1 màn hình khác/rộng hơn không map 1-1 vào luồng Special Meal riêng. **Chưa sửa — chờ xác nhận đội dự án** vì đây là quyết định về ranh giới UC, không phải lỗi kỹ thuật đơn thuần.
5. **`16_ViewSchoolPolicyScreen.md` (UC-137) không có đặc tả Normal/Alternative/Exception Flow đầy đủ trong `Report 3.md`** — chỉ có 1 dòng mô tả tóm tắt (dòng 1144). Toàn bộ 3 field của màn này đang ở dạng **[Giả định]**. Không chặn vận hành ở mức thiết kế hiện tại (màn hình đơn giản, ít rủi ro sai lệch), nhưng nếu lập trình, nên coi đây là "thiết kế nội bộ dự án" thay vì "theo đúng SRS" khi viết tài liệu bàn giao — đã tự flag từ trước, giữ nguyên trạng thái, không cần hành động thêm trừ khi đội dự án muốn viết bổ sung Normal Flow chính thức.
6. **Rủi ro vận hành thực tế của quyết định descope (MSG-093)**: cảnh báo điểm checklist <60% hiện chỉ gửi tới School Administration và **không có cơ chế leo thang** nếu School Administration bỏ qua cảnh báo nhiều lần liên tiếp (không giống BR-148 — High severity incident bắt buộc cảnh báo đa kênh ngay lập tức). Về mặt thiết kế, đây là hệ quả **đã được người dùng chủ động chấp nhận** khi quyết định descope (ưu tiên "không gián đoạn phục vụ bữa ăn" hơn "chế tài tự động") — không phải lỗi, nhưng nên được ghi nhận là **rủi ro vận hành đã biết, đã chấp nhận** (accepted residual risk) trong tài liệu bàn giao, để tránh về sau bị hiểu nhầm là thiếu sót của thiết kế. Không có hành động sửa nào ở đây; chỉ khuyến nghị đội dự án cân nhắc bổ sung 1 cơ chế theo dõi số lần cảnh báo liên tiếp (không bắt buộc, ngoài phạm vi yêu cầu hiện tại — **không tự thêm** vì đây sẽ là mở rộng nghiệp vụ ngoài scope đã duyệt).
7. **Mục #10 (Mục 4) vẫn còn mở**: `MedicalStaff_UserStory.md` bị cắt cụt ở dòng ~140 (giữa câu, trong US-21) và thiếu hẳn mục "Phụ lục A" mà US-06/07/08/09 đều tự trích dẫn — đã nêu từ đợt audit trước, nhắc lại ở đây vì đây là loại lỗi cùng nhóm với phát hiện #2/#3 ở trên (cắt cụt do lỗi ghi file). **Chưa sửa — vẫn chờ xác nhận đội dự án** trước khi tự ý bổ sung nội dung Phụ lục A (không có cách suy luận chắc chắn nội dung gốc đã mất là gì).

### 6.3. Xác nhận: các luồng nghiệp vụ còn lại vận hành nhất quán, đúng phạm vi

Đối chiếu toàn bộ chuỗi Incident Management (Assign Severity UC-009 → Investigation Workspace UC-041 → Establish CAPA → School Admin duyệt CAPA UC-005 → Record Corrective Actions/Validate UC-168 → Generate Incident Report UC-033 → School Admin Close Incident UC-013) và chuỗi Violation Management (Record Process/Staff Violation UC-178/179 → Record Violation Resolution UC-180 → School Admin Approve/Reject UC-182) — cả 2 chuỗi đều nhất quán giữa các file Screen Spec, đúng thứ tự, đúng phân quyền theo actor, không phát hiện thêm lỗ hổng hay trích dẫn sai. Riêng biệt: cơ chế cách ly **lô nguyên liệu** (BR-149, kích hoạt khi Assign Severity = High hoặc khi có sự cố thực sự) hoạt động độc lập, đúng phạm vi, không bị ảnh hưởng bởi quyết định descope "điểm checklist <60%" — 2 cơ chế này nay đã tách bạch rõ ràng ở mọi tài liệu sau đợt sửa 05/07/2026.

**Kết luận đợt audit**: sau khi vá 3 lỗi ở mục 6.1 (1 mâu thuẫn thuật ngữ BR-208 + 2 file tổng hợp lỗi thời + 1 file tổng hợp School Admin liên đới), toàn bộ tài liệu Medical Staff (Web+Mobile) hiện **nhất quán với nhau và với `business_rules.md`**. Còn 3 điểm mở (#4 UC-036/UC-078, #5 UC-137 thiếu đặc tả gốc, #7 Phụ lục A) đều đã được flag rõ ràng, không chặn vận hành ở mức hiện tại, và đúng theo yêu cầu "hỏi trước khi giả định" — không tự ý quyết định thay đội dự án.

---

## 7. Audit trải nghiệm người dùng (UX) — mô phỏng sử dụng thực tế theo `MedicalStaff_UserStory.md` (06/07/2026)

**Bối cảnh & phương pháp**: người dùng phản ánh cảm giác UX của các màn Medical Staff "rất tệ" nhưng chưa rõ nguyên nhân — field sai, thiếu tính năng, hay UI kém — và yêu cầu tôi đóng vai một Medical Staff dùng phần mềm thật, đi theo đúng trình tự `MedicalStaff_UserStory.md`. Tôi đã thử dựng `npm run dev` thật trong sandbox nhưng bị chặn: `node_modules/@next/` chỉ có sẵn binary `swc-win32-x64-msvc` (máy phát triển gốc là Windows), sandbox chạy Linux nên Next.js cần tải `@next/swc-linux-x64-gnu` qua mạng — xác nhận qua `curl` rằng `registry.npmjs.org` bị chặn bởi allowlist mạng của sandbox (`403 blocked-by-allowlist`). Vì vậy, audit được thực hiện bằng cách đọc toàn bộ 18 file `page.tsx` của Medical Staff (kể cả state/mutation logic, không chỉ JSX) và mô phỏng từng luồng theo đúng thứ tự US-01 → US-21.

### 7.1. Lỗi logic/dữ liệu nghiêm trọng (không phải chỉ UI) — người dùng đã xác nhận đúng, ghi nhận vào tài liệu (chưa sửa code, quyết định 06/07/2026)

**(a) Investigation Workspace không lưu dữ liệu thật.** `incidents/[id]/investigation/page.tsx` chỉ import `useDetail` từ `@/lib/api/hooks` — cả 4 panel (Extract Sample / Gathering Evidence / Root Cause / Treatment Measures) đều dùng `useState` nội bộ + `toast.success(...)`, không có `useCreate`/`useUpdate` nào. Rời trang hoặc F5 sẽ mất toàn bộ bằng chứng/kết luận/biện pháp vừa nhập. Nút "Establish Measures" không tạo `CorrectiveAction` thật — quay lại `incidents/[id]/page.tsx`, thẻ "CAPA liên quan (BR-152)" vẫn chỉ hiển thị đúng 2 bản ghi tĩnh có sẵn trong `CORRECTIVE_ACTIONS` (`mock-data.ts`), không phản ánh hành động vừa "thành công".

**(b) Cổng kiểm soát "Validate CAPA đã duyệt" không thực sự kiểm tra trạng thái duyệt.** Ở `incidents/[id]/page.tsx`, biến `capaApproved = relatedCapa.length > 0` — chỉ cần **có** CAPA gắn với sự cố (bất kể trạng thái) là đủ để bật nút, trong khi mô tả dialog ghi rõ "Chỉ hiển thị CAPA đã được School Administration phê duyệt (BR-153)". Đối chiếu interface `CorrectiveAction` (`mock-data.ts`), trường `status` chỉ nhận `ASSIGNED | SUBMITTED | IN_PROGRESS | COMPLETED | OVERDUE` — **không có giá trị nào đại diện "đã được School Admin duyệt"**. Với sự cố mẫu `INC-2026-014`, 2 CAPA gắn vào đang ở `SUBMITTED`/`IN_PROGRESS` (chưa xong) nhưng nút Validate đã sáng sẵn ngay từ đầu. Đây là khoảng trống trong chính data model (thiếu 1 trạng thái), không chỉ thiếu 1 điều kiện `if`.
**Phạm vi (quyết định người dùng 06/07/2026)**: chỉ ghi nhận và xử lý ở phía **Medical Staff** trong đợt này — **chưa** mở rộng kiểm tra màn hình Approve/Reject Corrective Action phía School Administration (UC-005); việc đối chiếu 2 phía để đội dự án tự quyết định ở đợt sau nếu cần.

**(c) Root cause bị nhập trùng lặp, không liên thông.** Investigation Workspace (panel 3 "Xác định nguyên nhân gốc rễ") và dialog "Lập báo cáo sự cố" (Step 3, UC-033, ở `incidents/[id]/page.tsx`) là 2 ô nhập liệu hoàn toàn tách biệt cho cùng 1 khái niệm nghiệp vụ — không có pre-fill từ nơi này sang nơi kia. Vừa gây thao tác trùng lặp, vừa có rủi ro 2 kết luận khác nhau được ghi cho cùng 1 sự cố. Việc sửa mục này phụ thuộc vào việc sửa mục (a) trước (phải lưu dữ liệu thật thì mới có gì để pre-fill).

### 7.2. Thiếu tính năng / phản hồi không đủ — đã xác nhận đúng (chưa sửa code, quyết định 06/07/2026)

**(d) "Cần bổ sung" không có field lý do.** `health-reviews/page.tsx`: nút "Cần bổ sung" chỉ đổi `healthStatus` sang `NEEDS_INFO` kèm toast, không có ô nào để Medical Staff ghi rõ **thiếu thông tin gì** — phụ huynh nhận thông báo mơ hồ. Với hồ sơ dị ứng ảnh hưởng an toàn thực tế của học sinh, đây là khoảng trống đáng lo hơn mức "thiếu 1 field UI" thông thường.

**(e) Validation sai thời điểm ở dialog thu hồi mẫu.** `samples/page.tsx` (UC-076): dropdown "Incident ID" liệt kê cả sự cố **đã đóng** (tự ghi "ĐÃ ĐÓNG" ngay trong option), nhưng chỉ chặn **sau khi** người dùng đã chọn, nhập thêm người ủy quyền + phòng lab, rồi bấm Submit mới báo lỗi (MSG-023). Nên lọc/vô hiệu hóa các option đã đóng ngay tại dropdown, tránh để người dùng nhập xong bị văng ra.

**(f) Trường dị ứng là text tự do, rủi ro an toàn dữ liệu.** `health-records/[id]/page.tsx` (dialog "Cập nhật hồ sơ", UC-094): field dị ứng là `Input` text tự do, phân tách bằng dấu phẩy (`allergiesText.split(",")`), không chọn từ danh mục chuẩn như `specialDiet` (BR-066: Halal/Vegetarian/Gluten-Free/Nut-Free/Lactose-Free). Gõ tự do dễ tạo biến thể ("Tôm"/"tôm"/"Hải sản") khiến hệ thống bếp match cảnh báo dị ứng theo chuỗi ký tự bị sai lệch.
**Quyết định người dùng (06/07/2026)**: đổi sang chọn từ danh mục dị ứng chuẩn (tag/multi-select), theo đúng mẫu đã áp dụng cho `specialDiet`. **Điểm chặn cần đội dự án xác nhận trước khi lập trình**: `business_rules.md` hiện **chưa định nghĩa danh mục dị ứng chuẩn nào** (BR-031 chỉ yêu cầu Y tế xác nhận khai báo, không liệt kê danh mục dị nguyên) — cần bổ sung 1 Business Rule mới hoặc mở rộng BR-031 để liệt kê danh mục chuẩn trước khi có thể code tag-picker. Đây là quyết định y khoa/nghiệp vụ, **không tự đề xuất danh mục cụ thể** — cần đội dự án hoặc cố vấn y tế xác nhận.

### 7.3. UI / kiến trúc thông tin — ưu tiên thấp hơn, không chặn vận hành

**(g)** Sidebar Medical Staff (`roles.ts`) có 7 nhóm, trong đó 4 nhóm ("Lưu mẫu Thực phẩm", "Sự cố An toàn Thực phẩm", "Quản lý Vi phạm", "Nhà trường") chỉ có đúng **1 mục mỗi nhóm** — tạo nhiều tiêu đề nhóm không cần thiết, sidebar dài hơn mức cần cho việc điều hướng hàng ngày.

**(h)** Dữ liệu `KitchenInspection` (kết quả checklist vệ sinh bếp, UC-014) hiển thị lặp lại ở **3 màn khác nhau**: `compliance/page.tsx` (điểm gần nhất), `conduct-inspection/page.tsx` (bảng "Lịch sử kiểm tra bếp" cuối trang), và `reports/page.tsx` (tab "Monthly Kitchen Audits") — cùng 1 nguồn dữ liệu, không rõ màn nào là nơi tra cứu chính thức.

**(i)** Tên gọi "Hồ sơ kiểm thực" (`inspections/page.tsx`, UC-079 — kiểm thực ba bước: nguyên liệu/chế biến/trước phục vụ) và "Kiểm tra an toàn bếp" (`conduct-inspection/page.tsx`, UC-014 — checklist vệ sinh hàng ngày) là 2 nghiệp vụ khác nhau nhưng tên tiếng Việt gần giống nhau ("kiểm thực" vs "kiểm tra"), dễ gây nhầm lẫn cho người dùng không quen thuật ngữ dự án.

**(j)** `dashboard/page.tsx`: 6 shortcut không có "Kiểm tra an toàn bếp" (hành động chính hàng ngày, US-05) nhưng lại có "Kết quả kiểm thực 3 bước" (chỉ xem, không phải hành động) — ưu tiên hiển thị đang lệch giữa hành động cần làm và màn chỉ xem.

**(k)** Ảnh minh chứng (checklist kiểm tra bếp, biên bản vi phạm) chỉ là nút tăng số đếm (`setEvidence(...photos + 1)`), không xem trước/xóa được ảnh chụp nhầm — chấp nhận được ở giai đoạn mock, nhưng cần thay bằng upload/preview thật khi lập trình chính thức.

**(l)** Nhãn nút trộn tiếng Anh/Việt không nhất quán trong cùng 1 màn: "Save review & Activate process", "Export & Submit Incident Reports", "Come back" đặt cạnh "Hủy", "Đóng", "Xác nhận duyệt".

### 7.4. Mật độ hiển thị: chiều cao ô input & khoảng cách giữa field (mới, 06/07/2026)

Người dùng phản ánh chữ/field trong các form (dialog Update Health Record, Conduct Inspection, Violation...) hiển thị dày đặc, ô input thấp, gây rối mắt. Đối chiếu `components/ui/input.tsx` (component dùng chung **mọi role**, không riêng Medical Staff):
- `Input`/`Select` hiện tại: `h-9` (36px), `py-1`.
- `Textarea` hiện tại: `min-h-[80px]`, `py-2`.
- Khoảng cách Label → Input trong 1 field: `space-y-1.5` (6px) — lặp lại ở hầu hết dialog Medical Staff.
- Khoảng cách giữa các field trong 1 form: `space-y-4` (16px).

**Quyết định người dùng (06/07/2026)**: chỉ tăng chiều cao/khoảng cách cho **các trang Medical Staff**, **không sửa** `components/ui/input.tsx` dùng chung (sửa ở đây sẽ làm lệch phong cách các role khác — School Administration, Service Caterers... — vi phạm nguyên tắc nhất quán toàn dự án). Hướng kỹ thuật đề xuất khi lập trình: truyền `className` override riêng cho từng `Input`/`Select`/`Textarea` trong các file `medical-staff/**/*.tsx` (component đã hỗ trợ merge `className` qua `cn()`), ví dụ tăng `h-9` → `h-11` (44px) cho Input/Select, `min-h-[80px]` → `min-h-[110px]` cho Textarea, `space-y-1.5` → `space-y-2` cho Label→Input, `space-y-4` → `space-y-5`/`space-y-6` giữa các field. Giá trị cụ thể cần đội dự án/thiết kế xác nhận trước khi áp dụng đại trà trên cả 18 file, để tránh mỗi nơi chỉnh một kiểu khác nhau (mất tính nhất quán ngay trong nội bộ Medical Staff).

### 7.5. Tổng kết đợt audit UX

Không tìm thấy nguyên nhân chính là "field sai" (phần lớn field đã đúng bối cảnh nghiệp vụ theo đối chiếu ở Mục 6). Nguyên nhân chính khiến trải nghiệm "tệ" nằm ở 3 nhóm: (1) một số hành động quan trọng (Investigation Workspace) **trông như đã lưu nhưng thực chất không lưu** — mất niềm tin người dùng; (2) một cổng kiểm soát nghiệp vụ (CAPA approval) bị vô hiệu hóa do thiếu 1 trạng thái trong data model; (3) một vài chỗ thiếu phản hồi/validation đúng thời điểm (lý do "Cần bổ sung", dropdown Incident ID) khiến người dùng phải đoán hoặc làm lại. Phần UI/mật độ hiển thị (Mục 7.3–7.4) là có thật nhưng ảnh hưởng nhẹ hơn, được xếp ưu tiên thấp/trung bình trong kế hoạch (Mục 4, #27–#28).

Theo đúng quyết định người dùng (06/07/2026): **toàn bộ Mục 7 chỉ dừng ở tài liệu hóa** (đưa vào Mục 4, #21–#28) — chưa đụng vào code, chờ người dùng duyệt kế hoạch trước khi lập trình.

---

## 8. Cập nhật 06/07/2026 (đợt 6) — 2 thay đổi nghiệp vụ mới: Checklist kiểm tra bếp động (BR-210/UC-186) và Biên bản vi phạm chuẩn hóa + chữ ký 3 bên (BR-211)

### 8.1. Checklist kiểm tra bếp chuyển sang quản lý động — BR-210, UC-186

Theo yêu cầu người dùng, `Kitchen Safety and Sanitation Checklist` (UC-014) không còn là danh sách hạng mục cố định do System Admin cấu hình. Medical Staff nay có toàn quyền CRUD (thêm/sửa/xóa/sắp xếp) cả nhóm và hạng mục, kể cả nội dung chú thích căn cứ pháp lý (icon "ℹ"), không cần System Admin, không cần phê duyệt. Đây là **thay đổi kiến trúc quyền sở hữu dữ liệu**, không phải chỉ thêm field — đã sửa trực tiếp điều khoản "System Admin cấu hình chú thích, áp dụng chung mọi trường" trong **BR-207** (thay vì âm thầm ghi đè) và cấp **BR-210** mới cho cơ chế CRUD, theo đúng nguyên tắc "luôn kiểm tra nhất quán, không tự ý sửa mà không ghi vết".

Nội dung 6 nhóm checklist cụ thể (Hồ sơ; Yêu cầu người tiếp xúc trực tiếp; Điều kiện bảo quản; Điều kiện sơ chế/chế biến; An toàn thực phẩm; Sổ kiểm thực 3 bước và lưu mẫu — do người dùng cung cấp trực tiếp, ~35 mục) đã được tự nghiên cứu căn cứ pháp lý từ `00_Policies/` cho từng mục (không suy diễn khi không có nguồn — 9 mục còn thiếu căn cứ chính xác được đánh dấu `[cần xác nhận thêm]` trong `MedicalStaff_KitchenInspectionChecklist_Proposal.md`), và chuyển vai trò từ "checklist chính thức cố định" sang "**dữ liệu khởi tạo mặc định**" mà Medical Staff có thể tự chỉnh sau khi hệ thống vận hành.

**Đã cập nhật đồng bộ**: `business_rules.md` (BR-207 sửa đổi + BR-210 mới, changelog v1.4), `traceability_matrix.md` (UC-186 + BR-210 + MSG-094), `error_message.md` (MSG-094, Warning §3 — xác nhận xóa hạng mục đang dùng), `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (viết lại hoàn toàn theo checklist mới), `MedicalStaff_UserStory.md` (US-22, Epic 5 mới), Screen Spec `12_ConductFoodSafetyInspectionScreen.md` (Web+Mobile, thêm mục "Quản lý Checklist UC-186"), và cả 2 file `00_TongHop_MedicalStaff.md` (Web+Mobile, xem Mục 9). **Chưa sửa code** — xem Mục 4 (#29).

### 8.2. Biên bản Vi phạm chuẩn hóa: xuất PDF + lưu ảnh ký 3 bên — BR-211

Theo yêu cầu người dùng, dựa trên 1 mẫu biên bản thực tế (`specialreport2173_1.pdf`, "Biên bản Sự việc" của Trường Đại học FPT P.VSATTP) do người dùng cung cấp, các field của `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (UC-178/179) được thiết kế lại để khớp cấu trúc mẫu giấy này — bổ sung `locationOfIncident`, `occurredAt` (đã có ở UC-179, nay đồng bộ thêm cho UC-178), `caterersRepresentativeId` (liên kết tài khoản Service Caterers, không nhập tay tự do), và `witnessId` (đồng bộ 2 chiều giữa UC-178/179, vì mẫu giấy có đúng 3 bên ký cố định).

Sau khi Medical Staff điền đủ các field trên (trừ chữ ký), hệ thống xuất **PDF** (không phải Word — lựa chọn dựa trên tiêu chí "nhẹ cho hệ thống" theo đúng yêu cầu người dùng: PDF không cần thư viện xử lý định dạng .docx phức tạp, render nhất quán khi in). Medical Staff in ra giấy, thu thập chữ ký tay của 3 bên (Y tế trường học / Nhà thầu / Người chứng kiến) ngoài đời thực, chụp ảnh biên bản đã ký, và tải ảnh lên hệ thống (`signedRecordPhotoUrl`) — theo đúng lựa chọn của người dùng giữa 2 phương án đã đề xuất (in-ký tay-chụp ảnh, so với ký điện tử trên màn hình).

**Gate bắt buộc (chặn cứng)**: theo quyết định cuối cùng của người dùng (sau khi được giải thích rõ khác biệt giữa chặn cứng và cảnh báo mềm), nút "Đóng biên bản" (LOW) và "Gửi School Administration phê duyệt" (MEDIUM/HIGH) ở `24_RecordViolationResolution.md` bị **disable hoàn toàn** cho đến khi `signedRecordPhotoUrl` tồn tại — không chỉ hiển thị cảnh báo. Rủi ro vận hành đi kèm (có thể chậm trễ đóng hồ sơ nếu quy trình ký giấy chậm) đã được người dùng chủ động chấp nhận. Vì gate đặt ở bước Medical Staff nộp, màn phê duyệt của School Administration (UC-182, `48_ApproveRejectViolationResolution.md`) không cần thêm gate riêng — dữ liệu luôn đầy đủ khi tới đó.

**Đã cập nhật đồng bộ**: `00_Policies/specialreport2173_1.md` (transcript mẫu giấy, đánh dấu rõ không phải văn bản pháp luật), `business_rules.md` (BR-211 mới, changelog v1.5), `traceability_matrix.md` (BR-211 + MSG-095, cập nhật hàng UC-178/179/180), `error_message.md` (MSG-095 — chuyển từ Warning §3 sang **Error §1** sau quyết định chặn cứng), Screen Spec `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md`/`24_RecordViolationResolution.md` (Web+Mobile, MedicalStaff), `46_RecordViolationResolution.md`/`48_ApproveRejectViolationResolution.md` (Web, SchoolAdministration), và 3 file `00_TongHop_*.md` liên quan (xem Mục 9). **Chưa sửa code** — xem Mục 4 (#30). **Còn mở**: tên chính thức của văn bản in ra ("BIÊN BẢN SỰ VIỆC" hay "BIÊN BẢN VI PHẠM") và câu chữ pháp lý đóng biên bản cần đội dự án duyệt trước khi dùng làm mẫu in chính thức — đã flag trong Screen Spec, không tự quyết định câu chữ pháp lý.

## 9. Đồng bộ file `00_TongHop_*.md` theo BR-210/BR-211 (06/07/2026)

Theo đúng bài học đã ghi nhận ở Mục 6.1 (#2, #3) — các file "bản gộp để đọc nhanh/xuất báo cáo" từng bị phát hiện lỗi thời/cắt cụt và có nguy cơ gây hiểu nhầm nếu ai đó dùng nhầm thay file lẻ — đã chủ động rà và đồng bộ lại cả 3 file `00_TongHop_*.md` liên quan ngay trong đợt cập nhật này, thay vì chờ phát hiện ở lần audit sau:

- `Web/MedicalStaff/00_TongHop_MedicalStaff.md`: đồng bộ mục "Conduct Food Safety Inspection Screen" (thêm "Quản lý Checklist UC-186"), "Record Process Violation", "Record Staff Violation", "Record Violation Resolution" theo đúng nội dung file lẻ hiện hành. **Phát hiện thêm cùng loại lỗi cắt cụt giữa câu đã ghi nhận ở Mục 6.1 (#2)** — lần này tại đúng 2 vị trí: cuối mục "Record Process Violation"/đầu "Record Staff Violation", và cuối mục "Record Violation Resolution"/đầu "Incident List". Vì các mục này đang được thay thế toàn bộ theo BR-211, việc thay thế đã tự sửa luôn phần bị cắt cụt.
- `Mobile/MedicalStaff/00_TongHop_MedicalStaff.md`: đồng bộ tương tự 4 mục trên. Cũng phát hiện đúng 1 vị trí cắt cụt cùng loại (cuối "Record Process Violation"/đầu "Record Staff Violation") — đã tự sửa khi thay thế nội dung.
- `Web/SchoolAdministration/00_TongHop_SchoolAdministration.md`: đồng bộ mục "Record Violation Resolution (bản School Administration)" (thêm field 7 — badge tình trạng biên bản đã ký) và "Approve/Reject Violation Resolution" (thêm ghi chú BR-211). Không phát hiện cắt cụt mới ở phạm vi 2 mục này.

Không có thay đổi nào khác ngoài phạm vi các mục liên quan tới BR-210/BR-211 được thực hiện ở các file TongHop này trong đợt này.

---

## 10. Quyết định thiết kế 07/07/2026 — Gộp dialog Record Process Violation (UC-178) + Record Staff Violation (UC-179) thành 1 dialog duy nhất

> **TRẠNG THÁI: đã chốt thiết kế field (bản cuối 11/07/2026) VÀ ĐÃ ÁP DỤNG ĐẦY ĐỦ vào toàn bộ tài liệu thiết kế (hoàn tất 11/07/2026).** Xem §10.6 bên dưới cho danh sách đầy đủ file đã cập nhật và các điểm còn hở (nếu có).

> **Cập nhật 11/07/2026 (chốt cuối cùng, thay thế 1 phần §10.3/10.3b/10.4 bên dưới)**:
> 1. **Giữ tách BR-183/BR-184 ở tầng dữ liệu** (hướng "a" — đúng như lý luận đã có sẵn ở §10.2, người dùng xác nhận lại 11/07/2026): `NonConformanceRecord` (tự động từ kiểm thực 3 bước/audit) và `StaffViolationRecord` (nhập tay độc lập) vẫn là 2 entity riêng; khi `NonConformanceRecord` được "nâng cấp" theo BR-183, hệ thống tạo 1 bản ghi `StaffViolationRecord` tương ứng để dùng chung 1 màn hình/1 mẫu biên bản — đúng nguyên văn ý đã note sẵn ở BR-211.
> 2. **UC-179 là mã "sống sót"** (không cấp mã mới) — deprecate UC-178 theo đúng pattern giữ số hiệu đã dùng cho BR (xem Mục cập nhật BR-208 09/07/2026): giữ số UC-178, đánh dấu `(Deprecated 11/07/2026)`, trỏ sang UC-179.
> 3. **Đổi tên tiêu đề bản in PDF**: từ "BIÊN BẢN SỰ VIỆC" (nguyên văn mẫu giấy gốc `specialreport2173_1.md`) → **"BIÊN BẢN VI PHẠM"** — vì hệ thống chỉ dùng mẫu này cho trường hợp vi phạm, không phải sự việc chung chung.
> 4. **Bỏ hẳn field nhân chứng khỏi form nhập liệu** (đảo ngược 1 phần quyết định ở §10.3 field 4-5 bên dưới — lúc đó mới giảm từ "Select bắt buộc" xuống "text tự do tùy chọn", nay bỏ hẳn không còn field nào): hệ thống không track thông tin người chứng kiến dưới bất kỳ hình thức nào (không `witnessId`, không `witnessName`/`witnessPosition`). Bản PDF xuất ra **vẫn giữ nguyên cột chữ ký thứ 3 "Người chứng kiến"** (khớp đúng mẫu giấy gốc, cũng để trống trong ví dụ thật) — nhưng đây là khoảng trống tĩnh trên PDF, không gắn với field dữ liệu nào, điền tay ngoài đời nếu cần.
> 5. **Lý do phân trách nhiệm không cần tên cá nhân bắt buộc** (làm rõ thêm, không đổi field 6 `staffName` — field này vẫn giữ nguyên optional như §10.3 đã chốt): mục đích cốt lõi của biên bản là **Nhà trường (Medical Staff) ghi nhận lỗi của Service Caterers (cấp công ty)**, 2 bên ký xác nhận cùng chịu trách nhiệm — không bắt buộc quy cho 1 cá nhân. `staffName` (field 6) vẫn để tùy chọn đúng như thiết kế cũ, chỉ dùng khi Medical Staff **chủ động muốn** kích hoạt thêm BR-184/185/187 (kỷ luật cá nhân) — không phải yêu cầu bắt buộc của biên bản.

### 10.1. Vấn đề người dùng nêu ra

Người dùng chỉ ra rằng dialog "Lập biên bản vi phạm quy trình" (UC-178) và "Lập biên bản vi phạm nhân viên" (UC-179) hiện tại gần như giống hệt nhau, chỉ khác vài field — trong khi mẫu biên bản giấy thực tế (`00_Policies/specialreport2173_1.md`) không hề phân biệt 2 loại này: cùng 1 cấu trúc 3 bên (đại diện Y tế trường học/Nhà thầu/người chứng kiến), cùng 1 khối nội dung tự do, cùng 1 khối chữ ký. Việc duy trì 2 dialog riêng gây trùng lặp toàn bộ field BR-211 (địa điểm, thời điểm, đại diện Nhà thầu, mô tả, ảnh, mức độ, nhân chứng, xuất PDF, ảnh ký) giữa 2 nơi.

### 10.2. Phản biện đã trao đổi và kết luận

Tôi phản biện lại người dùng rằng việc tách UC-178/UC-179 **ở tầng dữ liệu/BR** vẫn hợp lý — không phải do SRS đặt ra tùy tiện — vì 2 lý do cốt lõi: (a) quan hệ nhân-quả không phải 1-1 (1 lỗi quy trình có thể không quy được cho ai, hoặc quy cho nhiều người); (b) khác biệt thẩm quyền xử lý thật: nhân viên bếp/catering thuộc biên chế **Nhà thầu (Service Caterers)**, không phải trường — nên trường chỉ có thể "**đề xuất** chấm dứt hợp đồng" (BR-185) chứ không tự quyết được, trong khi vi phạm quy trình/sản phẩm (BR-183) trường xử lý trực tiếp vì đó là chất lượng dịch vụ theo hợp đồng. Người dùng đồng ý giữ 2 khái niệm BR-183/BR-184 tách biệt **ở tầng dữ liệu**, nhưng thống nhất gộp **ở tầng UI**: 1 dialog duy nhất, không có toggle chọn "loại vi phạm" — BR-183 hay BR-184 được suy ra tự động từ việc có điền "Nhân viên liên quan" hay không, thay vì người dùng phải chọn chế độ trước.

### 10.3. Bảng field cuối cùng (đã chốt qua nhiều vòng trao đổi 06–07/07/2026)

| # | Field | Loại nhập | Bắt buộc? | Nguồn / vai trò |
| --- | --- | --- | --- | --- |
| 1 | Địa điểm xảy ra sự việc (`locationOfIncident`) | Input text | Bắt buộc | Mẫu giấy: "Hôm nay, tại: ___" |
| 2 | Đại diện Y tế trường học (`reportedBy`) | Tự động (tài khoản đăng nhập), không phải ô nhập tay | — | Mẫu giấy: "Đại diện P.VSATTP" |
| 3 | Đại diện Nhà thầu (`caterersRepresentativeId`) | Select (tài khoản Service Caterers) | Bắt buộc | Mẫu giấy: "Đại diện nhà thầu"; liên kết tài khoản có thật, không nhập tay tự do (quyết định người dùng trước đó) |
| ~~4~~ | ~~Người chứng kiến — Tên~~ | — | — | **Bỏ hẳn 11/07/2026** — hệ thống không track người chứng kiến; PDF vẫn giữ cột chữ ký trống tĩnh (xem ghi chú field 14) |
| ~~5~~ | ~~Người chứng kiến — Chức vụ~~ | — | — | **Bỏ hẳn 11/07/2026**, đi kèm field 4 cũ |
| 6 | Nhân viên liên quan (`staffName`) | Input text tự do (không phải khóa ngoại — nhân viên catering không có tài khoản User) | Tùy chọn — để trống nếu không quy trách nhiệm cho 1 cá nhân cụ thể | Không có trên mẫu giấy — giữ lại làm điều kiện tự động kích hoạt BR-184/185/187 (xem 10.4). **Xác nhận 11/07/2026**: mục đích cốt lõi biên bản là Nhà trường ghi nhận lỗi Service Caterers cấp công ty, không bắt buộc quy cá nhân — field này chỉ kích hoạt kỷ luật cá nhân khi Medical Staff chủ động điền |
| 7 | Chức vụ nhân viên (`staffPosition`) | Input text tự do | Tùy chọn, chỉ hiện khi đã điền (6) | Không có trên mẫu giấy |
| 8 | Mức độ nghiêm trọng (`severityLevel`) | Select (LOW/MEDIUM/HIGH) | Bắt buộc | Không có trên mẫu giấy — giữ Select (không phải text tự do) vì là điều kiện tự động phân luồng nút "Đóng biên bản"/"Gửi School Administration phê duyệt" ở `24_RecordViolationResolution.md` theo BR-186 |
| 9 | Loại vi phạm (`violationType`, gồm `OVERRIDE_ABUSE`) | **Select** (không phải text tự do — quyết định lại 07/07/2026 sau khi nhận ra text tự do sẽ làm hỏng khả năng lọc) | Tùy chọn | Không có trên mẫu giấy — giữ Select để BR-190 (báo cáo lạm dụng manual override hàng tuần) lọc được chính xác, tránh sai lệch do gõ tay không nhất quán |
| 10 | Thời điểm xảy ra sự việc (`occurredAt`) | DateTimePicker | Bắt buộc | Dùng để hệ thống tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày]..." trong nội dung PDF, khớp mẫu giấy |
| 11 | **Nội dung biên bản** (thay cho "Mô tả vi phạm" cũ) | **Rich text editor** (toolbar định dạng: đậm/nghiêng/gạch chân, cỡ chữ, màu chữ, bảng, căn lề, chèn ảnh/link) | Bắt buộc | Mẫu giấy: phần văn xuôi tự do sau câu "Vào hồi..." (VD: "1. Chỉ tiêu Hàn the... 3. Chỉ tiêu Độ sạch bát đĩa...") — nằm ngay sau câu tự sinh ở field 10, người dùng viết tự do đúng như cách viết biên bản giấy thật, không bị ép vào khuôn Textarea đơn giản |
| 12 | Ảnh bằng chứng | Camera/Upload | Bắt buộc | Không có trên mẫu giấy — yêu cầu có sẵn từ BR-183/184, giữ nguyên, độc lập với nội dung |
| 13 | Nút Save Violation | Button | — | — |
| 14 | Nút Xuất Biên bản (PDF) | Button | — | Tiêu đề PDF **"BIÊN BẢN VI PHẠM"** (đổi từ "BIÊN BẢN SỰ VIỆC" nguyên văn mẫu giấy, 11/07/2026); sinh câu kết cố định + **3** khoảng chữ ký theo mẫu giấy (Y tế trường học / Nhà thầu / Người chứng kiến — cột thứ 3 luôn để trống tĩnh, không gắn field) |
| 15 | Ảnh biên bản đã ký (`signedRecordPhotoUrl`) | Upload | Bắt buộc trước khi đóng/gửi duyệt (BR-211, chặn cứng) | Mẫu giấy: "Chữ ký 3 bên: P.VSATTP / NHÀ THẦU / NGƯỜI LÀM CHỨNG" |

### 10.3b. Đặc tả đầy đủ — dùng để code thử (bản nháp, chưa chính thức đưa vào Screen Spec 21/22)

> Mục này viết theo đúng cấu trúc 1 Screen Spec chuẩn của dự án (Business Logic → Trạng thái màn hình → Platform Layout → AI Generation Prompt) để dev/coding agent có thể lấy trực tiếp làm căn cứ code thử, mà không cần đợi bản chính thức được chép vào `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md`.

**Tên màn hình đề xuất**: "Lập Biên bản Vi phạm" (dialog dùng chung, thay thế cả 2 dialog UC-178/UC-179 hiện có trong code)
**Actor**: Medical Staff (dùng chung với Service Caterers theo BR-183/184) | **UC liên quan**: UC-178 + UC-179 (dùng chung 1 dialog, xem §10.2 lý do giữ tách BR nhưng gộp UI) | **BR liên quan**: BR-183, BR-184, BR-185, BR-186, BR-187, BR-190, BR-211
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error — thiếu field bắt buộc), MSG-086 (Success), MSG-088 (Warning — cảnh báo nghi ngờ tái diễn, chỉ áp dụng khi có field 6)

#### Danh sách Field / Thành phần (chi tiết ràng buộc)

| # | Field | Loại | Ràng buộc chi tiết | Giá trị mặc định |
| --- | --- | --- | --- | --- |
| 1 | `locationOfIncident` | Input text | Bắt buộc, không rỗng | Rỗng |
| 2 | `reportedBy` | Text hiển thị (đọc-only) | Tự động = user hiện tại lúc mở dialog | User hiện tại |
| 3 | `caterersRepresentativeId` | Select | Bắt buộc; danh sách = tài khoản Service Caterers đang hoạt động của trường hiện tại | Không chọn sẵn |
| ~~4~~ | ~~`witnessName`~~ | — | **Bỏ hẳn 11/07/2026** — không còn field nào trong form; PDF vẫn in cột chữ ký "Người chứng kiến" trống tĩnh | — |
| ~~5~~ | ~~`witnessPosition`~~ | — | **Bỏ hẳn 11/07/2026**, đi kèm field 4 cũ | — |
| 6 | `staffName` | Input text | Tùy chọn; để trống = vi phạm quy trình thuần túy (BR-183); có giá trị = kích hoạt thêm BR-184/185/187 | Rỗng |
| 7 | `staffPosition` | Input text | Tùy chọn; chỉ **hiện trong UI** khi (6) đã có ký tự đầu tiên (progressive disclosure, không phải validation bắt buộc) | Rỗng, ẩn |
| 8 | `severityLevel` | Select | Bắt buộc, 1 trong 3: `LOW` / `MEDIUM` / `HIGH` | Không chọn sẵn |
| 9 | `violationType` | Select | Tùy chọn; danh sách đề xuất giữ nguyên 4 giá trị đã có từ bản cũ: "Bỏ qua bước quy trình bắt buộc", "Vi phạm vệ sinh cá nhân", "Lạm dụng quyền ghi đè hệ thống (`OVERRIDE_ABUSE`)", "Hành động không được ủy quyền" — **cần đội dự án xác nhận danh sách này trước khi khóa cứng enum trong code**, không tự thêm giá trị mới ngoài 4 giá trị đã có | Không chọn sẵn |
| 10 | `occurredAt` | DateTimePicker | Bắt buộc | Rỗng |
| 11 | `violationContent` (nội dung biên bản) | Rich text editor | Bắt buộc, không rỗng sau khi loại bỏ thẻ HTML rỗng; lưu dạng HTML string; phía trên ô editor hiển thị (không cho sửa) câu tự sinh "Vào hồi [giờ từ occurredAt] ngày [ngày từ occurredAt]..." | Rỗng |
| 12 | Ảnh bằng chứng | Camera/Upload, nhiều ảnh | Bắt buộc ít nhất 1 ảnh | 0 ảnh |
| 13 | Nút Save Violation | Button (primary) | Disabled khi thiếu bất kỳ field bắt buộc nào (1, 3, 8, 10, 11, 12) | — |
| 14 | Nút Xuất Biên bản (PDF) | Button (secondary) | Chỉ hiện sau khi Save thành công | — |
| 15 | `signedRecordPhotoUrl` | Upload | Không bắt buộc để Save tại đây; bắt buộc trước khi đóng/gửi duyệt ở `24_RecordViolationResolution.md` (BR-211, chặn cứng, không đổi) | Rỗng |

#### Trạng thái màn hình

- **Loading**: spinner trên nút Save Violation khi đang lưu.
- **Empty**: không áp dụng (dialog tạo mới, không có trạng thái rỗng).
- **Error**: MSG-004/MSG-005 (lỗi hệ thống chung); MSG-085 khi thiếu field 1/3/8/10/11/12 — cảnh báo inline ngay tại field thiếu.
- **Success**: MSG-086 (Toast) khi Save thành công. Nếu field 6 (`staffName`) có giá trị và trùng khớp với 1 bản ghi khác cùng mức ≥ MEDIUM trong 90 ngày gần nhất → thêm MSG-088 (banner cảnh báo nghi ngờ tái diễn, best-effort, không chặn lưu — BR-187). Điều hướng tiếp sang `24_RecordViolationResolution.md` như luồng cũ, không đổi.

#### Platform Layout — Web

Dialog 1 cột, thứ tự đúng như bảng field ở trên. Field 7 (chức vụ nhân viên) chỉ render khi field 6 có giá trị (client-side, không cần gọi API). Ô "Nội dung biên bản" (field 11) chiếm toàn bộ chiều rộng dialog, chiều cao tối thiểu ~200px, có toolbar định dạng cố định ngay phía trên ô (bold/italic/underline, cỡ chữ, bảng, căn lề — theo đúng kiểu toolbar người dùng đã minh họa), câu tự sinh từ `occurredAt` hiển thị dạng text tĩnh ngay trên đầu ô, không nằm trong vùng chỉnh sửa được. Nút Save Violation cuối form; nút Xuất Biên bản (PDF) xuất hiện cạnh Save Violation sau khi lưu thành công.

#### Platform Layout — Mobile

Trang riêng full-screen, camera (field 12) ưu tiên đặt đầu form theo quy ước cũ của dự án. **Lưu ý cần xác nhận thêm**: toolbar định dạng rich text đầy đủ (bảng, căn lề, màu chữ) trên màn hình mobile hẹp có thể không khả thi về mặt UX như trên Web — đề xuất rút gọn toolbar mobile chỉ còn đậm/nghiêng/gạch chân + danh sách gạch đầu dòng, đội dự án/dev xác nhận trước khi code thay vì tôi tự quyết định cắt bớt tính năng.

#### AI Generation Prompt ⭐

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
nút Save Violation, sau khi lưu hiện thêm nút Xuất Biên bản (PDF).
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM).
Trạng thái cần thể hiện: loading, error (MSG-085 tại field thiếu), success (MSG-086, cảnh báo MSG-088
nếu có Nhân viên liên quan và nghi ngờ tái diễn).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm toggle chọn "loại vi phạm" —
BR-183/BR-184 được suy ra tự động từ việc có điền "Nhân viên liên quan" hay không.
```

### 10.4. Cơ chế phân loại BR-183 vs BR-184 (không còn toggle thủ công)

- Field 6 (`staffName`) để trống → bản ghi chỉ chịu **BR-183** (vi phạm quy trình/sản phẩm) — không có cá nhân để áp kỷ luật, field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` **không hiện**.
- Field 6 có giá trị → kích hoạt thêm **BR-184/185** (kỷ luật cá nhân — field "Mức độ hình thức kỷ luật" hiện ra ở màn 24) và **BR-187** (cảnh báo nghi ngờ tái diễn — so khớp theo đúng chuỗi `staffName` trong 90 ngày gần nhất). Field 9 (`violationType` = `OVERRIDE_ABUSE`) vẫn có thể chọn độc lập với field 6 — phục vụ riêng cho báo cáo BR-190.
- Field 3 (`caterersRepresentativeId`) và field 1/10/11/12 luôn bắt buộc bất kể field 6 có điền hay không — đây là phần "khung biên bản" chung cho mọi trường hợp.

### 10.5. Lịch sử: việc từng "chờ đợt sau" — nay đã xong (xem §10.6)

Mục này (nội dung gốc 07/07/2026) từng liệt kê 6 việc (a)–(f) là "chưa làm, chờ đợt sau" vì lúc đó người dùng yêu cầu tạm giữ nguyên Screen Spec trong khi thiết kế còn có thể đổi. Toàn bộ 6 việc đó **đã hoàn tất 11/07/2026** — xem bảng đầy đủ ở §10.6 ngay dưới đây (bảng #31 cũ đã cập nhật trạng thái ✅ Xong).

### 10.6. Trạng thái áp dụng cuối cùng (11/07/2026) — danh sách đầy đủ file đã cập nhật

| # | Việc cần làm | Vị trí | Trạng thái |
| --- | --- | --- | --- |
| 31 | Viết lại `22_RecordStaffViolation.md` (Web+Mobile, MedicalStaff) thành spec dialog hợp nhất (UC-179 sống sót); đổi `21_RecordProcessViolation.md` (Web+Mobile) thành file tham chiếu ngắn, UC-178 `(Deprecated 11/07/2026)` | `05_ScreenSpecifications/{Web,Mobile}/MedicalStaff/21_*.md`, `22_*.md` | ✅ Xong |
| 32 | Đồng bộ điều kiện field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` theo cơ chế `staffName` tự động phân loại | `24_RecordViolationResolution.md` | ✅ Xong (đã đúng cơ chế theo §10.4 từ đợt BR-211) |
| 33 | Đồng bộ 2 file `00_TongHop_MedicalStaff.md` (Web+Mobile) — thay khối Record Process/Staff Violation cũ bằng dialog hợp nhất | `05_ScreenSpecifications/{Web,Mobile}/MedicalStaff/00_TongHop_MedicalStaff.md` | ✅ Xong |
| 34 | Cập nhật `traceability_matrix.md`: UC-178 Deprecated → trỏ UC-179; BR-183/BR-211 cập nhật ghi chú | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` | ✅ Xong |
| 35 | Sửa `business_rules.md` BR-211: bỏ bullet `witnessId`, đổi tiêu đề PDF "BIÊN BẢN SỰ VIỆC" → "BIÊN BẢN VI PHẠM" | `business_rules.md` §18 | ✅ Xong |
| 36 | Rà 2 ScreenFlow Medical Staff + rà toàn bộ ScreenFlow liên quan actor khác xem có còn mô tả 2 dialog riêng | `03_Medical_Staff_Screen_Flow.md`, `08_Medical_Staff_Screen_Flow_Web.md`, `04_Service_Caterers_Screen_Flow.md`, `09_Service_Caterers_Screen_Flow_Web.md`, `02_School_Administration_Screen_Flow.md`, `00_Screen_Flow_Overview.md`, `14_Screen_Authorization_Matrix.md` | ✅ Xong |
| 37 | **[Phát hiện thêm ngoài phạm vi 6 việc gốc]** Service Caterers cũng là actor dùng chung dialog — `ServiceCaterers/36_RecordProcessViolation.md` (Web+Mobile) và `00_TongHop_ServiceCaterers.md` (Web+Mobile) chưa từng được cập nhật theo gộp dialog | `05_ScreenSpecifications/{Web,Mobile}/ServiceCaterers/36_*.md`, `00_TongHop_ServiceCaterers.md` | ✅ Xong (viết lại 11/07/2026) |
| 38 | **[Phát hiện thêm]** `class_diagram_description.md` vẫn còn field `witnessId` trên `NonConformanceRecord` và `StaffViolationRecord` dù đã quyết định bỏ hẳn | `01_Diagrams/01_Class_Diagram/class_diagram_description.md` §2.13 | ✅ Xong (gỡ field, ghi chú lý do) |
| 39 | **[Phát hiện thêm]** `MedicalStaff_Role_and_Workflows.md` §4.5, `MedicalStaff_UserStory.md` US-17, `error_message.md` MSG-095 vẫn mô tả luồng 2-dialog cũ | `02_Requirements/MedicalStaff_Role_and_Workflows.md`, `MedicalStaff_UserStory.md`, `00_BusinessRules/error_message.md` | ✅ Xong |
| 40 | **[Phát hiện thêm]** 3 file Prototype status-tracking (`Mobile_MedicalStaff_Prototype.md`, `Mobile/Web_ServiceCaterers_Prototype.md`) còn nhãn node cũ | `03_Design/07_Prototype/*.md` | ✅ Xong |

**Còn tồn đọng, CHƯA xử lý (khác phạm vi tài liệu thiết kế văn bản)**:

| # | Việc còn hở | Vị trí | Lý do chưa làm |
| --- | --- | --- | --- |
| 41 | Wireframe HTML (`06_Wireframes/{Web,Mobile}/ServiceCaterers/32-33_35-37_CorrectiveActionsViolations.html`) vẫn vẽ UI cũ có field "Chọn Nhân chứng" bắt buộc | `06_Wireframes/...` | Cần redesign trực quan (không chỉ sửa chữ) — ngoài phạm vi cập nhật tài liệu, cần xác nhận trước khi làm |
| 42 | Các field mới của BR-211 (`locationOfIncident`, `caterersRepresentativeId`, `occurredAt`, `violationContent`, `signedRecordPhotoUrl`) **chưa từng được thêm vào `class_diagram_description.md`** — kể cả trước khi có quyết định gộp dialog (06/07/2026) | `class_diagram_description.md` §2.13 | Gap có sẵn từ trước, lớn hơn phạm vi gộp dialog, cần xác nhận riêng trước khi sửa class diagram chính thức |
| 43 | Áp thiết kế dialog hợp nhất vào **code thật** (`conduct-inspection`, `violations/page.tsx` của Medical Staff & Service Caterers, `mock-data.ts`) | `03_Design/08_FrontEnd/Web/src/...` | ✅ **Phần Medical Staff xong 12/07/2026** (`violations/page.tsx` viết lại theo §10.3b: 1 dialog, không toggle, không field nhân chứng, staffName optional → progressive staffPosition, severity/violationType Select không preselect, rich text nội dung + câu tự sinh từ occurredAt, PDF "BIÊN BẢN VI PHẠM" với cột ký thứ 3 trống tĩnh, phân loại BR-183/184 tự động, resolution chỉ hiện kỷ luật khi có staffName; gate BR-211/MSG-095 giữ nguyên; toolbar rich text mock đơn giản — bản thật dùng editor đầy đủ; `violationType` dùng đúng 4 giá trị chờ xác nhận #44, bỏ "Khác"). Còn lại: bản Service Caterers — ngoài phạm vi Medical Staff |
| 44 | (i) danh sách cố định 4 giá trị của `violationType` — giữ nguyên hay đổi; (ii) mức độ rút gọn toolbar rich text trên Mobile | Screen Spec 22 (Web+Mobile) | Cần đội dự án/người dùng xác nhận, đã flag rõ trong Screen Spec, không tự quyết định thay |
| 45 | **Nhóm E (Food Sample Retention)** — "người ủy quyền" nên tự động = user đăng nhập (readonly) thay vì nhập tay; mở rộng "Phòng lab đích" thành chuỗi chain-of-custody đầy đủ (tương tự cách BR-211 làm cho Violation) | `14_RetrieveFoodSampleRecordsDialog.md`, có thể cần BR mới tương tự BR-211 | **Chưa thảo luận/quyết định gì** — mới chỉ được ghi nhận là 1 trong 7 nhóm phản hồi ảnh chụp màn hình, chưa đi vào chi tiết |

Mục #30 (Mục 4, mô tả việc lập trình code) vẫn đang phản ánh **thiết kế 2-dialog cũ** — nếu/khi triển khai việc #43 ở trên (áp vào code thật), cần đối chiếu lại #30 trước để khớp đúng thiết kế 1-dialog mới, tránh code theo bản đặc tả đã lỗi thời.

## 11. Quyết định 11/07/2026 — Export theo khoảng ngày đang chọn (rà soát ảnh chụp màn hình, "Nhóm D")

Người dùng chỉ ra nút "Export"/"Xuất báo cáo" ở 2 màn hình dưới đây không nói rõ có tôn trọng bộ lọc khoảng ngày đang áp dụng trên màn hình hay không, dễ khiến người dùng hiểu nhầm là luôn xuất toàn bộ dữ liệu bất kể filter. Quyết định: **ghi chú rõ hành vi đúng (xuất theo đúng khoảng ngày/dữ liệu đang hiển thị theo filter hiện tại), không tự đặt ra giá trị mặc định (VD "30 ngày gần nhất") vì không có căn cứ trong tài liệu gốc.**

- `Web/MedicalStaff/15_ViewFoodSafetyReportsScreen.md` — field 7 (nút Export): ghi chú xuất đúng theo khoảng ngày đang chọn ở field 2 (Date range filter), không xuất toàn bộ dữ liệu bất kể filter.
- `Web/MedicalStaff/13_FoodSampleRecords.md` — field 4 (nút Export): ghi chú tương tự, tham chiếu field 1.

> **Đã áp dụng vào code 12/07/2026**: `reports/page.tsx` bổ sung Date range filter (spec 15 field 2 — trước đó code chưa có) + bảng Monthly Kitchen Audits lọc theo khoảng ngày, Export toast nêu rõ khoảng đang xuất; `samples/page.tsx` Export toast nêu rõ filter hiện tại. Không đặt giá trị mặc định nào.

**Phát hiện phụ (chưa xử lý, để riêng vì ngoài phạm vi Nhóm D)**: bản Web standalone và bản Mobile TongHop của "View Food Safety Reports" không khớp cấu trúc field với nhau — bản Mobile TongHop có thêm 1 field "Date range selector riêng" (field 5) cho khối Compliance Summary mà bản Web standalone không có. Lỗi tồn tại từ trước, không liên quan Nhóm D.

## 12. Xác nhận 11/07/2026 — Giữ nguyên hành vi đóng "Không liên quan ATTP" (rà soát ảnh chụp màn hình, "Nhóm F1")

Người dùng đặt câu hỏi liệu hành động "Đóng — Không liên quan ATTP" ở Incident Triage nên áp dụng cho **1 dòng báo cáo tại 1 thời điểm** hay cho phép chọn nhiều dòng cùng lúc. Sau khi rà lại `review_medicalstaff_incidentmanagement.md` (bảng hành động §7.x, dòng "Đóng — Không liên quan ATTP" yêu cầu "Đúng 1 dòng được chọn"), người dùng xác nhận **giữ nguyên hành vi hiện có (1 dòng/lần), không đảo ngược thành chọn nhiều dòng**. Không có file thiết kế nào cần sửa — quyết định này chỉ mang tính xác nhận lại thiết kế đã đúng từ trước.

## 13. Quyết định 12/07/2026 — Bổ sung thông tin bàn giao mẫu ở UC-076 (rà soát ảnh chụp màn hình, "Nhóm E")

### 13.1. Vấn đề người dùng nêu ra

Người dùng chỉ ra 2 điểm ở `14_RetrieveFoodSampleRecordsDialog.md` (UC-076): (1) field "Tên người ủy quyền" đang là Input tự do — nên tự động lấy từ tài khoản đăng nhập thay vì cho gõ tay; (2) field "Phòng lab đích" nên mở rộng thành 1 cấu trúc chain-of-custody đầy đủ hơn, tương tự cách đã làm cho BR-211.

### 13.2. Nghiên cứu căn cứ trước khi thiết kế

Đọc lại UC-076 gốc (`Report 3.md`, dòng 3757–3771), BR-113/BR-114, và toàn bộ `00_Policies/` — không thấy tài liệu nào mô tả cụ thể cấu trúc "phòng lab đích". Người dùng yêu cầu tìm xem có văn bản chính thức nào của Bộ Y tế quy định việc gửi mẫu thực phẩm đi xét nghiệm không. Đã tìm và xác nhận: **Thông tư 14/2011/TT-BYT** (Bộ Y tế, 01/04/2011, "Hướng dẫn chung về lấy mẫu thực phẩm phục vụ thanh tra, kiểm tra chất lượng, vệ sinh an toàn thực phẩm") — toàn văn lấy từ Công báo Chính phủ (`vbpl.vn`), lưu tại `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md`. Thông tư có **Phụ lục V — Biên bản bàn giao mẫu**, đúng là văn bản chain-of-custody chính thức khi bàn giao mẫu cho đơn vị kiểm nghiệm: 2 bên ký (bên giao/bên nhận), có field "Đại diện là" (người đại diện mỗi bên), "Chỉ tiêu yêu cầu kiểm nghiệm", "Căn cứ đánh giá".

### 13.3. Quyết định — không rập khuôn hoàn toàn theo BR-211

Khác với khuyến nghị ban đầu (áp dụng y hệt pattern BR-211: in – ký 2 bên – chụp ảnh – gate chặn cứng), quyết định cuối cùng là **chỉ bổ sung field dữ liệu khớp Phụ lục V, không thêm bước ký/chụp ảnh/gate cứng**, vì 2 khác biệt bối cảnh quan trọng so với BR-211:

1. **Bên nhận khác bản chất**: ở BR-211, "Nhà thầu" (Service Caterers) là actor có tài khoản thật trong SFMS — ký/chụp ảnh có ý nghĩa vì cả 2 bên đều thao tác trên cùng hệ thống. "Phòng lab đích" (CDC Hà Nội, Viện Kiểm nghiệm ATVSTP Quốc gia...) là bên thứ ba hoàn toàn ngoài hệ thống, không có tài khoản, không thao tác trên SFMS.
2. **Áp lực thời gian khác nhau**: UC-076 diễn ra giữa lúc đang điều tra 1 sự cố ATTP đang mở — thêm gate cứng có nguy cơ làm chậm việc gửi mẫu đi xét nghiệm, trong khi BR-211 (lập biên bản vi phạm) không có áp lực thời gian tương tự.

### 13.4. Thiết kế đã áp dụng

- **BR-114** (sửa): field "Tên người ủy quyền" (`authorizedBy`) đổi từ Input tự do sang **tự động = tài khoản Medical Staff đang đăng nhập, readonly** — nhất quán với `reportedBy` (BR-183) và "Đại diện Y tế trường học" (BR-211). Ghi chú thêm: code mock hiện tại (`samples/page.tsx`) đã prefill sẵn `authorizer = "Ngô Thị Hà (Y tế)"` nhưng vẫn để editable — chính là bằng chứng cho thấy ý định ban đầu đã ngầm định "= user đăng nhập", chỉ chưa khóa lại thành readonly.
- **BR-212** (mới): bổ sung field "Đại diện phòng lab nhận mẫu" (tùy chọn — có thể chưa xác định tại thời điểm thu hồi) và "Chỉ tiêu yêu cầu kiểm nghiệm" (bắt buộc), khớp đúng Phụ lục V. Biên nhận in ra (đã có sẵn) mở rộng nội dung tương ứng.
- Đã cập nhật: `business_rules.md` (BR-114 sửa, BR-212 mới, changelog v1.6), `traceability_matrix.md` (UC-076, BR-212), `14_RetrieveFoodSampleRecordsDialog.md` (Web+Mobile), `00_TongHop_MedicalStaff.md` (Web+Mobile).
- ~~Chưa sửa code~~ → **Đã áp dụng vào code 12/07/2026** (`samples/page.tsx`): "Người ủy quyền" tự động = tài khoản đăng nhập, readonly (BR-114 sửa); thêm "Đại diện phòng lab nhận mẫu" (tùy chọn) + "Chỉ tiêu yêu cầu kiểm nghiệm" (bắt buộc khi thu hồi vật lý) theo BR-212; payload/biên bản bàn giao mở rộng (`FoodSample.labRepresentative/testCriteria`); KHÔNG thêm gate ký/chụp ảnh — đúng §13.3.

## 14. Quyết định 12/07/2026 — Đồng bộ "nhật ký kiểm soát côn trùng" + gỡ BR-089 vào Screen Spec báo cáo (rà soát ảnh chụp màn hình, đợt 7 ảnh mới)

### 14.1. Vấn đề người dùng nêu ra

Từ ảnh chụp code thật (`reports/page.tsx`, `send-compliance-report-dialog.tsx`, `compliance/page.tsx`): (1) tab "Pest Control Log" ở `reports/page.tsx` vẫn hiển thị badge nhị phân "Còn hiệu lực"/"Hết hạn" (field `p.validUntil`/`p.status`) — trong khi quyết định đổi sang model "nhật ký sự kiện" đã chốt từ trước (`review_medicalstaff_compliance_kitcheninspection.md` Mục 1) và đã áp cho `compliance/page.tsx`, nhưng CHƯA áp cho `reports/page.tsx` lẫn `send-compliance-report-dialog.tsx`; (2) BR-089 (Deprecated theo `business_rules.md` Changelog v1.6) vẫn còn xuất hiện trong mô tả/tab label/card title/chú thích cảnh báo IoT ở nhiều nơi trong code lẫn Screen Spec.

### 14.2. Đối chiếu — đây là lỗi lan truyền quyết định cũ, không phải quyết định mới

Cả 2 điểm đều là quyết định **đã chốt trước đó** (BR-089 Deprecated: task #37, 11/07/2026; nhật ký côn trùng: `review_medicalstaff_compliance_kitcheninspection.md` Mục 1) nhưng chưa được lan truyền đầy đủ sang mọi file Screen Spec liên quan. Không cần xác nhận lại nghiệp vụ — chỉ cần đồng bộ tài liệu thiết kế cho khớp quyết định đã có (đúng phạm vi "Chỉ tài liệu thiết kế trước" người dùng đã chọn cho đợt 7 ảnh này).

### 14.3. Đã áp dụng vào tài liệu thiết kế

- `15_ViewFoodSafetyReportsScreen.md` (Web+Mobile): gỡ mọi tham chiếu BR-089 (BR liên quan, field 1/3/5, AI Prompt, Nguồn dữ liệu — nêu rõ "BR-089 Deprecated — Changelog v1.6"); thêm field 4b "Nhật ký kiểm soát côn trùng" (nhóm Pest Control Log) với 5 cột đúng model Mục 1 (Ngày kiểm soát/xử lý, Đơn vị thực hiện, Loại côn trùng phát hiện + số lượng ước tính, Biện pháp xử lý, Ngày hẹn kiểm tra tiếp theo), thay cho badge cũ.
- `28_SendComplianceReportDialog.md` (Web+Mobile): field 5 đổi từ "Tình trạng hiệu lực chứng nhận kiểm soát côn trùng" (badge) sang "Kiểm soát côn trùng — bản ghi gần nhất trong kỳ" (card tóm tắt: ngày xử lý gần nhất, loại côn trùng nếu có, ngày hẹn tiếp theo).
- `00_TongHop_MedicalStaff.md` (Web+Mobile): đồng bộ cả 2 khối trên (embedded content khớp bản standalone).
- Giữ nguyên mã **BR-102** cho kiểm soát côn trùng (chưa cấp mã BR mới thay thế — đúng note "chờ xác nhận" ở `review_medicalstaff_compliance_kitcheninspection.md` §7; chỉ đổi nội dung/model hiển thị, không đổi mã).
- **Phạm vi đợt này**: chỉ tài liệu thiết kế (`Chỉ tài liệu thiết kế trước` — người dùng đã chọn). ~~Chưa sửa code~~ → **ĐÃ ÁP CODE 14/07/2026, `tsc --noEmit` sạch**: `reports/page.tsx` — tab Pest Control Log chuyển sang bảng nhật ký sự kiện 5 cột (đúng field 4b spec 15), dùng resource `pest-control-log` (bỏ `PEST_CONTROL_LOGS` cũ có validUntil/status); gỡ mọi trích dẫn BR-089 (tab label, card title, description, chú thích ">30 phút" đổi thành "đo thủ công theo ca" đúng BR-110 sửa 11/07). `send-compliance-report-dialog.tsx` — field 5 đổi từ badge hiệu lực sang card "bản ghi gần nhất" (ngày, loại côn trùng nếu có, ngày hẹn tiếp theo), payload gửi kèm `latestPestControl` thay `pestControlValid`.
- **Chưa xử lý trong Mục này** (thuộc task #59, #60 riêng): filter "Bước (phụ)"/"Món" ở `11_FoodInspectionRecords.md`; card "Nhiệt độ kho & tủ lưu mẫu (BR-089/BR-110)" ở `23_ComplianceDashboard.md`.

## 15. Quyết định 12/07/2026 — Bỏ filter "Bước" và "Món" khỏi Food Inspection Records (rà soát ảnh chụp màn hình, Hình 5)

### 15.1. Vấn đề người dùng nêu ra

Từ ảnh chụp `inspections/page.tsx`: màn "Hồ sơ kiểm thực ba bước" (UC-112) hiện có cả filter "Bước (phụ)" (kèm filter con "Trạng thái bước" khi chọn 1 bước cụ thể) và filter "Món" bên cạnh filter chính "Kết quả". Người dùng yêu cầu bỏ cả 2 filter này.

### 15.2. Đối chiếu với quyết định trước

Quyết định 09/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 5) chỉ xác định **"Kết quả"** là filter chính thay "Bước kiểm tra", đồng thời cho phép (không bắt buộc) **giữ** filter "Món" và filter phụ theo "Bước" nếu không tốn công triển khai — đội phát triển đã chọn giữ cả hai khi lên code. Quyết định hôm nay (12/07/2026) là **thu hẹp lại**: bỏ hẳn 2 filter tùy chọn đó, chỉ còn Date range + Kết quả — vì modal "Xem chi tiết" đã hiển thị đầy đủ dữ liệu theo từng bước (Mẫu 1/2/3, xem Mục 6), nên filter theo Bước không còn cần thiết ở tầng danh sách; filter Món cũng được đánh giá là không cần thiết ở màn này.

### 15.3. Thiết kế đã áp dụng

- `11_FoodInspectionRecords.md` (Web+Mobile): field 2 "Filter theo Kết quả" (giữ nguyên từ Mục 5); loại bỏ hoàn toàn field cũ "Filter theo bước kiểm tra"/"Filter theo thực đơn/món" khỏi bảng field, ghi rõ trong khối "Đã bỏ (12/07/2026)"; cập nhật Platform Layout, AI Generation Prompt (thêm dòng cấm rõ "KHÔNG có filter Bước, KHÔNG có filter Món"), Nguồn dữ liệu.
- `00_TongHop_MedicalStaff.md` (Web+Mobile): đồng bộ khối embedded tương ứng.
- **Phạm vi đợt này**: chỉ tài liệu thiết kế. ~~Chưa sửa code~~ → **ĐÃ ÁP CODE 14/07/2026**: `inspections/page.tsx` bỏ hẳn filter "Bước (phụ)" + "Trạng thái bước" + "Món" (kèm state/param/derivation liên quan) — chỉ còn Date range + Kết quả; cột "Món chính" trong bảng giữ nguyên (Mục 15 chỉ bỏ filter, không bỏ cột).

## 16. Quyết định 12/07/2026 — Không đưa card "Nhiệt độ kho & tủ lưu mẫu (BR-089/BR-110)" vào đặc tả Compliance Dashboard (rà soát ảnh chụp màn hình, Hình 6)

### 16.1. Vấn đề người dùng nêu ra

Từ ảnh chụp `compliance/page.tsx`: màn Compliance Dashboard hiện có 1 card "Nhiệt độ kho & tủ lưu mẫu (BR-089/BR-110)" (dữ liệu `TEMP_SENSORS`: Kho khô/Kho lạnh gắn BR-089/MSG-055, Tủ lưu mẫu gắn BR-110/MSG-059). Người dùng yêu cầu bỏ hẳn mục này vì liên quan BR-089 đã Deprecated.

### 16.2. Đối chiếu với đặc tả gốc — phát hiện đây là scope creep ở code, không phải quyết định cần đảo ngược

Đối chiếu `23_ComplianceDashboard.md` (Web+Mobile, cả bản gốc lẫn sau khi cập nhật 05/07/2026): danh sách field chưa từng có card nhiệt độ kho/tủ lưu mẫu — 5 field hiện tại chỉ gồm (1) trạng thái 3 bước, (2) điểm checklist, (3) ảnh xác minh, (4) cảnh báo non-conformance, (4b) cảnh báo điểm thấp, (5) nút xem chi tiết. Card nhiệt độ là phần **phát sinh khi triển khai code, chưa từng qua đặc tả** — đúng nguyên tắc "không hư cấu yêu cầu" của dự án, không được coi đây là 1 field hợp lệ chỉ vì đã có trong code.

### 16.3. Quyết định

**Không bổ sung** card này vào đặc tả Compliance Dashboard, dù dưới bất kỳ hình thức nào (kể cả đổi sang BR-110 only) — vì: (1) sẽ trùng lặp dữ liệu đã có đúng chỗ ở `15_ViewFoodSafetyReportsScreen.md` (nhóm báo cáo "Refrigerator Temperature Logs", đúng BR-110); (2) Compliance Dashboard theo đặc tả gốc tập trung vào tình trạng kiểm thực 3 bước + checklist vệ sinh trong ngày, không phải nơi theo dõi nhiệt độ thiết bị.

### 16.4. Đã áp dụng

- `23_ComplianceDashboard.md` (Web+Mobile) + `00_TongHop_MedicalStaff.md` (Web+Mobile): thêm ghi chú "Quyết định 12/07/2026" giải thích rõ card này không thuộc đặc tả và không được thêm vào.
- **Phạm vi đợt này**: chỉ tài liệu thiết kế. ~~Chưa sửa code~~ → **ĐÃ ÁP CODE 14/07/2026**: `compliance/page.tsx` gỡ hẳn card "Nhiệt độ kho & tủ lưu mẫu" + hằng `TEMP_SENSORS` + 2 khối Alert MSG-055/MSG-059 + mọi tham chiếu BR-089 trong description/header comment; card kiểm soát côn trùng (BR-102 nhật ký sự kiện) + khám SK NV bếp (BR-103) giữ nguyên theo Mục 1 review compliance.

## 17. Quyết định 13/07/2026 — "ATTP Report Center": mở rộng UC-185 thành 3 loại báo cáo + màn lịch sử (Hình 7)

### 17.1. Vấn đề người dùng nêu ra

Người dùng đặt 2 câu hỏi độc lập: (1) yêu cầu thiết kế lại field của `28_SendComplianceReportDialog.md` (UC-185) chi tiết hơn, có nút xuất PDF cho School Administration, theo hướng "Medical Staff tổng hợp lại thông tin ATTP mình quản lý rồi viết report gửi lên"; (2) nhận định Medical Staff đang thiếu 1 trang để theo dõi lại các report mình đã gửi cho School Administration, và yêu cầu phản biện ý kiến này.

### 17.2. Phản biện ý kiến "thiếu trang lịch sử" — xác nhận là khoảng trống thật

Đối chiếu quy trình hiện tại của UC-185: sau khi gửi thành công chỉ có Toast (MSG-091) rồi đóng dialog, không còn nơi nào xem lại nội dung đã gửi. `BR-170` (audit log) chỉ là audit trail kỹ thuật, không phải màn hình nghiệp vụ để đọc lại report. Kiểm tra `14_Screen_Authorization_Matrix.md`: màn "Audit Logs" (UC-106) hiện chỉ System Admin + School Administration có quyền (cột MS trống) — không thể "mượn" màn đó cho Medical Staff mà không mở thêm quyền, và Audit Logs hiển thị Timestamp/User/Action/Resource, không tối ưu để đọc lại nội dung report. Tìm được tiền lệ đúng hướng hơn trong dự án: **UC-107 "View Batch History"** (Raw Food Supplier) — 1 màn lịch sử riêng theo actor, không dựa vào Audit Logs chung. Kết luận: đồng ý với người dùng, xác nhận đây là khoảng trống thật, thiết kế theo mẫu UC-107.

### 17.3. Đối chiếu nội dung report mở rộng — tránh trùng lặp 2 cơ chế đã có sẵn

Khi đối chiếu đề xuất "chia 3 nhóm báo cáo" của người dùng (Giám sát tuân thủ / Sự cố / Vi phạm) với các màn đã có, phát hiện 2 điểm quan trọng cần xử lý trước khi thiết kế:

1. **UC-033 "Generate Incident Report Dialog"** (`20_GenerateIncidentReportDialog.md`) đã tự động sinh báo cáo chi tiết cho **từng sự cố** (clinical notes, kết quả xét nghiệm mẫu, root cause, corrective action) và gửi PDF cho School Administration ngay khi đóng sự cố (BR-154). Nếu báo cáo tổng hợp sự cố theo kỳ lại nhập/liệt kê lại toàn bộ nội dung này sẽ tạo ra 2 nguồn dữ liệu cùng mô tả 1 sự việc — rủi ro lệch nhau khi 1 bên sửa mà bên kia không cập nhật.
2. **BR-211** đã cấp cơ chế xuất PDF biên bản vi phạm (ký 3 bên) ngay khi lập **từng** vi phạm. Hệ thống cũng đã có sẵn `27_ViolationRegistry.md` (UC-181) — Medical Staff đã có quyền xem (Secondary Actor). Phát hiện thêm: màn này **chưa có Export**, khác với các màn danh sách khác đã có sẵn (`25_IncidentList.md`, `15_ViewFoodSafetyReportsScreen.md`) — khoảng trống nhất quán cần vá.

**Nguyên tắc thiết kế chốt** (người dùng xác nhận qua AskUserQuestion): báo cáo tổng hợp Sự cố/Vi phạm chỉ **rollup số liệu + điều hướng**, KHÔNG lặp lại nội dung chi tiết đã có ở UC-033/BR-211 — tránh trùng lặp dữ liệu, giữ đúng tinh thần "ưu tiên nhất quán toàn dự án".

### 17.4. Kiến trúc đã chốt (qua 2 vòng AskUserQuestion)

- **UC-187 "ATTP Report Center"** (hub mới): 1 điểm vào chung, 3 card hành động dẫn tới 3 dialog soạn báo cáo, cộng nút "Xem lịch sử báo cáo đã gửi". Không thay thế các điểm vào cũ (`23_ComplianceDashboard.md` vẫn còn nút "Gửi báo cáo tuân thủ" như cũ).
- **UC-188 "Send Incident Summary Report"** (mới, BR-213): rollup số liệu sự cố theo kỳ (Daily/Weekly) từ `25_IncidentList.md` — đếm theo trạng thái/mức độ, danh sách mã sự cố + link `26_IncidentDetail.md`, KHÔNG có nội dung điều tra chi tiết. Tuân thủ BR-181 (ẩn danh học sinh) — đánh dấu "Confidential".
- **UC-189 "Send Violation Summary Report"** (mới, BR-214): rollup số liệu vi phạm theo kỳ từ `27_ViolationRegistry.md` — đếm theo loại/mức độ, danh sách vi phạm + link biên bản PDF đã ký (nếu có), KHÔNG lặp nội dung biên bản. Đồng thời bổ sung field "Filter theo khoảng ngày" + "Nút Export" vào `27_ViolationRegistry.md` (vá khoảng trống phát hiện ở §17.3 mục 2).
- **UC-190 "View Sent Reports History"** (mới, **không cấp BR mới** — hiện thực hóa **BR-177** đã có sẵn từ trước trong `business_rules.md` §16, quy định lưu trữ báo cáo tối thiểu 12 tháng + audit trail mỗi lần xuất): danh sách mọi lần gửi cả 3 loại, filter theo loại/khoảng ngày, nút "Tải lại PDF" mỗi dòng — tải lại đúng file đã sinh, không tính lại số liệu (đúng BR-182, toàn vẹn dữ liệu báo cáo).
- Nút "Xuất PDF" độc lập (đề xuất ban đầu của người dùng cho Hình 7) — **bị loại bỏ theo phản hồi của chính người dùng** khi được hỏi lại ("liệu gửi báo cáo có cần thiết phải làm theo xuất pdf kiểu như vậy không, hay nên làm theo dạng tổng hợp data đã kiểm tra"): thay vì 1 nút export tách rời, dialog hiển thị đầy đủ dữ liệu đã tổng hợp (rollup) để Medical Staff soát lại trước khi gửi; PDF vẫn tự sinh lúc bấm "Gửi báo cáo" như cơ chế BR-178/BR-209 hiện có — không đổi. Nút "Tải lại PDF" chỉ xuất hiện ở màn Lịch sử (UC-190), đúng ngữ cảnh cần tải lại file đã có.

### 17.5. Đã áp dụng

- `business_rules.md`: Changelog v1.7; cấp **BR-213**, **BR-214** (§16); chú thích bổ sung vào **BR-177** (căn cứ UC-190) và **BR-209** (điểm vào hub mới); cập nhật bảng tóm tắt module.
- `error_message.md`: Changelog v1.3; cấp **MSG-096/097** (Error), **MSG-098/099** (Success); cập nhật bảng tóm tắt module.
- `traceability_matrix.md`: thêm hàng **UC-187/188/189/190** (bảng UC, §1.12) + ghi chú bối cảnh; cập nhật bảng BR↔UC cho BR-170/175/177/178/181, thêm hàng BR-213/214.
- Screen Spec mới (Web+Mobile): `29_ATTPReportCenter.md` (UC-187), `30_SendIncidentSummaryDialog.md` (UC-188), `31_SendViolationSummaryDialog.md` (UC-189), `32_SentReportsHistory.md` (UC-190).
- Screen Spec sửa: `27_ViolationRegistry.md` (Web+Mobile — thêm filter khoảng ngày + nút Export), `28_SendComplianceReportDialog.md` (Web+Mobile — bổ sung điểm vào từ hub, liên kết sang màn Lịch sử khi gửi thành công).
- `00_TongHop_MedicalStaff.md` (Web+Mobile): đồng bộ toàn bộ nội dung trên (bản rút gọn cho 4 màn mới, đồng bộ đầy đủ cho 2 màn sửa).
- **Phạm vi đợt này**: chỉ tài liệu thiết kế (UC/BR/MSG/Screen Spec/traceability), đúng "Chỉ tài liệu thiết kế trước" đã chọn từ đầu phiên rà soát 7 ảnh. ~~Chưa áp dụng vào code thật~~ → **ĐÃ ÁP DỤNG VÀO CODE 13/07/2026**: data model `SentReport` + resource `sent-reports` (thay `compliance-reports` — UC-185/188/189 cùng ghi vào 1 lịch sử, seed 1 bản ghi); route mới `report-center` (hub UC-187, 3 card + "Lần gửi gần nhất" theo loại) và `report-center/history` (UC-190, filter loại + khoảng ngày gửi, nút "Tải lại PDF" đúng file đã sinh); 2 component dialog mới `send-incident-summary-dialog` (UC-188 — rollup 3 nhóm trạng thái khớp Incident List + phân bố mức độ chỉ tính sự cố đã qua Step 1, bảng mã sự cố mở tab mới, badge Confidential BR-181, MSG-096/098) và `send-violation-summary-dialog` (UC-189 — rollup 3 loại BR-189 gồm đối tác từ `caterer-violations`, phân bố BR-186, "Xem biên bản" chỉ khi có PDF ký BR-211, MSG-097/099); helper kỳ báo cáo dùng chung `lib/report-period.ts`; sidebar thêm "Trung tâm Báo cáo ATTP" (mục "Báo cáo ATTP" cũ đổi nhãn "Xem báo cáo ATTP" để phân biệt viewer UC-115/117 với hub — quyết định đặt nhãn khi code, cần user xác nhận lại nếu muốn tên khác); `27_ViolationRegistry` code thêm filter khoảng ngày + nút Export theo filter (§17.3 mục 2); dialog UC-185 gửi xong toast trỏ sang màn Lịch sử.
- **⚠️ Kiểm tra lại 14/07/2026 — `tsc --noEmit` KHÔNG sạch**: chạy lại xác minh phát hiện **17 lỗi biên dịch** trên đúng 6 file vừa sửa cho tính năng này: `app/(app)/medical-staff/violations/page.tsx` (JSX fragment/Dialog không đóng thẻ, lỗi từ đoạn thêm filter khoảng ngày + Export §17.3 mục 2), `components/app/send-compliance-report-dialog.tsx` (Dialog/DialogContent không đóng thẻ), `lib/api/db.ts`, `lib/api/mock-server.ts`, `lib/mock-data.ts`, `lib/roles.ts` (đều lỗi cú pháp dạng thiếu dấu đóng `)`/`}`). Ghi chú "tsc sạch" ở trên là **không đúng với hiện trạng thực tế tại thời điểm kiểm tra lại** — cần một lượt sửa lỗi cú pháp riêng (đề xuất gộp cùng nhóm task #43) trước khi coi tính năng này là hoàn tất về code. Không sửa trực tiếp code trong lượt rà soát tài liệu này vì ngoài phạm vi được giao.
- **Còn lại**: ~~cập nhật `14_Screen_Authorization_Matrix.md`... và các file ScreenFlow liên quan~~ → **đã hoàn tất trong cùng đợt 13/07/2026** (xác minh lại 14/07/2026): `14_Screen_Authorization_Matrix.md` đã có 6 hàng mới (dòng 78-83, gồm cả UC-185/186 vốn bị thiếu từ trước); `03_Medical_Staff_Screen_Flow.md` và `08_Medical_Staff_Screen_Flow_Web.md` đã có changelog + sơ đồ mermaid cập nhật node UC-187/188/189/190. Việc còn lại thực sự: sửa 17 lỗi `tsc` nêu trên.
