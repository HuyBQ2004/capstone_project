# Review Report 3 vs Source Code — SFMS SEP490-G8

> **Ngày review:** 12/08/2026  
> **Reviewer:** Antigravity (AI Agent)  
> **Phạm vi:** `06_Document/01_Report/Report3_12_08_2026.md` ↔ `04_Development/Backend`, `04_Development/Frontend`, `04_Development/Mobile`, `04_Development/AiServices`

---

## Tóm tắt

Report 3 là tài liệu SRS (Software Requirement Specification) mô tả toàn bộ yêu cầu chức năng, workflow, use case và màn hình của hệ thống SFMS. Sau khi đối chiếu với source code hiện tại, review này liệt kê các phần **còn thiếu**, **mô tả sai**, hoặc **không khớp** giữa tài liệu và code thực tế.

---

## 1. Workflow — Các phần thiếu/sai so với code

### 1.1 Supplier Registration & Approval Workflow (Workflow 1)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W1-01 | **Thiếu Service Caterers onboarding workflow** | Report có Workflow 1 mô tả supplier registration. Tuy nhiên, code backend có cả `PartnershipApplicationController.java` (MF04) và `SupplierPartnershipController.java` (MF04), thể hiện 2 luồng riêng biệt: supplier ↔ caterer partnership. Workflow trong Report chỉ mô tả một luồng chung, không tách biệt 2 loại. |
| W1-02 | **Workflow bỏ qua bước Rate Limiter khi đăng ký** | Code có `RegisterRateLimiter.java` (MF02) và `SlidingWindowRateLimiter.java` (MF04) cho face recognition. Workflow trong report không đề cập đến rate limiting tại bước đăng ký nhà cung cấp. |
| W1-03 | **DeliveryPerson là entity riêng trong code, nhưng workflow không tách bước** | Code có `DeliveryPersonController.java` riêng (MF04). Workflow chỉ mô tả "Supplier submits application" mà không nêu bước tạo delivery person sau khi được phê duyệt. |

### 1.2 Ingredient Receiving Workflow (Workflow 2)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W2-01 | **Code có `ReceivingAuthorityController.java` (MF08) — không có trong workflow** | Workflow mô tả Medical Staff kiểm tra nhưng không đề cập đến việc **phân quyền nhận hàng** (ReceivingAuthority). Trong code, có entity này với vai trò kiểm soát ai được phép nhận hàng. |
| W2-02 | **Workflow thiếu bước LabRequest cho batch bị nghi ngờ** | Code có `SampleLabRequestController.java` (MF08) và `SampleLabelReservationController.java` (MF08). Workflow Ingredient Receiving không đề cập đến trường hợp gửi mẫu lab khi phát hiện batch nghi ngờ chất lượng tại khâu nhận hàng. |
| W2-03 | **`ReceivingSessionController.java` vs `SupplierBatchController.java`** | Code tách rõ: `ReceivingSession` là phiên nhận hàng, `SupplierBatch` là lô hàng từ nhà cung cấp. Workflow trong report hợp nhất 2 khái niệm này, dễ gây nhầm lẫn khi triển khai. |

### 1.3 Ingredient Storage & Inventory Management Workflow (Workflow 3)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W3-01 | **`TraceabilityController.java` (MF06) không được mô tả trong workflow** | Workflow mô tả "Record Batch Traceability" (Step 9) là action của System, nhưng trong code có endpoint riêng cho Traceability — không rõ actor nào trigger, điều kiện nào. Report không có use case riêng cho "View Traceability Map" dù backend đã implement. |
| W3-02 | **Workflow thiếu bước xử lý khi `InventoryTransaction` thất bại** | Code có `InventoryTransactionController.java` (MF06). Workflow chỉ mô tả happy path, không có alternative flow khi transaction inventory gặp lỗi (ví dụ: optimistic lock, concurrent update). |

