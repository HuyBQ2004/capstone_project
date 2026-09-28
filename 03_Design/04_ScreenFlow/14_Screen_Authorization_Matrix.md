# Screen Authorization Matrix — SFMS (bản chuẩn, cập nhật 2026-07-01)

> **Nguồn gốc**: `Report 3.md` mục "Screen Authorization" (dòng 5053–5236) — bảng gốc 180 màn hình × 7 actor, cột `x` xác định quyền truy cập, **không có cột Nền tảng hay UC** trong bản gốc.
> **Bổ sung trong bản chuẩn này (2026-07-01)**:
> 1. **Cột "Nền tảng"** — áp dụng quyết định làm việc "all web + Mobile song song" (`00_UI_Design_Documentation_Plan.md` mục 0, 5, 8b; đã hiện thực hoá trong `03_Design/04_ScreenFlow/00_Screen_Flow_Overview.md`): màn hình chỉ thuộc System Admin/School Administration → **Web**; màn hình có ít nhất 1 trong 5 actor field (Teacher, Raw Food Supplier, Medical Staff, Service Caterers, Parent) → **Web + Mobile**.
> 2. **11 màn hình mới** (chưa từng có trong Report 3 gốc vì chính thức hoá sau khi Report 3 được viết): 5 màn Academic Year Management (UC-174–177) + 6 màn Violation Management (UC-178–183), xem khối cuối bảng. **Cập nhật 11/07/2026**: UC-178 (Record Process Violation) đã Deprecated — gộp UI vào UC-179 (dialog hợp nhất), còn thực tế **5 màn hình** cho Violation Management.
> 3. **Cột "UC/Ghi chú"**: điền đầy đủ cho System Admin (27 màn), School Administration (38 màn) và 11 màn mới — đã đối chiếu trực tiếp Normal Flow thật trong `Report 3.md` §3.2 khi viết lại `01_System_Admin_Screen_Flow.md`/`02_School_Administration_Screen_Flow.md` (2026-07-01). Để **trống (—)** cho 5 actor field còn lại (Teacher/Raw Food Supplier/Medical Staff/Service Caterers/Parent) vì UC của nhóm này được đặc tả field-by-field trực tiếp trong Report 3 §3.3.2 — xem UC chi tiết tại file ScreenFlow tương ứng, không lặp lại ở đây để tránh lệch dữ liệu giữa 2 tài liệu theo thời gian.
> **Không dùng làm nguồn để suy ra Business Rule/System Message** — chỉ dùng cho mục đích xác định "actor nào thấy màn hình nào, ở nền tảng nào". BR/MSG xem `business_rules.md`, `error_message.md`; UC đầy đủ xem `traceability_matrix.md`.

## Chú giải

- **SA** = System Admin · **ScA** = School Administration · **T** = Teacher · **RFS** = Raw Food Supplier · **MS** = Medical Staff · **SC** = Service Caterers · **P** = Parent
- Nền tảng: **Web** (chỉ Next.js) · **Web + Mobile** (Next.js song song Flutter, dùng chung UC/BR/MSG — xem `Naming_Convention.md`)
- 🆕 = màn hình bổ sung 2026-07-01 (Academic Year Management / Violation Management), chưa có trong Report 3 gốc

---

## 1. Common & Authentication (dùng chung mọi actor)

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Login | x | x | x | x | x | x | x | Web + Mobile | UC-034 |
| Forgot Password | x | x | x | x | x | x | x | Web + Mobile | UC-032 |
| Reset Password | x | x | x | x | x | x | x | Web + Mobile | UC-032 |
| Verify OTP | x | x | x | x | x | x | x | Web + Mobile | — |
| Profile Management | x | x |  |  | x | x |  | Web + Mobile | UC-097, UC-059, UC-138 |
| Change Password | x | x |  |  | x | x |  | Web + Mobile | UC-011 |
| Notification Center/List |  | x | x |  |  | x | x | Web + Mobile | — |
| Notification Detail |  |  | x |  |  |  | x | Web + Mobile | — |
| Notification Settings | x | x | x | x | x | x | x | Web + Mobile | UC-088 |

