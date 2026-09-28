# Wireframe — Dashboard, Partnership & Menu Oversight (nhóm màn hình School Administration)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/SchoolAdministration/` — 8 file Screen Specification liệt kê ở bảng mục 1 (Draft — xem cảnh báo mục 5).
**Công cụ sinh**: Thủ công — theo skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), quy tắc kỹ thuật tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/SchoolAdministration/01_DashboardPartnershipMenuOversight.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/02_School_Administration_Screen_Flow.md` (nhánh Dashboard → Catering/Supplier Partnership Requests → Detail → Approve/Reject Dialog; Dashboard → Menu Management → Menu Detail → Approve/Reject Dialog). Sidebar tham chiếu `03_Design/00_Foundation/Navigation_Guidelines.md` mục 1 (cấu trúc menu 2 cấp theo actor).

---

## 1. Danh sách 7 màn hình trong file HTML (nguồn từ 8 Screen Spec)

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | School Administration Dashboard | `01_SchoolAdministrationDashboard.md` | Node gốc điều hướng |
| 02 | Catering Partnership Requests | `02_CateringPartnershipRequests.md` | Lọc mặc định Pending (UC-127) |
| 03 | Supplier Partnership Requests | `03_SupplierPartnershipRequests.md` | Lọc mặc định Pending (UC-131) |
| 04 | Service Caterers Detail | `04_ServiceCaterersDetail.md` | Kèm Approve/Reject Dialog (UC-004) |
| 05 | Supplier Detail | `05_SupplierDetail.md` | Kèm Approve/Reject Dialog (UC-007) |
| 06 | Menu Management | `06_MenuManagement.md` | Danh sách thực đơn (UC-128) |
| 07 | Menu Detail | `07_MenuDetail.md` | Kèm Approve/Reject Dialog (UC-006) |
| 38 | Verification Documents | `38_VerificationDocuments.md` | **Không phải màn điều hướng riêng** — bundled dạng panel/expander "Tài liệu xác minh" ngay trong Service Caterers Detail (#04) và Supplier Detail (#05), đúng ghi chú nguồn của chính Screen Spec 38 |

Tổng cộng: **7 `<div class="screen">`** trong file HTML (Verification Documents không tách screen riêng) + 3 modal overlay (Approve/Reject dialog cho Service Caterers Detail, Supplier Detail, Menu Detail).

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 01. School Administration Dashboard
- [x] Đủ toàn bộ field: 4 KPI card (sự cố mở, đối tác chờ duyệt, thực đơn chờ duyệt, tuân thủ hôm nay) + shortcut grid theo module + alert cảnh báo gần nhất — đúng 6 dòng bảng Business Logic, không thêm field ngoài danh sách.
- [x] Đúng Design System *(ghi chú: bản wireframe hand-drawn/grayscale theo skill wireframer, CHƯA áp Design System thật — badge success/warning/danger phân biệt bằng kiểu viền + icon doodle thay vì màu)*.
- [x] Đủ trạng thái tối thiểu — có ghi chú: **Loading** thể hiện qua dòng ghi chú text "skeleton từng card, tải song song" (không dựng skeleton động vì đây là ảnh tĩnh 1 trạng thái/màn); **Success** là trạng thái chính được render đầy đủ số liệu; **Error** không dựng riêng (theo yêu cầu bài: chỉ bắt buộc dựng alert chặn cho 2 màn Detail có rule MSG-013/MSG-051); **Empty**: không áp dụng theo đúng Screen Spec ("dashboard luôn có số liệu, kể cả 0").

### 02. Catering Partnership Requests
- [x] Đủ field: Search, Filter Status (mặc định Pending), bảng (Tên công ty/Trạng thái hợp đồng/Ngày đăng ký), badge trạng thái, action xem chi tiết — đúng 5 dòng Business Logic.
- [x] Đúng Design System *(ghi chú: grayscale hand-drawn, badge warning=viền đứt nét+icon tam giác, success=viền liền+dấu tick, danger=viền đậm+icon X)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** render đầy đủ bảng 4 dòng mẫu; **Empty** thể hiện bằng khối ghi chú text trích nguyên văn AF1 UC-127 ngay dưới bảng (không dựng bảng rỗng riêng để tránh nhân đôi màn hình ngoài phạm vi yêu cầu); **Loading/Error**: không dựng hình ảnh riêng, ghi chú MSG-004/MSG-005 áp dụng chung theo Screen Spec.

### 03. Supplier Partnership Requests
- [x] Đủ field: Search, Filter Status (mặc định Pending), bảng (Tên công ty/Trạng thái duyệt/Hiệu lực chứng nhận/Chỉ số hiệu suất gần nhất), badge "Performance Warning", badge trạng thái, action xem chi tiết — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(ghi chú: grayscale hand-drawn; badge Performance Warning dùng cùng kiểu viền đứt nét như warning, kèm text riêng để phân biệt ngữ nghĩa)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** kèm minh hoạ badge Performance Warning ở dòng 2 (AF2); **Empty** thể hiện bằng khối ghi chú text trích AF1 UC-131; **Loading/Error**: ghi chú tham chiếu MSG-004/MSG-005, không dựng hình riêng.

### 04. Service Caterers Detail (kèm Verification Documents + Approve/Reject Dialog)
- [x] Đủ field: thông tin công ty, badge trạng thái hợp đồng, panel Verification Documents (giấy phép + chứng nhận ATTP với ngày hết hạn, dùng Option B `[Image Placeholder]`), điểm site inspection, nút Approve, nút Reject + Textarea lý do bắt buộc (đánh dấu `*`) — đúng 6 dòng Business Logic của #04, cộng field #1-4 của Screen Spec 38 (bundled).
- [x] Đúng Design System *(ghi chú: nút Reject dùng `.btn-danger` viền đậm 3px thay token màu danger thật; Verification Documents dựng dạng expander mở sẵn (open) trong trang, không phải màn riêng)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** là trạng thái chính (trạng thái Pending, đủ điều kiện Approve/Reject); Approve/Reject Dialog dựng dạng modal overlay đúng ghi chú đề bài; **Loading/Empty/Error**: không dựng riêng cho màn này (Screen Spec ghi "Empty: Không áp dụng"; Loading/Error chỉ ghi chú text tham chiếu MSG).

### 05. Supplier Detail (kèm Verification Documents + Approve/Reject Dialog)
- [x] Đủ field: thông tin công ty (tên, liên hệ, tax ID), badge trạng thái duyệt, panel Verification Documents (giấy phép + chứng nhận ATTP + badge "Expiring Soon" nếu <30 ngày), lịch sử giao hàng tóm tắt (bảng), chỉ số hiệu suất hàng tháng + badge cảnh báo nếu <70% 2 tháng liên tiếp (MSG-060), nút Approve (bị disable + khối alert chặn MSG-013), nút Reject + Textarea lý do bắt buộc — đúng 7 dòng Business Logic của #05, cộng field bundled của Screen Spec 38.
- [x] Đúng Design System *(ghi chú: badge Expiring Soon = viền đứt nét; badge hiệu suất thấp = viền đậm 3px kèm text MSG-060, không dùng màu)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Error/blocking case** là trọng tâm được dựng đầy đủ — khối alert-danger cạnh nút Approve nêu rõ điều kiện chặn (MSG-013, chứng nhận ATTP hết hạn/sắp hết hạn), nút Approve `disabled` với `title` giải thích; **Success** thể hiện qua toàn bộ dữ liệu mẫu còn lại (lịch sử giao hàng, hiệu suất); **Loading/Empty**: không dựng riêng, theo đúng Screen Spec ("Empty: Không áp dụng").

### 06. Menu Management
- [x] Đủ field: Search theo tuần, Filter Status (Draft/Pending Approval/Approved/Published/Rejected), bảng (Tuần áp dụng/Trạng thái/Tên Service Caterers/Tóm tắt dinh dưỡng), badge trạng thái (info/warning/success/danger), action xem chi tiết, nút Export Excel/CSV — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(ghi chú: badge info=Draft dùng viền chấm (dotted) để phân biệt với warning/success/danger)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** render bảng 4 dòng đủ 4 trạng thái badge; **Empty** thể hiện bằng khối ghi chú text "Chưa có thực đơn nào khớp bộ lọc"; **Loading/Error**: ghi chú tham chiếu MSG-004/MSG-005, không dựng hình riêng.

### 07. Menu Detail (kèm Approve/Reject Dialog)
- [x] Đủ field: ngày phục vụ + meal slot, danh sách món ăn + nguyên liệu + trace allergen (bảng), tóm tắt dinh dưỡng (card số liệu theo nhóm tuổi), alert cảnh báo xung đột dị ứng (danger, chặn Approve), lịch sử duyệt/từ chối (bảng: timestamp/chữ ký điện tử/ghi chú), nút Approve (disable khi có cảnh báo dị ứng nghiêm trọng), nút Reject + Textarea lý do bắt buộc — đúng 7 dòng Business Logic.
- [x] Đúng Design System *(ghi chú: alert dị ứng dùng viền đậm 3px + icon X tròn thay token danger thật)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Error/blocking case** là trọng tâm được dựng đầy đủ — khối alert-danger cạnh nút Approve nêu rõ điều kiện chặn (MSG-051, xung đột dị ứng tôm với 3 học sinh lớp 2A), nút Approve `disabled` với `title` giải thích, bảng nguyên liệu highlight badge danger tại dòng allergen nghiêm trọng; **Success**: thể hiện qua phần dinh dưỡng đạt chuẩn (BR-062) và lịch sử duyệt; **Loading/Empty**: không dựng riêng, theo đúng Screen Spec ("Empty: Không áp dụng").

### 38. Verification Documents (bundled trong #04 và #05)
- [x] Đủ field: danh sách tài liệu + file preview (Option B — `[Image Placeholder]`), ngày hết hạn từng tài liệu + badge Expiring Soon (<30 ngày), nút "Xem tài liệu", nút "Tải xuống" — đúng 4 dòng Business Logic, dựng dạng expander/panel trong cả 2 màn Detail thay vì màn riêng, đúng ghi chú "Entry/Exit points" của chính Screen Spec 38.
- [x] Đúng Design System *(ghi chú: badge Expiring Soon dùng viền đứt nét, đồng nhất với cách dùng ở Supplier Detail)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** dựng đầy đủ ở cả 2 nơi bundled; **Empty** ("Đối tác chưa upload tài liệu nào") **không dựng hình** trong file này — chỉ ghi chú tại đây vì phạm vi bài giao là nhóm "Dashboard/Partnership/Menu Oversight" tập trung luồng chính (Pending có đủ hồ sơ), không lặp thêm biến thể; **Loading/Error**: không dựng riêng, tham chiếu MSG-004/MSG-005.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS, 7 `<div class="screen" id="...">`, điều hướng bằng hàm `showScreen(id)` thuần JS (không router).
- Grayscale/mono tuyệt đối; `wired-elements` nhúng qua CDN (`unpkg.com/wired-elements?module`); font `Patrick Hand` (Google Fonts); nền graph-paper (`radial-gradient` 20px); sketchy border trick áp dụng cho toàn bộ card/table-wrap/modal/button.
- Nút dùng `<button class="btn-primary/btn-secondary/btn-danger">` (mở rộng thêm biến thể `.btn-danger` viền đậm hơn cho hành động Reject, không dùng `<wired-button>`).
- Toàn bộ icon là SVG line-art doodle inline tự vẽ (không dùng emoji, không dùng react-doodle-icons vì môi trường vanilla HTML).
- Badge success/warning/danger/info phân biệt bằng kiểu viền (liền/đứt nét/đậm 3px/chấm) + icon doodle riêng (tick, tam giác cảnh báo, X tròn), không dùng màu.
- Images & Placeholders: chỉ dùng Option B — khối `[Image Placeholder]` viền đứt nét chữ "X" cho preview tài liệu (Verification Documents), không có ảnh sinh thật.
- Sidebar liệt kê đủ 9 nhóm chức năng của School Administration (Tổng quan/Đối tác/Thực đơn/Lớp-Học sinh/Sự cố/Báo cáo/Thông báo/Nhân viên/Cấu hình/Vi phạm); chỉ nhóm Dashboard/Đối tác/Thực đơn có thể click chuyển màn, các mục còn lại là `div.side-link.disabled` kèm ghi chú nhỏ "(xem file wireframe nhóm khác)".

---

## 4. Cảnh báo nguồn Draft

**Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn (báo lại Thảo — QA)

1. **07_MenuDetail.md — lệch số Business Rule (BR)**: Chính file nguồn đã tự ghi nhận nghi vấn — UC-006 trong Report 3 (dòng 2817) liệt kê BR-070/BR-074/BR-075 cho "khóa menu/ghi log rejection/thông báo", nhưng `business_rules.md` hiện hành lại gán 3 mã BR này cho nội dung hoàn toàn khác (xác minh trạng thái nhà cung cấp, xác minh vị trí nhận hàng, nguyên tắc FEFO). File nguồn đã đánh dấu "[Giả định — cần team xác nhận]" nhưng vẫn giữ nguyên trích dẫn Report 3 gốc — wireframe kế thừa nguyên trạng nghi vấn này, cần Thảo đối chiếu lại `business_rules.md` phiên bản mới nhất trước khi khoá Screen Spec.
2. **04_ServiceCaterersDetail.md và 05_SupplierDetail.md — không có field "Empty" cụ thể cho panel Verification Documents dù bundled**: Screen Spec 38 (Verification Documents) có mô tả trạng thái Empty ("Đối tác chưa upload tài liệu nào" — AF2 UC-007), nhưng 2 Screen Spec Detail (#04, #05) nơi panel này được bundled lại không nhắc lại trạng thái Empty này trong khối "Trạng thái màn hình" của chính chúng — cần Thảo xác nhận rõ: khi tài liệu thiếu, riêng phần Verification Documents hiển thị Empty trong khi phần còn lại của Detail vẫn Success, hay toàn màn Detail phải chuyển hẳn sang trạng thái cảnh báo khác.
3. **05_SupplierDetail.md — không nêu rõ ngưỡng hiển thị badge "Expiring Soon" áp dụng cho những loại tài liệu nào**: field #3 chỉ ghi "Badge Expiring Soon nếu <30 ngày" áp dụng chung cho "Giấy phép kinh doanh + Chứng nhận ATTP", nhưng ví dụ dữ liệu mẫu trong wireframe giả định badge chỉ gắn cho Chứng nhận ATTP (vì đây là chứng từ có MSG-013 liên quan) — cần Thảo xác nhận Giấy phép kinh doanh có cũng cần badge này hay không.
4. **06_MenuManagement.md — cột "Tóm tắt dinh dưỡng" trong bảng chưa định nghĩa định dạng hiển thị cụ thể**: Business Logic chỉ ghi "Tóm tắt dinh dưỡng" là 1 cột Table nhưng không nêu rõ đơn vị/nội dung tóm tắt hiển thị trong bảng danh sách (khác với card chi tiết ở Menu Detail có BR-062 rõ ràng) — wireframe tạm dùng text mẫu dạng "~620 kcal/bữa trưa, đạt chuẩn nhóm tuổi 6-10", cần Thảo xác nhận định dạng chuẩn.
