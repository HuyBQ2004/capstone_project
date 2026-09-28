# Design Documentation Plan — Thiết kế UI cho SFSMSS (03_Design/)

> **Mục đích tài liệu này**: Kế hoạch xây dựng bộ tài liệu thiết kế UI hoàn chỉnh cho hệ thống School Food Safety Management Support System (SFMS/SFSMSS), dựa trên rà soát toàn bộ tài liệu hiện có trong workspace.
> **Phương pháp**: Chỉ dùng thông tin trích xuất trực tiếp từ mã nguồn tài liệu (đường dẫn + trích dẫn kèm theo). Không suy diễn nghiệp vụ. Mọi thiếu sót được đánh dấu `TODO`. Mọi mâu thuẫn được liệt kê kèm đường dẫn file cụ thể.
> **Nguồn đã đọc** (theo đúng thứ tự ưu tiên yêu cầu):
> 1. `03_Design/02_ADR/ADR-000.md` … `ADR-005.md`
> 2. `03_Design/01_Diagrams/01_Class_Diagram/class_diagram_description.md`
> 3. `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md`
> 4. `06_Document/01_Report/Report 1.md`
> 5. `06_Document/01_Report/Report 3.md` (mục 3.3 Functional Requirements, mục 3.4 Non-Functional Requirements, Requirement Appendix)
> 6. Đối chiếu bổ sung: `03_Design/03_Workflow/00_Business_Workflows.md`, `03_Design/04_ScreenFlow/*.md`, `02_Requirements/00_BusinessRules/business_rules.md`, `07_implement/MF01_User_Management_Access_Control/ADR-001_User_Auth_Architecture.md`, `06_Document/Templates/PHASE-2_ADR.md`, thư mục `05_Testing/`, `04_Development/`, `01_ProjectPlanning/`, `00_Policies/`.

---

## 0. Bối cảnh dự án (căn cứ xác nhận trước khi lập kế hoạch)

- Theo `06_Document/01_Report/Report 1.md` (mục "Group's R&D Operational Sequence"), dự án đang ở **Phase 5: Database & Architecture Design**, trước **Phase 6: Iterative Sprint Development**. Điều này khớp với thực tế: `04_Development/` và `05_Testing/` hiện **rỗng** (không có file nào), và `03_Design/` mới có kiến trúc (ADR), class diagram/ERD, business workflow, và screen flow — **chưa có bất kỳ tài liệu Design System / Wireframe / Prototype nào**.
- Phần mềm mục tiêu: **Web App + Mobile App** (`Report 1.md`, dòng "Software Type: Web App, Mobile App"), kiến trúc gốc theo `03_Design/02_ADR/ADR-001.md`: **Next.js (Web)** cho vai trò quản trị/dashboard phức tạp + **Flutter (Mobile)** cho vai trò cần camera/GPS/thao tác hiện trường.
- **Cập nhật hướng đang áp dụng cho kế hoạch này (2026-07-01)**: team đang nghiêng theo hướng **"all web + Mobile song song đầy đủ"** — mọi actor đều có bản Web (kể cả 5 role hiện chỉ có Mobile), Mobile **giữ nguyên phạm vi hiện có** cho 5 role field (không bị thu hẹp thành fork từng tính năng nhỏ lẻ). Kế hoạch dưới đây đã được cập nhật theo hướng này (mục 3, 4.5, 5, 6.4, 8, 8b). **Đây vẫn là quyết định làm việc (working decision), chưa phải ADR chính thức** — cần một ADR mới bổ sung (không supersede) `ADR-001.md`, ghi nhận 5 role field có 2 client song song thay vì 1.
- Toàn bộ 03_Design hiện tại đã áp dụng phương pháp "CASE 2.0" cho ADR (xem `06_Document/Templates/PHASE-2_ADR.md`) — có section `§AI Prompt Constraint` bắt buộc, thiết kế để inject thẳng vào prompt AI khi implement. Kế hoạch này **kế thừa tinh thần đó**: mỗi loại tài liệu UI mới đề xuất cũng có phần "AI Generation Prompt" tương ứng, phục vụ mục tiêu sinh wireframe/prototype bằng AI (Stitch, Figma AI, ChatGPT...).

---

## 1. Tình trạng hiện tại của `03_Design/`

| Thư mục/File | Trạng thái | Nội dung |
| --- | --- | --- |
| `01_Diagrams/01_Class_Diagram/` | ✅ Đã có | Class diagram PlantUML + mô tả (`class_diagram_description.md`), 12 package nghiệp vụ |
| `01_Diagrams/02_ERD/erd_crowsfoot.puml` | ✅ Đã có | ERD Crow's Foot, nguồn từ class diagram + business rules |
| `01_Diagrams/assign_permission_to_role.puml` | ✅ Đã xoá | Xem mục 2.2 — file lạc domain, đã xử lý |
| `01_Diagrams/save_exam_progress_sequence.puml` | ✅ Đã xoá | Xem mục 2.2 — file lạc domain, đã xử lý |
| `02_ADR/ADR-000.md` … `ADR-005.md` | ✅ Đã có, mâu thuẫn đánh số đã xử lý | Xem mục 2.1 |
| `03_Workflow/00_Business_Workflows.md` | ✅ Đã có | 12 business workflow, Mermaid, dựng lại từ Report 3 (ảnh gốc là base64 không đọc được) |
| `04_ScreenFlow/00_Screen_Flow_Overview.md` + 7 file theo actor | ✅ Đã có | Sơ đồ điều hướng màn hình theo actor, dựng lại từ bảng Screen Authorization + mô tả field-by-field trong Report 3 |
| **Design Foundation** | 🆕 Chưa có | — |
| **Design System** | 🆕 Chưa có | Không tìm thấy bất kỳ tài liệu color palette/typography/component ở đâu trong repo (đã grep toàn repo: `design system`, `color palette`, `typography`, `figma`, `design token` — chỉ có 1 kết quả là ghi chú trong `04_ScreenFlow/00_Screen_Flow_Overview.md` nhắc tới việc "khi có file Figma/Balsamiq") |
| **UI Guidelines** | 🆕 Chưa có | — |
| **Screen Specifications** | 🆕 Chưa có (dữ liệu thô đã có rải rác trong Report 3, chưa được tách thành tài liệu đặc tả riêng) | — |
| **Wireframes** | 🆕 Chưa có (Report 3 có mô tả dạng bảng field-by-field cho 1 phần màn hình Mobile — xem mục 5) | — |
| **Prototype** | 🆕 Chưa có | — |
| **Assets** | 🆕 Chưa có | — |

---

## 2. Mâu thuẫn & vấn đề phát hiện được trong tài liệu nguồn

Theo quy tắc bắt buộc của nhiệm vụ, các mâu thuẫn dưới đây được liệt kê rõ kèm đường dẫn file — **không tự suy diễn cách xử lý**, chỉ nêu vấn đề để team xác nhận.

### 2.1. Xung đột đánh số ADR — ĐÃ CHỐT quy ước

> **Cập nhật (2026-07-01, đã xử lý)**: cả 2 xung đột dưới đây đã được sửa trực tiếp trong các file ADR liên quan. Mục này giữ lại làm bằng chứng vấn đề gốc, không còn là vấn đề mở.