### 1.4 Menu Planning & Approval Workflow (Workflow 4)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W4-01 | **`DietGroupController.java` (MF05) và `MenuTemplateController.java` (MF05) không có trong workflow** | Workflow mô tả Menu Planning theo flow: Draft → Submit → Approve. Không đề cập đến việc Service Caterers sử dụng **Menu Templates** làm điểm khởi đầu, hoặc **Diet Groups** để cấu hình nhóm ăn đặc biệt. Cả 2 controller đã tồn tại trong code. |
| W4-02 | **Workflow thiếu step "AI Recommendation"** | Code có `MenuRecommendationServiceImpl.java` và `MenuController` có endpoint `@PostMapping("/{code}/ai-suggest")`. Report Workflow 4 không có bước nào mô tả việc AI gợi ý thực đơn trong flow chính — chỉ mô tả tóm tắt trong UC-007 (AI recommendation). |
| W4-03 | **`DietCoverageValidator.java` và `AllergyConflictChecker.java` chạy tự động nhưng workflow không mô tả** | Code tự động validate khi submit menu (BR-063). Workflow chỉ có bước "Step X Passed?" chung, không mô tả rõ **allergy conflict check** và **diet coverage validation** chạy ở bước nào của flow. |

### 1.5 Ingredient Issue to Kitchen Workflow (Workflow 5)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W5-01 | **Workflow bỏ qua `PurchaseRequestController.java` (MF06)** | Workflow Ingredient Issue to Kitchen (Step 12-13) đề cập đến "Request Inventory Replenishment" nhưng không mô tả rằng việc tạo Purchase Request là một workflow riêng với supplier. Code có `PurchaseRequestController.java` đầy đủ. Report thiếu **Purchase Request Sub-workflow** trong phần này. |
| W5-02 | **FEFO logic không được mô tả rõ trong workflow** | Code service inventory phải xử lý FEFO (First Expired, First Out) khi issue nguyên liệu. Workflow chỉ mô tả "Verify Inventory Level" (Step 5) và "System deducts issued quantities" (Step 8) mà không đề cập đến logic chọn batch theo FEFO. |

### 1.6 Three-Step Food Inspection Workflow (Workflow 7)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W6-01 | **`KitchenInspectionController.java` (MF07) không có trong workflow** | Code có `KitchenInspectionController.java` (MF07) riêng biệt với `InspectionController.java`. Workflow Three-Step Food Inspection chỉ mô tả loại inspection cho bữa ăn (3 bước), không đề cập đến **Kitchen Inspection** (kiểm tra điều kiện bếp) vốn là entity/workflow riêng trong code. |
| W6-02 | **`ChecklistGroupController.java` (MF07) không khớp với workflow** | Workflow mô tả inspection theo 3 step cố định. Code có `ChecklistGroup` cho phép cấu hình dynamic checklist groups. Quan hệ giữa ChecklistGroup và 3 steps không được làm rõ trong report. |
| W6-03 | **`SafetySignoffController.java` (MF07) — thiếu trong workflow** | Workflow không có bước "Safety Sign-off" sau khi hoàn thành 3 bước. Code có endpoint này. Report không mô tả ai ký xác nhận cuối cùng sau inspection, dù code đã implement. |

### 1.7 Food Sample Retention Workflow (Workflow 8)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W7-01 | **`SampleLabRequestController.java` (MF08) xuất hiện trong code nhưng Workflow chỉ mô tả ở "Alternative Flow"** | Step 15 "Retrieve Food Sample" là alternative flow khi có incident. Nhưng Lab Request (gửi mẫu đi xét nghiệm) là một workflow riêng trong code (MF08) với `SampleLabRequestController` và `SampleLabelReservationController`. Workflow trong report không mô tả đủ lifecycle của lab request. |
| W7-02 | **`SampleLabelReservationController.java` (MF08) — thiếu hoàn toàn trong workflow** | Code có endpoint để **đặt trước số thứ tự nhãn mẫu** (SampleLabelReservation). Workflow trong report không đề cập đến việc nhãn mẫu phải được đặt trước (pre-reserve) trước khi lấy mẫu. |

