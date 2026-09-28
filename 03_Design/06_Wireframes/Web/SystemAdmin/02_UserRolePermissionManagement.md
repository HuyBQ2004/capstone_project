# Wireframe — User, Role & Permission Management (nhóm màn hình System Admin)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/SystemAdmin/` — 7 file Screen Specification liệt kê ở bảng mục 1 (Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — theo skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), quy tắc kỹ thuật tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/SystemAdmin/02_UserRolePermissionManagement.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/01_System_Admin_Screen_Flow.md` (nhánh Dashboard → User Account List (UC-153) → Manage User Screen/Dialog (Create, UC-026) / Active-Deactivate User Dialog (UC-003); Dashboard → Role List (UC-130) → Manage Role Screen dùng chung Create/Update/Delete (UC-022/UC-098/UC-029); Dashboard → Manage Permission List Screen (UC-129, read-only)). Sidebar tham chiếu `03_Design/00_Foundation/Navigation_Guidelines.md` mục 1 (cấu trúc menu 2 cấp theo actor).

---

## 1. Danh sách 7 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | System Administrator Dashboard | `02_SystemAdministratorDashboard.md` | Node gốc điều hướng, chỉ shortcut/summary liên quan nhóm User/Role/Permission có thể click |
| 02 | User Account List | `04_UserAccountList.md` | UC-153, kèm nút mở Create User + toggle trạng thái |
| 03 | Manage User Screen/Dialog (Create) | `03_ManageUserScreenDialog.md` | UC-026, dựng dạng Dialog modal, mở từ User Account List |
| 04 | Active/Deactivate User Dialog | `08_ActiveDeactivateUserDialog.md` | UC-003, dựng dạng AlertDialog, 4 kịch bản demo (Admin mặc định bị chặn, Active→Inactive, Pending→Inactive, Inactive→Active) |
| 05 | Role List | `06_RoleList.md` | UC-130, badge Default/Custom phân biệt bằng viền |
| 06 | Manage Role Screen (Create/Update/Delete) | `05_ManageRoleScreen.md` | UC-022/UC-098/UC-029, dựng dạng Sheet trượt phải, 4 nút demo chuyển chế độ (Create, Update role Custom, Update role Default, Delete role Custom) + AlertDialog xác nhận xoá |
| 07 | Manage Permission List Screen | `07_ManagePermissionListScreen.md` | UC-129, read-only, không có nút Create/Edit/Delete |

Tổng cộng: **5 `<div class="screen">`** điều hướng được từ Sidebar/shortcut (Dashboard, User Account List, Role List, Manage Role Screen demo-container, Manage Permission List) + **2 modal overlay** (Manage User Dialog, Active/Deactivate User Dialog) + **1 sheet overlay** (Manage Role Screen) + **1 AlertDialog lồng trong Sheet** (xác nhận xoá Role).

Ghi chú kỹ thuật: "Manage Role Screen" được dựng thành 1 `<section class="screen" id="manage-role">` chứa khối giới thiệu + 3 nút demo, và Sheet thật (`#role-sheet-overlay`) là 1 overlay riêng dùng chung cho cả 3 chế độ Create/Update/Delete — đúng yêu cầu đề bài "1 màn Manage Role Screen duy nhất có thể chuyển đổi giữa các chế độ qua nút demo đơn giản".

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 01. System Administrator Dashboard
- [x] Đủ field theo 6 dòng Business Logic: card tổng số trường (Active/Inactive/Pending), card tổng số user theo role, card trạng thái hệ thống (badge), biểu đồ hoạt động 24h (dựng placeholder chart Option B), danh sách shortcut điều hướng, cảnh báo hệ thống gần nhất (2 alert mẫu Critical/High) — không thêm field ngoài bảng.
- [x] Đúng Design System *(ghi chú: grayscale hand-drawn theo skill wireframer, CHƯA áp Design System thật — badge trạng thái Healthy dùng viền liền + icon tick thay màu success)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Loading** thể hiện qua dòng ghi chú text "skeleton từng card, tải song song, lỗi 1 card không chặn card khác" (không dựng skeleton động vì đây là ảnh tĩnh 1 trạng thái/màn); **Success** là trạng thái chính, render đầy đủ số liệu mẫu; **Empty**: không áp dụng theo đúng Screen Spec ("dashboard luôn có ít nhất số liệu 0"); **Error**: không dựng riêng cho từng card lỗi cục bộ (ngoài phạm vi nhóm màn User/Role/Permission được giao).
- Ghi chú phạm vi: chỉ shortcut tile "User Account List", "Role List", "Manage Permission List" (đúng nhóm được giao) là click được; các shortcut khác (Quản lý Trường học, Cấu hình Hệ thống, Audit Log) hiển thị mờ, không click, ghi chú "(xem file wireframe nhóm khác)".