- `03_Design/02_ADR/ADR-000.md` — trước đây nội dung file tự nhận tiêu đề **"ADR-002 — Môi trường Phát triển Cục bộ (Local Development Environment)"** (dòng 1), trùng với `03_Design/02_ADR/ADR-002.md` (Face Biometric). → **Đã sửa**: tiêu đề và 2 chỗ tự tham chiếu bên trong file đổi lại thành **"ADR-000"** cho khớp tên file — không có tài liệu nào khác trong repo trích dẫn file này bằng số "ADR-002" nên việc sửa không ảnh hưởng tham chiếu chéo. `ADR-002.md` giữ nguyên là số ADR-002 chính thức, duy nhất (Face Biometric — đã được `00_Business_Workflows.md` và `ADR-004.md` trích dẫn với nội dung này từ trước).
- `07_implement/MF01_User_Management_Access_Control/ADR-001_User_Auth_Architecture.md` (dòng 1): **"ADR-001 — Authentication & Authorization Architecture for User Management Module"**, Status `Accepted`, ngày `2026-06-25` — cùng ngày và cùng số "ADR-001" với `03_Design/02_ADR/ADR-001.md` ("ADR-001 — Lựa chọn Kiến trúc Tổng thể cho Hệ thống SFMS"). Đây là **hai ADR-001 khác nội dung**, một ở cấp hệ thống (`03_Design/02_ADR/`) và một ở cấp module (`07_implement/`). → **Đã chốt quy ước**: 2 namespace độc lập — ADR **cấp hệ thống** dùng số trần (`03_Design/02_ADR/ADR-XXX.md`), ADR **cấp module** luôn giữ hậu tố mô tả trong tên file (`ADR-XXX_TênMôTả.md`) và phải trích dẫn bằng tên đầy đủ, không rút gọn thành "ADR-XXX" trần. Đã thêm ghi chú xác nhận vào đầu cả 2 file `ADR-001.md` (hệ thống) và `ADR-001_User_Auth_Architecture.md` (module). Đồng thời sửa danh sách "Decisions unlocked" trong ADR module (dự kiến ADR-002/003 tiếp theo cho Password History/Email Verification) để chỉ rõ đây là số module-scoped, không trùng ADR-002 (Face Biometric)/ADR-003 (RAG Pipeline) hệ thống.

**Tác động tới kế hoạch UI**: Từ nay Screen Specification/Wireframe có thể trích dẫn ADR làm nguồn ràng buộc mà không còn rủi ro trỏ nhầm file (VD: màn hình xác thực khuôn mặt Delivery Person → trích `03_Design/02_ADR/ADR-002.md`, không còn nhầm với ADR-000).

### 2.2. Nghi vấn file không thuộc domain SFSMSS — ĐÃ XỬ LÝ (đã xoá)

> **Cập nhật (2026-07-01)**: team đã xác nhận và xoá 2 file dưới đây khỏi `03_Design/01_Diagrams/`. Mục này giữ lại làm bằng chứng vấn đề gốc.

- ~~`03_Design/01_Diagrams/assign_permission_to_role.puml`~~: dùng naming convention kiểu C#/.NET (`AddPermissionToRoleVM`, `Task<IActionResult>`, hậu tố `VM`) — không khớp với style Java/Spring Boot + UUID của `class_diagram_description.md` và ADR-001 (Spring Boot, Spring Data JPA).
- ~~`03_Design/01_Diagrams/save_exam_progress_sequence.puml`~~: nội dung là sequence diagram cho nghiệp vụ **"Save Exam Progress"** (`StudentExamController`, `StudentExamService`, `studentAnswerRepository`) — hoàn toàn không liên quan tới an toàn thực phẩm học đường.

→ Xác nhận: hai file này là **tài liệu sót lại từ dự án khác** (copy nhầm khi khởi tạo cấu trúc thư mục) — đã xoá, không còn tồn tại trong `01_Diagrams/`.

### 2.3. Mâu thuẫn về khả năng hoạt động offline — ĐÃ CHỐT chính sách chính thức

> **Cập nhật (2026-07-01, đã xác nhận với team)**: hệ thống áp dụng chính sách **offline có giới hạn** cho Mobile — cho phép ghi nhận bằng chứng khi mất kết nối, nhưng bắt buộc kết nối mạng cho mọi thao tác xác minh an toàn thời gian thực. Web luôn yêu cầu kết nối như trước (không đổi). Quyết định đã được hình thức hoá thành BR-196–200 (`02_Requirements/00_BusinessRules/business_rules.md` mục 17) và MSG-077/078/079 (`02_Requirements/00_BusinessRules/error_message.md`), với hướng dẫn UI chi tiết tại `00_Foundation/UI_Design_Guidelines.md` mục 5. Mục dưới đây giữ lại làm bằng chứng mâu thuẫn gốc đã phát hiện, không còn là vấn đề mở.

- `06_Document/01_Report/Report 1.md` (mục "Overview", đoạn 2): *"...offline-capable inspection workflows help maintain system usability in kitchen environments where internet connectivity may be unstable."* Mục "Global technology trends" cũng liệt kê "Offline-capable operational interfaces" như một xu hướng công nghệ mà SFMS hướng tới, và phần so sánh đối thủ (SafetyCulture) khen ngợi "offline operation" là điểm mạnh.
- `06_Document/01_Report/Report 3.md`, mục **Non-Functional Requirements → Operational Constraints** (dòng ~6219): *"The system requires a stable Internet connection for full functionality."* — không có mục NFR nào khác mô tả cơ chế offline-first, queue đồng bộ, hay conflict resolution.

→ **Mâu thuẫn gốc**: Report 1 định vị SFMS như một giải pháp có khả năng offline (luận điểm bán hàng/định vị sản phẩm), còn Report 3 yêu cầu kết nối Internet ổn định, không đặc tả cơ chế offline nào. → **Giải quyết**: chọn hướng trung gian — evidence capture (chụp ảnh/GPS/checklist, BR-196) được phép offline qua sync queue cục bộ; xác minh an toàn thời gian thực (cảnh báo dị ứng BR-123, trạng thái Approved nhà cung cấp BR-070, Quarantine BR-149, sinh trắc học BR-051 — xem BR-197) luôn bắt buộc online và bị chặn nếu mất kết nối; xung đột đồng bộ xử lý theo last-write-wins theo timestamp server (BR-199); cache offline có hạn sử dụng (BR-200, số giờ cụ thể còn là TODO). Chi tiết đầy đủ và hành vi UI từng trạng thái xem `00_Foundation/UI_Design_Guidelines.md` mục 5.

### 2.4. Hai nguồn Business Rules không khớp nhau — ĐÃ CHỐT nguồn chính thức

> **Cập nhật (đã xác nhận với team)**: `06_Document/01_Report/Report 3.md` (Requirement Appendix → Business Rules) là **bản cũ, không chính xác**. `02_Requirements/00_BusinessRules/business_rules.md` mới là **bản đã được duyệt, chính xác** — dùng làm nguồn BR duy nhất cho mọi tài liệu từ đây trở đi (Screen Specification, đề xuất mới...). Mục dưới đây giữ lại làm bằng chứng đã xác minh, không còn là vấn đề mở.

Các trường hợp cụ thể đã phát hiện lệch nhau giữa 2 nguồn (Report 3 = cũ/sai, business_rules.md = đúng):

- **BR-068**: `business_rules.md` (dòng 236) = *"Quy trình phê duyệt thực đơn"* (đúng) — `Report 3.md` (dòng 6294) = *"Dish rotation: không quá 2 lần/tuần"* (sai/cũ).
- **BR-019**: xuất hiện trong `traceability_matrix.md` (mục 1.2, 2) nhưng **không tồn tại** trong `business_rules.md` (đã grep, không có kết quả) — khớp **GAP-005** đã ghi trong traceability matrix, cần traceability_matrix.md tự sửa lại tham chiếu này.
- **BR-113**: `business_rules.md` (dòng 326) = *"Thu hồi mẫu khi có sự cố"* (đúng) — `Report 3.md` (dòng 6339) = *"Sample disposal... witness"* (sai/cũ) — phát hiện khi rà soát field `SampleDisposalRecord.witnessId` trong class diagram (xem `02_Requirements/Violation_Management_Proposal.md`).

→ **Hệ quả cần lưu ý**: field `SampleDisposalRecord.witnessId` trong `class_diagram_description.md` (dòng 681) có khả năng được suy ra từ BR-113 **bản cũ của Report 3** (yêu cầu nhân chứng khi hủy mẫu) — trong khi BR-112/BR-113 **bản đã duyệt** (`business_rules.md`) chỉ yêu cầu ghi nhận "nhân viên chịu trách nhiệm", không yêu cầu nhân chứng tường minh. **TODO: team nên rà lại xem field `witnessId` này có còn cần giữ trong class diagram hay không**, vì nguồn gốc của nó (Report 3) nay đã được xác nhận là không chính xác.