## 2. System Admin (Web only) — 19 màn hình *(sửa 2026-07-01: gỡ "Campus Config" — hệ thống không quản lý campus, xem `Web/SystemAdmin/13_CampusConfig.md`)*

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Audit Logs | x | x |  |  |  |  |  | Web | UC-106 |
| System Administrator Dashboard | x |  |  |  |  |  |  | Web | entry point |
| Manage User Screen/Dialog | x |  |  |  |  |  |  | Web | UC-026 |
| User Account List | x |  |  |  |  |  |  | Web | UC-153 |
| Manage Role Screen | x |  |  |  |  |  |  | Web | UC-022, UC-098, UC-029 |
| Role List | x |  |  |  |  |  |  | Web | UC-130 |
| Manage Permission List Screen | x |  |  |  |  |  |  | Web | UC-129 (chỉ xem) |
| Active/Deactivate User Dialog | x |  |  |  |  |  |  | Web | UC-003 |
| Active/Deactivate School Dialog | x |  |  |  |  |  |  | Web | UC-001 |
| School List | x |  |  | x |  |  |  | Web + Mobile | UC-140 |
| School Detail | x |  |  | x |  |  |  | Web + Mobile | UC-122 |
| Update School Screen/Dialog | x |  |  |  |  |  |  | Web | UC-095 |
| Backup Management | x |  |  |  |  |  |  | Web | bundled trong UC-149 (BR-160) |
| Security Policies | x |  |  |  |  |  |  | Web | UC-184 (UC mới, bổ sung 2026-07-01 — audit A8) |
| AI Settings | x |  |  |  |  |  |  | Web | 1 phần của UC-040 |
| System Activity Monitoring | x |  |  |  |  |  |  | Web | UC-057 |
| System Status Screen | x |  |  |  |  |  |  | Web | UC-149 |
| Error Logs Screen | x |  |  |  |  |  |  | Web | UC-111 |
| Usage Statistics Screen | x |  |  |  |  |  |  | Web | UC-150 |