### 02. User Account List
- [x] Đủ field theo 10 dòng Business Logic: Search (tên/email), filter Role, filter School/Company, filter Status, bảng (Tên/Email/Role/School-Company/Trạng thái), badge trạng thái (Active=success/Pending=warning/Inactive=danger), nút "Create User Account", action toggle trạng thái, nút Export Excel/CSV, phân trang.
- [x] Đúng Design System *(ghi chú: badge success=viền liền+tick, warning=viền đứt nét+tam giác, danger=viền đậm 3px+X, không dùng màu)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** render bảng 4 dòng mẫu đủ 3 trạng thái (Active/Pending/Inactive) + 1 dòng System Admin mặc định; **Empty** thể hiện bằng khối ghi chú text "Chưa có tài khoản nào khớp bộ lọc" + gợi ý xoá filter (không dựng bảng rỗng riêng để tránh nhân đôi màn hình); **Loading/Error**: ghi chú tham chiếu MSG-004/MSG-005, không dựng hình riêng.

### 03. Manage User Screen/Dialog (Create)
- [x] Đủ đúng 6 field: Họ tên đầy đủ (input text, bắt buộc), Email (input email, bắt buộc + MSG-006 nếu trùng), Số điện thoại (input tel), Vai trò/Role (select), Trường liên kết/School (select, chỉ hiện khi Role cấp trường — JS `toggleSchoolField()` theo `onchange` của select Role), nút "Tạo tài khoản" — không thêm field ngoài bảng.
- [x] Đúng Design System *(ghi chú: lỗi field dùng text đen thay token danger thật, ghi chú MSG-006 hiển thị dưới field Email theo đúng vị trí Screen Spec yêu cầu)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** là trạng thái chính (form đầy đủ field); ghi chú text riêng cho **Error** (MSG-006 khi email trùng) và **Success** (MSG-031 Toast + đóng dialog); **Loading**: ghi chú "Button chuyển trạng thái loading khi submit, disable form" bằng text, không dựng động vì giới hạn ảnh tĩnh; **Empty**: không áp dụng (form tạo mới).
- Rule quan trọng đã dựng: field "Trường liên kết" mặc định `display:none`, chỉ hiện khi chọn Role "School Administration (cấp trường)" hoặc "Teacher (cấp trường)" qua hàm JS `toggleSchoolField()`.

### 04. Active/Deactivate User Dialog
- [x] Đủ đúng 5 field: Thông tin tài khoản (tên/email/role, read-only), Trạng thái hiện tại → mới (badge trước/sau + mũi tên), Cảnh báo hệ quả nếu Deactivate (alert warning: hủy session + gửi email), nút "Xác nhận" (destructive nếu Deactivate, bị chặn nếu target là System Admin mặc định duy nhất), nút "Huỷ".
- [x] Đúng Design System *(ghi chú: nút Xác nhận dùng `.btn-danger` viền đậm khi Deactivate, `.btn-primary` khi Activate; alert cảnh báo dùng viền đứt nét thay token warning thật)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Error/blocking case** là trọng tâm — dựng đầy đủ qua kịch bản demo "admin-default" (nút Xác nhận `disabled` + alert-danger giải thích BR-008); 3 kịch bản còn lại minh hoạ **Success** cho Active→Inactive (cảnh báo hệ quả), Pending→Inactive, và Inactive→Active (không cần cảnh báo hệ quả); **Loading/Empty**: không dựng riêng, ghi chú text tham chiếu MSG-004/MSG-005.
- Kỹ thuật: dựng bằng JS render nội dung động (`openDeactivateDialog(key)` + object `deactivateScenarios`) vào 1 modal dùng chung, để minh hoạ đủ 4 tình huống mà không phải tạo 4 modal riêng.

### 05. Role List
- [x] Đủ đúng 6 field: Search (tên role), bảng (Role Name/Description/User Count/Status), badge loại Role (Default/Custom), nút "Create New Role", action Edit (mọi role), action Delete (chỉ hiện với role Custom).
- [x] Đúng Design System *(ghi chú: badge Default = viền chấm (dotted), Custom = viền đứt nét (dashed) — theo đúng yêu cầu đề bài "badge phân biệt bằng viền/icon", không dùng info/secondary màu thật)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** render bảng 5 dòng mẫu (3 role Default: System Admin/School Administration/Teacher, 2 role Custom: 1 có 3 user gán, 1 có 0 user gán để đối chiếu 2 kịch bản xoá khác nhau ở màn 06); **Empty**: không dựng vì Screen Spec ghi "Không xảy ra thực tế (luôn có ít nhất role mặc định)"; **Loading/Error**: ghi chú tham chiếu MSG-004/MSG-005.