→ **Tác động tới kế hoạch UI**: Screen Specification từ nay trích dẫn BR **chỉ từ `02_Requirements/00_BusinessRules/business_rules.md`** — không còn dùng Requirement Appendix trong `Report 3.md` làm căn cứ BR. Các phần khác của Report 3 (Use Case Normal Flow/Alternative Flow, mô tả wireframe Mobile Screen, Screen Authorization) **vẫn dùng được bình thường** — vấn đề chỉ nằm ở bảng Business Rules Appendix, không phải toàn bộ Report 3.

### 2.5. Khoảng trống giữa Screen Authorization và Mobile Screen wireframe — ĐÃ XỬ LÝ phần điều hướng Web

> **Cập nhật (2026-07-01)**: đã viết lại toàn bộ `03_Design/04_ScreenFlow/` theo hướng "all web + Mobile song song" — bổ sung 6 file Web mới (`08_Medical_Staff_Screen_Flow_Web.md` … `13_Guest_Common_Screen_Flow.md`), refine `01_System_Admin_Screen_Flow.md`/`02_School_Administration_Screen_Flow.md` lên độ tin cậy Cao (đối chiếu Normal Flow thật của 51 UC), và bổ sung UC-174–183 (Academic Year + Violation Management). Khoảng trống "System Admin/School Admin cần bản Web của Login/OTP/Reset/Forgot Password" nêu ở đoạn cuối mục này **đã được lấp** bằng `13_Guest_Common_Screen_Flow.md`. **Vẫn còn nguyên vấn đề khác**: Screen Specification field-by-field đầy đủ cho Web (mục 4.5) là việc khác, chưa làm — ScreenFlow chỉ định nghĩa điều hướng, không phải field/validation.

Mục dưới đây giữ nguyên làm bằng chứng vấn đề gốc đã phát hiện trước khi xử lý:

- `Report 3.md`, mục **Screen Authorization** (dòng 4832–5015) liệt kê **~140 màn hình** theo 7 vai trò (cột `x`), nhưng đây **chỉ là bảng tên màn hình + quyền truy cập, không có mô tả field/wireframe**.
- Mục **Mobile Screen** (`Report 3.md`, dòng 5079–6178, mã mục 3.3.2) chỉ đặc tả field-by-field (kèm mô tả wireframe dạng "placeholder image frame") cho **6 nhóm**: Guest and Common (15 màn), Service Caterers (16 màn), Medical Staff (10 màn), Parent (15 màn), Raw Food Supplier (7 màn), Teacher (5 màn) — tổng cộng khoảng **68 màn hình có đặc tả chi tiết**, trong khi Screen Authorization liệt kê nhiều hơn thế cho các vai trò tương ứng (VD: Service Caterers có ~35 dòng trong Screen Authorization nhưng chỉ 16 màn có wireframe chi tiết).
- **System Admin và School Administration hoàn toàn không có mục Mobile Screen tương ứng** — chỉ có tên màn hình trong Screen Authorization (~20 màn cho System Admin, ~38 màn cho School Admin), không có bất kỳ mô tả field/wireframe nào ở đâu trong Report 3.

→ Điều này khớp với ghi chú đã có sẵn trong `03_Design/04_ScreenFlow/00_Screen_Flow_Overview.md` (dòng 7): *"System Admin và School Administration... flow được suy luận theo nhóm chức năng nghiệp vụ logic... Cần review kỹ phần này vì độ tin cậy thấp hơn."*

→ **Mâu thuẫn phụ đáng chú ý**: Bảng Screen Authorization (`Report 3.md` dòng 4836–4844) gán các màn hình `Login`, `Forgot Password`, `Reset Password`, `Verify OTP`, `Notification Settings` cho **cả 7 vai trò**, bao gồm System Admin và School Admin. Nhưng các màn hình này chỉ được đặc tả wireframe trong mục **"3.3.2.1 Guest and Common Screen" — nằm dưới "3.3.2 Mobile Screen"**. Nếu System Admin/School Admin thực sự chỉ dùng Web (theo suy luận trong `04_ScreenFlow/00_Screen_Flow_Overview.md` dựa trên ADR-001), thì hai vai trò này **cũng cần một phiên bản Web tương đương** của Login/Forgot Password/OTP/Reset — nhưng **không có wireframe Web nào cho các màn hình này ở bất kỳ đâu trong tài liệu hiện có**. **TODO: đây là khoảng trống quan trọng cần bổ sung trước khi sinh Wireframe cho Web Admin Portal** (xem mục 8, mục 1).

---

## 3. Cấu trúc thư mục thực tế cho `03_Design/` — ĐÃ RECONCILE với scaffold thật (2026-07-01)

> **Cập nhật quan trọng (2026-07-01)**: bản kế hoạch trước đây đề xuất 12 thư mục con đánh số `05_DesignFoundation`…`12_DesignReview`, nhưng scaffold **thật** đã được tạo sẵn trong workspace chỉ có 8 thư mục (`00_Foundation`, `01_Diagrams`, `02_ADR`, `03_Workflow`, `04_ScreenFlow`, `05_ScreenSpecifications`, `06_Wireframes`, `07_Prototype`) — đơn giản hơn nhiều. Theo yêu cầu gộp toàn bộ tài liệu nền tảng (Design System, UI Guidelines, Component/Navigation/Naming/Accessibility/AI Guidelines...) vào **một thư mục `Foundation` duy nhất**, tài liệu này **từ nay dùng scaffold thật làm chuẩn**, không dùng cách đánh số 12 thư mục đề xuất ban đầu nữa. Mục 4, 6, 8, 9 bên dưới đã cập nhật theo cấu trúc mới.

```
03_Design/
├── 00_Foundation/                       ✅ ĐÃ VIẾT (2026-07-01) — 9 tài liệu nền tảng, gộp chung 1 thư mục
│   ├── UI_Design_Guidelines.md          (nguyên tắc chung, form/validation, GPS/camera permission, notification, offline sync BR-196–200)
│   ├── Design_System.md                 (màu thương hiệu + màu ngữ nghĩa, typography, spacing/radius/elevation — đóng TODO #1)
│   ├── Layout_Guidelines.md              (bố cục Dashboard Web, bố cục 1 cột Mobile)
│   ├── Responsive_Guidelines.md          (breakpoint Web + adaptive layout Mobile)
│   ├── Component_Guidelines.md           (ánh xạ shadcn/ui ↔ Material 3 theo nhu cầu UI)
│   ├── Navigation_Guidelines.md          (sidebar theo actor Web, bottom-nav theo actor Mobile)
│   ├── Naming_Convention.md              (quy ước tên file Screen Spec, component, token)
│   ├── Accessibility.md                  (WCAG 2.2 AA — đóng TODO #8)
│   └── AI_Design_Guidelines.md           (prompt chuẩn sinh Wireframe bằng AI, mở rộng mục 6.4 cũ)
│
├── 01_Diagrams/                        ✅ đã có — giữ nguyên (2 file lạc domain đã xoá, mục 2.2)
│   ├── 01_Class_Diagram/
│   └── 02_ERD/
│
├── 02_ADR/                             ✅ đã có — giữ nguyên (đánh số đã chốt, mục 2.1)
│
├── 03_Workflow/                        ✅ đã có — giữ nguyên
│   └── 00_Business_Workflows.md
│
├── 04_ScreenFlow/                       ✅ đã có — giữ nguyên, BỔ SUNG thêm 2 việc:
│   ├── 00_Screen_Flow_Overview.md
│   ├── 01_System_Admin_Screen_Flow.md          (🆕 cần review lại — độ tin cậy thấp)
│   ├── 02_School_Administration_Screen_Flow.md (🆕 cần review lại — độ tin cậy thấp)
│   ├── 03_Medical_Staff_Screen_Flow.md
│   ├── 04_Service_Caterers_Screen_Flow.md
│   ├── 05_Parent_Screen_Flow.md
│   ├── 06_Raw_Food_Supplier_Screen_Flow.md
│   └── 07_Teacher_Screen_Flow.md
│
├── 05_ScreenSpecifications/              🆕 CHƯA VIẾT (khối lượng lớn nhất, xem mục 4.5, TODO #2) — tổ chức theo actor + nền tảng
│   ├── Web/                              (🆕 MỞ RỘNG theo hướng "all web", mục 0 + 8b — đủ cả 7 actor)
│   │   ├── SystemAdmin/  ├── SchoolAdministration/  ├── ServiceCaterers/  ├── MedicalStaff/
│   │   ├── Parent/  ├── RawFoodSupplier/  ├── Teacher/  └── GuestCommon/
│   └── Mobile/                           (✅ GIỮ NGUYÊN phạm vi hiện có — không thu hẹp)
│       ├── GuestCommon/  ├── ServiceCaterers/  ├── MedicalStaff/  ├── Parent/  ├── RawFoodSupplier/  └── Teacher/
│
├── 06_Wireframes/                        🆕 CHƯA VIẾT — sinh sau khi Screen Specifications hoàn chỉnh (Web/ + Mobile/, cùng cấu trúc actor)
│
└── 07_Prototype/                         🆕 CHƯA VIẾT
    ├── Web_Admin_Portal_Prototype.md
    ├── Mobile_App_Prototype.md
    └── Prototype_Links.md               (link Figma/AI tool output thực tế)
```

