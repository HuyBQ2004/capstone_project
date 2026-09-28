# System Messages — School Food Safety Management Support System (SFMS)

> **Source**: Report 3 – Software Requirement Specification, Section 3.5.2 System Messages  
> **Version**: 1.11  *(2026-08-04)*  
> **Date**: 2026-08-04 *(bản gốc 25/06/2026)*
> **Changelog v1.11 (2026-08-04)**: Bổ sung **MSG-149…MSG-158** (Error) và **MSG-159/160/161** (Success) cho **MF11 — Incident & Emergency Management**, đóng mục treo **H1** và **H5** của `TDS-MF11_Backend.md` §2.5, mở khoá MF11 chuyển Approved. Quyết định (Huy) 2026-08-04. **Bối cảnh:** MF11 là module **cuối cùng** còn dùng *"mã lỗi cục bộ TDS"* với message **tiếng Việt** (trái **D7**) — grep `IEM-` trên v1.9 ra **0**, trong khi cùng đợt đã cấp cho MF08 (MSG-119…128, v1.5), MF09 (MSG-129…133, v1.5) và MF07 (MSG-145…148, v1.10). **Ba điều lặp lại khuôn của v1.10 và đáng nhắc lại:** **(1) Grep trước khi xin mã mới.** 13 mã cục bộ của TDS-MF11 chỉ cần **10** mã mới: `IEM-001`/`IEM-404`/`IEM-429` gán về **MSG-138/137/141** đã có (dùng chung mọi module) — MF11 thiếu cả ba, giống hệt MF07. **(2) Không gộp hai mã cùng chủ đề.** `MSG-152` (*cách ly lại lô*) và `MSG-155` (*đóng sự cố khi lô chưa xử lý xong*) đều nói về lô cách ly nhưng chặn **hai thao tác khác nhau**; gộp là một `error.code` mang 2 nghĩa — đúng lỗi đã buộc tách MSG-128 (v1.5), MSG-140 (v1.7), MSG-142 (v1.9). Cùng lý do giữ `MSG-156` tách khỏi `MSG-095`. **(3) MSG-159/160/161 sửa một lớp lỗi khác với v1.10: MSG bị gán SAI NGỮ NGHĨA chứ không phải thiếu.** Traceability §1.11 gán **MSG-029** (*"Profile updated successfully"*) cho cả UC-005 (duyệt CAPA), UC-009 (phân loại mức độ) và UC-013 (đóng sự cố); mock FE thì mượn **MSG-043** (checklist ATTP/MF07) và **MSG-046** (phản hồi bữa ăn/MF10). Ba mã mới thay cả năm chỗ. **Nguy hiểm hơn thiếu mã**: thiếu thì test đỏ, còn gán sai thì hệ thống chạy xanh và người dùng đọc thông báo không liên quan (CF-M11-6).
> **Changelog v1.9 → v1.10:** xem 2 dòng ngay dưới *(thứ tự trong file này không theo số hiệu — v1.10 được chèn trước v1.9, giữ nguyên theo Policy 4.4)*.
> **Changelog v1.1 (2026-07-01)**: Bổ sung MSG-080–089 cho Academic Year Management và Violation Management (xem `business_rules.md` v1.2 Section 18–19).
> **Changelog v1.2 (2026-07-06)**: Bổ sung MSG-094 cho UC-186 (Manage Kitchen Inspection Checklist, BR-210 — checklist động do Medical Staff tự CRUD); bổ sung MSG-095 (Error, chặn cứng) cho UC-178/179/180 (BR-211 — bắt buộc có ảnh biên bản vi phạm đã ký 3 bên trước khi đóng/gửi duyệt, theo quyết định người dùng).
> **Changelog v1.3 (2026-07-13)**: Bổ sung MSG-096/MSG-097 (Error) và MSG-098/MSG-099 (Success) cho UC-188 (Send Incident Summary Report, BR-213) và UC-189 (Send Violation Summary Report, BR-214) — 2 UC mới mở rộng "ATTP Report Center" (UC-187), gộp cùng UC-185.
> **Changelog v1.4 (2026-07-15)**: Bổ sung MSG-100 (Success — lưu bản nháp biên bản sự cố) và MSG-101 (Success — hoàn tất biên bản sự cố) cho màn mới `33_CreateIncidentRecordDialog.md` (UC-114 mở rộng, xem `business_rules.md` §11 và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.3). Tái sử dụng MSG-023 hiện có cho lỗi thiếu nội dung biên bản — không cấp MSG lỗi mới.
> **Changelog v1.5 (2026-07-29)**: Bổ sung **MSG-104–MSG-138** — chính thức hóa ~35 "mã lỗi cục bộ TDS" mà 7 Backend-TDS đang tự đặt nhưng không tồn tại trong tài liệu này (phát hiện bởi `07_implement/review.md` mục A dòng 7). Mọi mã dẫn xuất BR trước đây viết bằng tiếng Việt trong TDS §10 (SMP-105/107/109/111/112/113/030/203/212, SRV-094, SRV-022) đã được **dịch sang tiếng Anh** cho nhất quán với toàn bộ tài liệu — theo quyết định **D7** trong `07_implement/00_Common_Conventions.md`. Kèm 2 sửa lỗi: (a) **MSG-023 bị dùng sai ngữ nghĩa** ở MF08 SMP-023 (mã này nói "báo cáo sự cố thiếu thông tin", bị mượn cho tình huống "Incident ID không tồn tại/đã đóng" — CF-M08-6) → cấp **MSG-128** thay thế; (b) ghi chú trong TDS-MF02 §10 nói *"MSG-013 không tồn tại"* là **sai** — MSG-013 có tồn tại (chứng nhận ATTP nhà cung cấp hết hạn, thuộc MF04), chỉ là không liên quan tọa độ GPS của BR-028 → cấp **MSG-107/108** cho GPS.
> **Changelog v1.6 (2026-07-31)**: Bổ sung **MSG-139** — đóng §2 L9 của `TestSpec-MF02_School_Class_Management.md` (D5: client gửi `id`/`code` trùng ở `POST` chưa có mã lỗi chính thức). Dùng chung mọi module giống MSG-137/138.
> **Changelog v1.7 (2026-07-31)**: Bổ sung **MSG-140** (`SCH-012`) — đóng §2 L8b của `TestSpec-MF02_School_Class_Management.md`, cấp cho **BR-219** mới (Sức chứa lớp học không được nhỏ hơn sĩ số hiện tại). Tách khỏi `SCH-010`/MSG-010 (vốn chỉ dành cho trần 50 học sinh, BR-025) theo D7 — một `error.code` không mang 2 nghĩa.
> **Changelog v1.8 (2026-07-31)**: Bổ sung **MSG-141** (rate limit HTTP 429) — đóng nốt `MF02-TC-SEC-005` của `TestSpec-MF02_School_Class_Management.md`. **Dùng chung mọi module** giống MSG-137/138/139: nhiều TDS khác (MF05 AI, MF11 đóng sự cố, MF12 gọi AI) đều có kịch bản rate-limit 429 nhưng chưa module nào có MSG chính thức — cấp một lần cho tất cả, mỗi module tự gán mã cục bộ riêng (VD `SCH-014` cho MF02).
> **Changelog v1.10 (2026-08-03)**: Bổ sung **MSG-145/146/147/148** — đóng mục treo #1 của `TDS-MF07_Backend.md` §2.5, mở khóa MF07 chuyển Approved. Bốn mã này thay 4 dòng `*(không có MSG riêng)*` + message **tiếng Việt** (trái **D7**) mà TDS-MF07 v1.1 đang dùng cho `INSP-091`/`INSP-092`/`INSP-079`/`INSP-083`. Quyết định (Huy) 2026-08-03. **Ba điều đáng lan sang module khác:** **(1) Grep trước khi kết luận "không có MSG".** TDS-MF07 v1.1 ghi **8 mã** thiếu MSG; rà lại thì `INSP-094`/`INSP-098` khớp **nguyên văn MSG-129** đã có từ v1.5 (*"Meal cannot be served: inspection step [Step Name] did not pass."*) — chỉ là trước đó MSG-129 chỉ được gán cho `SRV-094` của MF09 nên MF07 không biết. Thêm 4 mã **dùng chung mọi module** (MSG-137/138/139/141) mà MF07 thiếu cả bốn ⇒ **8 → 4** trước khi phải cấp mã nào. **(2) Hai lần suýt mượn nhầm MSG của module khác**, cùng một khuôn lỗi đã gây ra MSG-128 (v1.5) và MSG-142 (v1.9): MSG-020 (*tủ lưu mẫu 2-8°C*, BR-110/MF08) suýt bị mượn cho nhiệt độ giữ ấm 60°C của BR-091; MSG-120 (*"for food sample retention"*, BR-107/MF08) suýt bị mượn cho ảnh bằng chứng kiểm thực của BR-083. Cả hai đều **ghi cứng ngữ cảnh trong chuỗi tiếng Anh** nên mượn là sai hiển thị với người dùng — theo **D7**, một `error.code` không mang 2 nghĩa. **(3) Bảng phân nhóm §cuối cần tách:** nhóm *"Food Safety Inspection"* (dòng 219) đang gộp **MSG-091/092/094/096/097/098/099** — cả 7 mã đều thuộc **UC-185…UC-189 ở §1.12 (MF12)**, không phải MF07. Ai đọc bảng đó để tra "mã nào của MF07" sẽ lấy nhầm 7 mã.
> **Changelog v1.9 (2026-08-02)**: Bổ sung **MSG-142/143/144** — đóng **Q3/Q5/Q1** của `TestSpec-MF03_Student_Parent_Management.md` §2, mở khóa 3 test case cuối để MF03 chuyển Approved. (a) **MSG-142** (`STU-010`) cho "lớp đã đủ sĩ số theo `capacity` của chính lớp đó" — tách khỏi MSG-010 theo **D7** vì MSG-010 ghi cứng *"Maximum allowed is 50 students"*, sai ở lớp `capacity=45`; cùng lý do đã tách MSG-140 khỏi MSG-010 ở v1.7. (b) **MSG-143** (`STU-500`) — trước đó MF03 mượn MSG-001 vốn ghi *"school data"*, thuộc module khác. (c) **MSG-144** (`STU-066`) cho `dietaryType` ngoài danh mục chuẩn, gắn với **BR-220 mới**; đồng thời **MSG-114 ngừng dùng** vì nó cite BR-066 làm oracle cho việc *từ chối*, trong khi BR-066 là danh mục **MỞ** (*"và các loại khác"*) — phát hiện ở `TestSpec-MF03` §2 **L14**. MSG-114 giữ nguyên trong bảng (Policy 4.4), chỉ gỡ mã gán và ghi rõ dùng gì thay.