### 06. Manage Role Screen (Create/Update/Delete)
- [x] Đủ đúng 6 field: Tên Role (input, disable khi Update role mặc định), Mô tả (textarea), Danh sách Permission theo module (checkbox group, 3 module mẫu: User Management/Menu Management/System Monitoring, lấy đúng danh mục xuất hiện ở màn 07), nút "Lưu" (Create/Update), nút "Xoá Role" (chỉ hiện ở Update + chỉ với role Custom), Dialog xác nhận xoá (AlertDialog, chặn nếu còn user gán).
- [x] Đúng Design System *(ghi chú: nút Xoá dùng `.btn-danger` viền đậm 3px thay token destructive thật)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Error/blocking case** là trọng tâm — dựng đầy đủ 2 tình huống: (a) Update role Default → field Tên Role `disabled` + alert-danger giải thích BR-008 (không cho sửa tên); (b) AlertDialog xoá role Custom "Kiểm định viên An toàn" (đang có 3 user gán) → alert-danger hiển thị đúng số lượng user theo AF1 UC-029, nút "Xác nhận Xoá" `disabled`; **Success**: minh hoạ qua chế độ Create (mọi field trống, editable) và Update role Custom (field editable, nút Xoá hiện vì role Custom); **Loading/Empty**: không dựng riêng, ghi chú text.
- Kỹ thuật: 1 `<section class="screen" id="manage-role">` giới thiệu + 3 nút demo ("Xem chế độ Create", "Xem chế độ Update (role Custom)", "Xem chế độ Update (role Default)") gọi hàm JS `openRoleSheet(mode)` để bơm nội dung khác nhau vào cùng 1 Sheet overlay (`#role-sheet-overlay`) — đúng yêu cầu đề bài "1 màn Manage Role Screen duy nhất, chuyển đổi qua nút demo đơn giản, không cần logic phức tạp". Nút "Xoá Role" trên chính Sheet mở thêm AlertDialog `#modal-delete-role` lồng bên trong.

### 07. Manage Permission List Screen
- [x] Đủ đúng 4 field: Search (từ khoá), Filter theo Module, bảng danh sách Permission read-only (cột Permission ID/Name/Module-Category/Description), nút "Export Excel/CSV" — **không** có bất kỳ nút Create/Edit/Delete nào, đúng cảnh báo bắt buộc của Screen Spec.
- [x] Đúng Design System *(ghi chú: không dùng badge trạng thái nào cho bảng này — đúng ghi chú Screen Spec "permission không có trạng thái Active/Inactive")*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** render bảng 5 dòng mẫu permission (PERM-001…PERM-020) trải đều 3 module dùng ở màn 06 (User Management/Menu Management/System Monitoring) để nhất quán dữ liệu giữa 2 màn; **Empty**: không dựng vì Screen Spec ghi "Không xảy ra thực tế (permission là danh mục cố định)"; **Loading/Error**: ghi chú tham chiếu MSG-004/MSG-005.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS, `<div class="screen" id="...">` cho 5 màn điều hướng được (Dashboard, User Account List, Role List, Manage Role Screen container, Manage Permission List) + 2 `<div class="modal-overlay">` (Manage User Dialog, Active/Deactivate User Dialog) + 1 `<div class="sheet-overlay">` (Manage Role Screen Sheet) + 1 `<div class="modal-overlay">` lồng trong Sheet (AlertDialog xác nhận xoá Role). Điều hướng bằng hàm `showScreen(id)` thuần JS, modal/sheet dùng `openModal/closeModal/openRoleSheet/closeRoleSheet` — không router.
- Grayscale/mono tuyệt đối; `wired-elements` nhúng qua CDN (`unpkg.com/wired-elements?module`); font `Patrick Hand` (Google Fonts); nền graph-paper (`radial-gradient` 20px); sketchy border trick áp dụng cho toàn bộ card/table-wrap/modal/sheet/button.
- Nút dùng `<button class="btn-primary/btn-secondary/btn-danger">` (không dùng `<wired-button>`).
- Toàn bộ icon là SVG line-art doodle inline tự vẽ (không dùng emoji, không dùng react-doodle-icons vì môi trường vanilla HTML) — bao gồm icon nav sidebar, icon field (email/role/school), icon trạng thái, icon action (edit/delete/toggle).
- Badge Active/Success/Default/Warning/Danger/Custom phân biệt bằng kiểu viền (liền/đứt nét/đậm 3px/chấm) + icon doodle riêng (tick, tam giác cảnh báo, X tròn), không dùng màu — theo đúng ngoại lệ bắt buộc của đề bài.
- Images & Placeholders: chỉ dùng Option B — khối `[Chart Placeholder] X` cho biểu đồ hoạt động hệ thống ở Dashboard (không có ảnh sinh thật).
- Sidebar liệt kê đủ 5 nhóm chức năng của System Admin (Tổng quan, Trường học, Quản lý Người dùng Hệ thống, Cấu hình Hệ thống, Giám sát & Audit, Sao lưu) theo `Navigation_Guidelines.md`; **chỉ nhóm "Quản lý Người dùng Hệ thống"** có 3 mục click được (User Account List, Role List, Manage Permission List); các nhóm còn lại là `div.side-link.disabled` kèm ghi chú nhỏ "(xem file wireframe nhóm khác)".
- Đã dựng đủ 4 rule nghiệp vụ bắt buộc theo đề bài: (1) field "Trường liên kết" trong Manage User Dialog ẩn/hiện động theo Role qua `toggleSchoolField()`; (2) Active/Deactivate User Dialog chặn + alert nếu target là System Admin mặc định duy nhất (kịch bản `admin-default`); (3) Manage Role Screen — nút Xoá chỉ hiện với role Custom (`role-delete-btn` ẩn ở chế độ `update-default`); (4) Manage Role Screen — Update role Default disable sửa tên (`role-name-input` gắn `disabled` + alert-danger giải thích).
- Đã BỎ QUA "Initialization Routine" của SKILL.md theo đúng ngoại lệ bắt buộc của đề bài (không tạo/sửa file `agents.md`/`CLAUDE.md` gốc dự án).