**Chú thích ký hiệu**: ✅ đã tồn tại/đã viết · 🆕 chưa viết · ⚠️ cần xác nhận/xử lý trước.

> **Đã gộp/bỏ so với đề xuất 12 thư mục ban đầu**: `Assets` (logo/icon/QR label spec) không còn là thư mục cấp cao riêng — khi cần, đặt thành thư mục con `00_Foundation/Assets/` hoặc `07_Prototype/Assets/` tuỳ ngữ cảnh dùng, tạo khi thực sự có tài nguyên (hiện chưa có file ảnh nào để lưu). `DesignReview` (checklist đối chiếu BR/UC/MSG) gộp thành 1 file `07_Prototype/00_Review_Checklist.md` khi tới bước đó, không cần thư mục riêng.

---

## 4. Mô tả chi tiết từng thư mục

### 4.1. `00_Foundation/` — Nền tảng thiết kế (✅ ĐÃ VIẾT ĐẦY ĐỦ, 2026-07-01)

- **Mục đích**: Chốt toàn bộ quyết định nền trước khi vẽ bất kỳ màn hình nào — nền tảng công nghệ nào, màu/font nào, bố cục/breakpoint/component/điều hướng/tên gọi/accessibility nào, và cách dùng AI sinh wireframe nhất quán. Gộp chung 1 thư mục theo yêu cầu team (trước đây kế hoạch đề xuất tách 3 thư mục `DesignFoundation`/`DesignSystem`/`UIGuidelines` riêng — nay gộp lại đúng theo scaffold thật).
- **Quyết định công nghệ đã chốt (2026-07-01)**: **Web dùng shadcn/ui + Tailwind CSS** (khớp `ADR-001.md`), **Mobile dùng Material Design 3** (mặc định của Flutter, khớp `ADR-001.md`) — 2 ngôn ngữ thiết kế riêng, neo chung 1 brand seed color. Chi tiết: `Design_System.md` mục 1.
- **9 tài liệu đã viết** (xem cây thư mục mục 3 để biết nội dung tóm tắt từng file):
  1. `UI_Design_Guidelines.md` — nguyên tắc chung + pattern tương tác (form, GPS/camera, notification, **offline sync BR-196–200** — đã dời từ vị trí cũ `00_Foundation/UI_Design_Guidelines.md` mục 5).
  2. `Design_System.md` — **đóng TODO #1 [BLOCKING]**: bảng màu thương hiệu (`#0E7490` primary) + màu ngữ nghĩa (success/warning/danger/critical/info, neo theo BR-038), typography (Be Vietnam Pro), spacing/radius/elevation, cách áp dụng cụ thể cho cả shadcn (OKLCH CSS variables) và Flutter (`ColorScheme.fromSeed` + `ThemeExtension`).
  3. `Layout_Guidelines.md` — bố cục Dashboard Web (sidebar+content) và bố cục 1 cột Mobile (card-based + bottom-nav).
  4. `Responsive_Guidelines.md` — breakpoint Tailwind cho Web, adaptive layout cho Mobile — dựa trên NFR Usability gốc (Desktop ≥1280px, Mobile/Tablet ≥360px).
  5. `Component_Guidelines.md` — bảng ánh xạ 1-1 nhu cầu UI ↔ component shadcn (Web) ↔ widget Material 3 (Mobile).
  6. `Navigation_Guidelines.md` — sidebar theo actor (Web) + bottom-nav theo actor (Mobile), dựa trên `04_ScreenFlow/*.md` đã có.
  7. `Naming_Convention.md` — quy ước tên file Screen Spec (Web/Mobile cùng tên để dễ đối chiếu cặp), tên component/token, giữ namespace ADR đã chốt ở mục 2.1.
  8. `Accessibility.md` — **đóng TODO #8 [MEDIUM]**: WCAG 2.2 AA, contrast ratio cụ thể theo từng token màu, touch target 48dp Mobile / 24-40px Web.
  9. `AI_Design_Guidelines.md` — prompt chuẩn sinh Wireframe bằng AI (Stitch/Figma AI/v0), mở rộng khối "AI Generation Prompt" ở mục 6.4 cũ.
- **Quan hệ với tài liệu khác**: là input bắt buộc cho toàn bộ `05_ScreenSpecifications/`, `06_Wireframes/`; tham chiếu trực tiếp `03_Design/02_ADR/ADR-001.md`, `ADR-002.md` (Face Biometric).
- **Nguồn dữ liệu chính**: `business_rules.md` (BR-038 màu dị ứng, BR-013 giới hạn file, BR-079 GPS, BR-158 Quiet Hours, BR-196–200 offline), `class_diagram_description.md` (enum trạng thái), nghiên cứu bên ngoài (shadcn/ui theming docs, Material Design 3 ColorScheme docs, WCAG 2.2, Google Fonts Vietnamese support — trích dẫn đầy đủ ở cuối mỗi file trong `00_Foundation/`).
- **Còn mở**: cỡ font/màu chi tiết đã chốt nhưng vẫn là **đề xuất cần team duyệt** (không phải brand guideline chính thức do bên thứ 3/khách hàng cung cấp) — nếu trường/đối tác có yêu cầu brand riêng, cập nhật `Design_System.md` mục 2 trước khi áp dụng hàng loạt vào Screen Specification.

### 4.4. `04_ScreenFlow/` (đã tồn tại) — hoàn thiện thêm

- **Mục đích**: Đã có, không tạo trùng. Cần bổ sung: review lại độ chính xác của `01_System_Admin_Screen_Flow.md` và `02_School_Administration_Screen_Flow.md` (tự nhận độ tin cậy thấp), và bổ sung mục "Web version" cho luồng Guest/Common (Login, Forgot Password, OTP) nếu xác nhận System Admin/School Admin cũng cần các màn hình này trên Web (mục 2.5).
- **Quan hệ**: là input trực tiếp, bắt buộc cho `05_ScreenSpecifications` (danh sách + thứ tự điều hướng) và gián tiếp cho `06_Wireframes`.
- **Nguồn dữ liệu bổ sung nếu review**: `Report 3.md` mục Screen Authorization (dòng 4832–5015).

### 4.5. `05_ScreenSpecifications/` — Đặc tả màn hình (theo ví dụ trong yêu cầu: "← lấy từ Report 3 + Traceability Matrix")