## 3. School Administration (Web only) — 39 màn hình *(sửa 2026-07-01: đếm lại trực tiếp từ bảng ra 39 dòng, không phải 38 — phát hiện bởi Nguyễn Văn Nam khi viết Screen Specification)*

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| School Administration Dashboard |  | x |  |  |  |  |  | Web | entry point |
| Catering Partnership Requests |  | x |  |  |  |  |  | Web | UC-127 (lọc Pending) |
| Supplier Partnership Requests |  | x |  |  |  |  |  | Web | UC-131 (lọc Pending) |
| Service Caterers Detail |  | x |  |  |  |  |  | Web | UC-108, UC-004 |
| Supplier Detail |  | x |  |  |  |  |  | Web | UC-148, UC-007 |
| Menu Management |  | x |  |  |  |  |  | Web | UC-128 |
| Menu Detail |  | x |  |  |  |  |  | Web | UC-134, UC-006 — 🗑️ gỡ cột Parent 2026-07-01: UC-134 xác nhận Primary Actor = School Administration, Secondary Actors = None (không có Parent); 4 file ScreenFlow của Parent (Mobile+Web) chưa từng có node "Menu Detail"; 18 file Screen Spec thật của Parent cũng không có file này — Parent xem đủ qua Weekly Menu + Nutrition Detail |
| Incident Dashboard |  | x |  |  |  |  |  | Web | entry point |
| Incident List |  | x |  |  | x |  |  | Web + Mobile | UC-114 |
| Incident Detail |  | x |  |  | x |  |  | Web + Mobile | UC-116 — 🗑️ gỡ cột Teacher 2026-07-01: UC-116 xác nhận Primary Actor = School Administration, Secondary Actors = None; ScreenFlow của Teacher (Mobile+Web) chưa từng có node "Incident Detail"; file thật `Web/Teacher/13_IncidentHistory.md` chỉ có bảng tổng hợp read-only (không có action xem chi tiết/modal) — Teacher xem đủ qua Incident History |
| Corrective Action Review |  | x |  |  |  |  |  | Web | UC-005 |
| Resolution Report |  | x |  |  |  |  |  | Web | UC-013, UC-121 |
| Food Sample Retention Reports |  | x |  |  |  |  |  | Web | UC-171, UC-172 (đã có Functional Spec, bổ sung 2026-07-01) |
| Receiving Reports |  | x |  |  |  |  |  | Web | UC-123 (View Ingredient Receiving Records — xác nhận 2026-07-01) |
| Inspection Reports |  | x |  |  |  |  |  | Web | UC-113 |
| Partnership Reports |  | x |  |  |  |  |  | Web | UC-171, UC-172 (đã có Functional Spec, bổ sung 2026-07-01) |
| Partnership Approval History |  | x |  |  |  |  |  | Web | UC-106 (View Audit Logs, lọc theo Partnership — xác nhận 2026-07-01) |
| Compliance Dashboard |  | x |  |  | x |  |  | Web + Mobile | UC-027 |
| Send Compliance Report Dialog 🆕 |  |  |  |  | x |  |  | Web + Mobile | UC-185 (bổ sung 05/07/2026 — chưa từng thêm vào bảng này, vá khoảng trống) |
| Manage Kitchen Inspection Checklist 🆕 |  |  |  |  | x |  |  | Web + Mobile | UC-186 (bổ sung 06/07/2026 — chưa từng thêm vào bảng này, vá khoảng trống) |
| ATTP Report Center 🆕 |  |  |  |  | x |  |  | Web + Mobile | UC-187 (mới 13/07/2026) |
| Send Incident Summary Dialog 🆕 |  |  |  |  | x |  |  | Web + Mobile | UC-188 (mới 13/07/2026) |
| Send Violation Summary Dialog 🆕 |  |  |  |  | x |  |  | Web + Mobile | UC-189 (mới 13/07/2026) |
| Sent Reports History 🆕 |  |  |  |  | x |  |  | Web + Mobile | UC-190 (mới 13/07/2026) |
| Classroom Management |  | x |  |  |  |  |  | Web | UC-037, UC-166 |
| Classroom Detail |  | x |  |  |  |  |  | Web | UC-165 (+ action UC-008, UC-090) |
| Create Classroom |  | x |  |  |  |  |  | Web | UC-018 |
| Student Profile Management |  | x |  |  |  |  |  | Web | UC-147 (+ action UC-025) |
| Student Profile Detail |  | x |  |  |  |  |  | Web | UC-099 |
| Bulk Import Student Profiles |  | x |  |  |  |  |  | Web | UC-010 |
| Parent Account Detail |  | x |  |  |  |  |  | Web | bundled trong UC-025 |
| Link Parent Account |  | x |  |  |  |  |  | Web | bundled trong UC-025 |
| Linked Students List |  | x |  |  |  |  |  | Web | bundled trong UC-025 |
| School Announcement Management |  | x |  |  |  |  |  | Web | UC-125 |
| Create Announcement |  | x |  |  |  |  |  | Web | UC-017 |
| Update Announcement |  | x |  |  |  |  |  | Web | UC-089 |
| Announcement Detail |  | x |  |  |  |  |  | Web | UC-105 |
| Staff Management |  | x |  |  |  |  |  | Web | UC-144 |
| Upload Staff List |  | x |  |  |  |  |  | Web | UC-024 (biến thể bulk) |
| Invitation Status |  | x |  |  |  |  |  | Web | bundled, theo dõi BR-010 |
| System Usage Metrics |  | x |  |  |  |  |  | Web | UC-171 (đã có Functional Spec, bổ sung 2026-07-01) |
| Traceability Reports |  | x |  |  |  |  |  | Web | UC-171, UC-172 (đã có Functional Spec, bổ sung 2026-07-01) |
| School Configuration |  | x |  |  |  |  |  | Web | UC-095 (Update Information School, phần School-level — xác nhận 2026-07-01) |
| Verification Documents |  | x |  |  |  |  |  | Web | bundled trong UC-007/UC-004 |
| Allergy Review |  | x |  |  |  |  |  | Web | TODO — chưa có UC chính thức cho School Admin |

*(Export Incident Reports — UC-031 — không có dòng riêng trong Screen Authorization gốc nhưng có UC/Screen Spec chính thức; xem `02_School_Administration_Screen_Flow.md`.)*

