# Traceability Matrix — School Food Safety Management Support System (SFMS)

> **Source**: Business Rules (business_rules.md) + Report 3 SRS (Use Case Descriptions & Functional Specifications)  
> **Version**: 1.4  
> **Date**: 2026-08-04 *(bản gốc 28/06/2026)*  
> **Changelog v1.4 (2026-08-04)**: **Đóng 3 mục treo của MF11** (`TDS-MF11_Backend.md` §2.5 **H4/H5/H6**), mở khoá MF11 chuyển Approved. Quyết định (Huy) 2026-08-04. (1) **§1.11 bổ sung 3 BR đã có trong `business_rules.md` nhưng thiếu ở ma trận**: **BR-216** (vòng đời CAPA 6 trạng thái, 1 Incident = 1 CAPA → UC-005/038/082/168), **BR-217** (cách ly lô một chiều → UC-041/013), **BR-188** (audit log bất biến hồ sơ vi phạm → UC-179/180/182). (2) **§1.11 gán 13 MSG mới của `error_message.md` v1.11** (MSG-149…161) và **gỡ 3 MSG dùng sai ngữ nghĩa**: MSG-029 (*"Profile updated successfully"*) khỏi UC-005/009/013 → MSG-160/159/161; MSG-004 (*"Session expired"*) khỏi UC-168 → MSG-153. (3) **UC-179 đổi tên** *"Record Staff Violation"* → *"Record **Unit** Violation"* và **BR-184 đánh dấu Deprecated** — BR-183/185/187 bản 26/07/2026 đã chuyển đối tượng lập biên bản từ **nhân viên** sang **đơn vị Nhà thầu**. (4) **Ghi chú 4 dưới §1.11 sửa cửa sổ món nghi vấn 48h → 24h** cho khớp BR-149 bản 27/07/2026 và BR-111 (hạn lưu mẫu 24h) — ghi chú cũ viết 15/07, lạc hậu 12 ngày; để nguyên thì người hiện thực lấy 48h và đề xuất điều tra những món **không còn mẫu đối chứng để xét nghiệm**. **Bài học lan sang module khác:** cả 4 mục đều là *"ma trận nói một đằng, `business_rules.md` nói một nẻo"* — khi hai nguồn lệch, **BR bản mới nhất thắng**, và phải **sửa ma trận** chứ không chỉ ghi chú trong TDS, vì ma trận là thứ người viết TDS đọc đầu tiên (§2c bắt buộc).  
> **Changelog v1.3 (2026-07-29)**: Bổ sung **§3.1** — ánh xạ MSG-104–138 (mã lỗi cục bộ TDS vừa được chính thức hóa ở `error_message.md` v1.5) về BR và UC. 19/35 mã hiện **chưa xác định được UC** vì TDS chỉ gắn chúng vào BR — đã đánh dấu ⚠️ kèm tên người phụ trách bổ sung, không suy diễn UC từ nội dung BR. Ghi nhận chính thức 2 xung đột treo của MF01 (CF-1: BR-015 ↔ UC-011/UC-032; CF-2: UC-154 lệch giữa file UC và §1.1).  
> **Changelog v1.1**: Sửa định nghĩa BR-013 (Phân quyền tạo nhân viên) và BR-068 (Quy trình phê duyệt thực đơn); bổ sung BR-080, BR-081, BR-082 (Menu); bổ sung BR-175–182 (Reporting & Analytics) và Section 1.13; gộp UC-062/UC-063 về Section 1.5; xóa BR-117/BR-118 không tồn tại; sửa MSG-013 loại bỏ UC-070 sai; đổi UC-059_inc → UC-168.
> **Changelog v1.2 (2026-08-02)**: **Đóng 3 mục treo của MF04.** (1) **CF-M04-2** — §1.4 bổ sung `MSG-013`→`UC-101`, `MSG-014`→`UC-068` (vốn đã có ở `UC-079`), `MSG-007`→`UC-158`; bảng MSG→UC ngược đồng bộ theo. **`MSG-025` cố ý KHÔNG gắn UC** — đây là lỗi hạ tầng dùng chung (`error_message.md` §4 xếp vào *AI & System Services*), phát sinh khi transaction rollback ở bất kỳ module nào, không thuộc luồng nghiệp vụ của UC nào. (2) **CF-M04-1** — `BR-010` bổ sung `UC-067` (Register Service Caterers Account) và `UC-071` (Register Supplier Account): hai UC này tạo tài khoản nên phải qua xác minh email. (3) `BR-041` đổi nhãn thành `Pending/Approved/Rejected/Suspended` và `BR-060` đổi nhãn thành *"Mã hóa dữ liệu sinh trắc học (AES-256)"* — khớp `business_rules.md` v1.11. Ma trận vốn đã ghi đúng nội dung AES-256 cho BR-060; trước v1.11 hai file mâu thuẫn nhau về chính định nghĩa của BR này.
| 2026-07-01 | Merge phê duyệt | Chính thức hoá Academic Year Management (UC-174–177, BR-191–195, mở rộng §1.2) và Violation Management (UC-178–183, BR-183–190, mở rộng §1.7 + §1.11); bổ sung MSG-080–089 vào §3; khôi phục bảng §4 (Tóm tắt độ phủ theo Module) bị cắt cụt do lỗi ghi file trước đó (chỉ còn 4/13 dòng, phần còn lại được tính lại từ dữ liệu §1/§2 thực tế). |
| 2026-06-28 | MinhTTHE186258 — QA Lead | Bổ sung MSG-076 (Account locked BR-002) vào §1.1 UC-034, §3, §4; đồng bộ sau khi fix TDS-001 |
| 2026-06-28 | MinhTTHE186258 — QA Lead | Bổ sung BR-028 (GPS coordinate validation & lock) vào §1.2 UC-070, UC-095 và §2; đồng bộ với business_rules.md |
| 2026-07-01 | Reconciliation pass | **Nguồn ID chuẩn chuyển sang "UC ID and Name" trong Report 3 Functional Specifications** (không còn dùng bảng "Use Cases Description" tổng hợp hay số ID tạm thời làm chuẩn). Đổi: UC-151→UC-153 (View User Account List), UC-160→UC-154 (Activate Account & Set Password), UC-037(Manage Compliance Documents)→UC-167, UC-059B→UC-168 (Record Corrective Actions Report), UC-153(Track Supplier Application Status)→UC-169, UC-154(Update/Resubmit Supplier Application)→UC-170, UC-038 (Policy)→UC-137 (View Policy Of School), UC-161/162/163 (Reporting & Analytics)→UC-171/172/173 (giải phóng 161–163 vì Report 3 đã dùng các số này cho module Meal Feedback); bổ sung §1.10 các UC-161–164 Meal Feedback còn thiếu. Chi tiết bằng chứng: xem `UC_ID_Reconciliation_Report.md`. |

---

## Mục lục