- **Mục đích**: Tài liệu trung tâm — mỗi màn hình có một đặc tả field-by-field, validation, error state, và liên kết BR/UC/MSG. Đây là input trực tiếp để AI sinh Wireframe/Prototype.
- **Nội dung cần có**: tiêu đề màn hình, actor, platform, entry points (từ ScreenFlow), field list + kiểu dữ liệu + validation, trạng thái (loading/empty/error/success), MSG liên quan, BR liên quan, UC liên quan.
- **Quan hệ**: nhận input từ `04_ScreenFlow` (thứ tự/điều hướng) + `00_Foundation/Design_System.md` + `00_Foundation/Component_Guidelines.md` (quy ước màu/thành phần) + `00_Foundation/UI_Design_Guidelines.md` (pattern tương tác); là nguồn duy nhất cho `06_Wireframes`.
- **Nguồn dữ liệu, chia theo mức độ sẵn sàng**:
  - **Đã có field-by-field, chỉ cần tách/định dạng lại** (68 màn hình, bản Mobile): `Report 3.md` mục 3.3.2 (Guest/Common, Service Caterers, Medical Staff, Parent, Raw Food Supplier, Teacher — số dòng cụ thể xem mục 5 bên dưới), đối chiếu chéo với UC tương ứng ở mục 3.2 và dòng BR/MSG trong `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md`.
  - **Chỉ có tên màn hình, CHƯA có field** (~58 màn Web System Admin/School Admin + phần Mobile còn thiếu của Service Caterers/Medical Staff/Raw Food Supplier): phải viết mới, dựa trên mô tả Use Case Specification tương ứng ở `Report 3.md` mục 3.2 (Normal Flow/Alternative Flow/Exceptions từng UC) + business rule liên quan trong `02_Requirements/00_BusinessRules/business_rules.md` + cột BR/MSG trong `traceability_matrix.md`.
  - **🆕 Theo hướng "all web" (mục 0, 8b)**: 5 role hiện đã có bản Mobile field-by-field (Service Caterers, Medical Staff, Parent, Raw Food Supplier, Teacher) giờ cần thêm **bản Web tương ứng** cho từng UC — đây là khối lượng phát sinh thêm, không thay thế phần Mobile. **Nguyên tắc**: field list, validation, BR, MSG là **một bộ dữ liệu dùng chung** cho cả 2 bản; chỉ khác phần bố cục (layout block, component) theo nền tảng — xem template tách 2 phần ở mục 6.4. **Đây là khối lượng công việc lớn nhất của toàn bộ kế hoạch** (xem mục 8, TODO #2 đã cập nhật phạm vi).

### 4.6. `06_Wireframes/` — Wireframe (theo ví dụ: "← sinh từ Screen Specification")

- **Mục đích**: Bố cục low-fidelity của từng màn hình, đủ chi tiết để AI tool (Stitch/Figma AI) hoặc designer dựng High-fidelity.
- **Nội dung cần có**: layout block (header/nav/content/action), vị trí field theo đúng thứ tự trong Screen Specification, chú thích thành phần dùng từ `00_Foundation/Component_Guidelines.md`.
- **Quan hệ**: sinh 1-1 từ `05_ScreenSpecifications`; **không tự thêm field/nghiệp vụ mới không có trong Screen Specification**.
- **Nguồn dữ liệu**: `05_ScreenSpecifications/*` (không quay lại đọc trực tiếp Report 3 để tránh lệch giữa 2 tài liệu theo thời gian) + `00_Foundation/Component_Guidelines.md` + `00_Foundation/AI_Design_Guidelines.md` (quy trình + prompt chuẩn dùng AI sinh wireframe). Với 68 màn hình đã có mô tả wireframe gốc trong `Report 3.md` (dạng bảng "Element | Description", ví dụ dòng 5089 *"SFMS Logo | ... Shown as a placeholder image frame with an 'X' mark in wireframe"*), Wireframe mới có thể tham chiếu trực tiếp các mô tả này làm căn cứ bố cục ban đầu.

### 4.7. `07_Prototype/` — Prototype (theo ví dụ: "← sinh từ Screen Specification + Design System")

- **Mục đích**: Bản click-through có tương tác, nối các Wireframe theo đúng luồng trong `04_ScreenFlow`, dùng để demo/UAT. Cũng là nơi đặt checklist rà soát cuối (thay cho `12_DesignReview/` đề xuất cũ — gộp thành 1 file `00_Review_Checklist.md` trong thư mục này khi tới bước đó).
- **Nội dung cần có**: liên kết điều hướng giữa các Wireframe (theo mermaid flow đã có sẵn trong `04_ScreenFlow`), áp dụng Design System hoàn chỉnh (màu/font thật thay vì placeholder), link tới công cụ dựng thật (Figma/Stitch).
- **Quan hệ**: phụ thuộc `06_Wireframes` + `00_Foundation/Design_System.md` + `00_Foundation/UI_Design_Guidelines.md` (đặc biệt là trạng thái lỗi/loading phải nhất quán); checklist rà soát cuối dùng làm input cho Phase 8 UAT (`Report 1.md`).
- **Nguồn dữ liệu**: `06_Wireframes/*`, `04_ScreenFlow/*` (thứ tự điều hướng), `00_Foundation/*`, `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` (dùng lại cấu trúc ma trận UC↔BR↔MSG để mở rộng thêm cột "Screen" khi làm checklist rà soát).

> **Assets (logo/icon/QR label spec)**: không còn là thư mục cấp cao riêng (đã gộp/bỏ theo mục 3) — khi có tài nguyên thật (icon set, QR label spec từ `Report 1.md` Measure B), đặt tại `00_Foundation/Assets/` hoặc `07_Prototype/Assets/` tuỳ ngữ cảnh, tạo thư mục khi thực sự có file.

---

## 5. Ma trận nền tảng (Web / Mobile) theo nhóm màn hình

> Dựa trên `Report 3.md` mục Screen Authorization (dòng 4832–5015) đối chiếu `03_Design/04_ScreenFlow/00_Screen_Flow_Overview.md` và `ADR-001.md`. **Đây là dữ liệu tái dựng (reconstructed), không phải quyết định tường minh riêng — xem cảnh báo ở mục 2.5.**
>
> **🆕 Cập nhật theo hướng "all web + Mobile song song" (mục 0, 8b)**: cột "Web song song?" dưới đây phản ánh hướng đang áp dụng — Web giờ bắt buộc cho **mọi** actor; Mobile **giữ nguyên, không thu hẹp** cho 5 role field.

| Actor | Nền tảng chính (theo tài liệu gốc) | Web song song? (🆕 hướng mới) | Số màn hình (ước lượng) | Đã có field-by-field trong Report 3? | Vị trí nguồn |
| --- | --- | --- | --- | --- | --- |
| System Admin | Web (Next.js) — suy luận, **chưa có xác nhận tường minh** | Đã là Web | ~20 | ❌ Không | `Report 3.md` dòng 4846–4864 |
| School Administration | Web (Next.js) — suy luận, **chưa có xác nhận tường minh** | Đã là Web | ~38 | ❌ Không | `Report 3.md` dòng 4865–4903 |
| Medical Staff | Mobile (Flutter) | ✅ Có, cần viết mới bản Web | ~20 | ✅ Một phần (10/~20 màn, bản Mobile) | `Report 3.md` dòng 4943–4962, wireframe tại §3.3.2.3 |
| Service Caterers | Mobile (Flutter) | ✅ Có, cần viết mới bản Web | ~35 | ✅ Một phần (16/~35 màn, bản Mobile) | `Report 3.md` dòng 4963–4997, wireframe tại §3.3.2.2 |
| Parent | Mobile (Flutter) | ✅ Có, cần viết mới bản Web | ~15 | ✅ Gần đầy đủ (15/15 màn, bản Mobile) | `Report 3.md` dòng 4998–5015, wireframe tại §3.3.2.4 |
| Raw Food Supplier | Mobile (Flutter) | ✅ Có, cần viết mới bản Web | ~22 | ✅ Một phần (7/~22 màn, bản Mobile) | `Report 3.md` dòng 4917–4942, wireframe tại §3.3.2.5 |
| Teacher | Mobile (Flutter) | ✅ Có, cần viết mới bản Web | ~11 | ✅ Một phần (5/~11 màn, bản Mobile) | `Report 3.md` dòng 4904–4916, wireframe tại §3.3.2.6 |
| Guest & Common (Login, Register, OTP, Profile, Notification, AI Chatbot) | **Mâu thuẫn** — Report 3 xếp vào "Mobile Screen" (§3.3.2.1) nhưng Screen Authorization gán cho cả System Admin/School Admin (giả định Web-only) | ✅ Có, dùng chung cho mọi actor trên Web | 15 | ✅ Đầy đủ (nhưng chỉ bản Mobile) | `Report 3.md` dòng 5083–5281; xem mục 2.5 |

**Trước đây không có màn hình nào được xác nhận là "đa nền tảng"** (mỗi actor chỉ Web-only hoặc Mobile-only). **Theo hướng mới đang áp dụng**, 5 role field giờ có cả 2 nền tảng — không phải 1 đặc tả responsive dùng chung, mà là **2 Screen Spec riêng (Web + Mobile) cùng tham chiếu 1 bộ field/BR/MSG chung** (xem mục 4.5, 6.4). Yêu cầu "hỗ trợ cả Web Responsive và Mobile" cần hiểu ở hai lớp:
1. **Ở cấp hệ thống**: đáp ứng đầy đủ hơn trước — mọi actor đều có Web, 5 role field có thêm Mobile song song.
2. **Ở cấp một ứng dụng Web đơn lẻ có tự responsive xuống độ rộng tablet hay không**: `Report 3.md` NFR Usability (dòng 6207–6209) yêu cầu responsive breakpoint cho cả "Desktop ≥1280px" và "Mobile/Tablet ≥360px" — đã làm rõ và chốt tại `00_Foundation/Responsive_Guidelines.md` (áp dụng ngưỡng 360px cho cả 2: baseline kỹ thuật Web và baseline thiết bị Mobile).

---

## 6. Template tài liệu cho từng loại

### 6.1–6.3. Template Design Foundation / Design System / UI Guideline — ĐÃ THAY THẾ bằng tài liệu thật

> **Cập nhật (2026-07-01)**: 3 mẫu template dưới đây (Platform Strategy, Color Semantics, Interaction Pattern) đã được **viết thành tài liệu thật**, không còn là template rỗng chờ điền:
> - Platform Strategy → nội dung tương đương nằm trong `00_Foundation/Responsive_Guidelines.md` (breakpoint) + `00_Foundation/Layout_Guidelines.md` (bố cục theo nền tảng).
> - Color Semantics → `00_Foundation/Design_System.md` mục 3 (bảng màu ngữ nghĩa đầy đủ, đã có hex chính thức — không còn `#TODO`).
> - Interaction Pattern → `00_Foundation/UI_Design_Guidelines.md` mục 2–6 (form, GPS/camera, notification, offline sync, trạng thái màn hình).
>
> Giữ mục 6.1–6.3 rỗng dưới đây chỉ để lưu vết cấu trúc mẫu gốc — khi cần viết thêm 1 pattern/token mới ngoài 9 tài liệu đã có, dùng lại đúng cấu trúc bảng/heading như 3 mẫu này để nhất quán văn phong.

### 6.4. Template — Screen Specification (`05_ScreenSpecifications/.../<TênMànHình>.md`)

*(Mẫu chi tiết nhất — theo tinh thần CASE 2.0 của `06_Document/Templates/PHASE-2_ADR.md`, có thêm phần AI Generation Prompt để phục vụ sinh Wireframe/Prototype bằng AI)*

> **🆕 Tách 2 phần theo hướng "all web + Mobile song song" (mục 0, 5)**: phần **Business Logic** dùng chung cho cả Web và Mobile của cùng 1 UC (field, validation, BR, MSG không lặp lại giữa 2 nền tảng); phần **Platform Layout** viết riêng theo từng nền tảng. Với actor chỉ có 1 nền tảng (System Admin/School Admin = Web only), chỉ cần 1 khối Platform Layout.

```markdown
# Screen Spec — [Tên màn hình]

**Actor**: [vai trò] | **UC liên quan**: UC-xxx (Report 3 §3.2) | **Status**: Draft | Confirmed
**BR liên quan**: BR-xxx, BR-yyy
**MSG liên quan**: MSG-xxx (Error/Success/Warning)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | ... | Input/Button/Card/List... | ... | BR-xxx | ... |

### Trạng thái màn hình
- Loading: ...
- Empty: ...
- Error: [MSG nào hiển thị khi nào]
- Success: [MSG nào]

---

## Platform Layout — Web
**Entry/Exit points**: [từ 04_ScreenFlow bản Web]
**Bố cục**: [Header / Sidebar / Content / Action — desktop ≥1280px, co giãn xuống tablet theo `00_Foundation/Responsive_Guidelines.md` + `00_Foundation/Layout_Guidelines.md`]

## Platform Layout — Mobile
**Entry/Exit points**: [từ 04_ScreenFlow bản Mobile]
**Bố cục**: [Header / Bottom-nav / Content / Action — kế thừa mô tả wireframe gốc trong Report 3 §3.3.2 nếu có]

*(Actor chỉ có 1 nền tảng: xoá khối Platform Layout không áp dụng, không để trống.)*

---

## AI Generation Prompt ⭐ (dùng cho Stitch/Figma AI/ChatGPT — chạy riêng cho từng nền tảng)
```
Tạo wireframe cho màn hình [Tên màn hình], nền tảng [Web/Mobile], actor [vai trò].
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: [liệt kê từ bảng field ở khối Business Logic]
Áp dụng Design System: [tham chiếu 00_Foundation/Design_System.md — xem prompt chuẩn đầy đủ tại 00_Foundation/AI_Design_Guidelines.md mục 2]
Trạng thái cần thể hiện: [loading/empty/error/success]
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng [xxx-yyy] (nếu có field-by-field gốc, thường là bản Mobile)
- traceability_matrix.md (dòng ánh xạ BR/MSG)
```

### 6.5. Template — Wireframe (`06_Wireframes/.../<TênMànHình>_wireframe.md`)

```markdown
# Wireframe — [Tên màn hình]

**Nguồn**: 05_ScreenSpecifications/.../<TênMànHình>.md
**Công cụ sinh**: [Stitch | Figma AI | ChatGPT | Thủ công]
**File/Link**: [đường dẫn ảnh hoặc link Figma]

## Đối chiếu với Screen Specification
- [ ] Đủ toàn bộ field
- [ ] Đúng Design System (màu/font/spacing)
- [ ] Thể hiện đủ trạng thái Error/Empty/Loading
```

### 6.6. Template — Prototype (`07_Prototype/<TênNhómMànHình>_Prototype.md`)

```markdown
# Prototype — [Tên nhóm, VD "Mobile App — Service Caterers"]

**Link công cụ**: [Figma link]
**Phạm vi màn hình**: [danh sách, theo 04_ScreenFlow]
**Ngày dựng**: YYYY-MM-DD | **Trạng thái**: Draft cho Review | Đã Review

## Luồng điều hướng đã hiện thực trong Prototype
[copy/trích mermaid flow từ 04_ScreenFlow tương ứng, đánh dấu bước nào đã click-through được]

## Ghi chú review (điền ở 07_Prototype/00_Review_Checklist.md khi được viết)
```

### 6.7. Template — Assets manifest (`00_Foundation/Assets/00_Assets_Manifest.md`, tạo khi có tài nguyên thật)

```markdown
# Assets Manifest

| Asset | Loại | Dùng ở màn hình | Nguồn/Ghi chú |
| --- | --- | --- | --- |
| Logo SFMS | Brand | Toàn hệ thống | TODO — chưa có brand identity chính thức |
| QR Sample Label | Icon/Template | Food Sample Retention screens | Report 1.md, Measure B |
```

---

## 7. Workflow đề xuất tạo UI

```
Requirements                    00_Foundation/          ✅ ĐÃ VIẾT (2026-07-01)
(02_Requirements/,              (9 tài liệu nền tảng)
 02_ADR)                                │
     │                                  ▼
     └──────────────────────►  04_ScreenFlow (đã có, cần review 2 file Web)
                                        │
                                        ▼
                          05_ScreenSpecifications      🆕 CHƯA VIẾT — bước tiếp theo
                   (nguồn: Report 3 §3.3.2 cho 68 màn đã có field;
                    viết mới cho phần còn lại dựa trên UC §3.2 + BR + MSG;
                    áp dụng token/component/pattern từ 00_Foundation/)
                                        │
                                        ▼
                               06_Wireframes
                        (sinh bằng AI: Stitch/Figma AI/ChatGPT theo
                         00_Foundation/AI_Design_Guidelines.md,
                         từ Screen Spec + Design_System.md)
                                        │
                                        ▼
                               07_Prototype
                    (nối Wireframe theo 04_ScreenFlow, áp Design System đầy đủ,
                     kèm checklist rà soát BR/UC/MSG cuối cùng)
```

**Mô tả từng bước**:

1. **Requirements → Foundation**: **đã hoàn thành (2026-07-01)** — `00_Foundation/` có đủ 9 tài liệu (Design System, Layout, Responsive, Component, Navigation, Naming, Accessibility, AI Guidelines, UI Guidelines chung).
2. **Requirements → Screen Flow**: `04_ScreenFlow/*` đã tồn tại, dựng từ bảng Screen Authorization + mô tả field-by-field trong Report 3. Việc cần làm: review lại 2 file Web (System Admin, School Admin) vì tự nhận độ tin cậy thấp, và bổ sung xác nhận về Guest/Common screens trên Web (mục 2.5).
3. **Screen Flow → Screen Specification** (bước tiếp theo cần làm): với 68 màn hình Mobile đã có field-by-field trong Report 3 §3.3.2, công việc chủ yếu là **tách và định dạng lại** theo khối Business Logic của template 6.4, gắn thêm cột BR/UC/MSG từ `traceability_matrix.md`. Với phần Web (🆕 giờ là toàn bộ 7 actor theo mục 0, 8b — không chỉ 2 role admin) và phần Mobile còn thiếu field, phải **viết mới** dựa trên Use Case Specification (Normal Flow/Alternative Flow/Exceptions) ở Report 3 §3.2. Với 5 role đã có Business Logic từ bản Mobile, phần Web chỉ cần viết thêm khối Platform Layout — Web, không viết lại field/BR/MSG.
4. **Screen Specification → Wireframe**: dùng AI tool, prompt theo mẫu ở `00_Foundation/AI_Design_Guidelines.md` mục 2, áp Design System.
5. **Wireframe → Prototype**: nối các Wireframe theo đúng mũi tên điều hướng đã có sẵn trong `04_ScreenFlow` (mermaid flowchart) — không tự thêm luồng mới.
6. **Review**: đối chiếu ngược lại — mỗi màn hình trong Prototype phải thể hiện đủ BR/MSG được gán trong Screen Specification, dùng checklist ở `07_Prototype/00_Review_Checklist.md` (viết khi tới bước đó).

---

## 8. Tài liệu/quyết định còn thiếu — BẮT BUỘC bổ sung trước khi tạo Prototype bằng AI

Xếp theo mức độ chặn (blocking) đối với việc sinh Wireframe/Prototype bằng AI:

1. ~~**[BLOCKING] Brand identity / Design Tokens cụ thể**~~ **ĐÃ CHỐT (2026-07-01)**: bảng màu thương hiệu + màu ngữ nghĩa + font (Be Vietnam Pro) + spacing/radius đã viết đầy đủ tại `00_Foundation/Design_System.md` (là đề xuất dựa trên nghiên cứu shadcn/ui + Material Design 3 + WCAG, cần team duyệt chính thức nếu có brand guideline riêng từ đối tác/trường).
2. **[BLOCKING] Screen Specification Web — phạm vi đã MỞ RỘNG (2026-07-01)**: không chỉ ~58 màn System Admin/School Administration (Report 3 chỉ có tên màn hình, không có field/wireframe), mà theo hướng "all web + Mobile song song" (mục 0, 8b) giờ cần thêm bản Web cho cả 5 role vốn chỉ có Mobile (Service Caterers, Medical Staff, Parent, Raw Food Supplier, Teacher — field/BR/MSG dùng lại từ bản Mobile theo mục 6.4, chỉ viết mới phần Platform Layout). Đây là khối lượng lớn nhất của toàn bộ kế hoạch — **giờ là hạng mục [BLOCKING] duy nhất còn mở** sau khi TODO #1 đã chốt.
3. ~~**[BLOCKING] Xác nhận Guest/Common screens có bản Web hay không**~~ **ĐÃ CHỐT theo hướng mới**: có — Guest/Common (Login, OTP, Profile, AI Chatbot...) giờ có bản Web dùng chung cho mọi actor (mục 5, mục 3). Vẫn cần viết mới phần Platform Layout — Web.
4. ~~**[BLOCKING] Giải quyết mâu thuẫn Offline vs Online-only (mục 2.3)**~~ **ĐÃ CHỐT (2026-07-01)**: offline có giới hạn cho Mobile (evidence capture offline, xác minh an toàn bắt buộc online) — xem BR-196–200 và `00_Foundation/UI_Design_Guidelines.md` mục 5. Còn 1 TODO nhỏ chưa chốt: số giờ hiệu lực cache offline cụ thể (BR-200).
5. ~~**[HIGH] Xác nhận lại đánh số ADR (mục 2.1)**~~ **ĐÃ CHỐT (2026-07-01)**: ADR-000.md sửa lại đúng số 000 (không còn trùng ADR-002 Face Biometric); ADR-001 hệ thống và ADR-001 module (`ADR-001_User_Auth_Architecture.md`) xác nhận là 2 namespace độc lập, luôn trích dẫn ADR module bằng tên đầy đủ. Có thể trích dẫn ADR hàng loạt trong `05_ScreenSpecifications`.
6. **[HIGH] 72 màn hình Mobile còn thiếu field-by-field** (Service Caterers 19 màn, Medical Staff ~10 màn, Raw Food Supplier ~15 màn — số liệu ước lượng từ đối chiếu Screen Authorization với mục Mobile Screen chi tiết).
7. ~~**[HIGH] Chọn 1 nguồn Business Rules chính thức**~~ **ĐÃ CHỐT**: `02_Requirements/00_BusinessRules/business_rules.md` là bản đã duyệt/chính xác; Requirement Appendix trong `Report 3.md` là bản cũ/không chính xác (mục 2.4). Từ nay chỉ trích BR từ `business_rules.md`.
8. ~~**[MEDIUM] Accessibility baseline**~~ **ĐÃ CHỐT (2026-07-01)**: xem `00_Foundation/Accessibility.md` (WCAG 2.2 AA, contrast, touch target 48dp).
9. **[MEDIUM] Device Support Matrix cho Flutter** (phiên bản Android/iOS tối thiểu) — chỉ có 1 con số breakpoint (360px), chưa đủ để AI tool set canvas size chính xác cho nhiều loại thiết bị. Ghi chú tạm thời đã có ở `00_Foundation/Responsive_Guidelines.md` mục 3, còn thiếu số liệu chính thức.
10. **[MEDIUM] Ngôn ngữ hiển thị UI (i18n)**: Report 3 viết field description bằng tiếng Anh nhưng người dùng thực tế là nhân viên trường học Việt Nam (theo Report 1) — chưa có quyết định UI hiển thị tiếng Việt/song ngữ nào được ghi nhận.
11. **[LOW] Giới hạn dung lượng file ảnh không khớp** giữa BR-013 (5MB) và MSG-008 trong traceability_matrix.md ("vượt giới hạn 10MB") — cần làm rõ trước khi viết validation message trong Screen Specification liên quan tới upload ảnh.
12. **[LOW] Xác nhận 2 file `.puml` nghi ngờ không thuộc domain** (mục 2.2) — không chặn trực tiếp việc tạo UI nhưng nên dọn dẹp trước khi `01_Diagrams/` được dùng làm tham chiếu chéo cho Screen Specification.
14. **🆕 [HIGH] Viết ADR bổ sung `ADR-001.md`** cho hướng "all web + Mobile song song" (mục 0, 8b): ghi nhận chính thức 5 role field có 2 client (Web + Mobile) cùng phục vụ, không phải 1 như quyết định gốc. Cần xong trước khi `00_Foundation/Layout_Guidelines.md`/`Responsive_Guidelines.md` trích dẫn làm nguồn chính thức (mục 4.1).

---

## 8b. Ghi chú thảo luận — Chiến lược nền tảng: từ "toàn bộ lên Web" đến "all web + Mobile song song"

> Ghi lại diễn biến thảo luận với team về chiến lược nền tảng, phục vụ tham khảo khi viết ADR bổ sung `ADR-001.md` chính thức.

**Kịch bản 1 — "Toàn bộ lên Web" (đã thảo luận, KHÔNG chọn)**: thay hẳn Flutter Mobile bằng Web responsive/PWA duy nhất. Về kỹ thuật khả thi (Geolocation API, `getUserMedia`, quét QR trên trình duyệt đều thay thế được tính năng Mobile hiện tại; Face Recognition ở `ADR-002` cũng không bị ảnh hưởng vì chỉ là REST call). Nhưng rủi ro lớn: push notification iOS Safari yếu hơn app native (chỉ hoạt động từ iOS 16.4, cần "Add to Home Screen"), và PWA offline yếu hơn Flutter nhiều — trong khi yêu cầu offline vẫn đang **mâu thuẫn giữa 2 nguồn** (mục 2.3). Kịch bản này đồng nghĩa **supersede hẳn ADR-001**.

**Kịch bản 2 — "all web + Mobile song song đầy đủ" (🆕 hướng đang áp dụng cho kế hoạch, 2026-07-01)**: **không bỏ** Flutter Mobile — giữ nguyên phạm vi hiện có cho 5 role field (Service Caterers, Medical Staff, Parent, Raw Food Supplier, Teacher). **Cộng thêm** bản Web cho tất cả 7 actor (kể cả 5 role vốn chỉ có Mobile). Khác biệt cốt lõi so với Kịch bản 1: đây là **bổ sung ADR-001**, không phải thay thế — Modular Monolith + Next.js + Flutter vẫn giữ nguyên, chỉ thêm quyết định "5 role field có 2 client song song."

Hệ quả cụ thể của Kịch bản 2 lên kế hoạch tài liệu (đã cập nhật ở các mục liên quan):
- `05_ScreenSpecifications/Web/` mở rộng đủ 7 actor (mục 3).
- Ma trận nền tảng mục 5 thêm cột "Web song song?".
- Template Screen Spec (mục 6.4) tách phần Business Logic dùng chung khỏi phần Platform Layout riêng theo nền tảng, tránh phải duy trì 2 tài liệu độc lập cho cùng 1 UC.
- Khối lượng viết Screen Spec Web **tăng đáng kể** (không giảm như Kịch bản 1): giờ cần viết Web cho cả 58 màn admin (vốn đã biết) lẫn phần tương đương của 5 role field (mục 4.5, mục 8 TODO #2).
- Câu hỏi offline (mục 2.3) **vẫn treo nguyên và vẫn quan trọng** cho riêng app Mobile — không được giải quyết tự động bởi việc thêm Web, vì Mobile vẫn tồn tại song song.
- **Vẫn cần 1 ADR mới**, nhưng nhẹ hơn Kịch bản 1: bổ sung, không supersede `ADR-001.md`.

---

## 9. Tổng hợp TODO (để tiện theo dõi)

| # | TODO | Người cần xác nhận | Vị trí sẽ áp dụng |
| --- | --- | --- | --- |
| 1 | ~~Chốt bảng màu/font/logo chính thức~~ — **ĐÃ CHỐT (2026-07-01)**: xem `Design_System.md` (đề xuất, cần team duyệt nếu có brand riêng) | Team thiết kế / stakeholder | `00_Foundation/Design_System.md` |
| 2 | 🆕 Viết Screen Spec Web cho **7 actor** (không còn chỉ 2 role admin) — System Admin/School Admin viết mới hoàn toàn; 5 role field chỉ cần viết thêm Platform Layout — Web (Business Logic dùng lại từ bản Mobile) | Team BA/UI | `05_ScreenSpecifications/Web/` |
| 3 | ~~Xác nhận Guest/Common có bản Web hay không~~ — **ĐÃ CHỐT theo hướng mới**: có, dùng chung mọi actor | Team kiến trúc | `04_ScreenFlow`, `05_ScreenSpecifications` |
| 4 | ~~Quyết định Offline vs Online-only chính thức~~ — **ĐÃ CHỐT**: offline có giới hạn (BR-196–200), xem `00_Foundation/UI_Design_Guidelines.md` mục 5. Còn mở: số giờ hiệu lực cache (BR-200) | Team kiến trúc/BA | `00_Foundation/UI_Design_Guidelines.md` |
| 5 | ~~Thống nhất lại đánh số ADR cũ~~ — **ĐÃ CHỐT**: ADR-000 sửa đúng số, ADR-001 hệ thống/module xác nhận 2 namespace độc lập | Team kiến trúc | `03_Design/02_ADR/` |
| 6 | Bổ sung field-by-field cho phần Mobile còn thiếu (Service Caterers/Medical Staff/Raw Food Supplier) | Team BA/UI | `05_ScreenSpecifications/Mobile/` |
| 7 | ~~Chọn nguồn Business Rules chính thức duy nhất~~ — **ĐÃ CHỐT: `business_rules.md`** | Team BA | Toàn bộ `05_ScreenSpecifications` |
| 13 | Rà lại field `SampleDisposalRecord.witnessId` trong class diagram — khả năng bắt nguồn từ BR-113 bản cũ (Report 3), không khớp BR-112/113 bản đã duyệt | Team kiến trúc/BA | `class_diagram_description.md` |
| 14 | 🆕 Viết ADR bổ sung `ADR-001.md` cho "all web + Mobile song song" (5 role field có 2 client) | Team kiến trúc | `03_Design/02_ADR/` |
| 8 | ~~Xác nhận accessibility baseline~~ — **ĐÃ CHỐT (2026-07-01)**: xem `Accessibility.md` (WCAG 2.2 AA) | Team thiết kế | `00_Foundation/Accessibility.md` |
| 9 | Xác nhận device/OS support matrix Flutter (còn mở — chưa có nguồn dữ liệu, chỉ có ghi chú tạm thời trong `Responsive_Guidelines.md` mục 3) | Team dev mobile | `00_Foundation/Responsive_Guidelines.md` |
| 10 | Quyết định ngôn ngữ UI (Việt/song ngữ) — hiện `Naming_Convention.md` mục 4 áp dụng mặc định tiếng Việt trong lúc chờ chốt chính thức | Team BA/PO | `05_ScreenSpecifications`, `00_Foundation/Naming_Convention.md` |
| 11 | Làm rõ giới hạn file ảnh 5MB vs 10MB | Team BA | `00_Foundation/UI_Design_Guidelines.md` mục 2 |
| 12 | ~~Xác nhận 2 file `.puml` lạc domain~~ — **ĐÃ XỬ LÝ**: đã xoá | Team kiến trúc | `01_Diagrams/` |

---

*Tài liệu này được tổng hợp từ việc đọc trực tiếp: `03_Design/02_ADR/*`, `03_Design/01_Diagrams/01_Class_Diagram/class_diagram_description.md`, `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md`, `06_Document/01_Report/Report 1.md`, `06_Document/01_Report/Report 3.md` (mục 3.3, 3.4, Requirement Appendix), `03_Design/03_Workflow/00_Business_Workflows.md`, `03_Design/04_ScreenFlow/*`, `02_Requirements/00_BusinessRules/business_rules.md`, `07_implement/MF01_User_Management_Access_Control/ADR-001_User_Auth_Architecture.md`, `06_Document/Templates/PHASE-2_ADR.md`. Không có nội dung nghiệp vụ nào được tự suy diễn ngoài phạm vi các tài liệu trên; mọi khoảng trống được đánh dấu TODO.*
