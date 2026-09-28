# Wireframe — School Management & System Configuration (System Admin)

**Nguồn**: `05_ScreenSpecifications/Web/SystemAdmin/09_ActiveDeactivateSchoolDialog.md`, `10_SchoolList.md`, `11_SchoolDetail.md`, `12_UpdateSchoolScreenDialog.md`, `15_SecurityPolicies.md`, `16_AISettings.md`
**Công cụ sinh**: Thủ công (agilek/wireframer-skill — vanilla HTML/JS, hand-drawn Balsamiq style)
**File/Link**: `03_Design/06_Wireframes/Web/SystemAdmin/09_SchoolManagementAndSystemConfig.html` (single-file SPA, 6 `<div class="screen">`, điều hướng bằng `showScreen(id)`, không router)

> **Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

## Danh sách màn hình

| STT | Màn hình | ID trong SPA | File nguồn (Screen Spec) | UC liên quan |
| --- | --- | --- | --- | --- |
| 1 | School List | `school-list` | `10_SchoolList.md` | UC-140 |
| 2 | School Detail | `school-detail` | `11_SchoolDetail.md` | UC-122 |
| 3 | Update School Screen/Dialog | `update-school` | `12_UpdateSchoolScreenDialog.md` | UC-095 |
| 4 | Active/Deactivate School Dialog | `active-deactivate-school` | `09_ActiveDeactivateSchoolDialog.md` | UC-001 |
| 5 | Security Policies | `security-policies` | `15_SecurityPolicies.md` | UC-184 (mới, audit A8) |
| 6 | AI Settings (tab Gemini AI trong External Integrations) | `ai-settings` | `16_AISettings.md` | UC-040 (1 phần) |

**Ghi chú loại bỏ**: `13_CampusConfig.md` (Campus Config) **KHÔNG có wireframe trong file này** — màn hình đã CHÍNH THỨC LOẠI BỎ khỏi hệ thống theo quyết định 2026-07-01 (hệ thống không quản lý campus). Field "bán kính geofence" trước đây thuộc Campus Config nay đã xác nhận chuyển hẳn về **School Administration** (`Web/SchoolAdministration/37_SchoolConfiguration.md`), không xuất hiện ở bất kỳ màn nào trong nhóm System Admin này.

**Ghi chú điều hướng**: Sidebar liệt kê đủ các nhóm chức năng của toàn bộ actor System Admin (Quản lý Người dùng Hệ thống, Quản lý Trường học, Cấu hình Hệ thống, Giám sát & Vận hành, Báo cáo & Thống kê — theo `01_System_Admin_Screen_Flow.md` + `Navigation_Guidelines.md` mục 1.1), nhưng **chỉ 2 nhóm "Quản lý Trường học" và "Cấu hình Hệ thống" (School List + Security Policies/External Integrations) là clickable** trong file này; các nhóm/màn khác hiển thị dạng disabled (đúng ngoài phạm vi nhóm màn hình được giao). Nút "◀ Quay lại Dashboard chính" ở mỗi màn không có chức năng thật (theo yêu cầu). Sơ đồ điều hướng bám đúng mermaid trích từ `01_System_Admin_Screen_Flow.md`: `Dash → School List → School Detail → (Edit) → Update School Screen/Dialog`; `School List → (status toggle) → Active/Deactivate School Dialog`; `Dash → Security Policies`; `Dash → AI Settings (External Integrations)`.

## Checklist đối chiếu Screen Specification

### 1. School List (`10_SchoolList.md`)
- [x] Đủ field: Search (tên trường), Filter Status (Active/Inactive/Pending), bảng (Name/Code/Tax ID/Status), badge trạng thái (success/danger/warning — thể hiện bằng viền: liền = Active, viền đôi = Inactive, viền chấm = Pending), action xem chi tiết + toggle trạng thái, nút Export Excel/CSV
- [x] Grayscale/mono — badge phân biệt bằng viền/icon, không dùng màu Design System thật
- [x] Trạng thái Success thể hiện (3 dòng dữ liệu, đủ 3 trạng thái Active/Inactive/Pending); Empty/Loading chỉ mô tả ở dòng status-line (giữ 1 view)
- [x] Không thêm field ngoài bảng Business Logic (không có nút "Create School" — đúng theo Platform Layout gốc)

### 2. School Detail (`11_SchoolDetail.md`)
- [x] Đủ field: Tên trường, Địa chỉ, Mã số thuế, Email liên hệ, Toạ độ GPS (lat/long) + mini-map + badge "Đã khoá tọa độ" (coordinateLocked=true), Giờ hoạt động, Liên hệ khẩn cấp, Card thống kê tổng hợp (số lớp/học sinh/nhân viên), nút Edit, nút Export PDF
- [x] Bố cục 2 khối: thông tin cơ bản (col-span-8) cạnh card thống kê (col-span-4), khối toạ độ GPS/mini-map bên dưới — đúng Platform Layout
- [x] Trạng thái Success thể hiện đầy đủ
- [x] Không thêm field ngoài bảng Business Logic

