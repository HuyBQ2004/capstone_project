# SFMS — Screen Flow Overview (theo Actor)

> **Cập nhật lớn 2026-07-01**: toàn bộ 04_ScreenFlow đã được rà soát và viết lại theo hướng **"all web + Mobile song song"** (`00_UI_Design_Documentation_Plan.md` mục 0, 5, 8b) — mọi actor giờ có bản Web; 5 role trước đây chỉ Mobile (Medical Staff, Service Caterers, Parent, Raw Food Supplier, Teacher) **giữ nguyên Mobile, có thêm bản Web song song**. Đồng thời bổ sung UC-174–183 (Academic Year Management + Violation Management, chính thức hoá cùng ngày) vào School Administration/Medical Staff/Service Caterers. System Admin và School Administration được viết lại với độ tin cậy cao hơn (đối chiếu trực tiếp Normal Flow của từng UC thay vì chỉ suy luận theo nhóm chức năng).
>
> **Nguồn**: `06_Document\01_Report\Report 3.md` — bảng **Screen Authorization** (dòng 5057–5236, đầy đủ ~140 màn hình × 7 actor), mục **Mobile Screen** field-by-field (§3.3.2, dòng 5305–6178) cho Guest/Common + 5 role field, và **Normal Flow** của từng UC trong §3.2 (đối chiếu trực tiếp cho System Admin/School Administration — 51 UC tổng cộng). Bổ sung: `business_rules.md` §18–19 (BR-183–195) và `02_Requirements/AcademicYear_Management_Proposal.md` / `Violation_Management_Proposal.md` cho phần UC mới.
> **Hạn chế nguồn dữ liệu**: Các hình "Screens Flow for..." gốc trong Report 3 là ảnh base64 không thể trích xuất — không có dữ liệu mũi tên điều hướng gốc nào đọc được trực tiếp từ hình. Toàn bộ sơ đồ trong các file con vẫn là **dựng lại (reconstructed)**, nhưng nay bám sát Normal Flow thật của UC thay vì chỉ suy luận, nên độ tin cậy đã tăng đáng kể so với bản trước (xem cột "Độ tin cậy" bên dưới).
> Khi có file thiết kế UI gốc (Figma/Balsamiq) đầy đủ, đề nghị đối chiếu lại các sơ đồ này.

## Common Entry Flow (áp dụng cho mọi actor, cả Web lẫn Mobile)

> Đã tách thành file riêng — xem **[13_Guest_Common_Screen_Flow.md](13_Guest_Common_Screen_Flow.md)** (15 màn hình: Guest Portal, 3 màn Register, Login, Forgot Password, Verify OTP, Reset Password, Personal Profile, Edit Profile, Change Password, Notification Settings, Notification History, AI Chatbot, AI Chat History — có cả sơ đồ Mobile và **Web mới** cho từng màn). Lý do tách: file này trước đây chỉ có 1 sơ đồ Mobile chung, không đủ cho nhu cầu Web của System Admin/School Administration (khoảng trống đã ghi nhận ở `00_UI_Design_Documentation_Plan.md` mục 2.5).

## Danh sách file theo actor — 15 file (8 cũ đã refine + 6 file Web mới + 1 file ma trận phân quyền)

| Actor | Nền tảng chính (ADR-001) | File Mobile | File Web | UC mới 2026-07-01 |
| --- | --- | --- | --- | --- |
| System Admin | Web (Next.js) | — | [01_System_Admin_Screen_Flow.md](01_System_Admin_Screen_Flow.md) *(refine — độ tin cậy Cao)* | — |
| School Administration | Web (Next.js) | — | [02_School_Administration_Screen_Flow.md](02_School_Administration_Screen_Flow.md) *(refine — độ tin cậy Cao)* | UC-174–177 (Academic Year), UC-181–183 (Violation) |
| Medical Staff | Mobile (Flutter) | [03_Medical_Staff_Screen_Flow.md](03_Medical_Staff_Screen_Flow.md) *(refine)* | 🆕 [08_Medical_Staff_Screen_Flow_Web.md](08_Medical_Staff_Screen_Flow_Web.md) | UC-179, UC-180 (Violation) |
| Service Caterers | Mobile (Flutter) | [04_Service_Caterers_Screen_Flow.md](04_Service_Caterers_Screen_Flow.md) *(refine)* | 🆕 [09_Service_Caterers_Screen_Flow_Web.md](09_Service_Caterers_Screen_Flow_Web.md) | UC-179 (Violation, dialog hợp nhất — UC-178 Deprecated 11/07/2026) |
| Parent | Mobile (Flutter) | [05_Parent_Screen_Flow.md](05_Parent_Screen_Flow.md) *(refine nhẹ)* | 🆕 [10_Parent_Screen_Flow_Web.md](10_Parent_Screen_Flow_Web.md) | — |
| Raw Food Supplier | Mobile (Flutter) | [06_Raw_Food_Supplier_Screen_Flow.md](06_Raw_Food_Supplier_Screen_Flow.md) *(refine nhẹ)* | 🆕 [11_Raw_Food_Supplier_Screen_Flow_Web.md](11_Raw_Food_Supplier_Screen_Flow_Web.md) | — |
| Teacher | Mobile (Flutter) | [07_Teacher_Screen_Flow.md](07_Teacher_Screen_Flow.md) *(refine nhẹ)* | 🆕 [12_Teacher_Screen_Flow_Web.md](12_Teacher_Screen_Flow_Web.md) | — |
| Guest & Common | Cả 2 | 🆕 [13_Guest_Common_Screen_Flow.md](13_Guest_Common_Screen_Flow.md) (gộp Mobile + Web) | — | |