## 4. Teacher (Web + Mobile) — 13 màn hình

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Teacher Home |  |  | x |  |  |  |  | Web + Mobile | — |
| Student List |  |  | x |  |  |  |  | Web + Mobile | — |
| Student Detail |  |  | x |  |  |  |  | Web + Mobile | — |
| Allergy Information |  |  | x |  | x |  | x | Web + Mobile | — |
| Health Information |  |  | x |  |  |  |  | Web + Mobile | — |
| Special Meal Requirements |  |  | x |  |  |  |  | Web + Mobile | — |
| Meal Records |  |  | x |  |  |  |  | Web + Mobile | — |
| Distribution List |  |  | x |  |  |  |  | Web + Mobile | — |
| Distribution Detail |  |  | x |  |  |  |  | Web + Mobile | — |
| Distribution Confirmation |  |  | x |  |  |  |  | Web + Mobile | — |
| Upload Evidence |  |  | x |  |  |  |  | Web + Mobile | — |
| Incident Form |  |  | x |  |  |  |  | Web + Mobile | — |
| Incident History |  |  | x |  |  |  |  | Web + Mobile | — |

## 5. Raw Food Supplier (Web + Mobile) — 22 màn hình

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Supplier Dashboard |  |  |  | x |  |  |  | Web + Mobile | — |
| Select School Workspace |  |  |  | x |  |  |  | Web + Mobile | — |
| School Partnerships |  |  |  | x |  |  |  | Web + Mobile | — |
| Product Origin Documents |  |  |  | x |  |  |  | Web + Mobile | — |
| Food Safety Certificates |  |  |  | x |  |  |  | Web + Mobile | — |
| Upload Supplier Certificates |  |  |  | x |  |  |  | Web + Mobile | — |
| Partnership Application |  |  |  | x |  |  |  | Web + Mobile | — |
| Application Status |  |  |  | x |  |  |  | Web + Mobile | UC-169 |
| Purchase Requests |  |  |  | x |  |  |  | Web + Mobile | — |
| Purchase Request List |  |  |  | x |  |  |  | Web + Mobile | UC-139 |
| Purchase Request Detail |  |  |  | x |  |  |  | Web + Mobile | — |
| Ingredient Batches |  |  |  | x |  |  |  | Web + Mobile | — |
| Batch List |  |  |  | x |  |  |  | Web + Mobile | UC-043 |
| Batch Detail |  |  |  | x |  |  |  | Web + Mobile | — |
| Submit Batch Information |  |  |  | x |  |  |  | Web + Mobile | UC-081 |
| Upload Batch Certificates |  |  |  | x |  |  |  | Web + Mobile | UC-100 |
| View Batch History |  |  |  | x |  |  |  | Web + Mobile | UC-107 |
| Deliveries |  |  |  | x |  |  |  | Web + Mobile | — |
| Delivery List |  |  |  | x |  |  |  | Web + Mobile | UC-039 |
| Delivery Detail |  |  |  | x |  |  |  | Web + Mobile | — |
| Submit Delivery Information |  |  |  | x |  |  |  | Web + Mobile | UC-083 |
| Confirm Delivery Schedule |  |  |  | x |  |  |  | Web + Mobile | UC-015 |
| Update Delivery Status |  |  |  | x |  |  |  | Web + Mobile | UC-092 |
| View Delivery Batch Records |  |  |  | x |  |  |  | Web + Mobile | UC-109 |
| Update Documents |  |  |  | x |  |  |  | Web + Mobile | UC-091 |
| Compliance Documents |  |  |  | x |  |  |  | Web + Mobile | UC-167 |