1. [Ma trận UC ↔ Business Rules](#1-ma-trận-uc--business-rules)
2. [Ma trận Business Rules ↔ Use Cases](#2-ma-trận-business-rules--use-cases)
3. [Ma trận UC ↔ System Messages](#3-ma-trận-uc--system-messages)
4. [Tóm tắt độ phủ theo Module](#4-tóm-tắt-độ-phủ-theo-module)

---

## 1. Ma trận UC ↔ Business Rules

Mỗi Use Case ánh xạ đến các Business Rules mà nó phải tuân thủ.

### 1.1 User Management & Access Control

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-034 | Login | User | BR-002, BR-004, BR-005, BR-009, BR-010 | MSG-002, MSG-003, MSG-004, MSG-026, MSG-062, MSG-076 |
| UC-035 | Logout | User | BR-004 | MSG-004, MSG-005, MSG-027 |
| UC-011 | Change Password | User | BR-001, BR-006, BR-007 | MSG-002, MSG-004, MSG-005, MSG-030, MSG-063 |
| UC-032 | Forgot Password | User | BR-001, BR-003, BR-006 | MSG-003, MSG-030 |
| UC-154 | Activate Account & Set Password | User | BR-010, BR-011 | MSG-028 |
| UC-097 | Update Profile | User | BR-012 | MSG-004, MSG-005, MSG-008, MSG-009, MSG-029 |
| UC-059 | Profile Manage | User | BR-012 | MSG-004, MSG-005 |
| UC-138 | View Profile | User | — | MSG-004, MSG-005 |
| UC-024 | Create Staff | School Administration | BR-010, BR-012, BR-013 | MSG-004, MSG-005, MSG-006, MSG-031 |
| UC-026 | Create User | System Admin | BR-008, BR-010, BR-012, BR-013 | MSG-004, MSG-005, MSG-006, MSG-031 |
| UC-002 | Active/Deactivate Staff | School Administration | BR-007, BR-008 | MSG-004, MSG-005, MSG-029 |
| UC-003 | Active/Deactivate User | System Admin | BR-007, BR-008 | MSG-004, MSG-005, MSG-029 |
| UC-022 | Create Role | System Admin | BR-008 | MSG-004, MSG-005, MSG-031 |
| UC-029 | Delete Role | System Admin | BR-008 | MSG-004, MSG-005 |
| UC-098 | Update Role | System Admin | BR-008 | MSG-004, MSG-005 |
| UC-129 | View Permission List | System Admin | BR-007 | MSG-004, MSG-005 |
| UC-130 | View Role List | System Admin | BR-007, BR-008 | MSG-004, MSG-005 |
| UC-144 | View Staff Account List | School Administration | BR-013 | MSG-004, MSG-005 |
| UC-153 | View User Account List | System Admin | BR-007 | MSG-004, MSG-005 |
| UC-191 | Login with Google (OAuth2) | User | BR-004, BR-005, BR-010, BR-218 | MSG-026, MSG-102, MSG-103 |

### 1.2 School & Class Management

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-070 | Register School | Guest | BR-028 | MSG-007, MSG-032 |
| UC-001 | Active/Deactivate School | System Admin | BR-021 | MSG-001 |
| UC-095 | Update Information School | System Admin | BR-028 | MSG-001, MSG-029 |
| UC-122 | View Information School | System Admin | — | MSG-004, MSG-005 |
| UC-140 | View School List | System Admin | — | MSG-004, MSG-005 |
| UC-018 | Create Classroom | School Administration | BR-024, BR-025, BR-192 | MSG-010, MSG-033, MSG-081 |
| UC-090 | Update Classroom | School Administration | BR-024, BR-025 | MSG-010, MSG-029 |
| UC-008 | Assign Homeroom Teacher | School Administration | BR-020, BR-026 | MSG-011, MSG-034 |
| UC-037 | Manage Classrooms | School Administration | BR-024, BR-025 | MSG-004, MSG-005 |
| UC-165 | View Classroom Detail | School Administration | BR-020, BR-024, BR-025 | MSG-004, MSG-005 |
| UC-166 | View List Classrooms | School Administration | BR-024, BR-025 | MSG-004, MSG-005 |
| UC-137 | View Policy Of School | Teacher/Medical/Caterers | BR-022 | MSG-004, MSG-005 |
| UC-174 | Create Academic Year | School Administration | BR-191, BR-192, BR-194 | MSG-080, MSG-082 |
| UC-175 | Activate/Close Academic Year | School Administration | BR-191, BR-193 | MSG-080, MSG-083 |
| UC-176 | View Academic Year List | School Administration, System Admin | BR-195 | MSG-004, MSG-005 |
| UC-177 | Promote Students to Next Academic Year | School Administration | BR-194 | MSG-084 |

> UC-137 has no detailed Functional Specification section in Report 3 yet — it only appears in the master summary table. GAP-005 (BR-019 → BR-025) is now resolved — all citations above use BR-025. **UC-174–177 (Academic Year Management) chính thức hoá 2026-07-01** từ `AcademicYear_Management_Proposal.md` — xem `business_rules.md` §19.

### 1.3 Student & Parent Management

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-025 | Create Student | School Administration | BR-030, BR-201 | MSG-012, MSG-031 |
| UC-099 | Update Student | School Administration | BR-030 | MSG-029 |
| UC-010 | Bulk Import Student & Parent | School Administration | BR-032 | MSG-035 |
| UC-147 | View Student List | School Administration | — | MSG-004, MSG-005 |
| UC-084 | Submit Health & Allergy Information | Parent | BR-030, BR-031 | MSG-036 |
| UC-093 | Update Health & Allergy Information | Parent | BR-031, BR-201 | MSG-029, MSG-036 |
| UC-077 | Review Health Information | Medical Staff | BR-031 | MSG-029 |
| UC-078 | Review Special Meal Requests | Medical Staff | BR-031, BR-065 | MSG-029 |
| UC-094 | Update Health Records | Medical Staff | BR-172 | MSG-029 |
| UC-050 | View List Student Health Information | Parent | BR-030, BR-201 | MSG-004, MSG-005 |
| UC-051 | View List Student Health Records | Medical Staff | BR-172 | MSG-004, MSG-005 |
| UC-104 | View Allergy Information | Medical Staff/Parent | BR-030 | MSG-004, MSG-005 |
| UC-145 | View Student Health & Allergy Info | Teacher | BR-030, BR-123 | MSG-004, MSG-005 |
| UC-146 | View Student Health History | Medical Staff | BR-172 | MSG-004, MSG-005 |
| UC-036 | View List Allergy & Special Meals | Medical Staff | BR-030, BR-065 | MSG-004, MSG-005 |

### 1.4 Supplier & Partner Management

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-071 | Register Supplier Account | Guest | BR-040, BR-046 | MSG-006, MSG-028 |
| UC-152 | Submit Supplier Application | Raw Food Supplier | BR-040, BR-046, BR-047, BR-048 | MSG-008, MSG-009, MSG-028 |
| UC-169 | Track Supplier Application Status | Raw Food Supplier | BR-041 | MSG-004, MSG-005 |
| UC-170 | Update/Resubmit Supplier Application | Raw Food Supplier | BR-040, BR-048 | MSG-029 |
| UC-155 | Manage Supplier Application | Raw Food Supplier | BR-040, BR-041 | MSG-004, MSG-005 |
| UC-007 | Approval / Reject Supplier | School Administration | BR-041, BR-048 | MSG-037 |
| UC-067 | Register Service Caterers Account | Guest | BR-040, BR-046 | MSG-006, MSG-028 |
| UC-156 | Submit Catering Application | Service Caterers | BR-040, BR-046 | MSG-028 |
| UC-157 | Track Catering Application Status | Service Caterers | BR-041 | MSG-004, MSG-005 |
| UC-158 | Update/Resubmit Catering Application | Service Caterers | BR-040 | MSG-007, MSG-029 |
| UC-159 | Manage Catering Application | Service Caterers | BR-041 | MSG-004, MSG-005 |
| UC-004 | Approval / Reject Service Caterers | School Administration | BR-041, BR-048 | MSG-037 |
| UC-019 | Create Info Delivery Person | Raw Food Supplier | BR-050 | MSG-031 |
| UC-068 | Register Face Data | Raw Food Supplier | BR-051, BR-060 | MSG-014, MSG-038 |
| UC-028 | Delete Face Data | Raw Food Supplier | BR-051 | MSG-029 |
| UC-167 | Manage Compliance Documents | Raw Food Supplier | BR-046, BR-047 | MSG-008, MSG-009, MSG-053 |
| UC-091 | Update Compliance Documents | Raw Food Supplier | BR-046, BR-047 | MSG-029, MSG-053 |
| UC-101 | Upload Food Safety Certificates | Raw Food Supplier | BR-046, BR-047 | MSG-008, MSG-009, MSG-013, MSG-053 |
| UC-102 | Upload Product Origin Documents | Raw Food Supplier | BR-046 | MSG-008, MSG-009 |
| UC-049 | Manage Profile | Raw Food Supplier | — | MSG-029 |
| UC-108 | View Service Caterers Detail | School Administration | — | MSG-004, MSG-005 |
| UC-110 | View Delivery Person List | Raw Food Supplier | BR-050 | MSG-004, MSG-005 |
| UC-127 | View List Service Caterers | School Administration | — | MSG-004, MSG-005 |
| UC-131 | View List Supplier | School Administration | — | MSG-004, MSG-005 |
| UC-148 | View Supplier Detail | School Administration | — | MSG-004, MSG-005 |

### 1.5 Ingredient & Inventory Management

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-015 | Confirm Delivery Schedule | Raw Food Supplier | BR-071 | MSG-004, MSG-005 |
| UC-039 | Manage Deliveries | Raw Food Supplier | BR-071 | MSG-004, MSG-005 |
| UC-083 | Submit Delivery Information | Raw Food Supplier | BR-071 | MSG-004, MSG-005 |
| UC-092 | Update Delivery Status | Raw Food Supplier | BR-071 | MSG-029, MSG-065 |
| UC-081 | Submit Batch Information | Raw Food Supplier | BR-071 | MSG-008, MSG-009 |
| UC-100 | Upload Batch Certificates | Raw Food Supplier | BR-071 | MSG-008, MSG-009 |
| UC-043 | Manage Ingredient Batches | Raw Food Supplier | BR-071 | MSG-004, MSG-005 |
| UC-044 | Manage Ingredient Receiving | Service Caterers | BR-070, BR-072, BR-073, BR-074, BR-078 | MSG-015, MSG-041 |
| UC-060 | Record Ingredient Receiving | Service Caterers | BR-070, BR-071, BR-072, BR-073, BR-074 | MSG-015, MSG-041 |
| UC-072 | Reject Ingredient Batch | Service Caterers | BR-078 | MSG-041 |
| UC-061 | Record Ingredient Storage | Service Caterers | BR-075, BR-202 | MSG-042 |
| UC-064 | Record Inventory Transactions | Service Caterers | BR-075, BR-076, BR-204 | MSG-042 |
| UC-045 | Manage Inventory | Service Caterers | BR-075, BR-077, BR-205 | MSG-018, MSG-042, MSG-052, MSG-054 |
| UC-021 | Create Purchase Request | Service Caterers | BR-077 | MSG-018 |
| UC-062 | Record Ingredients Before Processing | Service Caterers | BR-071, BR-076, BR-079, BR-083, BR-092, BR-095 | MSG-016 |
| UC-063 | Record Ingredients In Processing | Service Caterers | BR-076, BR-090, BR-093, BR-095, BR-096 | MSG-016, MSG-017 |
| UC-066 | Record Prepare Ingredient Information | Service Caterers | BR-076 | MSG-016 |
| UC-107 | View Batch History | Raw Food Supplier | BR-076 | MSG-004, MSG-005 |
| UC-109 | View Delivery Batch Records | Raw Food Supplier | BR-071 | MSG-004, MSG-005 |
| UC-123 | View Ingredient Receiving Records | School Administration | BR-076 | MSG-004, MSG-005 |
| UC-139 | View Purchase Requests | Raw Food Supplier | — | MSG-004, MSG-005 |

### 1.6 Menu & Diet Management

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-020 | Create Menu | Service Caterers | BR-061, BR-062, BR-063, BR-064, BR-069 | MSG-039, MSG-051, MSG-067, MSG-071 |
| UC-096 | Update Menu | Service Caterers | BR-061, BR-062, BR-063, BR-067, BR-068 | MSG-029, MSG-074, MSG-075 |
| UC-075 | Request AI Menu Recommendations | Service Caterers | BR-156, BR-064 | MSG-039, MSG-067, MSG-073 |
| UC-023 | Create Special Meal Plan | Service Caterers | BR-065, BR-066, BR-071, BR-073 | MSG-072 |
| UC-087 | Submit Menu For Approval | Service Caterers | BR-061, BR-063, BR-067, BR-068 | MSG-039, MSG-051 |
| UC-006 | Approval / Reject Menu | School Administration | BR-067, BR-068 | MSG-040, MSG-074 |
| UC-048 | Manage Menus | Service Caterers | BR-061, BR-067, BR-068 | MSG-039, MSG-040, MSG-074 |
| UC-128 | View List Menu | School Administration | — | MSG-004, MSG-005 |
| UC-134 | View Menu Detail | School Administration | — | MSG-004, MSG-005 |
| UC-135 | View Menu Information | Parent | BR-201 | MSG-004, MSG-005, MSG-090 |

### 1.7 Food Safety Inspection & Monitoring

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-052 | Manage Three-Step Food Inspection | Service Caterers | BR-079, BR-083, BR-090, BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099 | MSG-016, MSG-017, MSG-043, MSG-056 |
| UC-065 | Record Meals Before Serving | Service Caterers | BR-091, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099 | MSG-016, MSG-043, MSG-056 |
| UC-014 | Conduct Food Safety Inspection | Medical Staff | BR-100, BR-101, BR-102, BR-103, BR-104 | MSG-043 |
| UC-027 | Daily Food Safety Monitoring | School Administration | BR-100, BR-104 | MSG-004, MSG-005 |
| UC-054 | Monitor Food Safety | Medical Staff | BR-089, BR-100, BR-101, BR-102, BR-103, BR-104 | MSG-055, MSG-059 |
| UC-112 | View Food Inspection Records | Medical Staff | BR-099 | MSG-004, MSG-005 |
| UC-113 | View Food Inspection Reports | School Administration | BR-099 | MSG-004, MSG-005 |
| UC-115 | View Food Safety Compliance Report | Medical Staff | BR-100, BR-101 | MSG-004, MSG-005 |
| UC-117 | View Food Safety Reports | Medical Staff | — | MSG-004, MSG-005 |
| UC-124 | View Inspection Records | Service Caterers | BR-099 | MSG-004, MSG-005 |
| UC-178 | ~~Record Process Violation~~ `(Deprecated 11/07/2026)` | Service Caterers, Medical Staff | BR-183, BR-211 | MSG-085, MSG-086, MSG-095 |

> **UC-178 (Violation Management) chính thức hoá 2026-07-01** từ `Violation_Management_Proposal.md` — mở rộng `NonConformanceRecord` thành Biên bản Vi phạm Quy trình đầy đủ. Xem `business_rules.md` §18 và các UC liên quan UC-179–183 tại §1.11.
> **Deprecated 11/07/2026**: dialog nhập liệu đã gộp vào UC-179 (`22_RecordStaffViolation.md`, dialog hợp nhất) — không còn màn hình riêng cho UC-178. Giữ nguyên số hiệu UC-178 (không tái sử dụng), BR-183 vẫn áp dụng ở tầng dữ liệu qua chính UC-179. Chi tiết: `review/medical_staff/review_screen_medical_staff.md` Mục 10, `business_rules.md` BR-183/BR-211.

### 1.8 Food Sample Retention

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-069 | Register Food Sample | Service Caterers | BR-105, BR-106, BR-107, BR-108, BR-109, BR-114, BR-203 | MSG-019, MSG-044 |
| UC-042 | Manage Food Sample Retention | Service Caterers | BR-105, BR-110, BR-111, BR-112, BR-113, BR-114 | MSG-019, MSG-020, MSG-044, MSG-059 |
| UC-030 | Dispose Food Sample | Service Caterers | BR-112, BR-113 | MSG-004, MSG-005 |
| UC-076 | Retrieve Food Sample Records | Medical Staff | BR-113, BR-114, BR-212 *(mới 12/07/2026)* | MSG-004, MSG-005, MSG-023 |
| UC-118 | View Food Sample Records | Medical Staff / School Admin | BR-114 | MSG-004, MSG-005 |

### 1.9 Meal Serving & Distribution

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-047 | Manage Meal Service | Service Caterers | BR-120, BR-121, BR-125, BR-130 | MSG-021, MSG-045 |
| UC-016 | Confirm Meal Distribution | Teacher | BR-120, BR-121 | MSG-045 |
| UC-080 | Student Meal Consumption | Teacher | BR-121 | MSG-004, MSG-005 |
| UC-079 | Scan Face Data | Service Caterers | BR-051, BR-060 | MSG-014 |
| UC-103 | View Allergy Alerts | Service Caterers / Teacher | BR-123, BR-125 | MSG-004, MSG-005 |
| UC-132 | View Meal Distribution List | Service Caterers | BR-120 | MSG-004, MSG-005 |
| UC-053 | Monitor Child Meal History | Parent | BR-121, BR-133, BR-201 | MSG-004, MSG-005 |
| UC-055 | Monitor School Meals | Parent | BR-133 | MSG-004, MSG-005 |
| UC-056 | Monitor Student Meal History | Teacher | BR-121 | MSG-004, MSG-005 |
| UC-133 | View Meal History | Parent | BR-121, BR-133 | MSG-004, MSG-005 |
| UC-141 | View Special Meal Arrangements | Parent | BR-065, BR-201 | MSG-004, MSG-005 |
| UC-142 | View Special Meal Records | Parent | BR-065 | MSG-004, MSG-005 |
| UC-143 | View Special Meal Requirements | Teacher | BR-125, BR-065 | MSG-004, MSG-005 |

### 1.10 Feedback & Announcement Management

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-085 | Submit Meal & Food Quality Feedback | Parent | BR-139, BR-140, BR-142 | MSG-022, MSG-046, MSG-057, MSG-058 |
| UC-086 | Submit Meal Feedback | Teacher | BR-139 | MSG-022, MSG-046 |
| UC-046 | Manage Meal Feedback | Parent | BR-139, BR-141, BR-143 | MSG-046, MSG-057 |
| UC-017 | Create Announcement | School Administration | BR-138, BR-144 | MSG-047 |
| UC-089 | Update Announcement | School Administration | BR-144 | MSG-029, MSG-047 |
| UC-105 | View Announcement Detail | School Administration | — | MSG-004, MSG-005 |
| UC-125 | View List Announcement | School Administration | — | MSG-004, MSG-005 |
| UC-126 | View List Announcements | Parent / Teacher | — | MSG-004, MSG-005 |
| UC-088 | Manage Notification Preferences | User | BR-158, BR-158b | MSG-004, MSG-005 |
| UC-058 | Receive Notification | User | BR-138, BR-158, BR-159 | MSG-004 |
| UC-136 | View Notification History | User | — | MSG-004, MSG-005 |
| UC-161 | View List Meal Feedback | School Administration / Service Caterers | BR-140, BR-142 | MSG-004, MSG-005 |
| UC-162 | View Meal Feedback Detail | School Administration / Service Caterers | BR-145 | MSG-004, MSG-005 |
| UC-163 | Respond Meal Feedback | Service Caterers | BR-143 | MSG-004, MSG-005 |
| UC-164 | View Meal Feedback Dashboard | School Administration | BR-141, BR-145 | MSG-004, MSG-005 |

### 1.11 Incident & Emergency Management

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-073 | Report Food Safety Concern | Parent | BR-146 | MSG-023, MSG-048 |
| UC-074 | Report Food Safety Incident | Teacher | BR-146 | MSG-023, MSG-048 |
| UC-041 | Manage Food Safety Incidents | Medical Staff | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, **BR-217** | ~~MSG-024~~ *(khoá bếp descope 05/07/2026 — **không endpoint nào phát ra**, CF-M11-7)*, MSG-048, **MSG-150, MSG-151, MSG-152, MSG-158** |
| UC-009 | Assign Incident Severity | Medical Staff | BR-147, BR-148 | ~~MSG-029~~ → **MSG-159**, MSG-150 |
| UC-038 | Manage Corrective Actions | Service Caterers | BR-152, **BR-216** | MSG-049, **MSG-154** |
| UC-082 | Submit Corrective Action Results | Service Caterers | BR-152, **BR-216** | MSG-049, **MSG-154** |
| UC-168 | Record Corrective Actions Report | Medical Staff | BR-152, BR-154, **BR-155, BR-216** | ~~MSG-004~~, MSG-005, **MSG-153** |
| UC-005 | Approval / Reject Corrective Action | School Administration | BR-153, **BR-216** | ~~MSG-029~~ → **MSG-160**, **MSG-154** |
| UC-013 | Close Incident | School Administration | BR-155, **BR-217** | ~~MSG-029~~ → **MSG-161**, **MSG-153, MSG-155** |
| UC-033 | Generate Incident Report | Medical Staff | BR-154 | MSG-004, MSG-005 |
| UC-031 | Export Incident Reports | School Administration | BR-154 | MSG-004, MSG-005 |
| UC-114 | View Food Poisoning Incident List *(mở rộng 15/07/2026 — thêm bước soạn "biên bản sự cố" thủ công trước khi mở/đóng điều tra, xem ghi chú dưới)* | School Administration, Medical Staff | BR-146, BR-149 | MSG-004, MSG-005, MSG-023, MSG-100, MSG-101, **MSG-149** |
| UC-116 | View Food Safety Incident Detail *(15/07/2026 — gộp trình bày UC-009 vào cùng màn, xem ghi chú dưới)* | School Administration, Medical Staff | BR-146–155 | MSG-004, MSG-005 |
| UC-120 | View Incident Information | Service Caterers | — | MSG-004, MSG-005 |
| UC-121 | View Incident Report | School Administration | BR-154 | MSG-004, MSG-005 |
| UC-179 | Record **Unit** Violation *(đổi tên 04/08/2026 — BR-183/185/187 sửa 26/07 chuyển từ cấp **nhân viên** sang cấp **đơn vị Nhà thầu**; nay là dialog hợp nhất — gộp UC-178, xem ghi chú §1.7)* | Medical Staff, Service Caterers | BR-183, ~~BR-184~~ *(Deprecated 26/07/2026, gộp vào BR-183)*, BR-187, **BR-188**, BR-190, BR-211 | MSG-085, MSG-086, MSG-088, MSG-095 |
| UC-180 | Record Violation Resolution | Medical Staff, School Administration | BR-185, BR-186, **BR-188**, BR-211 | MSG-085, MSG-087, MSG-095, **MSG-156, MSG-157** |
| UC-181 | View Violation Registry | School Administration, Medical Staff | BR-189 | MSG-004, MSG-005 |
| UC-182 | Approve/Reject Violation Resolution | School Administration | BR-186, **BR-188** | ~~MSG-029~~ *(sai ngữ nghĩa — "Profile updated")*, MSG-089, **MSG-157** |
| UC-183 | View Weekly Override Abuse Report | School Administration | BR-190 | MSG-004, MSG-005 |

> **UC-179–183 (Violation Management) chính thức hoá 2026-07-01** từ `Violation_Management_Proposal.md`. `ViolationResolutionRecord` (UC-180, UC-182) chạy **song song** với `CorrectiveActionRequest`/CAPA hiện có (UC-005, UC-038, UC-082, UC-168 ở trên) — không thay thế. Xem `business_rules.md` §18.
>
> **BR-211 bổ sung 06/07/2026** vào UC-178, UC-179, UC-180 — xuất Biên bản Vi phạm dạng PDF theo mẫu chuẩn (`00_Policies/specialreport2173_1.md`) + lưu ảnh biên bản đã ký 3 bên, theo yêu cầu người dùng. Không thay thế cơ chế severity/witness/audit log hiện có (BR-183, BR-184, BR-186–188) — chỉ bổ sung field và bước xuất bản/lưu ảnh, cộng thêm **gate bắt buộc (chặn cứng)**: không cho đóng biên bản (LOW)/gửi School Administration phê duyệt (MEDIUM/HIGH) nếu chưa có ảnh biên bản đã ký — quyết định người dùng 06/07/2026. MSG-095 (**Error**, chặn cứng) đi kèm.
>
> **Thiết kế lại "Sự cố & Điều tra" — 15/07/2026** (xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10, không có trong `Report 3.md` gốc, không cấp BR mới):
> 1. **UC-114 mở rộng**: sau khi Medical Staff chọn 1+ báo cáo thô (`RPT-xxx`) liên quan ATTP, thêm bước bắt buộc soạn 1 "biên bản sự cố" thủ công (`33_CreateIncidentRecordDialog.md`, theo mẫu `00_Policies/specialreport2173_1.md`) trước khi quyết định đóng (không liên quan ATTP) hay mở phiên điều tra (tạo `INC-xxx`) — đảo ngược quyết định 09/07/2026 (gộp làm 1 bước) vì bước giữa nay là soạn thảo văn bản chính thức, không còn là xác nhận suông dư thừa. MSG-100 (lưu nháp)/MSG-101 (hoàn tất) mới.
> 2. **UC-009 (Assign Incident Severity) gộp trình bày vào UC-116 (Incident Detail)**: không còn là dialog riêng phải hoàn tất trước — severity + đánh giá rủi ro lâm sàng hiển thị cùng màn với "Tường trình ban đầu" + "Chọn món ăn nghi vấn". UC-009 giữ nguyên số hiệu/ràng buộc BR-147/BR-148, chỉ đổi cách trình bày.
> 3. **UC-168 (Record Corrective Actions Report) + UC-033 (Generate Incident Report) gộp trình bày** thành 1 điểm vào "Hoàn tất sự cố" — 2 khối tuần tự trong cùng 1 màn thay vì 2 điều hướng tách rời. Giữ nguyên 2 UC riêng biệt (khác actor-trigger/BR áp dụng).
> 4. ~~Chọn "món ăn nghi vấn" (BR-149) nay mặc định lọc theo món chế biến trong **48 giờ**~~ → **SỬA 2026-08-04: cửa sổ là 24 GIỜ.** Ghi chú này viết 15/07, đã **lạc hậu so với `business_rules.md` BR-149 bản sửa 27/07/2026** (*"đổi cửa sổ 72 giờ → **24 giờ**, áp thống nhất cho CẢ danh sách món nghi vấn lẫn danh sách mẫu"*), đồng bộ với **BR-111** (hạn lưu mẫu đối chứng 24h). Căn cứ vẫn là Quyết định 39/2006/QĐ-BYT (`00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`), có thể mở rộng thủ công. **Vì sao phải sửa dòng này chứ không chỉ tin BR:** ai đọc traceability để hiện thực sẽ lấy 48h, ra danh sách món **rộng gấp đôi** khoảng còn tồn mẫu đối chứng — tức đề xuất điều tra những món **không còn mẫu để xét nghiệm**. Phát hiện ở `TestSpec-MF11` **L38**; mốc gốc tính cửa sổ: `occurredAt → mealDate → reportedAt`.
>
> **Bổ sung 2026-08-04 (đóng mục treo H4 của `TDS-MF11_Backend.md` §2.5 — CF-M11-1/CF-M11-3):** ba BR sau **đã có trong `business_rules.md` nhưng thiếu ở bảng trên**, nay bổ sung vào các UC tương ứng:
> - **BR-216** (vòng đời CAPA 6 trạng thái · **1 Incident = 1 CAPA**) — quy tắc **trung tâm** của UC-005/038/082/168. Không có nó thì vòng đời CAPA không có định nghĩa và 3 màn của 3 vai (Medical Staff / School Admin / Service Caterers) nói ba chuyện khác nhau.
> - **BR-217** (cách ly lô → trả về NCC → **bỏ mở khoá**) — quyết định hành vi **không thể hoàn tác**, gán cho UC-041 và UC-013.
> - **BR-188** (audit log bất biến cho hồ sơ vi phạm) — gán cho UC-179/180/182. Bỏ đi thì hồ sơ vi phạm **mất giá trị pháp lý**, trong khi đây là căn cứ chế tài hợp đồng với nhà thầu và phải giữ 10 năm.
>
> **BR-184 đánh dấu Deprecated tại UC-179** (gộp vào BR-183 từ 26/07/2026) và **UC-179 đổi tên** *"Record Staff Violation"* → *"Record Unit Violation"*: BR-183/185/187 bản 26/07 đã chuyển đối tượng lập biên bản từ **nhân viên** sang **đơn vị Nhà thầu** (`caterersRepresentativeId` + `unitSanctionLevel`), nên tên cũ dẫn người đọc hiện thực sai chủ thể. ⚠️ **MSG-088 giữ nguyên văn** (*"staff name matches…"*) — nội dung MSG không sửa theo Policy 4.4; diễn giải mới: đối chiếu theo **đại diện đơn vị**, không theo tên nhân viên.
>
> **Ba MSG mới thay MSG-029 dùng sai ngữ nghĩa (đóng H5 — CF-M11-6):** UC-009 → **MSG-159**, UC-005 → **MSG-160**, UC-013 → **MSG-161** (`error_message.md` v1.11). MSG-029 là *"Profile updated successfully."* — không mô tả đúng bất kỳ hành động nào trong ba. Cùng lúc, mock FE đang mượn **MSG-043** (checklist ATTP, MF07) và **MSG-046** (phản hồi bữa ăn, MF10) cho hai trong ba chỗ — **Minh** sửa mock.
>
> **UC-168 gỡ MSG-004** *(Session expired)*: UC file EX1 chỉ định MSG-004 khi còn HS chưa hồi phục — sai ngữ nghĩa, sẽ **đá người dùng ra màn đăng nhập giữa lúc xử lý sự cố**. Thay bằng **MSG-153** (409). Xem CF-M11-15.

### 1.12 AI & Supporting Services

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-012 | Chat with AI | User | BR-157 | MSG-073 |
| UC-119 | View History Chat | User | BR-157 | MSG-004, MSG-005 |
| UC-040 | Manage External Integrations | System Admin | BR-163, BR-164 | MSG-004, MSG-005 |
| UC-057 | Monitor System Activities | System Admin | BR-165, BR-170 | MSG-004, MSG-005 |
| UC-106 | View Audit Logs | System Admin | BR-162, BR-170, BR-171, BR-172, BR-174 | MSG-004, MSG-005 |
| UC-111 | View Error Logs | System Admin | BR-164 | MSG-004, MSG-005 |
| UC-149 | View System Status | System Admin | BR-165 | MSG-004, MSG-005 |
| UC-150 | View Usage Statistics | System Admin | BR-165 | MSG-004, MSG-005 |
| UC-184 | Configure Security Policies | System Admin | BR-002, BR-004, BR-006, BR-170 | MSG-004, MSG-005, MSG-029 |
| UC-185 | Send Daily/Weekly Compliance Report | Medical Staff | BR-209, BR-178, BR-170 | MSG-091, MSG-092 |
| UC-186 | Manage Kitchen Inspection Checklist | Medical Staff | BR-210, BR-170 | MSG-094 |
| UC-187 | ATTP Report Center (hub) | Medical Staff | BR-175 | MSG-004, MSG-005 |
| UC-188 | Send Incident Summary Report | Medical Staff | BR-213, BR-178, BR-158, BR-159, BR-170, BR-181 | MSG-096, MSG-098 |
| UC-189 | Send Violation Summary Report | Medical Staff | BR-214, BR-178, BR-158, BR-159, BR-170 | MSG-097, MSG-099 |
| UC-190 | View Sent Reports History | Medical Staff | BR-177, BR-175 | MSG-004, MSG-005 |

*(UC-184 bổ sung 2026-07-01 qua audit A8 — Report 3.md §3.2.12.13. Màn hình `Web/SystemAdmin/15_SecurityPolicies.md` không khớp UC nào có sẵn; BR-002/004/006 trước đó chỉ là hằng số nghiệp vụ, chưa có UC cho phép xem/sửa.)*

*(UC-185 bổ sung 05/07/2026 — chính thức hóa US-09 trong `MedicalStaff_UserStory.md`. Không có trong `Report 3.md` gốc — bổ sung ngoài SRS sau khi phát hiện mô hình pull hiện tại (UC-027 Compliance Dashboard) chưa có route triển khai thật ở phía School Administration, xem `03_Design/08_FrontEnd/Web/review/review_screen_medical_staff.md` mục 3.9. Đội dự án đã duyệt 05/07/2026.)*

*(UC-186 bổ sung 06/07/2026 — chính thức hóa quyết định chuyển Kitchen Safety and Sanitation Checklist (UC-014) sang mô hình dynamic: Medical Staff tự CRUD nhóm/hạng mục/chú thích căn cứ pháp lý, không qua System Admin/phê duyệt (thay thế điều khoản tương ứng của BR-207 gốc). Không có trong `Report 3.md` gốc. Xem `business_rules.md` BR-210, `MedicalStaff_KitchenInspectionChecklist_Proposal.md` Mục 0, `MedicalStaff_UserStory.md` US-22.)*

*(UC-187–190 bổ sung 13/07/2026 — phát sinh từ phản hồi người dùng khi rà soát UC-185 ("Hình 7"), phát hiện Medical Staff còn thiếu kênh báo cáo chủ động cho 2 mảng dữ liệu khác (sự cố, vi phạm) và thiếu nơi xem lại báo cáo đã gửi. Không có trong `Report 3.md` gốc. UC-187 là màn hub tổng hợp, gom điểm vào của UC-185/188/189; UC-188/189 chỉ **rollup số liệu** — không lặp nội dung đã có ở UC-033 (báo cáo từng sự cố) hay BR-211 (biên bản vi phạm đã ký) để tránh 2 nguồn dữ liệu trùng mô tả 1 sự việc; UC-190 hiện thực hóa BR-177 (đã có sẵn, không cấp BR mới). Xem `business_rules.md` BR-213, BR-214, Changelog v1.7; `03_Design/08_FrontEnd/Web/review/medical_staff/review_screen_medical_staff.md` Mục 17.)*

### 1.13 Reporting & Analytics

> **Lưu ý renumbering**: UC-171/172/173 trước đây được đánh số UC-161/162/163, nhưng các số này đã bị Report 3 dùng cho module Meal Feedback (xem §1.10). Vì "UC ID and Name" trong Functional Specifications của Report 3 là nguồn chuẩn, 3 UC dưới đây được cấp số mới.
>
> **Cập nhật 2026-07-01 (audit A8)**: UC-171 và UC-172 nay **đã có Functional Specification chi tiết** trong Report 3.md §3.2.13.1–3.2.13.2, viết bổ sung để làm nguồn chính thức cho các màn hình báo cáo dùng chung (Food Sample Retention Reports, Partnership Reports, System Usage Metrics, Traceability Reports). **UC-173 vẫn chỉ là stub** — chưa có màn hình nào trong Screen Specifications cần đến, để team bổ sung Functional Specification khi cần.

| UC ID | Use Case Name | Actor | Business Rules | System Messages |
|---|---|---|---|---|
| UC-171 | View Analytics Dashboard | School Administration / Medical Staff / System Admin | BR-175, BR-176, BR-180, BR-181, BR-182 | MSG-004, MSG-005 |
| UC-172 | Export Report | School Administration / System Admin | BR-175, BR-176, BR-177, BR-178, BR-181, BR-182 | MSG-004, MSG-005 |
| UC-173 | Schedule Automated Report | School Administration / System Admin | BR-175, BR-176, BR-179 | MSG-004, MSG-005 |

---

## 2. Ma trận Business Rules ↔ Use Cases

Mỗi Business Rule ánh xạ đến các Use Case thực thi nó.

| BR Code | Business Rule Summary | Use Cases |
|---|---|---|
| BR-001 | Mật khẩu mã hóa BCrypt | UC-011, UC-032 |
| BR-002 | Khóa tài khoản sau 5 lần sai (15 phút) | UC-034, UC-184 |
| BR-003 | Token đặt lại MK hết hạn sau 1 giờ | UC-032 |
| BR-004 | Session timeout 30 phút không hoạt động | UC-034, UC-035, UC-184 |
| BR-005 | Đăng nhập một thiết bị (Single Session) | UC-034 |
| BR-006 | Chính sách độ mạnh mật khẩu | UC-011, UC-032, UC-184 |
| BR-007 | Phân quyền theo vai trò (RBAC) | UC-002, UC-003, UC-034 |
| BR-008 | Bảo vệ vai trò hệ thống mặc định | UC-002, UC-003, UC-022, UC-029, UC-098 |
| BR-009 | Chấp nhận điều khoản khi đăng nhập lần đầu | UC-034 |
| BR-010 | Xác minh email trước khi đăng nhập | UC-024, UC-026, UC-034, UC-154, UC-067, UC-071 |
| BR-011 | Token kích hoạt tài khoản hết hạn sau 48h | UC-154 |
| BR-012 | Định dạng thông tin người dùng (SĐT, email) | UC-024, UC-026, UC-059, UC-097 |
| BR-013 | Phân quyền tạo nhân viên — School Admin tạo staff, System Admin tạo user toàn hệ thống | UC-024, UC-026 |
| BR-020 | Một giáo viên chủ nhiệm mỗi lớp | UC-008 |
| BR-021 | Vô hiệu hóa trường cascade | UC-001 |
| BR-024 | Cấp lớp hợp lệ 1-12 | UC-018, UC-090, UC-037, UC-165, UC-166 |
| BR-025 | Sức chứa tối đa 50 học sinh | UC-018, UC-090, UC-037, UC-165, UC-166 |
| BR-026 | Ghi nhật ký phân công giáo viên | UC-008 |
| BR-028 | Xác minh và khóa tọa độ GPS trường học | UC-070, UC-095 |
| BR-030 | Học sinh liên kết với phụ huynh | UC-025, UC-084, UC-093, UC-104, UC-145 |
| BR-031 | Xét duyệt thông tin sức khỏe | UC-077, UC-078, UC-084, UC-093 |
| BR-032 | Nhập hàng loạt học sinh và phụ huynh | UC-010 |
| BR-201 | Giới hạn 5 hồ sơ học sinh/tài khoản phụ huynh (bổ sung 2026-07-01, thay chỗ ghost-citation "BR-040" cũ) | UC-025, UC-050, UC-053, UC-093, UC-135, UC-141 |
| BR-040 | Quy trình đăng ký nhà cung cấp | UC-071, UC-152, UC-067, UC-156 |
| BR-041 | Trạng thái nhà cung cấp Pending/Approved/Rejected/**Suspended** | UC-007, UC-004, UC-169, UC-157 |
| BR-046 | Yêu cầu tài liệu tuân thủ nhà cung cấp | UC-167, UC-071, UC-091, UC-101, UC-152 |
| BR-047 | Cảnh báo hết hạn tài liệu 30 ngày | UC-167, UC-091, UC-101 |
| BR-048 | Từ chối nhà cung cấp kèm lý do | UC-007, UC-004 |
| BR-050 | Đăng ký nhân viên giao hàng | UC-019, UC-110 |
| BR-051 | Xác thực sinh trắc học khuôn mặt | UC-028, UC-068, UC-079 |
| BR-060 | Mã hóa dữ liệu sinh trắc học (AES-256) | UC-068, UC-079 |
| BR-061 | Thực đơn nộp trước 3 ngày | UC-020, UC-087, UC-048 |
| BR-062 | Mục tiêu dinh dưỡng theo nhóm tuổi | UC-020, UC-023 |
| BR-063 | Kiểm tra xung đột dị ứng bắt buộc | UC-006, UC-020, UC-087 |
| BR-064 | Tối ưu hóa thực đơn theo tồn kho | UC-020, UC-075 |
| BR-065 | Kế hoạch bữa ăn đặc biệt bắt buộc | UC-023, UC-078, UC-141, UC-143 |
| BR-066 | Danh mục chế độ ăn đặc biệt | UC-023 |
| BR-067 | Thực đơn Approved/Published không thể sửa | UC-006, UC-096 |
| BR-068 | Quy trình phê duyệt thực đơn: Draft → Pending → Approved / Rejected → Published | UC-006, UC-020, UC-048, UC-087, UC-096 |
| BR-069 | Khai báo đầy đủ nguyên liệu và chất dị ứng | UC-020, UC-087 |
| BR-070 | Xác minh trạng thái nhà cung cấp khi nhận hàng | UC-044, UC-060 |
| BR-071 | Chứng nhận lô hàng bắt buộc | UC-060, UC-081, UC-100, UC-015, UC-039, UC-043, UC-083, UC-092, UC-109 |
| BR-072 | Chụp ảnh bằng chứng bắt buộc khi nhận hàng | UC-044, UC-060 |
| BR-073 | Ghi tọa độ GPS và timestamp tự động | UC-044, UC-060 |
| BR-074 | Xác minh vị trí nhận hàng | UC-044, UC-060 |
| BR-075 | Nguyên tắc FEFO | UC-045, UC-061, UC-064 |
| BR-076 | Truy xuất lô nguyên liệu | UC-062, UC-063, UC-064, UC-066 |
| BR-077 | Cảnh báo thiếu kho | UC-021, UC-045 |
| BR-078 | Từ chối lô nguyên liệu không đạt | UC-072 |
| BR-079 | Ba bước kiểm tra bắt buộc | UC-052, UC-062, UC-063, UC-065 |
| BR-080 | Trần chi phí thực đơn | UC-020, UC-048, UC-075 |
| ~~BR-081~~ | ~~Món thay thế cho 5 loại dị ứng phổ biến~~ **ĐÃ XOÁ (2026-07-01)** — xem `business_rules.md` §7. Số "BR-081" từng bị dùng trùng bởi UC-061 (Record Ingredient Storage) với nghĩa "khớp khu vực lưu trữ theo nhiệt độ" — nghĩa đó nay là **BR-202** (dòng bên dưới). | UC-020, UC-023 *(đã gỡ khỏi 2 UC này)* |
| BR-082 | Chế biến riêng cho bữa ăn đặc biệt | UC-023, UC-047, UC-065 |
| BR-083 | Ghi nhận bằng chứng mỗi bước | UC-052, UC-062, UC-063, UC-065 |
| BR-204 | Bắt buộc mã lý do và xác nhận quản lý khi điều chỉnh tồn kho thủ công *(bổ sung 2026-07-02, thay cho ghost-citation "BR-082" trong Report 3 UC-064 — BR-082 thật = Chế biến riêng cho bữa ăn đặc biệt, không liên quan kho)* | UC-064 |
| BR-205 | Nguyên liệu hết hạn bị khóa tự động trong kho *(bổ sung 2026-07-02, thay cho ghost-citation "BR-083" trong Report 3 UC-045 — BR-083 thật = Ghi nhận bằng chứng mỗi bước Three-Step, không liên quan quản lý kho)* | UC-045 |
| BR-089 | Giám sát nhiệt độ tủ lạnh | UC-054 |
| BR-202 | Khớp khu vực lưu trữ theo yêu cầu nhiệt độ *(bổ sung 2026-07-01, thay cho "BR-081" bị trùng số)* | UC-061 |
| BR-090 | Nhiệt độ tối thiểu 75°C sau nấu | UC-063, UC-052 |
| BR-091 | Nhiệt độ giữ ấm 60°C tối thiểu | UC-065, UC-052 |
| BR-092 | Kiểm tra hạn sử dụng nguyên liệu | UC-062, UC-052 |
| BR-093 | Hồ sơ kiểm tra đầy đủ | UC-052 |
| BR-094 | Chặn phục vụ nếu kiểm tra thất bại | UC-052, UC-065 |
| BR-095 | Ghi nhận không tuân thủ | UC-052, UC-062, UC-063, UC-065 |
| BR-096 | Tái kiểm tra sau hành động khắc phục | UC-052 |
| BR-097 | Tích hợp với quản lý sự cố | UC-041, UC-052 |
| BR-098 | Phê duyệt bữa ăn để phục vụ | UC-052, UC-065 |
| BR-099 | Quyền xem hồ sơ kiểm tra | UC-112, UC-113, UC-124 |
| BR-100 | Điểm số vệ sinh hàng ngày | UC-014, UC-027, UC-054 |
| BR-101 | Kiểm tra ATTP & vệ sinh hàng tháng | UC-014, UC-054 |
| BR-102 | Nhật ký kiểm soát côn trùng | UC-054 |
| BR-103 | Xác nhận sức khỏe nhân viên bếp | UC-054 |
| BR-104 | Điểm số vệ sinh đầu bếp | UC-027, UC-054 |
| BR-105 | Lưu mẫu bắt buộc sau kiểm tra thành công | UC-042, UC-069 |
| BR-106 | Nhãn mẫu bắt buộc | UC-069 |
| BR-107 | Chụp ảnh mẫu bắt buộc | UC-069 |
| BR-108 | Ghi GPS và timestamp tự động khi lưu mẫu | UC-042, UC-069 |
| BR-109 | Xác minh vị trí lưu mẫu | UC-042, UC-069 |
| BR-110 | Nhiệt độ bảo quản mẫu 2–8°C | UC-042, UC-054 |
| BR-111 | Thời gian lưu mẫu tối thiểu | UC-042 |
| BR-112 | Hủy mẫu sau thời hạn | UC-030 |
| BR-113 | Thu hồi mẫu khi có sự cố | UC-076 |
| BR-114 | Liên kết mẫu với thực đơn và kiểm tra | UC-042, UC-069, UC-076, UC-118 |
| BR-203 | Khối lượng và dụng cụ lưu mẫu tối thiểu *(bổ sung 2026-07-02, đối chiếu 00_Policies/QĐ 1246)* | UC-069 |
| BR-212 | Bổ sung thông tin bàn giao mẫu cho phòng kiểm nghiệm *(mới 12/07/2026, đối chiếu 00_Policies/Thông tư 14/2011/TT-BYT Phụ lục V)* | UC-076 |
| BR-120 | Xác nhận phân phát theo lớp | UC-016, UC-047, UC-132 |
| BR-121 | Theo dõi lịch sử bữa ăn học sinh | UC-053, UC-056, UC-080, UC-133 |
| BR-123 | Cảnh báo dị ứng khi quét ID học sinh | UC-103, UC-145 |
| BR-125 | Xem yêu cầu bữa ăn đặc biệt khi phục vụ | UC-103, UC-143 |
| BR-130 | Ngăn phân phát trùng lặp | UC-047 |
| BR-133 | Tóm tắt bữa ăn hàng ngày cho phụ huynh | UC-053, UC-055, UC-133 |
| BR-135 | Thông báo trễ phục vụ bữa ăn | UC-047 |
| BR-138 | Cảnh báo quan trọng bỏ qua phê duyệt | UC-017, UC-058 |
| BR-139 | Phản hồi trong 24 giờ | UC-046, UC-085, UC-086 |
| BR-140 | Tag quan tâm chất lượng khi đánh giá <3 sao | UC-085 |
| BR-141 | Tổng hợp phản hồi hàng tuần | UC-046 |
| BR-142 | Phát hiện từ khóa bệnh lý trong phản hồi | UC-085 |
| BR-143 | Phản hồi từ nhà cung cấp catering | UC-046 |
| BR-144 | Lịch sử sửa đổi thông báo | UC-089 |
| BR-145 | Ẩn danh dữ liệu phản hồi | UC-046 |
| BR-146 | Tự động kích hoạt điều tra sự cố | UC-041, UC-073, UC-074 |
| BR-147 | Phân loại mức độ nghiêm trọng sự cố | UC-009, UC-041 |
| BR-148 | Cảnh báo ngay cho sự cố nghiêm trọng | UC-009, UC-041 |
| BR-149 | Tự động cách ly lô hàng khi có sự cố | UC-041, UC-114, UC-116 *(khung 48h chọn món ăn nghi vấn, bổ sung 15/07/2026)* |
| BR-150 | Điều tra và phân tích nguyên nhân gốc rễ | UC-041 |
| BR-151 | Bản đồ truy xuất nguồn gốc trong 4 giờ | UC-041 |
| BR-152 | Quản lý CAPA | UC-038, UC-168, UC-082 |
| BR-153 | Phê duyệt hành động khắc phục | UC-005 |
| BR-154 | Báo cáo sự cố chính thức | UC-031, UC-033, UC-121 |
| BR-155 | Đóng sự cố | UC-013 |
| BR-156 | Đề xuất thực đơn bằng AI | UC-075 |
| BR-157 | Lưu trữ trao đổi AI bắt buộc | UC-012, UC-119 |
| BR-158 | Giờ yên tĩnh (Quiet Hours) | UC-058, UC-088 |
| BR-158b | Cấu hình giờ yên tĩnh | UC-088 |
| BR-159 | Thử lại khi gửi thông báo thất bại | UC-058 |
| BR-160 | Sao lưu cơ sở dữ liệu tự động | UC-057 |
| BR-161 | Bảo vệ file sao lưu | UC-057 |
| BR-162 | Nhật ký audit không thể thay đổi | UC-106 |
| BR-163 | Kiểm tra kết nối tích hợp bên ngoài | UC-040 |
| BR-164 | Mã hóa thông tin xác thực tích hợp | UC-040 |
| BR-165 | Báo cáo sức khỏe hệ thống hàng tuần | UC-057, UC-149, UC-150 |
| BR-170 | Ghi nhật ký mọi hành động quan trọng | UC-057, UC-106, UC-184, UC-188, UC-189 |
| BR-171 | Chính sách lưu trữ nhật ký | UC-106 |
| BR-172 | Bảo vệ toàn vẹn nhật ký | UC-106, UC-094, UC-146 |
| BR-173 | Bảo vệ dữ liệu cá nhân (PDPD) | UC-068, UC-079 |
| BR-174 | Truy vết lịch sử vận hành | UC-106 |
| BR-175 | Phân quyền xem báo cáo theo vai trò (RBAC) | UC-113, UC-115, UC-117, UC-121, UC-171, UC-172, UC-173, UC-187, UC-190 |
| BR-176 | Phạm vi dữ liệu báo cáo giới hạn theo trường | UC-113, UC-115, UC-117, UC-121, UC-171, UC-172 |
| BR-177 | Lưu trữ lịch sử báo cáo tối thiểu 12 tháng | UC-033, UC-172, UC-173, UC-190 *(UC-190 bổ sung 13/07/2026 — hiện thực hóa BR-177 cho phạm vi Medical Staff)* |
| BR-178 | Xuất báo cáo định dạng PDF và Excel (XLSX) | UC-031, UC-033, UC-172, UC-185, UC-188, UC-189 |
| BR-179 | Lên lịch xuất báo cáo tự động định kỳ | UC-173 |
| BR-180 | Dashboard cập nhật trong vòng 5 phút | UC-171 |
| BR-181 | Ẩn danh dữ liệu cá nhân trong báo cáo tổng hợp | UC-113, UC-115, UC-117, UC-121, UC-171, UC-188 *(UC-188 bổ sung 13/07/2026 — báo cáo tổng hợp sự cố không nêu tên/mã học sinh cụ thể)* |
| BR-182 | Toàn vẹn và độ chính xác dữ liệu báo cáo | UC-033, UC-171, UC-172 |
| BR-209 | Gửi báo cáo tuân thủ ATTP định kỳ (Daily/Weekly) chủ động cho School Administration *(bổ sung 05/07/2026, chính thức hóa US-09 — không có trong Report 3.md gốc)* | UC-185 |
| BR-210 | Quản lý động (CRUD) Kitchen Safety and Sanitation Checklist bởi Medical Staff, không qua System Admin/phê duyệt *(bổ sung 06/07/2026 — thay thế điều khoản tương ứng của BR-207 gốc, không có trong Report 3.md gốc)* | UC-186 |
| BR-213 | Gửi báo cáo tổng hợp sự cố ATTP theo kỳ (rollup, không lặp nội dung UC-033) *(bổ sung 13/07/2026 — không có trong Report 3.md gốc)* | UC-188 |
| BR-214 | Gửi báo cáo tổng hợp vi phạm theo kỳ (rollup, không lặp nội dung BR-211) *(bổ sung 13/07/2026 — không có trong Report 3.md gốc)* | UC-189 |
| BR-183 | Hình thức hóa Biên bản Vi phạm Quy trình/Sản phẩm | ~~UC-178~~ → UC-179 *(dialog hợp nhất, 11/07/2026)* |
| BR-184 | Biên bản Vi phạm Nhân viên | UC-179 |
| BR-185 | Biên bản Xử lý Sai phạm | UC-180 |
| BR-186 | Phân cấp phê duyệt Xử lý Sai phạm (LOW tự duyệt / MEDIUM-HIGH cần School Admin) | UC-180, UC-182 |
| BR-187 | Cảnh báo nghi ngờ tái diễn vi phạm (best-effort, mặc định 90 ngày) | UC-179 |
| BR-188 | Audit log bất biến cho hồ sơ vi phạm | UC-178, UC-179, UC-180 |
| BR-189 | Danh sách tổng hợp vi phạm (Violation Registry) | UC-181 |
| BR-190 | Báo cáo lạm dụng Manual Override hàng tuần | UC-179, UC-183 |
| BR-211 | Xuất Biên bản Vi phạm dạng PDF chuẩn + lưu ảnh biên bản đã ký 3 bên *(bổ sung 06/07/2026, theo yêu cầu người dùng — không có trong Report 3.md gốc)* | UC-178, UC-179, UC-180 |
| BR-191 | Một năm học Active duy nhất mỗi trường | UC-174, UC-175 |
| BR-192 | Classroom phải thuộc về một năm học Active | UC-018, UC-174 |
| BR-193 | Đóng năm học chuyển Classroom sang chỉ đọc | UC-175 |
| BR-194 | Sao chép cấu trúc lớp; "lên lớp" là hành động tường minh | UC-174, UC-177 |
| BR-195 | Dữ liệu năm học Archived luôn truy vấn được | UC-176 |

---

## 3. Ma trận UC ↔ System Messages

Mapping tổng hợp từ Functional Specifications — Use Case có thể phát sinh những thông báo nào.

| MSG Code | Type | Nội dung tóm tắt | Use Cases phát sinh |
|---|---|---|---|
| MSG-001 | Error | Lỗi hệ thống lưu dữ liệu trường | UC-001, UC-095 |
| MSG-002 | Error | Thông tin đăng nhập sai | UC-034, UC-011 |
| MSG-003 | Error | Tài khoản bị vô hiệu hóa | UC-034, UC-032 |
| MSG-004 | Error | Phiên đăng nhập hết hạn | Tất cả UC yêu cầu xác thực |
| MSG-005 | Error | Không có quyền truy cập | Tất cả UC phân quyền |
| MSG-006 | Error | Email đã tồn tại | UC-024, UC-026, UC-071, UC-067 |
| MSG-007 | Error | Mã số thuế sai định dạng | UC-070, UC-158 |
| MSG-008 | Error | File vượt giới hạn 10MB | UC-167, UC-081, UC-091, UC-097, UC-100, UC-101, UC-102, UC-152 |
| MSG-009 | Error | Định dạng file không hỗ trợ | UC-167, UC-081, UC-091, UC-097, UC-100, UC-101, UC-102 |
| MSG-010 | Error | Lớp học vượt sức chứa 50 HS | UC-018, UC-090 |
| MSG-011 | Error | Giáo viên đã phân công lớp khác | UC-008 |
| MSG-012 | Error | Mã học sinh không hợp lệ | UC-025, UC-010 |
| MSG-013 | Error | Chứng nhận nhà cung cấp hết hạn | UC-044, UC-060, UC-071, UC-101 |
| MSG-076 | Error | Tài khoản bị khóa tạm thời sau 5 lần đăng nhập sai | UC-034 |
| MSG-014 | Error | Nhận diện khuôn mặt thất bại | UC-068, UC-079 |
| MSG-015 | Error | Vị trí GPS ngoài khu vực cho phép | UC-044, UC-060, UC-069 |
| MSG-016 | Error | Checklist kiểm tra bắt buộc chưa đầy đủ | UC-052, UC-062, UC-063, UC-065 |
| MSG-017 | Error | Nhiệt độ nấu thấp hơn 75°C | UC-052, UC-063 |
| MSG-018 | Error | Tồn kho thiếu nguyên liệu | UC-021, UC-045 |
| MSG-019 | Error | Trọng lượng mẫu lưu thấp hơn 100g | UC-069 |
| MSG-020 | Error | Nhiệt độ tủ lưu mẫu ngoài 2–8°C | UC-042, UC-054 |
| MSG-021 | Error | Học sinh đã quét 2 lần | UC-047 |
| MSG-022 | Error | Phản hồi ngoài cửa sổ 24 giờ | UC-085, UC-086 |
| MSG-023 | Error | Báo cáo sự cố thiếu thông tin | UC-073, UC-074 |
| MSG-024 | Error | Bếp đang bị cách ly | UC-041, UC-047 |
| MSG-025 | Error | Lỗi giao dịch database | Hệ thống tự động |
| MSG-026 | Success | Đăng nhập thành công | UC-034 |
| MSG-027 | Success | Đăng xuất thành công | UC-035 |
| MSG-028 | Success | Đăng ký thành công | UC-067, UC-070, UC-071 |
| MSG-029 | Success | Cập nhật thành công | UC-002, UC-003, UC-005, UC-008, UC-009, UC-013, UC-090, UC-091, UC-093, UC-094, UC-095, UC-097, UC-098, UC-099 |
| MSG-030 | Success | Đổi mật khẩu thành công | UC-011, UC-032 |
| MSG-031 | Success | Tài khoản tạo thành công | UC-022, UC-024, UC-025, UC-026 |
| MSG-032 | Success | Đăng ký trường học thành công | UC-070 |
| MSG-033 | Success | Tạo lớp học thành công | UC-018 |
| MSG-034 | Success | Phân công giáo viên thành công | UC-008 |
| MSG-035 | Success | Nhập hàng loạt hồ sơ thành công | UC-010 |
| MSG-036 | Success | Cập nhật hồ sơ dị ứng thành công | UC-077, UC-084, UC-093 |
| MSG-037 | Success | Phê duyệt nhà cung cấp thành công | UC-004, UC-007 |
| MSG-038 | Success | Đăng ký sinh trắc học thành công | UC-068 |
| MSG-039 | Success | Nộp thực đơn thành công | UC-020, UC-087 |
| MSG-040 | Success | Phê duyệt và công bố thực đơn | UC-006 |
| MSG-041 | Success | Nhận hàng và cập nhật kho | UC-044, UC-060 |
| MSG-042 | Success | Ghi nhận giao dịch kho | UC-061, UC-064 |
| MSG-043 | Success | Nộp checklist kiểm tra ATTP | UC-014, UC-052, UC-065 |
| MSG-044 | Success | Đăng ký lưu mẫu thành công | UC-069 |
| MSG-045 | Success | Xác nhận phân phát bữa ăn | UC-016, UC-047 |
| MSG-046 | Success | Nộp phản hồi bữa ăn thành công | UC-046, UC-085, UC-086 |
| MSG-047 | Success | Công bố thông báo thành công | UC-017, UC-089 |
| MSG-048 | Success | Ghi nhận báo cáo sự cố | UC-041, UC-073, UC-074 |
| MSG-049 | Success | Nộp kế hoạch CAPA thành công | UC-038, UC-082 |
| MSG-050 | Success | Sao lưu database thành công | UC-057 (auto) |
| MSG-051 | Warning | Xung đột dị ứng trong thực đơn | UC-006, UC-020, UC-087 |
| MSG-052 | Warning | Lô nguyên liệu sắp hết hạn | UC-045 |
| MSG-053 | Warning | Chứng nhận nhà cung cấp sắp hết hạn | UC-167, UC-091, UC-101 |
| MSG-054 | Warning | Tồn kho đạt mức tối thiểu | UC-021, UC-045 |
| MSG-055 | Warning | Cảnh báo nhiệt độ cao trong kho | UC-054 |
| MSG-056 | Warning | Cần tái kiểm tra sau thất bại | UC-052, UC-065 |
| MSG-057 | Warning | Phụ huynh đánh giá thấp <3 sao | UC-046, UC-085 |
| MSG-058 | Warning | Từ khóa bệnh lý trong phản hồi | UC-085 |
| MSG-059 | Warning | Nhiệt độ tủ lưu mẫu dao động | UC-042, UC-054 |
| MSG-060 | Warning | Chỉ số hiệu suất nhà cung cấp <70% | UC-007 (auto monitor) |
| MSG-061 | Warning | Ngưỡng tải hệ thống đạt giới hạn | UC-057 (auto) |
| MSG-062 | Warning | Đăng nhập đáng ngờ thiết bị mới | UC-034 |
| MSG-063 | Warning | Mật khẩu yếu | UC-011, UC-032 |
| MSG-064 | Warning | Hồ sơ sức khỏe học sinh chưa đầy đủ | UC-027, UC-054 |
| MSG-065 | Warning | Giao hàng trễ so với lịch | UC-044, UC-060, UC-092 |
| MSG-066 | Warning | Báo cáo kiểm tra vệ sinh tuần chưa nộp | UC-027, UC-054 |
| MSG-067 | Warning | Thực đơn thiếu đa dạng dinh dưỡng (AI) | UC-020, UC-075 |
| MSG-068 | Warning | Hàng đợi push notification tắc nghẽn | UC-058 (auto) |
| MSG-069 | Warning | Độ trễ API bên ngoài cao | UC-040, UC-057 |
| MSG-070 | Warning | Dung lượng backup gần đầy | UC-057 (auto) |
| MSG-071 | Success | Lưu bản nháp thực đơn thành công | UC-020, UC-096 |
| MSG-072 | Success | Lưu kế hoạch bữa ăn đặc biệt | UC-023 |
| MSG-073 | Warning | Dịch vụ Gemini AI không khả dụng | UC-012, UC-075 |
| MSG-074 | Success | Cập nhật thực đơn thành công | UC-096 |
| MSG-075 | Error | Món ăn lặp lại quá 2 lần/tuần | UC-020, UC-096 |
| MSG-080 | Error | Đã có năm học Active, phải đóng trước khi tạo mới | UC-174, UC-175 |
| MSG-081 | Error | Chưa có năm học Active nên không thể tạo lớp | UC-018 |
| MSG-082 | Success | Tạo năm học thành công | UC-174 |
| MSG-083 | Success | Cập nhật trạng thái năm học thành công | UC-175 |
| MSG-084 | Success | Lên lớp hàng loạt thành công | UC-177 |
| MSG-085 | Error | Thiếu thông tin bắt buộc khi lập biên bản vi phạm | UC-178, UC-179, UC-180 |
| MSG-086 | Success | Lập biên bản vi phạm thành công | UC-178, UC-179 |
| MSG-087 | Success | Nộp Biên bản Xử lý Sai phạm để phê duyệt | UC-180 |
| MSG-088 | Warning | Nghi ngờ tái diễn vi phạm, cần xác minh thủ công | UC-179 |
| MSG-095 | Error | Chặn đóng/gửi duyệt xử lý vi phạm khi chưa có ảnh biên bản đã ký 3 bên (BR-211 — chặn cứng, theo quyết định người dùng 06/07/2026) | UC-178, UC-179, UC-180 |
| MSG-089 | Success | Phê duyệt xử lý sai phạm, áp dụng kỷ luật | UC-182 |

### 3.1 MSG-104–138 (bổ sung v1.3, 29/07/2026) — chính thức hóa mã lỗi cục bộ TDS

> **Nguồn:** `07_implement/review.md` mục A dòng 7 — 7 Backend-TDS đang dùng ~35 mã lỗi tự đặt không tồn tại trong `error_message.md`. Đã cấp MSG chính thức ở `error_message.md` v1.5.
>
> ⚠️ **Cột "Use Cases" chỉ ghi UC mà TDS §10 nêu đích danh.** Ô `⚠️ chưa xác định` nghĩa là TDS gắn mã vào **BR** nhưng không dẫn UC nào — **chủ module phải bổ sung**, tuyệt đối không suy diễn UC từ nội dung BR.

| MSG Code | Type | Mã TDS | BR nguồn | Use Cases phát sinh |
|---|---|---|---|---|
| MSG-104 | Error | `IAM-006policy` | BR-006 | ⚠️ chưa xác định — Nam bổ sung (liên quan tạo/đổi/đặt lại mật khẩu) |
| MSG-105 | Error | `IAM-006policy` | BR-015 | UC-011, UC-032 — ⚠️ **CF-1**: BR-015 chưa có trong cột Business Rules của UC-011/UC-032 tại §1.1. Cần quyết định: bổ sung BR-015 vào §1.1, hay gỡ khỏi TDS-MF01 |
| MSG-106 | Error | `IAM-009` | BR-008 | UC-029 (AF1) |
| MSG-107 | Error | `SCH-013` | BR-028 | UC-070, UC-095 (theo ánh xạ BR-028 hiện có ở §1.2) |
| MSG-108 | Error | `SCH-013` | BR-028 | UC-070, UC-095 |
| MSG-109 | Error | `SCH-024` | BR-024 | UC-018, UC-090 |
| MSG-110 | Error | `STU-201` | BR-201 | ⚠️ chưa xác định — Huy bổ sung |
| MSG-111 | Error | `STU-030` | BR-030 | ⚠️ chưa xác định — Huy bổ sung |
| MSG-112 | Error | `STU-031` | BR-031 | ⚠️ chưa xác định — Huy bổ sung (nộp/xác minh khai báo dị ứng) |
| MSG-113 | Error | `STU-032`, `SUP-048` | BR-031, BR-048 | UC-077, UC-078 (Y tế); phần nhà cung cấp ⚠️ chưa xác định — Mạnh bổ sung |
| MSG-114 | Error | `STU-066` | BR-066 | UC-078 |
| MSG-115 | Error | `STU-094` | BR-031, BR-172 | ⚠️ chưa xác định — Huy bổ sung |
| MSG-116 | Error | `STU-172` | BR-172 | ⚠️ chưa xác định — Huy bổ sung |
| MSG-117 | Error | `SUP-050` | BR-050 | UC-019 (AF1) |
| MSG-118 | Error | `SUP-409` | BR-040 | UC-152, UC-156, UC-101 (AF3) |
| MSG-119 | Error | `SMP-105` | BR-105 | UC-069 |
| MSG-120 | Error | `SMP-107` | BR-107 | ⚠️ chưa xác định — Mạnh bổ sung |
| MSG-121 | Error | `SMP-109` | BR-109 | ⚠️ chưa xác định — Mạnh bổ sung |
| MSG-122 | Error | `SMP-111` | BR-111 | UC-030 |
| MSG-123 | Error | `SMP-112` | BR-112 | UC-030 (AF1) |
| MSG-124 | Error | `SMP-113` | BR-113 | UC-076 |
| MSG-125 | Error | `SMP-030` | BR-112 | ⚠️ chưa xác định — Mạnh bổ sung |
| MSG-126 | Error | `SMP-203` | BR-203 | ⚠️ chưa xác định — Mạnh bổ sung |
| MSG-127 | Error | `SMP-212` | BR-212 | UC-076 |
| MSG-128 | Error | `SMP-023` | BR-114 | UC-076 (AF1) — **thay việc mượn MSG-023**, xem CF-M08-6 |
| MSG-129 | Error | `SRV-094` | BR-094 (MF07) | UC-016 — CF-M09-4 |
| MSG-130 | Error | `SRV-022` | ADR-M09-1, ràng buộc C3 | ⚠️ chưa xác định — Minh bổ sung |
| MSG-131 | Error | `SRV-023` | (AF1 UC-016) | UC-016 (AF1) |
| MSG-132 | Error | `SRV-024` | ADR-M09-3 | ⚠️ chưa xác định — Minh bổ sung |
| MSG-133 | Error | `SRV-025` | BR-121, BR-133 | ⚠️ chưa xác định — Minh bổ sung |
| MSG-134 | Error | `FBA-140` | BR-140 | ⚠️ chưa xác định — Mạnh bổ sung (phụ huynh gửi phản hồi) |
| MSG-135 | Error | `FBA-DUP` | BR-139 | ⚠️ chưa xác định — Mạnh bổ sung |
| MSG-136 | Error | `FBA-STATE` | BR-144 | ⚠️ chưa xác định — Mạnh bổ sung |
| MSG-137 | Error | `*-404` (6 module) | — | Tất cả UC đọc/sửa tài nguyên theo ID |
| MSG-138 | Error | `SRV-001`, `FBA-REQ` | — | Tất cả UC có form nhập liệu |

**Hai xung đột traceability của MF01 vẫn treo, chưa xử lý ở bản này:**

| CF | Nội dung | Trạng thái |
|---|---|---|
| **CF-1** | BR-015 (không dùng lại 3 mật khẩu gần nhất) áp dụng cho UC-011/UC-032 nhưng **không** có trong cột Business Rules của 2 UC này tại §1.1 | Chờ quyết định — xem MSG-105 ở trên |
| **CF-2** | UC-154 có danh sách BR/MSG **khác nhau** giữa file UC `MF01_User_Management_Access_Control.md` và §1.1 của tài liệu này | Chờ đối chiếu — quy tắc hiện hành lấy §1.1 làm chuẩn (Team Plan §2c) |

---

## 4. Tóm tắt độ phủ theo Module

| Module | Số UC | Số BR phủ | Số MSG phát sinh |
|---|---|---|---|
| User Management & Access Control | 19 | 13 (BR-001–013) | 14 |
| School & Class Management (gồm Academic Year, +UC-174–177) | 16 | 12 (BR-020–026, BR-028, BR-191–195) | 11 |
| Student & Parent Management | 15 | 5 (BR-030, BR-031, BR-032, BR-065) | 4 |
| Supplier & Partner Management | 25 | 8 (BR-040, BR-041, BR-046, BR-047, BR-048, BR-050, BR-051, BR-060) | 11 |
| Ingredient & Inventory Management | 21 | 20 (BR-070–079, BR-082, BR-083, BR-090, BR-092, BR-093, BR-095, BR-096, BR-202, BR-204, BR-205) | 14 |
| Menu & Diet Management | 10 | 12 (BR-061–069, BR-071, BR-073, BR-156) | 12 |
| Food Safety Inspection & Monitoring (gồm Violation, +UC-178, +UC-185, +UC-186) | 13 | 22 (BR-079, BR-083, BR-089–104, BR-183, BR-209, BR-210, BR-211) | 14 (+MSG-091, MSG-092, MSG-093 — bổ sung 05/07/2026, US-09/UC-185 và descope điểm<60%; +MSG-094 — bổ sung 06/07/2026, UC-186; +MSG-095 — bổ sung 06/07/2026, BR-211)|
| Food Sample Retention | 5 | 11 (BR-105–114, BR-203) | 6 |
| Meal Serving & Distribution | 13 | 9 (BR-051, BR-060, BR-065, BR-120, BR-121, BR-123, BR-125, BR-130, BR-133) | 5 |
| Feedback & Announcement Management | 15 | 11 (BR-138–145, BR-158, BR-158b, BR-159) | 8 |
| Incident & Emergency Management (gồm Violation, +UC-179–183) | 20 | 18 (BR-146–155, BR-184–190, BR-211) | 13 (+MSG-095 — bổ sung 06/07/2026, BR-211) |
| AI & Supporting Services | 8 | 9 (BR-157, BR-162–165, BR-170–172, BR-174) | 3 |
| Reporting & Analytics | 3 | 8 (BR-175–182) | 2 |

> **Ghi chú (2026-07-01)**: Phần "Tóm tắt độ phủ theo Module" trước đây bị cắt cụt giữa bảng (chỉ có 4/13 dòng, phần còn lại bị mất do lỗi ghi file) — đã khôi phục đầy đủ 13 dòng dựa trên dữ liệu thực tế ở Mục 1 và 2. Đồng thời tích hợp Academic Year Management (UC-174–177) vào dòng School & Class Management và Violation Management (UC-178–183) vào 2 dòng Food Safety Inspection & Monitoring / Incident & Emergency Management — không tạo module mới, theo đúng quyết định merge ngày 2026-07-01.