---

## Mục lục

1. [Error Messages (Lỗi)](#1-error-messages)
2. [Success Messages (Thành công)](#2-success-messages)
3. [Warning Messages (Cảnh báo)](#3-warning-messages)
4. [Tóm tắt theo Module](#4-tóm-tắt-theo-module)

---

## 1. Error Messages

Các thông báo lỗi ngăn người dùng tiếp tục thao tác và yêu cầu hành động khắc phục.

| MSG Code | Message Content | Trigger Context |
|---|---|---|
| MSG-001 | System error while saving school data. Please try again. | Lỗi hệ thống khi lưu dữ liệu trường học |
| MSG-002 | Invalid credentials. Please check your email and password. | Đăng nhập sai thông tin |
| MSG-003 | Your account has been deactivated. Please contact your administrator. | Tài khoản bị vô hiệu hóa |
| MSG-004 | Session expired. Please log in again. | Phiên đăng nhập hết hạn (BR-004: 30 phút) |
| MSG-005 | Access denied: You do not have permission to access this resource. | Không có quyền truy cập tài nguyên |
| MSG-006 | Registration failed: The email address is already in use. | Email đã tồn tại trong hệ thống (BR-012) |
| MSG-007 | Invalid tax ID format. Please verify and try again. | Mã số thuế sai định dạng |
| MSG-008 | File upload failed: File size exceeds the maximum limit of 10MB. | Upload file vượt quá giới hạn 10MB (BR-013) |
| MSG-009 | File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed. | Định dạng file không được hỗ trợ |
| MSG-010 | Classroom capacity exceeded. Maximum allowed is 50 students. | Lớp học vượt sức chứa tối đa 50 HS (BR-025/BR-019) |
| MSG-011 | Homeroom teacher is already assigned to another classroom. | Giáo viên chủ nhiệm đã được gán cho lớp khác (BR-020) |
| MSG-012 | Student admission code is invalid or has already been used. | Mã học sinh không hợp lệ hoặc đã sử dụng |
| MSG-013 | Supplier food safety certificate has expired. Onboarding blocked. | Chứng nhận ATTP nhà cung cấp đã hết hạn (BR-046/BR-048) |
| MSG-014 | Face recognition failed: Biometric identity could not be verified. | Xác thực khuôn mặt thất bại (BR-051) |
| MSG-015 | Receiving action blocked: GPS location verification failed (out of tolerance). | Vị trí GPS ngoài khu vực cho phép (BR-074/BR-079) |
| MSG-016 | Checklist validation failed: Mandatory safety checks are missing. | Checklist kiểm tra bắt buộc chưa đầy đủ (BR-095) |
| MSG-017 | Cooking temperature too low: Meal core temperature must reach 75°C. | Nhiệt độ nấu chưa đạt tối thiểu 75°C (BR-090/BR-093) |
| MSG-018 | Inventory shortage: Insufficient stock of ingredient [Ingredient Name]. | Tồn kho không đủ nguyên liệu (BR-077) |
| MSG-019 | Food sample retention weight too low: Must be at least 100g. | Trọng lượng mẫu lưu dưới 100g (BR-203, sửa 2026-07-02 — trước đây gán nhầm BR-107) |
| MSG-020 | Retention refrigerator temperature is out of safe range (2-8°C). | Nhiệt độ tủ lưu mẫu ngoài khoảng 2–8°C (BR-110) |
| MSG-021 | Duplicate meal distribution: Student has already scanned for this session. | Học sinh đã được quét 2 lần trong cùng phiên (BR-130) |
| MSG-022 | Feedback submission blocked: Outside the 24-hour limit of meal serving. | Phản hồi ngoài cửa sổ 24 giờ (BR-139) |
| MSG-023 | Incident report incomplete: Symptoms, date of ingestion, and dishes are required. | Báo cáo sự cố thiếu thông tin bắt buộc |
| MSG-024 | Kitchen quarantine is active: Operation blocked due to safety violations. | Bếp đang bị cách ly vì vi phạm an toàn (BR-105) |
| MSG-025 | Database transaction failed. Rollback executed. | Giao dịch cơ sở dữ liệu thất bại |
| MSG-075 | Dish rotation limit exceeded: [Dish Name] cannot be served more than twice a week. | Món ăn lặp lại quá 2 lần/tuần (BR-068) |
| MSG-076 | Account temporarily locked due to too many failed login attempts. Please try again after [Minutes] minutes. | Tài khoản bị khóa tạm thời sau 5 lần đăng nhập sai liên tiếp — lockout 15 phút (BR-002) |
| MSG-077 | Action blocked: Internet connection required to verify this safety check. Please reconnect and try again. | Chặn thao tác an toàn thời gian thực khi offline (BR-197) |
| MSG-080 | Cannot create Active academic year: an Active academic year already exists for this school. Please close it first. | Trường đã có năm học Active, phải đóng trước khi kích hoạt năm học mới (BR-191) |
| MSG-081 | Cannot create classroom: this school has no Active academic year. Please activate an academic year first. | Chưa có năm học Active nên không thể tạo lớp (BR-192) |
| MSG-085 | Violation record submission blocked: required fields (description, evidence photo) are missing. | Thiếu thông tin bắt buộc khi lập biên bản vi phạm (BR-183/BR-184) |
| MSG-095 | Cannot close/submit this violation resolution: the signed 3-party record photo has not been uploaded yet. Please print, collect signatures, and upload the signed record first. | Không thể đóng/gửi duyệt xử lý vi phạm: chưa tải ảnh biên bản đã ký 3 bên. Vui lòng in, thu thập chữ ký, và tải ảnh biên bản đã ký trước (UC-179 [dialog hợp nhất, UC-178 Deprecated 11/07/2026], UC-180, BR-211 — mới 06/07/2026; **chặn cứng**, theo quyết định người dùng — thay thế bản nháp "cảnh báo mềm" ban đầu) |
| MSG-090 | No menu has been published for the selected week yet. Please check back later. | Chưa có thực đơn được công bố cho tuần đã chọn (UC-135 View Menu Information — bổ sung 2026-07-01, thay cho citation sai "MSG-076" phát hiện bởi Nguyễn Phương Thảo; MSG-076 thực chất là "tài khoản bị khoá do đăng nhập sai", không liên quan) |
| MSG-092 | No kitchen inspection data available for the selected period. Cannot generate compliance report. | Không có dữ liệu kiểm tra bếp trong kỳ đã chọn, không thể tạo báo cáo (BR-209, UC-185 — bổ sung 05/07/2026) |
| MSG-096 | No incidents recorded for the selected period. Cannot generate incident summary report. | Không có sự cố nào phát sinh trong kỳ đã chọn, không thể tạo báo cáo tổng hợp sự cố (BR-213, UC-188 — bổ sung 13/07/2026) |
| MSG-097 | No violations recorded for the selected period. Cannot generate violation summary report. | Không có vi phạm nào ghi nhận trong kỳ đã chọn, không thể tạo báo cáo tổng hợp vi phạm (BR-214, UC-189 — bổ sung 13/07/2026) |
| MSG-102 | Google account not linked to any SFMS account. Please contact your administrator to create your account first. | Đăng nhập Google nhưng email chưa có tài khoản SFMS tương ứng (BR-218, UC-191 — bổ sung 28/07/2026) |
| MSG-103 | Google authentication failed. Please try again. | Lỗi xác thực OAuth2 với Google — token exchange thất bại hoặc bị từ chối (BR-218, UC-191 — bổ sung 28/07/2026) |

### 1.1. Mã bổ sung v1.5 (MSG-104–138) — chính thức hóa mã cục bộ TDS

> Nhóm này thay thế các mã "dẫn xuất từ BR" mà Backend-TDS tự đặt (`SMP-105`, `STU-031`, `FBA-140`…). Cột **Mã TDS** ghi mã cục bộ tương ứng để tra ngược. Mã cục bộ vẫn dùng làm `error.code` trong envelope API (xem `00_Common_Conventions.md` D2); MSG ở đây là **nội dung thông điệp chuẩn**.

| MSG Code | Message Content | Mã TDS | Trigger Context |
|---|---|---|---|
| MSG-104 | Password does not meet the security policy. It must contain uppercase, lowercase, a number, and a special character. | `IAM-006policy` | Mật khẩu không đạt chính sách khi tạo/đổi/đặt lại (BR-006). Trước v1.5 MF01 mượn MSG-063 — nhưng MSG-063 là **Warning** ("recommend"), không chặn được thao tác |
| MSG-105 | You cannot reuse any of your last 3 passwords. Please choose a different one. | `IAM-006policy` | Đổi mật khẩu trùng 1 trong 3 mật khẩu gần nhất (BR-015, UC-011/UC-032). ⚠️ Liên quan **CF-1** của MF01: BR-015 chưa có trong cột Business Rules của UC-011/UC-032 tại `traceability_matrix.md` §1.1 — cần Huy đối chiếu |
| MSG-106 | Cannot delete this role: [Number] user(s) are still assigned to it. Please reassign them first. | `IAM-009` | Xóa vai trò còn người dùng gán, kể cả đã vô hiệu hóa (BR-008, UC-029 AF1). Trước v1.5 MF01 mượn MSG-005 "Access denied" — sai ngữ nghĩa (đây là xung đột dữ liệu, không phải thiếu quyền) |
| MSG-107 | Invalid GPS coordinates. Latitude must be between -90 and 90, longitude between -180 and 180. | `SCH-013` | Tọa độ trường ngoài dải hợp lệ (BR-028, UC-070) |
| MSG-108 | School coordinates are locked. Only a System Administrator can change them. | `SCH-013` | Sửa tọa độ khi `coordinateLocked=true` mà không phải System Admin (BR-028) |
| MSG-109 | Grade level must be between 1 and 12. | `SCH-024` | `grade` ngoài khoảng [1,12] khi tạo/sửa lớp (BR-024, UC-018/090) |
| MSG-110 | This parent account is already linked to 5 students. School Administration verification is required. | `STU-201` | Liên kết học sinh thứ 6 vào một tài khoản phụ huynh (BR-201) |
| MSG-111 | Each student must be linked to at least one parent account. | `STU-030` | Tạo HS không có phụ huynh, hoặc gỡ liên kết phụ huynh cuối cùng (BR-030) |
| MSG-112 | Severe allergy declaration requires supporting medical documentation. | `STU-031` | `severity=SEVERE` mà thiếu `proofFileId` khi nộp/xác minh khai báo dị ứng (BR-031) |
| MSG-113 | A reason is required for this action. | `STU-032`, `SUP-048` | Y tế từ chối/yêu cầu bổ sung thông tin không kèm lý do (BR-031, UC-077/078); từ chối hoặc đình chỉ nhà cung cấp không kèm lý do (BR-048, ADR-M04-6) |
| MSG-114 | Dietary type is not in the standard supported list. | ~~`STU-066`~~ — **không còn gán mã nào** | ⚠️ **NGỪNG DÙNG từ 2026-08-02** (không xóa, Policy 4.4). Cite **BR-066** làm oracle cho việc *từ chối*, nhưng BR-066 là danh mục **MỞ** (*"và các loại khác"*) nên không chống lưng được — xem `TestSpec-MF03` §2 **L14**. Thay bằng **MSG-144** + **BR-220**. Nếu MF05 đang dùng MSG-114 thì chuyển sang MSG-144. |
| MSG-115 | This field cannot be edited through this endpoint. | `STU-094` | `PATCH /students/{id}/health-file` gửi kèm field ngoài phạm vi (`allergies`/`specialDiet`/`healthStatus`) — BR-031, BR-172 |
| MSG-116 | Health history is append-only and cannot be modified or deleted. | `STU-172` | `PUT`/`DELETE` vào bản ghi lịch sử sức khỏe (BR-172) |
| MSG-117 | This national ID is already registered for another delivery person. | `SUP-050` | CCCD trùng khi đăng ký nhân viên giao hàng (BR-050, UC-019 AF1) |
| MSG-118 | An open application already exists, or this document has already been submitted. | `SUP-409` | Đã có đơn ở trạng thái PENDING/ACTION_REQUIRED (UC-152/156); hoặc trùng bộ `(partner, type, number)` (BR-040, UC-101 AF3) |
| MSG-119 | Food sample retention is blocked: the meal batch has not passed all 3 inspection steps yet. | `SMP-105` | `inspectionId` chưa PASSED 3/3 khi lưu mẫu (BR-105, UC-069) |
| MSG-120 | Evidence photo is required for food sample retention. | `SMP-107` | Không có file `EVIDENCE_PHOTO` khi lưu mẫu (BR-107) |
| MSG-121 | Sample retention location is outside the designated kitchen/storage area. | `SMP-109` | GPS ngoài geofence trường khi lưu mẫu (BR-109 — CF-M08-5, **không** dùng MSG-015 vì đó là ngữ cảnh nhận nguyên liệu) |
| MSG-122 | Minimum retention period of 24 hours has not elapsed. [Number] hour(s) remaining. | `SMP-111` | Hủy mẫu trước `retentionDeadline` (BR-111 — sửa 26/07/2026 từ 72h xuống 24h, UC-030) |
| MSG-123 | This sample is locked: it is linked to an open food safety incident [Incident ID]. | `SMP-112` | Hủy mẫu đang liên kết sự cố mở hoặc bị cơ quan quản lý yêu cầu giữ (BR-112, UC-030 AF1) |
| MSG-124 | Sample [Sample Code] is no longer in Retained status and cannot be handed over again. | `SMP-113` | Bàn giao mẫu đã ở trạng thái `SENT_TO_LAB`/`DISPOSED` (BR-113, UC-076) |
| MSG-125 | Disposal record is incomplete: disposer, witness, and quality note are required. | `SMP-030` | Payload hủy mẫu thiếu field bắt buộc theo Mẫu số 5 cột 10, 12 (BR-112) |
| MSG-126 | Sample container must be sealed, sanitized, and individually labeled per dish. | `SMP-203` | `sanitized=false` hoặc `sealed=false` khi lưu mẫu (BR-203) |
| MSG-127 | Test criteria or the sample handover record is missing. | `SMP-212` | `testCriteria` rỗng (yêu cầu LAB) hoặc không có file `HANDOVER_RECORD` (BR-212, UC-076) |
| MSG-128 | Incident ID does not exist or the incident is already closed. | `SMP-023` | Bàn giao/liên kết mẫu tới sự cố không tồn tại hoặc đã đóng (BR-114, UC-076 AF1). **Thay cho việc mượn MSG-023** — MSG-023 nói về báo cáo sự cố thiếu thông tin, sai ngữ cảnh (CF-M08-6) |
| MSG-129 | Meal cannot be served: inspection step [Step Name] did not pass. | `SRV-094` | Bữa ăn có `InspectionSession` FAILED (BR-094 — MF07, UC-016 — CF-M09-4) |
| MSG-130 | No approved or published menu found for [Meal] on [Date] for grade [Grade]. Meal distribution cannot be recorded. | `SRV-022` | `MenuQueryPort` không trả block thực đơn ⇒ `servingDishes` rỗng (ADR-M09-1, ràng buộc C3) |
| MSG-131 | Distribution completion is blocked: an unresolved distribution error was reported. | `SRV-023` | Còn `DistributionErrorReport.resolved=false` khi hoàn tất lớp (UC-016 AF1) |
| MSG-132 | Cannot revoke a distribution that was confirmed on a previous day. | `SRV-024` | Thu hồi xác nhận phát suất sau ngày phát (ADR-M09-3) |
| MSG-133 | Consumption record is locked after the serving day. | `SRV-025` | Sửa ghi nhận tiêu thụ sau ngày phục vụ, khi đã vào tóm tắt (BR-121, BR-133) |
| MSG-134 | A quality concern tag is required for ratings below 3 stars. | `FBA-140` | `rating < 3` mà `tags` rỗng khi phụ huynh gửi phản hồi (BR-140) |
| MSG-135 | You have already submitted feedback for this meal. | `FBA-DUP` | Gửi trùng phản hồi cho cùng bộ (người gửi, phiên ăn, món) — ràng buộc `uq_fb_once` (BR-139) |
| MSG-136 | A published announcement cannot be reverted to draft. | `FBA-STATE` | `PATCH` chuyển thông báo từ `PUBLISHED` về `DRAFT` (BR-144) |
| MSG-137 | Resource not found. | `SCH-404`, `STU-404`, `SUP-404`, `SMP-404`, `SRV-404`, `FBA-404` | **Dùng chung mọi module** — tài nguyên không tồn tại hoặc nằm ngoài phạm vi dữ liệu được phép đọc |
| MSG-138 | Validation failed: one or more required fields are missing or invalid. | `SRV-001`, `FBA-REQ` | **Dùng chung mọi module** — DTO sai định dạng, sai giá trị enum, hoặc thiếu field bắt buộc |
| MSG-139 | A record with this value already exists. | `SCH-409` | **Dùng chung mọi module** — D5 (`00_Common_Conventions.md`): client gửi `id`/`code` trùng ở `POST`, hoặc trùng giá trị unique khác (VD `taxId`) |
| MSG-145 | Holding temperature is below the required minimum of 60°C. | `INSP-091` | Nhiệt độ giữ ấm dưới 60°C khi chốt Bước 3 kiểm thực (**BR-091**, UC-052/UC-065). ⚠️ **Không** dùng MSG-020 — chuỗi đó là *"Retention refrigerator temperature is out of safe range (2-8°C)"*, thuộc **BR-110/MF08** (tủ lưu mẫu), khác hẳn nghiệp vụ. Cùng lý do đã tách MSG-140/142 khỏi MSG-010 |
| MSG-146 | Ingredient batch [Batch Code] has expired and cannot be used in this inspection. | `INSP-092` | Bước 1 kiểm thực dùng lô nguyên liệu quá hạn sử dụng (**BR-092**, UC-052). Hệ thống tự tra `InventoryBatch.expiryDate` của MF06 |
| MSG-147 | Step [Step Name] is locked until the previous step has passed. | `INSP-079` | Nộp Bước 2/Bước 3 khi bước liền trước chưa `PASSED` (**BR-079** — QĐ 1246/QĐ-BYT Điều 3–5 quy định **đúng 3 bước theo thứ tự**) |
| MSG-148 | An evidence photo is required for every item marked as not compliant. | `INSP-083` | Nộp bước kiểm thực / hạng mục checklist đánh giá "Không đạt" mà thiếu ảnh bằng chứng (**BR-083**, **BR-207**). ⚠️ **Không** dùng MSG-120 — chuỗi đó ghi cứng *"for food sample retention"*, thuộc BR-107/MF08 |
| MSG-140 | Classroom capacity cannot be reduced below the current number of enrolled students. | `SCH-012` | Cập nhật `capacity` nhỏ hơn sĩ số hiện tại của lớp (**BR-219** — mới 2026-07-31, tách khỏi `SCH-010`/MSG-010 theo D7) |
| MSG-141 | Too many requests. Please try again later. | `SCH-014`, và mã tương ứng ở module khác có rate limit (MF05 AI, MF11, MF12) | **Dùng chung mọi module** — vượt ngưỡng rate limit (HTTP 429), VD `POST /schools/register` 5/min/IP |
| MSG-142 | This classroom has reached its capacity. | `STU-010` | Thêm/chuyển học sinh vào lớp đã đủ sĩ số theo `capacity` **của chính lớp đó** (BR-025). **Tách khỏi MSG-010** theo D7 — MSG-010 ghi cứng *"Maximum allowed is 50 students"* nên **sai ngữ nghĩa** ở lớp `capacity < 50`. Sĩ số thật + trần đặt trong `error.details` `{field, capacity, current}` (D2) |
| MSG-143 | System error while saving student data. Please try again. | `STU-500` | Lỗi hệ thống khi lưu dữ liệu học sinh. **Không** mượn MSG-001 — MSG-001 ghi nguyên văn *"school data"*, thuộc School & Class Management |
| MSG-144 | This dietary type is outside the standard list and requires medical justification before approval. | `STU-066` | Duyệt yêu cầu suất ăn đặc biệt có `dietaryType` **ngoài** danh mục chuẩn (**BR-220** — mới 2026-08-02). **Không phải lỗi từ chối**: yêu cầu chuyển `NEEDS_INFO` chờ bổ sung căn cứ, vì **BR-066 là danh mục MỞ**. Danh mục 5 giá trị đặt trong `error.details` |
| MSG-149 | An incident record can only be created from a source report that is pending triage. | `IEM-114` | Gộp báo cáo thô (`RPT`) không ở trạng thái `PENDING_TRIAGE` thành biên bản sự cố (UC-114, ADR-M11-1) |
| MSG-150 | Incident severity must be one of LOW, MEDIUM or HIGH. | `IEM-147` | Phân loại mức độ sự cố với giá trị ngoài enum, hoặc rời `SEVERITY_PENDING` mà chưa chọn mức (**BR-147**, UC-009). Sentinel `"PENDING"` là **giá trị hiển thị** khi cột `severity IS NULL`, **không** phải giá trị hợp lệ để ghi |
| MSG-151 | A quarantine reason is required before a batch can be quarantined. | `IEM-149` | Cách ly lô nghi vấn mà `reason` rỗng (**BR-149** sửa 27/07/2026). Kiểm ở **server**, không phải ở dialog FE — cách ly là hành động **không thể hoàn tác** |
| MSG-152 | Batch [Batch Code] was already quarantined at [Timestamp]. This action cannot be undone. | `IEM-149B` | Cách ly lại lô đã cách ly (**BR-217**, ADR-M11-3 — một chiều, không có endpoint nhả lô). ⚠️ **Không gộp với MSG-155**: mã này chặn *cách ly lại*, MSG-155 chặn *đóng sự cố* — gộp là một `error.code` mang 2 nghĩa, đúng lỗi đã phải tách MSG-128/MSG-140/MSG-142 |
| MSG-153 | This incident cannot be closed: [Number] corrective actions are still open and [Number] affected students have not been confirmed as recovered. | `IEM-155` | Đóng sự cố (UC-013) hoặc xác nhận CAPA (UC-168 EX1) khi chưa đủ điều kiện (**BR-155** + ràng buộc **C1** `data_model.puml`). Hai con số đặt trong `error.details` `{field, capaOpen, studentsNotRecovered}` (D2). ⚠️ **Thay MSG-004** mà UC-168 EX1 đang chỉ định — MSG-004 là *"Session expired"*, sẽ đá người dùng ra màn đăng nhập giữa lúc xử lý sự cố (CF-M11-15) |
| MSG-154 | Invalid corrective action transition, or incident [Incident Code] already has corrective action [CAPA Code]. | `IEM-216` | Sai thứ tự vòng đời CAPA 6 trạng thái, hoặc tạo CAPA thứ 2 cho cùng sự cố (**BR-216** — partial unique index `uq_capa_one_per_incident`) |
| MSG-155 | This incident cannot be closed: quarantined batches have not reached a final state. | `IEM-217` | Đóng sự cố khi lô cách ly chưa ở trạng thái cuối (`RETURNED_TO_SUPPLIER`/`DISPOSED`) bên MF06 (**BR-217**). Thay luật audit `DONG_SU_CO_MA_CON_LO_KHOA` đã lỗi thời sau khi BR-217 bỏ nhả lô (CF-M11-11) |
| MSG-156 | Export and print the violation record PDF for signature before uploading the signed copy. | `IEM-211` | Tải ảnh biên bản đã ký khi `pdfExported = false` (**BR-211**). Khác **MSG-095** — MSG-095 chặn *đóng/gửi duyệt khi chưa có ảnh ký*, mã này chặn *tải ảnh ký khi chưa xuất PDF*; hai bước khác nhau của cùng quy trình 3 bên |
| MSG-157 | Violations rated MEDIUM or HIGH must be approved by School Administration. | `IEM-186` | Medical Staff tự chuyển biên bản mức ≥ MEDIUM sang `APPROVED` (**BR-186**, UC-180/UC-182) |
| MSG-158 | This incident is read-only: the official report has already been submitted. | `IEM-RO` | Mọi thao tác ghi trên hồ sơ sự cố sau khi `reportExported = true` (UC-033, ADR-M11-8 — khoá **một chiều**, không có endpoint mở khoá) |

---

## 2. Success Messages

Các thông báo xác nhận thao tác đã được thực hiện thành công.

| MSG Code | Message Content | Trigger Context |
|---|---|---|
| MSG-026 | Login successful. Welcome back, [Name]! | Đăng nhập thành công |
| MSG-027 | Logout successful. Session terminated safely. | Đăng xuất thành công |
| MSG-028 | Registration request submitted! A verification email has been sent. | Đăng ký thành công, email xác minh đã gửi |
| MSG-029 | Profile updated successfully. | Cập nhật hồ sơ thành công |
| MSG-030 | Password changed successfully. | Đổi mật khẩu thành công |
| MSG-031 | User account created successfully. | Tạo tài khoản người dùng thành công |
| MSG-032 | School registered successfully. Pending System Admin approval. | Đăng ký trường học thành công, chờ phê duyệt |
| MSG-033 | Classroom created successfully. | Tạo lớp học thành công |
| MSG-034 | Homeroom teacher assigned successfully. | Phân công giáo viên chủ nhiệm thành công |
| MSG-035 | Student profiles and parent links imported successfully. | Nhập hàng loạt hồ sơ học sinh thành công |
| MSG-036 | Allergy profile updated successfully and alerts set. | Cập nhật hồ sơ dị ứng và thiết lập cảnh báo thành công |
| MSG-037 | Supplier onboarding application approved successfully. | Phê duyệt hồ sơ nhà cung cấp thành công |
| MSG-038 | Delivery staff face biometrics registered successfully. | Đăng ký sinh trắc học khuôn mặt thành công |
| MSG-039 | Menu draft submitted for approval successfully. | Nộp thực đơn để phê duyệt thành công |
| MSG-040 | Menu approved and published successfully. Notifications sent to parents. | Phê duyệt và công bố thực đơn thành công |
| MSG-041 | Ingredient delivery received. Inventory levels updated. | Nhận hàng và cập nhật kho thành công |
| MSG-042 | Inventory transaction recorded and stock adjusted. | Ghi nhận giao dịch kho thành công |
| MSG-043 | Food safety inspection checklist submitted successfully. | Nộp checklist kiểm tra ATTP thành công |
| MSG-044 | Food sample retention registered. Cold storage timer initiated. | Đăng ký lưu mẫu thực phẩm thành công |
| MSG-045 | Meal distribution list confirmed for class [Class Name]. | Xác nhận danh sách phân phát bữa ăn cho lớp |
| MSG-046 | Meal feedback submitted successfully. Thank you for your feedback! | Nộp phản hồi bữa ăn thành công |
| MSG-047 | Announcement published successfully to target audience. | Công bố thông báo thành công |
| MSG-048 | Suspected food safety incident report logged. Investigation map generated. | Ghi nhận báo cáo sự cố, tạo bản đồ truy xuất |
| MSG-049 | Corrective actions plan submitted successfully for review. | Nộp kế hoạch CAPA để xem xét thành công |
| MSG-050 | System database backup completed successfully and uploaded to secure cloud. | Sao lưu cơ sở dữ liệu thành công |
| MSG-071 | Menu draft was saved successfully. | Lưu bản nháp thực đơn thành công |
| MSG-072 | Special meal plan saved successfully. | Lưu kế hoạch bữa ăn đặc biệt thành công |
| MSG-074 | Menu updated successfully. | Cập nhật thực đơn thành công |
| MSG-078 | Saved offline. Will sync automatically once connection is restored. | Ghi nhận offline, chờ đồng bộ (BR-196) |
| MSG-082 | Academic year created successfully. | Tạo năm học thành công (BR-192) |
| MSG-083 | Academic year status updated successfully. | Cập nhật trạng thái năm học thành công (BR-191, BR-193) |
| MSG-084 | Students promoted to next academic year successfully. | Lên lớp hàng loạt thành công (BR-194) |
| MSG-086 | Violation record saved successfully. | Lập biên bản vi phạm thành công (BR-183/BR-184) |
| MSG-087 | Violation resolution submitted for approval. | Nộp Biên bản Xử lý Sai phạm để phê duyệt thành công (BR-185) |
| MSG-089 | Violation resolution approved. Disciplinary action applied. | Phê duyệt xử lý sai phạm, áp dụng hình thức kỷ luật (BR-186) |
| MSG-091 | Compliance report sent successfully to School Administration. | Đã gửi báo cáo tuân thủ ATTP tới School Administration thành công (BR-209, UC-185 — bổ sung 05/07/2026) |
| MSG-098 | Incident summary report sent successfully to School Administration. | Đã gửi báo cáo tổng hợp sự cố tới School Administration thành công (BR-213, UC-188 — bổ sung 13/07/2026) |
| MSG-099 | Violation summary report sent successfully to School Administration. | Đã gửi báo cáo tổng hợp vi phạm tới School Administration thành công (BR-214, UC-189 — bổ sung 13/07/2026) |
| MSG-100 | Incident record draft saved successfully. | Đã lưu bản nháp biên bản sự cố (UC-114 mở rộng — bổ sung 15/07/2026) |
| MSG-101 | Incident record finalized successfully. Ready for review. | Đã hoàn tất biên bản sự cố, chuyển sang danh sách chờ xử lý (UC-114 mở rộng — bổ sung 15/07/2026) |
| MSG-159 | Incident severity assigned successfully. Investigation is now in progress. | Phân loại mức độ sự cố, `SEVERITY_PENDING → INVESTIGATING` (**UC-009**, BR-147 — bổ sung 04/08/2026). **Thay MSG-029** mà traceability §1.11 đang gán — MSG-029 là *"Profile updated successfully"*, không mô tả hành động này (CF-M11-6) |
| MSG-160 | Corrective action plan approved. The assigned unit can now begin implementation. | Ban giám hiệu duyệt kế hoạch khắc phục, `PENDING_ADMIN_APPROVAL → APPROVED_FOR_IMPLEMENTATION` (**UC-005**, BR-153/BR-216 — bổ sung 04/08/2026). **Thay MSG-029**; đồng thời **thay MSG-043** mà mock FE đang dùng nhầm (MSG-043 là *"Food safety inspection checklist submitted"*, thuộc MF07) |
| MSG-161 | Incident closed successfully. The case record is now read-only. | Đóng sự cố sau khi đủ 3 điều kiện gate (**UC-013**, BR-155 — bổ sung 04/08/2026). **Thay MSG-029**; đồng thời **thay MSG-046** mà mock FE đang dùng nhầm (MSG-046 là *"Meal feedback submitted"*, thuộc MF10) |

---

## 3. Warning Messages

Các thông báo cảnh báo không chặn thao tác nhưng yêu cầu chú ý hoặc hành động phòng ngừa.

| MSG Code | Message Content | Trigger Context |
|---|---|---|
| MSG-051 | Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] affecting [Number] students. | Phát hiện xung đột dị ứng trong thực đơn (BR-063) |
| MSG-052 | Ingredient batch [Batch ID] is close to expiration ([Expiration Date]). | Lô nguyên liệu sắp hết hạn (BR-205, sửa 2026-07-02 — trước đây gán nhầm BR-047/BR-083: BR-047 là cảnh báo hết hạn tài liệu nhà cung cấp, BR-083 là ghi nhận bằng chứng Three-Step, đều không liên quan hạn nguyên liệu kho) |
| MSG-053 | Supplier food safety certificate is expiring in [Number] days. | Chứng nhận ATTP nhà cung cấp sắp hết hạn (BR-047) |
| MSG-054 | Safety stock level reached for ingredient [Ingredient Name]. Reorder recommended. | Tồn kho đạt mức tối thiểu an toàn (BR-077/BR-084) |
| MSG-055 | High temperature alert in inventory storage area: [Temperature]°C. | Cảnh báo nhiệt độ cao trong khu vực lưu kho (BR-089) |
| MSG-056 | Re-inspection required: Failed safety check during inspection step [Step Name]. | Cần tái kiểm tra sau thất bại một bước (BR-096/BR-098) |
| MSG-057 | Negative meal feedback rating (<3 stars) received from parent. | Phụ huynh đánh giá thấp dưới 3 sao (BR-140) |
| MSG-058 | Illness keywords detected in parent feedback. Potential incident flag. | Phát hiện từ khóa bệnh lý trong phản hồi (BR-142) |
| MSG-059 | Retention refrigerator door left open or temperature fluctuation detected. | Tủ lưu mẫu mở hoặc nhiệt độ dao động (BR-110/BR-089) |
| MSG-060 | Supplier monthly performance index score has dropped below 70%. | Chỉ số hiệu suất nhà cung cấp dưới 70% |
| MSG-061 | System load threshold reached. Auto-scaling initiated. | Ngưỡng tải hệ thống đạt giới hạn |
| MSG-062 | Suspicious login attempt detected from a new location/device. Verification code required. | Phát hiện đăng nhập đáng ngờ từ thiết bị mới (BR-002) |
| MSG-063 | Weak password rating. Recommend adding special characters and numbers. | Mật khẩu yếu, không đáp ứng chính sách (BR-006) |
| MSG-064 | Incomplete student health profiles detected for class [Class Name]. | Hồ sơ sức khỏe học sinh chưa đầy đủ (BR-031) |
| MSG-065 | Delivery delay alert: Supplier delivery time deviates from schedule. | Giao hàng trễ so với lịch (BR-135) |
| MSG-066 | Weekly sanitation audit report is overdue. Please submit immediately. | Báo cáo kiểm tra vệ sinh tuần chưa nộp |
| MSG-067 | AI menu analysis: Nutritional diversity is below the required 3-group minimum. | Phân tích AI: Thực đơn thiếu đa dạng dinh dưỡng (BR-062) |
| MSG-068 | Push notification delivery queue is backed up. Retrying failed targets. | Hàng đợi push notification bị tắc nghẽn (BR-159) |
| MSG-069 | External API response latency is high (exceeds 2000ms). | Độ trễ API bên ngoài cao (BR-163) |
| MSG-070 | Backup space alert: Backup storage utilization is at 92% capacity. | Dung lượng lưu trữ backup gần đầy (BR-160) |
| MSG-073 | Gemini AI Service is currently unavailable. Please try again later. | Dịch vụ Gemini AI không khả dụng |
| MSG-079 | Cached data is outdated ([Number] hours old). Please reconnect to sync before continuing. | Cache offline quá hạn, cần đồng bộ lại (BR-200) |
| MSG-088 | Possible repeat violation detected: staff name matches a prior violation within the lookback window. Manual verification required. | Nghi ngờ tái diễn vi phạm, cần School Administration xác minh thủ công (BR-187) |
| MSG-093 | Kitchen safety score below 60% threshold. Alert sent to School Administration — kitchen operations continue as normal. | Điểm kiểm tra bếp dưới ngưỡng 60%. Đã gửi cảnh báo tới School Administration — bếp vẫn vận hành bình thường, không bị khóa (UC-014 AF1 — descope 05/07/2026, thay cho MSG-024 dùng sai module trước đây; không gắn mã BR do chưa có căn cứ pháp lý cho ngưỡng 60%, xem `business_rules.md` §12) |
| MSG-094 | This checklist item/group has been used in saved inspection records. Deleting it will not remove historical data — it will only be hidden from new inspections. Continue? | Nhóm/hạng mục này đã được dùng trong các phiên kiểm tra đã lưu. Xóa sẽ không xóa dữ liệu lịch sử — chỉ ẩn khỏi các phiên kiểm tra mới. Tiếp tục? (UC-186, BR-210 — mới 06/07/2026; cảnh báo xác nhận, không phải lỗi chặn cứng) |

---

## 4. Tóm tắt theo Module

> **v1.5:** MSG-137 (Resource not found) và MSG-138 (Validation failed) **dùng chung cho mọi module** — không liệt kê lặp ở từng dòng dưới đây.
> **v1.6:** MSG-139 (Duplicate value) cũng **dùng chung mọi module** — cùng quy tắc.
> **v1.8:** MSG-141 (Too many requests / rate limit) cũng **dùng chung mọi module** — cùng quy tắc.

| Module | MSG Codes |
|---|---|
| User Management & Authentication | MSG-002, MSG-003, MSG-004, MSG-005, MSG-006, MSG-026, MSG-027, MSG-028, MSG-029, MSG-030, MSG-031, MSG-062, MSG-063, MSG-076, **MSG-104, MSG-105, MSG-106** |
| School & Class Management | MSG-001, MSG-007, MSG-010, MSG-011, MSG-032, MSG-033, MSG-034, **MSG-107, MSG-108, MSG-109, MSG-139, MSG-140, MSG-141** |
| Student & Parent Management | MSG-012, MSG-035, MSG-036, ~~MSG-064~~ *(không UC nào của §1.3 tham chiếu — CF-M03-12, `TestSpec-MF03` L15)*, **MSG-110, MSG-111, MSG-112, MSG-113, ~~MSG-114~~ → MSG-144, MSG-115, MSG-116**, **MSG-142, MSG-143, MSG-144** |
| Supplier & Partner Management | MSG-013, MSG-014, MSG-037, MSG-038, MSG-053, MSG-060, **MSG-113, MSG-117, MSG-118** |
| Ingredient & Inventory Management | MSG-008, MSG-009, MSG-015, MSG-018, MSG-041, MSG-042, MSG-052, MSG-054, MSG-055, MSG-065 |
| Menu & Diet Management | MSG-039, MSG-040, MSG-051, MSG-067, MSG-071, MSG-072, MSG-073, MSG-074, MSG-075, MSG-090 |
| **Food Safety Inspection (MF07)** — *đã tách 2026-08-03 v1.10, xem ghi chú dưới bảng* | MSG-016, MSG-017, MSG-043, MSG-056, MSG-093, MSG-094, MSG-129, **MSG-145, MSG-146, MSG-147, MSG-148** |
| **ATTP Report Center (MF12 — UC-185/187/188/189, §1.12)** — *tách khỏi dòng trên 2026-08-03* | MSG-091, MSG-092, MSG-096, MSG-097, MSG-098, MSG-099 |
| Food Sample Retention | MSG-019, MSG-020, MSG-044, MSG-059, **MSG-119, MSG-120, MSG-121, MSG-122, MSG-123, MSG-124, MSG-125, MSG-126, MSG-127, MSG-128** |
| Meal Serving & Distribution | MSG-021, MSG-045, **MSG-129, MSG-130, MSG-131, MSG-132, MSG-133** |
| Feedback & Announcement | MSG-022, MSG-046, MSG-047, MSG-057, MSG-058, **MSG-134, MSG-135, MSG-136** |
| **Incident Management (MF11 — §1.11)** (+biên bản sự cố thủ công, bổ sung 15/07/2026) | MSG-023, ~~MSG-024~~ *(khoá bếp đã descope 05/07/2026 — **không endpoint MF11 nào phát ra**, giữ để lưu vết §1.11; CF-M11-7)*, MSG-048, MSG-049, MSG-100, MSG-101, **MSG-149, MSG-150, MSG-151, MSG-152, MSG-153, MSG-154, MSG-155, MSG-158**, **MSG-159, MSG-160, MSG-161** |
| **Violation Management (MF11 — §1.11, gộp vào MF11)** (+BR-211, bổ sung 06/07/2026) | MSG-085, MSG-086, MSG-087, MSG-088, MSG-089, MSG-095, **MSG-156, MSG-157** |
| AI & System Services | MSG-025, MSG-050, MSG-061, MSG-068, MSG-069, MSG-070 |
| Offline & Data Sync | MSG-077, MSG-078, MSG-079 |
| Academic Year Management | MSG-080, MSG-081, MSG-082, MSG-083, MSG-084 |
| Violation Management | MSG-085, MSG-086, MSG-087, MSG-088, MSG-089 |

---

## Ghi chú về Placeholder

Một số thông báo sử dụng **placeholder động** (thay thế khi runtime):

| Placeholder | Mô tả |
|---|---|
| `[Name]` | Tên đầy đủ của người dùng đăng nhập |
| `[Dish Name]` | Tên món ăn cụ thể |
| `[Allergen]` | Tên chất gây dị ứng |
| `[Number]` | Số lượng học sinh bị ảnh hưởng / số ngày |
| `[Batch ID]` | Mã số lô hàng nguyên liệu |
| `[Expiration Date]` | Ngày hết hạn cụ thể |
| `[Ingredient Name]` | Tên nguyên liệu cụ thể |
| `[Temperature]` | Giá trị nhiệt độ đo được |
| `[Step Name]` | Tên bước kiểm tra (Step 1/2/3) |
| `[Class Name]` | Tên lớp học |
| `[Minutes]` | Số phút còn lại của lockout đăng nhập (MSG-076) |
| `[Sample Code]` | Mã mẫu lưu, VD `SMP-20260706-01` (bổ sung v1.5) |
| `[Incident ID]` | Mã sự cố ATTP, VD `INC-2026-014` (bổ sung v1.5) |
| `[Meal]` | Buổi ăn — Bữa sáng / Bữa trưa / Bữa phụ (bổ sung v1.5) |
| `[Date]` | Ngày cụ thể, định dạng ISO `yyyy-MM-dd` (bổ sung v1.5) |
| `[Grade]` | Khối lớp, VD `Khối 3` (bổ sung v1.5) |

---

*Tài liệu này được tổng hợp từ Section 3.5.2 System Messages của Report 3 (SRS) — Dự án SFMS SEP490 G8.*