### 1.8 Meal Serving Workflow (Workflow 10)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W8-01 | **`AllergyAlertController.java` (MF09) không được workflow mô tả như endpoint độc lập** | Workflow mô tả "system triggers allergy alert" như action tự động, nhưng code có endpoint riêng để query allergy alerts. Cần làm rõ flow: Teacher/Caterer chủ động query hay system push? |
| W8-02 | **`MealRecordController.java` (MF09) vs `MealHistoryController.java` (MF09) — 2 entity không phân biệt trong workflow** | Workflow chỉ mô tả chung "Record Meal" nhưng code tách: `MealRecord` (ghi nhận từng lượt phát cơm) và `MealHistory` (lịch sử tổng hợp). Workflow không mô tả rõ tại điểm nào MealRecord được tạo, khi nào được aggregate vào MealHistory. |
| W8-03 | **Workflow thiếu bước "Revoke" distribution** | Code có `POST /student-distributions/{code}/revoke` endpoint (MF09) cho phép hủy đánh dấu đã phát. Workflow không mô tả trường hợp Teacher revoke/undo một xác nhận đã thực hiện trong ngày. |

### 1.9 Food Safety Incident Handling Workflow (Workflow 11)

| # | Vấn đề | Mô tả |
|---|--------|-------|
| W9-01 | **`SourceReportController.java` (MF11) không có trong workflow** | Code có `SourceReportController.java` (MF11) riêng. Workflow không mô tả bước nào liên quan đến "Source Report" — bước xác định nguồn gốc vi phạm từ phía Teacher/Parent trước khi tạo incident chính thức. |
| W9-02 | **`IncidentRecordController.java` (MF11) vs `IncidentController.java` (MF11) — không phân biệt** | Code tách rõ `Incident` (sự kiện tổng thể) và `IncidentRecord` (các bản ghi chi tiết trong quá trình điều tra). Workflow chỉ mô tả "Incident" chung, không làm rõ lifecycle của IncidentRecord. |
| W9-03 | **`ViolationController.java` (MF11) — không có trong workflow 11** | Code có `ViolationController.java` (MF11) riêng cho quản lý vi phạm nhân viên. Workflow 11 (Food Safety Incident Handling) không đề cập đến việc tạo Violation từ incident — đây là một branch quan trọng trong flow thực tế. |
| W9-04 | **`CapaController.java` (MF11) — CAPA workflow chưa được làm rõ** | Workflow đề cập đến CAPA như là output cuối của incident investigation, nhưng code có `CapaController.java` với lifecycle phức tạp hơn (create, update, approve, reject). Report thiếu bước chi tiết cho CAPA lifecycle. |

---

## 2. Use Case — Các phần thiếu/sai/không khớp

### 2.1 Use Cases thiếu trong Report nhưng đã có code