🆕 **[14_Screen_Authorization_Matrix.md](14_Screen_Authorization_Matrix.md)** — bảng chuẩn tổng hợp toàn bộ 190 màn hình × 7 actor (179 màn gốc từ Report 3 + 11 màn mới Academic Year/Violation), có thêm cột Nền tảng (Web/Mobile) và UC/Ghi chú. Dùng file này làm nguồn tra cứu nhanh "actor nào thấy màn nào, ở nền tảng nào" thay vì dò lại bảng Screen Authorization gốc trong Report 3.

**Nguyên tắc dùng chung dữ liệu Web/Mobile** (theo `Naming_Convention.md`, `Navigation_Guidelines.md`): mỗi cặp file Web/Mobile của cùng actor dùng **chung 100% UC/BR/MSG/field**, chỉ khác bố cục (sidebar+dashboard cho Web, bottom-nav+card cho Mobile). Bản Web **không thêm chức năng mới** ngoài phạm vi UC đã có ở Mobile — đúng theo khuyến nghị "Menu rút gọn hơn, bám sát đúng các UC đã có" của `Navigation_Guidelines.md` mục 1.1.

## Độ tin cậy reconstruction theo file (cập nhật 2026-07-01)

| File | Độ tin cậy | Cơ sở |
| --- | --- | --- |
| 01_System_Admin (Web) | **Cao** *(nâng từ Trung bình)* | Đối chiếu Normal Flow thật của 18 UC |
| 02_School_Administration (Web) | **Cao** *(nâng từ Trung bình)* | Đối chiếu Normal Flow thật của 33 UC |
| 03/04/05/06/07 (Mobile, 5 file) | Cao | Report 3 §3.3.2 field-by-field có ghi chú điều hướng rõ ràng (không đổi so với bản trước) |
| 08–12 (Web mới, 5 file) | Trung bình–Cao | Suy luận bố cục Web từ đúng UC/field đã dùng ở bản Mobile tương ứng — chưa có đặc tả Web gốc trong Report 3 vì các actor này nguyên bản chỉ Mobile |
| 13_Guest_Common (Mobile + Web) | Cao (field) / Trung bình (bố cục Web) | Field lấy trực tiếp Report 3 §3.3.2.1; bố cục Web là suy luận mới |

## Khoảng trống còn mở (chưa giải quyết, cần team xác nhận)

- **Đã xử lý (2026-07-01, audit A8 + xác nhận chủ dự án)**: `Security Policies` (System Admin) → UC-184 mới; `Partnership Approval History` → UC-106; `School Configuration` → UC-095 (bán kính geofence CONFIRMED thuộc School Administration theo BR-028). `Campus Config` (System Admin) đã **loại bỏ hoàn toàn** — hệ thống không quản lý campus.
- **Còn mở**: `Allergy Review` (School Administration) **chưa tìm được UC nền tảng chính thức** trong `traceability_matrix.md` — xem ghi chú TODO chi tiết trong file tương ứng.
- Nhóm báo cáo của School Administration (`Food Sample Retention Reports`, `Receiving Reports`, `Traceability Reports`, `System Usage Metrics`, `Partnership Reports`) nhiều khả năng là các "view" khác nhau của UC-171/172 (Reporting & Analytics) nhưng chưa được xác nhận có tách UC riêng hay không.
- Bố cục Web cho AI Chatbot (panel nổi hay trang riêng) và Register School/Supplier/Caterer (có cần bản Web ngay từ đầu vì Guest chưa có "actor cố định" hay không) cần quyết định thêm ở bước `05_ScreenSpecifications`.
