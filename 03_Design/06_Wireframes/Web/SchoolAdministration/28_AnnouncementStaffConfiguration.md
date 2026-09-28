# Wireframe — Announcement, Staff & Configuration (School Administration)

**Nguồn**: `05_ScreenSpecifications/Web/SchoolAdministration/28_SchoolAnnouncementManagement.md`, `29_CreateAnnouncement.md`, `30_UpdateAnnouncement.md`, `31_AnnouncementDetail.md`, `32_StaffManagement.md`, `33_UploadStaffList.md`, `34_InvitationStatus.md`, `37_SchoolConfiguration.md`
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), vanilla HTML/JS, single-file SPA (không React/router)
**File/Link**: `06_Wireframes/Web/SchoolAdministration/28_AnnouncementStaffConfiguration.html`

> **Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA.

## Phạm vi — 8 màn hình trong 1 file

| STT | Màn hình | `id` screen trong HTML | UC | File nguồn (Screen Spec) |
| --- | --- | --- | --- | --- |
| 28 | School Announcement Management | `sam` | UC-125 | `28_SchoolAnnouncementManagement.md` |
| 29 | Create Announcement | `cra` | UC-017 | `29_CreateAnnouncement.md` |
| 30 | Update Announcement | `upa` | UC-089 | `30_UpdateAnnouncement.md` |
| 31 | Announcement Detail | `ad` | UC-105 | `31_AnnouncementDetail.md` |
| 32 | Staff Management | `stm` | UC-144 | `32_StaffManagement.md` |
| 33 | Upload Staff List | `usl` | UC-024 (biến thể bulk) | `33_UploadStaffList.md` |
| 34 | Invitation Status | `is` | bundled (UC-024/UC-010/UC-154) | `34_InvitationStatus.md` |
| 37 | School Configuration | `sc` | UC-095 (phần School Administration) | `37_SchoolConfiguration.md` |

## Điều hướng đã hiện thực (khớp mermaid `02_School_Administration_Screen_Flow.md`)

```
Dashboard (chỉ hiển thị, disabled trong file này)
 → School Announcement Management (sam)
     → [Create] → Create Announcement (cra) → Lưu → quay lại sam (mô phỏng bằng success banner tại chỗ)
     → [Row click] → Announcement Detail (ad)
         → [Edit] → Update Announcement (upa) → Lưu → quay lại ad (banner)
 → Staff Management (stm)
     → [Upload Staff List] → Upload Staff List (usl, stepper 4 bước)
         → sau Xác nhận nhập → theo dõi tại Invitation Status (is)
     → [link "Invitation Status"] → is
 → School Configuration (sc)
```
Sidebar cố định actor **School Administration**; chỉ 3 nhóm "Thông báo / Nhân viên / Cấu hình" (và các mục con trong `<script>` `showScreen()`) là click được. Mục "Dashboard" ở trên cùng để `disabled` (không thuộc phạm vi file này).

## Đối chiếu với Screen Specification — checklist chung
- [x] Đủ 8/8 màn hình theo đúng danh sách mục 3
- [x] Vanilla HTML/JS, `<div class="screen" id="...">` + `showScreen(id)`, KHÔNG router/framework
- [x] Grayscale/mono tuyệt đối — không dùng màu Design System thật, badge phân biệt bằng viền/icon (info/success/warning/danger dùng nét liền/nét đứt + icon SVG doodle, không dùng màu nền)
- [x] `wired-elements` qua CDN (`wired-input`, `wired-textarea`, `wired-combo`, `wired-checkbox`, `wired-radio(-group)`, `wired-search-input`, `wired-slider`, `wired-divider`) — không dùng `wired-button`
- [x] Font `Patrick Hand` (+ `Caveat` cho ghi chú dev-note), nền graph-paper, sketchy border trên card/nút
- [x] Nút hành động dùng `.btn-primary` / `.btn-secondary`, không dùng `<wired-button>`
- [x] KHÔNG emoji — toàn bộ icon là SVG doodle nhúng inline
- [x] Ảnh/placeholder: không màn nào trong 8 màn yêu cầu ảnh thật theo Business Logic → không cần Option B
- [x] Bỏ qua "Initialization Routine" của skill theo yêu cầu
- [x] Text mẫu thực tế (tên trường/nhân viên/thông báo tiếng Việt hợp lý), không lorem ipsum