| UC Thiếu | Module Code | Mô tả |
|----------|-------------|-------|
| **UC-???: View/Manage Kitchen Inspection** | `KitchenInspectionController.java` (MF07) | Report có UC-112 (View Food Inspection Records) và UC-124 (View Inspection Records) nhưng không có UC riêng cho Kitchen Inspection (kiểm tra điều kiện vệ sinh bếp). Code đã implement entity và controller riêng. |
| **UC-???: Manage Safety Signoff** | `SafetySignoffController.java` (MF07) | Không có use case mô tả quy trình ký xác nhận an toàn thực phẩm sau inspection. |
| **UC-???: View/Create Traceability Report** | `TraceabilityController.java` (MF06) | Không có UC tương ứng trong phần 3.2 Functional Specifications. Report đề cập "Traceability Reports" trong Screen Authorization table nhưng không có UC description. |
| **UC-???: Manage InventoryTransaction** | `InventoryTransactionController.java` (MF06) | Không có UC riêng. Chỉ được đề cập gián tiếp trong workflow. |
| **UC-???: View/Manage SupplierProduct** | `SupplierProductController.java` (MF06) | Không có UC mô tả việc supplier quản lý danh sách sản phẩm cung cấp. |
| **UC-???: Manage Source Report** | `SourceReportController.java` (MF11) | Không có UC mô tả việc ghi nhận nguồn báo cáo ban đầu (teacher/parent) trước khi tạo incident. |
| **UC-???: Manage Diet Group** | `DietGroupController.java` (MF05) | Không có UC mô tả việc tạo/quản lý nhóm ăn đặc biệt (Diet Group) cho school. |
| **UC-???: Manage Menu Template** | `MenuTemplateController.java` (MF05) | Không có UC mô tả việc tạo/lưu/áp dụng template thực đơn. |
| **UC-???: Manage Nutrition Target** | `NutritionTargetController.java` (MF02) | Không có UC mô tả việc cấu hình chỉ tiêu dinh dưỡng cho từng grade/school. |
| **UC-???: Manage Grade** | `GradeController.java` (MF02) | Không có UC mô tả việc quản lý cấp lớp (Grade) trong trường. |
| **UC-???: Manage IncidentRecord** | `IncidentRecordController.java` (MF11) | Không có UC riêng cho việc ghi nhận chi tiết trong quá trình điều tra incident. |
| **UC-???: Manage Report Schedule** | `ReportScheduleController.java` (MF13) | Không có UC mô tả việc lên lịch tự động generate báo cáo. Report đề cập UC-173 "Schedule Automated Report" trong Alternative Flow của UC-172 nhưng không có UC description riêng. |
| **UC-???: Manage Sent Report** | `SentReportController.java` (MF13) | Không có UC mô tả việc xem lịch sử báo cáo đã gửi. |
| **UC-???: Manage Student Health File** | `StudentHealthFileController.java` (MF03) | Không có UC riêng. Code có endpoint riêng cho StudentHealthFile (file sức khỏe tổng hợp) khác với HealthHistory. |
| **UC-???: Manage Announcement (create/update full flow)** | `AnnouncementController.java` (MF10) | UC-012 (Create Announcement) và UC-089 (Update Announcement) có nhưng không đề cập đến việc **xóa Announcement** mặc dù code có endpoint `DELETE`. |
| **UC-???: Manage Device Token** | `DeviceTokenController.java` (MF10) | Không có UC mô tả việc đăng ký/hủy đăng ký FCM device token. Đây là bước thiết yếu cho push notification flow. |
| **UC-???: View/Manage Compliance Documents** (cho Medical Staff/School Admin xem) | `ComplianceDocumentController.java` (MF04) | UC-012 mô tả supplier upload. Không có UC mô tả school admin/medical staff review compliance documents của partner. |

### 2.2 Use Cases mô tả sai Actor

| UC | Report mô tả | Code thực tế | Vấn đề |
|----|-------------|-------------|--------|
| **UC-003 (Active/Deactivate User)** | Primary Actor: **System Admin** | `@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")` | Code cho phép cả School Admin deactivate user trong school. Report chỉ ghi System Admin. |
| **UC-079 (Scan Face Data)** | Primary Actor: **Service Caterers** | FRS Service (`frs_service/main.py`) + `DeliveryPersonController.java` endpoint `verify` | UC-079 mô tả Service Caterers scan face nhưng trong code, FRS service được dùng để verify **delivery person** (tài xế giao hàng), không phải caterers. Actor chính phải là **Service Caterers/Receiving Staff** khi verify tài xế tại cổng trường. UC description hiện tại quá mơ hồ, thiếu Normal Flow chi tiết. |
| **UC-016 (Confirm Meal Distribution)** | Secondary Actor: **Service Caterers** | `StudentDistributionController.java` — `@PreAuthorize("hasAnyRole('TEACHER','SERVICE_CATERERS','SCHOOL_ADMIN','MEDICAL_STAFF')")` | Code cho phép 4 roles list distributions. UC-016 chỉ mô tả Teacher confirm nhưng Service Caterers cũng có quyền view/confirm theo code. |
| **UC-122 (View Information School)** | Primary Actor: **System Admin** | `SchoolController.java` — `@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")` | Code cho phép cả School Admin xem. UC-122 chỉ ghi System Admin. |
| **UC-140 (View School List)** | Primary Actor: **System Admin** | `SchoolController.java` — code cho phép Supplier xem danh sách trường | Screen Authorization table trong Report có dòng "School List" với dấu `x` cho cả **Raw Food Supplier**, nhưng UC-140 chỉ ghi Primary Actor là System Admin. Mâu thuẫn nội bộ trong chính report. |