---

## 4. Cảnh báo nguồn Draft

**Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn (báo lại Thảo — QA)

1. **05_ManageRoleScreen.md — MSG code cho Update/Delete Role chưa xác định**: chính Screen Spec đã tự đánh dấu "[Giả định — cần team xác nhận]" ở phần "Trạng thái màn hình → Success" — Report 3 không gán mã MSG riêng cho Update/Delete Role, chỉ có MSG-031 cho Create. Wireframe tạm dùng ghi chú text đề xuất MSG-029 ("Profile updated successfully" — mã này đã xác nhận tồn tại thật trong `error_message.md` dòng 69, nhưng về ngữ nghĩa gốc là dùng cho "Cập nhật hồ sơ" chứ không phải "Cập nhật Role" — cần Thảo xác nhận có nên tái dùng MSG-029 hay cấp mã MSG mới riêng cho Update/Delete Role).
2. **08_ActiveDeactivateUserDialog.md — không nêu rõ cơ chế xác định "System Admin mặc định duy nhất"**: Business Logic chỉ ghi field #4 "Bị chặn nếu target là tài khoản System Admin mặc định duy nhất (BR-008)" nhưng không mô tả rule kỹ hơn (VD: có phải luôn có đúng 1 tài khoản System Admin trong hệ thống, hay là "System Admin cuối cùng còn Active"? Nếu tạo thêm System Admin thứ 2 thì tài khoản gốc có còn bị khoá Deactivate không?) — wireframe tạm minh hoạ đơn giản bằng 1 kịch bản cứng (tài khoản đầu bảng User Account List), cần Thảo đối chiếu lại Normal Flow/Exception đầy đủ của UC-003 trong Report 3 để làm rõ điều kiện chính xác.
3. **03_ManageUserScreenDialog.md — lỗi định dạng SĐT chưa có MSG code cụ thể**: Screen Spec tự ghi "chưa có MSG code cụ thể cho lỗi SĐT — dùng banner cảnh báo chung" — wireframe không dựng riêng banner này (ngoài phạm vi minh hoạ trạng thái Success là chính theo yêu cầu đề bài), chỉ giữ nguyên ghi chú tại đây để Thảo lưu ý khi QA.
4. **05_ManageRoleScreen.md và 06_RoleList.md — chưa thống nhất danh mục Permission mẫu cụ thể**: cả 2 Screen Spec đều chỉ mô tả "Danh sách Permission (checkbox nhóm theo module)" và "lấy từ Manage Permission List Screen (UC-129)" nhưng không liệt kê tên permission cụ thể nào — wireframe tự đặt 3 module mẫu (User Management/Menu Management/System Monitoring) với tên permission minh hoạ hợp lý (PERM-001…PERM-020) để đủ tính thực tế theo yêu cầu "text mẫu thực tế hợp lý, không lorem ipsum"; đây là dữ liệu minh hoạ tự suy luận hợp lý từ ngữ cảnh, không phải trích nguyên văn từ UC-129, cần Thảo đối chiếu danh mục permission thật (nếu đã có ở tầng code/DB) trước khi dùng làm chuẩn cho Prototype/Hi-fi sau này.