## Checklist riêng từng màn hình

### 28 — School Announcement Management
- [x] Search + Filter Status (Draft/Published)
- [x] Bảng: Title, Target Group, Publication Status, Publication Date
- [x] Badge trạng thái (info=Draft, success=Published) — phân biệt bằng icon + kiểu viền, không dùng màu
- [x] Nút "Create Announcement" (góc trên phải) → mở `cra`
- [x] Row click → mở `ad`
- [x] Nút "Export CSV/PDF"
- [x] Trạng thái Success thể hiện (bảng đầy đủ dữ liệu mẫu)
- [x] Không thêm field ngoài bảng Business Logic (không thêm cột/nút nào khác)

### 29 — Create Announcement
- [x] Tiêu đề (bắt buộc), Nội dung/rich text (bắt buộc)
- [x] Target Group — checkbox group (Parents/Teachers/Service Caterers)
- [x] Publication Status — radio Draft/Published, mặc định Draft, ghi chú BR-138 (publish gửi real-time ngay)
- [x] Nút "Lưu"
- [x] Trạng thái Error (EX1 thiếu field khi publish) và Success (MSG-071 Draft / MSG-047 Published theo ghi chú lệch MSG-072 trong Screen Spec) đều có nút demo trong JS
- [x] Không thêm field ngoài danh sách

### 30 — Update Announcement
- [x] Tiêu đề, Nội dung (pre-filled)
- [x] Publication Status radio
- [x] Bảng lịch sử sửa đổi rút gọn (BR-144)
- [x] Nút "Lưu"
- [x] Trạng thái Error (EX2 thiếu field bắt buộc — có alert sẵn) và Success (ghi thêm revision + publish gửi thông báo)
- [x] Không thêm field ngoài danh sách

### 31 — Announcement Detail
- [x] Title, Content hiển thị
- [x] Target Group hiển thị
- [x] Badge Publication Status (info/success)
- [x] Author, Publication Date hiển thị
- [x] Bảng lịch sử sửa đổi đầy đủ (chỉ School Administration thấy — ghi chú BR-144 ngay trong UI)
- [x] Nút "Edit" → mở `upa`
- [x] Bố cục 2 cột (nội dung 8/12, lịch sử 4/12) đúng Platform Layout
- [x] Không thêm field ngoài danh sách

### 32 — Staff Management
- [x] Search (tên/email)
- [x] Filter Role (Teacher/Medical Staff/Catering) + Filter Status (Active/Inactive/Pending)
- [x] Bảng: Tên, Email, Role, SĐT, Trạng thái
- [x] Badge trạng thái (success/warning/danger)
- [x] Nút "Upload Staff List" (góc trên phải) → mở `usl`
- [x] Action toggle Active/Inactive (icon-button) theo BR-007/BR-008
- [x] Nút "Export Excel/CSV"
- [x] Link phụ trỏ sang Invitation Status (điều hướng hợp lý theo luồng, không phải field mới)
- [x] Không thêm field ngoài danh sách

### 33 — Upload Staff List
- [x] Nút "Tải template" (đánh dấu [Giả định] đúng như Screen Spec)
- [x] Input upload file (Excel/CSV)
- [x] Nút "Tải lên & Kiểm tra" (kích hoạt bảng validation)
- [x] Bảng tóm tắt validation + số liệu tổng (Tổng/Hợp lệ/Lỗi)
- [x] Nút "Xác nhận nhập" (chỉ có ý nghĩa khi có dòng hợp lệ — mô phỏng)
- [x] Stepper ngang 4 bước (Tải template → Upload → Validate → Xác nhận) đúng Platform Layout
- [x] Trạng thái Error minh hoạ: email trùng (MSG-006), role không hợp lệ (BR-013, "System Admin" bị chặn)
- [x] Trạng thái Success: MSG-031
- [x] Không thêm field ngoài danh sách