### 2.3 UC-079 (Scan Face Data) — Mô tả thiếu và sai

| Vấn đề | Chi tiết |
|--------|---------|
| **Normal Flow trống** | UC-079 trong report chỉ có một dòng description mơ hồ, không có Normal Flow chi tiết. Đây là UC có logic phức tạp nhất (FRS service, rate limiting, GPS check). |
| **Missing precondition** | Không mô tả: delivery person phải đã được register face data (UC-068) trước. |
| **Sai scope** | Report đặt UC-079 vào nhóm "AI & Supporting Services" nhưng thực tế đây thuộc workflow "Supplier & Receiving" — FRS là microservice riêng (`AiServices/frs_service/`), không phải AI chatbot/Gemini. |
| **Rate Limiting không được đề cập** | Code có `SlidingWindowRateLimiter.java` (MF04) áp dụng cho face scan (10 lần/phút). UC-079 không đề cập business rule này. |

### 2.4 Use Cases mô tả Business Rules sai/thiếu

| UC | BR trong Report | Vấn đề |
|----|----------------|--------|
| **UC-020/UC-096 (Create/Update Menu)** | BR-061, BR-062, BR-063, BR-064, BR-069 | Code comment trong `MenuController.java` đề cập thêm **BR-067** (chỉ được xóa menu ở trạng thái DRAFT/REJECTED). UC-020 và UC-096 không reference BR-067. |
| **UC-016 (Confirm Meal Distribution)** | BR-121, BR-065, BR-123, BR-125, BR-120, BR-130 | Code có error `SRV-094 (BR-094 MF07)` — liên kết với inspection approval. UC-016 không đề cập BR-094. |
| **UC-068 (Register Face Data)** | BR-051, BR-060 | FRS service code có logic PDPD compliance (không lưu ảnh gốc). Report không có BR tương ứng cho PDPD data protection compliance trong Register Face Data. |
| **UC-002/UC-003 (Activate/Deactivate Staff/User)** | BR-007, BR-008 | Code `AccountCascadeHandler.java` có cascade deactivation logic phức tạp. UC-002 và UC-003 chỉ reference BR-021 (cascade school) nhưng không reference BR cascade account cụ thể. |

---

## 3. Màn hình (Screens) — Thiếu/Không khớp

### 3.1 Frontend Web (Next.js) — Màn hình có trong code nhưng thiếu trong report

