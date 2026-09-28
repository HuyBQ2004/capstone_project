# SFMS — Business Workflows (Main Workflows)

> **Nguồn**: Tổng hợp và trực quan hóa lại từ `06_Document\01_Report\Report 3.md` (mục "Main Workflows"), đối chiếu mã BR với `02_Requirements\00_BusinessRules\business_rules.md`.
> **Ghi chú quan trọng**: Các hình vẽ workflow gốc trong Report 3 là ảnh base64 không thể đọc/trích xuất được (placeholder `![](data:image/png;base64...)`). Toàn bộ sơ đồ Mermaid dưới đây được **dựng lại** từ bảng các bước (Step Table) đã có trong Report 3, không phải chụp lại hình gốc. Khi review, vui lòng đối chiếu lại với hình gốc (nếu có file nguồn) để xác nhận hướng mũi tên đúng ý đồ thiết kế ban đầu.
> **Liên kết kiến trúc**: Toàn bộ workflow vận hành trong phạm vi Modular Monolith đã quyết định tại [ADR-001](../02_ADR/ADR-001.md); các luồng có GPS/biometric tham chiếu [ADR-002](../02_ADR/ADR-002.md); luồng AI gợi ý thực đơn tham chiếu [ADR-003](../02_ADR/ADR-003.md); mọi ghi log "audit" trong các bước dưới đây đều ghi vào `audit_log` theo thiết kế [ADR-004](../02_ADR/ADR-004.md).

---

## Context Diagram (tóm tắt)

| External Entity | Input to System | Output from System |
| --- | --- | --- |
| System Admin | System Configuration, User Management Information | System Status, Audit Log Information |
| School Administration | Approval Decisions, School Policy, School Management Information | Dashboard Info, Food Safety Reports, Incident Reports |
| Service Caterers | Menu, Processing, Inspection Information | Approved Menu, Ingredient Information, Allergy Alerts |
| Medical Staff | Health, Allergy Verification, Incident Information | Health Alerts, Sample Information, Food Safety Reports |
| Teacher | Student Information, Health Update Information | Menu Information, Notification Information |
| Parent | Health Declaration, Allergy, Feedback Information | Menu/Nutrition Information, Notification Information |
| Raw Food Supplier | Supplier, Delivery, Certificate Information | Approval Information, Evaluation Information |
| GPS Service | Location Verification Result | Location Verification Request |
| Gmail | Email Delivery Status | Email Information |
| Firebase Cloud Messaging | Notification Delivery Status | Notification Information |
| Gemini AI Service | Menu Recommendation, Nutrition Analysis | Menu Requirement, Student Health Information |

## Danh sách 12 Workflow