## 6. Medical Staff (Web + Mobile) — 20 màn hình

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Medical Staff Dashboard |  |  |  |  | x |  |  | Web + Mobile | — |
| Review Health Information Screen |  |  |  |  | x |  |  | Web + Mobile | UC-077 |
| Student Health Record List |  |  |  |  | x |  |  | Web + Mobile | UC-051 |
| Student Health Record Detail |  |  |  |  | x |  |  | Web + Mobile | — |
| Update Health Record Screen |  |  |  |  | x |  |  | Web + Mobile | UC-094 |
| Health History Screen |  |  |  |  | x |  |  | Web + Mobile | UC-146 |
| Review Special Meal Request Screen |  |  |  |  | x |  |  | Web + Mobile | UC-078 |
| Special Meal Request List |  |  |  |  | x |  |  | Web + Mobile | UC-036 |
| Approve Special Meal Dialog |  |  |  |  | x |  |  | Web + Mobile | — |
| Reject Special Meal Dialog |  |  |  |  | x |  |  | Web + Mobile | — |
| Food Inspection Records |  |  |  |  | x |  |  | Web + Mobile | UC-112 |
| Conduct Food Safety Inspection Screen |  |  |  |  | x |  |  | Web + Mobile | UC-014 |
| Food Sample Records |  |  |  |  | x |  |  | Web + Mobile | UC-118 |
| Retrieve Food Sample Records Dialog |  |  |  |  | x |  |  | Web + Mobile | UC-076 |
| View Food Safety Reports Screen |  |  |  |  | x |  |  | Web + Mobile | UC-117 |
| View School Policy Screen |  |  |  |  | x |  |  | Web + Mobile | UC-137 |
| Investigation Workspace |  |  |  |  | x |  |  | Web + Mobile | UC-041 |
| Assign Incident Severity Dialog *(Deprecated 15/07/2026 — gộp trình bày vào Incident Detail, UC-009 vẫn giữ nguyên quyền hạn)* |  |  |  |  | x |  |  | Web + Mobile | UC-009 |
| Create Incident Record Dialog 🆕 *(mới 15/07/2026)* |  |  |  |  | x |  |  | Web + Mobile | UC-114 (mở rộng) |
| Record Corrective Actions Report Screen *(gộp trình bày với Generate Incident Report Dialog từ 15/07/2026, cùng điểm vào "Hoàn tất sự cố")* |  |  |  |  | x |  |  | Web + Mobile | UC-168 |
| Generate Incident Report Dialog *(gộp trình bày với Record Corrective Actions Report Screen từ 15/07/2026)* |  |  |  |  | x |  |  | Web + Mobile | UC-033 |

*(View Food Safety Compliance Report — UC-115 — không có dòng riêng trong Screen Authorization gốc, gộp chung với "View Food Safety Reports Screen"; xem `03_Medical_Staff_Screen_Flow.md`.)*