| URL/Folder | Role | Có trong Screen Authorization? |
|-----------|------|-------------------------------|
| `school-admin/allergy-review/` | School Admin | Có trong folder. Screen "Allergy Review" CÓ trong bảng authorization (✓) |
| `school-admin/config/` | School Admin | `Config` page có trong code. Report đề cập "School Configuration" nhưng **không mô tả screen spec riêng** trong phần 3.3 |
| `school-admin/corrective-actions/` | School Admin | Có trong code. Screen "Corrective Action Review" trong bảng authorization (✓) nhưng thiếu screen spec chi tiết |
| `service-caterers/cook-sheet/` | Service Caterers | **Không có trong Screen Authorization table** và **không có screen spec**. Code đã implement. |
| `service-caterers/cook-sheet/` | Service Caterers | "Cook Sheet" (bảng chế biến/công thức nấu ăn) hoàn toàn vắng mặt trong report. |
| `service-caterers/meal-service/` | Service Caterers | Không có screen spec chi tiết trong report phần 3.3 Mobile Screens |
| `service-caterers/notification-settings/` | Service Caterers | Không có screen spec riêng cho Service Caterers notification settings (report chỉ có mobile screen) |
| `supplier/certificates/` | Supplier | Có trong folder. Report có "Food Safety Certificates" trong bảng (✓) nhưng thiếu web screen spec |
| `supplier/delivery-staff/` | Supplier | **Không có trong Screen Authorization table** cho web frontend. Chỉ có mobile screens. |
| `system-admin/` | System Admin | Folder tồn tại trong frontend nhưng report không mô tả web-specific screens cho System Admin trong phần 3.3 |

### 3.2 Mobile App (Flutter) — Màn hình có trong code nhưng thiếu trong report

| File Dart | Role | Có trong Report Section 3.3? |
|-----------|------|------------------------------|
| `caterer/kitchen_inspections_screen.dart` | Service Caterers | **Không có** trong phần 3.3.2.2 (Service Caterers Screens). Report không mô tả screen Kitchen Inspection cho mobile. |
| `caterer/class_deliveries_screen.dart` | Service Caterers | **Không có** screen spec trong 3.3.2.2. |
| `medical_staff/open_investigation_screen.dart` | Medical Staff | **Không có** screen spec trong 3.3.2.3. |
| `medical_staff/sample_handover_screen.dart` | Medical Staff | **Không có** screen spec trong 3.3.2.3. |
| `medical_staff/inspection_history_screen.dart` | Medical Staff | **Không có** screen spec trong 3.3.2.3. |
| `teacher/class_deliveries_screen.dart` | Teacher | **Không có** screen spec trong 3.3.2.6. |
| `teacher/meal_records_screen.dart` | Teacher | Report có "Meal Records" trong Screen Authorization nhưng **không có screen spec** trong phần 3.3. |
| `parent/safety_concern_screen.dart` | Parent | Report có UC-085 (Submit Safety Concern) nhưng **không có mobile screen spec** tương ứng trong 3.3.2.4. |
| `parent/weekly_menu_screen.dart` | Parent | Report có mobile screen "Weekly Menu" nhưng **spec chi tiết thiếu** (chỉ có bảng Table 1.168 cơ bản). |
| `supplier/supplier_dashboard.dart` | Supplier | Report có Table 3.77 (Supplier Dashboard) nhưng thiếu nhiều fields/buttons có trong code. |

### 3.3 Mobile Screens có trong Report nhưng không có code

| Screen trong Report | Section | Vấn đề |
|--------------------|---------|--------|
| **Screen for Prepare Ingredients** (Table 3.65) | 3.3.2.2.16 | Không tìm thấy dart file tương ứng trong `caterer/presentation/`. |
| **Screen for Record Cooking Information** (Table 3.66) | 3.3.2.2.17 | Không tìm thấy dart file tương ứng. |
| **Screen for Special Meals Configuration** (Table 3.64) | 3.3.2.2.14 | Không tìm thấy dart file tương ứng. |

---

## 4. Non-Functional Requirements — Mâu thuẫn với Implementation

### 4.1 Offline & Data Sync (Business Rules BR-196 đến BR-200)

| Vấn đề | Chi tiết |
|--------|---------|
| **Report mô tả offline capability cho Mobile** | Report (Section 3.4, Table 1.196 "17. Offline & Data Sync") mô tả BR-196 đến BR-200 về offline support, cache 12h, sync khi có kết nối. |
| **Code mobile không có offline/cache layer** | Trong `04_Development/Mobile/lib/`, không tìm thấy local database (hive/sqflite/isar) hoặc offline sync service. Mobile app hiện tại có vẻ là pure online client. |
| **MSG-077 và MSG-078 tồn tại nhưng chưa hoàn chỉnh** | Report có MSG-077 (Action blocked: Internet connection required) và MSG-078 (Saved offline, will sync). Cần implement offline queue trong mobile. |