### 3. Update School Screen/Dialog (`12_UpdateSchoolScreenDialog.md`)
- [x] Đủ field: Tên trường (input, bắt buộc), Địa chỉ (textarea, bắt buộc), Toạ độ GPS lat/long (disable vì coordinateLocked=true), nút "Mở khoá toạ độ" + textarea lý do bắt buộc (chỉ hiện sau khi bấm mở khoá), Mã số thuế, Email chính thức, Tham số vận hành (giờ hoạt động), nút Lưu
- [x] **XÁC NHẬN KHÔNG có field "bán kính geofence/Campus Config"** — đúng theo `12_UpdateSchoolScreenDialog.md` hiện tại (đã tự loại bỏ geofence, ghi chú rõ trong spec là đã chuyển về School Administration)
- [x] Bố cục Sheet trượt từ phải, nhóm theo section (Thông tin cơ bản / Toạ độ GPS / Tham số vận hành), nút Lưu cố định cuối Sheet
- [x] Trạng thái Error (MSG-007 sai mã số thuế, toạ độ ngoài khoảng) và Success (MSG-029) đều thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 4. Active/Deactivate School Dialog (`09_ActiveDeactivateSchoolDialog.md`)
- [x] Đủ field: Thông tin trường (tên/mã, read-only), Badge trạng thái trước/sau, Alert cảnh báo cascade (BR-021, liệt kê rõ Classroom/Staff/Student sẽ bị tạm dừng), nút Xác nhận (destructive), nút Huỷ (ghost)
- [x] Bố cục AlertDialog giữa màn hình, không điều hướng trang riêng
- [x] Alert cảnh báo cascade dùng token danger (mô phỏng bằng viền đậm 3px + icon cảnh báo) — rõ ràng, dễ nhận diện rủi ro cao
- [x] Trạng thái Error (MSG-001/MSG-004/MSG-005) và Success (toast "Cập nhật trạng thái trường thành công" + cascade + audit log) đều thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 5. Security Policies (`15_SecurityPolicies.md`)
- [x] Đủ field: Số lần đăng nhập sai tối đa (number, editable, mặc định 5), Thời gian khoá tài khoản (number, phút, mặc định 15), Thời gian session timeout (number, phút, mặc định 30), Chính sách độ mạnh mật khẩu (checkbox group: độ dài tối thiểu/ký tự đặc biệt/chữ hoa+số/không trùng 3 mật khẩu gần nhất), nút Lưu
- [x] Bố cục Dashboard Shell, form 1 cột nhóm theo section (Đăng nhập/Khoá tài khoản, Session, Mật khẩu)
- [x] Trạng thái Error (validation khoảng giá trị — đánh dấu [Giả định] đúng theo spec gốc vì chưa có MSG code riêng) và Success (MSG-029) đều thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 6. AI Settings (`16_AISettings.md`)
- [x] Đủ field: Tabs chọn dịch vụ (GPS/Gmail/Firebase/Gemini AI — tab Gemini AI đang mở), API Endpoint, OAuth2 Client Credentials/API Key (ẩn giá trị dạng password), nút Test Connection (secondary), nút Save (primary, disable tới khi Test Connection thành công), Badge trạng thái kết nối hiện tại (success/danger)
- [x] Implement đúng khuyến nghị spec: 1 màn "External Integrations" dùng Tabs, không tách 4 trang riêng
- [x] Trạng thái Error (AF1 Test Connection thất bại — "Invalid API key") và Success (đã kết nối, badge success) đều thể hiện
- [x] Không thêm field ngoài bảng Business Logic

## Tổng hợp các trường hợp "[Giả định]" theo màn hình (để báo Nguyễn Phương Thảo)

- **Active/Deactivate School Dialog**: MSG success dùng thông báo chung "Cập nhật trạng thái trường thành công" (Report 3 không gán MSG success riêng cho UC-001) — [Giả định]; ghi chú gốc cũng nêu nghi vấn MSG-011 bị copy-paste sai từ UC khác, wireframe chỉ dùng MSG-001 làm exception chính theo đúng quyết định trong spec.
- **School List / School Detail**: BR-022/BR-025 được Report 3 gán cho 2 UC này nhưng không mô tả trực tiếp nghiệp vụ — giữ nguyên trích dẫn theo spec gốc, đánh dấu mức độ liên quan còn cần team xác nhận (không ảnh hưởng field/hiển thị wireframe).
- **Security Policies**: phạm vi giá trị min-max cho phép chỉnh sửa 3 field number chưa được xác định trong spec gốc; MSG code riêng cho lỗi validation khoảng giá trị chưa có — wireframe minh hoạ Error state bằng ghi chú [Giả định] tại field, không tự đặt ra khoảng số cụ thể.
- **Update School Screen/Dialog**: không có mục [Giả định] mới phát sinh khi dựng wireframe — đã loại bỏ hoàn toàn field geofence theo đúng cập nhật 2026-07-01 trong spec nguồn.
- **AI Settings**: không có mục [Giả định] mới phát sinh; giữ nguyên ghi chú gốc rằng "AI Settings" là 1 tab của màn "External Integrations" chung (UC-040), không phải 1 UC độc lập.