## 7. Service Caterers (Web + Mobile) — 35 màn hình

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Service Caterers Dashboard |  |  |  |  |  | x |  | Web + Mobile | — |
| Menu Management Dashboard |  |  |  |  |  | x |  | Web + Mobile | UC-048 |
| Create Menu Workspace |  |  |  |  |  | x |  | Web + Mobile | UC-020 |
| View Menu Recommendations Panel (AI Integration) |  |  |  |  |  | x |  | Web + Mobile | UC-075 |
| Submit Menu For Approval Trigger |  |  |  |  |  | x |  | Web + Mobile | UC-087 |
| Update Menu Screen |  |  |  |  |  | x |  | Web + Mobile | UC-096 |
| Receiving Management Dashboard |  |  |  |  |  | x |  | Web + Mobile | — |
| Receive Ingredients Workspace (Start Receiving Session) |  |  |  |  |  | x |  | Web + Mobile | UC-044 |
| Verify Supplier Delivery Panel |  |  |  |  |  | x |  | Web + Mobile | — |
| Capture Receiving Evidence Screen |  |  |  |  |  | x |  | Web + Mobile (Mobile ưu tiên — GPS/camera) | bundled UC-044 |
| Store Ingredients Form (Storage Assignment) |  |  |  |  |  | x |  | Web + Mobile | UC-061 |
| Reject Ingredient Batch Form |  |  |  |  |  | x |  | Web + Mobile | UC-072 |
| Record Ingredient Receiving |  |  |  |  |  | x |  | Web + Mobile | UC-060 |
| Receiving History & Reports |  |  |  |  |  | x |  | Web + Mobile | — |
| Three-Step Inspection Dashboard |  |  |  |  |  | x |  | Web + Mobile (Mobile ưu tiên) | UC-052 |
| Inspect Ingredients Before Processing Screen (Step 1) |  |  |  |  |  | x |  | Web + Mobile (Mobile ưu tiên — GPS) | UC-062 |
| Upload Image Cooked Food Screen (Step 2) |  |  |  |  |  | x |  | Web + Mobile (Mobile ưu tiên — GPS) | UC-063 |
| Inspect Meals Before Serving Screen (Step 3) |  |  |  |  |  | x |  | Web + Mobile (Mobile ưu tiên — GPS) | UC-065 |
| Record Preparation Information Form (Cooking Record) |  |  |  |  |  | x |  | Web + Mobile | UC-066 |
| View Preparation Records Ledger |  |  |  |  |  | x |  | Web + Mobile | — |
| Record Sample Information Form |  |  |  |  |  | x |  | Web + Mobile (Mobile ưu tiên — GPS) | UC-069 |
| Food Sample Detail |  |  |  |  |  | x |  | Web + Mobile | — |
| Food Sample Retention Dashboard |  |  |  |  |  | x |  | Web + Mobile | UC-042 |
| Meal Service Management Dashboard |  |  |  |  |  | x |  | Web + Mobile | UC-047 |
| View Meal Distribution List |  |  |  |  |  | x |  | Web + Mobile | UC-132 |
| View Allergy Alerts Panel (Special Meal Monitoring) |  |  |  |  |  | x |  | Web + Mobile | UC-103 |
| Manage Special Meals Configuration |  |  |  |  |  | x |  | Web + Mobile | — |
| Inventory Management Dashboard |  |  |  |  |  | x |  | Web + Mobile | UC-045 |
| Issue Ingredients Workspace |  |  |  |  |  | x |  | Web + Mobile | — |
| Monitor Expiration Dates Panel (Expiration Alerts) |  |  |  |  |  | x |  | Web + Mobile | — |
| Record Inventory Transactions Log |  |  |  |  |  | x |  | Web + Mobile | UC-064 |
| Corrective Actions Dashboard |  |  |  |  |  | x |  | Web + Mobile | UC-038 |
| Submit Corrective Action Results Form |  |  |  |  |  | x |  | Web + Mobile | UC-082 |
| View Inspection Records Log |  |  |  |  |  | x |  | Web + Mobile | UC-124 |
| View Incident Information Detail |  |  |  |  |  | x |  | Web + Mobile | UC-120 |

*(Create Purchase Request — UC-021 — không có dòng riêng trong Screen Authorization gốc, truy cập từ Inventory Management Dashboard; xem `04_Service_Caterers_Screen_Flow.md`.)*

## 8. Parent (Web + Mobile) — 18 màn hình

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Parent Dashboard |  |  |  |  |  |  | x | Web + Mobile | — |
| Weekly Menu |  |  |  |  |  |  | x | Web + Mobile | UC-135 |
| Nutrition Detail |  |  |  |  |  |  | x | Web + Mobile | — |
| Nutrition History |  |  |  |  |  |  | x | Web + Mobile | — |
| Submit Allergy Information |  |  |  |  |  |  | x | Web + Mobile | UC-084 |
| Update Allergy Information |  |  |  |  |  |  | x | Web + Mobile | UC-093 |
| Student Health Information |  |  |  |  |  |  | x | Web + Mobile | UC-050 |
| Submit Health Information |  |  |  |  |  |  | x | Web + Mobile | UC-084 |
| Update Health Information |  |  |  |  |  |  | x | Web + Mobile | UC-093 |
| Special Meal Arrangements |  |  |  |  |  |  | x | Web + Mobile | UC-141 |
| Special Meal Records |  |  |  |  |  |  | x | Web + Mobile | UC-142 |
| Meal History |  |  |  |  |  |  | x | Web + Mobile | UC-133 |
| Meal History Detail |  |  |  |  |  |  | x | Web + Mobile | — |
| School Meals Dashboard |  |  |  |  |  |  | x | Web + Mobile | UC-055 |
| Feedback Center |  |  |  |  |  |  | x | Web + Mobile | — |
| Submit Meal Feedback |  |  |  |  |  |  | x | Web + Mobile | UC-085, UC-086 |
| Submit Food Quality Feedback |  |  |  |  |  |  | x | Web + Mobile | UC-085 |
| Report Food Safety Concern |  |  |  |  |  |  | x | Web + Mobile | UC-073 |

---