### 4.2 GPS Verification

| Vấn đề | Chi tiết |
|--------|---------|
| **Report mô tả GPS check cho Receiving và Food Sample** | BR-074, BR-075, BR-109 yêu cầu GPS verification khi nhận hàng và khi lấy mẫu thực phẩm. |
| **Code backend có GPS check nhưng Mobile chưa rõ** | `ReceivingSessionController.java` có xử lý GPS metadata. Tuy nhiên, trong dart files, không thấy rõ GPS permission request hay location service integration. |

### 4.3 Face Recognition Service — Phân loại nhầm trong Report

| Vấn đề | Chi tiết |
|--------|---------|
| **Report đặt Scan Face Data vào "AI & Supporting Services" (Section 3.2.12)** | FRS là microservice riêng tại `AiServices/frs_service/` dùng DeepFace (not Gemini AI). |
| **Gemini AI và FRS là 2 service hoàn toàn khác nhau** | Report (`AiServices/rag_service/`) là RAG service dùng cho AI Chatbot. `AiServices/frs_service/` là Face Recognition dùng DeepFace. Report không làm rõ sự khác biệt này trong phần Technical Architecture. |

---

## 5. Business Rules — Thiếu/Sai trong Report

| BR | Vấn đề | Nguồn code |
|----|--------|-----------|
| **Thiếu BR cho Rate Limiting** | Code có `SlidingWindowRateLimiter` cho face scan (10/min) và `RegisterRateLimiter` cho school registration. Report không có BR tương ứng. | `MF02/RegisterRateLimiter.java`, `MF04/SlidingWindowRateLimiter.java` |
| **Thiếu BR cho PDPD Compliance** | FRS service explicit nói "KHONG luu ảnh gốc, chỉ giữ faceId (PDPD Compliance)". Report không có BR về bảo vệ dữ liệu sinh trắc học theo PDPD. | `AiServices/frs_service/main.py` |
| **BR-067 thiếu reference trong UC-020** | BR-067 (chỉ xóa menu ở DRAFT/REJECTED) được implement trong code (`MenuController: DELETE /{code}`) nhưng không được reference trong UC-020 (Create Menu). | `MF05/MenuController.java` |
| **Thiếu BR cho StudentCode trong Menu list** | Code comment: "studentCode (PARENT - con đang chọn) KHÔNG phải cột của menus nên phải tách khỏi allParams". Đây là business rule đặc thù cần document. | `MF05/MenuController.java` |
| **Thiếu BR cho SupplierBatch vs ReceivingSession** | Code rõ ràng tách 2 entity. Report không có BR mô tả quan hệ 1-N giữa ReceivingSession và SupplierBatch. | `MF06/SupplierBatchController.java`, `MF06/ReceivingSessionController.java` |

---

## 6. Mâu thuẫn Nội bộ trong Report