| # | Workflow | Actor chính | Mục lục |
| --- | --- | --- | --- |
| 1 | Supplier Registration & Approval | Raw Food Supplier, School Administration | [§1](#1-supplier-registration--approval-workflow) |
| 2 | Ingredient Receiving | Raw Food Supplier, Service Caterers | [§2](#2-ingredient-receiving-workflow) |
| 3 | Ingredient Storage & Inventory Management | Service Caterers | [§3](#3-ingredient-storage--inventory-management-workflow) |
| 4 | Menu Planning & Approval | Service Caterers, Gemini AI, School Administration | [§4](#4-menu-planning--approval-workflow) |
| 5 | Ingredient Issue to Kitchen | Service Caterers | [§5](#5-ingredient-issue-to-kitchen-workflow) |
| 6 | Food Preparation | Service Caterers | [§6](#6-food-preparation-workflow) |
| 7 | Three-Step Food Inspection | Service Caterers | [§7](#7-three-step-food-inspection-workflow) |
| 8 | Food Sample Retention | Service Caterers, Medical Staff | [§8](#8-food-sample-retention-workflow) |
| 9 | Allergy & Special Meal Management | Parent, Medical Staff, Service Caterers | [§9](#9-allergy--special-meal-management-workflow) |
| 10 | Meal Serving | Service Caterers (Kitchen Manager), Teacher | [§10](#10-meal-serving-workflow) |
| 11 | Food Safety Incident Handling | Medical Staff, School Administration, Service Caterers | [§11](#11-food-safety-incident-handling-workflow) |
| 12 | Parent Meal Monitoring | Parent | [§12](#12-parent-meal-monitoring-workflow) |

Luồng nghiệp vụ tổng thể giữa các workflow:

```mermaid
flowchart LR
    W1[1. Supplier Registration & Approval] --> W2[2. Ingredient Receiving]
    W2 --> W3[3. Storage & Inventory]
    W4[4. Menu Planning & Approval] --> W5[5. Ingredient Issue to Kitchen]
    W3 --> W5
    W5 --> W6[6. Food Preparation]
    W6 --> W7[7. Three-Step Food Inspection]
    W7 --> W8[8. Food Sample Retention]
    W7 --> W10[10. Meal Serving]
    W9[9. Allergy & Special Meal Management] --> W4
    W9 --> W10
    W10 --> W11[11. Food Safety Incident Handling]
    W8 -.retrieve sample.-> W11
    W10 --> W12[12. Parent Meal Monitoring]
    W4 --> W12
```

---

## 1. Supplier Registration & Approval Workflow

**BR References**: `BR-040`, `BR-041`, `BR-046`, `BR-047`, `BR-048`

```mermaid
flowchart TD
    S1["1. Submit Supplier Profile\n(Raw Food Supplier)"] --> S2["2. Review Supplier Documents\n(School Administration)"]
    S2 --> S3{"3. Documents Valid?"}
    S3 -- Yes --> S4["4. Evaluate Supplier\n(School Administration)"]
    S3 -- No --> S8["8. Record Rejection Reason\n(School Administration)"]
    S4 --> S5{"5. Approve Supplier?"}
    S5 -- Yes --> S6["6. Create Supplier Record\n(System)"]
    S5 -- No --> S8
    S6 --> S7["7. Send Approval Notification\n(System)"]
    S8 --> S9["9. Send Rejection Notification\n(System)"]
```

| Step | Tên bước | Role | Ghi chú |
| --- | --- | --- | --- |
| 1 | Submit Supplier Profile | Supplier | Start of flow |
| 2 | Review Supplier Documents | School Administration | Manual review |
| 3 | Documents Valid? | School Administration | Decision: Yes→4, No→8 |
| 4 | Evaluate Supplier | School Administration | Có thể cần verify thêm |
| 5 | Approve Supplier? | School Administration | Decision: Yes→6, No→8 |
| 6 | Create Supplier Record | System | Gán status Approved |
| 7 | Send Approval Notification | System | End — success |
| 8 | Record Rejection Reason | School Administration | Khi không approve |
| 9 | Send Rejection Notification | System | End — rejection |

---

## 2. Ingredient Receiving Workflow

**BR References**: `BR-070`, `BR-071`, `BR-072`, `BR-073`, `BR-074`, `BR-078`

```mermaid
flowchart TD
    R1["1. Deliver Ingredient Batch\n(Raw Food Supplier)"] --> R2["2. Start Receiving Session\n(Service Caterers)"]
    R2 --> R3{"3. Verify Supplier Status\n(System)"}
    R3 -- Approved --> R4["4. Review Batch Information"]
    R3 -- Rejected --> R14["14. Reject Ingredient Batch"]
    R4 --> R5["5. Inspect Ingredient Quality"]
    R5 --> R6["6. Capture Evidence Photos"]
    R6 --> R7["7. Capture GPS & Timestamp (System, auto)"]
    R7 --> R8{"8. Verify Receiving Location"}
    R8 -- Valid --> R9["9. Complete Inspection Checklist"]
    R8 -- Invalid --> R14
    R9 --> R10{"10. Ingredient Qualified?"}
    R10 -- Yes --> R11["11. Create Batch Record (System)"]
    R10 -- No --> R14
    R11 --> R12["12. Update Inventory (System)"]
    R12 --> R13["13. Generate Receiving Record (System)"]
    R14["14. Reject Ingredient Batch\n(Service Caterers / System)"]
```

Bước 7–8 (GPS/timestamp tự động + xác minh vị trí) là cơ chế anti-fraud, liên kết trực tiếp với ràng buộc GPS trong [ADR-002](../02_ADR/ADR-002.md). Bước 13 sinh ra Receiving Record là input cho Workflow 3 (Storage & Inventory).

---

## 3. Ingredient Storage & Inventory Management Workflow

**BR References**: `BR-075` (FEFO), `BR-076` (Traceability), `BR-077` (Shortage Alert)

```mermaid
flowchart TD
    I1["1. Receive Approved Batch\n(System, từ Workflow 2)"] --> I2["2. Assign Storage Location"]
    I2 --> I3["3. Store Ingredient Batch"]
    I3 --> I4["4. Record Storage Information (System)"]
    I4 --> I5["5. Monitor Inventory Status (System)"]
    I5 --> I6["6. Monitor Expiration Date (System)"]
    I6 --> I7{"7. Expiration Alert Required?"}
    I7 -- Yes --> I8["8. Generate Expiration Alert"]
    I7 -- No --> I9{"9. Ingredient Request Received?"}
    I8 --> I9
    I9 -- Yes --> I10["10. Verify Inventory Availability"]
    I9 -- No --> I5
    I10 --> I11{"11. Inventory Available?"}
    I11 -- Yes --> I12["12. Issue Ingredient Batch (FIFO/FEFO)"]
    I11 -- No --> I15["15. Generate Inventory Shortage Alert"]
    I12 --> I13["13. Update Inventory Quantity (System)"]
    I13 --> I14["14. Record Inventory Transaction (System)"]
```

Vòng lặp 5→6→7→9→5 thể hiện giám sát liên tục (continuous monitoring), không có "kết thúc" workflow — chỉ kết thúc một lượt request tại bước 14 hoặc 15.

---

## 4. Menu Planning & Approval Workflow

**BR References**: `BR-061`, `BR-062`, `BR-063`, `BR-064`, `BR-065`, `BR-066`, `BR-067`, `BR-068`, `BR-069`, `BR-080`, `BR-082`, `BR-156` (AI menu suggestion — [ADR-003](../02_ADR/ADR-003.md)) — *(BR-081 "Món thay thế cho dị ứng phổ biến" đã xoá 2026-07-01, xem `business_rules.md` §7)*

```mermaid
flowchart TD
    M1["1. Review Menu Requirements\n(Service Caterers)"] --> M2["2. Review Allergy Statistics (System)"]
    M2 --> M3["3. Generate Menu Draft (Service Caterers)"]
    M3 --> M4["4. Generate Menu Recommendation (Gemini AI)"]
    M4 --> M5["5. Analyze Nutrition (Gemini AI)"]
    M5 --> M6["6. Detect Allergy Conflicts (System)"]
    M6 --> M7{"7. Allergy Conflict Detected?"}
    M7 -- Yes --> M8["8. Generate Alternative Meal Suggestions (Gemini AI)"]
    M7 -- No --> M10["10. Submit Menu for Approval"]
    M8 --> M9["9. Review Alternative Meals (Service Caterers)"]
    M9 --> M10
    M10 --> M11["11. Review Menu (School Administration)"]
    M11 --> M12{"12. Menu Approved?"}
    M12 -- Yes --> M13["13. Publish Menu (System)"]
    M12 -- No --> M16["16. Reject Menu (comments)"]
    M13 --> M14["14. Notify Stakeholders"]
    M14 --> M15["15. Store Menu Records (System)"]
    M16 --> M17["17. Revise Menu"]
    M17 -.return to.-> M3
```

Bước 4–5–8 (Gemini AI) phải tuân theo pipeline RAG + hard-filter dị ứng quyết định tại [ADR-003](../02_ADR/ADR-003.md) — gợi ý AI không bao giờ được bỏ qua bước hard filter dị ứng ở bước 6.

---

## 5. Ingredient Issue to Kitchen Workflow

**BR References**: `BR-075` (FEFO), `BR-076` (Traceability), `BR-077` (Shortage Alert)

```mermaid
flowchart TD
    K1["1. Review Approved Menu\n(Service Caterers)"] --> K2["2. Create Ingredient Request"]
    K2 --> K3["3. Verify Inventory Availability (System)"]
    K3 --> K4{"4. Inventory Available?"}
    K4 -- Yes --> K5["5. Select Ingredient Batches (FEFO, System)"]
    K4 -- No --> K12["12. Generate Shortage Alert"]
    K5 --> K6["6. Review Selected Batches (Service Caterers)"]
    K6 --> K7["7. Issue Ingredients"]
    K7 --> K8["8. Update Inventory Records (System)"]
    K8 --> K9["9. Record Batch Traceability (System)"]
    K9 --> K10["10. Generate Issue Record (System)"]
    K10 --> K11["11. Ingredients Ready for Preparation"]
    K12 --> K13["13. Request Inventory Replenishment"]
```

---

## 6. Food Preparation Workflow

**BR References**: `BR-069` (khai báo nguyên liệu đầy đủ), `BR-082` (chế biến riêng cho bữa ăn đặc biệt)

```mermaid
flowchart TD
    P1["1. Review Approved Menu\n(Service Caterers)"] --> P2["2. Retrieve Issued Ingredients"]
    P2 --> P3["3. Verify Ingredient Information"]
    P3 --> P4["4. Prepare Ingredients"]
    P4 --> P5["5. Record Pre-processing Information (System)"]
    P5 --> P6["6. Start Cooking Process"]
    P6 --> P7["7. Record Cooking Information (System)"]
    P7 --> P8["8. Complete Meal Preparation"]
    P8 --> P9["9. Generate Preparation Record (System)"]
    P9 --> P10["10. Meals Ready for Inspection"]
```

Bước 10 chuyển tiếp trực tiếp sang Workflow 7 (Three-Step Food Inspection).

---

## 7. Three-Step Food Inspection Workflow

**BR References**: `BR-079`, `BR-083`, `BR-090`, `BR-091`, `BR-092`, `BR-093`, `BR-094`, `BR-095`, `BR-096`, `BR-097`, `BR-098`, `BR-099`

```mermaid
flowchart TD
    T1["1. Start Inspection Session\n(Service Caterers)"] --> T2["2. Perform Inspection Step 1\n(trước chế biến)"]
    T2 --> T3["3. Record Inspection Step 1 (System: photo, GPS, timestamp)"]
    T3 --> T4{"4. Step 1 Passed?"}
    T4 -- Yes --> T5["5. Perform Inspection Step 2\n(sau khi nấu)"]
    T4 -- No --> T13["13. Record Non-Conformance"]
    T5 --> T6["6. Record Inspection Step 2 (System)"]
    T6 --> T7{"7. Step 2 Passed?"}
    T7 -- Yes --> T8["8. Perform Inspection Step 3\n(trước khi phục vụ)"]
    T7 -- No --> T13
    T8 --> T9["9. Record Inspection Step 3 (System)"]
    T9 --> T10{"10. Step 3 Passed?"}
    T10 -- Yes --> T11["11. Generate Inspection Record (System)"]
    T10 -- No --> T13
    T11 --> T12["12. Approve Meals for Serving"]
    T13 --> T14["14. Create Corrective Action Request"]
    T14 --> T15["15. Re-inspection Required"]
    T15 -.return to failed step.-> T2
```

Mỗi bước inspection (2-1, 5-1, 8-1) đều ghi GPS + timestamp + ảnh bằng chứng (BR-083, BR-073-style anti-fraud) — đây là 3 lớp kiểm soát độc lập, không thể bỏ qua một bước để chuyển thẳng sang bước kế (BR-094: chặn phục vụ nếu kiểm tra thất bại).

---

## 8. Food Sample Retention Workflow

**BR References**: `BR-105`, `BR-106`, `BR-107`, `BR-108`, `BR-109`, `BR-110`, `BR-111`, `BR-112`, `BR-113`, `BR-114`

```mermaid
flowchart TD
    F1["1. Complete Food Inspection\n(System, pass cả 3 bước)"] --> F2["2. Identify Meals for Retention\n(Service Caterers)"]
    F2 --> F3["3. Collect Food Samples"]
    F3 --> F4["4. Label Food Samples"]
    F4 --> F5["5. Capture Evidence Photos"]
    F5 --> F6["6. Capture GPS & Timestamp (System, auto)"]
    F6 --> F7["7. Verify Retention Location (System)"]
    F7 --> F8["8. Create Sample Record (System)"]
    F8 --> F9["9. Store Food Samples"]
    F9 --> F10["10. Record Storage Information (System)"]
    F10 --> F11["11. Monitor Retention Period (System)"]
    F11 --> F12{"12. Retention Period Expired?"}
    F12 -- Yes --> F13["13. Dispose of Food Samples"]
    F13 --> F14["14. Record Disposal Information (System)"]
    F12 -- "No / Incident occurs" --> F15a["15a. Chỉ định thu hồi mẫu\n(Medical Staff — UC-076)"]
    F15a --> F15b["15b. Lấy mẫu & bàn giao khi cơ quan tới nhận\n(Service Caterers — BR-215)"]
    F15b --> F16["16. Record Investigation Usage (System)"]
```

Nhánh 15a–16 là alternative flow được kích hoạt từ Workflow 11 (Food Safety Incident Handling) khi cần truy vết bằng mẫu lưu.

> **Cập nhật 22/07/2026** — tách bước 15 cũ ("Retrieve Food Sample — Medical Staff") thành 15a/15b cho khớp BR-113/BR-215 đã sửa từ 19/07/2026: Medical Staff **chỉ định** mẫu cần thu hồi + nơi tiếp nhận + chỉ tiêu; **Service Caterers** lấy mẫu tại tủ lưu và **bàn giao khi cán bộ của cơ quan tiếp nhận tới trường**, rồi tải biên bản bàn giao (BR-212) — bếp KHÔNG tự mang mẫu đi, không ai để bên bị điều tra tự vận chuyển vật chứng.

> **Đính chính 22/07/2026 (cùng ngày)** — bản đầu của mục này viết "bàn giao phòng kiểm nghiệm", vẫn hàm ý bếp mang mẫu đi; đã sửa. Nơi tiếp nhận cũng không chỉ là phòng kiểm nghiệm: công an hoặc thanh tra có thể tới **thu giữ** mẫu làm vật chứng — khi đó trường không đặt chỉ tiêu xét nghiệm và có thể không nhận được kết quả, chỉ giữ biên bản thu giữ (xem `ReceivingAuthority.type` trong mock). Sơ đồ trước đó vẫn ghi Medical Staff trực tiếp thực hiện — chỉ `business_rules.md` được sửa, tài liệu Workflow chưa theo (ghi nhận tại `review_medicalstaff_to_servicecaterers.md` Mục 4).

---

## 9. Allergy & Special Meal Management Workflow

**BR References**: `BR-031` (xét duyệt thông tin sức khỏe), `BR-063` (kiểm tra xung đột dị ứng), `BR-065`, `BR-066`, `BR-082`, `BR-123` — *(BR-081 "Món thay thế cho dị ứng phổ biến" đã xoá 2026-07-01, xem `business_rules.md` §7)*

```mermaid
flowchart TD
    A1["1. Submit Health Information\n(Parent)"] --> A2["2. Create Health Declaration Record (System)"]
    A2 --> A3["3. Review Health Information (Medical Staff)"]
    A3 --> A4{"4. Additional Verification Required?"}
    A4 -- Yes --> A5["5. Request Additional Information (System)"]
    A5 -.-> A3
    A4 -- No --> A6["6. Verify Allergy Information (Medical Staff)"]
    A6 --> A7["7. Create Allergy Profile (System)"]
    A7 --> A8["8. Classify Special Meal Category (System)"]
    A8 --> A9["9. Generate Allergy Alerts (System)"]
    A9 --> A10["10. Notify Service Caterers (System)"]
    A10 --> A11["11. Review Special Meal Requirements (Service Caterers)"]
    A11 --> A12["12. Prepare Special Meal Plan (Service Caterers)"]
    A12 --> A13["13. Monitor Allergy Compliance (System, continuous)"]
    A13 --> A14{"14. Allergy Conflict Detected?"}
    A14 -- Yes --> A15["15. Generate Allergy Warning"]
    A15 --> A16["16. Update Alternative Meal Plan"]
    A16 --> A17["17. Store Allergy Management Records (System)"]
    A14 -- No --> A13
```

Workflow này là nguồn dữ liệu đầu vào (allergy profile, special meal category) cho Workflow 4 (Menu Planning), Workflow 10 (Meal Serving) và là một control xuyên suốt liên tục (bước 13 lặp vô hạn cho tới khi có conflict hoặc menu mới).

---

## 10. Meal Serving Workflow

**BR References**: `BR-120`, `BR-121`, `BR-123`, `BR-125`, `BR-130`, `BR-133`, `BR-135`

```mermaid
flowchart TD
    MS1["1. Prepare Meal Portions\n(Kitchen Manager — theo Allocation List, Workflow 3)"] --> MS2["2. Deliver Meals to Classrooms"]
    MS2 --> MS3["3. Distribute Meals to Students\n(Teacher, theo danh sách bữa đặc biệt)"]
    MS3 --> MS4{"4. Meal Distribution Correct?"}
    MS4 -- No --> MS5["5. Correct Error (Kitchen Manager)"]
    MS5 -.-> MS3
    MS4 -- Yes --> MS6["6. Complete Meal Distribution (Teacher)"]
    MS6 --> MS7["7. Record Serving Info (SFMS)"]
    MS7 --> MS8["8. Monitor & Record Meal Consumption (Teacher)"]
    MS8 --> MS9["9. Submit Meal Feedback (Teacher)"]
    MS9 --> MS10{"10. Potential Safety Incident Reported?"}
    MS10 -- No --> MS11["11. Complete Service (End)"]
    MS10 -- Yes --> MS12["12. Create Incident Notification (SFMS)"]
    MS12 --> MS13["13. Transfer to Workflow 11\n(Food Safety Incident Handling)"]
    MS13 --> MS14["14. Create Record (incident log)"]
    MS14 --> MS15["15. Complete Service (End)"]
```

---

## 11. Food Safety Incident Handling Workflow

**BR References**: `BR-146`, `BR-147`, `BR-148`, `BR-149`, `BR-150`, `BR-151`, `BR-152` (CAPA), `BR-153`, `BR-154`, `BR-155`, `BR-216` (vòng đời CAPA)

> **Cập nhật 20/07/2026** — đồng bộ nhánh CAPA (bước 16-20) theo `BR-216` (mới), thống nhất qua nhiều vòng thảo luận thiết kế xuất phát từ 1 tình huống giả định (Medical Staff có thể lợi dụng quyền đề xuất CAPA để áp đặt yêu cầu quá đáng lên Service Caterers) — chi tiết đầy đủ xem `08_FrontEnd/Web/review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 16. Thay đổi cụ thể: (1) bước 18 đổi tên thành "Xem + Accept, sau đó Implement" — Service Caterers chỉ xem và xác nhận (accept) phương án đã được School Administration duyệt ở bước 17, không có nhánh từ chối trên hệ thống (việc bàn bạc tính khả thi diễn ra trực tiếp ngoài đời, trước bước 16); (2) **bỏ bước 19 "Monitor Corrective Actions (School Administration)"** — không có căn cứ trong `BR-216`, 2 điểm kiểm soát của School Administration là đủ (duyệt kế hoạch ở bước 17, rà soát toàn hồ sơ trước khi đóng ở bước 21), thêm 1 lớp giám sát giữa chừng là dư thừa; bước 18b "Đạt" nối thẳng sang bước 20; (3) ghi chú ở bước 18b (dưới sơ đồ) về việc "chưa có BR/UC chính thức" nay đã giải quyết — `UC-168` + `BR-216` đã chính thức hoá đầy đủ. Không đổi thứ tự 17 trước 18 (đã đúng từ trước, không phải điểm cần sửa — điểm sai nằm ở `Report 3.md` UC-005 mô tả ngược, đã sửa riêng 20/07/2026).

> **Cập nhật 19/07/2026** — đồng bộ lại 4 điểm lệch giữa sơ đồ và SRS, phát hiện qua review Web Medical Staff Incident Management (Vòng 7, chi tiết: `08_FrontEnd/Web/review/medical_staff/review_medicalstaff_design_pattern.md`): (1) đổi nhãn actor "Kitchen Manager" (không thuộc 7 vai trò canonical BR-007) → "Service Caterers (Kitchen Manager)" ở bước 11, và "Service Caterers" ở bước 18; (2) sửa actor bước 9 từ "(SFMS)" → "(Medical Staff — UC-076)" cho khớp Primary Actor thật của UC-076; (3) bổ sung bước 18b "Xác nhận khắc phục (UC-168)" — vốn đã bị `MedicalStaff_Role_and_Workflows.md` §7 điểm 2 flag là thiếu trong sơ đồ gốc, và đã được hiện thực trong code Web (Block A, Section D `26_IncidentDetail.md`) từ trước — nay thêm nhánh "Chưa đạt" quay lại bước 18 (bổ sung nghiệp vụ mới, chưa có mã UC/BR chính thức, cần đội dự án xác nhận/cấp mã sau); (4) đổi gate "Root Cause Confirmed?" (bước 14, chặn cứng CAPA) thành "Đủ căn cứ đề xuất CAPA? (kết luận sơ bộ)" — vì BR-152/UC-038 yêu cầu CAPA phải giao trong 12 giờ, trong khi BR-149 (bổ sung 15/07) ghi nhận một số tác nhân có thời gian ủ bệnh 62–87 giờ; gate cứng theo kết luận **chính thức** khiến SLA 12h bất khả thi với nhóm sự cố này — bước 16b (hoàn tất Root Cause chính thức) chạy song song, không chặn CAPA, nhưng vẫn là điều kiện bắt buộc trước bước 20 (đã chốt tại review Web, Mục 11.9.1 `review_medicalstaff_incidentmanagement.md`). Bước 21→22 **giữ nguyên, không thêm nhánh reject/version** — đã có 2 điểm kiểm soát của School Administration (duyệt CAPA ở bước 17/UC-005, và tự rà soát investigation summary + CAPA completion ở bước 21/UC-013 trước khi đóng), thêm cơ chế reject riêng cho báo cáo là dư thừa so với 2 điểm kiểm soát đã có.

```mermaid
flowchart TD
    E1["1. Review Incident Information\n(Medical Staff — trigger từ Workflow 10)"] --> E2{"2. Food Safety Related?"}
    E2 -- No --> E3["3. Close Non-Food-Safety Case"]
    E2 -- Yes --> E4["4. Assess Incident Severity\n(Medical Staff — UC-009; quarantine lô nghi vấn ngay, BR-149)"]
    E4 --> E5["5. Provide Medical Treatment"]
    E5 --> E6{"6. High Severity Incident?"}
    E6 -- Yes --> E7["7. Activate Emergency Response"]
    E6 -- No --> E8["8. Start Investigation"]
    E7 --> E8
    E8 --> E9["9. Retrieve Food Sample Logs\n(Medical Staff — UC-076)"]
    E9 --> E10["10. Retrieve Ingredient Batches (SFMS)"]
    E10 --> E11["11. Trace Supplier Information\n(Service Caterers — Kitchen Manager)"]
    E11 --> E12["12. Collect Supporting Evidence (Medical Staff)"]
    E12 --> E13["13. Identify Root Cause — Sơ bộ\n(Medical Staff)"]
    E13 --> E14{"14. Đủ căn cứ đề xuất CAPA?\n(kết luận sơ bộ, không chờ lab)"}
    E14 -- Chưa đủ --> E15["15. Additional Investigation"]
    E15 -.-> E12
    E13 -.-> E16b["16b. Hoàn tất Root Cause Chính thức\n(Medical Staff — song song, có thể sau CAPA)"]
    E14 -- Đủ --> E16["16. Define Corrective Actions (CAPA)\n(Medical Staff — root cause có thể còn Sơ bộ)"]
    E16 --> E17{"17. Corrective Actions Approved?\n(School Administration — UC-005, BR-216)"}
    E17 -- No --> E16
    E17 -- Yes --> E18["18. Xem + Accept, sau đó Implement\n(Service Caterers — UC-038/082, BR-216)"]
    E18 --> E18b{"18b. Xác nhận khắc phục đạt yêu cầu?\n(Medical Staff — UC-168, BR-216)"}
    E18b -- Chưa đạt, dùng lại đúng phương án đã duyệt --> E18
    E16b -.-> E20
    E18b -- Đạt --> E20["20. Generate Incident Report\n(Medical Staff — UC-033; yêu cầu Root Cause đã Chính thức)"]
    E20 --> E21["21. Review Incident Report (School Administration)"]
    E21 --> E22["22. Close Incident (School Administration — UC-013)"]
```

Bước 9 (Retrieve Food Sample Logs) liên kết trực tiếp với nhánh "Retrieve Food Sample" (bước 15-16) của Workflow 8.
Bước 18b (nhánh "Chưa đạt" quay lại 18) — thống nhất tại review 19/07/2026, **đã chính thức hoá 20/07/2026** qua `UC-168` (`Report 3.md`, đã viết lại nhánh AF1 "Implementation Not Satisfactory") và `BR-216` (mới, `business_rules.md`).

---

## 12. Parent Meal Monitoring Workflow

**BR References**: `BR-133`, `BR-139`, `BR-140`, `BR-141`, `BR-142`, `BR-143`, `BR-145`

```mermaid
flowchart TD
    PM1["1. Access Meal Monitoring Portal\n(Parent)"] --> PM2["2. View Published Menu (SFMS)"]
    PM2 --> PM3["3. View Allergy Information (SFMS)"]
    PM3 --> PM4["4. Review Special Meal Status (Parent)"]
    PM4 --> PM5["5. View Food Safety Information (SFMS)"]
    PM5 --> PM6["6. Receive Health Alerts (SFMS)"]
    PM6 --> PM7["7. Submit Meal Feedback (Parent)"]
    PM7 --> PM8["8. Create Feedback Record (SFMS)"]
    PM8 --> PM9{"9. Feedback Requires Investigation?"}
    PM9 -- Yes --> PM10["10. Create Incident Report (SFMS)"]
    PM9 -- No --> PM11["11. Store Monitoring Records (SFMS — End)"]
    PM10 --> PM11
```

Bước 10 liên kết với Workflow 11 (Food Safety Incident Handling) tương tự bước 12-13 của Workflow 10.

---

*Tài liệu này tổng hợp lại nội dung Main Workflows từ `Report 3.md` dưới dạng Mermaid để dễ review/maintain trong markdown — không thay thế hình vẽ gốc nếu file nguồn (Visio/draw.io) còn tồn tại ở nơi khác.*