## 9. 🆕 Academic Year Management (mới 2026-07-01 — mở rộng School & Class Management)

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Academic Year List 🆕 |  | x |  |  |  |  |  | Web | UC-176 |
| Create Academic Year 🆕 |  | x |  |  |  |  |  | Web | UC-174 |
| Academic Year Detail 🆕 |  | x |  |  |  |  |  | Web | bundled (UC-175, UC-177) |
| Activate/Close Academic Year Dialog 🆕 |  | x |  |  |  |  |  | Web | UC-175 |
| Promote Students Workspace 🆕 |  | x |  |  |  |  |  | Web | UC-177 |

## 10. 🆕 Violation Management (mới 2026-07-01 — mở rộng Food Safety Inspection + Incident & Emergency Management)

| Màn hình | SA | ScA | T | RFS | MS | SC | P | Nền tảng | UC/Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ~~Record Process Violation~~ 🆕 |  |  |  |  |  |  |  | — | UC-178, **Deprecated 11/07/2026** — gộp vào dòng dưới |
| Record Violation (dialog hợp nhất) 🆕 |  |  |  |  | x | x |  | Web + Mobile | UC-179 (mã sống sót) |
| Record Violation Resolution 🆕 |  | x |  |  | x |  |  | Web (ScA) + Web/Mobile (MS) | UC-180 |
| Violation Registry 🆕 |  | x |  |  | x |  |  | Web (ScA) + Web/Mobile (MS) | UC-181 |
| Approve/Reject Violation Resolution 🆕 |  | x |  |  |  |  |  | Web | UC-182 |
| Weekly Override Abuse Report 🆕 |  | x |  |  |  |  |  | Web | UC-183 |

---

## Tổng số màn hình

| Khối | Số màn hình |
| --- | --- |
| Common & Authentication | 9 |
| System Admin | 19 *(sửa 2026-07-01: gỡ "Campus Config" — hệ thống không quản lý campus)* |
| School Administration | 40 *(39 dòng bảng + 1 "Export Incident Reports" ở ghi chú cuối khối — sửa 2026-07-01, trước ghi nhầm 38)* |
| Teacher | 13 |
| Raw Food Supplier | 26 |
| Medical Staff | 20 *(chưa gồm 3 màn dùng chung với School Administration đã đếm ở trên: Incident List, Incident Detail, Compliance Dashboard — xem mục 5 để biết Medical Staff cần viết thêm 3 file riêng cho các màn này)* |
| Service Caterers | 35 |
| Parent | 18 |
| 🆕 Academic Year Management | 5 |
| 🆕 Violation Management | 6 |
| **Tổng (màn hình duy nhất)** | **191** *(sửa 2026-07-01 từ 192 → 191, gỡ Campus Config)* |

*(Lưu ý: 192 là số màn hình **duy nhất** trong hệ thống. Số **file Screen Specification cần viết** cao hơn vì màn dùng chung nhiều actor cần 1 file riêng theo mỗi actor — xem `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` mục 2 để có đúng số file theo từng người.)*

*(Chênh lệch so với số ước lượng trong `00_UI_Design_Documentation_Plan.md` mục 5 — ~140 màn — là do bảng gốc Report 3 thực tế có ~180 dòng (dòng 5057–5236), không phải ~140 như ước lượng ban đầu trước khi đọc trực tiếp toàn bộ bảng. Số 179 ở trên là tổng đếm trực tiếp theo từng khối actor; chênh lệch 1 dòng so với ước lượng khoảng cách dòng gốc (180) là do dòng header/khoảng trắng, không phải màn hình bị bỏ sót.)*

## Nguồn dữ liệu
- `Report 3.md` mục Screen Authorization (dòng 5053–5236) — nguồn chính cho 180 màn hình gốc.
- `03_Design/04_ScreenFlow/01_System_Admin_Screen_Flow.md`, `02_School_Administration_Screen_Flow.md` — nguồn đối chiếu UC cho 2 actor Web-only.
- `business_rules.md` §18–19, `02_Requirements/AcademicYear_Management_Proposal.md`, `Violation_Management_Proposal.md` — nguồn cho 11 màn hình mới.
- `03_Design/00_UI_Design_Documentation_Plan.md` mục 0, 5, 8b — quyết định "all web + Mobile song song" dùng để suy ra cột Nền tảng.