| # | Vị trí | Mâu thuẫn |
|---|--------|-----------|
| C1 | UC-140 vs Screen Authorization Table | UC-140 (View School List): Primary Actor = System Admin. Nhưng Screen Authorization table đánh dấu "School List" = ✓ cho cả **Raw Food Supplier**. |
| C2 | UC-003 vs UC-002 | UC-002 (Active/Deactivate Staff) và UC-003 (Active/Deactivate User): Có similar normal flow nhưng UC-002 không có bước "Check protected primary admin" như UC-003. Code xử lý tương tự cho cả 2. |
| C3 | Section 3.2.12.5 UC-079 | UC-079 (Scan Face Data) được đặt trong mục 3.2.12 "AI & Supporting Services" nhưng UC description nói "Service Caterers scan face" liên quan đến nhận hàng — không phải AI chatbot. |
| C4 | BR-135 vs MSG-065 | BR-135: "System automatically notifies homeroom teacher and parents when meal serving is delayed by more than 20 minutes". MSG-065: "Delivery delay alert: Supplier delivery time deviates from schedule." MSG-065 nói về supplier delivery, không phải meal serving. 2 khái niệm khác nhau đang share cùng notification type. |
| C5 | Table of Contents vs UC numbering | Một số bảng dùng "Table 1.xxx" (ví dụ Table 1.157) trong khi những bảng khác dùng "Table 3.x" (Table 3.1, 3.2...). Đánh số không nhất quán gây khó tra cứu. |
| C6 | Parent role trong Allergy Information Screen | Screen Authorization: "Allergy Information" ✓ cho Teacher, Medical Staff và **Parent**. Nhưng UC-104 (View Allergy Information) Primary Actor chỉ ghi "Medical Staff, Parent" — thiếu Teacher. |

---

## 7. Tổng hợp theo mức độ ưu tiên

### Ưu tiên cao (ảnh hưởng đến nghiệp vụ chính)
1. UC-079 (Scan Face Data) — Mô tả quá sơ sài, sai nhóm, thiếu Normal Flow
2. Thiếu UC cho Kitchen Inspection, Safety Signoff (code đã implement MF07)
3. Thiếu workflow cho Cook Sheet (service caterers, code đã implement)
4. Mô tả sai Actor cho UC-003, UC-122, UC-140

### Ưu tiên trung bình (ảnh hưởng đến tính đầy đủ)
5. Thiếu UC cho Menu Template, Diet Group, Nutrition Target
6. Thiếu screen specs cho mobile: kitchen inspection, class deliveries, sample handover
7. Thiếu Business Rules cho Rate Limiting, PDPD Compliance
8. Offline capability (BR-196~BR-200) chưa được implement trong mobile code

### Ưu tiên thấp (cosmetic/consistency)
9. Đánh số bảng không nhất quán (Table 1.xxx vs Table 3.x)
10. Mô tả AiServices (Gemini vs FRS) không tách bạch

---

## 8. Danh sách Use Cases cần bổ sung vào Report

| UC ID gợi ý | Tên | Actor | Module Code |
|-------------|-----|-------|-------------|
| UC-176 | View/Create Kitchen Inspection | Medical Staff, Service Caterers | MF07 |
| UC-177 | Manage Safety Signoff | Medical Staff | MF07 |
| UC-178 | View Traceability Map | School Admin, Medical Staff | MF06 |
| UC-179 | Manage Inventory Transaction | Service Caterers | MF06 |
| UC-180 | Manage Supplier Product Catalog | Raw Food Supplier | MF06 |
| UC-181 | Manage Source Report | Teacher, Parent | MF11 |
| UC-182 | Manage Diet Group | School Admin, Service Caterers | MF05 |
| UC-183 | Manage Menu Template | Service Caterers | MF05 |
| UC-184 | Configure Nutrition Target | School Admin | MF02 |
| UC-185 | Manage Incident Record (Investigation Detail) | Medical Staff | MF11 |
| UC-186 | Manage Report Schedule | School Admin, Medical Staff | MF13 |
| UC-187 | View Sent Report History | School Admin | MF13 |
| UC-188 | Manage Student Health File | Medical Staff | MF03 |
| UC-189 | Register Device Token | All authenticated users | MF10 |
| UC-190 | Cook Sheet Management | Service Caterers | Frontend: cook-sheet/ |
| UC-173 | Schedule Automated Report *(chỉ có Alternative Flow trong UC-172)* | School Admin | MF13 |

---

*Document này được tạo tự động bằng cách đối chiếu source code tại `04_Development/` với Report 3 (`Report3_12_08_2026.md`). Cần review lại bởi team trước khi cập nhật tài liệu chính thức.*