### 34 — Invitation Status
- [x] Filter Status (Pending Verification/Active/Expired)
- [x] Bảng: Tên, Email, Ngày gửi, Trạng thái, Hạn token (48h)
- [x] Badge trạng thái (warning=Pending, success=Active, danger=Expired)
- [x] Nút "Gửi lại email xác minh" — chỉ enable khi Expired/Pending quá lâu (2 dòng đầu disabled, dòng Expired enable, đúng ràng buộc BR-011)
- [x] Trạng thái Success khi gửi lại (banner + disable nút)
- [x] Không thêm field ngoài danh sách

### 37 — School Configuration
- [x] Trần chi phí thực đơn (per suất ăn, VNĐ) — BR-080 [Giả định]
- [x] Bán kính geofence (50–500m, mặc định 200m) — BR-028, có slider + input number
- [x] Danh sách email nhận báo cáo định kỳ (multi-email) — BR-179 [Giả định]
- [x] Nút "Lưu" + banner Success (MSG-029)
- [x] KHÔNG thêm field Campus Config nào (đã loại bỏ khỏi hệ thống theo ghi chú xác nhận 2026-07-01 trong Screen Spec nguồn)
- [x] Ghi chú UC-095 + xác nhận BR-028 thuộc School Administration hiển thị dạng alert-info ngay đầu form

## Ghi chú phát hiện khi đối chiếu Screen Spec (báo lại người phụ trách trước khi qua QA/Prototype)
1. **MSG-072 / MSG-074 lệch số** (đã có trong Screen Spec 29 và 30): Create/Update Announcement dẫn chiếu MSG-072/MSG-074 nhưng 2 mã này trong `error_message.md` thuộc domain khác (special meal plan / menu update). Wireframe đã bám đúng phương án tạm thời được đề xuất trong Screen Spec (dùng MSG-047 khi Published, MSG-071 khi Draft) — cần team xác nhận chính thức mã MSG đúng trước khi chốt Prototype.
2. **BR-030 ghost-citation** (đã có trong Screen Spec 37): UC-095 Normal Flow bước 4 trích BR-030 nhưng BR-030 thực tế là "Liên kết học sinh với phụ huynh" — không liên quan School Configuration. Wireframe không thêm field nào dựa trên BR-030 (đúng theo cảnh báo trong Screen Spec), nhưng cần team làm rõ có cần 1 BR mới cho "operational rules" hay không.
3. **Cấu trúc file template bulk cho Upload Staff List** là [Giả định] (suy ra từ mẫu `Bulk Import Student Profiles`) — Normal Flow gốc UC-024 chỉ mô tả tạo 1 staff đơn lẻ qua form, chưa có đặc tả chính thức cho luồng bulk upload (định dạng cột trong file Excel/CSV, quy tắc mapping). Wireframe minh hoạ bảng validation với dữ liệu mẫu hợp lý nhưng cột thực tế cần Screen Spec xác nhận lại.
4. **Invitation Status không có UC riêng** — suy luận từ postcondition UC-024/UC-010/UC-154, cần xác nhận đây có tách UC chính thức hay giữ nguyên dạng "bundled" khi đưa vào `07_Prototype/`.
5. Toàn bộ trạng thái Loading/Empty của màn 37 (School Configuration) đang là [Giả định] trong Screen Spec nguồn (chưa có MSG code chính thức cho Error) — wireframe chỉ thể hiện Success theo đúng phạm vi được yêu cầu ưu tiên.
